package com.police.kb.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
public class DocumentDTO {

  private Long id;

  @NotBlank(message = "文档标题不能为空")
  @Size(max = 200, message = "标题长度不能超过200个字符")
  private String title;

  @Size(max = 500, message = "摘要长度不能超过500个字符")
  private String summary;

  private String content;

  @NotNull(message = "请选择知识库")
  private Long kbId;

  private Long categoryId;

  private String tags;

  private Integer status;

  private String cover;

  private String source;

  private String originScope;

  private String originDepartment;

  private String author;

  private String sourceTime;

     private String docUrl;
}
