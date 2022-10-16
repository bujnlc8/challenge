package site.haihui.challenge.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import site.haihui.challenge.common.auth.UserContext;
import site.haihui.challenge.common.constant.AppType;
import site.haihui.challenge.common.constant.CoinSource;
import site.haihui.challenge.dto.WeixinLoginDTO;
import site.haihui.challenge.dto.weixin.WeixinDecryptData;
import site.haihui.challenge.entity.Round;
import site.haihui.challenge.entity.RoundDetail;
import site.haihui.challenge.entity.User;
import site.haihui.challenge.mapper.RoundDetailMapper;
import site.haihui.challenge.mapper.RoundMapper;
import site.haihui.challenge.mapper.UserMapper;
import site.haihui.challenge.service.ICoinRecordService;
import site.haihui.challenge.service.IRedisService;
import site.haihui.challenge.service.IShareService;
import site.haihui.challenge.service.IUserService;
import site.haihui.challenge.utils.Alipay;
import site.haihui.challenge.utils.Numbers;
import site.haihui.challenge.utils.StringUtils;
import site.haihui.challenge.utils.Weixin;
import site.haihui.challenge.vo.MineVO;
import site.haihui.challenge.vo.WeixinLoginResponseVO;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author linghaihui
 * @since 2022-09-17
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private IRedisService<String> redisService;

    @Autowired
    private IRedisService<Object> redisService2;

    @Autowired
    private RoundDetailMapper roundDetailMapper;

    @Autowired
    private RoundMapper roundMapper;

    @Autowired
    private IShareService shareService;

    @Autowired
    private ICoinRecordService coinRecordService;

    @Autowired
    private Alipay alipay;

    @Override
    public WeixinLoginResponseVO loginByWeixin(WeixinLoginDTO weixinLoginDTO)
            throws JsonProcessingException {
        WeixinDecryptData weixinDecryptData;
        if (!weixinLoginDTO.getAppType().equals(AppType.ALIPAY.getAppType())) {
            weixinDecryptData = Weixin.getWeixin(AppType.getAppType(weixinLoginDTO.getAppType()))
                    .getWeixinDecryptData(
                            weixinLoginDTO.getCode(),
                            weixinLoginDTO.getEncryptedData(), weixinLoginDTO.getIv());
        } else {
            weixinDecryptData = alipay.getAlipayUserInfo(weixinLoginDTO.getCode());
        }
        log.info("DecryptData: {}", weixinDecryptData);
        Integer appType = weixinLoginDTO.getAppType();
        User user = getUserByOpenId(weixinDecryptData.getOpenId(), appType);
        Date now = new Date();
        if (null == user) {
            user = new User();
            user.setSex(weixinDecryptData.getGender());
            user.setCity(weixinDecryptData.getCity());
            user.setAvatar(weixinDecryptData.getAvatarUrl());
            user.setOpenid(weixinDecryptData.getOpenId());
            user.setCountry(weixinDecryptData.getCountry());
            user.setProvince(weixinDecryptData.getProvince());
            user.setCity(weixinDecryptData.getCity());
            user.setNickname(weixinDecryptData.getNickName());
            user.setAppType(appType);
            user.setCreateTime(now);
            user.setUpdateTime(now);
            userMapper.insert(user);
            // 加币
            coinRecordService.operateCoin(user.getId(), CoinSource.NEW_USER, 0, 0);
        } else {
            if (user.getAvatar().startsWith("https://thirdwx.qlogo.cn")
                    || user.getAvatar().startsWith("https://thirdqq.qlogo.cn")) {
                user.setAvatar(weixinDecryptData.getAvatarUrl());
                user.setNickname(weixinDecryptData.getNickName());
                userMapper.updateById(user);
            }
        }
        WeixinLoginResponseVO res = new WeixinLoginResponseVO();
        res.setUid(user.getId());
        String token = StringUtils.getMd5Str(user.getId() + ":" + now.getTime());
        redisService.set(String.format("%s:token", user.getId()), token);
        res.setToken(token);
        res.setAvatar(user.getAvatar());
        res.setNickname(user.getNickname());
        shareService.setCachedUser(user.getId(), user);
        return res;
    }

    private User getUserByOpenId(String openId, Integer appType) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("openId", openId);
        queryWrapper.eq("app_type", appType);
        queryWrapper.last("limit 1");
        return userMapper.selectOne(queryWrapper);
    }

    @Override
    public MineVO getMine(Integer uid) {
        MineVO res = new MineVO();
        User user = UserContext.getCurrentUser();
        if (null == user) {
            user = userMapper.selectById(uid);
        }
        res.setAvatar(user.getShowAvatar());
        res.setNickname(user.getShowNickname());
        // 获取最高分
        Integer maxScore = shareService.getUserMaxScore(uid, 2);
        if (Numbers.isBlank(maxScore)) {
            Round round = getMaxScoreRound(uid);
            maxScore = round == null ? 0 : round.getScore();
            shareService.setUserMaxScore(uid, maxScore, 2);
        }
        res.setMaxScore(maxScore);
        // 获取答题总数
        Integer toalNum = shareService.getCachedQuestionNum(uid, 1);
        if (null == toalNum) {
            toalNum = getUserAnswerTotalQuestion(uid);
            shareService.setCachedQuestionNum(uid, toalNum, 1);
        }
        res.setTotalQuestion(toalNum);
        // 获取错题数
        Integer wrongNum = shareService.getCachedQuestionNum(uid, 0);
        if (null == wrongNum) {
            wrongNum = shareService.getWrongQuestion(uid, 0);
            shareService.setCachedQuestionNum(uid, wrongNum, 0);
        }
        res.setWrongQuestion(wrongNum);
        // 获取有效错题数
        Integer wrongValidNum = shareService.getCachedQuestionNum(uid, 5);
        if (null == wrongValidNum) {
            wrongValidNum = shareService.getWrongQuestion(uid, 1);
            shareService.setCachedQuestionNum(uid, wrongValidNum, 5);
        }
        res.setWrongValidQuestion(wrongValidNum);
        // 获取币
        res.setCoin(coinRecordService.getCoin(uid));
        // 设置等级
        res.setGrade(coinRecordService.getGrade(uid));
        // 下一等级需要币的数量
        res.setToUpgradeCoin(Numbers.formatNumberAbbreviation(coinRecordService.getToUpgradeCoin(uid)));
        return res;
    }

    private Round getMaxScoreRound(Integer uid) {
        QueryWrapper<Round> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("uid", uid);
        queryWrapper.orderByDesc("score");
        queryWrapper.last("limit 1");
        return roundMapper.selectOne(queryWrapper);
    }

    private Integer getUserAnswerTotalQuestion(Integer uid) {
        QueryWrapper<RoundDetail> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("uid", uid);
        queryWrapper.gt("cost_second", 0);
        queryWrapper.select("count(1) as c");
        List<Map<String, Object>> list = roundDetailMapper.selectMaps(queryWrapper);
        if (list.size() == 0) {
            return 0;
        }
        Map<String, Object> map = list.get(0);
        if (null != map.get("c")) {
            return Integer.valueOf(String.valueOf(map.get("c")));
        }
        return 0;
    }

    @Override
    public void updateAvatarNickname(Integer uid, String avatar, String nickname) {
        User user = UserContext.getCurrentUser();
        if (null == user) {
            user = userMapper.selectById(uid);
        }
        boolean update = false;
        if (!StringUtils.isBlank(nickname)) {
            user.setNickname(nickname);
            update = true;
        } else if (!StringUtils.isBlank(avatar)) {
            user.setAvatar(avatar);
            update = true;
        }
        if (update) {
            user.setUpdateTime(new Date());
            userMapper.updateById(user);
        }
        shareService.setCachedUser(user.getId(), user);
        // 清除排行榜缓存
        redisService2.delete(StringUtils.makeRankListCacheKey(1));
        redisService2.delete(StringUtils.makeRankListCacheKey(0));
    }
}
