# WordClub 用户登录认证功能 — 实现提示词

> 复制本文档内容，粘贴给任意 LLM（Claude、ChatGPT、Gemini 等），即可开始实现。

---

## 任务：为 WordClub 项目实现用户登录认证功能

### 项目背景

WordClub 是一个智能词汇学习应用，三端架构：
- **后端**：Spring Boot 3.5.14 + Java 17 + MySQL + Redis（端口 8080）
- **Web 前端**：Vue 3 + Vite 8 + Pinia + Vue Router + Axios（端口 5173，代理 /api → 8080）
- **Android**：Kotlin + Jetpack Compose + Material 3 + Navigation Compose

项目当前状态：后端是空骨架（只有 WordclubApplication.java），没有任何 Controller/Service/Entity。前端和 Android 页面已完整但使用硬编码数据，无登录功能。

设计系统：Lumina Learning Design System，主色 #3B82F6，圆角 8-12px，字体 Inter。


### 技术选型

- **认证框架**：Sa-Token 1.44.0（不用 Spring Security）
- **Token 格式**：JWT（sa-token-jwt 插件）
- **Token 存储**：Redis（sa-token-redis-jackson 插件）
- **数据库**：MySQL + Spring Data JPA
- **密码加密**：BCrypt


### 具体要求

#### 一、后端（Spring Boot）

**1. 依赖添加（pom.xml）**
- sa-token-spring-boot3-starter 1.44.0
- sa-token-jwt 1.44.0
- sa-token-redis-jackson 1.44.0
- spring-boot-starter-data-jpa
- 不要引入 spring-boot-starter-security

**2. application.yaml 配置**
- 配置 MySQL 数据源（库名 wordclub，localhost:3306）
- 配置 Redis（localhost:6379）
- JPA ddl-auto: update
- Sa-Token 配置：
  - token-name: Authorization
  - token-prefix: Bearer
  - timeout: 7200（2小时）
  - activity-timeout: 259200（3天）
  - is-concurrent: true（允许多端登录）
  - max-login-count: 3（最多同时3个设备）
  - jwt-secret-key: 自行生成一个密钥

**3. 数据库 Entity**
- User 表字段：id (BIGINT PK), username (VARCHAR 50 UNIQUE), email (VARCHAR 100 UNIQUE), password (VARCHAR 255 BCrypt), nickname (VARCHAR 50), avatarUrl (VARCHAR 255), enabled (TINYINT default 1), createdAt, updatedAt

**4. 需要创建的类**
- `config/SaTokenConfig.java`：实现 SaTokenConfigure，路由拦截（/api/auth/** 放行，其余 /api/** 需登录）
- `entity/User.java`：JPA Entity
- `repository/UserRepository.java`：JPA Repository（findByUsername 方法）
- `dto/LoginRequest.java`：{ username, password }，带 @NotBlank 校验
- `dto/RegisterRequest.java`：{ username, email, password }，带校验
- `dto/AuthResponse.java`：{ token, user }
- `service/UserService.java`：注册（查重 + BCrypt 加密 + 保存）、按用户名查找、按 ID 查找
- `controller/AuthController.java`：五个接口
  - POST /api/auth/login → 查用户 → BCrypt 验密 → StpUtil.login(userId) → 返回 token+用户信息
  - POST /api/auth/register → 校验 → BCrypt 加密 → 保存 → 返回成功
  - POST /api/auth/logout → @SaCheckLogin → StpUtil.logout()
  - GET /api/auth/me → @SaCheckLogin → StpUtil.getLoginId() → 返回用户信息
  - POST /api/auth/refresh → 刷新 token（可选）
- `exception/GlobalExceptionHandler.java`：全局异常处理，统一返回格式
- 统一响应类 `Result<T>`：{ code, message, data }

**5. 密码规则**
- 注册时：密码最少 6 位
- BCrypt 加密存储


#### 二、Web 前端（Vue 3）

**1. 新增页面**
- `views/Login.vue`：登录页
  - 居中卡片布局，品牌 Logo + "欢迎回来" 标题
  - 用户名输入框 + 密码输入框（带显示/隐藏切换）
  - 蓝色主按钮"登录"（#3B82F6）
  - 底部"还没有账户？立即注册 →"链接
  - 登录成功跳首页，失败提示错误信息
  - 页面样式需符合 Lumina Learning 设计规范（复用 style.css 的 CSS 变量）
- `views/Register.vue`：注册页，风格和 Login.vue 一致

**2. 新增 Store**
- `stores/auth.js`（Pinia）：
  - state：user, token
  - actions：login(), register(), logout(), fetchUser()
  - token 存 localStorage，页面刷新时自动恢复登录态
  - 初始化时从 localStorage 读 token，调 /api/auth/me 恢复 user

**3. 修改路由**
- 添加 /login 和 /register 路由
- router.beforeEach 守卫：需要认证的页面没有 token → 跳 /login；已登录访问 /login → 跳 /

**4. 修改 Axios 拦截器**
- 请求拦截器：自动附加 `Authorization: Bearer <token>`
- 响应拦截器：401 时清 token 跳登录页

**5. 修改现有文件**
- `App.vue`：Header 右上角根据登录状态显示"登录"按钮或用户头像+下拉菜单（个人信息、退出登录）
- `AppSidebar.vue`：底部"退出登录"改为调用 authStore.logout()


#### 三、Android 端（Compose）

**1. 新增依赖（app/build.gradle.kts）**
- Retrofit 2.11.0 + converter-gson
- OkHttp logging-interceptor 4.12.0
- DataStore Preferences 1.1.1
- lifecycle-viewmodel-compose 2.8.7

**2. 需要创建的文件**
- `data/api/AuthApi.kt`：Retrofit 接口（login, register, logout, me）
- `data/api/RetrofitClient.kt`：Retrofit 单例，baseUrl 指向后端地址，OkHttp 拦截器自动带 Token
- `data/model/AuthModels.kt`：LoginRequest, RegisterRequest, AuthResponse 数据类
- `data/repository/AuthRepository.kt`：封装 API 调用
- `util/TokenManager.kt`：DataStore 存取 token
- `ui/screens/LoginScreen.kt`：登录页 UI，对标 Web 端设计，使用 Lumina Learning 配色
- `ui/screens/RegisterScreen.kt`：注册页 UI
- `viewmodel/AuthViewModel.kt`：管理认证状态（login/register/logout 方法 + 状态流）

**3. 修改 NavGraph.kt**
- 起始路由：有 token → home，无 token → login
- 底部导航栏：登录/注册页不显示
- 添加 logout 逻辑：清 token → navigate("login")

**4. 修改 MainActivity.kt**
- 添加 ViewModel 初始化（如需要）


#### 四、设计规范（三端统一）

- 主色 #3B82F6，成功 #10B981，警告 #F59E0B，错误 #EF4444
- 背景 #F9FAFB，卡片表面 #FFFFFF
- 圆角 8-12px
- 输入框高度 44-48px，带图标前缀
- 按钮高度 44-48px，圆角 8px
- 卡片投影 0 1px 3px rgba(0,0,0,0.1)


#### 五、约束
- 代码风格简洁，匹配现有项目风格
- 不要引入不必要的抽象层
- 异常情况要有友好提示（用户名不存在、密码错误、网络错误等）
- 登录/注册页面在 Web 和 Android 两端的 UI 要保持一致
- 后端所有接口返回统一的 Result<T> 格式
