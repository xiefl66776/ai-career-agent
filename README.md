# AI Career Agent

基于 DeepSeek V4 的 AI 简历岗位匹配与求职 Agent 系统。

## 项目简介

AI Career Agent 是一款面向求职者的智能分析工具，能够根据用户简历和目标岗位 JD，借助 DeepSeek V4 大模型进行深度匹配分析，输出匹配度评分、技能差距、项目建议、学习路线和简历优化建议。同时提供 AI 求职 Agent 问答功能，基于历史分析上下文给出个性化求职指导。

## 技术栈

| 类别 | 技术 |
|------|------|
| 语言 | Java 21 |
| 框架 | Spring Boot 3.x |
| Web 层 | Spring Web (MVC) |
| 持久层 | Spring Data JPA |
| 数据库 | MySQL |
| HTTP 客户端 | WebClient (Spring WebFlux) |
| AI 模型 | DeepSeek V4 |
| 前端 | HTML / CSS / JavaScript (原生) |
| 构建工具 | Maven |
| 工具库 | Lombok, Jackson, Jakarta Validation |

## 核心功能

1. **简历岗位匹配分析**：输入简历和 JD，AI 输出匹配度、技能对比、项目建议、学习计划、简历优化建议
2. **历史记录持久化**：每次分析结果自动保存到 MySQL
3. **历史记录查询**：按时间倒序查看所有分析记录
4. **AI 求职 Agent 问答**：基于最近一次分析上下文，提供个性化求职指导
5. **静态前端页面**：浏览器直接访问，无需前端构建

## 项目结构

```
src/main/java/com/xfl/aicareeragent/
├── AiCareerAgentApplication.java   # 启动类
├── common/
│   └── Result.java                 # 统一响应体
├── config/
│   ├── DeepSeekConfig.java         # DeepSeek WebClient Bean
│   └── DeepSeekProperties.java     # DeepSeek 配置属性
├── controller/
│   ├── AnalysisController.java     # 简历匹配接口
│   └── AgentController.java        # Agent 问答接口
├── dto/
│   ├── AgentChatRequest.java       # Agent 请求体
│   ├── MatchRequest.java           # 匹配请求体
│   └── MatchResult.java            # 匹配结果
├── entity/
│   └── AnalysisRecordEntity.java   # 分析记录实体
├── exception/
│   ├── BusinessException.java      # 业务异常
│   └── GlobalExceptionHandler.java # 全局异常处理
├── repository/
│   └── AnalysisRecordRepository.java
├── service/
│   ├── AnalysisService.java        # 匹配分析服务
│   ├── AgentService.java           # Agent 问答服务
│   └── DeepSeekService.java        # DeepSeek API 调用
src/main/resources/
├── application.yml                 # 应用配置
└── static/
    └── index.html                  # 前端页面
```

## 系统流程

```
用户输入简历 + JD
      │
      ▼
POST /api/analysis/match ──► DeepSeek V4 分析
      │                         │
      ▼                         ▼
保存到 MySQL ◄────────── 解析 MatchResult
      │
      ▼
返回匹配结果到前端
      │
      ▼
POST /api/agent/chat ──► 读取最近分析记录
      │                     │
      ▼                     ▼
返回个性化回答 ◄────── 组装上下文 + 调用 DeepSeek
```

## 数据库设计

### analysis_record 表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT (PK, AUTO_INCREMENT) | 主键 |
| resume_text | LONGTEXT | 简历原文 |
| job_description | LONGTEXT | 岗位 JD 原文 |
| match_score | INT | 匹配度评分 (0-100) |
| result_json | LONGTEXT | 完整 AI 返回 JSON |
| created_at | DATETIME | 创建时间 |

表名通过 JPA `ddl-auto: update` 自动创建，无需手动建表。

## 接口文档

### 1. 简历岗位匹配分析

```
POST /api/analysis/match
Content-Type: application/json
```

**请求体：**

```json
{
  "resumeText": "张三，2026届应届生，中山大学计算机科学...",
  "jobDescription": "Java 后端实习生岗位。要求：熟悉 Java 和 Spring Boot..."
}
```

**响应：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "matchScore": 60,
    "matchedSkills": ["Java", "Spring Boot", "MySQL"],
    "missingSkills": ["Redis", "Docker"],
    "projectSuggestions": ["在项目中集成 Redis 缓存"],
    "learningPlan": ["第一天：学习 Redis 数据结构..."],
    "resumeSuggestions": ["补充技术难点描述"],
    "summary": "候选人具备 Java 基础，但缺少中间件经验..."
  }
}
```

### 2. 查询历史分析记录

```
GET /api/analysis/history
```

**响应：**

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "resumeText": "张三，2026届应届生...",
      "jobDescription": "Java 后端实习生岗位...",
      "matchScore": 60,
      "resultJson": "{\"matchScore\":60,...}",
      "createdAt": "2026-06-19T18:00:00"
    }
  ]
}
```

### 3. AI 求职 Agent 问答

```
POST /api/agent/chat
Content-Type: application/json
```

**请求体：**

```json
{
  "question": "我接下来一个月应该重点学习什么？"
}
```

**响应：**

```json
{
  "code": 200,
  "message": "success",
  "data": "根据你的简历和岗位分析，接下来一个月建议..."
}
```

> 注意：需先完成一次匹配分析，Agent 才能基于历史上下文回答。

### 错误响应格式

```json
{
  "code": 400,
  "message": "resumeText: 简历内容不能为空"
}
```

## 本地启动步骤

### 前置要求

- JDK 21+
- Maven 3.8+
- MySQL 8.0+

### 1. 克隆项目

```bash
git clone <repo-url>
cd ai-career-agent
```

### 2. 创建数据库

```sql
CREATE DATABASE IF NOT EXISTS ai_career_agent DEFAULT CHARACTER SET utf8mb4;
```

### 3. 配置环境变量

```bash
export DEEPSEEK_API_KEY=sk-xxxxxxxxxxxxxxxx
export MYSQL_HOST=localhost
export MYSQL_PORT=3306
export MYSQL_DATABASE=ai_career_agent
export MYSQL_USER=root
export MYSQL_PASSWORD=your_password
```

### 4. 编译启动

```bash
mvn clean compile
mvn spring-boot:run
```

### 5. 访问页面

```
http://localhost:8080/index.html
```

## 环境变量配置

| 变量名 | 默认值 | 说明 |
|--------|--------|------|
| DEEPSEEK_API_KEY | (必填) | DeepSeek API Key |
| MYSQL_HOST | localhost | MySQL 主机地址 |
| MYSQL_PORT | 3306 | MySQL 端口 |
| MYSQL_DATABASE | ai_career_agent | 数据库名 |
| MYSQL_USER | root | 数据库用户名 |
| MYSQL_PASSWORD | root | 数据库密码 |
| JPA_SHOW_SQL | false | 是否打印 SQL |
| LOG_LEVEL | debug | 日志级别 |

## 测试示例

### 使用 cURL

```bash
# 简历匹配分析
curl -X POST http://localhost:8080/api/analysis/match \
  -H "Content-Type: application/json" \
  -d '{
    "resumeText": "张三，2026届应届生，中山大学计算机科学本科。熟悉 Java、Spring Boot、MySQL。做过校园二手交易平台项目。",
    "jobDescription": "Java 后端实习生。要求：熟悉 Java 和 Spring Boot，了解 MySQL 和 Redis，了解微服务和 Docker。"
  }'

# 查询历史记录
curl http://localhost:8080/api/analysis/history

# Agent 问答
curl -X POST http://localhost:8080/api/agent/chat \
  -H "Content-Type: application/json" \
  -d '{"question": "我接下来一个月应该重点学习什么？"}'
```

### 浏览器测试

打开 `http://localhost:8080/index.html`，在页面中操作：
1. 填写简历和 JD → 点击"开始分析" → 查看匹配结果
2. 下拉至 Agent 区域 → 输入问题 → 点击"向 Agent 提问" → 查看回答

## 项目亮点

- **零前端依赖**：原生 HTML/CSS/JS，无需 Node.js 生态
- **DeepSeek V4 接入**：WebClient 异步调用，支持 JSON 模式，自动清洗 ```json 标记
- **API Key 安全**：全部敏感配置通过环境变量注入，代码零硬编码
- **统一响应体**：`Result<T>` + `GlobalExceptionHandler` 保证前端一致的数据结构
- **自动建表**：JPA `ddl-auto: update` 自动同步实体，无需手动 DDL
- **上下文问答**：Agent 自动读取最近分析记录，携带简历+JD+分析结果作为完整上下文

## 后续优化方向

- [ ] 用户登录与多用户隔离
- [ ] 简历文件上传（PDF/Word 解析）
- [ ] 多种 AI 模型支持（DeepSeek / 通义千问 / 文心一言）
- [ ] 流式响应（SSE）提升对话体验
- [ ] 前端 Markdown 渲染 Agent 回答
- [ ] 分析结果导出 PDF
- [ ] Docker 容器化部署
- [ ] 单元测试与集成测试覆盖
