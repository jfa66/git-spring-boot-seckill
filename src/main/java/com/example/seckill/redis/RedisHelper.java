package com.example.seckill.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * @author jiangfengan
 */
@Component
public class RedisHelper {

    @Autowired
    private RedisTemplate redisTemplate;

    private static final String UNLOCK_SCRIPT= "if redis.call('get', KEYS[1]) == ARGV[1] then" +
            " return redis.call('del', KEYS[1]) else return 0 end";

    /**
     * 获取锁
     */
    public boolean lock(String k,String v){
        return redisTemplate.opsForValue().setIfAbsent(k,v,10L, TimeUnit.SECONDS);
    }

    /**
     * 获取锁
     * @param k
     * @param v
     * @param timeout
     */
    public boolean lock(String k,String v,long timeout){
        return redisTemplate.opsForValue().setIfAbsent(k,v,timeout, TimeUnit.SECONDS);
    }

    /**
     * 释放锁
     */
    public boolean unlock(String k,String v){
        DefaultRedisScript<Long> redisScript=new DefaultRedisScript<>(UNLOCK_SCRIPT,Long.class);
        Long l=(Long) redisTemplate.execute(redisScript, Collections.singletonList(k),v);
        return l==0?false:true;
    }


    public boolean push(int capacity,String k,Object v){
        //不是原子性，会有并发问题，应该用lua脚本处理
        //lua脚本暂时不会写，先搁置
        if(redisTemplate.opsForList().size(k)<=capacity){
            redisTemplate.opsForList().leftPushIfPresent(k,v);
            return true;
        }
        return false;
    }

    public Object pop(String k){
        return redisTemplate.opsForList().rightPop(k);
    }

    public void delKey(String k){
        redisTemplate.delete(k);
    }
}
