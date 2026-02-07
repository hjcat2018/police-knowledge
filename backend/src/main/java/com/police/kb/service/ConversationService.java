package com.police.kb.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.police.kb.domain.entity.Conversation;

/**
 * 对话服务接口
 * <p>
 * 提供对话（Conversation）的业务逻辑操作。
 * 继承IService接口，自动获得基础的CRUD能力。
 * </p>
 * <p>
 * 主要功能：
 * <ul>
 *   <li>对话的创建、查询、更新、删除</li>
 *   <li>对话消息管理</li>
 *   <li>对话状态控制</li>
 * </ul>
 * </p>
 *
 * @author Police KB System
 * @version 1.0.0
 * @since 2026-02-05
 */
public interface ConversationService extends IService<Conversation> {
}
