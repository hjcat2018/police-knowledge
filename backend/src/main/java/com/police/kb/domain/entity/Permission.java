package com.police.kb.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_permission")
public class Permission extends BaseEntity {

    /**
     * 权限编码（唯一标识）
     */
    private String code;

    /**
     * 权限名称
     */
    private String name;

    /**
     * 权限描述
     */
    private String description;

    /**
     * 权限类型（如：menu、button、api）
     */
    private String type;

    /**
     * 父级权限ID
     */
    private String parentId;

    /**
     * 排序号
     */
    private Integer sort;

    /**
     * 路由路径
     */
    private String path;

    /**
     * 图标
     */
    private String icon;

    /**
     * 组件路径
     */
    private String component;

    /**
     * 状态（0-禁用，1-启用）
     */
     private Integer status;
}
