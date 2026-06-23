# WordClub — 智能词汇学习应用

基于 Spring Boot + Vue 3 + Android (Jetpack Compose) 的三端词汇学习平台，提供认读模式、拼写模式、词素分析等学习功能。

## 项目架构

```
wordclub/
├── src/                     # Spring Boot 后端
│   └── main/
│       ├── java/com/jred/wordclub/
│       │   ├── WordclubApplication.java
│       │   ├── common/          # 统一响应 Result<T>
│       │   ├── config/          # Sa-Token 路由拦截配置
│       │   ├── controller/      # AuthController (登录/注册/登出)
│       │   ├── dto/             # LoginRequest, RegisterRequest, AuthResponse
│       │   ├── entity/          # User JPA 实体
│       │   ├── exception/       # GlobalExceptionHandler
│       │   ├── repository/      # UserRepository
│       │   └── service/         # UserService (BCrypt 密码处理)
│       └── resources/
│           └── application.yaml
├── frontend/                # Vue 3 Web 前端
│   ├── src/
│   │   ├── api/             # Axios 封装 + 请求/响应拦截器 (自动带 Token)
│   │   ├── components/      # 公共组件 (AppSidebar)
│   │   ├── router/          # Vue Router + beforeEach 登录守卫
│   │   ├── stores/          # Pinia 状态管理 (auth.js + word.js)
│   │   └── views/           # 页面 (Login, Register, Home, ...)
│   ├── index.html
│   ├── vite.config.js
│   └── package.json
├── android/                 # Android 原生 App
│   ├── app/src/main/java/com/jred/WordClub_App/
│   │   ├── MainActivity.kt
│   │   ├── data/            # API 接口 + 数据模型 + Repository
│   │   ├── util/            # TokenManager (DataStore)
│   │   ├── viewmodel/       # AuthViewModel
│   │   └── ui/
│   │       ├── navigation/  # 导航图 (登录守卫 + 底部栏隐藏)
│   │       ├── screens/     # 页面 (Login/Register + 学习页面)
│   │       └── theme/       # 主题 (Color/Type/Theme)
│   ├── app/build.gradle.kts
│   └── build.gradle.kts
├── docs/                    # 功能需求文档
│   └── login-feature-prompt.md
└── pom.xml                  # Maven 父 POM
```

## 技术栈

| 模块 | 技术 | 版本 |
|------|------|------|
| **后端** | Spring Boot | 3.5.14 |
| | Java | 17 |
| | MySQL | Connector J |
| | Redis | Spring Data Redis |
| | Sa-Token (认证) | 1.44.0 |
| | Sa-Token JWT 插件 | 1.44.0 |
| | Sa-Token Redis 插件 | 1.44.0 |
| | BCrypt | spring-security-crypto |
| | Maven | Wrapper 自带 |
| **Web 前端** | Vue | 3.5 |
| | Vite | 8.x |
| | Pinia | 3.x |
| | Vue Router | 5.x |
| | Axios | 1.x |
| | Material Icons | Google Icons |
| **Android** | Kotlin | 2.0.21 |
| | Compose BOM | 2024.09 |
| | AGP | 9.0.0-alpha06 |
| | Min SDK / Target SDK | 26 / 36 |
| | Navigation Compose | 2.8.0 |
| | Retrofit | 2.11.0 |
| | OkHttp (Logging) | 4.12.0 |
| | DataStore Preferences | 1.1.1 |
| | Lifecycle ViewModel | 2.8.7 |

## 功能模块

### Web 前端 & Android App（页面一一对应）

| 页面 | 路由 (Web) | 路由 (Android) | 认证 | 功能 |
|------|-----------|----------------|------|------|
| 登录 | `/login` | `login` | 游客 | 用户名+密码登录，密码显隐切换，错误提示 |
| 注册 | `/register` | `register` | 游客 | 用户名+邮箱+密码注册，成功后跳转登录 |
| 首页/控制台 | `/` | `home` | 需登录 | 欢迎页、快捷统计、学习模式入口、词库预览 |
| 认读模式 | `/learn/first-sight` | `learn/first-sight` | 需登录 | 单词卡片 + 不认识/模糊/认识三键操作，支持发音、设置面板 |
| 拼写模式 | `/learn/spelling` | `learn/spelling` | 需登录 | 看释义拼写单词，字母提示，正确/错误反馈 |
| 单词详情 | `/word/:id` | `word/{wordId}` | 需登录 | 词素分析（前缀/词根/后缀）、释义、例句 |
| 书库选择 | `/library` | `library` | 需登录 | 课程选择（CET-4/6、考研、IELTS）、学习计划配置 |
| 学习总结 | `/summary` | `summary` | 需登录 | 学习统计、热力图、打卡分享 |

### 后端 API

| 方法 | 路径 | 认证 | 说明 |
|------|------|------|------|
| POST | `/api/auth/register` | 无 | 注册（username, email, password ≥6位） |
| POST | `/api/auth/login` | 无 | 登录 → 返回 JWT token + 用户信息 |
| POST | `/api/auth/logout` | 需登录 | 登出，清除 token |
| GET | `/api/auth/me` | 需登录 | 获取当前登录用户信息 |
| POST | `/api/auth/refresh` | 需登录 | 刷新 token 有效期 |

**认证机制**：Sa-Token + JWT + Redis
- Token 通过 `Authorization: Bearer <token>` 头传递
- 登录态有效期 2 小时，无操作 3 天内自动续期
- 支持多端同时登录（最多 3 设备）
- 路由拦截：`/api/auth/**` 放行，其余 `/api/**` 需登录

**统一响应格式**：
```json
{ "code": 200, "message": "success", "data": { ... } }
```

API 设计遵循 RESTful 风格，Web 前端通过 Vite proxy (`/api` → `localhost:8080`) 访问后端。

## 设计系统

统一采用 **Lumina Learning** 设计规范：

| Token | 色值 | 用途 |
|-------|------|------|
| Primary | `#3B82F6` | 主色调、按钮、链接 |
| Success | `#10B981` | 认识/正确/已掌握 |
| Warning | `#F59E0B` | 模糊/提示 |
| Error | `#EF4444` | 不认识/错误 |
| Background | `#F9FAFB` | 页面背景 |
| Surface | `#FFFFFF` | 卡片、面板背景 |

- 字体：Inter（Web）/ System Default（Android）
- 圆角：8–16px
- 图标：Material Icons（Web 使用 Google Icons 字体，Android 使用 material-icons-extended）

## 快速开始

### 1. 环境准备

```bash
# 启动 MySQL（需先创建数据库 wordclub，Charset: utf8mb4）
# 默认连接：localhost:3306，用户名 root，密码 123123

# 启动 Redis
redis-server
```

### 2. 后端

```bash
# 确保 Java 17+ 和 MySQL/Redis 已启动

# Windows
mvnw.cmd spring-boot:run

# Linux / macOS
./mvnw spring-boot:run
```

后端默认运行在 `http://localhost:8080`，首次启动 JPA 自动建表。

### 3. Web 前端

```bash
cd frontend

# 安装依赖
npm install

# 启动开发服务器 (端口 5173)
npm run dev

# 构建生产版本
npm run build
```

开发时访问 `http://localhost:5173`，API 请求自动代理到后端 8080 端口。

### 4. Android App

> **Android SDK 路径**：`D:\Android\SDK`（已配置在 `android/local.properties`）

```bash
cd android

# 编译调试版 APK
./gradlew assembleDebug

# 安装到模拟器
./gradlew installDebug

# 安装并启动
adb install app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n com.jred.WordClub_App/.MainActivity
```

**推荐**：用 Android Studio 打开 `android/` 目录作为独立项目，获得完整的 IDE 支持（代码补全、模拟器管理、断点调试）。

> IDEA 中 `android/` 是普通子目录，无法直接运行调试。需用 Android Studio 单独打开，或终端执行 Gradle 命令。

## 环境要求

| 工具 | 最低版本 |
|------|---------|
| JDK | 17 |
| Node.js | 18+ |
| Android Studio | Hedgehog+ (2023.1+) |
| Android SDK | API 36 |
| MySQL | 8.0+ |
| Redis | 7.0+ |

## 开发指南

### 后端开发

- 统一响应格式 `Result<T>` 在 `common/Result.java`（code/message/data）
- 认证使用 Sa-Token，路由拦截在 `config/SaTokenConfig.java`
- 全局异常处理在 `exception/GlobalExceptionHandler.java`
- 新 Controller 放在 `controller/`，Service 放 `service/`，Entity 放 `entity/`

### 前端开发

- 路由定义在 `frontend/src/router/index.js`（含 `beforeEach` 登录守卫）
- 全局状态在 `frontend/src/stores/auth.js`（认证）和 `word.js`（业务）
- 设计 Token 在 `frontend/src/style.css`（CSS 变量）
- API 封装在 `frontend/src/api/index.js`（请求拦截器自动带 Token，401 自动跳登录）
- 游客页面（登录/注册）通过 `meta.guest: true` 标记，不渲染 AppShell

### Android 开发

- 页面在 `ui/screens/` 下，每个页面一个 Kotlin 文件
- 导航图在 `ui/navigation/NavGraph.kt`，根据登录态决定起始路由
- 主题色定义在 `ui/theme/Color.kt`
- API 层在 `data/` 下（api/Retrofit 接口 + model/数据类 + repository/仓库）
- Token 管理在 `util/TokenManager.kt`（DataStore 持久化）
- 添加新页面需在 `NavGraph.kt` 的 `NavHost` 中注册 `composable` 路由

### 添加新功能

1. **后端**：在对应子包下创建 Controller/Service/Repository/Entity → 在 `SaTokenConfig.java` 配置路由权限
2. **Web**：在 `frontend/src/views/` 创建 `.vue` 页面 → 在 `router/index.js` 注册路由（设置 `meta.requiresAuth`）
3. **Android**：在 `ui/screens/` 创建 `Screen.kt` → 在 `NavGraph.kt` 注册路由

## 项目状态

- ✅ 后端骨架（Spring Boot + MySQL + Redis）
- ✅ 用户认证系统（注册/登录/登出 + Sa-Token JWT + BCrypt）
- ✅ Web 前端完整 UI（8 页面含登录/注册 + 路由守卫 + 认证状态管理）
- ✅ Android App 完整 UI（8 页面含登录/注册 + 底部导航 + 认证状态管理）
- ✅ 前后端联调（Auth API 全部验证通过）
- ⬜ 单词数据库初始化
- ⬜ 学习记录 API 实现

## 设计稿来源

UI 设计参考 Stitch 项目 **"单词背诵-初见模式"**（项目 ID: `17752904279606003911`），包含 5 个完整页面的设计规范。
