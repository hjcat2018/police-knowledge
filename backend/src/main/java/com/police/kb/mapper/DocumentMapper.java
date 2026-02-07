package com.police.kb.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.police.kb.domain.entity.Document;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 文档数据访问接口
 * <p>
 * 提供文档数据的CRUD操作和高级查询功能。
 * 继承BaseMapper接口，自动获得基础数据库操作能力。
 * </p>
 *
 * @author Police KB System
 * @version 1.0.0
 * @since 2026-02-05
 */
@Mapper
public interface DocumentMapper extends BaseMapper<Document> {

    /**
     * 根据知识库ID查询文档
     *
     * @param kbId 知识库ID
     * @return 文档列表
     */
    @Select("SELECT d.*, sd.detail as kb_name FROM document d LEFT JOIN sys_dict sd ON d.kb_id = sd.id WHERE d.deleted = 0 AND d.status = 1 AND FIND_IN_SET(#{kbId}, d.kb_path)")
    List<Document> selectByKbId(@Param("kbId") Long kbId);

    /**
     * 查询热门文档
     * <p>
     * 按浏览量降序排列，返回前10条。
     * </p>
     *
     * @return 热门文档列表
     */
    @Select("SELECT * FROM document WHERE status = 1 AND deleted = 0 ORDER BY view_count DESC LIMIT 10")
    List<Document> selectHotDocuments();

    /**
     * 查询最近文档
     * <p>
     * 按创建时间降序排列，返回前10条。
     * </p>
     *
     * @return 最近文档列表
     */
    @Select("SELECT * FROM document WHERE deleted = 0 ORDER BY created_time DESC LIMIT 10")
    List<Document> selectRecentDocuments();

    /**
     * 分页查询（支持关键词过滤）
     *
     * @param page 分页参数
     * @param kbId 知识库ID（可选，支持级联）
     * @param keyword 关键词（可选，匹配标题）
     * @param originScope 来源范围（可选）
     * @param originDepartment 来源部门（可选）
     * @return 分页结果
     */
    @Select("<script>" +
        "SELECT d.*, sd.detail as kb_name " +
        "FROM document d " +
        "LEFT JOIN sys_dict sd ON d.kb_id = sd.id " +
        "WHERE d.deleted = 0 " +
        "<if test='kbId != null'>AND FIND_IN_SET(#{kbId}, d.kb_path)</if> " +
        "<if test='keyword != null and keyword != \"\"'>AND d.title LIKE CONCAT('%', #{keyword}, '%')</if> " +
        "<if test='originScope != null and originScope != \"\"'>AND d.origin_scope = #{originScope}</if> " +
        "<if test='originDepartment != null and originDepartment != \"\"'>AND d.origin_department = #{originDepartment}</if> " +
        "ORDER BY d.is_top DESC, d.created_time DESC" +
        "</script>")
    @Results({
        @Result(column = "kb_name", property = "kbName")
    })
    IPage<Document> selectPageByKbIdAndKeyword(Page<Document> page, @Param("kbId") Long kbId,
        @Param("keyword") String keyword, @Param("originScope") String originScope,
        @Param("originDepartment") String originDepartment);

    /**
     * 分页查询（按知识库筛选）
     *
     * @param page 分页参数
     * @param kbId 知识库ID（可选，支持级联）
     * @param originScope 来源范围（可选）
     * @param originDepartment 来源部门（可选）
     * @return 分页结果
     */
    @Select("<script>" +
        "SELECT d.*, sd.detail as kb_name " +
        "FROM document d " +
        "LEFT JOIN sys_dict sd ON d.kb_id = sd.id " +
        "WHERE d.deleted = 0 " +
        "<if test='kbId != null'>AND FIND_IN_SET(#{kbId}, d.kb_path)</if> " +
        "<if test='originScope != null and originScope != \"\"'>AND d.origin_scope = #{originScope}</if> " +
        "<if test='originDepartment != null and originDepartment != \"\"'>AND d.origin_department = #{originDepartment}</if> " +
        "ORDER BY d.is_top DESC, d.created_time DESC" +
        "</script>")
    @Results({
        @Result(column = "kb_name", property = "kbName")
    })
    IPage<Document> selectPageByKbId(Page<Document> page, @Param("kbId") Long kbId,
        @Param("originScope") String originScope, @Param("originDepartment") String originDepartment);

    /**
     * 查询全部文档（分页）
     *
     * @param page 分页参数
     * @param originScope 来源范围（可选）
     * @param originDepartment 来源部门（可选）
     * @return 分页结果
     */
    @Select("<script>" +
        "SELECT d.*, sd.detail as kb_name " +
        "FROM document d " +
        "LEFT JOIN sys_dict sd ON d.kb_id = sd.id " +
        "WHERE d.deleted = 0 " +
        "<if test='originScope != null and originScope != \"\"'>AND d.origin_scope = #{originScope}</if> " +
        "<if test='originDepartment != null and originDepartment != \"\"'>AND d.origin_department = #{originDepartment}</if> " +
        "ORDER BY d.is_top DESC, d.created_time DESC" +
        "</script>")
    @Results({
        @Result(column = "kb_name", property = "kbName")
    })
    IPage<Document> selectPageAll(Page<Document> page, @Param("originScope") String originScope,
        @Param("originDepartment") String originDepartment);

    /**
     * 根据ID查询文档详情
     *
     * @param id 文档ID
     * @return 文档实体
     */
    @Select("SELECT d.*, sd.detail as kb_name FROM document d LEFT JOIN sys_dict sd ON d.kb_id = sd.id WHERE d.deleted = 0 AND d.id = #{id}")
    @Results({
        @Result(column = "kb_name", property = "kbName")
    })
    Document selectById(@Param("id") Long id);

    /**
     * 更新文档摘要信息
     * <p>
     * 用于方案1文档摘要功能，更新摘要相关字段。
     * </p>
     *
     * @param id 文档ID
     * @param summary 摘要内容
     * @param status 摘要状态：pending/processing/completed/failed
     * @param chunks 分块数量
     * @param length 摘要长度
     */
    @Select("UPDATE document SET summary = #{summary}, summary_status = #{status}, summary_chunks = #{chunks}, summary_length = #{length} WHERE id = #{id}")
    void updateSummary(@Param("id") Long id, @Param("summary") String summary,
                       @Param("status") String status, @Param("chunks") Integer chunks,
                       @Param("length") Integer length);
}
