package com.police.kb.service;

import com.police.kb.domain.entity.Message;
import com.police.kb.domain.vo.RagAnswer;

import java.util.List;
import java.util.function.Consumer;

/**
 * RAG服务接口
 * <p>
 * 定义检索增强生成（RAG）的业务操作方法，包括文档搜索、上下文构建、提示词生成、流式对话等。
 * 支持基于知识库的智能问答功能。
 * </p>
 *
 * @author 黄俊
 * @date 2026-01-23 12:00:00
 */
public interface RagService {

    /**
     * 生成问答答案
     *
     * @param conversationId 对话ID
     * @param question       用户问题
     * @param kbId           知识库ID
     * @param categoryId     分类ID
     * @param topK           参考文档数量
     * @return RAG答案
     */
    RagAnswer generateAnswer(Long conversationId, String question, Integer kbId, Integer categoryId, int topK);

    /**
     * 搜索相关文档
     *
     * @param question 用户问题
     * @param kbId     知识库ID
     * @param categoryId 分类ID
     * @param originScope 溯源层级（可为空）
     * @param originDepartment 溯源部门（可为空）
     * @param topK     返回数量
     * @return 文档引用列表
     */
    List<RagAnswer.DocumentReference> searchRelevantDocuments(String question, Integer kbId,
        Integer categoryId, String originScope, String originDepartment, int topK);

    /**
     * 构建上下文
     *
     * @param references 文档引用列表
     * @return 上下文文本
     */
    String buildContext(List<RagAnswer.DocumentReference> references);

    /**
     * 构建提示词
     *
     * @param question 用户问题
     * @param context  上下文
     * @return 提示词
     */
    String buildPrompt(String question, String context);

    /**
     * 构建直接提示词（无上下文）
     *
     * @param question 用户问题
     * @return 提示词
     */
    String buildDirectPrompt(String question);

    /**
     * 构建参考文档JSON
     *
     * @param references 文档引用列表
     * @return JSON字符串
     */
    String buildReferencesJson(List<RagAnswer.DocumentReference> references);

    /**
     * 流式对话（简化版）
     *
     * @param prompt   提示词
     * @param onChunk  数据块回调
     * @param onError  错误回调
     */
    void streamChat(String prompt, Consumer<String> onChunk, Consumer<Exception> onError);

    /**
     * 流式对话
     *
     * @param systemPrompt 系统提示词
     * @param prompt       用户提示词
     * @param onChunk      数据块回调
     * @param onError      错误回调
     */
    void streamChat(String systemPrompt, String prompt, Consumer<String> onChunk, Consumer<Exception> onError);

    /**
     * 获取对话历史消息
     *
     * @param conversationId 对话ID
     * @return 消息列表
     */
    List<Message> getHistoryMessages(Long conversationId);

    /**
     * 构建带历史的提示词
     *
     * @param question        用户问题
     * @param context         上下文
     * @param historyMessages 历史消息列表
     * @return 提示词
     */
    String buildPromptWithHistory(String question, String context, List<Message> historyMessages);

    /**
     * 构建简单聊天提示词（无知识库）
     *
     * @param question        用户问题
     * @param historyMessages 历史消息列表
     * @return 提示词
     */
    String buildSimpleChatPrompt(String question, List<Message> historyMessages);
}
