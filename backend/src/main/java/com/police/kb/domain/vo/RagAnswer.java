package com.police.kb.domain.vo;

import lombok.Data;
import java.util.List;

/**
 * RAG答案视图对象
 * <p>
 * 用于封装RAG检索增强生成的答案信息，包含答案内容、参考文档列表等。
 * </p>
 *
 * @author 黄俊
 * @date 2026-01-23 12:00:00
 */
@Data
public class RagAnswer {

    /**
     * 答案内容
     */
    private String answer;

    /**
     * 参考文档列表
     */
    private List<DocumentReference> references;

    /**
     * 参考文档数量
     */
    private int sourcesCount;

    /**
     * 文档引用内部类
     * <p>
     * 用于封装参考文档的详细信息。
     * </p>
     */
    @Data
    public static class DocumentReference {

        /**
         * 文档ID
         */
        private Long documentId;

        /**
         * 文档标题
         */
        private String title;

        /**
         * 文档内容
         */
        private String content;

        /**
         * 相关性分数
         */
        private double score;
    }
}
