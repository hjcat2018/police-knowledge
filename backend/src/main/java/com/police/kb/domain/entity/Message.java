package com.police.kb.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
@TableName("chat_message")
public class Message {

  /**
   * 主键ID
   */
  @TableId(type = IdType.AUTO)
  private Long id;

  /**
   * 所属对话ID
   */
  private Long conversationId;

  /**
   * 消息角色（user-用户，assistant-AI）
   */
  private String role;

  /**
   * 消息内容
   */
  private String content;

  /**
   * 参考文档信息（JSON格式）
   */
  @TableField("`references`")
  private String references;

  /**
   * 创建时间
   */
  @TableField(fill = FieldFill.INSERT)
  @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
  private LocalDateTime createdTime;

  /**
   * 删除标识（0-未删除，1-已删除）
   */
     @TableLogic
     private Integer deleted;
}
