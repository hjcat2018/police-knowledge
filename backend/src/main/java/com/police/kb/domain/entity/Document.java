package com.police.kb.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("document")
public class Document extends BaseEntity implements Serializable {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 文档标题
     */
    private String title;

    /**
     * 文档内容
     */
    private String content;

    /**
     * 文档摘要
     */
    private String summary;

    /**
     * 摘要状态（pending-待生成, processing-生成中, completed-已完成, failed-失败）
     */
    private String summaryStatus;

    /**
     * 摘要分块数量
     */
    private Integer summaryChunks;

    /**
     * 摘要长度
     */
    private Integer summaryLength;

    /**
     * 文档状态（0-草稿，1-已发布，2-待审核）
     */
    private Integer status;

    /**
     * 所属知识库ID
     */
    private Long kbId;

    /**
     * 知识库路径（逗号分隔的ID列表，用于级联查询）
     * <p>如：16,17 表示该文档属于刑法，其父级是法律法规</p>
     */
    private String kbPath;

    /**
     * 所属知识库名称（非数据库字段，用于展示）
     */
    @TableField(exist = false)
    private String kbName;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 标签（多个标签用逗号分隔）
     */
    private String tags;

    /**
     * 浏览次数
     */
    private Integer viewCount;

    /**
     * 收藏次数
     */
    private Integer favoriteCount;

    /**
     * 是否置顶（0-否，1-是）
     */
    private Integer isTop;

    /**
     * 是否热门（0-否，1-是）
     */
    private Integer isHot;

    /**
     * 封面图URL
     */
    private String cover;

    /**
     * 来源
     */
    private String source;

    /**
     * 归属地
     * <p>
     * national-国家级, ministerial-部级, provincial-省级, municipal-市级, county-县级, unit-单位级
     * </p>
     */
    private String originScope;

    /**
     * 来源部门
     * <p>
     * 如：治安管理支队-二大队、刑侦支队-六大队等
     * </p>
     */
    private String originDepartment;

    /**
     * 作者
     */
    private String author;

    /**
     * 发布时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime publishTime;

    /**
     * 文档来源时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime sourceTime;

    /**
     * 文档地址链接（MinIO存储后的URL）
     */
     private String docUrl;

     /**
      * 文档状态枚举
      */
    public enum DocumentStatus {
        /**
         * 草稿
         */
        DRAFT(0, "草稿"),
        /**
         * 已发布
         */
        PUBLISHED(1, "已发布"),
        /**
         * 待审核
         */
        PENDING_REVIEW(2, "待审核");

        /**
         * 状态值
         */
        private final int value;
        /**
         * 状态描述
         */
        private final String description;

        /**
         * 构造函数
         *
         * @param value       状态值
         * @param description 状态描述
         */
        DocumentStatus(int value, String description) {
            this.value = value;
            this.description = description;
        }

        /**
         * 获取状态值
         *
         * @return 状态值
         */
        public int getValue() {
            return value;
        }

        /**
         * 获取状态描述
         *
         * @return 状态描述
         */
        public String getDescription() {
            return description;
        }

        /**
         * 根据值获取枚举常量
         *
         * @param value 状态值
         * @return 枚举常量
         */
        public static DocumentStatus fromValue(int value) {
            for (DocumentStatus status : values()) {
                if (status.value == value) {
                    return status;
                }
            }
            return DRAFT;
        }
    }
}
