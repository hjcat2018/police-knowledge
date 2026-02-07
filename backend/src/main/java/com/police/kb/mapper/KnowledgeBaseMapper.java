package com.police.kb.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.police.kb.domain.entity.KnowledgeBase;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 知识库数据访问接口
 * <p>
 * 提供知识库（KnowledgeBase）数据的数据库操作。
 * 继承BaseMapper接口，自动获得基础的CRUD能力。
 * </p>
 *
 * @author Police KB System
 * @version 1.0.0
 * @since 2026-02-05
 */
@Mapper
public interface KnowledgeBaseMapper extends BaseMapper<KnowledgeBase> {

    /**
     * 分页查询（按关键词搜索）
     *
     * @param page 分页参数
     * @param keyword 搜索关键词
     * @return 分页结果
     */
    @Select("SELECT * FROM knowledge_base WHERE deleted = 0 AND name LIKE CONCAT('%', #{keyword}, '%') ORDER BY created_time DESC")
    IPage<KnowledgeBase> selectPageByKeyword(Page<KnowledgeBase> page, @Param("keyword") String keyword);

    /**
     * 查询全部知识库
     *
     * @return 知识库列表
     */
    @Select("SELECT * FROM knowledge_base WHERE deleted = 0 ORDER BY created_time DESC")
    List<KnowledgeBase> selectAll();
}
