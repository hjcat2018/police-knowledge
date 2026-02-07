package com.police.kb.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.police.kb.domain.dto.KnowledgeBaseDTO;
import com.police.kb.domain.entity.KnowledgeBase;

import java.util.List;

/**
 * 知识库服务接口
 * <p>
 * 定义知识库管理的业务操作方法，包括分页查询、CRUD、状态管理等。
 * </p>
 *
 * @author 黄俊
 * @date 2026-01-23 12:00:00
 */
public interface KnowledgeBaseService extends IService<KnowledgeBase> {

    /**
     * 分页查询知识库列表
     *
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @param keyword  搜索关键词
     * @return 分页知识库列表
     */
    IPage<KnowledgeBase> pageKnowledgeBases(int pageNum, int pageSize, String keyword);

    /**
     * 获取所有知识库
     *
     * @return 知识库列表
     */
    List<KnowledgeBase> listKnowledgeBases();

    /**
     * 获取知识库详情
     *
     * @param id 知识库ID
     * @return 知识库信息
     */
    KnowledgeBase getKnowledgeBaseById(Long id);

    /**
     * 创建知识库
     *
     * @param dto 知识库信息
     * @return 创建的知识库
     */
    KnowledgeBase createKnowledgeBase(KnowledgeBaseDTO dto);

    /**
     * 更新知识库
     *
     * @param id  知识库ID
     * @param dto 知识库信息
     * @return 更新后的知识库
     */
    KnowledgeBase updateKnowledgeBase(Long id, KnowledgeBaseDTO dto);

    /**
     * 删除知识库
     *
     * @param id 知识库ID
     */
    void deleteKnowledgeBase(Long id);

    /**
     * 批量删除知识库
     *
     * @param ids 知识库ID列表
     */
    void batchDeleteKnowledgeBases(List<Long> ids);

    /**
     * 更改知识库状态
     *
     * @param id     知识库ID
     * @param status 状态
     */
    void changeStatus(Long id, Integer status);
}
