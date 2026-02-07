# 公安专网知识库系统 - 归属地过滤功能开发计划

**创建日期**: 2026-01-31  
**版本**: v1.0  
**状态**: 待实施

---

## 一、需求概述

### 1.1 功能目标
为知识库系统增加归属地和来源部门过滤功能，支持按以下维度检索文档：
- **归属地**：国家级、部级、省级、市级、县级、单位级
- **来源部门**：如"治安管理支队-二大队"、"刑侦支队-六大队"

### 1.2 核心逻辑
- 文档独立存储归属地（origin_scope）和来源部门（origin_department）
- 支持灵活筛选：可单独使用任一条件，或组合使用
- 向量化搜索支持元数据过滤

### 1.3 过滤逻辑说明
- 选择"省级" → 显示所有 `document.origin_scope = 'provincial'` 的文档
- 文档归属地独立于知识库，同一知识库内不同文档可设置不同归属地
- 来源部门自由输入，支持模糊查询

---

## 二、数据库变更

### 2.1 执行顺序
1. 备份现有sys_dict表
2. 优化sys_dict表结构
3. 初始化字典数据
4. document表新增字段
5. document_vectors表新增字段

### 2.2 文件列表

| 文件名 | 说明 |
|--------|------|
| `backend/sql/001_sys_dict_backup.sql` | 备份字典表 |
| `backend/sql/002_sys_dict_optimize.sql` | 字典表结构优化 |
| `backend/sql/003_sys_dict_init.sql` | 字典数据初始化 |
| `backend/sql/004_document_scope_fields.sql` | document表新增字段 |
| `backend/sql/005_document_vectors_fields.sql` | document_vectors表新增字段 |

---

### 2.3 SQL脚本详情

#### 2.3.1 备份字典表

```sql
-- 文件: backend/sql/001_sys_dict_backup.sql
RENAME TABLE sys_dict TO sys_dict_backup;
```

#### 2.3.2 字典表结构优化

```sql
-- 文件: backend/sql/002_sys_dict_optimize.sql

-- 创建优化后的新表
CREATE TABLE `sys_dict` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  
  -- 核心字段
  `kind` VARCHAR(50) NOT NULL COMMENT '字典类型（如：origin_scope, origin_department, kb_category）',
  `code` VARCHAR(64) NOT NULL COMMENT '节点编码（同一type下唯一）',
  `detail` VARCHAR(160) NOT NULL COMMENT '名称',
  `alias` VARCHAR(160) DEFAULT NULL COMMENT '别名/英文名',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '详细描述',
  
  -- 层级结构
  `parent_id` BIGINT(20) DEFAULT 0 COMMENT '父节点ID（0表示一级节点）',
  `parent_code` VARCHAR(64) DEFAULT NULL COMMENT '父节点编码',
  `level` TINYINT(2) NOT NULL DEFAULT 1 COMMENT '层级（1=一级，2=二级...）',
  `leaf` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否叶子节点：0-否，1-是',
  `sort` INT(11) NOT NULL DEFAULT 0 COMMENT '排序（同级内排序）',
  
  -- 扩展信息
  `icon` VARCHAR(100) DEFAULT NULL COMMENT '图标（如：element-plus图标名）',
  `color` VARCHAR(20) DEFAULT NULL COMMENT '颜色（如：#1890ff）',
  `ext_config` JSON DEFAULT NULL COMMENT '扩展配置（JSON格式）',
  
  -- 状态管理
  `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态：0-停用，1-启用',
  `is_system` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否系统内置：0-否，1-是',
  
  -- 审计字段
  `created_by` BIGINT(20) DEFAULT NULL COMMENT '创建人ID',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` BIGINT(20) DEFAULT NULL COMMENT '更新人ID',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  
  -- 备注
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
  
  PRIMARY KEY (`id`),
  
  -- 唯一约束
  UNIQUE KEY `uk_kind_code` (`kind`, `code`),
  
  -- 索引优化
  KEY `idx_kind` (`kind`),
  KEY `idx_parent` (`parent_id`, `kind`, `sort`),
  KEY `idx_kind_level` (`kind`, `level`, `sort`),
  KEY `idx_status` (`status`),
  KEY `idx_deleted` (`deleted`),
  KEY `idx_created_at` (`created_at`)
  
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci 
COMMENT='系统字典表（优化版）';

-- 数据迁移
INSERT INTO sys_dict 
(`id`, `kind`, `code`, `detail`, `alias`, `description`,
 `parent_id`, `parent_code`, `level`, `leaf`, `sort`,
 `icon`, `color`, `ext_config`, `status`, `is_system`,
 `created_by`, `created_at`, `updated_by`, `updated_at`,
 `remark`, `deleted`)
SELECT 
  `id`,
  `kind`,
  `code`,
  `detail`,
  `alias`,
  NULL,
  IFNULL(`parentId`, 0),
  `superCode`,
  IFNULL(`level`, 1),
  IFNULL(`leaf`, 1),
  IFNULL(`sort`, 0),
  NULL,
  NULL,
  NULL,
  IFNULL(`status`, 1),
  0,
  NULL,
  IFNULL(`createDate`, NOW()),
  NULL,
  IFNULL(`updateDate`, NOW()),
  `remark`,
  0
FROM sys_dict_backup;
```

#### 2.3.3 字典数据初始化

```sql
-- 文件: backend/sql/003_sys_dict_init.sql

-- ----------------------------
-- 1. 归属地字典（origin_scope）
-- ----------------------------
INSERT INTO sys_dict (kind, code, detail, parent_id, parent_code, level, sort, status) VALUES
('origin_scope', 'national', '国家级', 0, NULL, 1, 1, 1),
('origin_scope', 'ministerial', '部级', 0, NULL, 1, 2, 1),
('origin_scope', 'provincial', '省级', 0, NULL, 1, 3, 1),
('origin_scope', 'municipal', '市级', 0, NULL, 1, 4, 1),
('origin_scope', 'county', '县级', 0, NULL, 1, 5, 1),
('origin_scope', 'unit', '单位级', 0, NULL, 1, 6, 1);

-- ----------------------------
-- 2. 来源部门字典（origin_department）- 一级部门
-- ----------------------------
INSERT INTO sys_dict (kind, code, detail, parent_id, parent_code, level, sort, status) VALUES
('origin_department', 'zazhi', '治安管理支队', 0, NULL, 1, 1, 1),
('origin_department', 'xingzheng', '刑侦支队', 0, NULL, 1, 2, 1),
('origin_department', 'jingji', '经侦支队', 0, NULL, 1, 3, 1),
('origin_department', 'jiaojing', '交警支队', 0, NULL, 1, 4, 1),
('origin_department', 'jidu', '禁毒支队', 0, NULL, 1, 5, 1),
('origin_department', 'chukujing', '出入境管理支队', 0, NULL, 1, 6, 1),
('origin_department', 'fazhi', '法制支队', 0, NULL, 1, 7, 1),
('origin_department', 'zhihui', '指挥中心', 0, NULL, 1, 8, 1),
('origin_department', 'qinshi', '勤务支撑', 0, NULL, 1, 9, 1),
('origin_department', 'xunlian', '训练支队', 0, NULL, 1, 10, 1);

-- ----------------------------
-- 3. 来源部门字典 - 二级部门（治安管理支队）
-- ----------------------------
INSERT INTO sys_dict (kind, code, detail, parent_id, parent_code, level, sort, status)
SELECT 'origin_department', 'zazhi_1', '一大队', id, 'zazhi', 2, 1, 1 FROM sys_dict WHERE code = 'zazhi' AND kind = 'origin_department';

INSERT INTO sys_dict (kind, code, detail, parent_id, parent_code, level, sort, status)
SELECT 'origin_department', 'zazhi_2', '二大队', id, 'zazhi', 2, 2, 1 FROM sys_dict WHERE code = 'zazhi' AND kind = 'origin_department';

INSERT INTO sys_dict (kind, code, detail, parent_id, parent_code, level, sort, status)
SELECT 'origin_department', 'zazhi_3', '三大队', id, 'zazhi', 2, 3, 1 FROM sys_dict WHERE code = 'zazhi' AND kind = 'origin_department';

INSERT INTO sys_dict (kind, code, detail, parent_id, parent_code, level, sort, status)
SELECT 'origin_department', 'zazhi_4', '四大队', id, 'zazhi', 2, 4, 1 FROM sys_dict WHERE code = 'zazhi' AND kind = 'origin_department';

INSERT INTO sys_dict (kind, code, detail, parent_id, parent_code, level, sort, status)
SELECT 'origin_department', 'zazhi_5', '五大队', id, 'zazhi', 2, 5, 1 FROM sys_dict WHERE code = 'zazhi' AND kind = 'origin_department';

-- ----------------------------
-- 4. 来源部门字典 - 二级部门（刑侦支队）
-- ----------------------------
INSERT INTO sys_dict (kind, code, detail, parent_id, parent_code, level, sort, status)
SELECT 'origin_department', 'xingzheng_1', '一大队', id, 'xingzheng', 2, 1, 1 FROM sys_dict WHERE code = 'xingzheng' AND kind = 'origin_department';

INSERT INTO sys_dict (kind, code, detail, parent_id, parent_code, level, sort, status)
SELECT 'origin_department', 'xingzheng_2', '二大队', id, 'xingzheng', 2, 2, 1 FROM sys_dict WHERE code = 'xingzheng' AND kind = 'origin_department';

INSERT INTO sys_dict (kind, code, detail, parent_id, parent_code, level, sort, status)
SELECT 'origin_department', 'xingzheng_3', '三大队', id, 'xingzheng', 2, 3, 1 FROM sys_dict WHERE code = 'xingzheng' AND kind = 'origin_department';

INSERT INTO sys_dict (kind, code, detail, parent_id, parent_code, level, sort, status)
SELECT 'origin_department', 'xingzheng_6', '六大队', id, 'xingzheng', 2, 6, 1 FROM sys_dict WHERE code = 'xingzheng' AND kind = 'origin_department';

-- ----------------------------
-- 5. 知识库分类字典（kb_category）- 一级分类
-- ----------------------------
INSERT INTO sys_dict (kind, code, detail, parent_id, parent_code, level, sort, status) VALUES
('kb_category', 'falvfagui', '法律法规', 0, NULL, 1, 1, 1),
('kb_category', 'yewuzhinan', '业务指引', 0, NULL, 1, 2, 1),
('kb_category', 'anjianchuli', '案件处理', 0, NULL, 1, 3, 1),
('kb_category', 'gongzuozhidao', '工作指导', 0, NULL, 1, 4, 1),
('kb_category', 'peixunjiaocai', '培训教材', 0, NULL, 1, 5, 1),
('kb_category', 'anquanzhishi', '安全知识', 0, NULL, 1, 6, 1);

-- ----------------------------
-- 6. 知识库分类字典 - 二级分类
-- ----------------------------
INSERT INTO sys_dict (kind, code, detail, parent_id, parent_code, level, sort, status)
SELECT 'kb_category', 'falvfagui_xingfa', '刑法', id, 'falvfagui', 2, 1, 1 FROM sys_dict WHERE code = 'falvfagui' AND kind = 'kb_category';

INSERT INTO sys_dict (kind, code, detail, parent_id, parent_code, level, sort, status)
SELECT 'kb_category', 'falvfagui_minshi', '民法', id, 'falvfagui', 2, 2, 1 FROM sys_dict WHERE code = 'falvfagui' AND kind = 'kb_category';

INSERT INTO sys_dict (kind, code, detail, parent_id, parent_code, level, sort, status)
SELECT 'kb_category', 'falvfagui_xingzheng', '行政法', id, 'falvfagui', 2, 3, 1 FROM sys_dict WHERE code = 'falvfagui' AND kind = 'kb_category';

INSERT INTO sys_dict (kind, code, detail, parent_id, parent_code, level, sort, status)
SELECT 'kb_category', 'yewuzhinan_bumen', '部门规定', id, 'yewuzhinan', 2, 1, 1 FROM sys_dict WHERE code = 'yewuzhinan' AND kind = 'kb_category';

INSERT INTO sys_dict (kind, code, detail, parent_id, parent_code, level, sort, status)
SELECT 'kb_category', 'yewuzhinan_dashang', '上级指引', id, 'yewuzhinan', 2, 2, 1 FROM sys_dict WHERE code = 'yewuzhinan' AND kind = 'kb_category';

INSERT INTO sys_dict (kind, code, detail, parent_id, parent_code, level, sort, status)
SELECT 'kb_category', 'anjianchuli_anjian', '案件办理', id, 'anjianchuli', 2, 1, 1 FROM sys_dict WHERE code = 'anjianchuli' AND kind = 'kb_category';

INSERT INTO sys_dict (kind, code, detail, parent_id, parent_code, level, sort, status)
SELECT 'kb_category', 'anjianchuli_chengxu', '程序规定', id, 'anjianchuli', 2, 2, 1 FROM sys_dict WHERE code = 'anjianchuli' AND kind = 'kb_category';

-- ----------------------------
-- 7. 验证数据
-- ----------------------------
SELECT kind, code, detail, parent_code, level, status 
FROM sys_dict 
WHERE deleted = 0 AND kind IN ('origin_scope', 'origin_department', 'kb_category')
ORDER BY kind, level, sort;
```

#### 2.3.4 document表新增字段

```sql
-- 文件: backend/sql/004_document_scope_fields.sql

-- 为document表新增归属地和来源部门字段
ALTER TABLE document 
ADD COLUMN origin_scope VARCHAR(20) NULL COMMENT '归属地: national-国家级, ministerial-部级, provincial-省级, municipal-市级, county-县级, unit-单位级' AFTER source,
ADD COLUMN origin_department VARCHAR(200) NULL COMMENT '来源部门(如: 治安管理支队-二大队)' AFTER origin_scope;

-- 创建索引
CREATE INDEX idx_doc_origin_scope ON document(origin_scope);
CREATE INDEX idx_doc_origin_department ON document(origin_department);
```

#### 2.3.5 document_vectors表新增字段

```sql
-- 文件: backend/sql/005_document_vectors_fields.sql

-- 为document_vectors表新增元数据字段
ALTER TABLE document_vectors 
ADD COLUMN origin_scope VARCHAR(20) NULL COMMENT '归属地' AFTER kb_id,
ADD COLUMN origin_department VARCHAR(200) NULL COMMENT '来源部门' AFTER origin_scope;

-- 创建索引
CREATE INDEX idx_vec_origin_scope ON document_vectors(origin_scope);
CREATE INDEX idx_vec_origin_department ON document_vectors(origin_department);
```

---

## 三、后端开发

### 3.1 新增文件

| 文件路径 | 说明 |
|----------|------|
| `backend/src/main/java/com/police/kb/common/OriginScope.java` | 归属地枚举类 |

### 3.2 修改文件

| 文件路径 | 修改内容 |
|----------|----------|
| `backend/src/main/java/com/police/kb/entity/Document.java` | 新增originScope和originDepartment字段 |
| `backend/src/main/java/com/police/kb/dto/DocumentDTO.java` | 新增originScope和originDepartment属性 |
| `backend/src/main/java/com/police/kb/controller/SearchController.java` | SearchRequest新增过滤参数，接口支持过滤 |
| `backend/src/main/java/com/police/kb/controller/DictController.java` | 新增字典查询接口 |
| `backend/src/main/java/com/police/kb/service/RagService.java` | 接口方法增加过滤参数 |
| `backend/src/main/java/com/police/kb/service/impl/RagServiceImpl.java` | 搜索逻辑增加过滤条件构建 |
| `backend/src/main/java/com/police/kb/service/VectorService.java` | 新增searchWithFilters和syncVectorMetadata方法 |
| `backend/src/main/java/com/police/kb/service/impl/VectorServiceImpl.java` | 实现带过滤的向量搜索和元数据同步 |
| `backend/src/main/java/com/police/kb/service/impl/DocumentServiceImpl.java` | 文档保存/更新时同步向量元数据 |
| `backend/src/main/java/com/police/kb/service/DictService.java` | 新增字典查询服务接口 |
| `backend/src/main/java/com/police/kb/service/impl/DictServiceImpl.java` | 实现字典查询逻辑 |

### 3.3 核心代码示例

#### 3.3.1 归属地枚举类

```java
package com.police.kb.common;

/**
 * 归属地枚举
 */
public enum OriginScope {
    national("national", "国家级"),
    ministerial("ministerial", "部级"),
    provincial("provincial", "省级"),
    municipal("municipal", "市级"),
    county("county", "县级"),
    unit("unit", "单位级");
    
    private final String code;
    private final String description;
    
    OriginScope(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
    
    public static OriginScope fromCode(String code) {
        for (OriginScope scope : values()) {
            if (scope.code.equals(code)) {
                return scope;
            }
        }
        return null;
    }
}
```

#### 3.3.2 SearchRequest

```java
@Data
public static class SearchRequest {
    @ApiModelProperty("搜索关键词")
    private String keyword;
    
    @ApiModelProperty("知识库ID")
    private Long kbId;
    
    @ApiModelProperty("归属地过滤: national/ministrial/provincial/municipal/county/unit")
    private String originScope;
    
    @ApiModelProperty("来源部门过滤")
    private String originDepartment;
    
    @ApiModelProperty("返回数量")
    private Integer topK = 10;
}
```

#### 3.3.3 带过滤的向量搜索

```java
public List<SearchResult> searchWithFilters(String keyword, Long kbId,
    Map<String, Object> filters, Integer topK) {
    
    // 1. 文本向量化
    List<Float> queryVector = embedText(keyword);
    
    // 2. 构建SeekDB过滤条件
    SeekDBFilter filter = new SeekDBFilter();
    
    if (kbId != null) {
        filter.addCondition("kb_id", SeekDBOperator.EQ, kbId);
    }
    
    if (filters.containsKey("originScope")) {
        filter.addCondition("origin_scope", SeekDBOperator.EQ, filters.get("originScope"));
    }
    if (filters.containsKey("originDepartment")) {
        filter.addCondition("origin_department", SeekDBOperator.EQ, filters.get("originDepartment"));
    }
    
    // 3. 执行搜索
    return seekDB.search(queryVector, filter, topK);
}
```

#### 3.3.4 字典查询接口

```java
@RestController
@RequestMapping("/api/v1/dict")
public class DictController {
    
    @GetMapping("/list")
    public Result<List<SysDict>> list(@RequestParam String kind) {
        return dictService.getDictList(kind);
    }
    
    @GetMapping("/tree")
    public Result<List<SysDict>> tree(@RequestParam String kind) {
        return dictService.getDictTree(kind);
    }
    
    @GetMapping("/origin-scopes")
    public Result<List<SysDict>> getOriginScopes() {
        return dictService.getDictList("origin_scope");
    }
    
    @GetMapping("/origin-departments")
    public Result<List<SysDict>> getOriginDepartments() {
        return dictService.getDictTree("origin_department");
    }
}
```

---

## 四、前端开发

### 4.1 修改文件

| 文件路径 | 修改内容 |
|----------|----------|
| `frontend/src/types/api.ts` | 新增OriginScope类型和SearchRequest接口 |
| `frontend/src/api/search.ts` | 搜索API支持过滤参数 |
| `frontend/src/api/dict.ts` | 新增字典查询API |
| `frontend/src/views/search/index.vue` | 归属地和部门筛选组件 |
| `frontend/src/views/doc/list/index.vue` | 归属地和部门展示列 |
| `frontend/src/views/doc/list/components/DocDialog.vue` | 归属地和部门录入组件 |

### 4.2 核心代码示例

#### 4.2.1 类型定义

```typescript
// 归属地类型
type OriginScope = 'national' | 'ministerial' | 'provincial' | 'municipal' | 'county' | 'unit';

// 搜索请求接口
interface SearchRequest {
  keyword: string;
  kbId?: number;
  originScope?: OriginScope;
  originDepartment?: string;
  topK?: number;
}

// 归属地选项配置
const ORIGIN_SCOPE_OPTIONS: { value: OriginScope | ''; label: string }[] = [
  { value: '', label: '全部' },
  { value: 'national', label: '国家级' },
  { value: 'ministerial', label: '部级' },
  { value: 'provincial', label: '省级' },
  { value: 'municipal', label: '市级' },
  { value: 'county', label: '县级' },
  { value: 'unit', label: '单位级' }
];
```

#### 4.2.2 搜索页面筛选组件

```vue
<el-form :inline="true" :model="searchForm">
  <!-- 归属地筛选 -->
  <el-form-item label="归属地">
    <el-select v-model="searchForm.originScope" placeholder="请选择归属地" clearable>
      <el-option
        v-for="item in originScopeOptions"
        :key="item.value"
        :label="item.label"
        :value="item.value"
      />
    </el-select>
  </el-form-item>
  
  <!-- 来源部门筛选 -->
  <el-form-item label="来源部门">
    <el-input 
      v-model="searchForm.originDepartment" 
      placeholder="请输入来源部门" 
      clearable
      style="width: 200px;"
    />
  </el-form-item>
  
  <!-- 知识库筛选 -->
  <el-form-item label="知识库">
    <el-select v-model="searchForm.kbId" placeholder="请选择知识库" clearable>
      <el-option
        v-for="kb in knowledgeBaseList"
        :key="kb.id"
        :label="kb.name"
        :value="kb.id"
      />
    </el-select>
  </el-form-item>
</el-form>
```

#### 4.2.3 文档弹窗组件

```vue
<el-form-item label="归属地" prop="originScope">
  <el-select v-model="docForm.originScope" placeholder="请选择归属地" clearable style="width: 100%;">
    <el-option
      v-for="item in originScopeOptions"
      :key="item.value"
      :label="item.label"
      :value="item.value"
    />
  </el-select>
</el-form-item>

<el-form-item label="来源部门" prop="originDepartment">
  <el-input 
    v-model="docForm.originDepartment" 
    placeholder="如: 治安管理支队-二大队"
    maxlength="200"
    show-word-limit
  />
</el-form-item>
```

---

## 五、测试用例

### 5.1 后端测试

```java
@Test
void testSearchWithOriginScope() {
    SearchRequest request = new SearchRequest();
    request.setKeyword("盗窃");
    request.setOriginScope("provincial");
    
    List<SearchResult> results = ragService.semanticSearch(
        request.getKeyword(), 
        null, 
        request.getOriginScope(), 
        null, 
        10
    );
    
    // 验证只返回省级文档
    assertTrue(results.stream().allMatch(r -> 
        "provincial".equals(r.getOriginScope())));
}

@Test
void testSearchWithDepartment() {
    SearchRequest request = new SearchRequest();
    request.setKeyword("盗窃");
    request.setOriginDepartment("治安");
    
    List<SearchResult> results = ragService.hybridSearch(
        request.getKeyword(), 
        null, 
        null, 
        request.getOriginDepartment(), 
        10
    );
    
    // 验证只返回来源部门包含"治安"的文档
    assertTrue(results.stream()
        .allMatch(r -> r.getOriginDepartment() != null 
            && r.getOriginDepartment().contains("治安")));
}

@Test
void testSearchWithBothFilters() {
    SearchRequest request = new SearchRequest();
    request.setKeyword("盗窃");
    request.setOriginScope("provincial");
    request.setOriginDepartment("刑侦");
    
    List<SearchResult> results = ragService.semanticSearch(
        request.getKeyword(), 
        null, 
        request.getOriginScope(), 
        request.getOriginDepartment(), 
        10
    );
    
    // 验证同时满足两个条件
    assertTrue(results.stream().allMatch(r -> 
        "provincial".equals(r.getOriginScope()) 
        && r.getOriginDepartment() != null 
        && r.getOriginDepartment().contains("刑侦")));
}
```

---

## 六、实施清单

### 6.1 数据库变更（优先级：高）

| 序号 | SQL文件 | 内容 | 预估时间 |
|------|---------|------|----------|
| 1 | `001_sys_dict_backup.sql` | 备份字典表 | 1分钟 |
| 2 | `002_sys_dict_optimize.sql` | 字典表结构优化 | 5分钟 |
| 3 | `003_sys_dict_init.sql` | 字典数据初始化 | 2分钟 |
| 4 | `004_document_scope_fields.sql` | document表新增字段 | 2分钟 |
| 5 | `005_document_vectors_fields.sql` | document_vectors表新增字段 | 2分钟 |
| **小计** | | | **12分钟** |

### 6.2 后端开发（优先级：高）

| 序号 | 文件 | 内容 | 预估时间 |
|------|------|------|----------|
| 1 | `OriginScope.java` | 新增枚举类 | 5分钟 |
| 2 | `Document.java` | 实体修改 | 5分钟 |
| 3 | `DocumentDTO.java` | DTO修改 | 5分钟 |
| 4 | `SearchController.java` | 接口修改 | 10分钟 |
| 5 | `DictController.java` | 字典接口新增 | 10分钟 |
| 6 | `RagService.java` | 接口修改 | 5分钟 |
| 7 | `RagServiceImpl.java` | 搜索过滤逻辑 | 15分钟 |
| 8 | `VectorService.java` | 接口新增 | 5分钟 |
| 9 | `VectorServiceImpl.java` | 向量过滤和同步 | 20分钟 |
| 10 | `DocumentServiceImpl.java` | 文档服务同步 | 10分钟 |
| 11 | `DictService.java` | 接口新增 | 5分钟 |
| 12 | `DictServiceImpl.java` | 字典查询实现 | 10分钟 |
| **小计** | | | **105分钟** |

### 6.3 前端开发（优先级：中）

| 序号 | 文件 | 内容 | 预估时间 |
|------|------|------|----------|
| 1 | `api.ts` | 类型定义 | 5分钟 |
| 2 | `search.ts` | 搜索API | 5分钟 |
| 3 | `dict.ts` | 字典API | 5分钟 |
| 4 | `search/index.vue` | 搜索页面 | 20分钟 |
| 5 | `doc/list/index.vue` | 文档列表 | 15分钟 |
| 6 | `DocDialog.vue` | 文档弹窗 | 15分钟 |
| **小计** | | | **65分钟** |

### 6.4 测试（优先级：中）

| 序号 | 内容 | 预估时间 |
|------|------|----------|
| 1 | 单元测试 | 30分钟 |
| 2 | 集成测试 | 30分钟 |
| **小计** | | **60分钟** |

---

## 七、预估工作量

| 阶段 | 时间 |
|------|------|
| 数据库变更 | 12分钟 |
| 后端开发 | 105分钟（1小时45分钟） |
| 前端开发 | 65分钟（1小时5分钟） |
| 测试 | 60分钟（1小时） |
| **总计** | **约4小时** |

---

## 八、风险与注意事项

### 8.1 风险
1. **数据迁移风险**：sys_dict表结构优化时数据可能丢失
2. **向量化同步风险**：文档归属地变更时向量库元数据同步可能失败
3. **前端兼容性风险**：新增筛选组件可能与现有布局冲突

### 8.2 注意事项
1. **备份数据**：执行数据库变更前务必备份
2. **分步执行**：建议分步骤验证后再进行下一步
3. **回滚方案**：保留备份表，便于回滚
4. **测试覆盖**：确保过滤逻辑全面测试
5. **兼容性**：确保向后兼容，不影响现有功能

### 8.3 回滚方案
1. 数据库变更失败：恢复sys_dict_backup备份表
2. 后端代码问题：使用Git回滚
3. 前端问题：使用Git回滚

---

## 九、相关文件路径

```
backend/
├── sql/
│   ├── 001_sys_dict_backup.sql
│   ├── 002_sys_dict_optimize.sql
│   ├── 003_sys_dict_init.sql
│   ├── 004_document_scope_fields.sql
│   └── 005_document_vectors_fields.sql
└── src/main/java/com/police/kb/
    ├── common/
    │   └── OriginScope.java
    ├── entity/
    │   └── Document.java
    ├── dto/
    │   └── DocumentDTO.java
    ├── controller/
    │   ├── SearchController.java
    │   └── DictController.java
    └── service/
        ├── RagService.java
        ├── VectorService.java
        ├── DictService.java
        └── impl/
            ├── RagServiceImpl.java
            ├── VectorServiceImpl.java
            ├── DocumentServiceImpl.java
            └── DictServiceImpl.java

frontend/
└── src/
    ├── types/
    │   └── api.ts
    ├── api/
    │   ├── search.ts
    │   └── dict.ts
    └── views/
        ├── search/
        │   └── index.vue
        └── doc/
            └── list/
                ├── index.vue
                └── components/
                    └── DocDialog.vue
```

---

## 十、验收标准

1. [ ] 数据库变更执行成功，数据完整
2. [ ] 后端接口返回正确的过滤结果
3. [ ] 向量搜索支持归属地和部门过滤
4. [ ] 前端搜索页面可按归属地和部门筛选
5. [ ] 文档管理可设置和展示归属地、来源部门
6. [ ] 字典数据正确加载
7. [ ] 单元测试覆盖核心逻辑
8. [ ] 集成测试通过

---

**开发计划文档结束**

---

## 执行记录

| 日期 | 版本 | 执行人 | 执行内容 | 状态 |
|------|------|--------|----------|------|
| 2026-01-31 | v1.0 | - | 开发计划制定 | 待实施 |
