package com.police.kb.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_dict")
public class SysDict extends BaseEntity implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("kind")
    private String kind;

    @TableField("code")
    private String code;

    @TableField("detail")
    private String detail;

    @TableField("alias")
    private String alias;

    @TableField("description")
    private String description;

    @TableField("parent_id")
    private Long parentId;

    @TableField("parent_code")
    private String parentCode;

    @TableField("level")
    private Integer level;

    @TableField("leaf")
    private Integer leaf;

    @TableField("sort")
    private Integer sort;

    @TableField("icon")
    private String icon;

    @TableField("color")
    private String color;

    @TableField("ext_config")
    private String extConfig;

    @TableField("status")
    private Integer status;

    @TableField("is_system")
    private Integer isSystem;

    @TableField("is_public")
    private Integer isPublic;

    @TableField("view_count")
    private Integer viewCount;

    @TableField("favorite_count")
    private Integer favoriteCount;

    @TableField("doc_count")
    private Integer docCount;

    @TableField("remark")
    private String remark;

    @TableField("created_at")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createdTime;

    @TableField("updated_at")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updatedTime;

    @TableField("deleted")
    private Integer deleted;

     @TableField(exist = false)
     private List<SysDict> children = new ArrayList<>();
}
