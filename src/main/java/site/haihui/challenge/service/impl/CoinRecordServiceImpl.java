package site.haihui.challenge.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import site.haihui.challenge.common.constant.CoinSource;
import site.haihui.challenge.common.exception.CommonException;
import site.haihui.challenge.entity.CoinRecord;
import site.haihui.challenge.mapper.CoinRecordMapper;
import site.haihui.challenge.service.ICoinRecordService;
import site.haihui.challenge.service.IRedisService;
import site.haihui.challenge.utils.RedisLock;
import site.haihui.challenge.utils.StringUtils;
import site.haihui.challenge.utils.Time;
import site.haihui.challenge.vo.CoinRecordListVO;
import site.haihui.challenge.vo.CoinRecordVO;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author linghaihui
 * @since 2022-09-21
 */
@Service
@Transactional
public class CoinRecordServiceImpl extends ServiceImpl<CoinRecordMapper, CoinRecord> implements ICoinRecordService {

    @Autowired
    private CoinRecordMapper coinRecordMapper;

    @Autowired
    private IRedisService<Integer> redisService;

    @Autowired
    private RedisLock redisLock;

    private static final String userTodayCacheKey = "%s:%s:userTodayCacheKey";

    private static final String userTotalCacheKey = "%s:userTotalCacheKey";

    private String roundReliveTimes = "%s:%s:roundReliveTimes";

    private String roundUsedRelive = "%s:%s:usedRelive";

    @Override
    public boolean operateCoin(Integer uid, Integer source, Integer roundId, Integer amount) {
        String key = uid + ":operateCoin";
        String token = redisLock.tryLock(key, 10000);
        if (StringUtils.isBlank(token)) {
            throw new CommonException("请求太频繁，请稍后再试");
        }
        String cacheKey = getTodayCacheKey(uid);
        if (redisService.sIsMember(cacheKey, source)) {
            redisLock.unlock(key, token);
            return false;
        }
        if (source.equals(CoinSource.ANSWER) || source.equals(CoinSource.SHARE)) {
            redisService.addSet(cacheKey, source);
        }
        if (source.equals(CoinSource.RELIVE)) {
            if (getRoundReliveTime(uid, roundId) <= 0) {
                throw new CommonException("本轮复活次数超限");
            }
            if (getCoin(uid) < Math.abs(CoinSource.getCoinAmount(source))) {
                redisLock.unlock(key, token);
                throw new CommonException("百科币余额不足");
            }
            String key1 = String.format(roundReliveTimes, uid, roundId);
            redisService.decrement(key1, 1);
            // 设置曾经复活过
            String key2 = String.format(roundUsedRelive, uid, roundId);
            redisService.set(key2, 1, 24 * 3600);
        }
        CoinRecord coinRecord = new CoinRecord();
        coinRecord.setUid(uid);
        if (!source.equals(CoinSource.ANSWER_CORRECT)) {
            coinRecord.setAmount(CoinSource.getCoinAmount(source));
        } else {
            coinRecord.setAmount(amount);
        }
        coinRecord.setSource(source);
        coinRecord.setCreateTime(new Date());
        coinRecordMapper.insert(coinRecord);
        Integer total = getTotalCoinAmount(uid);
        String totalCacheKey = getTotalCacheKey(uid);
        redisService.set(totalCacheKey, total);
        redisLock.unlock(key, token);
        return true;
    }

    private String getTodayCacheKey(Integer uid) {
        return String.format(userTodayCacheKey, uid,
                Time.timestampToString(Time.currentTimeSeconds().intValue(), "yyyyMMdd"));
    }

    private String getTotalCacheKey(Integer uid) {
        return String.format(userTotalCacheKey, uid);
    }

    private Integer getTotalCoinAmount(Integer uid) {
        QueryWrapper<CoinRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("uid", uid);
        List<CoinRecord> records = coinRecordMapper.selectList(queryWrapper);
        Integer total = 0;
        for (CoinRecord coinRecord : records) {
            total += coinRecord.getAmount();
        }
        return total;
    }

    @Override
    public Integer getCoin(Integer uid) {
        String totalCacheKey = getTotalCacheKey(uid);
        Integer totalCoin = redisService.get(totalCacheKey);
        return null == totalCoin ? 0 : totalCoin;
    }

    @Override
    public CoinRecordListVO getCoinRecord(Integer uid, Integer page, Integer size) {
        QueryWrapper<CoinRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("uid", uid);
        queryWrapper.orderByDesc("id");
        Page<CoinRecord> pager = new Page<>(page, size);
        List<CoinRecord> records = coinRecordMapper.selectPage(pager, queryWrapper).getRecords();
        CoinRecordListVO res = new CoinRecordListVO();
        List<CoinRecordVO> list = new ArrayList<>();
        for (CoinRecord coinRecord : records) {
            CoinRecordVO vo = new CoinRecordVO();
            vo.setAmount(coinRecord.getAmount());
            vo.setSource(coinRecord.getSource());
            vo.setSourceText(CoinSource.getSourceText(coinRecord.getSource()));
            vo.setCreateTime(Time.dateToString(coinRecord.getCreateTime()));
            list.add(vo);
        }
        res.setTotal(getCoin(uid));
        res.setList(list);
        return res;
    }

    private Integer getRoundReliveTime(Integer uid, Integer roundId) {
        String key = String.format(roundReliveTimes, uid, roundId);
        Integer times = redisService.get(key);
        return null == times ? 0 : times;
    }

    @Override
    public Integer getGrade(Integer uid) {
        Integer coin = getCoin(uid);
        if (coin == 0) {
            coin = getTotalCoinAmount(uid);
            String totalCacheKey = getTotalCacheKey(uid);
            redisService.set(totalCacheKey, coin);
        }
        if (coin > 10000 && coin < 50000) {
            return 1;
        } else if (coin >= 50000 && coin < 100000) {
            return 2;
        } else if (coin >= 100000 && coin < 200000) {
            return 3;
        } else if (coin >= 200000 && coin < 500000) {
            return 4;
        } else if (coin >= 500000 && coin < 1000000) {
            return 5;
        } else if (coin >= 1000000) {
            return 6;
        }
        return 0;
    }

    @Override
    public Integer getToUpgradeCoin(Integer uid) {
        Integer grade = getGrade(uid);
        if (grade == 6) {
            return 0;
        }
        Integer coin = getCoin(uid);
        if (grade == 5) {
            return 1000000 - coin;
        } else if (grade == 4) {
            return 500000 - coin;
        } else if (grade == 3) {
            return 200000 - coin;
        } else if (grade == 2) {
            return 100000 - coin;
        } else if (grade == 1) {
            return 50000 - coin;
        }
        return 10000 - coin;
    }
}
