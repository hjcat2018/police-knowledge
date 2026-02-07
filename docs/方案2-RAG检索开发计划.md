# 方案2：RAG检索（SeekDB版）

## 方案概述
```
文档 → 分块 → 向量化 → SeekDB向量库

用户提问 → 向量化 → 检索TopK相关块 → 拼接上下文 → AI回答
```

## 技术架构

| 组件 | 技术选型 | 说明 |
|------|---------|------|
| 分块 | 智能语义分块 | chunkSize=512-1024, 支持Overlap |
| 向量模型 | BGE-m3 / SiliconFlow API | 1024维，支持中英文 |
| 向量库 | **SeekDB (OceanBase)** | 已集成，支持混合搜索 |
| 检索 | l2_distance + 加权融合 | TopK=20, 阈值=0.5 |

## 当前系统状态

### ✅ 已实现组件

| 组件 | 文件 | 状态 |
|------|------|------|
| SeekDB配置 | `backend/.../config/SeekDBConfig.java` | ✅ 完成 |
| 向量服务 | `backend/.../service/impl/VectorServiceImpl.java` | ✅ 完成 |
| RAG服务 | `backend/.../service/impl/RagServiceImpl.java` | ✅ 完成 |
| 智能分块 | `VectorServiceImpl.java:1276-1358` | ✅ 完成 |
| 混合搜索 | `VectorServiceImpl.java:213-282` | ✅ 完成 |

### ⏳ 待实现组件

| 组件 | 文件 | 优先级 |
|------|------|--------|
| RagController | `backend/.../controller/RagController.java` | 高 |
| 聊天RAG集成 | `StreamChatController.java` | 高 |
| 前端RAG API | `frontend/src/api/rag.ts` | 中 |
| 搜索页RAG模式 | `search/index.vue` | 中 |
| 对话页RAG开关 | `chat/normal/index.vue` | 中 |

## 系统架构图

```
┌─────────────────────────────────────────────────────────────────────────┐
│                           RAG系统架构                                    │
└─────────────────────────────────────────────────────────────────────────┘

┌──────────────┐     ┌──────────────┐     ┌──────────────┐
│   前端界面   │     │   后端服务   │     │  向量数据库  │
│              │     │              │     │              │
│ • 搜索页面   │────▶│ • RAG服务   │────▶│ • SeekDB    │
│ • 对话页面   │     │ • 索引服务   │     │ • 混合搜索  │
│ • 知识库选择 │     │ • 检索服务   │     │              │
└──────────────┘     └──────────────┘     └──────────────┘
                               │
                               ▼
                        ┌──────────────┐
                        │   LLM服务    │
                        │ • DeepSeek   │
                        │ • Qwen-Plus  │
                        │ • SiliconFlow│
                        └──────────────┘
```

## SeekDB配置说明

```yaml
# application.yml
seekdb:
  host: localhost
  port: 2881
  username: "root@test"
  password: "root123"
  database: "police_kb"
  collection: "document_vectors"
  dimension: 1024
  metric: COSINE
  hybrid-search-enabled: true
  vector-weight: 0.7
  fulltext-weight: 0.3

  ai:
    provider: siliconflow
    api-url: "https://api.siliconflow.cn/v1/embeddings"
    model-name: "BAAI/bge-m3"

  rag:
    top-k: 20
    similarity-threshold: 0.3
    chunk-size: 512
    chunk-overlap: 150
```

## 已实现功能详情

### VectorServiceImpl.java 核心方法

```java
// ✅ 已实现
public void vectorizeDocument(Long documentId)  // 文档向量化
public void revectorizeDocument(Long documentId) // 重新向量化
public void deleteDocumentVectors(Long documentId) // 删除向量
public List<SearchResult> semanticSearch(...)     // 语义搜索
public List<SearchResult> hybridSearch(...)        // 混合搜索

// ✅ 已实现 - SeekDB混合搜索
private List<SearchResult> seekDBHybridSearch(...)  // 加权融合
private List<SearchResult> weightedFusion(...)    // RRF融合
private List<SearchResult> vectorSimilaritySearch(...)  // l2距离
private List<SearchResult> fullTextSearch(...)    // 全文搜索

// ✅ 已实现 - 智能分块
private List<String> smartSemanticSplit(String content)  // 公安业务优化
private List<String> splitByArticles(String content)     // 法律条文分割
private List<String> splitByParagraphs(String content) // 段落分割
```

### RagServiceImpl.java 核心方法

```java
// ✅ 已实现
public RagAnswer generateAnswer(...)              // 生成RAG回答
public List<DocumentReference> searchRelevantDocuments(...) // 检索文档
public String buildContext(List<DocumentReference> references) // 构建上下文
public String buildPrompt(String question, String context)    // 构建Prompt
public void streamChat(String prompt, ...)        // 流式对话
```

## 实现步骤

> **注意**: 后端向量服务和混合搜索已实现，以下为待开发部分。

### Step 1: 新增RagController REST API

**文件**: `backend/src/main/java/com/police/kb/controller/RagController.java`

```java
@RestController
@RequestMapping("/api/v1/rag")
public class RagController {

    @Autowired
    private RagService ragService;

    @Autowired
    private VectorService vectorService;

    @Autowired
    private DocumentMapper documentMapper;

    /**
     * 为文档建立索引
     */
    @PostMapping("/index/{docId}")
    public Result<String> indexDocument(@PathVariable Long docId) {
        Document doc = documentMapper.selectById(docId);
        if (doc == null) {
            return Result.error("文档不存在");
        }

        // 调用向量服务向量化文档
        vectorService.vectorizeDocument(docId);

        return Result.success("文档索引建立成功");
    }

    /**
     * RAG检索
     */
    @PostMapping("/search")
    public Result<List<SearchResult>> search(@RequestBody RagSearchRequest request) {
        List<SearchResult> results = vectorService.hybridSearch(
            request.getQuery(),
            request.getKbId() != null ? request.getKbId().longValue() : null,
            request.getCategoryId() != null ? request.getCategoryId().longValue() : null,
            request.getOriginScope(),
            request.getOriginDepartment(),
            request.getTopK() != null ? request.getTopK() : 20
        );

        // 过滤低相似度结果
        double threshold = 0.3;
        results = results.stream()
            .filter(r -> r.getScore() >= threshold)
            .limit(request.getTopK() != null ? request.getTopK() : 20)
            .collect(Collectors.toList());

        return Result.success(results);
    }

    /**
     * 删除文档索引
     */
    @DeleteMapping("/index/{docId}")
    public Result<Void> deleteIndex(@PathVariable Long docId) {
        vectorService.deleteDocumentVectors(docId);
        return Result.success(null);
    }

    /**
     * 批量索引
     */
    @PostMapping("/index/batch")
    public Result<Map<String, Object>> batchIndex(@RequestBody BatchIndexRequest request) {
        Map<String, Object> results = new HashMap<>();
        int success = 0;
        int fail = 0;

        for (Long docId : request.getDocIds()) {
            try {
                vectorService.vectorizeDocument(docId);
                results.put("doc_" + docId, "success");
                success++;
            } catch (Exception e) {
                results.put("doc_" + docId, "error: " + e.getMessage());
                fail++;
            }
        }

        results.put("success", success);
        results.put("fail", fail);

        return Result.success(results);
    }
}

@Data
public class RagSearchRequest {
    private String query;
    private Long kbId;
    private Long categoryId;
    private String originScope;
    private String originDepartment;
    private Integer topK = 20;
}

@Data
public class BatchIndexRequest {
    private List<Long> docIds;
}
```

### Step 2: 修改对话接口支持RAG模式

**文件**: `backend/src/main/java/com/police/kb/controller/StreamChatController.java`

```java
@PostMapping(value = "/normal/{conversationId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public SseEmitter normalChat(
        @PathVariable Long conversationId,
        @RequestBody NormalChatRequest request) {

    String question = request.getQuestion();
    String models = request.getModels();
    Boolean useRag = request.getUseRag();        // 是否使用RAG
    List<Long> ragKbIds = request.getRagKbIds(); // RAG检索的知识库ID

    // ... 现有初始化逻辑 ...

    // 如果使用RAG模式
    if (useRag != null && useRag) {
        // 1. 检索相关文档
        List<SearchResult> searchResults = vectorService.hybridSearch(
            question,
            ragKbIds != null && !ragKbIds.isEmpty() ? ragKbIds.get(0) : null,
            null, null, null,
            20
        );

        // 2. 过滤低相似度结果
        searchResults = searchResults.stream()
            .filter(r -> r.getScore() >= 0.3)
            .limit(5)
            .collect(Collectors.toList());

        // 3. 构建RAG上下文
        StringBuilder ragContext = new StringBuilder();
        ragContext.append("\n\n【参考资料】\n");
        for (int i = 0; i < searchResults.size(); i++) {
            SearchResult result = searchResults.get(i);
            ragContext.append(String.format("【参考%d】%s\n%s\n\n",
                i + 1, result.getTitle(), result.getContent()));
        }

        // 4. 将RAG上下文拼接到systemPrompt
        systemPromptBuilder.append(ragContext.toString());
    }

    // ... 继续现有逻辑 ...
}
```

**文件**: `backend/.../domain/dto/NormalChatRequest.java`

```java
public class NormalChatRequest {
    private String question;
    private String models;
    private List<Long> fileIds;
    private Boolean useRag;       // ⬅️ 新增：是否使用RAG
    private List<Long> ragKbIds;  // ⬅️ 新增：RAG检索的知识库ID
    // ... existing fields
}
```

**文件**: `frontend/src/api/rag.ts`

```typescript
import request from '@/utils/request'

export interface RagSearchRequest {
  query: string
  docId?: number
  topK?: number
}

export interface SearchResult {
  docId: number
  title: string
  content: string
  similarity: number
  chunkIndex: number
}

export interface BatchIndexRequest {
  docIds: number[]
}

export const ragSearch = (params: RagSearchRequest) => {
  return request.post<Result<SearchResult[]>>('/v1/rag/search', params)
}

export const indexDocument = (docId: number) => {
  return request.post<Result<string>>(`/v1/rag/index/${docId}`)
}

export const batchIndex = (docIds: number[]) => {
  return request.post<Result<any>>('/v1/rag/index/batch', { docIds })
}

export const deleteIndex = (docId: number) => {
  return request.delete(`/v1/rag/index/${docId}`)
}
```

**文件**: `frontend/src/views/search/index.vue` (新增RAG搜索模式)

```vue
<template>
  <div class="search-container">
    <!-- 搜索模式切换 -->
    <div class="search-mode">
      <el-radio-group v-model="searchMode">
        <el-radio-button value="hybrid">混合搜索</el-radio-button>
        <el-radio-button value="rag">RAG检索</el-radio-button>
      </el-radio-group>
    </div>
    
    <!-- RAG模式下的知识库选择 -->
    <div class="kb-selector" v-if="searchMode === 'rag'">
      <el-select
        v-model="selectedKbIds"
        multiple
        collapse-tags
        placeholder="选择知识库(不选则搜索全部)"
        clearable>
        <el-option
          v-for="kb in knowledgeBases"
          :key="kb.id"
          :label="kb.name"
          :value="kb.id" />
      </el-select>
    </div>
    
    <!-- 搜索输入框 -->
    <div class="search-input">
      <el-input
        v-model="searchKeyword"
        placeholder="输入问题或关键词"
        @keyup.enter="handleSearch"
        clearable>
        <template #append>
          <el-button @click="handleSearch">
            <el-icon><Search /></el-icon>
          </el-button>
        </template>
      </el-input>
    </div>
    
    <!-- 搜索结果 -->
    <div class="search-results" v-if="searchMode === 'rag'">
      <div class="result-item" v-for="(result, index) in searchResults" :key="index">
        <div class="result-header">
          <span class="result-title">{{ result.title }}</span>
          <el-tag type="info" size="small">
            相关度: {{ (result.similarity * 100).toFixed(1) }}%
          </el-tag>
        </div>
        <div class="result-content">{{ result.content }}</div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ragSearch } from '@/api/rag'
import { list as listKb } from '@/api/kb'

const searchMode = ref('rag')
const searchKeyword = ref('')
const selectedKbIds = ref<number[]>([])
const searchResults = ref<SearchResult[]>([])
const knowledgeBases = ref([])

// 搜索处理
const handleSearch = async () => {
  if (!searchKeyword.value.trim()) return
  
  if (searchMode.value === 'rag') {
    // RAG检索模式
    const res = await ragSearch({
      query: searchKeyword.value,
      docId: selectedKbIds.value.length > 0 ? selectedKbIds.value[0] : undefined,
      topK: 10
    })
    searchResults.value = res.data || []
  } else {
    // 混合搜索模式（原有逻辑）
    // ...
  }
}
</script>
```

**文件**: `frontend/src/views/chat/normal/index.vue` (对话页面增加RAG开关)

```vue
<!-- 在ChatHeader中增加 -->
<el-button-group>
  <el-button
    :type="ragMode ? 'primary' : 'default'"
    @click="ragMode = !ragMode">
    <el-icon><Search /></el-icon>
    RAG
  </el-button>
</el-button-group>

<!-- RAG模式时显示知识库选择 -->
<div class="rag-config" v-if="ragMode">
  <el-select
    v-model="ragDocIds"
    multiple
    placeholder="选择关联知识库"
    size="small">
    <el-option
      v-for="kb in knowledgeBases"
      :key="kb.id"
      :label="kb.name"
      :value="kb.id" />
  </el-select>
</div>
```

## 复杂度评估

| 维度 | 评分(1-5) | 说明 |
|------|-----------|------|
| 实现难度 | 2 | SeekDB已集成，主要工作是API和UI |
| 维护成本 | 3 | SeekDB运维，索引更新 |
| Token消耗 | 1 | 只发送相关内容，极省 |
| 准确性 | 5 | 基于检索，准确定位 |
| 可扩展性 | 5 | 支持百万级文档 |

## 预估工作量

> **注意**: 向量服务、混合搜索、智能分块已完成，待开发工作量大幅减少

```
后端: 1.5天
├── RagController.java (0.5天)       ⬅️ 待开发
├── NormalChatRequest新增字段 (0.25天) ⬅️ 待开发
└── StreamChatController集成 (0.75天) ⬅️ 待开发

前端: 1.5天
├── rag.ts API (0.25天)              ⬅️ 待开发
├── 搜索页面RAG模式 (0.75天)          ⬅️ 待开发
└── 对话页面RAG开关 (0.5天)          ⬅️ 待开发

测试: 0.5天
└── 功能测试 + 集成测试

总计: 3.5天
```

## 向量库选型对比

| 向量库 | 优点 | 缺点 | 适用场景 |
|--------|------|------|---------|
| **SeekDB** | OceanBase内置、SQL友好、混合搜索 | 需OceanBase环境 | ✅ 已选型 |
| **ChromaDB** | 轻量、Python友好 | 需独立部署 | 备选 |
| **FAISS** | Facebook开源，性能高 | 纯内存 | 大规模 |
| **Milvus** | 分布式，云原生 | 部署复杂 | 超大规模 |

**当前选型**: SeekDB（OceanBase内置向量库，已集成）

## 风险与对策

| 风险 | 概率 | 影响 | 对策 |
|------|------|------|------|
| SeekDB连接失败 | 低 | 中 | 检查OceanBase连接配置 |
| 向量索引性能 | 中 | 中 | 调整topK和batch大小 |
| 检索不准确 | 中 | 中 | 调优权重参数(0.7/0.3) |
| 前端集成复杂度 | 低 | 低 | 使用现有API模式 |

## 验收标准

- [ ] RagController API正常响应
- [ ] 文档索引建立成功
- [ ] RAG检索返回相关片段
- [ ] 前端支持RAG/普通模式切换
- [ ] 对话时可选择关联知识库
- [ ] 检索延迟<1秒
- [ ] Token消耗降低80%以上

## 后续扩展

- [ ] Reranker（重排序提升准确性）
- [ ] Graph RAG（知识图谱增强）
- [ ] 多知识库联合检索
- [ ] 实时索引更新

## 待开发文件清单

### 后端 (待开发)
```
backend/src/main/java/com/police/kb/controller/RagController.java        ⬅️ 新建
backend/src/main/java/com/police/kb/domain/dto/NormalChatRequest.java   ⬅️ 修改
backend/src/main/java/com/police/kb/controller/StreamChatController.java ⬅️ 修改
```

### 前端 (待开发)
```
frontend/src/api/rag.ts                                            ⬅️ 新建
frontend/src/views/search/index.vue                                 ⬅️ 修改
frontend/src/views/chat/normal/index.vue                            ⬅️ 修改
```

### 已完成 (无需开发)
```
backend/src/main/java/com/police/kb/config/SeekDBConfig.java        ✅
backend/src/main/java/com/police/kb/service/VectorService.java       ✅
backend/src/main/java/com/police/kb/service/impl/VectorServiceImpl.java ✅
backend/src/main/java/com/police/kb/service/RagService.java         ✅
backend/src/main/java/com/police/kb/service/impl/RagServiceImpl.java   ✅
```

---

## 确认清单

请确认以下内容：

- [ ] 使用SeekDB作为向量数据库（已集成）
- [ ] 后端API：RagController
- [ ] 前端：搜索页RAG模式 + 对话页RAG开关
- [ ] 预估工作量：3.5天

**确认后请回复"确认实施"，我将开始开发。**
