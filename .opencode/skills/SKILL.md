---
name: code-reviewer
description: 审查Java/Spring Boot代码的最佳实践、安全漏洞、Spring框架规范。用于代码审查、分析和审计任务。
---

# Code Reviewer

## Instructions

在审查Police Knowledge Base系统代码时：

### 1. 安全漏洞检查
- SQL注入：检查LambdaQueryWrapper使用，避免字符串拼接
- XSS攻击：检查用户输入的处理和输出编码
- 敏感数据泄露：检查日志中的敏感信息（手机号、身份证等）
- 权限控制：检查接口是否有适当的权限验证

### 2. Spring Boot最佳实践
- 注解使用：@Service/@Mapper/@RestController等是否正确
- 依赖注入：使用@RequiredArgsConstructor替代构造器注入
- 事务管理：@Transactional使用是否正确
- 异常处理：是否使用全局异常处理器

### 3. 代码质量问题
- 空指针检查：Optional使用或null检查
- 资源关闭：Stream、数据库连接等是否正确关闭
- 魔法值：是否使用常量替代硬编码值
- 方法长度：单个方法是否过长

### 4. 性能问题
- N+1查询：检查是否存在循环查询
- 批量操作：是否应该使用批量操作替代循环操作
- 缓存使用：是否应该添加缓存
- 异步处理：耗时操作是否应该异步执行

### 5. 特定于项目的检查
- Document实体：summary、vector_id等字段使用
- 分页查询：是否使用Page<T>和LambdaQueryWrapper
- 向量搜索：VectorService使用是否正确
- RAG检索：RagService集成是否规范

## 输出格式

以中文返回代码评审结果：

```markdown
## 代码评审报告

### 基本信息
- 文件：`src/main/java/.../XxxService.java`
- 评审时间：2026-02-05
- 评审人：Code Reviewer

### 问题列表

#### 🔴 严重问题

1. **SQL注入风险**
   - 位置：`XxxController.java:45`
   - 问题：`like`查询使用字符串拼接
   - 代码：
   ```java
   wrapper.like("name LIKE '%" + username + "%'");  // ❌ 危险
   ```
   - 建议：
   ```java
   wrapper.like(User::getUsername, username);  // ✅ 安全
   ```

2. **敏感数据泄露**
   - ...
```

## 常见问题模式

### SQL注入
```java
// ❌ 危险
@Select("SELECT * FROM user WHERE name LIKE '%${name}%'")

// ✅ 安全
@Select("SELECT * FROM user WHERE name LIKE #{name}")
wrapper.like(User::getName, "%" + name + "%")
```

### XSS攻击
```java
// ❌ 危险
response.getWriter().write(userInput);

// ✅ 安全
response.getWriter().write(HtmlUtils.htmlEscape(userInput));
```

### 敏感日志
```java
// ❌ 危险
log.info("用户登录: {}", user.getPassword());

// ✅ 安全
log.info("用户登录: {}", user.getUsername());
```

### 资源泄露
```java
// ❌ 危险
InputStream is = null;
try {
    is = new FileInputStream(file);
    // ...
} finally {
    if (is != null) is.close();
}

// ✅ 安全
try (InputStream is = new FileInputStream(file)) {
    // ...
}
```

## 评审检查清单

### 安全性
- [ ] 无SQL注入风险
- [ ] 无XSS攻击风险
- [ ] 敏感数据已脱敏
- [ ] 接口有权限控制

### 规范性
- [ ] 使用LambdaQueryWrapper
- [ ] 分页查询使用Page
- [ ] 批量操作使用insertBatchSomeColumn
- [ ] 逻辑删除使用@TableLogic

### 质量
- [ ] 无空指针风险
- [ ] 资源正确关闭
- [ ] 方法职责单一
- [ ] 异常已处理

### 性能
- [ ] 无N+1查询
- [ ] 批量操作优化
- [ ] 必要处添加缓存
- [ ] 异步处理耗时操作