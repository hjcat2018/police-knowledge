# Spring Boot Best Practices - AGENTS.md

## 核心原则

在开发Police Knowledge Base系统时，始终遵循以下原则：

### 1. 项目结构
- 使用标准包结构：`config/controller/service/repository/entity/dto/vo/mapper/common`
- Controller只处理请求转发，不包含业务逻辑
- Service处理业务逻辑，ServiceImpl实现具体逻辑
- Mapper/Repository处理数据库操作
- Entity对应数据库表，使用@TableName注解
- DTO用于请求，VO用于响应

### 2. RESTful API设计
- 使用名词复数形式：`/api/v1/documents`
- HTTP方法对应操作：GET/POST/PUT/DELETE
- 统一响应格式：`{code, message, data}`
- 分页查询统一使用PageResult
- 使用Swagger注解：@Tag/@Operation

### 3. 数据库操作
- 使用MyBatis-Plus，继承BaseMapper<T>
- 使用LambdaQueryWrapper避免硬编码列名
- 分页使用PaginationInnerInterceptor
- 逻辑删除使用@TableLogic注解
- 字段填充使用MetaObjectHandler

### 4. 向量搜索（Police KB特有）
- VectorService处理向量相关逻辑
- 使用SeekDB/OceanBase存储向量
- 支持混合搜索：向量+全文
- 分块策略保持语义完整性

### 5. 安全性
- XSS过滤：清洗用户输入
- SQL注入防护：使用LambdaQueryWrapper参数化
- 敏感数据脱敏：手机号、身份证等
- 接口权限控制：SaToken认证

### 6. 性能优化
- 异步处理：@Async + ThreadPoolTaskExecutor
- 缓存：@Cacheable + Redis
- 数据库连接池：HikariCP
- 批量操作减少数据库访问

### 7. 日志规范
- DEBUG级别：调试信息
- INFO级别：关键操作
- WARN级别：异常但不影响功能
- ERROR级别：异常且需要处理

## 开发规范

### Controller层
```
✅ 做的：
- 参数校验：@Valid + @RequestBody
- 统一响应：Result<T>
- 业务异常：抛出BusinessException
- 接口文档：@Tag/@Operation
- SSE流式响应：SseEmitter

❌ 不要的：
- 业务逻辑
- try-catch（用全局异常处理）
- 直接调用Mapper
```

### Service层
```
✅ 做的：
- 业务逻辑封装
- 事务管理：@Transactional
- 调用多个Mapper
- 缓存逻辑

❌ 不要的：
- HTTP请求处理
- 直接操作Request/Response
```

### Mapper层
```
✅ 做的：
- 简单的CRUD操作
- 复杂查询
- 批量操作

❌ 不要的：
- 业务逻辑
- 事务管理
```

### VectorService（Police KB特有）
```
✅ 做的：
- 文档向量化
- 向量相似度搜索
- 混合搜索融合
- SeekDB配置管理

❌ 不要的：
- 直接操作数据库（用Mapper）
- 业务逻辑判断
```

## Police KB特有检查清单

在完成Police KB代码开发后，检查以下项目：

### 基础规范
- [ ] Controller使用统一响应格式Result
- [ ] 参数使用@Valid校验
- [ ] Service实现类使用@Service和@RequiredArgsConstructor
- [ ] Mapper继承BaseMapper<T>

### 查询规范
- [ ] 查询使用LambdaQueryWrapper
- [ ] 分页查询使用Page<T>
- [ ] 逻辑删除使用@TableLogic
- [ ] 已删除数据过滤条件（deleted = 0）

### 向量搜索（适用时）
- [ ] VectorService正确使用SeekDB
- [ ] 分块策略保持语义完整性
- [ ] 混合搜索权重配置合理
- [ ] 向量生成使用EmbeddingModel

### 安全规范
- [ ] SQL注入防护：LambdaQueryWrapper参数化
- [ ] XSS防护：用户输入清洗
- [ ] 敏感数据已脱敏
- [ ] 接口有权限控制

### 性能优化
- [ ] 使用@Async处理异步任务
- [ ] 配置了线程池（AsyncConfig）
- [ ] 使用Redis缓存
- [ ] 批量操作优化

### 日志规范
- [ ] 关键操作有INFO日志
- [ ] 异常有ERROR日志
- [ ] 不在日志中输出敏感信息
- [ ] 使用log变量（@Slf4j）

## 向量搜索相关检查

对于涉及向量搜索的代码，额外检查：

- [ ] 文档分块保持语义完整性
- [ ] 向量维度与配置一致（1024维）
- [ ] 混合搜索使用l2_distance函数
- [ ] 搜索结果按相似度排序
- [ ] 支持按kb_id/kb_path过滤
- [ ] 支持originScope/originDepartment过滤

---

## 参考资料

开发时参考官方文档：

| 技术 | 文档 |
|------|------|
| Spring Boot | https://spring.io/projects/spring-boot |
| Spring AI | https://spring.io/projects/spring-ai |
| Spring AI Alibaba | https://ai.alibaba.com/docs/ |
| OceanBase SeekDB | https://www.oceanbase.ai/docs/ |
| MyBatis-Plus | https://baomidou.com/ |
| Sa-Token | https://sa-token.dev33.cn/ |
| Ansj中文分词 | https://github.com/NLPchina/ansj_seg |

项目参考：
- Spring AI Alibaba Admin: https://github.com/spring-ai-alibaba/spring-ai-alibaba-admin
- Spring AI Alibaba Samples: https://github.com/alibaba/spring-ai-alibaba.git
