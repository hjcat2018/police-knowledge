package com.police.kb.domain.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.police.kb.domain.entity.Document;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 文档视图对象
 * <p>
 * 用于封装文档展示信息，继承Document实体并添加关联字段。
 * 包含知识库名称、分类名称、状态名称等扩展信息。
 * </p>
 *
 * @author 黄俊
 * @date 2026-01-23 12:00:00
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DocumentVO extends Document {

  /**
   * 知识库名称
   */
  private String kbName;

  /**
   * 分类名称
   */
  private String categoryName;

  /**
   * 状态名称
   */
  private String statusName;

  /**
   * 文档地址链接（MinIO存储后的URL）
   */
  private String docUrl;

  /**
   * 获取状态名称
   * <p>
   * 根据状态值返回对应的状态文本。
   * </p>
   *
   * @return 状态名称
   */
  public String getStatusName() {
    if (getStatus() == null)
      return "";
    return switch (getStatus()) {
      case 0 -> "草稿";
      case 1 -> "已发布";
      case 2 -> "待审核";
      default -> "未知";
    };
  }
}
