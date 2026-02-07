package com.police.kb.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

/**
 * WebSocket配置类
 * <p>
 * 配置Spring Boot的WebSocket支持，
 * 自动扫描并注册@ServerEndpoint注解的端点。
 * </p>
 * <p>
 * 启用功能：
 * <ul>
 *   <li>SummaryProgressWebSocket - 摘要进度推送端点</li>
 * </ul>
 * </p>
 *
 * @author Police KB System
 * @version 1.0.0
 * @since 2026-02-05
 */
@Configuration
public class WebSocketConfig {

    /**
     * ServerEndpointExporter Bean
     * <p>
     * 自动检测@ServerEndpoint注解的类，
     * 并将其注册为WebSocket端点。
     * </p>
     *
     * @return ServerEndpointExporter实例
     */
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }
}
