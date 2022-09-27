package site.haihui.challenge.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

/**
 * 
 * Redis分布式锁
 * 
 * @author: linghaihui
 */
@Component
public class RedisLock {

    private static final String unlockScript = "if redis.call(\"get\",KEYS[1]) == ARGV[1]\n"
            + "then\n"
            + "    return redis.call(\"del\",KEYS[1])\n"
            + "else\n"
            + "    return 0\n"
            + "end";

    @Autowired
    private StringRedisTemplate redisTemplate;

    public String lock(String name, long expire, long timeout) throws InterruptedException {
        long startTime = System.currentTimeMillis();
        String token = null;
        do {
            token = tryLock(name, expire);
            if (token == null) {
                if ((System.currentTimeMillis() - startTime) > (timeout - 50))
                    break;
                Thread.sleep(50);
            }
        } while (token == null);
        return token;
    }

    public String tryLock(String name, long expire) {
        String token = UUID.randomUUID().toString();
        if (redisTemplate.opsForValue().setIfAbsent(name, token, expire, TimeUnit.MILLISECONDS)) {
            return token;
        }
        return null;
    }

    public boolean unlock(String name, String token) {
        List<String> list = new ArrayList<>();
        list.add(name);
        Long result = redisTemplate.execute(RedisScript.of(unlockScript, Long.class), list, token);
        if (null == result || result == 0) {
            return false;
        }
        return true;
    }
}
