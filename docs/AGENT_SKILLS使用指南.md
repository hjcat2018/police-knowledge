# Police Knowledge Base - Agent Skills 使用指南

## 一、技能总览

| 技能                    | 目录                                         | 用途             | 优先级 |
| ----------------------- | -------------------------------------------- | ---------------- | ------ |
| **Spring Boot最佳实践** | `.agents/skills/spring-boot-best-practices`  | 后端开发规范     | ⭐⭐⭐ |
| **Code Reviewer**       | `.opencode/skills/`                          | Java代码安全审查 | ⭐⭐⭐ |
| **React最佳实践**       | `.agents/skills/vercel-react-best-practices` | 前端性能优化     | ⭐⭐   |
| **Web设计指南**         | `.agents/skills/web-design-guidelines`       | UI可访问性审计   | ⭐⭐   |

## 二、使用方法

### 1. 自动触发

在任务描述中包含关键词即可自动触发：

| 技能          | 触发关键词                                          |
| ------------- | --------------------------------------------------- |
| Spring Boot   | "创建Service", "RESTful API", "MyBatis", "分页查询" |
| Code Reviewer | "审查代码", "review", "安全检查"                    |
| React最佳实践 | "React组件", "useEffect", "useMemo", "性能优化"     |
| Web设计指南   | "可访问性", "a11y", "UI审查"                        |

### 2. 手动引用

在对话中明确请求：

```
"请使用spring-boot-engineer技能创建用户Service"
"使用code-reviewer审查这个Controller的安全问题"
"使用React最佳实践优化这个组件的性能"
"检查这个页面的可访问性"
```

## 三、技能详细说明

### Spring Boot最佳实践 (⭐⭐⭐ 必用)

#### 项目结构

```
src/main/java/com/police/kb/
├── config/        # 配置类
├── controller/    # 控制器层
├── service/      # 服务层
│   └── impl/     # 实现类
├── domain/       # 领域模型
│    ├── entity/       # 实体类
│    ├── dto/          # 传输对象
│    ├── vo/           # 视图对象
├── mapper/       # MyBatis映射
└── common/       # 公共类
```

#### RESTful API规范

```java
// ✅ 正确：统一响应格式
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    @GetMapping
    public Result<PageResult<UserVO>> list(PageQuery query) {
        // 分页查询
    }

    @PostMapping
    public Result<Void> create(@Valid @RequestBody CreateUserRequest request) {
        // 参数校验
    }
}

// ❌ 错误：直接返回对象
// @GetMapping
// public User getUser(Long id) { return userService.getById(id); }
```

#### 数据库操作

```java
// ✅ 正确：使用LambdaQueryWrapper
LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
wrapper.like(User::getUsername, username)
       .eq(User::getStatus, status)
       .orderByDesc(User::getCreatedTime);

// ✅ 正确：分页查询
Page<User> page = new Page<>(pageNum, pageSize);
IPage<User> result = userService.page(page, wrapper);

// ✅ 正确：批量操作
userService.saveBatch(users);
userService.updateBatchById(users);
```

#### 性能优化

```java
// ✅ 正确：异步处理
@Async("taskExecutor")
public CompletableFuture<Void> processAsync(Long id) {
    // 耗时操作
}

// ✅ 正确：缓存使用
@Cacheable(value = "users", key = "#id")
public User getById(Long id) {
    return userMapper.selectById(id);
}
```

### Code Reviewer (⭐⭐⭐ 必用)

#### 安全检查清单

| 检查项   | 示例                       |
| -------- | -------------------------- |
| SQL注入  | 检查LambdaQueryWrapper使用 |
| XSS攻击  | 检查用户输入处理           |
| 敏感数据 | 检查日志中的手机号、身份证 |
| 权限控制 | 检查接口是否有权限验证     |

#### 常见问题

```java
// ❌ 危险：SQL注入
wrapper.apply("name LIKE '%" + username + "%'");

// ✅ 安全：使用参数化
wrapper.like(User::getUsername, "%" + username + "%");

// ❌ 危险：敏感日志
log.info("用户密码: {}", user.getPassword());

// ✅ 安全：脱敏处理
log.info("用户登录: {}", user.getUsername());
```

### React最佳实践 (⭐⭐ 推荐)

#### 性能优化优先级

| 优先级   | 类别       | 影响           |
| -------- | ---------- | -------------- |
| CRITICAL | 消除瀑布流 | 2-10×改进      |
| CRITICAL | 包大小优化 | 直接影响LCP    |
| HIGH     | 服务端性能 | 减少响应时间   |
| MEDIUM   | 重渲染优化 | 减少不必要渲染 |

#### 常见优化

```typescript
// ❌ 错误：串行请求
const user = await fetchUser()
const posts = await fetchPosts(user.id)

// ✅ 正确：并行请求
const [user, posts] = await Promise.all([
  fetchUser(),
  fetchPosts(user.id)
])

// ❌ 错误：每次渲染都创建对象
<div>{items.filter(i => i.active).map(...)}</div>

// ✅ 正确：使用useMemo
const activeItems = useMemo(
  () => items.filter(i => i.active),
  [items]
)
```

### Web设计指南 (⭐⭐ 推荐)

#### 可访问性检查

| 检查项   | 要求                   |
| -------- | ---------------------- |
| 图像alt  | 所有图片有alt文本      |
| 键盘导航 | 支持Tab导航            |
| ARIA标签 | 复杂组件有ARIA属性     |
| 颜色对比 | 文本与背景对比度≥4.5:1 |

## 四、常见使用场景

### 场景1：创建新的Service

```
请创建一个DocumentService，包含：
- 分页查询文档列表
- 根据ID查询文档详情
- 创建文档
- 更新文档
- 删除文档（逻辑删除）
使用Spring Boot最佳实践技能。
```

### 场景2：代码安全审查

```
请使用code-reviewer审查以下代码：
[粘贴Controller代码]
重点检查：
- SQL注入风险
- XSS攻击
- 敏感数据泄露
```

### 场景3：前端组件优化

```
当前聊天页面加载很慢，请使用React最佳实践技能优化：
[描述问题]
预期优化：
- 消除数据请求瀑布流
- 使用React.memo减少重渲染
- 优化包大小
```

### 场景4：UI可访问性审计

```
请使用web-design-guidelines审查聊天页面：
[描述或提供页面代码]
检查：
- ARIA标签完整性
- 键盘导航支持
- 颜色对比度
```

## 五、技能文件结构

```
.agents/skills/
├── spring-boot-best-practices/
│   ├── SKILL.md              # Spring Boot技能主文档
│   └── AGENTS.md             # Agent规则集
├── vercel-react-best-practices/
│   ├── SKILL.md
│   ├── AGENTS.md
│   └── rules/                # 57条规则
├── web-design-guidelines/
│   └── ...
└── vercel-react-native-skills/
    └── ...

.opencode/skills/
└── SKILL.md                  # Code Reviewer技能
```

## 六、任务开始前检查清单 ⭐⭐⭐

### 6.1 检查流程

在开始任何开发任务前，依次检查：

```
Step 1: 识别技术栈
  □ 涉及后端开发（Java/Spring Boot）？
  □ 涉及前端UI（Vue/React）？
  □ 涉及性能优化？
  □ 涉及安全相关代码？

Step 2: 确定是否需要加载Skill
  □ 新模块/新功能开发 → 是
  □ 安全敏感代码（认证、授权） → 是
  □ 复杂架构设计 → 是
  □ 简单bug修复 → 可选

Step 3: 加载Skill并执行
  加载对应Skill → 阅读最佳实践 → 执行开发 → 代码审查
```

### 6.2 场景对照表

| 场景 | 应该加载的Skill |
|------|----------------|
| 开发新的Service层 | `spring-boot-best-practices` |
| 实现WebSocket | `spring-boot-best-practices` |
| 添加Redis缓存 | `spring-boot-best-practices` |
| Vue/React组件开发 | `vercel-react-best-practices` |
| 代码审查/安全审计 | `code-reviewer` |
| UI样式/无障碍 | `web-design-guidelines` |
| 组件架构设计 | `vercel-composition-patterns` |

### 6.3 重要提醒

⚠️ **以下情况必须加载Skill**：
- 新模块开发（DocumentSummary、XxxService等）
- 安全相关代码（认证、授权、加密）
- 异步处理、WebSocket等复杂功能
- 性能敏感代码

⚠️ **开发后必须审查**：
- 所有新代码 → `code-reviewer`
- 前端组件 → `vercel-react-best-practices`
- 页面UI → `web-design-guidelines`

---

## 七、最佳实践

### 1. 开发前

- [ ] 了解相关技能文档
- [ ] 确认使用正确的技能

### 2. 开发中

- [ ] 遵循技能规范
- [ ] 有疑问时参考技能文档

### 3. 开发后

- [ ] 使用Code Reviewer检查安全问题
- [ ] 使用React最佳实践检查性能
- [ ] 使用Web设计指南检查可访问性

## 八、故障排除

### 技能未触发？

明确指定技能名称：

```
"使用spring-boot-best-practices技能创建Controller"
```

### 规则应用不正确？

提供更具体的上下文：

```
"按照Spring Boot最佳实践中的Service层规范创建这个Service"
```

### 代码质量未改善？

检查是否应用了正确优先级的规则：

```
"优先解决CRITICAL级别的问题：消除数据请求瀑布流"
```

## 八、相关文档

| 文档 | 位置 |
| ---- | ---- |
| Spring Boot技能 | `.agents/skills/spring-boot-best-practices/SKILL.md` |
| Code Reviewer | `.opencode/skills/SKILL.md` |
| React最佳实践 | `.agents/skills/vercel-react-best-practices/SKILL.md` |
| Web设计指南 | `.agents/skills/web-design-guidelines/SKILL.md` |
| **Agent Skills检查清单** | `docs/AGENT_SKILLS_CHECKLIST.md` |

---

**最后更新**: 2026-02-05
