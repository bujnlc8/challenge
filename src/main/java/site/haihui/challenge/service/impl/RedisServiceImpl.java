package site.haihui.challenge.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import site.haihui.challenge.service.IRedisService;

/**
 * 
 * RedisServiceImpl
 * 
 * @author: linghaihui
 */
@Service
public class RedisServiceImpl<T> implements IRedisService<T> {

    @Autowired
    private RedisTemplate<String, T> redisTemplate;

    @Override
    public void set(String key, T value, long time) {
        redisTemplate.opsForValue().set(key, value, time, TimeUnit.SECONDS);
    }

    @Override
    public void set(String key, T value) {
        redisTemplate.opsForValue().set(key, value);
    }

    @Override
    public T get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    @Override
    public boolean delete(String key) {
        return redisTemplate.delete(key);
    }

    @Override
    public Long delete(Collection<String> keys) {
        return redisTemplate.delete(keys);
    }

    @Override
    public boolean expire(String key, long time) {
        return redisTemplate.expire(key, time, TimeUnit.SECONDS);
    }

    @Override
    public Long getExpire(String key) {
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    @Override
    public boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    @Override
    public Long increment(String key, long delta) {
        return redisTemplate.opsForValue().increment(key, delta);
    }

    @Override
    public Long decrement(String key, long delta) {
        return redisTemplate.opsForValue().increment(key, -delta);
    }

    @Override
    public Long addSet(String key, T value) {
        return redisTemplate.opsForSet().add(key, value);
    }

    @Override
    public Set<T> getSet(String key) {
        return redisTemplate.opsForSet().members(key);
    }

    @Override
    public Long deleteSet(String key, T value) {
        return redisTemplate.opsForSet().remove(key, value);
    }

    @Override
    public T execute(RedisCallback<T> redisCallback) {
        return redisTemplate.execute(redisCallback);
    }

    @Override
    public boolean sIsMember(String key, T value) {
        return redisTemplate.opsForSet().isMember(key, value);
    }

    @Override
    public Long sSize(String key) {
        return redisTemplate.opsForSet().size(key);
    }

    @Override
    public boolean addZSet(String key, Double score, T value) {
        return redisTemplate.opsForZSet().add(key, value, score);
    }

    @Override
    public Long countZSet(String key, Double min, Double max) {
        return redisTemplate.opsForZSet().count(key, min, max);
    }

    @Override
    public List<T> getZSetRevRange(String key, Integer start, Integer stop) {
        Set<ZSetOperations.TypedTuple<T>> typedTuples = redisTemplate.opsForZSet().reverseRangeWithScores(key, start,
                stop);
        Iterator<ZSetOperations.TypedTuple<T>> iterate = typedTuples.iterator();
        List<T> res = new ArrayList<>();
        while (iterate.hasNext()) {
            ZSetOperations.TypedTuple<T> typedTuple = iterate.next();
            res.add(typedTuple.getValue());
        }
        return res;
    }

    @Override
    public Long remZSet(String key, T value) {
        return redisTemplate.opsForZSet().remove(key, value);
    }
}
