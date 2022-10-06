package site.haihui.challenge.task;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import site.haihui.challenge.entity.Round;
import site.haihui.challenge.entity.User;
import site.haihui.challenge.mapper.RoundMapper;
import site.haihui.challenge.mapper.UserMapper;
import site.haihui.challenge.utils.RedisLock;

@Component
@Slf4j
public class MockChallengeTask {

    @Autowired
    private RoundMapper roundMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RedisLock redisLock;

    @Scheduled(cron = "0 0 * * * *")
    public void run() {
        if (null == redisLock.tryLock("mockChallengeTask:lock", 120 * 1000)) {
            return;
        }
        log.info("Start mockChallengeTask ...");
        List<User> users = getMockUser();
        List<Integer> exist = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            Integer j = getRandomInt(36);
            if (exist.indexOf(j) == -1) {
                exist.add(j);
                insertData(users.get(j).getId());
            }
        }
        log.info("mockChallengeTask ended.");
    }

    private void insertData(Integer uid) {
        Round round = new Round();
        Integer score = getRandomInt(2500) + 100;
        Integer correctNum = score / 220 + 1;
        round.setUid(uid);
        round.setScore(score);
        round.setIsOver(1);
        round.setTimeout(30);
        round.setTotalQuestion(correctNum);
        round.setCorrectQuestion(correctNum);
        Date now = new Date();
        round.setCreateTime(now);
        round.setUpdateTime(now);
        roundMapper.insert(round);
    }

    private List<User> getMockUser() {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.gt("id", 83);
        queryWrapper.le("id", 119);
        return userMapper.selectList(queryWrapper);
    }

    private int getRandomInt(Integer maxValue) {
        Random random = new Random();
        return random.nextInt(maxValue);
    }
}