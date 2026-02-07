package com.police.kb.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.police.kb.config.ChatModelsProperties;
import com.police.kb.config.SeekDBConfig;
import com.police.kb.domain.entity.Conversation;
import com.police.kb.domain.entity.Message;
import com.police.kb.domain.entity.McpService;
import com.police.kb.mapper.ConversationMapper;
import com.police.kb.service.McpServiceService;
import com.police.kb.service.RagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class StreamChatController {

  private final RagService ragService;
  private final ConversationMapper conversationMapper;
  private final org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;
  private final ObjectMapper objectMapper = new ObjectMapper();
  private final McpServiceService mcpServiceService;

  private static final String CHAT_MODEL_URL = "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions";

  @Value("${spring.ai.dashscope.api-key:sk-e8d86d11e21540d9b72bc83f78bf1154}")
  private String apiKey;

  @Autowired(required = false)
  private VectorStore vectorStore;

  @Autowired(required = false)
  private EmbeddingModel embeddingModel;

  @Autowired
  private SeekDBConfig seekDBConfig;

  @Autowired
  private ChatModelsProperties chatModelsProperties;

  private static final int THREAD_POOL_CORE_SIZE = 4;
  private static final int THREAD_POOL_MAX_SIZE = 20;
  private static final long THREAD_POOL_KEEP_ALIVE_TIME = 60L;
  private static final int THREAD_POOL_QUEUE_CAPACITY = 100;

  private final java.util.concurrent.ExecutorService sseExecutor = new ThreadPoolExecutor(
      THREAD_POOL_CORE_SIZE,
      THREAD_POOL_MAX_SIZE,
      THREAD_POOL_KEEP_ALIVE_TIME,
      TimeUnit.SECONDS,
      new ArrayBlockingQueue<>(THREAD_POOL_QUEUE_CAPACITY),
      new ThreadPoolExecutor.CallerRunsPolicy()
  );

  private static final long DEFAULT_TIMEOUT = 300000L;
  private static final long HEARTBEAT_INTERVAL = 10000L; // 10秒，避免负载均衡器断开连接
  private static final int SSE_BUFFER_SIZE = 1024;

  /**
   * 发送SSE事件（线程安全）
   */
  private final java.util.concurrent.locks.ReentrantLock emitterLock = new java.util.concurrent.locks.ReentrantLock();

  private void sendSseEvent(SseEmitter emitter, String eventName, Object data) {
    emitterLock.lock();
    try {
      emitter.send(SseEmitter.event().name(eventName).data(data));
    } catch (Exception e) {
      log.warn("发送SSE事件失败: {}", e.getMessage());
    } finally {
      emitterLock.unlock();
    }
  }

  /**
   * 专业模式流式聊天接口
   */
  @GetMapping(value = "/stream/{conversationId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public SseEmitter streamChat(
      @PathVariable Long conversationId,
      @RequestParam String question,
      @RequestParam(required = false) Integer kbId,
      @RequestParam(defaultValue = "10") int topK) {

    log.info("开始专业模式流式对话: conversationId={}, question={}, kbId={}",
        conversationId, question.substring(0, Math.min(50, question.length())), kbId);

    Conversation conversation = conversationMapper.selectById(conversationId);
    if (conversation == null || conversation.getDeleted() == 1) {
      log.warn("对话不存在: {}", conversationId);
      SseEmitter emitter = new SseEmitter();
      try {
        sendSseEvent(emitter, "error", "{\"error\":\"对话不存在\"}");
        emitter.complete();
      } catch (Exception e) {
        emitter.completeWithError(e);
      }
      return emitter;
    }

    SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);
    ScheduledExecutorService heartbeatScheduler = Executors.newSingleThreadScheduledExecutor();
    final long startTime = System.currentTimeMillis();
    final StringBuilder fullAnswer = new StringBuilder();

    Runnable heartbeatTask = () -> {
      try {
        sendSseEvent(emitter, "heartbeat", "{\"type\":\"ping\"}");
        log.debug("发送心跳: conversationId={}", conversationId);
      } catch (Exception e) {
        log.debug("心跳发送失败，连接可能已断开: {}", e.getMessage());
        heartbeatScheduler.shutdown();
      }
    };

    heartbeatScheduler.scheduleAtFixedRate(heartbeatTask, HEARTBEAT_INTERVAL, HEARTBEAT_INTERVAL, TimeUnit.MILLISECONDS);

    sseExecutor.execute(() -> {
      try {
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String convId = String.valueOf(conversationId);

        List<Message> historyMessages = ragService.getHistoryMessages(conversationId);

        String fullPrompt;
        String systemPrompt;
        String refsJson = "[]";

        if (kbId == null || kbId == 0) {
          fullPrompt = ragService.buildSimpleChatPrompt(question, historyMessages);
          systemPrompt = "你是一个智能助手，请直接简洁地回答用户的问题。";
          log.info("普通模式: systemPrompt={}", systemPrompt);
        } else {
          var references = ragService.searchRelevantDocuments(question, kbId, null, null, null, topK);
          String knowledgeContext = ragService.buildPrompt(question, ragService.buildContext(references));
          fullPrompt = ragService.buildPromptWithHistory(question, knowledgeContext, historyMessages);
          systemPrompt = "你是一个公安知识库智能助手，请根据参考文档准确回答问题。";
          refsJson = ragService.buildReferencesJson(references);
          log.info("专业模式: 搜索到 {} 条参考文档", references.size());
        }

        String insertSql = "INSERT INTO chat_message (conversation_id, role, content, `references`, created_time, deleted) VALUES ("
            + convId + ", 'user', '" + escapeSql(question) + "', NULL, '" + now + "', 0)";
        jdbcTemplate.update(insertSql);

        String updateTitleSql = "UPDATE conversation SET title = '" + escapeSql(question) + "' WHERE id = " + convId
            + " AND (title IS NULL OR title = '' OR title = '新对话')";
        jdbcTemplate.update(updateTitleSql);

        Map<String, Object> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", question);
        sendSseEvent(emitter, "message", toJson(userMsg));

        streamWithHttp(emitter, systemPrompt, fullPrompt, fullAnswer, convId, now, refsJson, kbId);

        long duration = System.currentTimeMillis() - startTime;
        log.info("流式对话完成: conversationId={}, duration={}ms, answerLength={}",
            conversationId, duration, fullAnswer.length());

      } catch (Exception e) {
        log.error("流式对话异常: {}", e.getMessage(), e);
        try {
          sendSseEvent(emitter, "error", "{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
        } catch (Exception ex) {
          emitter.completeWithError(ex);
        }
        emitter.completeWithError(e);
      } finally {
        heartbeatScheduler.shutdown();
      }
    });

    emitter.onTimeout(() -> {
      log.warn("流式对话超时: conversationId={}", conversationId);
      heartbeatScheduler.shutdown();
      try {
        sendSseEvent(emitter, "error", "{\"error\":\"对话超时\"}");
      } catch (Exception e) {
        log.debug("超时事件发送失败: {}", e.getMessage());
      }
      emitter.complete();
    });

    emitter.onCompletion(() -> {
      log.debug("流式对话完成: conversationId={}", conversationId);
      heartbeatScheduler.shutdown();
    });

    emitter.onError(e -> {
      log.error("流式对话错误: conversationId={}, error={}", conversationId, e.getMessage());
      heartbeatScheduler.shutdown();
    });

    return emitter;
  }

  /**
   * 普通模式聊天请求DTO
   */
  public static class NormalChatRequest {
    private String question;
    private String models;
    private List<FileInfo> files;
    private List<Long> mcpServiceIds;

    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }
    public String getModels() { return models; }
    public void setModels(String models) { this.models = models; }
    public List<FileInfo> getFiles() { return files; }
    public void setFiles(List<FileInfo> files) { this.files = files; }
    public List<Long> getMcpServiceIds() { return mcpServiceIds; }
    public void setMcpServiceIds(List<Long> mcpServiceIds) { this.mcpServiceIds = mcpServiceIds; }

    public static class FileInfo {
      private String name;
      private String content;

      public String getName() { return name; }
      public void setName(String name) { this.name = name; }
      public String getContent() { return content; }
      public void setContent(String content) { this.content = content; }
    }
  }

  /**
   * 普通模式聊天接口（支持多模型，POST 版本）
   */
  @PostMapping(value = "/normal/{conversationId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public SseEmitter normalChat(
      @PathVariable Long conversationId,
      @RequestBody NormalChatRequest request) {

    String question = request.getQuestion();
    String models = request.getModels();
    List<NormalChatRequest.FileInfo> files = request.getFiles();
    List<Long> mcpServiceIds = request.getMcpServiceIds();

    log.info("开始普通模式对话: conversationId={}, question={}, models={}, filesCount={}, mcpServiceIds={}",
        conversationId, question.substring(0, Math.min(50, question.length())), models, 
        files != null ? files.size() : 0, mcpServiceIds != null ? mcpServiceIds : "[]");

    // 解析MCP服务列表
    List<McpService> mcpServices = new ArrayList<>();
    if (mcpServiceIds != null && !mcpServiceIds.isEmpty()) {
      mcpServices = mcpServiceService.listByIds(mcpServiceIds);
      log.info("选中的MCP服务数量: {}", mcpServices.size());
    }

    // 解析文件列表
    List<String> fileContents = new ArrayList<>();
    if (files != null) {
      for (NormalChatRequest.FileInfo file : files) {
        String content = file.getContent();
        String name = file.getName();
        if (content != null && !content.isEmpty()) {
          fileContents.add("【文件: " + name + "】\n" + content);
        }
      }
    }

    // 解析模型列表
    List<String> modelIds = new ArrayList<>();
    if (models != null && !models.isEmpty()) {
      String[] parts = models.split(",");
      for (String part : parts) {
        String trimmed = part.trim();
        if (!trimmed.isEmpty()) {
          modelIds.add(trimmed);
        }
      }
    }
    if (modelIds.isEmpty()) {
      modelIds.add("gpt-3.5-turbo");
    }

    Conversation conversation = conversationMapper.selectById(conversationId);
    if (conversation == null || conversation.getDeleted() == 1) {
      log.warn("对话不存在: {}", conversationId);
      SseEmitter emitter = new SseEmitter();
      try {
        sendSseEvent(emitter, "error", "{\"error\":\"对话不存在\"}");
        emitter.complete();
      } catch (Exception e) {
        emitter.completeWithError(e);
      }
      return emitter;
    }

    SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);
    ScheduledExecutorService heartbeatScheduler = Executors.newSingleThreadScheduledExecutor();
    final long startTime = System.currentTimeMillis();
    final StringBuilder fullAnswer = new StringBuilder();

    Runnable heartbeatTask = () -> {
      try {
        sendSseEvent(emitter, "heartbeat", "{\"type\":\"ping\"}");
        log.debug("发送心跳: conversationId={}", conversationId);
      } catch (Exception e) {
        log.debug("心跳发送失败: {}", e.getMessage());
        heartbeatScheduler.shutdown();
      }
    };

    heartbeatScheduler.scheduleAtFixedRate(heartbeatTask, HEARTBEAT_INTERVAL, HEARTBEAT_INTERVAL, TimeUnit.MILLISECONDS);

    sseExecutor.execute(() -> {
      try {
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String convId = String.valueOf(conversationId);

        // 保存用户消息
        String insertSql = "INSERT INTO chat_message (conversation_id, role, content, `references`, created_time, deleted) VALUES ("
            + convId + ", 'user', '" + escapeSql(question) + "', NULL, '" + now + "', 0)";
        jdbcTemplate.update(insertSql);

        // 更新对话标题
        String updateTitleSql = "UPDATE conversation SET title = '" + escapeSql(question) + "' WHERE id = " + convId
            + " AND (title IS NULL OR title = '' OR title = '新对话')";
        jdbcTemplate.update(updateTitleSql);

        // 发送用户消息事件
        Map<String, Object> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", question);
        sendSseEvent(emitter, "message", toJson(userMsg));

        // 构建系统提示词
        StringBuilder systemPromptBuilder = new StringBuilder();
        systemPromptBuilder.append("你是一个智能助手，请直接简洁地回答用户的问题。");

        // 如果有上传的文件，添加到系统提示词
        if (!fileContents.isEmpty()) {
          systemPromptBuilder.append("\n\n用户上传了以下文件，请根据文件内容回答问题：\n");
          for (String fileContent : fileContents) {
            systemPromptBuilder.append("\n").append(fileContent).append("\n");
          }
        }

        String systemPrompt = systemPromptBuilder.toString();
        log.info("系统提示词长度: {} chars", systemPrompt.length());

        // 多模型并行调用
        log.info("开始多模型并行调用: models={}", modelIds);

        // 使用线程池和CompletableFuture来管理并发
        List<java.util.concurrent.CompletableFuture<Void>> futures = new ArrayList<>();
        Set<String> completedModels = Collections.synchronizedSet(new HashSet<>());

        for (String modelId : modelIds) {
            final String currentModelId = modelId;
            final StringBuilder modelAnswer = new StringBuilder();

            java.util.concurrent.CompletableFuture<Void> future = java.util.concurrent.CompletableFuture.runAsync(() -> {
                try {
                    ChatModelsProperties.ModelConfig modelConfig = findModelConfig(currentModelId);
                    if (modelConfig != null) {
                        log.info("调用模型: modelId={}, provider={}", currentModelId, modelConfig.getProvider());
                        streamWithModel(emitter, modelConfig, question, systemPrompt, modelAnswer, convId, now, currentModelId);
                    } else {
                        log.warn("未找到模型配置: {}", currentModelId);
                        try {
                            Map<String, Object> errorData = new HashMap<>();
                            errorData.put("modelId", currentModelId);
                            errorData.put("error", "模型配置不存在");
                            sendSseEvent(emitter, "error", toJson(errorData));
                        } catch (Exception ex) {
                            log.debug("发送错误事件失败: {}", ex.getMessage());
                        }
                    }
                } catch (Exception e) {
                    log.error("模型调用失败: modelId={}, error={}", currentModelId, e.getMessage());
                    try {
                        Map<String, Object> errorData = new HashMap<>();
                        errorData.put("modelId", currentModelId);
                        errorData.put("error", e.getMessage());
                        sendSseEvent(emitter, "error", toJson(errorData));
                    } catch (Exception ex) {
                        log.debug("发送错误事件失败: {}", ex.getMessage());
                    }
                } finally {
                    completedModels.add(currentModelId);
                    log.debug("模型完成: modelId={}, completedModels={}", currentModelId, completedModels.size());
                }
            }, sseExecutor);
            futures.add(future);
        }

        // 等待所有模型完成
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).thenRun(() -> {
            log.info("所有模型响应完成, completedModels={}, modelIds={}", completedModels.size(), modelIds.size());
            try {
                Map<String, Object> allDoneData = new HashMap<>();
                allDoneData.put("allComplete", true);
                sendSseEvent(emitter, "allDone", toJson(allDoneData));
            } catch (Exception e) {
                log.debug("发送allDone事件失败: {}", e.getMessage());
            }
            emitter.complete();
        });

        long duration = System.currentTimeMillis() - startTime;
        log.info("普通模式对话完成: conversationId={}, models={}, duration={}ms",
            conversationId, modelIds, duration);

      } catch (Exception e) {
        log.error("普通模式对话异常: {}", e.getMessage(), e);
        try {
          sendSseEvent(emitter, "error", "{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
        } catch (Exception ex) {
          emitter.completeWithError(ex);
        }
        emitter.completeWithError(e);
      } finally {
        heartbeatScheduler.shutdown();
      }
    });

    emitter.onTimeout(() -> {
      log.warn("普通模式对话超时: conversationId={}", conversationId);
      heartbeatScheduler.shutdown();
      try {
        sendSseEvent(emitter, "error", "{\"error\":\"对话超时\"}");
      } catch (Exception e) {
        log.debug("超时事件发送失败: {}", e.getMessage());
      }
      emitter.complete();
    });

    emitter.onCompletion(() -> {
      log.debug("普通模式对话完成: conversationId={}", conversationId);
      heartbeatScheduler.shutdown();
    });

    emitter.onError(e -> {
      log.error("普通模式对话错误: conversationId={}, error={}", conversationId, e.getMessage());
      heartbeatScheduler.shutdown();
    });

    return emitter;
  }

  /**
   * 使用配置中的模型进行流式对话
   */
  private void streamWithModel(SseEmitter emitter, ChatModelsProperties.ModelConfig modelConfig,
      String question, String systemPrompt, StringBuilder fullAnswer, String convId, String now, String modelId) {
    try {
      String baseUrl = modelConfig.getBaseUrl();
      String model = modelConfig.getModel();
      String apiKey = modelConfig.getApiKey();
      Double temperature = modelConfig.getTemperature() != null ? modelConfig.getTemperature() : 0.7;

      log.info("调用模型API: modelId={}, provider={}, model={}, baseUrl={}", modelId, modelConfig.getProvider(), model, baseUrl);

      // 构建请求体
      String jsonBody = buildRequestBody(modelConfig.getProvider(), model, systemPrompt, question, temperature);

      // 处理 DeepSeek 特殊路径
      String apiUrl = baseUrl;
      if ("DeepSeek".equalsIgnoreCase(modelConfig.getProvider())) {
        if (!baseUrl.endsWith("/")) {
          apiUrl = baseUrl + "/";
        }
        apiUrl = apiUrl + "chat/completions";
      } else {
        if (!baseUrl.endsWith("/")) {
          apiUrl = apiUrl + "/";
        }
        apiUrl = apiUrl + "chat/completions";
      }

      java.net.URL url = new java.net.URL(apiUrl);
      java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
      conn.setRequestMethod("POST");
      conn.setRequestProperty("Authorization", "Bearer " + apiKey);
      conn.setRequestProperty("Content-Type", "application/json");
      conn.setDoOutput(true);
      conn.setConnectTimeout(30000);
      conn.setReadTimeout(120000);

      // Claude 需要特殊头部
      if ("Anthropic".equalsIgnoreCase(modelConfig.getProvider())) {
        conn.setRequestProperty("x-api-key", apiKey);
        conn.setRequestProperty("Anthropic-Version", "2023-06-01");
      }

      try (OutputStream os = conn.getOutputStream()) {
        os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
      }

      int responseCode = conn.getResponseCode();
      log.info("API响应码: modelId={}, code={}", modelId, responseCode);

      if (responseCode != 200) {
        StringBuilder errorBody = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
          String line;
          while ((line = reader.readLine()) != null) {
            errorBody.append(line);
          }
        }
        log.error("API错误响应: modelId={}, responseCode={}, errorBody={}", modelId, responseCode, errorBody);
        Map<String, Object> errorData = new HashMap<>();
        errorData.put("modelId", modelId);
        errorData.put("error", extractErrorMessage(errorBody.toString(), responseCode));
        sendSseEvent(emitter, "error", toJson(errorData));
        return;
      }

      // 解析流式响应
      try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
        String line;
        while ((line = reader.readLine()) != null) {
          if (line.startsWith("data:")) {
            String data = line.substring(5).trim();
            if ("[DONE]".equals(data)) {
              log.debug("收到 [DONE] 信号: modelId={}", modelId);
              break;
            }
            if (!data.isEmpty()) {
              try {
                Map<String, Object> chunk = objectMapper.readValue(data, Map.class);
                String content = extractContent(chunk, modelConfig.getProvider());
                log.debug("收到内容块: modelId={}, content={}", modelId, content);
                if (content != null && !content.isEmpty()) {
                  fullAnswer.append(content);
                  Map<String, Object> chunkData = new HashMap<>();
                  chunkData.put("modelId", modelId);
                  chunkData.put("content", content);
                  sendSseEvent(emitter, "chunk", toJson(chunkData));
                  // 短暂暂停以确保前端接收（可选，优化流式体验）
                  if (fullAnswer.length() % 50 == 0) {
                    Thread.sleep(10);
                  }
                }
              } catch (Exception e) {
                log.warn("解析响应数据失败: modelId={}, error={}", modelId, e.getMessage());
              }
            }
          }
        }
      }

      // 保存助手消息并发送完成事件
      saveAssistantMessage(emitter, fullAnswer.toString(), "[]", convId, now, 0, fullAnswer.length() / 4, null, modelId);

    } catch (Exception e) {
      log.error("调用模型API失败: modelId={}, provider={}, model={}, error={}", 
          modelId, modelConfig.getProvider(), modelConfig.getModel(), e.getMessage(), e);
      try {
        Map<String, Object> errorData = new HashMap<>();
        errorData.put("modelId", modelId);
        // 尝试提取更详细的错误信息
        String detailedError = extractDetailedError(e);
        errorData.put("error", detailedError);
        sendSseEvent(emitter, "error", toJson(errorData));
      } catch (Exception ex) {
        log.debug("发送错误事件失败: {}", ex.getMessage());
      }
    }
  }

  /**
   * 从异常中提取详细错误信息
   */
  private String extractDetailedError(Exception e) {
    String message = e.getMessage();
    if (message == null || message.isEmpty()) {
      return "未知错误";
    }
    
    // 如果是Java网络异常，提取状态码和原因
    if (message.contains("java.net") || message.contains("HttpURLConnection")) {
      // 尝试提取响应体
      if (e.getCause() != null) {
        String causeMessage = e.getCause().getMessage();
        if (causeMessage != null && causeMessage.length() > 10) {
          return causeMessage;
        }
      }
    }
    
    // 清理并返回原始消息
    if (message.length() > 300) {
      return message.substring(0, 300) + "...";
    }
    return message;
  }

  /**
   * 构建请求体
   */
  private ChatModelsProperties.ModelConfig findModelConfig(String modelId) {
    if (chatModelsProperties == null || chatModelsProperties.getModels() == null) {
      return null;
    }
    for (ChatModelsProperties.ModelConfig config : chatModelsProperties.getModels()) {
      if (modelId.equals(config.getId())) {
        return config;
      }
    }
    return null;
  }

  /**
   * 构建请求体
   */
  private String buildRequestBody(String provider, String model, String systemPrompt, String question, Double temperature) {
    String tempStr = temperature != null ? String.valueOf(temperature) : "0.7";
    
    if (systemPrompt != null && !systemPrompt.isEmpty()) {
      return String.format(
          "{\"model\":\"%s\",\"messages\":[{\"role\":\"system\",\"content\":\"%s\"},{\"role\":\"user\",\"content\":\"%s\"}],\"max_tokens\":2000,\"temperature\":%s,\"stream\":true}",
          model, escapeJson(systemPrompt), escapeJson(question), tempStr);
    } else {
      return String.format(
          "{\"model\":\"%s\",\"messages\":[{\"role\":\"user\",\"content\":\"%s\"}],\"max_tokens\":2000,\"temperature\":%s,\"stream\":true}",
          model, escapeJson(question), tempStr);
    }
  }

  /**
   * 提取响应数据
   */
  private String extractChunkData(String line, String provider) {
    if (line.startsWith("data:")) {
      return line.substring(5).trim();
    }
    return line;
  }

  /**
   * 从错误响应中提取错误信息
   */
  private String extractErrorMessage(String errorBody, int responseCode) {
    if (errorBody == null || errorBody.isEmpty()) {
      return "API返回错误: " + responseCode;
    }
    try {
      // 尝试解析JSON格式的错误响应
      Map<String, Object> errorMap = objectMapper.readValue(errorBody, Map.class);
      
      // 多种错误格式
      if (errorMap.containsKey("error")) {
        Object error = errorMap.get("error");
        if (error instanceof String) {
          return (String) error;
        } else if (error instanceof Map) {
          Map<?, ?> errorObj = (Map<?, ?>) error;
          if (errorObj.containsKey("message")) {
            return String.valueOf(errorObj.get("message"));
          }
          if (errorObj.containsKey("type")) {
            return errorObj.get("type") + ": " + errorObj.get("message");
          }
        }
      }
      
      // OpenAI/DashScope格式
      if (errorMap.containsKey("message")) {
        return String.valueOf(errorMap.get("message"));
      }
      
      // 通用格式
      if (errorMap.containsKey("code")) {
        return errorMap.get("code") + ": " + errorMap.get("message");
      }
      
      // 返回原始错误体（截取前200字符）
      String truncated = errorBody.length() > 200 ? errorBody.substring(0, 200) + "..." : errorBody;
      return "API返回错误 " + responseCode + ": " + truncated;
    } catch (Exception e) {
      // 无法解析JSON，直接返回错误体
      String truncated = errorBody.length() > 200 ? errorBody.substring(0, 200) + "..." : errorBody;
      return "API返回错误 " + responseCode + ": " + truncated;
    }
  }

  /**
   * 从响应块中提取内容
   */
  private String extractContent(Map<String, Object> chunk, String provider) {
    try {
      List<Map<String, Object>> choices = (List<Map<String, Object>>) chunk.get("choices");
      if (choices != null && !choices.isEmpty()) {
        Map<String, Object> delta = (Map<String, Object>) choices.get(0).get("delta");
        if (delta != null && delta.containsKey("content")) {
          Object contentObj = delta.get("content");
          if (contentObj != null) {
            return contentObj.toString();
          }
        }
      }
    } catch (Exception e) {
      log.debug("提取内容失败: {}", e.getMessage());
    }
    return null;
  }

  /**
   * 使用HttpURLConnection进行流式对话（默认DashScope）
   */
  private void streamWithHttp(SseEmitter emitter, String systemPrompt, String prompt,
      StringBuilder fullAnswer, String convId, String now, String refsJson, Integer kbId) {
    try {
      String jsonBody;
      if (systemPrompt != null && !systemPrompt.isEmpty()) {
        jsonBody = String.format(
            "{\"model\":\"qwen-plus\",\"messages\":[{\"role\":\"system\",\"content\":\"%s\"},{\"role\":\"user\",\"content\":\"%s\"}],\"max_tokens\":2000,\"temperature\":0.7,\"stream\":true}",
            escapeJson(systemPrompt), escapeJson(prompt));
      } else {
        jsonBody = String.format(
            "{\"model\":\"qwen-plus\",\"messages\":[{\"role\":\"user\",\"content\":\"%s\"}],\"max_tokens\":2000,\"temperature\":0.7,\"stream\":true}",
            escapeJson(prompt));
      }

      java.net.URL url = new java.net.URL(CHAT_MODEL_URL);
      java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
      conn.setRequestMethod("POST");
      conn.setRequestProperty("Authorization", "Bearer " + apiKey);
      conn.setRequestProperty("Content-Type", "application/json");
      conn.setDoOutput(true);
      conn.setConnectTimeout(30000);
      conn.setReadTimeout(120000);

      try (OutputStream os = conn.getOutputStream()) {
        os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
      }

      int responseCode = conn.getResponseCode();
      log.info("API响应码: {}", responseCode);

      if (responseCode != 200) {
        StringBuilder errorBody = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
          String line;
          while ((line = reader.readLine()) != null) {
            errorBody.append(line);
          }
        }
        log.error("API错误响应: {}", errorBody);
        sendSseEvent(emitter, "error", "{\"error\":\"API返回错误: " + responseCode + "\"}");
        emitter.complete();
        return;
      }

      try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
        String line;
        while ((line = reader.readLine()) != null) {
          if (line.startsWith("data:")) {
            String data = line.substring(5).trim();
            if ("[DONE]".equals(data)) {
              break;
            }
            try {
              Map<String, Object> chunk = objectMapper.readValue(data, Map.class);
              List<Map<String, Object>> choices = (List<Map<String, Object>>) chunk.get("choices");
              if (choices != null && !choices.isEmpty()) {
                Map<String, Object> delta = (Map<String, Object>) choices.get(0).get("delta");
                if (delta != null && delta.containsKey("content")) {
                  String content = (String) delta.get("content");
                  if (content != null && !content.isEmpty()) {
                    fullAnswer.append(content);
                    Map<String, Object> chunkData = new HashMap<>();
                    chunkData.put("content", content);
                    sendSseEvent(emitter, "chunk", toJson(chunkData));
                  }
                }
              }
            } catch (Exception e) {
              log.warn("解析SSE数据失败: {}", e.getMessage());
            }
          }
        }
      }

      saveAssistantMessage(emitter, fullAnswer.toString(), refsJson, convId, now, 0, fullAnswer.length() / 4, kbId);
    } catch (Exception e) {
      log.error("流式调用大模型失败: {}", e.getMessage(), e);
      try {
        sendSseEvent(emitter, "error", "{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
      } catch (Exception ex) {
        emitter.completeWithError(ex);
      }
      emitter.completeWithError(e);
    }
  }

  /**
   * 保存助手消息并发送完成事件
   */
  private void saveAssistantMessage(SseEmitter emitter, String answer, String refsJson, String convId, String now,
      int promptTokens, int completionTokens, Integer kbId, String modelId) {
    try {
      String updateSql = "UPDATE conversation SET updated_time = '" + now + "' WHERE id = " + convId;
      jdbcTemplate.update(updateSql);

      // 保存消息时包含 model_id
      String modelIdValue = modelId != null ? "'" + escapeSql(modelId) + "'" : "NULL";
      String insertSql = "INSERT INTO chat_message (conversation_id, role, content, `references`, created_time, deleted, model_id) VALUES ("
          + convId + ", 'assistant', '" + escapeSql(answer) + "', '" + escapeSql(refsJson) + "', '" + now + "', 0, " + modelIdValue + ")";
      jdbcTemplate.update(insertSql);

      String selectSql = "SELECT id FROM chat_message WHERE conversation_id = " + convId
          + " AND role = 'assistant' ORDER BY created_time DESC LIMIT 1";
      Long msgId = jdbcTemplate.queryForObject(selectSql, Long.class);

      Map<String, Object> doneData = new HashMap<>();
      doneData.put("id", msgId != null ? msgId : 0);
      doneData.put("modelId", modelId != null ? modelId : "");
      doneData.put("references", refsJson);
      doneData.put("promptTokens", promptTokens);
      doneData.put("completionTokens", completionTokens);
      doneData.put("totalTokens", promptTokens + completionTokens);
      doneData.put("createdTime", now);
      doneData.put("kbId", kbId != null ? kbId : 0);
      sendSseEvent(emitter, "done", toJson(doneData));
    } catch (Exception e) {
      log.error("保存助手消息失败: {}", e.getMessage());
    }
  }

  /**
   * 保存助手消息并发送完成事件（兼容旧版本）
   */
  private void saveAssistantMessage(SseEmitter emitter, String answer, String refsJson, String convId, String now,
      int promptTokens, int completionTokens, Integer kbId) {
    saveAssistantMessage(emitter, answer, refsJson, convId, now, promptTokens, completionTokens, kbId, null);
  }

  private String toJson(Object obj) {
    try {
      return objectMapper.writeValueAsString(obj);
    } catch (Exception e) {
      log.error("JSON序列化失败: {}", e.getMessage());
      return "{}";
    }
  }

  private static String escapeSql(String str) {
    if (str == null) return null;
    return str.replace("'", "''");
  }

  private String escapeJson(String str) {
    if (str == null) return "";
    return str.replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\n", "\\n")
        .replace("\r", "\\r")
        .replace("\t", "\\t");
  }
}
