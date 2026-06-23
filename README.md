# WordClub

基于 Spring Boot + Vue 3 + Android (Jetpack Compose) 的三端词汇学习平台，提供认读模式、拼写模式、SM-2 间隔复习、词书选择等学习功能。内置 10 万+ 单词和 14 万+ 例句语料库。

## 项目架构

```
wordclub/
├── src/                     # Spring Boot 后端
│   └── main/
│       ├── java/com/jred/wordclub/
│       │   ├── WordclubApplication.java
│       │   ├── common/          # 统一响应 Result<T>
│       │   ├── config/          # Sa-Token 路由拦截
│       │   ├── controller/      # 5 个 Controller (Auth/Word/Book/Vocabulary/Learning)
│       │   ├── dto/             # Request/Response DTO
│       │   ├── entity/          # 9 个实体 (User/Word/Vocabulary/Book/VocBook/
│       │   │                    #   VocExample/UserWordProgress/UserFavorite/UserSetting)
│       │   ├── exception/       # GlobalExceptionHandler + RateLimitException
│       │   ├── repository/      # 8 个 JPA Repository
│       │   └── service/         # 6 个 Service (含 SM-2 算法)
│       └── resources/
│           └── application.yaml
├── data/                    # 词汇语料库 SQL/JSON (~110MB, gitignore)
│   ├── tb_vocabulary.*      # 10.4 万单词
│   ├── tb_book.*            # 6 本词书
│   ├── tb_voc_book.*        # 3.5 万词书关联
│   ├── tb_voc_examples.*    # 14.2 万例句
│   └── init-user-tables.sql # 学习进度/收藏建表脚本
├── frontend/                # Vue 3 Web 前端
│   └── src/
│       ├── api/             # Axios 封装 + Token 拦截器
│       ├── components/      # SearchModal (搜索弹窗)
│       ├── router/          # Vue Router + 登录守卫
│       ├── stores/          # Pinia (auth.js + word.js)
│       └── views/           # 10 个页面（含设置页 Settings）
├── android/                 # Android 原生 App (Jetpack Compose)
├── docs/                    # 功能需求文档
└── pom.xml                  # Maven
```

## 技术栈

| 模块 | 技术 | 版本 |
|------|------|------|
| **后端** | Spring Boot | 3.5.14 / Java 17 |
| | MySQL + Redis | Connector J / Data Redis |
| | Sa-Token | 1.44.0 (JWT + Redis 插件) |
| | BCrypt + Java Mail | QQ SMTP |
| **Web** | Vue 3 + Vite | 3.5 / 8.x |
| | Pinia + Vue Router + Axios | 3.x / 5.x / 1.x |
| | Material Icons | material-design-icons-iconfont (本地) |
| **Android** | Kotlin + Compose BOM | 2.0.21 / 2024.09 |
| | Navigation + Retrofit + DataStore | — |

## 数据库

MySQL `wordclub` 库，8 张表：

| 表 | 行数 | 说明 |
|---|---|---|
| `tb_vocabulary` | 103,977 | 单词拼写、英/美音标、释义、词频 |
| `tb_book` | 6 | 词书（四级/考研/星火四级/雅思/托福/考研二） |
| `tb_voc_book` | 34,748 | 单词↔词书关联 |
| `tb_voc_examples` | 141,775 | 英文例句 + 中文翻译 |
| `users` | — | 用户表 |
| `user_word_progress` | — | SM-2 学习进度 (需手动建表) |
| `user_favorites` | — | 用户收藏 (需手动建表) |
| `user_settings` | — | 用户学习设置 (需手动建表) |

**导入语料库**（仅首次，SQL 在 `data/` 目录）：
```bash
mysql -u root -p123123 wordclub < data/tb_book.sql
mysql -u root -p123123 wordclub < data/tb_vocabulary.sql
mysql -u root -p123123 wordclub < data/tb_voc_book.sql
mysql -u root -p123123 --default-character-set=utf8mb4 wordclub -e "source data/tb_voc_examples.sql"
# 创建学习进度和收藏表（仅首次）
mysql -u root -p123123 wordclub < data/init-user-tables.sql
```

## 后端 API

### 认证 `/api/auth`
| 方法 | 路径 | 认证 | 说明 |
|------|------|------|------|
| POST | `/login` | 无 | 邮箱+密码登录 → JWT |
| POST | `/register` | 无 | 邮箱验证码注册 |
| POST | `/send-code` | 无 | 发送邮箱验证码 |
| POST | `/logout` | 需 | 登出 |
| GET | `/me` | 需 | 当前用户信息 |

### 词书 `/api/books`
| GET | `/` | 需 | 词书列表 |
| GET | `/{id}` | 需 | 词书详情 |

### 词汇 `/api/vocabulary`
| GET | `/?page=&size=&bookId=` | 需 | 分页获取（可按词书筛选） |
| GET | `/{id}` | 需 | 单词详情 + 例句 |
| GET | `/search?q=` | 需 | 模糊搜索（英文+中文） |

### 学习 `/api/learning`
| POST | `/review` | 需 | SM-2 复习记录 {wordId, quality} |
| GET | `/stats` | 需 | 今日统计 |
| GET | `/queue` | 需 | 待复习队列 |
| GET | `/progress/book/{id}` | 需 | 词书学习进度（恢复位置） |
| GET | `/favorites` | 需 | 收藏列表 |
| POST | `/favorites/{wordId}` | 需 | 添加收藏 |
| DELETE | `/favorites/{wordId}` | 需 | 取消收藏 |
| GET | `/settings` | 需 | 获取用户学习设置 |
| PUT | `/settings` | 需 | 保存用户学习设置 |

**认证**：Sa-Token JWT + Redis。`/api/auth/**` 放行，其余 `/api/**` 需 `Bearer` token。

**SM-2 算法**：quality 0-5 评分 → 间隔 1→3→×EF 天 → 状态 NEW/LEARNING/REVIEW/MASTERED。

**响应格式**：`{ "code": 200, "message": "success", "data": {...} }`

## 设计系统 (Lumina Learning)

| Token | 色值 |
|-------|------|
| Primary | `#3B82F6` |
| Success | `#10B981` |
| Warning | `#F59E0B` |
| Error | `#EF4444` |
| BG / Surface | `#F9FAFB` / `#FFFFFF` |

图标使用 Material Icons 本地 npm 包（不依赖 Google CDN）。

## 快速开始

### 环境准备
```bash
# MySQL: localhost:3306, root/123123, 创建 wordclub 库 (utf8mb4)
# Redis: localhost:6379
```

### 导入语料库（仅首次）
```bash
cd data
for f in tb_book.sql tb_vocabulary.sql tb_voc_book.sql; do
  mysql -u root -p123123 wordclub < $f
done
mysql -u root -p123123 --default-character-set=utf8mb4 wordclub -e "source tb_voc_examples.sql"
mysql -u root -p123123 wordclub < init-user-tables.sql
```

### 后端
```bash
./mvnw spring-boot:run       # → localhost:8080
```
首次启动前需手动创建学习进度和收藏表（或执行 `data/init-user-tables.sql`）。

### 前端
```bash
cd frontend && npm install && npm run dev   # → localhost:5173
```
Vite proxy 将 `/api` 代理到后端 8080。

### Android
```bash
cd android && ./gradlew installDebug
```

## 环境要求

| 工具 | 版本 |
|------|------|
| JDK | 17+ |
| Node.js | 18+ |
| MySQL | 8.0+ |
| Redis | 7.0+ |
| Android Studio | Hedgehog+ |

## 项目状态

- ✅ 后端骨架 + 认证系统 + 安全防护
- ✅ Web 前端 10 页 + Android App 8 页
- ✅ 10 万单词语料库 + JPA 实体映射
- ✅ 词书选择 + 分页/搜索 API（英文+中文）
- ✅ 词书点击查看单词列表（/book/:id）
- ✅ SM-2 间隔复习 + 学习统计 + 收藏
- ✅ 顶部导航栏（侧边栏已移除）
- ✅ 全局搜索弹窗（Ctrl+K 风格，英文+中文双字段）
- ✅ 独立设置页（/settings）— 词书 + 每日目标（新词/复习比例）+ 偏好
- ✅ 单词学习自动播放发音
- ✅ 学习进度持久化 — 重新登录自动恢复位置
- ✅ 用户设置持久化 — 每日目标/偏好/考试目标存库，换设备不丢
- ✅ Material Icons 本地化（国内可用）
- ⬜ Android 端对接新单词 API
- ⬜ 拼写/选择题测验后端
- ⬜ 学习日历热力图持久化

## 参考

- UI 设计参考 Stitch 项目 "单词背诵-初见模式" (ID: `17752904279606003911`)
- 词汇语料库来自 [english-vocabulary](https://github.com/zhenghaoyang24/english-vocabulary)
