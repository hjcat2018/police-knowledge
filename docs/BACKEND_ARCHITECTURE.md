# 后端架构设计文档

> **文档版本**: 2.0.0  
> **最后更新**: 2025年1月20日  
> **技术栈**: Spring Boot 3.5.7 | Spring AI 1.1.2 | Sa-Token 1.39.x | SeekDB 1.0

---

## 一、技术选型

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.5.7 | 后端框架 |
| Spring AI | 1.1.2 | AI框架 |
| Spring AI Alibaba | 1.1.2.0 | 阿里云集成 |
| Sa-Token | 1.39.x | 认证授权框架 |
| Sa-Token-JWT | 1.39.x | JWT插件 |
| Redis | 7.x | 缓存/会话 |
| MySQL | 8.0+ | 关系数据库 |
| OceanBase SeekDB | 1.0+ | 向量数据库 |
| WebSocket | Spring WebSocket | 实时通信 |
| Springdoc OpenAPI | 2.x | API文档 |

---

## 二、项目结构

```
backend/
├── src/
│   ├── main/
│   │   ├── java/com/police/kb/
│   │   │   │
│   │   │   ├── KBApplication.java              # 启动类
│   │   │   │
│   │   │   ├── config/                         # 配置类
│   │   │   │   ├── SaTokenConfig.java          # Sa-Token配置
│   │   │   │   ├── SaTokenInterceptorConfig.java # Sa-Token拦截器
│   │   │   │   ├── SaTokenJwtConfig.java       # JWT配置
│   │   │   │   ├── RedisConfig.java            # Redis配置
│   │   │   │   ├── WebSocketConfig.java        # WebSocket配置
│   │   │   │   ├── SeekDBConfig.java           # SeekDB配置
│   │   │   │   ├── SwaggerConfig.java          # API文档配置
│   │   │   │   ├── CorsConfig.java             # 跨域配置
│   │   │   │   ├── AsyncConfig.java            # 异步配置
│   │   │   │   └── CacheConfig.java            # 缓存配置
│   │   │   │
│   │   │   ├── common/                         # 公共模块
│   │   │   │   ├── Result.java                 # 统一响应
│   │   │   │   ├── PageQuery.java              # 分页查询
│   │   │   │   ├── Constants.java              # 常量定义
│   │   │   │   ├── BaseEntity.java             # 实体基类
│   │   │   │   ├── enums/
│   │   │   │   │   ├── VisibilityEnum.java     # 可见性枚举
│   │   │   │   │   ├── DocumentStatusEnum.java # 文档状态枚举
│   │   │   │   │   └── AuditStatusEnum.java    # 审核状态枚举
│   │   │   │   └── exception/
│   │   │   │       ├── BusinessException.java  # 业务异常
│   │   │   │       ├── UnauthorizedException.java # 未授权异常
│   │   │   │       └── GlobalExceptionHandler.java # 全局异常处理
│   │   │   │
│   │   │   ├── dto/                            # 数据传输对象
│   │   │   │   ├── auth/
│   │   │   │   │   ├── LoginRequest.java       # 登录请求
│   │   │   │   │   ├── LoginResponse.java      # 登录响应
│   │   │   │   │   ├── TokenResponse.java      # Token响应
│   │   │   │   │   └── UserDTO.java            # 用户DTO
│   │   │   │   ├── chat/
│   │   │   │   │   ├── ChatRequest.java        # 问答请求
│   │   │   │   │   ├── ChatResponse.java       # 问答响应
│   │   │   │   │   ├── ChatMessageDTO.java     # 消息DTO
│   │   │   │   │   └── SourceDTO.java          # 来源DTO
│   │   │   │   ├── knowledge/
│   │   │   │   │   ├── KnowledgeDTO.java       # 知识库DTO
│   │   │   │   │   ├── KnowledgeQuery.java     # 知识库查询
│   │   │   │   │   ├── KnowledgeCreateRequest.java # 创建请求
│   │   │   │   │   └── KnowledgeUpdateRequest.java # 更新请求
│   │   │   │   ├── document/
│   │   │   │   │   ├── DocumentDTO.java        # 文档DTO
│   │   │   │   │   ├── DocumentQuery.java      # 文档查询
│   │   │   │   │   ├── DocumentUploadRequest.java # 上传请求
│   │   │   │   │   └── DocumentPreviewDTO.java # 预览DTO
│   │   │   │   ├── search/
│   │   │   │   │   ├── SearchRequest.java      # 搜索请求
│   │   │   │   │   ├── SearchResponse.java     # 搜索响应
│   │   │   │   │   ├── SearchResultDTO.java    # 搜索结果
│   │   │   │   │   └── SearchFacetDTO.java     # 搜索分面
│   │   │   │   ├── admin/
│   │   │   │   │   ├── AuditDTO.java           # 审核DTO
│   │   │   │   │   ├── AuditRequest.java       # 审核请求
│   │   │   │   │   ├── StatisticsDTO.java      # 统计DTO
│   │   │   │   │   └── SyncStatusDTO.java      # 同步状态DTO
│   │   │   │   └── common/
│   │   │   │       ├── PageResponse.java       # 分页响应
│   │   │   │       └── IdRequest.java          # ID请求
│   │   │   │
│   │   │   ├── model/                          # 实体类
│   │   │   │   ├── User.java                   # 用户实体
│   │   │   │   ├── Role.java                   # 角色实体
│   │   │   │   ├── KnowledgeBase.java          # 知识库实体
│   │   │   │   ├── Document.java               # 文档实体
│   │   │   │   ├── ChatSession.java            # 对话会话实体
│   │   │   │   ├── ChatMessage.java            # 对话消息实体
│   │   │   │   ├── PublishAudit.java           # 发布审核实体
│   │   │   │   ├── AuditLog.java               # 审计日志实体
│   │   │   │   ├── UserFavorite.java           # 用户收藏实体
│   │   │   │   └── SearchHistory.java          # 搜索历史实体
│   │   │   │
│   │   │   ├── repository/                     # 数据访问层
│   │   │   │   ├── UserRepository.java         # 用户仓储
│   │   │   │   ├── RoleRepository.java         # 角色仓储
│   │   │   │   ├── KnowledgeBaseRepository.java # 知识库仓储
│   │   │   │   ├── DocumentRepository.java     # 文档仓储
│   │   │   │   ├── ChatSessionRepository.java  # 会话仓储
│   │   │   │   ├── ChatMessageRepository.java  # 消息仓储
│   │   │   │   ├── PublishAuditRepository.java # 审核仓储
│   │   │   │   ├── AuditLogRepository.java     # 审计日志仓储
│   │   │   │   └── spec/
│   │   │   │       ├── DocumentSpec.java       # 文档规格
│   │   │   │       └── ChatSessionSpec.java    # 会话规格
│   │   │   │
│   │   │   ├── service/                        # 业务逻辑层
│   │   │   │   ├── AuthService.java            # 认证服务
│   │   │   │   ├── UserService.java            # 用户服务
│   │   │   │   ├── RoleService.java            # 角色服务
│   │   │   │   ├── KnowledgeService.java       # 知识库服务
│   │   │   │   ├── DocumentService.java        # 文档服务
│   │   │   │   ├── ChatService.java            # 问答服务
│   │   │   │   ├── SearchService.java          # 搜索服务
│   │   │   │   ├── AuditService.java           # 审核服务
│   │   │   │   ├── MonitorService.java         # 监控服务
│   │   │   │   ├── SecurityPlatformService.java # 治安平台服务
│   │   │   │   └── impl/                        # 服务实现
│   │   │   │       ├── AuthServiceImpl.java
│   │   │   │       ├── UserServiceImpl.java
│   │   │   │       └── ...
│   │   │   │
│   │   │   ├── controller/                     # 控制器层
│   │   │   │   ├── AuthController.java         # 认证接口
│   │   │   │   ├── UserController.java         # 用户接口
│   │   │   │   ├── KnowledgeController.java    # 知识库接口
│   │   │   │   ├── DocumentController.java     # 文档接口
│   │   │   │   ├── ChatController.java         # 问答接口
│   │   │   │   ├── SearchController.java       # 搜索接口
│   │   │   │   ├── AdminController.java        # 管理接口
│   │   │   │   └── MonitorController.java      # 监控接口
│   │   │   │
│   │   │   ├── ai/                             # AI相关
│   │   │   │   ├── ChatClientConfig.java       # ChatClient配置
│   │   │   │   ├── EmbeddingConfig.java        # 嵌入模型配置
│   │   │   │   ├── VectorStoreConfig.java      # 向量存储配置
│   │   │   │   ├── RAGService.java             # RAG服务
│   │   │   │   ├── RAGServiceImpl.java         # RAG服务实现
│   │   │   │   ├── PromptTemplate.java         # 提示词模板
│   │   │   │   └── Tool/
│   │   │   │       └── KnowledgeBaseTools.java # 知识库工具
│   │   │   │
│   │   │   ├── security/                       # 安全相关
│   │   │   │   ├── JwtUtil.java                # JWT工具
│   │   │   │   └── PasswordEncoder.java        # 密码编码器
│   │   │   │
│   │   │   ├── websocket/                      # WebSocket
│   │   │   │   ├── ChatWebSocketHandler.java   # 聊天处理器
│   │   │   │   └── WebSocketInterceptor.java   # 拦截器
│   │   │   │
│   │   │   └── util/                           # 工具类
│   │   │       ├── DateUtils.java              # 日期工具
│   │   │       ├── StringUtils.java            # 字符串工具
│   │   │       ├── CollectionUtils.java        # 集合工具
│   │   │       └── IdGenerator.java            # ID生成器
│   │   │
│   │   └── resources/
│   │       ├── application.yml                 # 主配置
│   │       ├── application-dev.yml             # 开发环境
│   │       ├── application-prod.yml            # 生产环境
│   │       ├── application-seekdb.yml          # SeekDB配置
│   │       ├── logback-spring.xml              # 日志配置
│   │       └── mapper/                         # MyBatis映射
│   │           ├── UserMapper.xml
│   │           ├── DocumentMapper.xml
│   │           └── ...
│   │
│   └── test/
│       ├── java/com/police/kb/
│       │   ├── KBApplicationTests.java        # 启动测试
│       │   ├── AuthServiceTest.java           # 认证测试
│       │   ├── ChatServiceTest.java           # 问答测试
│       │   └── SearchServiceTest.java         # 搜索测试
│       └── resources/
│           ├── application-test.yml           # 测试配置
│           └── test-data.sql                  # 测试数据
│
├── pom.xml
└── README.md
```

---

## 三、Sa-Token + JWT + Redis 配置

### 3.1 Maven依赖

```xml
<!-- Sa-Token核心 -->
<dependency>
    <groupId>cn.dev33</groupId>
    <artifactId>sa-token-core</artifactId>
    <version>1.39.0</version>
</dependency>

<!-- Sa-Token整合Spring Boot -->
<dependency>
    <groupId>cn.dev33</groupId>
    <artifactId>sa-token-spring-boot-starter</artifactId>
    <version>1.39.0</version>
</dependency>

<!-- Sa-Token JWT插件 -->
<dependency>
    <groupId>cn.dev33</groupId>
    <artifactId>sa-token-jwt</artifactId>
    <version>1.39.0</version>
</dependency>

<!-- Sa-Token整合Redis -->
<dependency>
    <groupId>cn.dev33</groupId>
    <artifactId>sa-token-dao-redis</artifactId>
    <version>1.39.0</version>
</dependency>

<!-- Redis连接池 -->
<dependency>
    <groupId>org.apache.commons</groupId>
    <artifactId>commons-pool2</artifactId>
</dependency>
```

### 3.2 Sa-Token配置类

```java
@Configuration
public class SaTokenConfig {

    /**
     * Sa-Token JWT配置
     */
    @Bean
    public SaTokenJwtConfig saTokenJwtConfig() {
        SaTokenJwtConfig config = new SaTokenJwtConfig();
        config.setSecret("your-256-bit-secret-key-here-must-be-at-least-256-bits");
        config.setExpiration(30 * 60);  // 30分钟
        config.setClockSkew(60);  // 允许60秒时钟偏移
        return config;
    }

    /**
     * Sa-Token DAO - Redis实现
     */
    @Bean
    public SaTokenDao saTokenDao(RedisTemplate<String, Object> redisTemplate) {
        SaTokenDaoRedisImpl dao = new SaTokenDaoRedisImpl();
        dao.setRedisTemplate(redisTemplate);
        dao.setTimeout(30 * 60);  // Token有效期30分钟
        return dao;
    }

    /**
     * Sa-Token管理器
     */
    @Bean
    public SaTokenManager saTokenManager(
            SaTokenDao saTokenDao,
            SaTokenJwtConfig jwtConfig,
            SaTokenInterceptor saTokenInterceptor) {
        
        SaTokenManager manager = new SaTokenManager();
        
        // 配置DAO
        manager.setSaTokenDao(saTokenDao);
        
        // 注册JWT插件
        manager.registerPlugin(new SaTokenJwtPlugin(jwtConfig));
        
        // 配置拦截器
        manager.registerInterceptor(saTokenInterceptor);
        
        return manager;
    }
}
```

### 3.3 Sa-Token拦截器配置

```java
@Configuration
public class SaTokenInterceptorConfig implements WebMvcConfigurer {

    @Autowired
    private SaTokenInterceptor saTokenInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(saTokenInterceptor)
            .addPathPatterns("/api/**")
            .excludePathPatterns(
                // 公开接口
                "/api/v1/auth/login",
                "/api/v1/auth/refresh",
                "/api/v1/auth/captcha",
                // Swagger
                "/swagger-ui/**",
                "/v3/api-docs/**",
                // Actuator
                "/actuator/**"
            );
    }
}
```

### 3.4 认证服务实现

```java
@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 用户登录
     */
    public LoginResponse login(LoginRequest request) {
        // 1. 验证用户是否存在
        User user = userRepository.findByUsername(request.getUsername())
            .orElseThrow(() -> new BusinessException("用户名或密码错误"));

        // 2. 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            // 记录登录失败日志
            saveAuditLog(user.getId(), "LOGIN_FAILED", "密码错误", "auth", user.getId());
            throw new BusinessException("用户名或密码错误");
        }

        // 3. 验证用户状态
        if (user.getStatus() != 1) {
            throw new BusinessException("用户已被禁用");
        }

        // 4. Sa-Token登录
        StpUtil.login(user.getId(), "PC");

        // 5. 获取Token
        String tokenValue = StpUtil.getTokenValue();
        long expiresIn = StpUtil.getTokenTimeout();

        // 6. 更新最后登录时间
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        // 7. 记录登录成功日志
        saveAuditLog(user.getId(), "LOGIN", "用户登录成功", "auth", user.getId());

        // 8. 返回结果
        return LoginResponse.builder()
            .accessToken(tokenValue)
            .refreshToken(generateRefreshToken(user))
            .expiresIn(expiresIn)
            .user(UserDTO.fromUser(user))
            .build();
    }

    /**
     * 用户登出
     */
    public void logout() {
        Long userId = StpUtil.getLoginIdAsLong();
        StpUtil.logout();
        
        // 记录登出日志
        if (userId != null) {
            saveAuditLog(userId, "LOGOUT", "用户登出", "auth", userId);
        }
    }

    /**
     * 获取当前登录用户
     */
    public User getCurrentUser() {
        Long userId = StpUtil.getLoginIdAsLong();
        return userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException("用户不存在"));
    }

    /**
     * 获取当前用户ID
     */
    public Long getCurrentUserId() {
        return StpUtil.getLoginIdAsLong();
    }

    /**
     * 检查是否已登录
     */
    public boolean isLoggedIn() {
        return StpUtil.isLogin();
    }

    /**
     * 检查是否有角色
     */
    public boolean hasRole(String roleCode) {
        User user = getCurrentUser();
        return user.getRole().getCode().equals(roleCode);
    }

    /**
     * 检查是否有权限
     */
    public boolean hasPermission(String permissionCode) {
        User user = getCurrentUser();
        String roleCode = user.getRole().getCode();
        
        // 管理员拥有所有权限
        if ("ADMIN".equals(roleCode)) {
            return true;
        }
        
        // 检查角色权限
        return roleRepository.findByCode(roleCode)
            .map(role -> role.getPermissions().stream()
                .anyMatch(p -> p.getCode().equals(permissionCode)))
            .orElse(false);
    }

    private String generateRefreshToken(User user) {
        return "refresh_" + user.getId() + "_" + System.currentTimeMillis();
    }

    private void saveAuditLog(Long userId, String operation, String desc, String resourceType, Object resourceId) {
        // 审计日志记录逻辑
    }
}
```

### 3.5 权限控制示例

```java
@RestController
@RequestMapping("/api/v1/knowledge")
public class KnowledgeController {

    @Autowired
    private KnowledgeService knowledgeService;

    /**
     * 知识列表 - 登录即可访问
     */
    @GetMapping("/list")
    @SaCheckLogin
    public Result<Page<KnowledgeDTO>> list(KnowledgeQuery query) {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(knowledgeService.list(userId, query));
    }

    /**
     * 知识详情 - 登录即可访问
     */
    @GetMapping("/{id}")
    @SaCheckLogin
    public Result<KnowledgeDTO> getById(@PathVariable Long id) {
        return Result.success(knowledgeService.getById(id));
    }

    /**
     * 创建知识 - 需要管理员或知识管理员角色
     */
    @PostMapping
    @SaCheckRole({"ADMIN", "MANAGER"})
    public Result<KnowledgeDTO> create(@RequestBody @Valid KnowledgeCreateRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(knowledgeService.create(userId, request));
    }

    /**
     * 更新知识 - 需要管理员或所有者
     */
    @PutMapping("/{id}")
    @SaCheckLogin
    public Result<KnowledgeDTO> update(
            @PathVariable Long id,
            @RequestBody @Valid KnowledgeUpdateRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(knowledgeService.update(userId, id, request));
    }

    /**
     * 删除知识 - 需要管理员角色
     */
    @DeleteMapping("/{id}")
    @SaCheckPermission("knowledge:delete")
    public Result<Void> delete(@PathVariable Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        knowledgeService.delete(userId, id);
        return Result.success();
    }

    /**
     * 发布知识 - 需要管理员角色
     */
    @PostMapping("/{id}/publish")
    @SaCheckRole({"ADMIN", "MANAGER"})
    public Result<Void> publish(@PathVariable Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        knowledgeService.publish(userId, id);
        return Result.success();
    }

    /**
     * 设为私有 - 所有者或管理员
     */
    @PostMapping("/{id}/private")
    @SaCheckLogin
    public Result<Void> setPrivate(@PathVariable Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        knowledgeService.setPrivate(userId, id);
        return Result.success();
    }
}
```

---

## 四、核心服务实现

### 4.1 RAG问答服务

```java
@Service
public class RAGService {

    @Autowired
    private ChatClient chatClient;

    @Autowired
    private VectorStore vectorStore;

    @Autowired
    private EmbeddingModel embeddingModel;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private PromptTemplate promptTemplate;

    /**
     * RAG问答（非流式）
     */
    public ChatResponse queryWithRAG(String question, Long knowledgeBaseId, List<String> policeTypes) {
        // 1. 向量化问题
        Embedding questionEmbedding = embeddingModel.embed(question);

        // 2. 构建检索条件
        SearchRequest.Builder searchBuilder = SearchRequest.builder()
            .query(question)
            .topK(5);

        // 添加知识库筛选
        if (knowledgeBaseId != null) {
            searchBuilder.filterExpression("knowledge_base_id = " + knowledgeBaseId);
        }

        // 添加警种筛选
        if (policeTypes != null && !policeTypes.isEmpty()) {
            String policeFilter = policeTypes.stream()
                .map(type -> "police_type = '" + type + "'")
                .collect(Collectors.joining(" OR "));
            searchBuilder.filterExpression(policeFilter);
        }

        // 3. 向量检索
        List<Document> relevantDocs = vectorStore.similaritySearch(searchBuilder.build());

        // 4. 构建提示词
        String context = relevantDocs.stream()
            .map(Document::getContent)
            .collect(Collectors.joining("\n\n"));

        String prompt = promptTemplate.buildRAGPrompt(question, context);

        // 5. 调用LLM
        ChatResponse response = chatClient.prompt()
            .user(prompt)
            .call()
            .chatResponse();

        // 6. 提取来源
        List<SourceDTO> sources = relevantDocs.stream()
            .map(doc -> {
                Document fullDoc = documentRepository.findById(doc.getId()).orElse(null);
                return SourceDTO.builder()
                    .documentId(doc.getId())
                    .title(fullDoc != null ? fullDoc.getTitle() : "")
                    .sourceUrl(fullDoc != null ? fullDoc.getSourceUrl() : "")
                    .source(fullDoc != null ? fullDoc.getSourcePlatform() : "")
                    .score(calculateSimilarity(questionEmbedding, doc.getEmbedding()))
                    .build();
            })
            .collect(Collectors.toList());

        return ChatResponse.builder()
            .messageId(UUID.randomUUID().toString())
            .response(response.getResult().getOutput().getContent())
            .sources(sources)
            .tokens(TokensDTO.builder()
                .prompt(1500)
                .completion(300)
                .build())
            .createdAt(LocalDateTime.now())
            .build();
    }

    /**
     * RAG问答（流式）
     */
    public Flux<String> streamQueryWithRAG(String question, Long knowledgeBaseId) {
        // 构建检索请求
        SearchRequest searchRequest = SearchRequest.builder()
            .query(question)
            .topK(5)
            .filterExpression("knowledge_base_id = " + knowledgeBaseId)
            .build();

        // 获取相关文档
        List<Document> relevantDocs = vectorStore.similaritySearch(searchRequest);

        // 构建提示词
        String context = relevantDocs.stream()
            .map(Document::getContent)
            .collect(Collectors.joining("\n\n"));

        String prompt = promptTemplate.buildRAGPrompt(question, context);

        // 流式调用LLM
        return chatClient.prompt()
            .user(prompt)
            .stream()
            .chatResponse()
            .map(response -> response.getResult().getOutput().getContent());
    }

    private double calculateSimilarity(Embedding a, Embedding b) {
        // 计算余弦相似度
        return CosineSimilarity.of(a, b);
    }
}
```

### 4.2 搜索服务

```java
@Service
public class SearchService {

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private VectorStore vectorStore;

    @Autowired
    private EmbeddingModel embeddingModel;

    /**
     * 混合搜索
     */
    public SearchResponse hybridSearch(SearchRequest request) {
        long startTime = System.currentTimeMillis();

        // 1. 向量化查询
        Embedding queryEmbedding = embeddingModel.embed(request.getKeyword());

        // 2. 构建检索条件
        SearchRequest.Builder searchBuilder = SearchRequest.builder()
            .query(request.getKeyword())
            .topK(request.getSize() != null ? request.getSize() : 20);

        // 添加筛选条件
        if (request.getPoliceType() != null) {
            searchBuilder.filterExpression("police_type = '" + request.getPoliceType() + "'");
        }
        if (request.getBusinessType() != null) {
            searchBuilder.filterExpression("business_type = '" + request.getBusinessType() + "'");
        }
        if (request.getSourceLevel() != null) {
            searchBuilder.filterExpression("source_level = '" + request.getSourceLevel() + "'");
        }
        if (request.getDocType() != null) {
            searchBuilder.filterExpression("doc_type = '" + request.getDocType() + "'");
        }

        // 3. 执行向量搜索
        List<Document> vectorResults = vectorStore.similaritySearch(searchBuilder.build());

        // 4. 获取完整文档信息
        List<SearchResultDTO> results = vectorResults.stream()
            .map(doc -> {
                Document fullDoc = documentRepository.findById(doc.getId()).orElse(null);
                return SearchResultDTO.builder()
                    .documentId(doc.getId())
                    .title(fullDoc != null ? fullDoc.getTitle() : "")
                    .content(truncate(doc.getContent(), 200))
                    .policeType(fullDoc != null ? fullDoc.getPoliceType() : null)
                    .businessType(fullDoc != null ? fullDoc.getBusinessType() : null)
                    .sourceLevel(fullDoc != null ? fullDoc.getSourceLevel() : null)
                    .docType(fullDoc != null ? fullDoc.getDocType() : null)
                    .publishedAt(fullDoc != null ? fullDoc.getCreatedAt() : null)
                    .score(doc.getMetadata().get("score", 0.0))
                    .highlight(buildHighlight(doc.getContent(), request.getKeyword()))
                    .build();
            })
            .collect(Collectors.toList());

        // 5. 生成分面统计
        List<SearchFacetDTO> facets = generateFacets();

        // 6. 保存搜索历史
        saveSearchHistory(request.getKeyword(), results.size());

        long took = System.currentTimeMillis() - startTime;

        return SearchResponse.builder()
            .total((long) results.size())
            .took((int) took)
            .results(results)
            .facets(facets)
            .build();
    }

    /**
     * 获取热门关键词
     */
    public List<HotKeywordDTO> getHotKeywords(int top) {
        return documentRepository.findHotKeywords(top);
    }

    /**
     * 获取搜索建议
     */
    public List<String> getSuggestions(String prefix) {
        return documentRepository.findTitleStartingWith(prefix, 10);
    }

    private String buildHighlight(String content, String keyword) {
        // 构建高亮片段
        return content.replace(keyword, "<em>" + keyword + "</em>");
    }

    private String truncate(String text, int maxLength) {
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength) + "...";
    }

    private void saveSearchHistory(String keyword, int resultCount) {
        // 保存搜索历史
    }

    private List<SearchFacetDTO> generateFacets() {
        // 生成分面统计
        return Arrays.asList(
            SearchFacetDTO.builder().key("policeType").values(Arrays.asList()).build(),
            SearchFacetDTO.builder().key("docType").values(Arrays.asList()).build()
        );
    }
}
```

---

## 五、配置文件

### 5.1 application.yml

```yaml
server:
  port: 8080
  servlet:
    context-path: /

spring:
  application:
    name: police-knowledge-base

  # 数据源
  datasource:
    url: jdbc:mysql://${DB_HOST:localhost}:${DB_PORT:3306}/${DB_NAME:police_kb}?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:password}
    driver-class-name: com.mysql.cj.jdbc.Driver
    hikari:
      minimum-idle: 5
      maximum-pool-size: 20
      idle-timeout: 300000
      max-lifetime: 600000
      connection-timeout: 30000

  # Redis
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD:}
      timeout: 10000ms
      lettuce:
        pool:
          max-active: 20
          max-idle: 10
          min-idle: 5

  # 文件上传
  servlet:
    multipart:
      enabled: true
      max-file-size: 100MB
      max-request-size: 100MB

  # Spring AI
  ai:
    dashscope:
      api-key: ${DASHSCOPE_API_KEY}
      chat:
        options:
          model: qwen-max
          temperature: 0.7
          maxTokens: 2000
      embedding:
        options:
          model: text-embedding-v3
          dimensions: 1024

# Sa-Token配置
sa-token:
  token-name: Authorization
  timeout: 1800
  is-concurrent: false
  is-share: false
  jwt:
    secret: ${JWT_SECRET:your-256-bit-secret-key-here-must-be-at-least-256-bits}
    expiration: 1800
  dao:
    type: redis

# SeekDB配置
seekdb:
  host: ${SEEKDB_HOST:localhost}
  port: ${SEEKDB_PORT:3307}
  database: ${SEEKDB_DATABASE:police_kb}
  table-prefix: kb_

# 日志
logging:
  level:
    root: INFO
    com.police.kb: DEBUG
    cn.dev33.satoken: DEBUG
  file:
    name: logs/police-kb.log

# Actuator
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: always

# Swagger
springdoc:
  api-docs:
    enabled: true
    path: /v3/api-docs
  swagger-ui:
    path: /swagger-ui.html
```

---

## 六、WebSocket配置

```java
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Autowired
    private ChatWebSocketHandler chatWebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(chatWebSocketHandler, "/ws/chat")
            .addInterceptors(new WebSocketInterceptor())
            .setAllowedOrigins("*");
    }

    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }
}
```

```java
@Component
@ServerEndpoint("/ws/chat")
public class ChatWebSocketHandler {

    private static final Map<Long, Session> SESSIONS = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session, @PathParam("token") String token) {
        // 验证Token
        Long userId = validateToken(token);
        if (userId != null) {
            SESSIONS.put(userId, session);
            StpUtil.login(userId, "WEBSOCKET");
        }
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        // 处理消息
        ChatRequest request = JSON.parseObject(message, ChatRequest.class);
        // 调用RAG服务
        RAGService ragService = ApplicationContextHolder.getBean(RAGService.class);
        ragService.streamQueryWithRAG(request.getMessage(), request.getKnowledgeBaseId())
            .subscribe(chunk -> {
                sendMessage(session, chunk);
            });
    }

    @OnClose
    public void onClose(Session session) {
        SESSIONS.entrySet().removeIf(entry -> entry.getValue().equals(session));
    }

    private void sendMessage(Session session, String data) {
        try {
            session.getAsyncRemote().sendText(data);
        } catch (Exception e) {
            log.error("WebSocket发送消息失败", e);
        }
    }

    private Long validateToken(String token) {
        // 验证Token逻辑
        return null;
    }
}
```

---

**文档版本**: 2.0.0  
**最后更新**: 2025年1月20日
