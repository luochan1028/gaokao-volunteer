'use strict';

const { collect, collectCategories } = require('./github');
const { cleanRepos } = require('./clean');
const { buildTop10Html, buildCategoriesHtml, buildEmptyHtml } = require('./html');
const { sendMail } = require('./mailer');
const { app, logTask, ts } = require('./logger');

function fmtDate() {
  const d = new Date();
  const p = (n) => String(n).padStart(2, '0');
  return `${d.getFullYear()}年${p(d.getMonth() + 1)}月${p(d.getDate())}日`;
}

function fmtDateTime() {
  const d = new Date();
  const p = (n) => String(n).padStart(2, '0');
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}:${p(d.getSeconds())}`;
}

async function runOnce(cfg, opts = {}) {
  const logEnabled = cfg.log && cfg.log.enabled !== false;
  const startTime = fmtDateTime();
  const record = {
    time: startTime,
    status: 'running',
    source: '',
    projectCount: 0,
    mailResult: '',
    error: ''
  };
  const stamp = (rec) => ({ ...rec, time: startTime });

  const pushMail = async (subject, html) => {
    app('INFO', `开始推送邮件，主题: ${subject}`);
    const mailRes = await sendMail(cfg, subject, html);
    if (mailRes.ok) {
      record.mailResult = `推送成功(尝试${mailRes.attempts}次)`;
      record.status = 'done';
      app('INFO', `邮件推送成功，收件人: ${cfg.mail.to.join(', ')}，尝试 ${mailRes.attempts} 次`);
    } else {
      record.mailResult = `推送失败(已重试${mailRes.attempts}次): ${mailRes.error}`;
      record.status = 'failed';
      app('ERROR', `邮件推送失败，已重试 ${mailRes.attempts} 次: ${mailRes.error}`);
    }
  };

  try {
    const useCategories = Array.isArray(cfg.categories) && cfg.categories.length > 0;

    if (useCategories) {
      // 领域订阅模式：每个领域独立搜索 TOP N，合并为一封分板块邮件
      app('INFO', `开始扫描订阅领域热门项目（${cfg.categories.map((c) => c.label).join('、')}），目标邮箱: ${cfg.mail.to.join(', ')}`);
      const { source, sections } = await collectCategories(cfg.categories, cfg.github && cfg.github.timeoutMs);
      record.source = source;
      const cleanedSections = sections.map((sec) => ({ ...sec, repos: cleanRepos(sec.repos) }));
      const total = cleanedSections.reduce((s, x) => s + x.repos.length, 0);
      record.projectCount = total;
      app('INFO', `领域采集完成，数据来源: ${source}，共 ${total} 个项目`);
      if (typeof opts.onSections === 'function') {
        try { opts.onSections(cleanedSections, source); } catch (e) { /* 回调异常不影响主流程 */ }
      }

      const pushedAt = fmtDateTime();
      if (total === 0) {
        const html = buildEmptyHtml({ dateStr: fmtDate(), topN: cfg.topN, pushedAt });
        await pushMail(`${cfg.mail.subjectPrefix}${fmtDate()}订阅领域热门开源项目（本次无数据）`, html);
        app('INFO', '本次无获取到有效热门项目数据');
      } else {
        const html = buildCategoriesHtml({ dateStr: fmtDate(), sections: cleanedSections, pushedAt });
        const labels = cfg.categories.map((c) => c.label).join('与');
        await pushMail(`${cfg.mail.subjectPrefix}${fmtDate()}${labels}领域热门开源项目`, html);
      }
      return record;
    }

    // 全站模式：GitHub Trending 榜单（Search API 兜底）
    app('INFO', `开始扫描 GitHub 热门项目，TOP${cfg.topN}，目标邮箱: ${cfg.mail.to.join(', ')}`);
    const { source, repos: rawRepos } = await collect(cfg.topN, cfg.github && cfg.github.timeoutMs);
    record.source = source;
    app('INFO', `采集完成，数据来源: ${source}，共 ${rawRepos.length} 个项目`);

    const repos = cleanRepos(rawRepos);
    record.projectCount = repos.length;
    if (typeof opts.onRepos === 'function') {
      try { opts.onRepos(repos, source); } catch (e) { /* 回调异常不影响主流程 */ }
    }

    const pushedAt = fmtDateTime();
    if (repos.length === 0) {
      const html = buildEmptyHtml({ dateStr: fmtDate(), topN: cfg.topN, pushedAt });
      await pushMail(`${cfg.mail.subjectPrefix}${fmtDate()}全网热度TOP${cfg.topN}开源项目（本次无数据）`, html);
      app('INFO', '本次无获取到有效热门项目数据');
    } else {
      const html = buildTop10Html({ dateStr: fmtDate(), topN: cfg.topN, source, repos, pushedAt });
      await pushMail(`${cfg.mail.subjectPrefix}${fmtDate()}全网热度TOP${cfg.topN}开源项目`, html);
    }
  } catch (e) {
    record.status = 'failed';
    record.error = e.message;
    app('ERROR', `任务执行异常: ${e.message}`);
  } finally {
    if (logEnabled) {
      try {
        logTask(stamp(record));
      } catch (e) {
        /* ignore */
      }
    }
  }
  return record;
}

module.exports = { runOnce, fmtDate, fmtDateTime };