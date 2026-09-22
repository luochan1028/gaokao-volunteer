const fs = require('fs');
const path = require('path');
const { execSync } = require('child_process');
const drama = require('./sources/drama');
const { renderReport } = require('./reporter');

const ROOT = path.join(__dirname, '..');
const LOG_FILE = path.join(ROOT, 'logs', 'run.log');
const TASK_NAME = 'VideoPromptsRefresh';
const INTERVAL_HOURS = 2;

function log(msg) {
  const line = `[${new Date().toISOString()}] ${msg}`;
  console.log(line);
  fs.mkdirSync(path.dirname(LOG_FILE), { recursive: true });
  fs.appendFileSync(LOG_FILE, line + '\n');
}

function pad(n) {
  return String(n).padStart(2, '0');
}

async function runOnce() {
  const startedAt = new Date();
  log('===== 开始抓取 AI 短剧提示词 =====');
  let dramaData = null;
  let dramaError = '';
  try {
    dramaData = await drama.fetchDramaData();
    log(`drama 源: 技能 ${dramaData.skills.length} 个, 提示词组 ${dramaData.promptGroups.length} 组${dramaData.fromCache ? ' (缓存)' : ''}`);
  } catch (e) {
    dramaError = e.message;
    log(`drama 源: 失败 - ${e.message}`);
  }
  const md = renderReport({ dramaData, dramaError }, startedAt);
  const reportsDir = path.join(ROOT, 'reports');
  const archiveDir = path.join(reportsDir, 'archive');
  fs.mkdirSync(archiveDir, { recursive: true });
  const stamp = `${startedAt.getFullYear()}-${pad(startedAt.getMonth() + 1)}-${pad(startedAt.getDate())}_${pad(startedAt.getHours())}${pad(startedAt.getMinutes())}`;
  fs.writeFileSync(path.join(reportsDir, 'latest.md'), md, 'utf8');
  fs.writeFileSync(path.join(archiveDir, `${stamp}.md`), md, 'utf8');
  log('报告已生成: reports/latest.md');
}

function installTask() {
  const cmdPath = path.join(ROOT, 'run-task.cmd');
  const nodeExe = process.execPath;
  const cmd = [
    '@echo off',
    'cd /d "%~dp0"',
    'if not exist logs mkdir logs',
    `"${nodeExe}" "%~dp0src\\index.js" --once >> "%~dp0logs\\task.log" 2>&1`,
    ''
  ].join('\r\n');
  fs.writeFileSync(cmdPath, cmd, 'utf8');
  const start = new Date(Date.now() + 60 * 1000);
  const st = `${pad(start.getHours())}:${pad(start.getMinutes())}`;
  const tr = `"${cmdPath}"`;
  execSync(`schtasks /Create /F /TN "${TASK_NAME}" /SC HOURLY /MO ${INTERVAL_HOURS} /ST ${st} /TR ${tr}`, { stdio: 'inherit' });
  console.log(`已注册计划任务 "${TASK_NAME}"：每 ${INTERVAL_HOURS} 小时运行一次，首次执行 ${st}`);
  execSync(`schtasks /Query /TN "${TASK_NAME}"`, { stdio: 'inherit' });
}

function removeTask() {
  execSync(`schtasks /Delete /TN "${TASK_NAME}" /F`, { stdio: 'inherit' });
  console.log(`已删除计划任务 "${TASK_NAME}"`);
}

function taskStatus() {
  execSync(`schtasks /Query /TN "${TASK_NAME}" /V /FO LIST`, { stdio: 'inherit' });
}

function main() {
  const arg = process.argv[2] || '--once';
  if (arg === '--install-task') return installTask();
  if (arg === '--remove-task') return removeTask();
  if (arg === '--task-status') return taskStatus();
  if (arg === '--once') {
    return runOnce()
      .then(() => process.exit(0))
      .catch(e => {
        log(`抓取异常: ${e.stack || e.message}`);
        process.exit(1);
      });
  }
  console.log('用法: node src/index.js [--once | --install-task | --remove-task | --task-status]');
}

main();
