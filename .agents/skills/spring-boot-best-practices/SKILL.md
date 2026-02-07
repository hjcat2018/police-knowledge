---
name: spring-boot-best-practices
description: Spring Boot最佳实践技能，包含项目结构、RESTful API、数据库集成、安全性、性能优化等。用于后端开发任务。
---

# Spring Boot 最佳实践技能

## 项目结构规范

基于 Police Knowledge Base 系统的实际项目结构：

```
src/main/java/com/police/kb/
├── PoliceKBApplication.java       # 启动类
├── common/                        # 公共模块
│   ├── Result.java               # 统一响应
│   ├── GlobalExceptionHandler.java # 全局异常处理
│   ├── ErrorCode.java            # 错误码定义
│   └── ChineseSegmenter.java      # 中文分词器
├── config/                        # 配置类
│   ├── SeekDBConfig.java         # SeekDB向量库配置
│   ├── SaTokenConfig.java        # 认证授权配置
│   ├── AsyncConfig.java          # 异步线程池配置
│   ├── RedisConfig.java          # Redis配置
│   ├── MybatisPlusConfig.java    # MyBatis-Plus配置
│   ├── MinioConfig.java          # MinIO文件存储配置
│   └── ...                       # 其他配置
├── controller/                    # 控制器层（REST API）
│   ├── AuthController.java
│   ├── DocumentController.java
│   ├── SearchController.java
│   ├── ChatController.java
│   └── ...
├── service/                      # 服务层接口
│   └── impl/                     # 服务实现
│       ├── DocumentServiceImpl.java
│       ├── VectorServiceImpl.java
│       └── ...
├── mapper/                       # MyBatis Mapper接口
│   ├── DocumentMapper.java
│   └── ...
├── domain/                       # 领域模型
│   ├── entity/                   # 实体类（对应数据库表）
│   │   ├── Document.java
│   │   ├── User.java
│   │   └── ...
│   ├── dto/                      # 数据传输对象
│   │   ├── DocumentDTO.java
│   │   └── ...
│   └── vo/                      # 视图对象
│       ├── SearchResult.java
│       └── ...
└── ...                          # 其他模块
```

## 目录说明

| 目录 | 职责 | 规范 |
|------|------|------|
| `common/` | 公共组件 | Result、异常处理、工具类 |
| `config/` | 配置类 | 配置Bean、拦截器、过滤器 |
| `controller/` | API入口 | @RestController、请求处理 |
| `service/` | 业务逻辑 | @Service、事务管理 |
| `mapper/` | 数据访问 | @Mapper、数据库操作 |
| `domain/entity/` | 实体 | @TableName、@TableField |
| `domain/dto/` | 请求DTO | Request、DTO后缀 |
| `domain/vo/` | 响应VO | VO后缀、封装响应数据 |

## RESTful API 设计规范

### 1. 统一响应格式（基于实际项目）

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {
    private int code;
    private String message;
    private T data;

    public static <T> Result<T> success(T data) {
        return new Result<>(200, "success", data);
    }

    public static <T> Result<T> success() {
        return new Result<>(200, "success", null);
    }

    public static <T> Result<T> error(String message) {
        return new Result<>(500, message, null);
    }

    public static <T> Result<T> error(int code, String message) {
        return new Result<>(code, message, null);
    }
}
```

### 2. 分页查询（基于实际项目）

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {
    private List<T> records;
    private long total;
    private int size;
    private int current;

    public static <T> PageResult<T> of(IPage<T> page) {
        return new PageResult<>(
            page.getRecords(),
            page.getTotal(),
            (int) page.getSize(),
            (int) page.getCurrent()
        );
    }
}
```

### 3. RESTful 控制器示例（基于实际项目）

```java
@RestController
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
@Tag(name = "文档管理", description = "文档相关API")
public class DocumentController {

    private final DocumentService documentService;

    @GetMapping
    @Operation(summary = "获取文档列表")
    public Result<PageResult<DocumentVO>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String title) {
        Page<Document> docPage = new Page<>(page, size);
        LambdaQueryWrapper<Document> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.isNotBlank(title), Document::getTitle, title)
               .eq(Document::getDeleted, 0)
               .orderByDesc(Document::getCreatedTime);
        IPage<Document> result = documentService.page(docPage, wrapper);
        return Result.success(PageResult.of(result));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取文档详情")
    public Result<DocumentVO> getById(@PathVariable Long id) {
        DocumentVO doc = documentService.getDocumentById(id);
        return Result.success(doc);
    }

    @PostMapping
    @Operation(summary = "创建文档")
    public Result<Long> create(@RequestBody @Valid CreateDocumentRequest request) {
        Long id = documentService.createDocument(request);
        return Result.success(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新文档")
    public Result<Void> update(@PathVariable Long id, 
                               @RequestBody @Valid UpdateDocumentRequest request) {
        documentService.updateDocument(id, request);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除文档（逻辑删除）")
    public Result<Void> delete(@PathVariable Long id) {
        documentService.deleteDocument(id);
        return Result.success();
    }
}
```

### 4. 聊天控制器（基于实际项目StreamChatController）

```java
@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping(value = "/normal/{conversationId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chat(@PathVariable Long conversationId,
                          @RequestBody @Valid NormalChatRequest request) {
        return chatService.streamChat(conversationId, request);
    }
}

@Data
public class NormalChatRequest {
    private String question;
    private String models;
    private List<Long> fileIds;
    private Boolean useRag;
    private List<Long> ragKbIds;
}
```

## 数据库集成规范

### 1. MyBatis-Plus 配置（基于实际项目）

```java
@Configuration
@EnableTransactionManagement
@MapperScan("com.police.kb.mapper")
public class MybatisPlusConfig {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}
```

### 2. 实体类规范（基于实际项目Document）

```java
@Data
@TableName("document")
public class Document implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("title")
    private String title;

    @TableField("content")
    private String content;

    @TableField("summary")
    private String summary;

    @TableField("kb_id")
    private Long kbId;

    @TableField("kb_path")
    private String kbPath;

    @TableField("origin_scope")
    private String originScope;

    @TableField("origin_department")
    private String originDepartment;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;

    @TableLogic
    private Integer deleted;
}
```

### 3. Mapper接口（基于实际项目）

```java
@Mapper
public interface DocumentMapper extends BaseMapper<Document> {

    @Select("SELECT * FROM document WHERE id = #{id} AND deleted = 0")
    Document selectById(@Param("id") Long id);

    @Select("SELECT * FROM document WHERE kb_path LIKE CONCAT('%', #{kbId}, '%') AND deleted = 0")
    List<Document> selectByKbPath(@Param("kbId") Long kbId);
}
```

### 4. 向量服务（Police KB特有）

```java
@Service
@RequiredArgsConstructor
public class VectorServiceImpl implements VectorService {

    private final JdbcTemplate jdbcTemplate;
    private final DocumentMapper documentMapper;
    private final EmbeddingModel embeddingModel;
    private final SeekDBConfig seekDBConfig;

    @Override
    public void vectorizeDocument(Long documentId) {
        Document doc = documentMapper.selectById(documentId);
        if (doc == null || doc.getContent() == null) {
            log.warn("文档不存在或内容为空: {}", documentId);
            return;
        }

        // 删除旧向量
        deleteDocumentVectors(documentId);

        // 分块
        List<String> paragraphs = splitByParagraph(doc.getContent());

        // 生成向量并存储
        for (int i = 0; i < paragraphs.size(); i++) {
            String vector = generateVector(paragraphs.get(i));
            saveVector(documentId, paragraphs.get(i), vector, i);
        }
    }

    @Override
    public List<SearchResult> hybridSearch(String keyword, Long kbId, 
                                           Long categoryId, String originScope, 
                                           String originDepartment, int topK) {
        // 生成查询向量
        String questionVector = generateVector(keyword);

        // 向量相似度搜索
        List<SearchResult> vectorResults = vectorSimilaritySearch(
            keyword, questionVector, kbId, categoryId, originScope, originDepartment, topK);

        // 全文搜索
        List<SearchResult> textResults = fullTextSearch(
            keyword, kbId, categoryId, originScope, originDepartment, topK);

        // 加权融合
        return weightedFusion(vectorResults, textResults);
    }
}
```

## 安全性规范

### 1. XSS 防护

```java
@Component
public class XssFilter extends OncePerRequestFilter {

    private final XssProperties xssProperties;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // 排除的URL
        if (xssProperties.getExcludedUrls().stream()
                .anyMatch(url -> request.getRequestURI().contains(url))) {
            filterChain.doFilter(request, response);
            return;
        }

        // XSS 清洗
        String content = WebUtils.getRequest().getParameter("content");
        if (StringUtils.isNotBlank(content)) {
            content = XssUtils.clean(content);
        }
        // ... 设置清洗后的内容
        filterChain.doFilter(request, response);
    }
}
```

### 2. SQL 注入防护

```java
// 使用 MyBatis-Plus 的 LambdaQueryWrapper 自动防护
LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
wrapper.like(User::getUsername, username);  // 自动转义
wrapper.in(User::getId, ids);             // 自动参数化
```

### 3. 敏感数据处理

```java
@Data
public class UserVO {

    private Long id;

    private String username;

    @JsonIgnore
    private String password;

    @JsonProperty("phone")
    @JsonSerialize(using = PhoneSerializer.class)
    private String phone;

    private String email;
}

// 自定义序列化器
public class PhoneSerializer extends JsonSerializer<String> {
    @Override
    public void serialize(String phone, JsonGenerator gen, SerializerProvider provider)
            throws IOException {
        if (StringUtils.isNotBlank(phone) && phone.length() == 11) {
            gen.writeString(phone.substring(0, 3) + "****" + phone.substring(7));
        } else {
            gen.writeString(phone);
        }
    }
}
```

## 性能优化

### 1. 异步处理

```java
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean("taskExecutor")
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("async-task-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }
}

@Service
public class DocumentService {

    @Async("taskExecutor")
    public CompletableFuture<Void> processDocumentAsync(Long documentId) {
        // 异步处理文档
        return CompletableFuture.completedFuture(null);
    }
}
```

### 2. 缓存使用

```java
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String USER_CACHE_KEY = "user:";

    @Cacheable(value = "users", key = "#id")
    public UserVO getUserById(Long id) {
        String cacheKey = USER_CACHE_KEY + id;
        UserVO cached = (UserVO) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }

        User user = userMapper.selectById(id);
        UserVO vo = convertToVO(user);

        redisTemplate.opsForValue().set(cacheKey, vo, 30, TimeUnit.MINUTES);
        return vo;
    }

    @CacheEvict(value = "users", key = "#id")
    public void updateUser(Long id, UpdateUserRequest request) {
        // 更新逻辑
    }
}
```

### 3. 数据库连接池

```yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    hikari:
      minimum-idle: 5
      maximum-pool-size: 20
      idle-timeout: 30000
      max-lifetime: 1800000
      connection-timeout: 30000
      pool-name: PoliceKBHikariCP
```

## 日志规范

### 1. 日志配置

```yaml
logging:
  level:
    root: INFO
    com.police.kb: DEBUG
    com.police.kb.controller: INFO
    com.police.kb.mapper: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{50} - %msg%n"
  file:
    name: logs/police-kb.log
  logback:
    rolling-policy:
      max-file-size: 100MB
      max-history: 30
```

### 2. 日志使用

```java
@Slf4j
@Service
public class DocumentService {

    public Document getById(Long id) {
        log.debug("查询文档, id={}", id);
        try {
            Document doc = documentMapper.selectById(id);
            if (doc == null) {
                log.warn("文档不存在, id={}", id);
                return null;
            }
            log.info("文档查询成功, id={}, title={}", id, doc.getTitle());
            return doc;
        } catch (Exception e) {
            log.error("查询文档异常, id={}", id, e);
            throw new BusinessException("查询文档失败");
        }
    }
}
```

## 异常处理

### 1. 全局异常处理器（基于实际项目）

```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        log.warn("业务异常: {}", e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Map<String, String>> handleValidationException(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getFieldErrors().forEach(error ->
            errors.put(error.getField(), error.getDefaultMessage())
        );
        log.warn("参数校验失败: {}", errors);
        return Result.error(400, "参数校验失败");
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.error(500, "系统异常，请稍后重试");
    }
}
```

### 2. 自定义业务异常（基于实际项目）

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BusinessException extends RuntimeException {

    private int code;

    public BusinessException(String message) {
        super(message);
        this.code = 500;
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
}
```

### 3. 错误码定义（基于实际项目）

```java
@Data
@AllArgsConstructor
public enum ErrorCode {
    
    SUCCESS(200, "success"),
    
    // 通用错误 1xx
    PARAM_ERROR(1001, "参数错误"),
    NOT_FOUND(1002, "资源不存在"),
    UNAUTHORIZED(1003, "未登录或登录已过期"),
    FORBIDDEN(1004, "没有操作权限"),
    
    // 文档错误 2xx
    DOCUMENT_NOT_FOUND(2001, "文档不存在"),
    DOCUMENT_CREATE_FAILED(2002, "文档创建失败"),
    DOCUMENT_UPDATE_FAILED(2003, "文档更新失败"),
    
    // 向量搜索错误 3xx
    VECTOR_SEARCH_FAILED(3001, "向量搜索失败"),
    EMBEDDING_FAILED(3002, "向量化失败"),
    
    // 文件错误 4xx
    FILE_UPLOAD_FAILED(4001, "文件上传失败"),
    FILE_PARSE_FAILED(4002, "文件解析失败");
    
    private final int code;
    private final String message;
}
```

## Swagger/OpenAPI 文档

```java
@Configuration
@EnableOpenApi
public class SwaggerConfig {

    @Bean
    public OpenAPI policeKBOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Police Knowledge Base API")
                        .version("1.0.0")
                        .description("公安知识库系统接口文档"))
                .externalDocs(new ExternalDocumentation()
                        .description("项目文档")
                        .url("https://police-kb.example.com/docs"));
    }

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("public-api")
                .pathsToMatch("/api/**")
                .build();
    }
}
```

## 常用命令

| 场景 | 命令 |
|------|------|
| 创建Service | `使用ServiceImpl和Service模式` |
| 创建Mapper | `继承BaseMapper` |
| 分页查询 | `Page + LambdaQueryWrapper` |
| 批量操作 | `insertBatchSomeColumn` |
| 逻辑删除 | `@TableLogic` |
| 自动填充 | `MetaObjectHandler` |
| 软删除 | `deleted = 0` 过滤 |

---

## 参考资料

开发Police Knowledge Base系统时，以下官方文档可供参考：

### 核心框架

| 文档 | URL | 用途 |
|------|-----|------|
| Spring Boot官方文档 | https://spring.io/projects/spring-boot | Spring Boot核心框架 |
| Spring AI官方文档 | https://spring.io/projects/spring-ai | AI/向量模型集成 |
| Spring AI Alibaba文档 | https://ai.alibaba.com/docs/ | 阿里AI能力集成 |

### 项目参考

| 文档 | URL | 用途 |
|------|-----|------|
| Spring AI Alibaba Admin | https://github.com/spring-ai-alibaba/spring-ai-alibaba-admin | AI管理后台参考 |
| Spring AI Alibaba Samples | https://github.com/alibaba/spring-ai-alibaba.git | 功能示例代码 |

### 数据库与存储

| 文档 | URL | 用途 |
|------|-----|------|
| OceanBase SeekDB | https://www.oceanbase.ai/docs/ | 向量数据库 |
| MyBatis-Plus | https://baomidou.com/ | ORM框架 |

### 安全认证

| 文档 | URL | 用途 |
|------|-----|------|
| Sa-Token | https://sa-token.dev33.cn/ | 认证授权框架 |

### 中文处理

| 文档 | URL | 用途 |
|------|-----|------|
| Ansj中文分词 | https://github.com/NLPchina/ansj_seg | 中文分词处理 |

---

## 开发建议

### 遇到问题时

1. **优先查看官方文档**：官方文档是最权威的参考资料
2. **参考项目示例**：Spring AI Alibaba Admin提供了完整的功能实现参考
3. **查看Issue**：GitHub项目的Issue中可能有解决方案
4. **阅读源码**：当文档不清晰时，直接阅读源码是最准确的方式

### 技能使用

开发时明确引用技能：

```
"使用spring-boot-best-practices技能创建DocumentController"
"参考Spring AI Alibaba文档实现向量搜索功能"
"使用Ansj中文分词优化文档分块策略"
```

---

**文档版本**: 1.0  
**最后更新**: 2026-02-05
