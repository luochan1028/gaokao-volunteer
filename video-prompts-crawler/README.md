# video-prompts-crawler

AI 短剧制作分步提示词手册 —— 内置"点子 → 剧本 → 视觉设定 → 分镜 → 关键帧 → 视频生成 → 配音 → 剪辑"七步全流程提示词模板，每 2 小时自动抓取社区最新短剧提示词并更新报告。

## 报告内容

1. **七步制作流程提示词模板**（内置，可直接照抄）：
   - 第 1 步 选题与剧本创作（含小说改编）
   - 第 2 步 视觉设定与角色一致性（连续性锁）
   - 第 3 步 分镜脚本（对白估时）
   - 第 4 步 关键帧图片生成（首帧 / 多参考图）
   - 第 5 步 视频生成提示词（Seedance 2.0 结构公式 + @ 引用语法 + 分时段模板）
   - 第 6 步 配音与音效（TTS 情绪标注）
   - 第 7 步 剪辑成片（剪辑单）
2. **社区最新短剧提示词**（每 2 小时自动抓取）：短剧与网剧 / 电影风格 / 动漫与动画三个章节的完整提示词全文 + 来源链接
3. **一键全流程工具箱**：drama-skills 11 技能套件的安装方法与技能清单

## 数据源

| 数据源 | 内容 |
| --- | --- |
| zenstory-ai/drama-skills | AI 短剧全流程 11 技能套件（2k+ stars，中文） |
| dexhunter/seedance2-skill | Seedance 2.0 官方提示词撰写指南（中文版） |
| ZeroLu/awesome-seedance | 高保真提示词合集（中文版，每 2 小时同步更新） |

抓取走 GitHub Contents API，附带 403 退避重试与本地缓存降级，每 2 小时仅约 3 次请求。

## 环境要求

- Node.js >= 18（零第三方依赖，无需 `npm install`）

## 安装方式

```powershell
cd D:\software\huaw-demo\video-prompts-crawler
```

1. 手动生成一次报告：

```powershell
npm start
```

2. 注册 Windows 计划任务（每 2 小时自动刷新，无需常驻终端）：

```powershell
npm run task:install
```

3. 管理命令：

```powershell
npm run task:status   # 查看任务状态
npm run task:remove   # 删除定时任务
```

## 报告位置

- 最新报告：`reports\latest.md`（每次刷新覆盖）
- 历史归档：`reports\archive\`
- 运行日志：`logs\run.log`（手动）、`logs\task.log`（计划任务）

## 目录结构

```
video-prompts-crawler/
├── package.json
├── run-task.cmd          # 计划任务入口（安装时自动生成）
├── src/
│   ├── index.js          # 主入口：抓取调度 / 报告落盘 / 任务管理
│   ├── http.js           # fetch 封装
│   ├── manual.js         # 七步提示词手册（内置主体内容）
│   ├── reporter.js       # 手册 + 动态内容融合渲染
│   └── sources/
│       └── drama.js      # drama-skills / awesome-seedance 抓取解析
├── reports/              # latest.md + archive/
└── logs/
```

## 说明

- 仅抓取公开仓库内容用于个人学习参考，抓取频率远低于 GitHub API 限流阈值。
- GitHub API 限速（403）时自动等待 20 秒重试，仍失败则回退最近缓存并在报告中标注。
