package com.police.kb.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * 摘要进度WebSocket处理器
 * <p>
 * 负责处理摘要生成进度的实时推送。
 * 支持多个客户端连接同一文档的进度监听。
 * </p>
 * <p>
 * WebSocket端点：ws://host/ws/summary/progress/{docId}
 * </p>
 * <p>
 * 消息格式：
 * <ul>
 *   <li>进度消息：{"type":"progress","current":1,"total":5}</li>
 *   <li>完成消息：{"type":"complete","summary":"..."}</li>
 *   <li>错误消息：{"type":"error","message":"..."}</li>
 * </ul>
 * </p>
 *
 * @author Police KB System
 * @version 1.0.0
 * @since 2026-02-05
 */
@Slf4j
@Component
@ServerEndpoint("/ws/summary/progress/{docId}")
public class SummaryProgressWebSocket {

    /**
     * 文档会话映射
     * <p>
     * Key: 文档ID
     * Value: 监听该文档的WebSocket会话集合
     * </p>
     */
    private static final Map<Long, Set<Session>> docSessions = new ConcurrentHashMap<>();

    /**
     * 当前WebSocket会话
     */
    private Session session;

    /**
     * 当前监听的文档ID
     */
    private Long docId;

    /**
     * WebSocket连接建立事件
     * <p>
     * 当客户端连接到进度推送端点时触发，
     * 将会话添加到对应文档的监听集合中。
     * </p>
     *
     * @param session WebSocket会话
     * @param docId 文档ID
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("docId") Long docId) {
        this.session = session;
        this.docId = docId;
        docSessions.computeIfAbsent(docId, k -> new CopyOnWriteArraySet<>()).add(session);
        log.info("WebSocket连接建立: docId={}, sessionId={}", docId, session.getId());
    }

    /**
     * WebSocket连接关闭事件
     * <p>
     * 当客户端断开连接时触发，
     * 从监听集合中移除会话。
     * </p>
     */
    @OnClose
    public void onClose() {
        if (docId != null && session != null) {
            Set<Session> sessions = docSessions.get(docId);
            if (sessions != null) {
                sessions.remove(session);
                if (sessions.isEmpty()) {
                    docSessions.remove(docId);
                }
            }
            log.info("WebSocket连接关闭: docId={}, sessionId={}", docId, session.getId());
        }
    }

    /**
     * WebSocket错误事件
     * <p>
     * 当连接发生错误时记录日志。
     * </p>
     *
     * @param session WebSocket会话
     * @param error 错误信息
     */
    @OnError
    public void onError(Session session, Throwable error) {
        log.error("WebSocket错误: docId={}, sessionId={}", docId, session.getId(), error);
    }

    /**
     * 发送进度消息
     * <p>
     * 向监听指定文档的所有客户端推送生成进度。
     * </p>
     *
     * @param docId 文档ID
     * @param current 当前完成的块数
     * @param total 总块数
     */
    public void sendProgress(Long docId, int current, int total) {
        Set<Session> sessions = docSessions.get(docId);
        if (sessions != null && !sessions.isEmpty()) {
            String message = String.format("{\"type\":\"progress\",\"current\":%d,\"total\":%d}", current, total);
            sessions.forEach(s -> sendMessage(s, message));
        }
    }

    /**
     * 发送完成消息
     * <p>
     * 向监听指定文档的所有客户端推送摘要生成完成消息，
     * 包含生成的摘要内容。
     * </p>
     *
     * @param docId 文档ID
     * @param summary 生成的摘要内容
     */
    public void sendComplete(Long docId, String summary) {
        Set<Session> sessions = docSessions.get(docId);
        if (sessions != null && !sessions.isEmpty()) {
            String escapedSummary = summary.replace("\"", "\\\"").replace("\n", "\\n");
            String message = String.format("{\"type\":\"complete\",\"summary\":\"%s\"}", escapedSummary);
            sessions.forEach(s -> sendMessage(s, message));
        }
    }

    /**
     * 发送错误消息
     * <p>
     * 向监听指定文档的所有客户端推送错误信息。
     * </p>
     *
     * @param docId 文档ID
     * @param error 错误信息
     */
    public void sendError(Long docId, String error) {
        Set<Session> sessions = docSessions.get(docId);
        if (sessions != null && !sessions.isEmpty()) {
            String escapedError = error.replace("\"", "\\\"");
            String message = String.format("{\"type\":\"error\",\"message\":\"%s\"}", escapedError);
            sessions.forEach(s -> sendMessage(s, message));
        }
    }

    /**
     * 发送WebSocket消息
     *
     * @param session 目标会话
     * @param message 消息内容
     */
    private void sendMessage(Session session, String message) {
        try {
            session.getBasicRemote().sendText(message);
        } catch (IOException e) {
            log.warn("推送消息失败: sessionId={}", session.getId());
        }
    }

    /**
     * 广播进度消息（静态方法）
     * <p>
     * 方便在Service层直接调用进行进度推送。
     * </p>
     *
     * @param docId 文档ID
     * @param current 当前完成的块数
     * @param total 总块数
     */
    public static void broadcastProgress(Long docId, int current, int total) {
        SummaryProgressWebSocket handler = new SummaryProgressWebSocket();
        handler.sendProgress(docId, current, total);
    }

    /**
     * 广播完成消息（静态方法）
     * <p>
     * 方便在Service层直接调用进行完成推送。
     * </p>
     *
     * @param docId 文档ID
     * @param summary 生成的摘要内容
     */
    public static void broadcastComplete(Long docId, String summary) {
        SummaryProgressWebSocket handler = new SummaryProgressWebSocket();
        handler.sendComplete(docId, summary);
    }
}
