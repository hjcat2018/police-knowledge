package com.police.kb.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * RestTemplate配置类
 * <p>
 * 配置HTTP客户端RestTemplate，用于发起HTTP请求。
 * 支持调用外部API服务，如嵌入模型API、重排序模型API等。
 * </p>
 *
 * @author 黄俊
 * @date 2026-01-23 12:00:00
 */
@Configuration
public class RestTemplateConfig {

    /**
     * 创建RestTemplate实例
     * <p>
     * RestTemplate是Spring提供的HTTP客户端模板，
     * 用于发送HTTP请求并接收响应。
     * </p>
     *
     * @return RestTemplate实例
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
