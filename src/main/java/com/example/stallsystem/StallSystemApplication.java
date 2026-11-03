package com.example.stallsystem;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.stallsystem.mapper")
public class StallSystemApplication {
    public static void main(String[] args) {
        SpringApplication.run(StallSystemApplication.class, args);
        System.out.println("===========================================");
        System.out.println("  摊位管理系统启动成功！端口：8080");
        System.out.println("  登录接口：POST http://localhost:8080/api/auth/login");
        System.out.println("===========================================");
    }
}
