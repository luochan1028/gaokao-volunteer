'use strict';

const { nextRunAt, parseTime, acquireLock, releaseLock } = require('../src/scheduler');
const fs = require('fs');
const path = require('path');

let failed = 0;
function t(name, cond) {
  if (cond) console.log(`PASS ${name}`);
  else { console.log(`FAIL ${name}`); failed = 1; }
}

t('parseTime 默认', (() => { const { hour, minute } = parseTime('09:30'); return hour === 9 && minute === 30; })());
t('parseTime 非法回退', (() => { const { hour, minute } = parseTime('xx'); return hour === 9 && minute === 0; })());

const now = new Date();
const cfgDaily = { schedule: { frequency: 'daily', time: '09:00' } };
const nextDaily = nextRunAt(cfgDaily);
t('daily 目标在将来', nextDaily.getTime() > Date.now());
t('daily 小时=9', nextDaily.getHours() === 9 && nextDaily.getMinutes() === 0);

const cfg2 = { schedule: { frequency: 'every2days', time: '09:00' } };
const next2 = nextRunAt(cfg2);
const diffDays = Math.round((next2 - now) / 86400000);
t('every2days 间隔 1~2 天', diffDays >= 1 && diffDays <= 2);

const cfgW = { schedule: { frequency: 'weekly', time: '09:00' } };
const nextW = nextRunAt(cfgW);
const diffW = Math.round((nextW - now) / 86400000);
t('weekly 间隔 1~7 天', diffW >= 1 && diffW <= 7);

const lockPath = path.join(__dirname, '..', 'logs', '.sched.test.lock');
releaseLock(lockPath);
t('acquire 首次成功', acquireLock(lockPath) === true);
t('acquire 二次失败(去重)', acquireLock(lockPath) === false);
releaseLock(lockPath);
t('release 后释放文件', !fs.existsSync(lockPath));

process.exit(failed);