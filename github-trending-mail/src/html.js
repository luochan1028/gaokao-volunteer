'use strict';

const { cleanText } = require('./clean');

function esc(s) {
  return String(s == null ? '' : s)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;');
}

function fmtNum(n) {
  n = Number(n) || 0;
  if (n >= 1000000) return (n / 1000000).toFixed(1).replace(/\.0$/, '') + 'M';
  if (n >= 1000) return (n / 1000).toFixed(1).replace(/\.0$/, '') + 'K';
  return String(n);
}

function repoCard(r) {
  const medal = r.rank === 1 ? '#e8b33a' : r.rank === 2 ? '#b4c0cc' : r.rank === 3 ? '#cd7f32' : '#4a6cf7';
  return `
  <div style="background:#f7f9ff;border:1px solid #e4e9f5;border-left:5px solid ${medal};border-radius:10px;padding:16px 18px;margin:0 0 14px;">
    <table width="100%" cellpadding="0" cellspacing="0" border="0" role="presentation">
      <tr>
        <td>
          <div style="display:inline-block;background:#1e2a52;color:#ffffff;font-size:13px;font-weight:700;border-radius:6px;padding:2px 10px;margin-right:8px;">#${r.rank}</div>
          <span style="font-size:17px;font-weight:700;color:#1a2238;"><a href="${esc(r.url)}" style="color:#1a2238;text-decoration:none;">${esc(r.fullName)}</a></span>
          ${r.todayStars > 0 ? `<span style="font-size:12px;color:#e8562a;font-weight:700;background:#fff1ea;border-radius:4px;padding:1px 7px;margin-left:6px;">▲ +${fmtNum(r.todayStars)}/日</span>` : ''}
        </td>
      </tr>
    </table>
    <table width="100%" cellpadding="0" cellspacing="0" border="0" role="presentation" style="margin-top:9px;">
      <tr>
        <td width="78" style="font-size:12px;color:#7a86a6;white-space:nowrap;vertical-align:top;padding:3px 0;">核心功能</td>
        <td style="font-size:13px;color:#3a4358;line-height:1.7;padding:3px 0;">${esc(r.summary) || '暂无描述'}</td>
      </tr>
      <tr>
        <td width="78" style="font-size:12px;color:#7a86a6;white-space:nowrap;vertical-align:top;padding:3px 0;">所属分类</td>
        <td style="font-size:13px;color:#3a4358;padding:3px 0;">${esc(r.category)}${r.language && r.language !== '未知' ? ` ｜ 语言：<span style="background:#eef2ff;color:#4a6cf7;border-radius:4px;padding:0 6px;font-size:12px;">${esc(r.language)}</span>` : ''}</td>
      </tr>
      <tr>
        <td width="78" style="font-size:12px;color:#7a86a6;white-space:nowrap;vertical-align:top;padding:3px 0;">热度数据</td>
        <td style="font-size:13px;color:#3a4358;padding:3px 0;">
          <span style="background:#1e2a52;color:#ffffff;border-radius:4px;padding:1px 8px;font-size:12px;font-weight:700;">★ ${fmtNum(r.stars)}</span>
          <span style="background:#eef2ff;color:#4a6cf7;border-radius:4px;padding:1px 8px;font-size:12px;margin-left:5px;">⑂ ${fmtNum(r.forks)} Forks</span>
          ${r.watchers > 0 ? `<span style="background:#f0f6f0;color:#2e7d32;border-radius:4px;padding:1px 8px;font-size:12px;margin-left:5px;">👁 ${fmtNum(r.watchers)} Watch</span>` : ''}
        </td>
      </tr>
      <tr>
        <td width="78" style="font-size:12px;color:#7a86a6;white-space:nowrap;vertical-align:top;padding:3px 0;">开源地址</td>
        <td style="font-size:12px;color:#4a6cf7;padding:3px 0;word-break:break-all;"><a href="${esc(r.url)}" style="color:#4a6cf7;text-decoration:none;">${esc(r.url)}</a></td>
      </tr>
      <tr>
        <td width="78" style="font-size:12px;color:#7a86a6;white-space:nowrap;vertical-align:top;padding:3px 0;">更新状态</td>
        <td style="font-size:12px;color:#7a86a6;padding:3px 0;">最近更新：${esc(r.updatedAt) || '未知'} ｜ 开源协议：${esc(r.license) || '未知'}</td>
      </tr>
    </table>
  </div>`;
}

function buildTop10Html({ dateStr, topN, source, repos, pushedAt }) {
  const today = cleanText(dateStr);
  const items = repos.map(repoCard).join('\n');

  return `<!DOCTYPE html>
<html lang="zh-CN">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>GitHub 热门项目推送</title>
</head>
<body style="margin:0;padding:0;background:#edf1f8;font-family:Helvetica,Arial,'PingFang SC','Microsoft YaHei',sans-serif;">
<div style="max-width:760px;margin:0 auto;background:#ffffff;border-radius:12px;overflow:hidden;border:1px solid #dfe6f2;">
  <div style="background:linear-gradient(135deg,#1e2a52 0%,#2f3f7f 60%,#4a6cf7 100%);padding:28px 32px;color:#ffffff;">
    <div style="font-size:22px;font-weight:700;letter-spacing:1px;">GitHub 热门项目每日推送</div>
    <div style="font-size:13px;color:#b9c6e8;margin-top:8px;">${esc(today)} ｜ 全网热度 TOP${topN} 开源项目滚动榜单</div>
    <div style="font-size:12px;color:#8fa3d8;margin-top:4px;">数据来源：${esc(source === 'github-trending' ? 'GitHub Trending 官方趋势榜单' : 'GitHub 公开搜索接口(近7日新增)')} ｜ 抓取时间：${esc(pushedAt)}</div>
  </div>
  <div style="padding:24px 28px;">
    <div style="background:#f0f4ff;border:1px solid #d8e2fa;border-radius:8px;padding:12px 16px;font-size:13px;color:#3a4358;line-height:1.7;margin-bottom:20px;">
      本次扫描说明：系统于 <b>${esc(pushedAt)}</b> 自动完成全网热门项目扫描与解析，共获取 <b>${repos.length}</b> 个热点项目，以下按热度从高到低展示。点击项目名称或开源地址可跳转至 GitHub 查看详情。
    </div>
    ${items}
    <div style="font-size:12px;color:#9aa5c0;line-height:1.8;margin-top:22px;padding-top:16px;border-top:1px solid #e8edf6;">
      本邮件由「GitHub 热门项目自动扫描推送系统」自动生成并推送。<br>
      如需调整扫描周期、接收邮箱或推送数量，请修改系统配置文件 config.json 后重启服务。
    </div>
  </div>
  <div style="background:#f4f7fc;color:#7a86a6;font-size:12px;text-align:center;padding:14px 0;">© GitHub Trending Mail ｜ 数据均来源于 GitHub 公开接口</div>
</div>
</body>
</html>`;
}

// 领域订阅模式：每个领域一个板块，各自 TOP N
function buildCategoriesHtml({ dateStr, sections, pushedAt }) {
  const today = cleanText(dateStr);
  const total = sections.reduce((s, x) => s + x.repos.length, 0);
  const parts = sections
    .map((sec) => {
      const cards = sec.repos.map(repoCard).join('\n');
      const emptyTip = sec.note
        ? `<div style="background:#fff7f0;border:1px solid #f5ddc8;border-radius:8px;padding:12px 16px;font-size:13px;color:#b45309;margin-bottom:14px;">${esc(sec.note)}</div>`
        : '';
      return `
    <div style="margin-bottom:26px;">
      <div style="border-left:5px solid #4a6cf7;padding:6px 0 6px 14px;margin-bottom:14px;">
        <span style="font-size:17px;font-weight:700;color:#1e2a52;">${esc(sec.label)}</span>
        <span style="font-size:12px;color:#7a86a6;margin-left:10px;">热度 TOP${sec.repos.length || (sec.topN || 5)} ｜ 按 Star 总数排序</span>
      </div>
      ${emptyTip}${cards}
    </div>`;
    })
    .join('\n');

  const labelSummary = sections.map((s) => `${s.label}TOP${s.repos.length || (s.topN || 5)}`).join(' + ');

  return `<!DOCTYPE html>
<html lang="zh-CN">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>GitHub 领域热门项目推送</title>
</head>
<body style="margin:0;padding:0;background:#edf1f8;font-family:Helvetica,Arial,'PingFang SC','Microsoft YaHei',sans-serif;">
<div style="max-width:760px;margin:0 auto;background:#ffffff;border-radius:12px;overflow:hidden;border:1px solid #dfe6f2;">
  <div style="background:linear-gradient(135deg,#1e2a52 0%,#2f3f7f 60%,#4a6cf7 100%);padding:28px 32px;color:#ffffff;">
    <div style="font-size:22px;font-weight:700;letter-spacing:1px;">GitHub 领域热门项目每日推送</div>
    <div style="font-size:13px;color:#b9c6e8;margin-top:8px;">${esc(today)} ｜ ${esc(labelSummary)} 开源项目</div>
    <div style="font-size:12px;color:#8fa3d8;margin-top:4px;">数据来源：GitHub 公开搜索接口（领域关键词 + 近期活跃） ｜ 抓取时间：${esc(pushedAt)}</div>
  </div>
  <div style="padding:24px 28px;">
    <div style="background:#f0f4ff;border:1px solid #d8e2fa;border-radius:8px;padding:12px 16px;font-size:13px;color:#3a4358;line-height:1.7;margin-bottom:20px;">
      本次扫描说明：系统于 <b>${esc(pushedAt)}</b> 按订阅领域（${esc(sections.map((s) => s.label).join('、'))}）完成扫描与解析，共获取 <b>${total}</b> 个热点项目，各领域内按 Star 总数从高到低展示。点击项目名称或开源地址可跳转至 GitHub 查看详情。
    </div>
    ${parts}
    <div style="font-size:12px;color:#9aa5c0;line-height:1.8;margin-top:22px;padding-top:16px;border-top:1px solid #e8edf6;">
      本邮件由「GitHub 热门项目自动扫描推送系统」自动生成并推送。<br>
      如需调整订阅领域、扫描周期或接收邮箱，请修改系统配置文件 config.json 后重启服务。
    </div>
  </div>
  <div style="background:#f4f7fc;color:#7a86a6;font-size:12px;text-align:center;padding:14px 0;">© GitHub Trending Mail ｜ 数据均来源于 GitHub 公开接口</div>
</div>
</body>
</html>`;
}

function buildEmptyHtml({ dateStr, topN, pushedAt }) {
  return `<!DOCTYPE html>
<html lang="zh-CN">
<head><meta charset="UTF-8"><title>GitHub 热门项目推送</title></head>
<body style="margin:0;padding:0;background:#edf1f8;font-family:Helvetica,Arial,'PingFang SC','Microsoft YaHei',sans-serif;">
<div style="max-width:760px;margin:0 auto;background:#ffffff;border-radius:12px;overflow:hidden;border:1px solid #dfe6f2;">
  <div style="background:linear-gradient(135deg,#1e2a52 0%,#4a6cf7 100%);padding:28px 32px;color:#ffffff;">
    <div style="font-size:22px;font-weight:700;">GitHub 热门项目每日推送</div>
    <div style="font-size:13px;color:#b9c6e8;margin-top:8px;">${esc(dateStr)} ｜ 全网热度 TOP${topN} 开源项目</div>
  </div>
  <div style="padding:40px 32px;text-align:center;">
    <div style="font-size:15px;color:#3a4358;">本次无获取到有效热门项目数据</div>
    <div style="font-size:12px;color:#9aa5c0;margin-top:10px;">数据抓取时间：${esc(pushedAt)}，系统将在下一周期自动重试。</div>
  </div>
</div>
</body>
</html>`;
}

module.exports = { buildTop10Html, buildCategoriesHtml, buildEmptyHtml, fmtNum, esc };