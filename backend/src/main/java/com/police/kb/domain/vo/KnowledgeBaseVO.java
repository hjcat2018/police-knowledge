package com.police.kb.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
public class KnowledgeBaseVO {

    private Long id;

    private String name;

    private String code;

    private String description;

    private String cover;

    private Long categoryId;

    private String categoryName;

    private Integer status;

    private String statusLabel;

    private Integer isPublic;

    private String isPublicLabel;

    private Integer viewCount;

    private Integer favoriteCount;

    private Integer docCount;

    private LocalDateTime createdTime;

    private String createdBy;

    private LocalDateTime updatedTime;
}
