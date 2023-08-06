package com.juzi.user.utils;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class RedisCommonProcessor {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public Object get(String key) {
        validateKey(key);
        return redisTemplate.opsForValue().get(key);
    }

    public void set(String key, Object value) {
        validateKey(key);
        redisTemplate.opsForValue().set(key, value);
    }

    public void set(String key, Object value, long time, TimeUnit timeUnit) {
        validateKey(key);
        if (time > 0) {
            redisTemplate.opsForValue().set(key, value, time, timeUnit);
        } else {
            set(key, value);
        }
    }

    public void setExpiredSeconds(String key, Object value, long seconds) {
        set(key, value, seconds, TimeUnit.SECONDS);
    }

    public void setExpiredMinutes(String key, Object value, long minutes) {
        set(key, value, minutes, TimeUnit.MINUTES);
    }

    public void setExpiredHours(String key, Object value, long hours) {
        set(key, value, hours, TimeUnit.HOURS);
    }

    public void setExpiredDays(String key, Object value, long days) {
        set(key, value, days, TimeUnit.DAYS);
    }

    public void remove(String key) {
        validateKey(key);
        redisTemplate.delete(key);
    }

    private void validateKey(String key) {
        if (StringUtils.isEmpty(key)) {
            throw new IllegalArgumentException("Key should not be null or empty.");
        }
    }
}
