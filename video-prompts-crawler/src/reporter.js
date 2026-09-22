const { STEPS, TOOLKIT, RESOURCES } = require('./manual');

function pad(n) {
  return String(n).padStart(2, '0');
}

function fmtTime(d) {
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`;
}

function codeBlock(text) {
  return '```\n' + text + '\n```';
}

function renderSteps() {
  const out = [];
  for (const step of STEPS) {
    out.push(`## 第 ${step.id} 步：${step.title}`);
    out.push('');
    out.push(`> **做什么**：${step.goal}`);
    out.push('');
    for (const p of step.prompts) {
      out.push(`**📌 ${p.name}**`);
      out.push('');
      out.push(codeBlock(p.text));
      out.push('');
    }
    out.push(`🔗 深入学习：[${step.skillLink.name}](${step.skillLink.url})`);
    out.push('');
  }
  return out.join('\n');
}

function renderCommunity(dramaData, dramaError) {
  const head = ['## 🔄 社区最新短剧提示词（每 2 小时自动更新）', ''];
  if (!dramaData) {
    head.push(`> ⚠️ 本次抓取失败：${dramaError}（GitHub API 限速通常下轮自动恢复，已保留上次缓存内容）`);
    head.push('');
    return head.join('\n');
  }
  const cacheNote = dramaData.fromCache ? `（来自缓存，原因：${dramaData.error}）` : '';
  const lines = [...head];
  for (const group of dramaData.promptGroups) {
    lines.push(`### 【${group.section}】`);
    lines.push('');
    for (const item of group.items.slice(0, 4)) {
      lines.push(`#### ${item.title}`);
      if (item.desc) lines.push(`*${item.desc}*`);
      lines.push('');
      lines.push(codeBlock(item.text));
      lines.push('');
      lines.push(`来源：[原文链接](${item.url})`);
      lines.push('');
    }
  }
  if (cacheNote) lines.push(`> ${cacheNote}`);
  return lines.join('\n');
}

function renderToolkit() {
  const lines = [
    '## 🧰 一键全流程工作流（可选进阶）',
    '',
    TOOLKIT.intro,
    '',
    '**安装步骤**：',
    '',
    codeBlock(TOOLKIT.install.join('\n')),
    '',
    `> ${TOOLKIT.skillsNote}`,
    ''
  ];
  return lines.join('\n');
}

function renderSkillsTable(dramaData) {
  if (!dramaData || !dramaData.skills || dramaData.skills.length === 0) return '';
  const lines = [
    '### drama-skills 技能清单',
    '',
    '| 技能 | 职责 |',
    '| --- | --- |'
  ];
  for (const s of dramaData.skills) {
    lines.push(`| \`${s.name}\` | ${s.duty.replace(/\|/g, '\\|')} |`);
  }
  lines.push('');
  return lines.join('\n');
}

function renderResources(dramaData) {
  const lines = ['## 📚 资源清单', ''];
  for (const r of RESOURCES) {
    lines.push(`- **[${r.name}](${r.url})** — ${r.desc}`);
  }
  if (dramaData && dramaData.updated) {
    lines.push('');
    lines.push(`> awesome-seedance 最后更新：${dramaData.updated.awesomeSeedance || '未知'}`);
  }
  lines.push('');
  return lines.join('\n');
}

function renderReport(data, startedAt) {
  const { dramaData, dramaError } = data;
  const flow = '点子 / 原著 → ① 剧本 → ② 视觉设定（角色一致性）→ ③ 分镜 → ④ 关键帧图片 → ⑤ 视频生成 → ⑥ 配音音效 → ⑦ 剪辑成片';
  const head = [
    '# 🎬 AI 短剧制作分步提示词手册',
    '',
    `> **刷新时间**：${fmtTime(startedAt)}  ｜  **自动更新**：每 2 小时拉取社区最新提示词`,
    '',
    `**制作流程总览**：${flow}`,
    '',
    '> 使用方法：按步骤顺序推进，每步的提示词模板可直接复制到对应工具（Claude/GPT 写剧本、即梦/可灵生成图片视频）。第 5 步是核心。',
    ''
  ].join('\n');

  return [
    head,
    renderSteps(),
    renderCommunity(dramaData, dramaError),
    renderToolkit(),
    renderSkillsTable(dramaData),
    renderResources(dramaData),
    '---',
    '',
    `*本手册由 video-prompts-crawler 于 ${fmtTime(new Date())} 生成；提示词模板基于 Seedance 2.0 官方写法与 drama-skills 工作流整理，社区提示词每 2 小时自动同步。*`,
    ''
  ].join('\n');
}

module.exports = { renderReport, fmtTime };
