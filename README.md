# 湖南大学人脸识别考勤系统

## 项目简介

这是一个采用湖南大学主题视觉的人脸识别考勤管理系统，支持学生人脸签到、教师管理课程、考勤统计等功能。

## 技术栈

### 后端
- Java 17
- Spring Boot 2.7.18
- MyBatis-Plus 3.5.5
- MySQL 8.0
- JWT

### 前端
- Vue 3.4.0
- Element Plus 2.5.0
- Pinia 2.1.0
- Vue Router 4.2.0
- Axios 1.6.0
- Vite 5.0.0

### 人脸识别
- Python 3.12
- Flask 3.0.0
- OpenCV 5.0.0
- MediaPipe 1.0.1（眨眼活体检测）
- NumPy 1.24.0

## 项目结构

```
attendance-system/
├── backend/              # 后端Spring Boot项目
├── frontend/             # 前端Vue3项目
├── database/             # 数据库脚本
├── face-recognition/     # Python人脸识别服务
└── docs/                 # 项目文档
```

## 快速开始

### 1. 环境准备

#### 必需软件
- JDK 17+
- Maven 3.9+
- Node.js 18+
- Python 3.11+

> 默认使用 `local` profile 和项目内的 H2 文件数据库，无需安装或启动 MySQL。
> 如需 MySQL，可用 `--spring.profiles.active=dev` 启动。

### 2. 数据库配置（可选，仅 MySQL 模式）

```bash
# 创建数据库
mysql -u root -p < database/schema.sql

# 插入初始数据
mysql -u root -p < database/init-data.sql
```

### 3. 后端启动

```bash
cd backend

# 启动项目
mvn spring-boot:run
```

后端服务将运行在 `http://localhost:8080/api`

### 4. 人脸识别服务启动

```bash
cd face-recognition

# 安装依赖
pip install -r requirements.txt

# 启动 OpenCV 版服务（使用随项目提供的模型）
python app_opencv.py
```

人脸识别服务将运行在 `http://localhost:5000`

### 5. 前端启动

```bash
cd frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

前端服务将运行在 `http://localhost:5173`

## 默认账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | admin123 |
| 教师1 | teacher1 | password123 |
| 教师2 | teacher2 | password123 |
| 学生1 | student1 | password123 |
| 学生2 | student2 | password123 |

## 主要功能

### 学生功能
- 学生首次自行注册人脸，录入后自动锁定；重新录入须提交申请并由任课教师批准
- 快速检测签到，或眨眼后左右转头的活体签到
- 查看考勤记录
- 查看学校统一安排的课程
- 查看所在课程小组；被指定为组长后可代本组成员完成考勤

### 教师功能
- 创建、编辑、停用、恢复和删除本人课程，并维护课程学生名单
- 发起限时签到，并可修改签到时间或取消签到
- 按课程分配学生小组、指定组长，授权组长代本组成员考勤
- 审批学生的人脸重新录入申请
- 查看每次课全部学生的考勤记录，并可处理特殊考勤状态
- 审批本人课程的请假申请
- 考勤统计与导出

### 管理员功能
- 学生和教师信息的新增、查看、修改、停用与删除
- 只读查看正常使用中的课程，不承担课程创建、编辑和停用

## 核心算法

### 活体检测
- 双眼睁开 → 闭合 → 睁开连续动作检测
- 活体模式随后完成左右转头，并在每帧核验身份一致性
- 快速模式只做人脸身份核验；小组模式强制使用活体检测

### 人脸识别
- 欧氏距离+余弦相似度融合
- 特征比对阈值0.60

## API接口

详细API文档请参考 `docs/api.md`

## 开发计划

本项目按照4周计划开发：

- 第1周：环境搭建与基础架构
- 第2周：认证与用户管理
- 第3周：课程与考勤管理
- 第4周：人脸识别与系统优化

## 注意事项

1. **本地数据库**：首次启动会在 `backend/data` 中自动创建并写入测试账号。
2. **人脸服务**：本地请使用 `app_opencv.py`；`app.py` 是需要 dlib 的可选版本。
3. **JWT密钥**：生产环境请修改application.yml中的jwt.secret

## 贡献者与单位

详见 [`CONTRIBUTORS.md`](CONTRIBUTORS.md)。

- **单位：** 湖南大学计算机学院软件工程系
- **指导老师：** 周军海
- **项目贡献者：** 艾子韬、陈维悠、邹永恒、郭永瑞、张振恒

本仓库部分代码与文档使用 AI 辅助开发，经人工审阅后合并；说明见贡献者文档。

## 许可证

MIT License

## AI 使用声明

本项目部分代码使用了 AI 工具辅助开发。具体使用情况如下：

- 使用工具：ChatGPT、GitHub Copilot
- 涉及范围：
  - `src/utils/sort.py` 中的排序算法由 AI 生成初稿，人工修改并测试。
  - `docs/` 中的部分文档由 AI 辅助润色。
- 人工审核：所有 AI 生成内容均已由项目维护者审核、修改和测试。
- 责任声明：AI 工具仅作为辅助，最终代码质量和正确性由人类开发者负责。

## 2026-09-05 功能更新

新增限时签到、成员勾选、眨眼活体、Excel 导出、完整请假详情及多维统计。升级步骤与验收说明见 `docs/功能修改与验收说明-20260905.md`。

完整的人工验收步骤、预期结果、Android 专项和缺陷记录模板见 `docs/完整功能测试流程清单-20260909.md`。
