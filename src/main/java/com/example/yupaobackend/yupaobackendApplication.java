package com.example.yupaobackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@MapperScan("com.example.yupaobackend.mapper")
@SpringBootApplication
@EnableScheduling
public class yupaobackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(yupaobackendApplication.class, args);
    }

}
