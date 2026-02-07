package com.police.kb.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 聊天消息视图对象
 * <p>
 * 用于封装对话消息的展示信息，包含消息ID、角色、内容、参考文档等。
 * </p>
 *
 * @author 黄俊
 * @date 2026-01-23 12:00:00
 */
@Data
public class ChatMessage {

  /**
   * 消息ID
   */
  private Long id;

  /**
   * 对话ID
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
  private String references;

  /**
   * 创建时间
   */
  @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
  private LocalDateTime createdTime;

  /**
   * Token数量
   */
  private Integer tokenCount;

  /**
   * 知识库ID
   */
  private Integer kbId;

  /**
   * 模型ID（用于多模型场景）
   */
  private String modelId;
}
