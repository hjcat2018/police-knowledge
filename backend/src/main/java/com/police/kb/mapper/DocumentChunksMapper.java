package com.police.kb.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.police.kb.domain.entity.DocumentChunks;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 文档分块数据访问接口
 * <p>
 * 提供文档分块数据的CRUD操作。
 * 继承BaseMapper接口，自动获得基础数据库操作能力。
 * </p>
 *
 * @author Police KB System
 * @version 1.0.0
 * @since 2026-02-05
 */
@Mapper
public interface DocumentChunksMapper extends BaseMapper<DocumentChunks> {

    /**
     * 批量插入分块数据
     * <p>
     * 高效地将多个文档分块插入数据库。
     * </p>
     *
     * @param chunks 分块数据列表
     */
    void insertBatch(@Param("chunks") List<DocumentChunks> chunks);

    /**
     * 根据文档ID查询所有分块
     * <p>
     * 返回指定文档的所有分块，按chunkIndex排序。
     * </p>
     *
     * @param documentId 文档ID
     * @return 分块列表
     */
    List<DocumentChunks> selectByDocumentId(@Param("documentId") Long documentId);

    /**
     * 根据文档ID删除所有分块
     * <p>
     * 删除指定文档的所有分块数据。
     * </p>
     *
     * @param documentId 文档ID
     */
    void deleteByDocumentId(@Param("documentId") Long documentId);
}
