package com.police.kb.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("prompt_template")
public class PromptTemplate implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String content;

    @TableField("`variables`")
    private String variables;

    private String description;

    @TableField("is_default")
    private Integer isDefault;

    @TableField("is_system")
    private Integer isSystem;  // 0-我的模板 1-系统模板 2-共享模板

    private Integer sort;

    @TableField("created_by")
    private Long createdBy;

    @TableField("created_time")
    private LocalDateTime createdTime;

    @TableField("updated_by")
    private Long updatedBy;

    @TableField("updated_time")
    private LocalDateTime updatedTime;

    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}
