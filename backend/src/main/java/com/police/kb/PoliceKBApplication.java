package com.police.kb;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 公安专网知识库系统 - 启动类
 */
@SpringBootApplication
@MapperScan("com.police.kb.mapper")
@EnableAsync
public class PoliceKBApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(PoliceKBApplication.class, args);
        System.out.println("================================================");
        System.out.println("  公安专网知识库系统启动成功！");
        System.out.println("  API文档: http://localhost:8080/swagger-ui.html");
        System.out.println("================================================");
    }
}
