package com.police.kb.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.police.kb.domain.dto.DocumentDTO;
import com.police.kb.domain.entity.Document;
import com.police.kb.domain.vo.DocumentVO;

import java.util.List;

/**
 * 文档服务接口
 * <p>
 * 定义文档管理的业务操作方法，包括分页查询、CRUD、状态管理、统计等。
 * </p>
 *
 * @author 黄俊
 * @date 2026-01-23 12:00:00
 */
public interface DocumentService extends IService<Document> {

    /**
     * 分页查询文档列表
     *
     * @param pageNum          页码
     * @param pageSize         每页数量
     * @param kbId             知识库ID
     * @param keyword          搜索关键词
     * @param originScope      归属地过滤
     * @param originDepartment 来源部门过滤
     * @return 分页文档列表
     */
    IPage<DocumentVO> pageDocuments(int pageNum, int pageSize, Long kbId, String keyword, String originScope, String originDepartment);

    /**
     * 获取文档详情
     *
     * @param id 文档ID
     * @return 文档详情
     */
    DocumentVO getDocumentById(Long id);

    /**
     * 创建文档
     *
     * @param dto 文档信息
     * @return 创建的文档
     */
    Document createDocument(DocumentDTO dto);

    /**
     * 更新文档
     *
     * @param id  文档ID
     * @param dto 文档信息
     * @return 更新后的文档
     */
    Document updateDocument(Long id, DocumentDTO dto);

    /**
     * 删除文档
     *
     * @param id 文档ID
     */
    void deleteDocument(Long id);

    /**
     * 批量删除文档
     *
     * @param ids 文档ID列表
     */
    void batchDeleteDocuments(List<Long> ids);

    /**
     * 更改文档状态
     *
     * @param id     文档ID
     * @param status 状态
     */
    void changeStatus(Long id, Integer status);

    /**
     * 增加文档浏览次数
     *
     * @param id 文档ID
     */
    void incrementViewCount(Long id);

    /**
     * 获取热门文档列表
     *
     * @return 热门文档列表
     */
    List<DocumentVO> getHotDocuments();

    /**
     * 获取最新文档列表
     *
     * @return 最新文档列表
     */
    List<DocumentVO> getRecentDocuments();
}
