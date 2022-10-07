package site.haihui.challenge.service;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.springframework.data.redis.core.RedisCallback;

/**
 * 
 * IRedisService
 * 
 * @author: linghaihui
 */
public interface IRedisService<T> {

    void set(String key, T value);

    void set(String key, T value, long time);

    T get(String key);

    boolean delete(String key);

    Long delete(Collection<String> keys);

    boolean expire(String key, long time);

    Long getExpire(String key);

    boolean hasKey(String key);

    Long increment(String key, long delta);

    Long decrement(String key, long delta);

    Long addSet(String key, T value);

    boolean sIsMember(String key, T value);

    Set<T> getSet(String key);

    Long deleteSet(String key, T value);

    Long sSize(String key);

    T execute(RedisCallback<T> redisCallback);

    boolean addZSet(String key, Double score, T value);

    Long countZSet(String key, Double min, Double max);

    List<T> getZSetRevRange(String key, Integer start, Integer stop);
}
