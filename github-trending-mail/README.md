# GitHub 热门项目自动扫描邮件推送系统

定时抓取 GitHub 全网热度 TOP10 开源项目，自动解析核心信息并经 SMTP 邮件推送至指定邮箱的后台自动化系统。纯 Node.js 实现，**零第三方依赖**，轻量易部署。

## 功能特性

- 双模式扫描：
  - **全站模式**（默认）：抓取 GitHub Trending 全网热度榜单，推送 TOP5/TOP10/TOP20
  - **领域订阅模式**：配置 `categories` 后，按关注领域（如财经、视频）分别搜索 TOP N，合并为一封分板块邮件
- 采集项目基础信息、热度数据（Star/Fork/Watch/近期增量）、核心功能简介、更新状态、开源协议
- 数据清洗：过滤乱码、冗余广告、空白内容，规整为标准结构化内容
- 邮件推送：结构化 HTML 排版，兼容手机/电脑主流邮箱客户端，推送失败自动重试 2-3 次
- 任务日志：记录执行时间、状态、项目数量、推送结果、异常信息，支持历史查询
- 基础配置：扫描周期（每日/每两日/每周）、接收邮箱、推送数量、日志开关均可自定义，无需改代码
- 容错设计：网络异常终止本次任务、数据为空发送提示邮件、任务去重防重复推送、跨领域项目去重

## 环境要求

- Node.js >= 18（内置 `fetch`）
- Windows / Linux / macOS 均可运行

## 快速开始

```bash
# 1. 生成配置文件模板
npm run init
# 或: node src/index.js --init

# 2. 编辑 config.json，填写 SMTP 与收件人信息（见下节）

# 3. 手动执行一次扫描推送（验证全链路）
npm run scan

# 4. 后台常驻运行，按配置周期自动扫描
npm run start

# 5. 查看历史任务日志
npm run logs
```

## 配置说明 (config.json)

```jsonc
{
  "schedule": {
    "enabled": true,       // 是否启用定时任务
    "time": "09:00",       // 每天执行时间 (HH:mm)
    "frequency": "daily"   // daily=每天, every2days=每两天, weekly=每周
  },
  "topN": 10,              // 全站模式推送数量: 5 / 10 / 20（配置 categories 后忽略）
  "categories": [          // 领域订阅模式（可选）：配置后替代全站模式，分板块推送
    {
      "key": "finance",    // 领域标识（可省略，默认取 label）
      "label": "财经",      // 板块显示名称
      "topics": ["finance", "trading", "stock"],  // GitHub topic 标签（最多5个，各 topic 独立搜索后合并按 Star 排序）
      "topN": 5,           // 该领域推送数量 (1~20)
      "minStars": 500,     // Star 数下限，过滤小项目
      "pushedDays": 90     // 仅看近 N 天内有更新的项目，过滤停更项目
    }
  ],
  "mail": {
    "from": "sender@example.com",
    "to": ["receiver1@example.com", "receiver2@example.com"],
    "subjectPrefix": "【GitHub每日热门项目推送】"
  },
  "smtp": {
    "host": "smtp.qq.com",        // SMTP 服务器
    "port": 465,                  // 465(SSL) 或 587(STARTTLS)
    "secure": true,               // true=SSL直连, false=STARTTLS
    "user": "sender@example.com", // 发件邮箱，一般为 SMTP 账号
    "pass": "your_smtp_auth_code" // SMTP 授权码（非邮箱登录密码）
  },
  "log": {
    "enabled": true,      // 是否记录任务日志
    "keepDays": 30        // 历史日志保留天数
  },
  "github": {
    "timeoutMs": 30000    // 接口请求超时(ms)
  }
}
```

### 常用邮箱 SMTP 配置

| 邮箱 | 服务器 | 端口 | 说明 |
| ---- | ------ | ---- | ---- |
| QQ 邮箱 | smtp.qq.com | 465/587 | 需开启 SMTP 并获取授权码 |
| 网易 163 | smtp.163.com | 465/587 | 需开启 SMTP 并获取授权码 |
| 126 邮箱 | smtp.126.com | 465/587 | 需开启 SMTP 并获取授权码 |
| Gmail | smtp.gmail.com | 465/587 | 需开启 Less secure apps / App Password |
| 企业邮箱 | 按邮件服务商文档 | 465/587 | 使用对应 SMTP 服务器 |

## 数据来源

- **全站模式**：优先抓取 **GitHub 官方 Trending 趋势榜单**（`github.com/trending`，公开页面、无需登录）；当主站不可达或解析失败时，自动降级使用 **GitHub 公开 Search API**（近 7 日新建项目按 Star 排序）
- **领域订阅模式**：使用 **GitHub 公开 Search API**，按配置的 topic 标签逐个搜索（GitHub 不支持 topic 之间用 OR），合并去重后各领域内按 Star 总数排序取 TOP N，并叠加 Star 下限与近 N 天活跃过滤；直接使用搜索结果自带的协议/更新时间/标签字段，单次运行接口调用数少，远低于匿名配额限制

均为 GitHub 官方公开数据，无需授权、无爬取违规。

## 运行模式

| 命令 | 作用 |
| ---- | ---- |
| `npm run scan` | 手动触发一次扫描并推送（带任务锁，防重复并发） |
| `npm run start` | 后台常驻，按 `schedule` 配置周期自动执行 |
| `npm run logs` | 查看最近 30 条任务执行日志 |
| `node src/index.js --logs --limit 50` | 查看最近 50 条日志 |
| `npm run init` | 重新生成配置文件模板（不影响已有文件） |
| `node src/index.js --help` | 查看全部命令帮助 |

## 目录结构

```
github-trending-mail/
├── src/
│   ├── index.js       # 入口：CLI 与定时调度
│   ├── config.js      # 配置加载/校验
│   ├── pipeline.js    # 核心业务流程编排
│   ├── github.js      # GitHub 数据采集（Trending + Search API 兜底）
│   ├── clean.js       # 数据清洗与规整
│   ├── html.js        # 邮件 HTML 模板
│   ├── mailer.js      # 零依赖 SMTP 客户端（SSL/STARTTLS + 重试）
│   ├── scheduler.js   # 周期调度与任务锁
│   └── logger.js      # 日志记录与历史查询
├── config.example.json
├── config.json        # 运行时生成（填入个人配置）
├── logs/              # 运行日志（app.log / tasks.jsonl）
└── package.json
```

## 异常场景处理

- **网络异常**：本次任务终止并记录异常日志，等待下个周期自动重试，不重复无效请求
- **数据为空**：推送「本次无获取到有效热门项目数据」提示邮件，避免空白推送
- **邮箱配置异常**：记录报错日志提示配置异常，便于排查
- **重复任务**：基于任务锁做去重，同一时刻仅执行一次，杜绝重复推送