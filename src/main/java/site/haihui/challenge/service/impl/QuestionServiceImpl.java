package site.haihui.challenge.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import site.haihui.challenge.common.auth.UserContext;
import site.haihui.challenge.common.constant.CoinSource;
import site.haihui.challenge.common.constant.Config;
import site.haihui.challenge.common.exception.BadRequestException;
import site.haihui.challenge.common.exception.CommonException;
import site.haihui.challenge.entity.Question;
import site.haihui.challenge.entity.Round;
import site.haihui.challenge.entity.RoundDetail;
import site.haihui.challenge.entity.User;
import site.haihui.challenge.entity.WrongQuestionBook;
import site.haihui.challenge.mapper.QuestionMapper;
import site.haihui.challenge.mapper.RoundDetailMapper;
import site.haihui.challenge.mapper.RoundMapper;
import site.haihui.challenge.mapper.WrongQuestionBookMapper;
import site.haihui.challenge.service.ICoinRecordService;
import site.haihui.challenge.service.IQuestionService;
import site.haihui.challenge.service.IRedisService;
import site.haihui.challenge.service.IShareService;
import site.haihui.challenge.utils.Numbers;
import site.haihui.challenge.utils.RedisLock;
import site.haihui.challenge.utils.StringUtils;
import site.haihui.challenge.utils.Time;
import site.haihui.challenge.vo.CheckAnswerVO;
import site.haihui.challenge.vo.QuestionListVO;
import site.haihui.challenge.vo.QuestionVO;
import site.haihui.challenge.vo.RankingListVO;
import site.haihui.challenge.vo.RankingVO;
import site.haihui.challenge.vo.WrongQuestionVO;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author linghaihui
 * @since 2022-09-17
 */
@Service
@Transactional
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question> implements IQuestionService {

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private RoundMapper roundMapper;

    @Autowired
    private RoundDetailMapper roundDetailMapper;

    @Autowired
    private WrongQuestionBookMapper wrongQuestionBookMapper;

    @Autowired
    private IShareService shareService;

    @Autowired
    private IRedisService<Object> redisService;

    public static final String VIPUSER = "VIPUSER";

    @Autowired
    private ICoinRecordService coinRecordService;

    @Autowired
    private RedisLock redisLock;

    @Autowired
    private IRedisService<Integer> redisServiceInt;

    private String roundReliveTimes = "%s:%s:roundReliveTimes";

    private String roundUsedRelive = "%s:%s:usedRelive";

    private String zSetRankKey = ShareServiceImpl.zSetRankKey;

    private String zSetWeekRankKey = ShareServiceImpl.zSetWeekRankKey;

    @Override
    public QuestionListVO challengeQuestion(Integer uid, Integer roundId, Integer questionId, Integer category) {
        Round round;
        List<Question> questions = new ArrayList<>();
        if (!Numbers.isBlank(roundId)) {
            round = roundMapper.selectById(roundId);
            if (null == round) {
                throw new BadRequestException("roundId错误");
            }
        } else {
            round = newRound(uid);
            if (questionId > 0) {
                Question question = getQustionById(questionId);
                if (question != null) {
                    questions.add(question);
                }
            }
            // 设置本轮3次复活机会
            setRoundReliveTimesCache(uid, round.getId());
        }
        if (questions.size() == 0) {
            questions = getRandomTenQuestions(category);
        }
        List<QuestionVO> questionVOs = new ArrayList<>();
        Integer index = 0;
        for (Question question : questions) {
            // 在简单列表不返回
            if (shareService.isQuesetionSet(uid, question.getId(), 4) && questionId == 0) {
                continue;
            }
            if (checkQuestionExist(uid, round.getId(), question.getId())) {
                continue;
            } else {
                saveRoundQuestion(uid, round.getId(), question.getId());
            }
            QuestionVO vo = new QuestionVO();
            vo.setId(question.getId());
            vo.setLevel(question.getLevel());
            vo.setContent(question.getContent());
            vo.setOptions(question.getOptions());
            vo.setCategory(question.getCategory());
            questionVOs.add(vo);
            // 缓存题目
            putQuestionCache(question);
            index += 1;
            if (index == 6) {
                break;
            }
        }
        round.setTotalQuestion(round.getTotalQuestion() + questionVOs.size());
        roundMapper.updateById(round);
        QuestionListVO res = new QuestionListVO();
        res.setRoundId(round.getId());
        res.setList(questionVOs);
        res.setQuestionNum(questionVOs.size());
        // 查询跳过次数
        Integer skipTimes = shareService.getSkipTimes(uid);
        Integer userGrade = coinRecordService.getGrade(uid);
        Integer lastSkipTimes = 0;
        if (null == skipTimes) {
            // 跳过次数为等级 + 1
            lastSkipTimes = 1 + userGrade;
        } else if (skipTimes <= userGrade) {
            lastSkipTimes = 1 + userGrade - skipTimes;
        }
        // vip福利
        if (isVipUser(uid)) {
            lastSkipTimes = 99;
        }
        res.setLastSkipChance(lastSkipTimes);
        // 查询剩余复活次数
        res.setLastReliveTimes(getRoundReliveTime(uid, round.getId()));
        // 答题超时时间
        res.setTimeout(round.getTimeout());
        // 送币
        coinRecordService.operateCoin(uid, CoinSource.ANSWER, round.getId(), 0);
        return res;
    }

    private boolean isVipUser(Integer uid) {
        return redisService.sIsMember(VIPUSER, uid);
    }

    private Round newRound(Integer uid) {
        Round round = new Round();
        round.setUid(uid);
        Date now = new Date();
        round.setCreateTime(now);
        round.setUpdateTime(now);
        round.setTotalQuestion(0);
        round.setCorrectQuestion(0);
        round.setTimeout(Config.COUNT_DOWN + coinRecordService.getGrade(uid));
        roundMapper.insert(round);
        return round;
    }

    private List<Question> getRandomTenQuestions(Integer category) {
        QueryWrapper<Question> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", 1);
        if (!Numbers.isBlank(category)) {
            queryWrapper.eq("category", category);
        }
        queryWrapper.last("order by rand() limit 100");
        return questionMapper.selectList(queryWrapper);
    }

    private boolean checkQuestionExist(Integer uid, Integer roundId, Integer questionId) {
        return getRoundDetail(uid, roundId, questionId) == null ? false : true;
    }

    private void saveRoundQuestion(Integer uid, Integer roundId, Integer questionId) {
        RoundDetail detail = new RoundDetail();
        detail.setUid(uid);
        detail.setScore(0);
        detail.setRoundId(roundId);
        detail.setQuestionId(questionId);
        Date now = new Date();
        detail.setCreateTime(now);
        detail.setUpdateTime(now);
        roundDetailMapper.insert(detail);
    }

    @Override
    public CheckAnswerVO checkAnswer(Integer uid, Integer roundId, Integer questionId, Integer countDown,
            Integer timeout, Integer answer) {
        String cacheKey = String.format("%s:checkAnswer", uid);
        String token = redisLock.tryLock(cacheKey, 10000);
        if (StringUtils.isBlank(token)) {
            throw new CommonException("请求太频繁，请稍后再试");
        }
        RoundDetail roundDetail = getRoundDetail(uid, roundId, questionId);
        Round round = roundMapper.selectById(roundId);
        CheckAnswerVO res = new CheckAnswerVO();
        if (null == roundDetail || null == round) {
            res.setResult(0);
            res.setTotalScore(0);
            res.setRightTotalQuestion(0);
            res.setRightAnswer(0);
            redisLock.unlock(cacheKey, token);
            return res;
        }
        if (round.getIsOver() == 1) {
            redisLock.unlock(cacheKey, token);
            throw new CommonException("本轮已经结束");
        }
        Date now = new Date();
        String toast = "";
        boolean isWrong = true;
        Integer grade = coinRecordService.getGrade(uid);
        if (timeout == 1) {
            res.setResult(0);
            roundDetail.setScore(0);
            roundDetail.setUpdateTime(now);
            roundDetail.setCostSecond(round.getTimeout());
            res.setTotalScore(round.getScore());
            res.setRightTotalQuestion(round.getCorrectQuestion());
            round.setIsOver(1);
        } else {
            Question question = getQustionById(questionId);
            if (question.getAnswer().equals(answer) && roundDetail.getScore() == 0
                    && roundDetail.getCostSecond() == 0) {
                res.setResult(1);
                Integer score = (int) ((10 + round.getTimeout() - Config.COUNT_DOWN)
                        * (countDown * 10 + (new Random()).nextInt(5)) / 10.0);
                if (round.getScore() < 5000 && (round.getScore() + score) >= 5000) {
                    toast = "恭喜你，本轮得分超过5000，额外送你500百科币！";
                    coinRecordService.operateCoin(uid, CoinSource.FIVE_K, roundId, 0);
                } else if (round.getScore() < 10000 && (round.getScore() + score) >= 10000) {
                    toast = "恭喜你，本轮得分超过10000，额外送你1000百科币！";
                    coinRecordService.operateCoin(uid, CoinSource.TEN_K, roundId, 0);
                }
                // 本轮没有复活过
                if (null == redisServiceInt.get(String.format(roundUsedRelive, uid, roundId))) {
                    if (round.getCorrectQuestion() == 4) {
                        toast = "恭喜你，本轮已连续答对5题，送你500百科币！";
                        coinRecordService.operateCoin(uid, CoinSource.FIVE_CONTINUE, roundId, 0);
                    } else if (round.getCorrectQuestion() == 9) {
                        toast = "恭喜你，本轮已连续答对10题，送你1000百科币！";
                        coinRecordService.operateCoin(uid, CoinSource.TEN_CONTINUE, roundId, 0);
                    } else if (round.getCorrectQuestion() == 19) {
                        toast = "恭喜你，本轮已连续答对20题，送你5000百科币！";
                        coinRecordService.operateCoin(uid, CoinSource.TWENTY_CONTINUE, roundId, 0);
                    }
                }
                round.setScore(round.getScore() + score);
                round.setUpdateTime(now);
                round.setCorrectQuestion(round.getCorrectQuestion() + 1);
                roundDetail.setScore(score);
                isWrong = false;
                // 这次回答小于27s且答对两次及以上，下次题目不再出现
                if (countDown >= (round.getTimeout() - 3) && shareService.isQuesetionSet(uid, questionId, 6)) {
                    shareService.putQuestionSet(uid, questionId, 4);
                }
                shareService.putQuestionSet(uid, questionId, 6);
                String key = shareService.getCacheKey(uid, 6);
                boolean exist = redisService.sIsMember(key, questionId);
                // 每答对100道题，获得1w百科币
                Long totalNum = redisService.sSize(key);
                if (!exist && totalNum % 100 == 0) {
                    coinRecordService.operateCoin(uid, CoinSource.CHALLENGE, roundId, 0);
                    toast = String.format("恭喜你，共答对%s道题，获得10000百科币！", totalNum);
                }
                // 添加奖励
                coinRecordService.operateCoin(uid, CoinSource.ANSWER_CORRECT, roundId, score / 5);
            } else {
                res.setResult(0);
                res.setRightAnswer(question.getAnswer());
                round.setIsOver(1);
            }
            res.setTotalScore(round.getScore());
            res.setRightTotalQuestion(round.getCorrectQuestion());
        }
        // 检查是否升级
        if (StringUtils.isBlank(toast)) {
            Integer nowGrade = coinRecordService.getGrade(uid);
            if (nowGrade > grade) {
                toast = String.format("恭喜你，成功升级到第%s级！", nowGrade);
            }
        }
        round.setUpdateTime(now);
        roundMapper.updateById(round);
        roundDetail.setAnswer(answer);
        roundDetail.setUpdateTime(now);
        Integer costSecond = round.getTimeout() - countDown;
        roundDetail.setCostSecond(costSecond <= 0 ? 1 : costSecond);
        roundDetailMapper.updateById(roundDetail);
        if (isWrong) {
            insertWrongQuestionBook(uid, questionId, answer);
        }
        // 设置用户最高分
        if (round.getScore() > shareService.getUserMaxScore(uid, 2)) {
            shareService.setRankCache(1, round);
        }
        // 设置本周最高分
        if (round.getScore() > shareService.getUserMaxScore(uid, 10)) {
            shareService.setRankCache(0, round);
        }
        // 设置排名
        Integer rank = redisService.countZSet(String.format(zSetWeekRankKey, Time.getCurrentWeekOfYear()),
                round.getScore().doubleValue() + 1, 1000000D).intValue();
        res.setRank(Numbers.isBlank(rank) ? 1 : rank + 1);
        // 增加答题总数
        shareService.incrCachedQuestionNum(uid, 1);
        redisLock.unlock(cacheKey, token);
        res.setToast(toast);
        return res;
    }

    private RoundDetail getRoundDetail(Integer uid, Integer roundId, Integer questionId) {
        QueryWrapper<RoundDetail> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("round_id", roundId);
        queryWrapper.eq("uid", uid);
        queryWrapper.eq("question_id", questionId);
        return roundDetailMapper.selectOne(queryWrapper);
    }

    @Override
    public RankingListVO getRankingList(Integer type) {
        RankingListVO res = new RankingListVO();
        Integer uid = 0;
        if (null != UserContext.getCurrentUser()) {
            uid = UserContext.getCurrentUser().getId();
        }
        RankingListVO cachedData = (RankingListVO) redisService
                .get("rankinglist:" + type);
        if (null != cachedData) {
            res = cachedData;
            res.setResetSeconds((int) (Time.getWeekEndDate().getTime() / 1000L - Time.currentTimeSeconds()));
            // 设置我的排名
            setRankExtraData(uid, type, res);
            return res;
        }
        List<Round> list;
        if (type == 0) {
            String key = String.format(zSetWeekRankKey, Time.getCurrentWeekOfYear());
            Long count = redisService.countZSet(key, 0D, 1000000D);
            if (null == count || count == 0) {
                list = getCurrentWeekRoundRankList();
                for (Round round : list) {
                    shareService.setRankCache(type, round);
                }
            } else {
                list = redisService.getZSetRevRange(key, 0, 100).stream().map(e -> (Round) e)
                        .collect(Collectors.toList());
            }
        } else {
            Long count = redisService.countZSet(zSetRankKey, 0D, 1000000D);
            if (null == count || count == 0) {
                list = getTotalRoundRankList();
                for (Round round : list) {
                    shareService.setRankCache(1, round);
                }
            } else {
                list = redisService.getZSetRevRange(zSetRankKey, 0, 100).stream().map(e -> (Round) e)
                        .filter(e -> e.getScore() >= 2000)
                        .collect(Collectors.toList());
            }
        }
        List<RankingVO> rankingVOs = new ArrayList<>();
        for (Round round : list) {
            RankingVO vo = new RankingVO();
            vo.setTotalScore(round.getScore());
            vo.setTotalQuestion(round.getCorrectQuestion());
            User user = shareService.getUser(round.getUid());
            vo.setNickname(user.getShowNickname());
            vo.setAvatarUrl(user.getShowAvatar());
            vo.setUid(user.getId());
            vo.setGrade(coinRecordService.getGrade(round.getUid()));
            rankingVOs.add(vo);
        }
        res.setList(rankingVOs);
        res.setResetSeconds((int) (Time.getWeekEndDate().getTime() / 1000L - Time.currentTimeSeconds()));
        // 设置我的排名
        setRankExtraData(uid, type, res);
        redisService.set("rankinglist:" + type, res, 7 * 24 * 3600);
        return res;
    }

    private void setRankExtraData(Integer uid, Integer type, RankingListVO res) {
        Integer rank = 0;
        Round r = null;
        if (type == 0) {
            rank = redisService.countZSet(String.format(zSetWeekRankKey, Time.getCurrentWeekOfYear()),
                    shareService.getUserMaxScore(uid, 10).doubleValue(), 1000000D).intValue();
            if (uid > 0) {
                r = shareService.getUserMaxScoreRound(uid, 12);
            }
        } else {
            rank = redisService.countZSet(zSetRankKey, shareService.getUserMaxScore(uid, 2).doubleValue(), 1000000D)
                    .intValue();
            if (uid > 0) {
                r = shareService.getUserMaxScoreRound(uid, 11);
            }
        }
        RankingVO mine = new RankingVO();
        mine.setUid(uid);
        mine.setGrade(coinRecordService.getGrade(uid));
        User user = UserContext.getCurrentUser();
        if (null != user) {
            mine.setNickname(user.getShowNickname());
            mine.setAvatarUrl(user.getShowAvatar());
        }
        if (null != r) {
            mine.setTotalScore(r.getScore());
            mine.setTotalQuestion(r.getCorrectQuestion());
            res.setRank(Numbers.isBlank(rank) ? 999 : rank);
        } else {
            mine.setTotalScore(-1);
            mine.setTotalQuestion(0);
            res.setRank(Numbers.isBlank(rank) ? 999 : rank + 1);
        }
        res.setMine(mine);
    }

    private List<Round> getCurrentWeekRoundRankList() {
        Date start = Time.getWeekStartDate();
        Date end = Time.getWeekEndDate();
        return roundMapper.getCurrentWeekRoundRankList(start, end);
    }

    private List<Round> getTotalRoundRankList() {
        return roundMapper.getCurrentWeekRoundRankList(null, null);
    }

    private void insertWrongQuestionBook(Integer uid, Integer questionId, Integer answer) {
        WrongQuestionBook questionBook = new WrongQuestionBook();
        questionBook.setUid(uid);
        questionBook.setAnswer(answer);
        questionBook.setStatus(1);
        questionBook.setQuestionId(questionId);
        Date now = new Date();
        questionBook.setCreateTime(now);
        questionBook.setUpdateTime(now);
        wrongQuestionBookMapper.insert(questionBook);
        shareService.incrCachedQuestionNum(uid, 0);
    }

    @Override
    public List<WrongQuestionVO> getWrongQuestionList(Integer uid, Integer page, Integer size) {
        QueryWrapper<WrongQuestionBook> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("uid", uid);
        queryWrapper.eq("status", 1);
        queryWrapper.orderByDesc("id");
        Page<WrongQuestionBook> pager = new Page<>(page, size);
        List<WrongQuestionBook> data = wrongQuestionBookMapper.selectPage(pager, queryWrapper).getRecords();
        List<WrongQuestionVO> res = new ArrayList<>();
        for (WrongQuestionBook wrongQuestionBook : data) {
            WrongQuestionVO vo = new WrongQuestionVO();
            vo.setId(wrongQuestionBook.getId());
            vo.setAnswer(wrongQuestionBook.getAnswer());
            vo.setCreateTime(Time.dateToString(wrongQuestionBook.getCreateTime()));
            Question question = getQustionById(wrongQuestionBook.getQuestionId());
            vo.setContent(question.getContent());
            vo.setOptions(question.getOptions());
            vo.setCorrectAnswer(question.getAnswer());
            vo.setQuestionId(question.getId());
            res.add(vo);
        }
        return res;
    }

    @Override
    public void clearQuestionCache(Integer id) {
        redisService.delete("question:" + id);
    }

    private void putQuestionCache(Question question) {
        redisService.set("question:" + question.getId(), question);
    }

    private Question getQuestionFromCache(Integer id) {
        return (Question) redisService.get("question:" + id);
    }

    private Question getQustionById(Integer questionId) {
        Question question = getQuestionFromCache(questionId);
        if (null != question) {
            return question;
        }
        return questionMapper.selectById(questionId);
    }

    @Override
    public boolean deleteWrongQuestion(Integer uid, Integer id) {
        WrongQuestionBook questionBook = wrongQuestionBookMapper.selectById(id);
        if (null == questionBook || !questionBook.getUid().equals(uid) || questionBook.getStatus() != 1) {
            return false;
        }
        questionBook.setStatus(0);
        questionBook.setUpdateTime(new Date());
        wrongQuestionBookMapper.updateById(questionBook);
        shareService.clearCache(shareService.getCacheKey(uid, 5));
        return true;
    }

    @Override
    public void hideQuestion(Integer uid, Integer id, Integer t) {
        if (t == 0) {
            shareService.putQuestionSet(uid, id, 4);
            String key = shareService.getCacheKey(uid, 9);
            Object num = redisService.get(key);
            Integer skipNum = 0;
            if (null != num) {
                skipNum = Integer.parseInt(num.toString());
            }
            if (skipNum >= 5 && !isVipUser(uid)) {
                throw new CommonException("今日隐藏次数已用完");
            }
            skipNum += 1;
            redisService.set(key, skipNum);
        } else if (t == 1) {
            Integer userGrade = coinRecordService.getGrade(uid);
            Integer skipTimes = shareService.getSkipTimes(uid);
            if (null == skipTimes || userGrade >= skipTimes) {
                shareService.incrSkipTimes(uid);
            } else {
                if (!isVipUser(uid)) {
                    throw new CommonException("今日跳过次数已用完");
                }
            }
        }
    }

    @Override
    public QuestionListVO trainingQuestions(Integer uid, Integer category) {
        if (redisService.sSize(shareService.getCacheKey(uid, 7)) >= 200
                || redisService.sSize(shareService.getCacheKey(uid, 8)) >= 400) {
            if (!isVipUser(uid)) {
                throw new CommonException("今日已超限");
            }
        }
        List<Question> questions = getRandomTenQuestions(category);
        List<QuestionVO> questionVOs = new ArrayList<>();
        Integer index = 0;
        for (Question question : questions) {
            // 在简单列表不返回
            if (shareService.isQuesetionSet(uid, question.getId(), 4)) {
                continue;
            }
            questionVOs.add(transData(question, uid));
            // 缓存题目
            putQuestionCache(question);
            shareService.putQuestionSet(uid, question.getId(), 8);
            index += 1;
            if (index == 10) {
                break;
            }
        }
        // 如果没有题目返回，返回第一个
        if (index == 0) {
            Question question = questions.get(0);
            questionVOs.add(transData(question, uid));
        }
        QuestionListVO res = new QuestionListVO();
        res.setList(questionVOs);
        res.setQuestionNum(questionVOs.size());
        return res;
    }

    private QuestionVO transData(Question question, Integer uid) {
        QuestionVO vo = new QuestionVO();
        vo.setId(question.getId());
        vo.setLevel(question.getLevel());
        vo.setContent(question.getContent());
        vo.setOptions(question.getOptions());
        vo.setCategory(question.getCategory());
        // 缓存题目
        putQuestionCache(question);
        shareService.putQuestionSet(uid, question.getId(), 8);
        return vo;
    }

    @Override
    public CheckAnswerVO checkTrainingQuestion(Integer uid, Integer questionId, Integer answer) {
        if (!shareService.isQuesetionSet(uid, questionId, 8)) {
            throw new CommonException("不在训练题目内");
        }
        if (redisService.sSize(shareService.getCacheKey(uid, 7)) >= 200 && !isVipUser(uid)) {
            throw new CommonException("今日已超限");
        }
        CheckAnswerVO res = new CheckAnswerVO();
        Question question = getQustionById(questionId);
        if (null == question) {
            throw new CommonException("题目不存在");
        }
        if (!question.getAnswer().equals(answer)) {
            res.setResult(0);
        } else {
            res.setResult(1);
        }
        res.setRightAnswer(question.getAnswer());
        // 加入已答列表
        shareService.putQuestionSet(uid, questionId, 7);
        if (redisService.sSize(shareService.getCacheKey(uid, 7)) >= 200) {
            coinRecordService.operateCoin(uid, CoinSource.TRAIN, 0, 0);
        }
        return res;
    }

    private void setRoundReliveTimesCache(Integer uid, Integer roundId) {
        String key = String.format(roundReliveTimes, uid, roundId);
        redisServiceInt.set(key, coinRecordService.getGrade(uid) + 1, 3600 * 24);
    }

    private Integer getRoundReliveTime(Integer uid, Integer roundId) {
        String key = String.format(roundReliveTimes, uid, roundId);
        Integer times = redisServiceInt.get(key);
        return null == times ? 0 : times;
    }
}
