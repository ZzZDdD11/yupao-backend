package com.example.yupaobackend.job;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.yupaobackend.model.domain.User;
import com.example.yupaobackend.service.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 缓存预热
 */
@Component
@Slf4j

public class preCacheJob {
    @Resource
    private UserService userService;

    @Resource
    private RedisTemplate redisTemplate;

    List<Long> mainUserList = Arrays.asList(1L);
    @Autowired
    private RedissonClient redissonClient;


    // 每天执行
    @Scheduled(cron = "0 25 09 * * *")
    public void doCacheRecommenduser() {
        RLock lock = redissonClient.getLock("yupaobackend:precachejob:docache:lock");
        try {

            if(lock.tryLock(0, 30000, TimeUnit.MILLISECONDS)){
                System.out.println("get lock");
                for (Long userId : mainUserList) {
                    QueryWrapper<User> queryWrapper = new QueryWrapper<>();
                    Page<User> page = userService.page(new Page<>(1, 20), queryWrapper);
                    String redisKey = String.format("yupaobackend:user:recommend:%s", userId);
                    ValueOperations<String,Object> valueOperations = redisTemplate.opsForValue();
                    // 写缓存
                    try {
                        valueOperations.set(redisKey,page,30000, TimeUnit.MILLISECONDS);
                    } catch (Exception e){
                        log.error("redis set key error",e);
                    }
                }

            }
        } catch (InterruptedException e) {
            log.error("redis set key error",e);
        } finally {
            // 只能释放自己的锁
            if(lock.isHeldByCurrentThread()) {
                System.out.println("unlock; " + Thread.currentThread().getId());
                lock.unlock();
            }
        }

    }

}
