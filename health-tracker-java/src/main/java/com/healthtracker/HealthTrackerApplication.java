package com.healthtracker;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.healthtracker.mapper")
public class HealthTrackerApplication {
    public static void main(String[] args) {
        SpringApplication.run(HealthTrackerApplication.class, args);
    }
}
