'use strict';

const fs = require('fs');
const path = require('path');
const { ROOT } = require('./config');

const LOG_DIR = process.env.GTM_LOG_DIR || path.join(ROOT, 'logs');
const TASK_LOG = path.join(LOG_DIR, 'tasks.jsonl');
const APP_LOG = path.join(LOG_DIR, 'app.log');

function ensureDir() {
  if (!fs.existsSync(LOG_DIR)) fs.mkdirSync(LOG_DIR, { recursive: true });
}

function ts() {
  const d = new Date();
  const p = (n) => String(n).padStart(2, '0');
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}:${p(d.getSeconds())}`;
}

function app(type, msg, extra) {
  ensureDir();
  const line = `[${ts()}] [${type}] ${msg}`;
  try {
    fs.appendFileSync(APP_LOG, line + '\n', 'utf8');
  } catch (e) {
    /* ignore */
  }
  if (type === 'ERROR') console.error(line);
  else if (process.env.GTM_VERBOSE) console.log(line);
}

function logTask(record) {
  ensureDir();
  try {
    fs.appendFileSync(TASK_LOG, JSON.stringify(record) + '\n', 'utf8');
  } catch (e) {
    /* ignore */
  }
}

function readTasks(limit) {
  ensureDir();
  if (!fs.existsSync(TASK_LOG)) return [];
  const rows = fs
    .readFileSync(TASK_LOG, 'utf8')
    .split('\n')
    .filter((l) => l.trim())
    .map((l) => {
      try {
        return JSON.parse(l);
      } catch (e) {
        return null;
      }
    })
    .filter(Boolean);
  return limit ? rows.slice(-limit).reverse() : rows.reverse();
}

function cleanupOld(keepDays) {
  ensureDir();
  if (!keepDays || keepDays <= 0) return;
  const cutoff = Date.now() - keepDays * 86400000;
  try {
    const stat = fs.statSync(TASK_LOG);
    if (stat.mtimeMs > cutoff) {
      const old = readTasks().filter((r) => new Date(r.time).getTime() < cutoff);
      if (old.length) {
        const remaining = readTasks().filter((r) => new Date(r.time).getTime() >= cutoff);
        fs.writeFileSync(TASK_LOG, remaining.map((r) => JSON.stringify(r)).join('\n') + (remaining.length ? '\n' : ''), 'utf8');
        app('INFO', `历史任务日志清理完成，删除 ${old.length} 条过期记录`);
      }
    }
  } catch (e) {
    /* ignore */
  }
}

module.exports = { app, logTask, readTasks, cleanupOld, TASK_LOG, APP_LOG, LOG_DIR, ts };