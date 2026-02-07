package com.police.kb.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * MCP 服务实体类
 */
@Data
@TableName("mcp_service")
public class McpService {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String code;

    private String description;

    private Integer enabled;

    @TableField("config_url")
    private String configUrl;

    @TableField("config_auth_type")
    private String configAuthType;

    @TableField("config_credentials")
    private String configCredentials;

    @TableField("config_timeout")
    private Integer configTimeout;

    @TableField("config_method")
    private String configMethod;

    private Integer sort;

    private String command;

    private String args;

    private String env;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    @TableField(fill = FieldFill.INSERT)
    private Long createdBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updatedBy;

    @TableLogic
    private Integer deleted;
}
