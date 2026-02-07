package com.police.kb.domain.vo;

import lombok.Data;

/**
 * 搜索结果视图对象
 * <p>
 * 用于封装文档搜索的结果信息，包含文档ID、标题、摘要、内容、相似度分数等。
 * </p>
 *
 * @author 黄俊
 * @date 2026-01-23 12:00:00
 */
@Data
public class SearchResult {
    /**
     * 文档ID
     */
    private Long documentId;

    /**
     * 文档标题
     */
    private String title;

    /**
     * 文档摘要
     */
    private String summary;

    /**
     * 文档内容（片段）
     */
    private String content;

    /**
     * 相似度分数
     */
    private Double score;

    /**
     * 知识库名称
     */
     private String kbName;

     public SearchResult() {}

    /**
     * 带参构造函数
     *
     * @param documentId 文档ID
     * @param title      标题
     * @param summary    摘要
     * @param content    内容
     * @param score      相似度分数
     */
     public SearchResult(Long documentId, String title, String summary, String content, Double score) {
         this.documentId = documentId;
         this.title = title;
         this.summary = summary;
         this.content = content;
         this.score = score;
     }
}
