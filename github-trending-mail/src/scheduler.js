'use strict';

const fs = require('fs');
const path = require('path');

function acquireLock(lockPath) {
  try {
    if (fs.existsSync(lockPath)) {
      const st = fs.statSync(lockPath);
      const age = Date.now() - st.mtimeMs;
      if (age < 30 * 60 * 1000) return false;
      fs.unlinkSync(lockPath);
    }
    fs.writeFileSync(lockPath, String(process.pid), 'utf8');
    return true;
  } catch (e) {
    return false;
  }
}

function releaseLock(lockPath) {
  try {
    if (fs.existsSync(lockPath)) fs.unlinkSync(lockPath);
  } catch (e) {
    /* ignore */
  }
}

function parseTime(time) {
  const m = String(time || '09:00').match(/^(\d{1,2}):(\d{2})$/);
  if (!m) return { hour: 9, minute: 0 };
  return { hour: parseInt(m[1], 10), minute: parseInt(m[2], 10) };
}

function nextRunAt(cfg) {
  const { frequency, time } = cfg.schedule || {};
  const { hour, minute } = parseTime(time);
  const now = new Date();
  const d = new Date(now.getFullYear(), now.getMonth(), now.getDate(), hour, minute, 0);

  if (frequency === 'weekly') {
    while (d <= now) d.setDate(d.getDate() + 7);
  } else if (frequency === 'every2days') {
    while (d <= now) d.setDate(d.getDate() + 2);
  } else {
    if (d <= now) d.setDate(d.getDate() + 1);
  }
  return d;
}

module.exports = { acquireLock, releaseLock, nextRunAt, parseTime };