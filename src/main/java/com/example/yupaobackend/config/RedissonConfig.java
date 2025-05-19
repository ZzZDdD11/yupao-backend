package com.example.yupaobackend.config;

import lombok.Data;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * redisson 配置
 */
@ConfigurationProperties(prefix = "spring.data.redis")
@Data
@Configuration
public class RedissonConfig {

    @Bean
    public RedissonClient redissonClient(){
        // 1.创建配置
        Config config = new Config();
        String Redisaddress = "redis://127.0.0.1:6379";
        config.useSingleServer().setAddress(Redisaddress).setDatabase(0);

        // 2.创建实例
        RedissonClient redisson = Redisson.create(config);
        return redisson;
    }


}
