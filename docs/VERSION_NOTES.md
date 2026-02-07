# 版本更新说明 (VERSION_NOTES)

> **当前版本**: 2.0.0  
> **发布日期**: 2025年1月20日  
> **前序版本**: 1.0.0  
> **应用场景**: 公安专网知识库系统 | 面向基层民警

---

## 一、版本兼容性矩阵

| 组件 | 1.0.0 (旧) | 2.0.0 (新) | 兼容性 |
|------|-----------|-----------|--------|
| **Spring AI** | 1.0.0 | 1.1.2 | ✅ 兼容 |
| **Spring AI Alibaba** | 1.0.0.2 | 1.1.2.0 | ✅ 兼容 |
| **Spring Boot** | 3.2.x | 3.5.7 | ✅ 兼容 |
| **JDK** | 17 | 21 | ⚠️ 需升级 |
| **SeekDB** | 1.0+ | 1.0+ | ✅ 兼容 |
| **安全框架** | Spring Security | **Sa-Token 1.39.x** | ❌ 不兼容 |

---

## 二、主要变更

### 2.1 公安专网适配（新增）

- 公安专网安全架构设计
- 等保三级安全适配
- 统一身份认证（专网SSO）对接
- 完整审计日志体系
- HTTPS强制配置

### 2.2 安全框架重构

**原方案**: Spring Security
**新方案**: Sa-Token + JWT + Redis

```java
// Sa-Token登录
StpUtil.login(user.getId(), "PC");
String token = StpUtil.getTokenValue();

// Sa-Token权限注解
@SaCheckLogin           // 需要登录
@SaCheckRole("ADMIN")   // 需要角色
@SaCheckPermission("user:delete")  // 需要权限
```

### 2.3 前端架构（新增）

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.5+ | 前端框架 |
| TypeScript | 5.x | 类型安全 |
| Vite | 6.x | 构建工具 |
| Element Plus | 2.x | UI组件库 |
| Pinia | 2.x | 状态管理 |

### 2.4 权限管理模型（优化）

- 私有文档（仅本人）+ 共享文档（全局）
- 审核发布流程
- 机制类文档特殊处理
- 三级用户角色（普通民警/知识管理员/系统管理员）

### 2.5 文档分类体系（新增）

| 分类维度 | 说明 | 示例 |
|----------|------|------|
| **警种** | 按警种分类 | 治安、刑侦、交警、禁毒 |
| **业务** | 按业务分类 | 户籍管理、治安管理、刑事侦查 |
| **来源** | 按来源分类 | 公安部、省厅、市局、区县局 |
| **类型** | 按类型分类 | 法律法规、规章制度、培训资料 |

### 2.6 外部系统对接（新增）

- 治安平台数据同步
- 定时拉取（每天凌晨2点）
- 手动同步支持
- 多格式文档处理（Word/PDF/TXT/Excel）
- 原文链接保留

### 2.7 监控运维体系（新增）

- 同步状态监控
- 查询热度统计
- 用户行为分析
- 完整告警机制

### 2.8 智能检索增强（优化）

- 语义理解（打架 → 殴打他人）
- 地域适配（市局/区县级文件优先级）
- 时效性排序（新规定优先显示）
- 双模式交互（Web搜索 + 智能对话）

---

## 三、新增文档

### 3.1 架构设计文档

| 文档 | 说明 |
|------|------|
| FRONTEND_ARCHITECTURE.md | 前端架构设计（Vue 3 + Vite + Element Plus） |
| BACKEND_ARCHITECTURE.md | 后端架构设计（Spring Boot + Sa-Token + Spring AI） |
| DATABASE_DESIGN.md | 数据库设计（MySQL + SeekDB） |

### 3.2 文档内容变更

| 文档 | 变更内容 |
|------|----------|
| ARCHITECTURE.md | 新增公安专网架构、权限模型、治安平台对接、监控运维 |
| DEVELOPMENT_GUIDE.md | 新增公安场景需求、文档分类体系、权限模型说明 |
| API.md | 新增权限管理API、监控运维API、治安平台对接API |
| DEPLOYMENT.md | 新增公安专网部署架构、等保配置、SSO对接、运维手册 |

---

## 四、技术栈变更

### 4.1 前端技术栈

| 技术 | 1.0.0 | 2.0.0 |
|------|-------|-------|
| Vue | 未指定 | **3.5+** |
| TypeScript | 否 | **是** |
| 构建工具 | 未指定 | **Vite 6.x** |
| UI库 | 未指定 | **Element Plus 2.x** |
| 状态管理 | 未指定 | **Pinia 2.x** |
| 认证 | 未指定 | **Sa-Token-Front** |

### 4.2 后端技术栈

| 技术 | 1.0.0 | 2.0.0 |
|------|-------|-------|
| 安全框架 | Spring Security | **Sa-Token 1.39.x** |
| Token | 未指定 | **JWT** |
| 会话存储 | 未指定 | **Redis 7.x** |
| API文档 | 未指定 | **Springdoc OpenAPI** |

---

## 五、升级指南

### 5.1 环境准备

```bash
# 检查JDK版本
java -version  # 需要JDK 21

# 安装JDK 21 (如果需要)
sdk install java 21.0.1-tem
sdk use java 21.0.1-tem
```

### 5.2 Maven依赖更新

```xml
<!-- 删除Spring Security -->
<!-- <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency> -->

<!-- 新增Sa-Token -->
<dependency>
    <groupId>cn.dev33</groupId>
    <artifactId>sa-token-spring-boot-starter</artifactId>
    <version>1.39.0</version>
</dependency>
<dependency>
    <groupId>cn.dev33</groupId>
    <artifactId>sa-token-jwt</artifactId>
    <version>1.39.0</version>
</dependency>
<dependency>
    <groupId>cn.dev33</groupId>
    <artifactId>sa-token-dao-redis</artifactId>
    <version>1.39.0</version>
</dependency>
```

### 5.3 配置迁移

```yaml
# 原Spring Security配置 (删除)
# security:
#   jwt:
#     secret: xxx

# 新Sa-Token配置
sa-token:
  token-name: Authorization
  timeout: 1800
  is-concurrent: false
  jwt:
    secret: ${JWT_SECRET:your-256-bit-secret-key-here}
    expiration: 1800
  dao:
    type: redis
```

### 5.4 代码迁移

```java
// 原Spring Security (删除)
// @EnableWebSecurity
// public class SecurityConfig extends WebSecurityConfigurerAdapter { ... }

// 新Sa-Token
@Configuration
public class SaTokenConfig {
    @Bean
    public SaTokenManager saTokenManager(...) { ... }
}

// 原权限控制 (替换)
// @PreAuthorize("hasRole('ADMIN')")
@SaCheckRole("ADMIN")

// @PreAuthorize("hasPermission('user:delete')")
@SaCheckPermission("user:delete")
```

---

## 六、已知问题

### 6.1 密码加密

系统使用BCrypt加密，初始密码：
- **admin**: admin123

### 6.2 构建环境要求

- JDK 21
- Maven 3.8+
- Node.js 18+

---

## 七、测试验证

### 7.1 API测试

```bash
# 登录
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "admin123"}'

# 获取用户信息
curl http://localhost:8080/api/v1/auth/info \
  -H "Authorization: Bearer {token}"

# 智能问答
curl -X POST http://localhost:8080/api/v1/chat/send \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{"message": "打架斗殴怎么处罚？", "sessionId": "test-session"}'
```

---

## 八、参考文档

- [Sa-Token文档](https://sa-token.dev33.cn/)
- [Vue 3文档](https://vuejs.org/)
- [Element Plus文档](https://element-plus.org/)
- [Spring AI文档](https://docs.spring.io/spring-ai/reference/)
- [SeekDB文档](https://www.oceanbase.com/docs/seekdb)

---

**文档版本**: 2.0.0  
**最后更新**: 2025年1月20日
