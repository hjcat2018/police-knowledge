package com.police.kb.service;

import com.police.kb.domain.vo.SearchResult;

import java.util.List;
import java.util.Map;

/**
 * 向量服务接口
 * <p>
 * 定义向量处理和搜索的业务操作方法，包括文档向量化、语义搜索、混合搜索等。
 * 用于支持RAG检索增强生成功能。
 * </p>
 *
 * @author 黄俊
 * @date 2026-01-23 12:00:00
 */
public interface VectorService {
    
    /**
     * 对文档进行向量化处理
     *
     * @param documentId 文档ID
     */
    void vectorizeDocument(Long documentId);
    
    /**
     * 重新向量化文档
     *
     * @param documentId 文档ID
     */
    void revectorizeDocument(Long documentId);
    
    /**
     * 删除文档的向量数据
     *
     * @param documentId 文档ID
     */
    void deleteDocumentVectors(Long documentId);
    
    /**
     * 语义搜索
     *
     * @param keyword 搜索关键词
     * @param kbId    知识库ID（叶子分类）
     * @param categoryId 分类ID（一级/二级，支持查询子分类）
     * @param originScope 溯源层级（可为空）
     * @param originDepartment 溯源部门（可为空）
     * @param topK    返回结果数量
     * @return 搜索结果列表
     */
    List<SearchResult> semanticSearch(String keyword, Long kbId, Long categoryId, String originScope, String originDepartment, int topK);

    /**
     * 混合搜索
     *
     * @param keyword 搜索关键词
     * @param kbId    知识库ID（叶子分类）
     * @param categoryId 分类ID（一级/二级，支持查询子分类）
     * @param originScope 溯源层级（可为空）
     * @param originDepartment 溯源部门（可为空）
     * @param topK    返回结果数量
     * @return 搜索结果列表
     */
    List<SearchResult> hybridSearch(String keyword, Long kbId, Long categoryId, String originScope, String originDepartment, int topK);

    /**
     * 带过滤条件的通用搜索
     *
     * @param keyword 搜索关键词
     * @param kbId    知识库ID
     * @param categoryId 分类ID
     * @param filters 过滤条件（可包含originScope、originDepartment等）
     * @param topK    返回结果数量
     * @return 搜索结果列表
     */
    List<SearchResult> searchWithFilters(String keyword, Long kbId, Long categoryId, Map<String, Object> filters, int topK);
    
    /**
     * 同步文档溯源字段到向量表
     *
     * @param documentId 文档ID
     * @param originScope 溯源层级
     * @param originDepartment 溯源部门
     */
    void syncVectorMetadata(Long documentId, String originScope, String originDepartment);
    
    /**
     * 生成文本向量
     *
     * @param text 文本内容
     * @return 向量字符串
     */
    String generateVector(String text);
}
