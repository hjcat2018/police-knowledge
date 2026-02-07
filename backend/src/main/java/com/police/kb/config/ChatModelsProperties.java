package com.police.kb.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 聊天模型配置类
 * <p>
 * 配置对话功能使用的AI模型列表。
 * 支持多个模型提供商，如DeepSeek、Claude等。
 * </p>
 *
 * @author Police KB System
 * @version 1.0.0
 * @since 2026-02-05
 */
@Data
@Component
@ConfigurationProperties(prefix = "chat")
public class ChatModelsProperties {

    /**
     * 模型配置列表
     */
    private List<ModelConfig> models = new ArrayList<>();

    /**
     * 单个模型配置
     * <p>
     * 定义单个AI模型的所有配置参数。
     * </p>
     */
    @Data
    public static class ModelConfig {
        
        /**
         * 模型唯一标识
         * <p>
         * 用于代码中引用，如：deepseek-chat、claude-3-opus
         * </p>
         */
        private String id;
        
        /**
         * 模型显示名称
         * <p>
         * 用于UI展示，如：DeepSeek Chat、Claude 3 Opus
         * </p>
         */
        private String name;
        
        /**
         * 服务提供商
         * <p>
         * 如：DeepSeek、OpenAI、Anthropic
         * </p>
         */
        private String provider;
        
        /**
         * 模型描述
         */
        private String description;
        
        /**
         * 是否启用该模型
         */
        private Boolean enabled = true;
        
        /**
         * API密钥
         */
        private String apiKey;
        
        /**
         * API基础URL
         */
        private String baseUrl;
        
        /**
         * 模型名称
         * <p>
         * 用于API调用时的模型标识
         * </p>
         */
        private String model;
        
        /**
         * 温度参数
         * <p>
         * 控制回复的随机性，范围0.0-2.0
         * </p>
         */
        private Double temperature = 0.7;
    }
}
