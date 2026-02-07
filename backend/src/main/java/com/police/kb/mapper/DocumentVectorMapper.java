package com.police.kb.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.police.kb.domain.entity.DocumentVector;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 文档向量Mapper (SeekDB)
 */
@Mapper
public interface DocumentVectorMapper extends BaseMapper<DocumentVector> {

    /**
     * 根据文档ID查询向量
     */
    @Select("SELECT * FROM document_vectors WHERE document_id = #{documentId}")
    List<DocumentVector> selectByDocumentId(@Param("documentId") Long documentId);

    /**
     * 删除文档关联的所有向量
     */
    @Select("DELETE FROM document_vectors WHERE document_id = #{documentId}")
    int deleteByDocumentId(@Param("documentId") Long documentId);

    /**
     * 统计向量数量
     */
    @Select("SELECT COUNT(*) FROM document_vectors")
    long countVectors();
}
