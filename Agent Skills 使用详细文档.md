# Agent Skills 使用详细文档

## 一、工具概述

**Agent Skills** 是一个为AI编码代理提供扩展能力的工具包，包含专业的开发指南和最佳实践。

### 已安装的技能包

| 技能名称                        | 描述                 | 主要功能                                      |
| ------------------------------- | -------------------- | --------------------------------------------- |
| **spring-boot-best-practices**  | Spring Boot最佳实践  | 项目结构、RESTful API、MyBatis-Plus、向量搜索 |
| **vercel-react-best-practices** | React最佳实践        | 性能优化指南，包含57条规则                    |
| **vercel-react-native-skills**  | React Native最佳实践 | 移动应用开发优化                              |
| **web-design-guidelines**       | 网页设计指南         | 可访问性、性能和UX审计                        |
| **code-reviewer**               | Java代码审查         | 安全漏洞检查、Spring规范                      |

### 安装的技能文件位置

```
.agents/skills/
├── spring-boot-best-practices/   ⬅️ Spring Boot最佳实践
│   ├── SKILL.md
│   └── AGENTS.md
├── vercel-react-best-practices/
│   ├── SKILL.md
│   ├── AGENTS.md
│   └── rules/
├── vercel-react-native-skills/
├── web-design-guidelines/
└── ...

.opencode/skills/
└── SKILL.md                       ⬅️ Code Reviewer
```

## 二、安装方法

### 技能已本地安装

所有技能已安装在项目目录中：

```bash
.agents/skills/              # Vercel官方技能
├── spring-boot-best-practices/   # Spring Boot最佳实践（本地创建）
├── vercel-react-best-practices/
├── vercel-react-native-skills/
└── web-design-guidelines/

.opencode/skills/            # OpenCode技能
└── SKILL.md                      # Code Reviewer
```

### 本地创建的文件

| 技能          | 文件                                                  | 说明                            |
| ------------- | ----------------------------------------------------- | ------------------------------- |
| Spring Boot   | `.agents/skills/spring-boot-best-practices/SKILL.md`  | 项目结构、RESTful API、向量搜索 |
| Spring Boot   | `.agents/skills/spring-boot-best-practices/AGENTS.md` | 开发规范、检查清单              |
| Code Reviewer | `.opencode/skills/SKILL.md`                           | 安全审查、Spring规范            |

## 三、使用方法

### 1. 自动触发

当AI代理检测到相关任务时，会自动应用相应的技能：

- **Spring Boot最佳实践**：编写Controller、Service、Mapper，处理数据库操作
- **React最佳实践**：编写React组件、实现数据获取、性能优化
- **React Native**：开发移动应用、优化移动性能
- **网页设计**：审查UI、检查可访问性、审计设计
- **Code Reviewer**：审查Java代码安全问题

### 2. 手动引用

在与AI代理交互时，可以明确请求应用特定技能：

```
"请使用spring-boot-best-practices技能创建用户Controller"
"使用code-reviewer审查这个Service的安全问题"
"请使用React最佳实践优化这个组件的性能"
"检查这个页面的可访问性，使用web-design-guidelines"
```

## 四、技能详细介绍

### 1. Spring Boot最佳实践 (spring-boot-best-practices) ⭐⭐⭐

#### 核心功能

- **项目结构**：基于Police KB实际结构（config/controller/service/mapper/domain/common）
- **RESTful API**：统一响应格式、分页查询、SSE流式响应
- **MyBatis-Plus**：LambdaQueryWrapper、分页、逻辑删除
- **向量搜索**：SeekDB集成、混合搜索、分块策略
- **异常处理**：全局异常处理器、业务异常、错误码

#### 项目结构规范

```
src/main/java/com/police/kb/
├── PoliceKBApplication.java       # 启动类
├── common/                        # 公共模块
│   ├── Result.java               # 统一响应
│   ├── GlobalExceptionHandler.java
│   └── ChineseSegmenter.java
├── config/                        # 配置类
│   ├── SeekDBConfig.java         # 向量库配置
│   ├── SaTokenConfig.java       # 认证配置
│   ├── AsyncConfig.java         # 异步线程池
│   └── RedisConfig.java
├── controller/                    # REST API
├── service/                       # 业务逻辑
│   └── impl/
├── mapper/                       # 数据访问
├── domain/                        # 领域模型
│   ├── entity/                   # 实体类
│   ├── dto/                      # 传输对象
│   └── vo/                       # 视图对象
└── ...
```

#### 使用示例

```java
// ✅ 正确：Spring Boot最佳实践
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
               .eq(Document::getDeleted, 0);
        IPage<Document> result = documentService.page(docPage, wrapper);
        return Result.success(PageResult.of(result));
    }
}
```

### 2. React最佳实践 (vercel-react-best-practices)

#### 核心功能

- 包含57条规则，按影响程度排序
- 覆盖8个性能优化类别
- 提供详细的代码示例和解释

#### 规则类别（按优先级）

1. **消除瀑布流** (CRITICAL) - 数据获取优化
2. **包大小优化** (CRITICAL) - 减少bundle大小
3. **服务器端性能** (HIGH) - 服务端渲染优化
4. **客户端数据获取** (MEDIUM-HIGH) - 客户端缓存和去重
5. **重渲染优化** (MEDIUM) - 减少不必要的渲染
6. **渲染性能** (MEDIUM) - 渲染速度优化
7. **JavaScript性能** (LOW-MEDIUM) - 微优化
8. **高级模式** (LOW) - 高级技术

### 3. Code Reviewer (code-reviewer)

#### 核心功能

- **安全漏洞检查**：SQL注入、XSS、敏感数据泄露
- **Spring Boot规范**：注解使用、依赖注入、事务管理
- **代码质量检查**：空指针、资源关闭、魔法值

#### 检查清单

| 检查项   | 示例                                                       |
| -------- | ---------------------------------------------------------- |
| SQL注入  | `wrapper.apply("name LIKE '%" + username + "%'")` ❌       |
|          | `wrapper.like(User::getUsername, "%" + username + "%")` ✅ |
| 敏感日志 | `log.info("密码: {}", user.getPassword())` ❌              |
|          | `log.info("用户登录: {}", user.getUsername())` ✅          |

## 五、文件结构

技能安装在 `.agents/skills/` 目录下，每个技能包含：

- `SKILL.md` - 技能主文档
- `AGENTS.md` - 完整规则集
- `rules/` - 详细规则文件（部分技能）

## 六、最佳实践

1. **后端开发**：始终使用spring-boot-best-practices技能
2. **代码审查**：使用code-reviewer审查安全问题
3. **前端优化**：定期使用React最佳实践审查代码
4. **可访问性**：使用web-design-guidelines确保UI符合标准
5. **持续学习**：定期查看规则更新

## 七、故障排除

### 常见问题

1. **技能未自动触发**：明确请求AI代理使用特定技能
2. **规则应用不正确**：提供更具体的上下文和代码示例
3. **性能未改善**：检查是否应用了正确优先级的规则
4. **Spring Boot项目**：确保使用 Police KB 的项目结构

### 解决方法

- 查看详细规则文档：`.agents/skills/{skill-name}/`
- 参考完整规则集：`.agents/skills/{skill-name}/AGENTS.md`
- 提供具体代码示例和使用场景
- 明确指定要应用的规则类别或优先级

## 八、总结

Agent Skills 工具为AI编码代理提供了专业的开发指南和最佳实践，帮助开发者：

- 编写性能优化的React和React Native代码
- 构建灵活、可维护的组件架构
- 确保网页符合可访问性和UX标准
- 避免常见的性能陷阱和代码质量问题

通过自动触发或手动引用，这些技能可以显著提高开发效率和代码质量，使AI代理能够提供更加专业和准确的开发建议。

---

**最后更新**: 2026-02-05

**当前技能版本**: Spring Boot (本地创建)、React Best Practices (Vercel官方)、Code Reviewer (OpenCode)

## 新功能开发检查清单

- [ ] 是否需要加载agent skill？
- [ ] Spring Boot后端 → 加载 `spring-boot-best-practices`
- [ ] 前端组件开发 → 考虑 `vercel-react-best-practices`
- [ ] 代码审查 → 加载 `code-reviewer`

### 加载skill命令

### 方案3：简单做法

在任务描述中直接写明：

> "请使用 spring-boot-best-practices skill 开发此功能"
