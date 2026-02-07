package com.police.kb.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.police.kb.common.Result;
import com.police.kb.domain.entity.Conversation;
import com.police.kb.mapper.ConversationMapper;
import com.police.kb.mapper.MessageMapper;
import com.police.kb.service.ConversationService;
import com.police.kb.service.RagService;
import com.police.kb.domain.vo.ChatMessage;
import com.police.kb.domain.vo.RagAnswer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/v1/conversations")
@RequiredArgsConstructor
public class ConversationController {

  private final ConversationService conversationService;
  private final ConversationMapper conversationMapper;
  private final MessageMapper messageMapper;
  private final RagService ragService;
  private final JdbcTemplate jdbcTemplate;

  private static final DateTimeFormatter[] TIME_FORMATTERS = {
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S"),
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SS"),
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
  };

  /**
   * 解析时间字符串
   *
   * @param obj 时间对象
   * @return LocalDateTime
   */
  private LocalDateTime parseTime(Object obj) {
    if (obj == null)
      return null;
    try {
      if (obj instanceof java.sql.Timestamp) {
        return ((java.sql.Timestamp) obj).toLocalDateTime();
      } else if (obj instanceof java.time.LocalDateTime) {
        return (LocalDateTime) obj;
      } else if (obj instanceof java.util.Date) {
        return ((java.util.Date) obj).toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime();
      }
      String timeStr = obj.toString();
      for (DateTimeFormatter formatter : TIME_FORMATTERS) {
        try {
          return LocalDateTime.parse(timeStr, formatter);
        } catch (Exception ignored) {
        }
      }
      return null;
    } catch (Exception e) {
      log.warn("解析时间失败: {}", obj, e);
      return null;
    }
  }

  /**
   * 创建对话
   *
   * @param request 请求参数（可选title, mode）
   * @return 创建的对话
   */
  @PostMapping
  public Result<Conversation> createConversation(@RequestBody(required = false) Map<String, String> request) {
    Conversation conversation = new Conversation();
    conversation.setTitle(request != null && request.containsKey("title")
        ? request.get("title")
        : "新对话");
    conversation.setMode(request != null && request.containsKey("mode")
        ? request.get("mode")
        : "normal");
    conversation.setCreatedTime(LocalDateTime.now());
    conversation.setUpdatedTime(LocalDateTime.now());
    conversation.setDeleted(0);
    conversationMapper.insert(conversation);
    return Result.success(conversation);
  }

  /**
   * 分页查询对话列表
   *
   * @param page 页码
   * @param size 每页数量
   * @param mode 模式筛选 (optional: professional, normal)
   * @return 分页对话列表
   */
  @GetMapping
  public Result<Page<Conversation>> listConversations(
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(required = false) String mode) {
    int offset = (page - 1) * size;
    String countSql = "SELECT COUNT(*) FROM conversation WHERE deleted = 0";
    String whereClause = " WHERE deleted = 0";
    
    if (mode != null && !mode.isEmpty()) {
      whereClause += " AND mode = '" + mode + "'";
      countSql = "SELECT COUNT(*) FROM conversation" + whereClause;
    }
    
    Long total = jdbcTemplate.queryForObject(countSql, Long.class);

    String sql = "SELECT id, title, mode, created_time, updated_time, deleted FROM conversation" 
        + whereClause + " ORDER BY updated_time DESC LIMIT ? OFFSET ?";
    List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, size, offset);

    List<Conversation> records = rows.stream().map(row -> {
      Conversation conv = new Conversation();
      conv.setId(((Number) row.get("id")).longValue());
      conv.setTitle((String) row.get("title"));
      conv.setMode((String) row.get("mode"));
      conv.setCreatedTime(parseTime(row.get("created_time")));
      conv.setUpdatedTime(parseTime(row.get("updated_time")));
      conv.setDeleted((Integer) row.get("deleted"));
      return conv;
    }).collect(Collectors.toList());

    Page<Conversation> result = new Page<>(page, size, total);
    result.setRecords(records);
    return Result.success(result);
  }

  /**
   * 获取对话详情
   *
   * @param id 对话ID
   * @return 对话信息
   */
  @GetMapping("/{id}")
  public Result<Conversation> getConversation(@PathVariable Long id) {
    Conversation conversation = conversationMapper.selectById(id);
    if (conversation == null || conversation.getDeleted() == 1) {
      return Result.error("对话不存在");
    }
    return Result.success(conversation);
  }

  /**
   * 获取对话消息列表
   *
   * @param id 对话ID
   * @return 消息列表
   */
  @GetMapping("/{id}/messages")
  public Result<List<ChatMessage>> getMessages(@PathVariable Long id) {
    try {
      String sql = "SELECT id, conversation_id, role, content, `references`, created_time, deleted, model_id FROM chat_message WHERE conversation_id = ? AND deleted = 0 ORDER BY created_time ASC";
      List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, id);

      List<ChatMessage> chatMessages = rows.stream().map(row -> {
        ChatMessage cm = new ChatMessage();
        cm.setId(((Number) row.get("id")).longValue());
        cm.setConversationId(((Number) row.get("conversation_id")).longValue());
        cm.setRole((String) row.get("role"));
        cm.setContent((String) row.get("content"));
        cm.setReferences((String) row.get("references"));
        cm.setCreatedTime(parseTime(row.get("created_time")));
        // 设置模型ID
        Object modelId = row.get("model_id");
        cm.setModelId(modelId != null ? (String) modelId : null);
        return cm;
      }).collect(Collectors.toList());

      return Result.success(chatMessages);
    } catch (Exception e) {
      log.error("获取消息失败: {}", e.getMessage(), e);
      return Result.error("获取消息失败: " + e.getMessage());
    }
  }

  /**
   * 发送消息
   *
   * @param id      对话ID
   * @param request 消息请求
   * @return AI回复消息
   */
  @PostMapping("/{id}/messages")
  public Result<ChatMessage> sendMessage(
      @PathVariable Long id,
      @RequestBody Map<String, Object> request) {
    String content = (String) request.get("content");

    Integer kbId = null;
    if (request.containsKey("kbId") && request.get("kbId") != null) {
      Object kbIdObj = request.get("kbId");
      if (kbIdObj instanceof Number) {
        kbId = ((Number) kbIdObj).intValue();
      } else if (kbIdObj instanceof String && !((String) kbIdObj).isEmpty()) {
        kbId = Integer.parseInt((String) kbIdObj);
      }
    }

    Integer categoryId = null;
    if (request.containsKey("categoryId") && request.get("categoryId") != null) {
      Object categoryIdObj = request.get("categoryId");
      if (categoryIdObj instanceof Number) {
        categoryId = ((Number) categoryIdObj).intValue();
      } else if (categoryIdObj instanceof String && !((String) categoryIdObj).isEmpty()) {
        categoryId = Integer.parseInt((String) categoryIdObj);
      }
    }

    Integer topK = request.containsKey("topK") ? (Integer) request.get("topK") : 5;

    if (content == null || content.trim().isEmpty()) {
      return Result.error("消息内容不能为空");
    }

    Conversation conversation = conversationMapper.selectById(id);
    if (conversation == null || conversation.getDeleted() == 1) {
      return Result.error("对话不存在");
    }

    RagAnswer answer = ragService.generateAnswer(id, content.trim(), kbId, categoryId, topK);

    String latestSql = "SELECT id, conversation_id, role, content, `references`, created_time, deleted FROM chat_message WHERE conversation_id = ? AND role = 'assistant' ORDER BY created_time DESC LIMIT 1";
    List<Map<String, Object>> latestRows = jdbcTemplate.queryForList(latestSql, id);

    ChatMessage result = new ChatMessage();
    if (!latestRows.isEmpty()) {
      Map<String, Object> row = latestRows.get(0);
      result.setId(((Number) row.get("id")).longValue());
      result.setConversationId(((Number) row.get("conversation_id")).longValue());
      result.setRole((String) row.get("role"));
      result.setContent((String) row.get("content"));
      result.setReferences((String) row.get("references"));
    }

    return Result.success(result);
  }

  /**
   * 删除对话
   *
   * @param id 对话ID
   * @return 操作结果
   */
  @DeleteMapping("/{id}")
  public Result<Void> deleteConversation(@PathVariable Long id) {
    Conversation conversation = conversationMapper.selectById(id);
    if (conversation == null) {
      return Result.error("对话不存在");
    }
    conversation.setDeleted(1);
    conversationMapper.updateById(conversation);
    return Result.success();
  }
}
