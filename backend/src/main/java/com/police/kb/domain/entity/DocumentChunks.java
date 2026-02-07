package com.police.kb.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文档分块实体
 * <p>
 * 用于存储文档分块后的内容及其摘要。
 * 每个文档可以被分割成多个块，每个块独立存储。
 * </p>
 * <p>
 * 数据库表：document_chunks
 * </p>
 *
 * @author Police KB System
 * @version 1.0.0
 * @since 2026-02-05
 */
@Data
@TableName("document_chunks")
public class DocumentChunks {

    /**
     * 分块ID（主键）
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 所属文档ID
     */
    private Long documentId;

    /**
     * 分块序号
     * <p>
     * 从0开始的序号，用于标识该块在文档中的位置。
     * </p>
     */
    private Integer chunkIndex;

    /**
     * 分块原始内容
     * <p>
     * 该块的完整文本内容。
     * </p>
     */
    private String content;

    /**
     * 分块摘要
     * <p>
     * 该块内容的AI生成摘要。
     * </p>
     */
    private String summary;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;
}
