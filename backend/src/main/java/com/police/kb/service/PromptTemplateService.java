package com.police.kb.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.police.kb.domain.entity.PromptTemplate;

import java.util.List;
import java.util.Map;

/**
 * 提示词模板服务接口
 * <p>
 * 定义提示词模板的增删改查操作，支持系统模板、私有模板和共享模板的管理。
 * </p>
 */
public interface PromptTemplateService extends IService<PromptTemplate> {

    /**
     * 查询模板列表
     *
     * @param type   模板类型: system(系统模板)/my(我的模板)/shared(共享模板)
     * @param userId 当前用户ID
     * @return 模板列表
     */
    List<PromptTemplate> listTemplates(String type, Long userId);

    /**
     * 获取模板详情
     *
     * @param id 模板ID
     * @return 模板信息
     */
    PromptTemplate getById(Long id);

    /**
     * 创建模板
     *
     * @param template 模板信息
     * @param userId  创建用户ID
     * @return 创建的模板ID
     */
    Long create(PromptTemplate template, Long userId);

    /**
     * 更新模板
     *
     * @param id      模板ID
     * @param template 模板信息
     * @param userId  当前用户ID
     */
    void update(Long id, PromptTemplate template, Long userId);

    /**
     * 删除模板（逻辑删除）
     *
     * @param id     模板ID
     * @param userId 当前用户ID
     */
    void delete(Long id, Long userId);

    /**
     * 设为默认模板
     *
     * @param id     模板ID
     * @param userId 当前用户ID
     */
    void setDefault(Long id, Long userId);

    /**
     * 渲染模板
     *
     * @param template 模板内容，如: "请总结以下内容：{{content}}"
     * @param context  变量上下文，如: {"content": "要总结的文本"}
     * @return 渲染后的字符串
     */
    String render(String template, Map<String, Object> context);
}
