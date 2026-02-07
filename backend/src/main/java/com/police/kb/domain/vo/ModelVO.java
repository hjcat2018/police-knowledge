package com.police.kb.domain.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * 模型响应对象
 * <p>
 * 用于 API 返回模型信息给前端。
 * </p>
 * 
 * @author 黄俊
 * @date 2026-02-03
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ModelVO {
    
    /**
     * 模型唯一标识
     */
    private String id;
    
    /**
     * 模型显示名称
     */
    private String name;
    
    /**
     * 服务提供商
     */
    private String provider;
    
    /**
     * 模型描述
     */
    private String description;
    
    /**
     * 是否启用
     */
    private Boolean enabled;
    
    /**
     * 温度参数（可选返回）
     */
    private Double temperature;
}
