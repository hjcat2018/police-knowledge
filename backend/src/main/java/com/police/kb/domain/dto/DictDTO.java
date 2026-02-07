package com.police.kb.domain.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

public class DictDTO {

    @Data
    public static class TypeDTO {
        @NotBlank(message = "字典类型编码不能为空")
        private String kind;

        @NotBlank(message = "字典类型名称不能为空")
        private String detail;

        private String alias;
        private String description;
        private String icon;
        private String color;
        private Integer sort = 1;
         private Integer status = 1;
    }

    @Data
    public static class DataDTO {
        private Long id;

        @NotBlank(message = "字典编码不能为空")
        private String code;

        @NotBlank(message = "字典名称不能为空")
        private String detail;

        private String alias;
        private String description;
        private String icon;
        private String color;
        private String extConfig;

        @NotNull(message = "请选择层级")
        @Min(value = 1, message = "层级最小值为1")
        @Max(value = 10, message = "层级最大值为10")
        private Integer level;

        private String kind;

        private Long parentId;

        private String parentCode;

        private Integer sort = 1;

        private Integer status = 1;

        private Integer leaf = 0;

        private Integer isSystem = 0;

         private Integer isPublic = 1;

         private String remark;
    }

    @Data
    public static class ImportDTO {
        private String kind;
        private String code;
        private String detail;
        private String alias;
        private String description;
        private String parentCode;
        private Integer level;
        private Integer sort;
        private Integer leaf;

        // 手动添加的getter和setter
        public String getKind() { return kind; }
        public void setKind(String kind) { this.kind = kind; }
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public String getDetail() { return detail; }
        public void setDetail(String detail) { this.detail = detail; }
        public String getAlias() { return alias; }
        public void setAlias(String alias) { this.alias = alias; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getParentCode() { return parentCode; }
        public void setParentCode(String parentCode) { this.parentCode = parentCode; }
        public Integer getLevel() { return level; }
        public void setLevel(Integer level) { this.level = level; }
        public Integer getSort() { return sort; }
        public void setSort(Integer sort) { this.sort = sort; }
        public Integer getLeaf() { return leaf; }
        public void setLeaf(Integer leaf) { this.leaf = leaf; }
    }

    /**
     * 字典导出DTO
     */
    @Data
    public static class ExportDTO {
        private String kind;
        private String code;
        private String detail;
        private String alias;
        private String description;
        private String parentCode;
        private Integer level;
        private Integer sort;
        private Integer leaf;
         private Integer status;

         public ExportDTO() {}

         public ExportDTO(String kind, String code, String detail, String alias, String description,
                          String parentCode, Integer level, Integer sort, Integer leaf, Integer status) {
             this.kind = kind;
             this.code = code;
             this.detail = detail;
             this.alias = alias;
             this.description = description;
             this.parentCode = parentCode;
             this.level = level;
             this.sort = sort;
             this.leaf = leaf;
             this.status = status;
         }
    }

    @Data
    public static class ImportResult {
        private int successCount;
        private int skipCount;
        private java.util.List<String> errors;

        public ImportResult(int successCount, int skipCount, java.util.List<String> errors) {
            this.successCount = successCount;
            this.skipCount = skipCount;
            this.errors = errors;
        }
    }
}
