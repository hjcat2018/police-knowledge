# Agent Skills 使用检查清单

## 目的
确保重要开发任务执行前，先加载对应的agent skill获取最佳实践。

---

## 可用 Skills

| Skill名称 | 用途 | 触发关键词 |
|-----------|------|-----------|
| `spring-boot-best-practices` | Spring Boot后端开发 | Spring、Java、后端服务 |
| `code-reviewer` | 代码审查、安全审计 | 审查、审计、安全检查 |
| `web-design-guidelines` | UI设计、无障碍检查 | UI、UX、无障碍、样式 |
| `vercel-react-best-practices` | React/Next.js性能优化 | React、Next.js、前端性能 |
| `vercel-composition-patterns` | React组件设计模式 | 组件、render props、context |

---

## 任务开始前检查

### Step 1: 识别技术栈
```
□ 涉及后端开发？  → Spring Boot
□ 涉及前端UI？    → Web Design
□ 涉及性能优化？  → Vercel best practices
□ 需要代码审查？  → Code Reviewer
```

### Step 2: 检查是否需要Skill
```
高级功能开发？     ☐ 加载skill
安全敏感代码？     ☐ 加载skill
复杂架构设计？     ☐ 加载skill
```

### Step 3: 执行开发
```
加载skill → 阅读最佳实践 → 执行开发 → 代码审查
```

---

## Skill加载方式

```bash
# 方式1：使用Task工具选择skill
# 方式2：在提示词中直接指定
# 方式3：手动加载
```

---

## 场景对照表

| 场景 | 应该加载的Skill |
|------|----------------|
| 开发新的Service层 | `spring-boot-best-practices` |
| 实现WebSocket | `spring-boot-best-practices` |
| 添加Redis缓存 | `spring-boot-best-practices` |
| Vue/React组件开发 | `vercel-react-best-practices` |
| 代码审查/安全检查 | `code-reviewer` |
| UI样式/无障碍 | `web-design-guidelines` |
| 组件架构设计 | `vercel-composition-patterns` |

---

## 注意事项

1. **重要功能必须加载**：新模块开发、安全相关代码
2. **可选加载**：常规功能、简单修改
3. **始终加载**：代码审查、安全审计

---

## 使用示例

### 开发前
```
任务："实现文档摘要功能的后端服务"
检查 → Spring Boot相关 → 加载 spring-boot-best-practices
```

### 开发后
```
任务："审查DocumentSummaryService代码"
检查 → 代码审查 → 加载 code-reviewer
```
