package com.police.kb.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sa-Token配置类
 * <p>
 * 配置Sa-Token认证框架的拦截器和跨域策略。
 * 实现JWT认证 + Redis会话存储，支持接口级别的权限控制。
 * </p>
 *
 * @author 黄俊
 * @date 2026-01-23 12:00:00
 */
@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    /**
     * 配置Sa-Token拦截器
     * <p>
     * - 对所有/api/**路径进行登录校验
     * - 排除登录、登出、公开接口、Swagger文档等无需认证的路径
     * - 使用SaInterceptor实现统一登录校验
     * </p>
     *
     * @param registry 拦截器注册器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handle -> StpUtil.checkLogin()))
            .addPathPatterns("/api/**")
            .excludePathPatterns(
                "/api/v1/auth/login",
                "/api/v1/auth/logout",
                "/api/v1/system/**",
                "/api/v1/vector/stats",
                "/api/v1/vector/test-embed",
                "/api/v1/search/**",
                "/api/v1/conversations/**",
                "/api/v1/chat/stream/**",
                "/api/v1/chat/normal/**",
                "/api/v1/kb/list",
                "/api/v1/models",
                "/api/v1/models/all",
                "/api/v1/test/**",
                "/swagger-ui/**",
                "/swagger-resources/**",
                "/v3/api-docs/**",
                "/webjars/**",
                "/doc.html",
                "/error"
            );
    }

    /**
     * 配置跨域资源共享（CORS）
     * <p>
     * - 允许所有来源域名
     * - 允许携带认证信息
     * - 允许所有HTTP方法
     * - 允许所有请求头
     * - 暴露Authorization头
     * - 预检请求有效期3600秒
     * </p>
     *
     * @param registry 跨域注册器
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            .allowedOriginPatterns("*")
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true)
            .maxAge(3600);
    }
}
