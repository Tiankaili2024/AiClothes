# AI智能穿搭生成系统

基于 SpringBoot + Vue3 + MySQL 的免费AI大模型对话式智能穿搭生成平台。

## 技术栈

- **前端**：Vue3 + Vite + Element Plus + ECharts
- **后端**：SpringBoot 2.7 + MyBatis-Plus + Redis
- **数据库**：MySQL 8.0
- **AI大模型**：Coze扣子免费API / 通义千问免费额度
- **AI绘图**：国内免费/低费用文生图接口

## 功能模块

1. **用户账号管理** - 注册、登录、密码修改、头像设置
2. **个人穿搭档案** - 性别、身材、肤色、风格偏好、色系偏好、城市
3. **天气智能适配** - 自动获取实时天气，联动穿搭推荐
4. **AI对话穿搭** - 自然语言输入，大模型智能解析，生成穿搭方案和绘图Prompt
5. **AI图片生成** - 根据Prompt生成真人高清穿搭效果图
6. **穿搭记录收藏** - 历史记录、收藏管理、图片下载
7. **快捷场景模板** - 通勤/约会/出游/运动/居家/礼服一键模板
8. **管理员后台** - 用户管理、数据可视化大屏、系统配置

## 快速启动

### 1. 数据库
\\\sql
source sql/init.sql
\\\

### 2. 后端
\\\ash
cd backend
mvn clean package -DskipTests
java -jar target/ai-outfit-system-1.0.0.jar
\\\

### 3. 前端
\\\ash
cd frontend
npm install
npm run dev
\\\

访问 http://localhost:3000

### 默认管理员账号
- 用户名：admin
- 密码：admin123

## 项目结构

\\\
ai-outfit-system/
├── backend/          # SpringBoot 后端
│   ├── src/main/java/com/outfit/
│   │   ├── config/       # 配置类（CORS、Redis、MyBatis-Plus、JWT拦截）
│   │   ├── controller/   # 控制器
│   │   ├── dto/          # 数据传输对象
│   │   ├── entity/       # 实体类
│   │   ├── mapper/       # MyBatis Mapper
│   │   ├── service/      # 服务层
│   │   └── common/       # 通用工具
│   └── src/main/resources/
│       └── application.yml
├── frontend/         # Vue3 前端
│   └── src/
│       ├── views/        # 页面组件
│       ├── api/          # API封装
│       ├── router/       # 路由
│       ├── store/        # 状态管理
│       └── components/   # 公共组件
└── sql/              # 数据库脚本
    └── init.sql
\\\"

## 核心API接口

| 接口 | 方法 | 说明 |
|------|------|------|
| /api/auth/login | POST | 用户登录 |
| /api/auth/register | POST | 用户注册 |
| /api/user/info | GET | 获取用户信息 |
| /api/user/profile | PUT | 更新个人档案 |
| /api/outfit/generate | POST | AI生成穿搭 |
| /api/outfit/records | GET | 穿搭记录列表 |
| /api/favorite/add/{id} | POST | 收藏穿搭 |
| /api/admin/dashboard | GET | 管理后台数据统计 |

## 关键特色

- ✅ 全程免费AI - 使用国内免费大模型接口，无需翻墙
- ✅ 五维个性化 - 性别+身材+肤色+天气+场景精准适配
- ✅ 功能完整闭环 - 生成→记录→收藏→下载→管理→可视化
- ✅ 限流保护 - 防止免费额度耗尽
