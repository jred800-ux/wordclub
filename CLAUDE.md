# CLAUDE.md

## 每次任务完成后

**必须执行以下操作：**

1. **更新 `README.md`** — 反映本次任务涉及的所有改动，包括但不限于：
   - 新增/删除的文件和组件
   - 新增/修改的 API 端点
   - 新增/修改的路由
   - 页面数量变化（`views/` 目录下的 .vue 文件数）
   - 项目状态条目更新
   - 架构或技术栈变化

2. **推送到 GitHub** — 使用 MCP GitHub 工具将所有改动（包括 README.md）commit 并 push 到 `origin/main`

## 项目技术栈

- **后端**: Spring Boot 3.5.14 / Java 17 / JPA + Hibernate / MySQL 8.0 / Redis
- **前端**: Vue 3 (Composition API) / Vite / Pinia / Vue Router / Axios
- **认证**: Sa-Token JWT (Bearer token, Authorization header)
- **CSS**: Scoped styles + CSS 自定义属性（`--color-*`, `--radius-*`, `--shadow-*`）
- **图标**: Material Icons (本地 npm 包，不依赖 Google CDN)

## 代码规范

- Vue 组件使用 `<script setup>` + Composition API
- Pinia store 使用 Composition API 风格 (`defineStore('name', () => { ... })`)
- 后端使用 Lombok (`@RequiredArgsConstructor` 注入)
- API 响应统一使用 `Result<T>` 包装 (`{ code, message, data }`)
- 后端 `ddl-auto: none` — 不自动改表，手动管理 DDL

## 关键文件

| 文件 | 说明 |
|------|------|
| `frontend/src/App.vue` | 主布局（header + 内容区，无侧边栏） |
| `frontend/src/router/index.js` | 路由定义 + 登录守卫 |
| `frontend/src/stores/word.js` | Pinia 状态管理（词书/单词/学习进度/设置） |
| `frontend/src/stores/auth.js` | 认证状态管理 |
| `frontend/src/api/index.js` | Axios 封装 + Token 拦截器 |
| `src/main/resources/application.yaml` | 后端配置 |
| `data/` | 语料库 SQL + 建表脚本 |

## 页面清单

当前共 **10** 个页面：
- `Login.vue`, `Register.vue` (访客)
- `Home.vue` (控制台)
- `BookLibrary.vue` (词库)
- `BookWords.vue` (词书单词列表)
- `Settings.vue` (学习设置)
- `FirstSight.vue` (认读模式)
- `SpellingMode.vue` (拼写模式)
- `WordDetail.vue` (单词详情)
- `StudySummary.vue` (学习统计)

## GitHub

- 仓库: `jred800-ux/wordclub`
- 分支: `main`
- 推送方式: MCP GitHub 工具 (`push_files` / `create_or_update_file`)
