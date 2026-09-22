'use strict';

const fs = require('fs');
const path = require('path');

const ROOT = path.resolve(__dirname, '..');
const CONFIG_PATH = process.env.GTM_CONFIG || path.join(ROOT, 'config.json');
const LOCK_PATH = path.join(ROOT, 'logs', '.task.lock');

const DEFAULT_CONFIG = {
  schedule: {
    enabled: true,
    time: '09:00',
    frequency: 'daily'
  },
  topN: 10,
  // 领域订阅模式：配置后按领域分板块推送，替代全站 TOP N 模式
  categories: [],
  mail: {
    from: '',
    to: [],
    subjectPrefix: '【GitHub每日热门项目推送】'
  },
  smtp: {
    host: '',
    port: 465,
    secure: true,
    user: '',
    pass: ''
  },
  log: {
    enabled: true,
    keepDays: 30
  },
  github: {
    timeoutMs: 30000
  }
};

function deepMerge(base, override) {
  const out = Array.isArray(base) ? [...base] : { ...base };
  if (typeof base !== 'object' || base === null) return override === undefined ? base : override;
  for (const k of Object.keys(override || {})) {
    const ov = override[k];
    if (ov && typeof ov === 'object' && !Array.isArray(ov) && base[k] && typeof base[k] === 'object') {
      out[k] = deepMerge(base[k], ov);
    } else {
      out[k] = ov;
    }
  }
  return out;
}

function loadConfig() {
  if (!fs.existsSync(CONFIG_PATH)) {
    const err = new Error(
      `未找到配置文件 ${CONFIG_PATH}\n请先执行: node src/index.js --init 生成配置，并填写邮箱/收件人/SMTP 信息后重试。`
    );
    err.code = 'NO_CONFIG';
    throw err;
  }
  const raw = JSON.parse(fs.readFileSync(CONFIG_PATH, 'utf8'));
  const cfg = deepMerge(DEFAULT_CONFIG, raw);

  const missing = [];
  if (!cfg.smtp.host) missing.push('smtp.host');
  if (!cfg.smtp.user) missing.push('smtp.user');
  if (!cfg.smtp.pass) missing.push('smtp.pass');
  if (!cfg.mail.from) missing.push('mail.from');
  if (!Array.isArray(cfg.mail.to) || cfg.mail.to.length === 0) missing.push('mail.to');
  if (/example\.com|your_name|your_smtp_auth_code|receiver@/i.test(JSON.stringify(cfg))) {
    const err = new Error(
      `配置文件仍包含示例占位内容（example.com / your_name 等），请打开 ${CONFIG_PATH} 填写真实的 SMTP、发件人与收件人信息后重试。`
    );
    err.code = 'INVALID_CONFIG';
    throw err;
  }
  const useCategories = Array.isArray(cfg.categories) && cfg.categories.length > 0;
  if (useCategories) {
    for (const c of cfg.categories) {
      if (!c.label || !Array.isArray(c.topics) || c.topics.length === 0 || c.topics.some((t) => !t || typeof t !== 'string')) {
        throw new Error(`categories 配置不完整：每个领域需包含 label 与非空 topics 字符串数组，请检查 ${CONFIG_PATH}`);
      }
      if (c.topics.length > 5) throw new Error(`categories 中 ${c.label} 的 topics 最多 5 个（GitHub 匿名搜索接口有频率限制）`);
      const n = parseInt(c.topN, 10) || 5;
      if (n < 1 || n > 20) throw new Error(`categories 中 ${c.label} 的 topN 需在 1~20 之间，当前为 ${c.topN}`);
      c.topN = n;
      if (!c.key) c.key = String(c.label);
    }
  } else {
    const n = parseInt(cfg.topN, 10);
    if (![5, 10, 20].includes(n)) throw new Error(`配置 topN 必须为 5/10/20 之一，当前为 ${cfg.topN}`);
    cfg.topN = n;
  }

  if (missing.length) {
    const err = new Error(`配置缺少必要项: ${missing.join(', ')}，请检查 ${CONFIG_PATH}`);
    err.code = 'INVALID_CONFIG';
    throw err;
  }
  return cfg;
}

function buildTemplate(cfg) {
  return {
    ...DEFAULT_CONFIG,
    schedule: { ...DEFAULT_CONFIG.schedule, time: cfg.schedule?.time || '09:00', frequency: cfg.schedule?.frequency || 'daily' },
    topN: cfg.topN || 10,
    mail: { ...DEFAULT_CONFIG.mail, ...(cfg.mail || {}) },
    smtp: { ...DEFAULT_CONFIG.smtp, ...(cfg.smtp || {}) },
    log: { ...DEFAULT_CONFIG.log, ...(cfg.log || {}) },
    github: { ...DEFAULT_CONFIG.github, ...(cfg.github || {}) }
  };
}

function ensureLockFile() {
  if (!fs.existsSync(path.dirname(LOCK_PATH))) fs.mkdirSync(path.dirname(LOCK_PATH), { recursive: true });
}

module.exports = { loadConfig, buildTemplate, CONFIG_PATH, LOCK_PATH, ROOT, DEFAULT_CONFIG, ensureLockFile };