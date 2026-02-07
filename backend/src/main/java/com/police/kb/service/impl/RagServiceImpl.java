package com.police.kb.service.impl;

import com.police.kb.config.SeekDBConfig;
import com.police.kb.domain.entity.Message;
import com.police.kb.mapper.MessageMapper;
import com.police.kb.service.RagService;
import com.police.kb.service.VectorService;
import com.police.kb.domain.vo.RagAnswer;
import com.police.kb.domain.vo.SearchResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RagServiceImpl implements RagService {

  private final SeekDBConfig seekDBConfig;
  private final MessageMapper messageMapper;
  private final VectorService vectorService;

  @Value("${spring.ai.dashscope.chat.options.model:qwen-plus}")
  private String chatModel;

  private static final int DEFAULT_TOP_K = 10;
  private static final double DEFAULT_SIMILARITY_THRESHOLD = 0.3;
  private static final int DEFAULT_MAX_TOKENS = 2000;

  @Override
  public RagAnswer generateAnswer(Long conversationId, String question,
      Integer kbId, Integer categoryId, int topK) {
    log.info("生成RAG回答 - 对话ID: {}, 知识库ID: {}, 分类ID: {}, TopK: {}",
        conversationId, kbId, categoryId, topK);

    try {
      int effectiveTopK = topK > 0 ? topK : seekDBConfig.getRag().getTopK();
      double threshold = seekDBConfig.getRag().getSimilarityThreshold();

      List<RagAnswer.DocumentReference> references = searchRelevantDocuments(
          question, kbId, categoryId, null, null, effectiveTopK);

      String context = buildContext(references);
      String prompt = buildPrompt(question, context);

      String answer = generateChatCompletion(prompt);

      RagAnswer ragAnswer = new RagAnswer();
      ragAnswer.setAnswer(answer);
      ragAnswer.setReferences(references);
      ragAnswer.setSourcesCount(references != null ? references.size() : 0);

      log.info("RAG回答生成完成，参考文档数: {}", ragAnswer.getSourcesCount());
      return ragAnswer;

    } catch (Exception e) {
      log.error("生成RAG回答失败: {}", e.getMessage(), e);
      RagAnswer errorAnswer = new RagAnswer();
      errorAnswer.setAnswer("抱歉，处理您的问题时出现错误，请稍后重试。");
      errorAnswer.setSourcesCount(0);
      return errorAnswer;
    }
  }

    @Override
    public List<RagAnswer.DocumentReference> searchRelevantDocuments(String question,
        Integer kbId, Integer categoryId, String originScope, String originDepartment, int topK) {
      log.info("搜索相关文档 - 问题: {}, 知识库ID: {}, 分类ID: {}, originScope: {}, originDepartment: {}, TopK: {}",
          question.substring(0, Math.min(30, question.length())), kbId, categoryId, originScope, originDepartment, topK);

      try {
        List<SearchResult> results = searchFromDatabase(question, kbId, categoryId, originScope, originDepartment, topK);

        return results.stream()
            .map(this::convertToDocumentReference)
            .collect(Collectors.toList());

      } catch (Exception e) {
        log.error("搜索相关文档失败: {}", e.getMessage(), e);
        return new ArrayList<>();
      }
    }

  @Override
  public String buildContext(List<RagAnswer.DocumentReference> references) {
    if (references == null || references.isEmpty()) {
      return "未找到相关参考文档。";
    }

    StringBuilder context = new StringBuilder();
    for (int i = 0; i < references.size(); i++) {
      RagAnswer.DocumentReference ref = references.get(i);
      context.append(String.format("【参考 %d】%s\n%s\n\n",
          i + 1, ref.getTitle(), ref.getContent()));
    }

    return context.toString();
  }

  @Override
  public String buildPrompt(String question, String context) {
    return "您是公安专网知识库的智能助手，专注于法律法规和业务规范的精准解答。请根据提供的参考文档，准确、完整地回答用户问题。\n\n" +
        "## 参考文档\n" +
        context + "\n\n" +
        "## 用户问题\n" +
        question + "\n\n" +
        "## 回答要求\n" +
        "1. **准确性**: 严格基于参考文档内容回答，不要添加文档中没有的信息\n" +
        "2. **完整性**: 如果涉及多条法规或多项规定，请逐一列出\n" +
        "3. **清晰性**: 使用清晰的标题和小标题组织内容，重要条款用**加粗**强调\n" +
        "4. **引用**: 在回答末尾明确标注引用来源（如\"根据《刑法》第XX条...\"）\n" +
        "5. **诚实**: 如果参考文档中没有明确答案，请明确告知\n\n" +
        "## 回答格式\n" +
        "【问题分析】\n" +
        "简要分析问题的核心和法律适用\n\n" +
        "【相关法规】\n" +
        "列出适用的法律条文（格式：法规名称 第X条）\n\n" +
        "【具体规定】\n" +
        "详细说明具体规定或处罚措施\n\n" +
        "【注意事项】\n" +
        "补充说明适用条件、例外情况等\n\n" +
        "请开始回答：\n";
  }

  @Override
  public String buildDirectPrompt(String question) {
    return String.format("请回答用户问题。\n\n问题：%s\n\n" +
        "请直接回答，不需要引用来源。", question);
  }

  @Override
  public String buildReferencesJson(List<RagAnswer.DocumentReference> references) {
    if (references == null || references.isEmpty()) {
      return "[]";
    }

    StringBuilder json = new StringBuilder("[");
    for (int i = 0; i < references.size(); i++) {
      RagAnswer.DocumentReference ref = references.get(i);
      if (i > 0)
        json.append(",");
      json.append(String.format(
          "{\"documentId\":%d,\"title\":\"%s\",\"content\":\"%s\",\"score\":%.4f}",
          ref.getDocumentId(),
          escapeJson(ref.getTitle()),
          escapeJson(ref.getContent()),
          ref.getScore()));
    }
    json.append("]");
    return json.toString();
  }

  @Override
  public void streamChat(String prompt, Consumer<String> onChunk, Consumer<Exception> onError) {
    streamChat(null, prompt, onChunk, onError);
  }

  @Override
  public void streamChat(String systemPrompt, String prompt,
      Consumer<String> onChunk, Consumer<Exception> onError) {
    log.info("流式对话 - 提示词长度: {}", prompt.length());

    try {
      // 简化实现：逐字输出模拟流式效果
      String[] sentences = splitIntoSentences(prompt);
      for (String sentence : sentences) {
        String[] chars = sentence.split("");
        for (String ch : chars) {
          onChunk.accept(ch);
          Thread.sleep(5);
        }
      }
      onChunk.accept("[DONE]");
    } catch (Exception e) {
      log.error("流式对话失败: {}", e.getMessage());
      onError.accept(e);
    }
  }

  @Override
  public List<Message> getHistoryMessages(Long conversationId) {
    try {
      return messageMapper.selectList(
          new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Message>()
              .eq(Message::getConversationId, conversationId)
              .orderByAsc(Message::getCreatedTime));
    } catch (Exception e) {
      log.error("获取历史消息失败: {}", e.getMessage());
      return new ArrayList<>();
    }
  }

  @Override
  public String buildPromptWithHistory(String question, String context,
      List<Message> historyMessages) {
    StringBuilder prompt = new StringBuilder();
    prompt.append("您是公安专网知识库的智能助手。以下是对话历史和参考文档，请据此回答当前问题。\n\n");

    if (historyMessages != null && !historyMessages.isEmpty()) {
      prompt.append("【对话历史】\n");
      for (Message msg : historyMessages) {
        String role = "user".equals(msg.getRole()) ? "用户" : "助手";
        prompt.append(String.format("%s：%s\n", role, msg.getContent()));
      }
      prompt.append("\n");
    }

    prompt.append("【参考文档】\n");
    prompt.append(context.isEmpty() ? "无相关参考文档" : context);
    prompt.append("\n\n");
    prompt.append(String.format("【当前问题】%s", question));

    return prompt.toString();
  }

  @Override
  public String buildSimpleChatPrompt(String question, List<Message> historyMessages) {
    StringBuilder prompt = new StringBuilder();

    if (historyMessages != null && !historyMessages.isEmpty()) {
      prompt.append("【对话历史】\n");
      for (Message msg : historyMessages) {
        String role = "user".equals(msg.getRole()) ? "用户" : "助手";
        prompt.append(String.format("%s：%s\n", role, msg.getContent()));
      }
      prompt.append("\n");
    }

    prompt.append(String.format("【当前问题】%s", question));

    return prompt.toString();
  }

      /**
       * 从数据库搜索相关文档
       * 使用混合搜索策略，结合向量检索和全文搜索
       */
      private List<SearchResult> searchFromDatabase(String question, Integer kbId,
          Integer categoryId, String originScope, String originDepartment, int topK) {
        try {
          var ragConfig = seekDBConfig.getRag();
          double threshold = ragConfig.getSimilarityThreshold();

          log.debug("搜索参数 - TopK: {}, 阈值: {}, kbId: {}, categoryId: {}, originScope: {}, originDepartment: {}", 
              topK, threshold, kbId, categoryId, originScope, originDepartment);

          if (kbId == null || kbId <= 0) {
            log.debug("kbId无效，跳过知识库搜索");
            return new ArrayList<>();
          }

          List<SearchResult> results = vectorService.hybridSearch(question, 
              kbId.longValue(), categoryId != null ? categoryId.longValue() : null,
              originScope, originDepartment, topK);

          log.info("搜索到 {} 条相关文档", results.size());

          return results.stream()
              .filter(r -> r.getScore() >= threshold)
              .collect(Collectors.toList());

        } catch (Exception e) {
          log.error("数据库搜索失败: {}", e.getMessage(), e);
          return new ArrayList<>();
        }
      }

  /**
   * 转换为文档引用
   */
  private RagAnswer.DocumentReference convertToDocumentReference(SearchResult result) {
    RagAnswer.DocumentReference ref = new RagAnswer.DocumentReference();
    ref.setDocumentId(result.getDocumentId());
    ref.setTitle(result.getTitle());
    ref.setContent(result.getContent());
    ref.setScore(result.getScore());
    return ref;
  }

  /**
   * 生成聊天完成（简化实现）
   */
  private String generateChatCompletion(String prompt) {
    log.debug("生成聊天完成");
    return "这是一个模拟回答。在实际实现中，应调用 DashScope API 生成回答。";
  }

  /**
   * 转义JSON字符串
   */
  private String escapeJson(String str) {
    if (str == null)
      return "";
    return str.replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\n", "\\n")
        .replace("\r", "\\r")
        .replace("\t", "\\t");
  }

  /**
   * 分割句子
   */
  private String[] splitIntoSentences(String text) {
    return text.split("(?<=[。！？])\\s*");
  }
}
