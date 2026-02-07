package com.police.kb.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus配置类
 * <p>
 * 配置MyBatis-Plus的核心组件，包括分页插件等。
 * 分页插件用于实现数据库查询的分页功能，支持MySQL数据库。
 * </p>
 *
 * @author 黄俊
 * @date 2026-01-23 12:00:00
 */
@Configuration
public class MybatisPlusConfig {

    /**
     * 配置MyBatis-Plus分页插件
     * <p>
     * 添加分页拦截器，支持MySQL数据库的分页查询。
     * 通过Page对象实现分页参数的自动处理。
     * </p>
     *
     * @return MyBatis-Plus拦截器实例
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 添加分页插件，指定数据库类型为MySQL
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}
