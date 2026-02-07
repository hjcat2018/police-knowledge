package com.police.kb.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 知识库数据传输对象
 * <p>
 * 用于封装知识库的创建和更新参数，包含名称、编码、描述、状态等。
 * </p>
 *
 * @author 黄俊
 * @date 2026-01-23 12:00:00
 */
@Data
public class KnowledgeBaseDTO {

    /**
     * 知识库ID（更新时使用）
     */
    private Long id;

    /**
     * 知识库名称
     */
    @NotBlank(message = "知识库名称不能为空")
    @Size(max = 100, message = "知识库名称不能超过100个字符")
    private String name;

    /**
     * 知识库编码（唯一标识）
     */
    @NotBlank(message = "知识库编码不能为空")
    @Size(max = 50, message = "知识库编码不能超过50个字符")
    private String code;

    /**
     * 知识库描述
     */
    @Size(max = 500, message = "描述不能超过500个字符")
    private String description;

    /**
     * 封面图URL
     */
    @Size(max = 500, message = "封面图URL不能超过500个字符")
    private String cover;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 状态（0-停用，1-启用）
     */
    private Integer status;

    /**
     * 是否公开（0-否，1-是）
     */
    private Integer isPublic;
}
