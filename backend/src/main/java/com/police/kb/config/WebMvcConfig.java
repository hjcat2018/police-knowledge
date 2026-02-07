package com.police.kb.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * WebMvc配置类
 * <p>
 * 配置Web应用的全局设置，主要包括跨域资源共享（CORS）配置。
 * 通过CorsFilter实现统一的跨域处理。
 * </p>
 *
 * @author 黄俊
 * @date 2026-01-23 12:00:00
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * 配置跨域过滤器
     * <p>
     * - 允许所有来源域名
     * - 允许携带认证信息
     * - 允许所有HTTP方法
     * - 允许所有请求头
     * - 暴露Authorization头供前端使用
     * - 预检请求有效期3600秒
     * </p>
     *
     * @return 配置好的CorsFilter实例
     */
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        // 允许的来源域名
        config.addAllowedOriginPattern("*");
        // 允许携带认证信息
        config.setAllowCredentials(true);
        // 允许的方法
        config.addAllowedMethod("*");
        // 允许的头信息
        config.addAllowedHeader("*");
        // 暴露的头信息
        config.addExposedHeader("Authorization");
        // 有效期
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
