package com.police.kb.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("knowledge_base")
public class KnowledgeBase extends BaseEntity {

    /**
     * 知识库名称
     */
    private String name;

    /**
     * 知识库编码（唯一标识）
     */
    private String code;

    /**
     * 知识库描述
     */
    private String description;

    /**
     * 封面图URL
     */
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

    /**
     * 浏览次数
     */
    private Integer viewCount;

    /**
     * 收藏次数
     */
    private Integer favoriteCount;

    /**
     * 文档数量
     */
     private Integer docCount;
}
