'use strict';

const fs = require('fs');
const path = require('path');
const { loadConfig, buildTemplate, CONFIG_PATH, LOCK_PATH, ROOT } = require('./config');
const { runOnce } = require('./pipeline');
const { nextRunAt, acquireLock, releaseLock } = require('./scheduler');
const { app, readTasks, cleanupOld, ts } = require('./logger');

function cmdInit() {
  if (fs.existsSync(CONFIG_PATH)) {
    console.log(`配置文件已存在: ${CONFIG_PATH}`);
    return;
  }
  const cfg = buildTemplate({});
  cfg.smtp.host = 'smtp.example.com';
  cfg.smtp.port = 465;
  cfg.smtp.user = 'your_name@example.com';
  cfg.smtp.pass = 'your_smtp_auth_code';
  cfg.mail.from = 'your_name@example.com';
  cfg.mail.to = ['receiver@example.com'];
  fs.writeFileSync(CONFIG_PATH, JSON.stringify(cfg, null, 2) + '\n', 'utf8');
  console.log(`已生成配置文件模板: ${CONFIG_PATH}`);
  console.log('请编辑该文件，填写 SMTP 服务器、邮箱账号密码及收件人地址后运行:');
  console.log('  node src/index.js --once   # 手动触发一次');
  console.log('  npm run start              # 后台定时运行');
}

async function cmdOnce(verbose) {
  if (verbose) process.env.GTM_VERBOSE = '1';
  if (!acquireLock(LOCK_PATH)) {
    console.log('检测到已有扫描任务正在执行，本次跳过（任务去重）。');
    return;
  }
  let cfg;
  try {
    cfg = loadConfig();
  } catch (e) {
    console.error('配置加载失败: ' + e.message);
    app('ERROR', '配置加载失败: ' + e.message);
    releaseLock(LOCK_PATH);
    return;
  }
  try {
    if (cfg.log && cfg.log.enabled) cleanupOld(cfg.log.keepDays);
    const record = await runOnce(cfg);
    console.log(`[任务结束] 状态: ${record.status}，获取项目: ${record.projectCount}，邮件: ${record.mailResult}`);
  } finally {
    releaseLock(LOCK_PATH);
  }
}

async function cmdDaemon() {
  let cfg;
  try {
    cfg = loadConfig();
  } catch (e) {
    console.error('配置加载失败: ' + e.message);
    app('ERROR', '配置加载失败: ' + e.message);
    return;
  }
  const schedule = cfg.schedule;
  if (!schedule || schedule.enabled === false) {
    console.log('定时任务未启用（schedule.enabled=false），请使用 --once 手动触发，或修改配置后重试。');
    return;
  }
  const freqLabel = { daily: '每天', every2days: '每两天', weekly: '每周' }[schedule.frequency] || schedule.frequency;
  console.log(`GitHub 热门项目邮件推送系统已启动`);
  console.log(`  扫描周期: ${freqLabel} ${schedule.time} | TOP${cfg.topN} | 收件人: ${cfg.mail.to.join(', ')}`);
  console.log(`  首次执行: ${nextRunAt(cfg).toLocaleString('zh-CN')}`);
  console.log('按 Ctrl+C 停止。');

  let running = false;
  async function checkAndRun() {
    const now = new Date();
    const target = nextRunAt(cfg);
    if (now >= target && !running) {
      running = true;
      try {
        if (acquireLock(LOCK_PATH)) {
          try {
            if (cfg.log && cfg.log.enabled) cleanupOld(cfg.log.keepDays);
            await runOnce(cfg);
          } finally {
            releaseLock(LOCK_PATH);
          }
        } else {
          app('WARN', '检测到任务锁，跳过本次定时执行');
        }
      } finally {
        running = false;
      }
    }
  }
  await checkAndRun();
  setInterval(checkAndRun, 60000);
}

function cmdLogs(limit) {
  const rows = readTasks(limit || 30);
  if (rows.length === 0) {
    console.log('暂无任务日志记录。');
    return;
  }
  console.log('最近任务记录:');
  for (const r of rows) {
    console.log(
      `  ${r.time} | ${r.status.toUpperCase().padEnd(6)} | 来源: ${(r.source || '无').padEnd(18)} | 项目数: ${String(r.projectCount).padEnd(3)} | 邮件: ${r.mailResult}${r.error ? ' | 异常: ' + r.error : ''}`
    );
  }
}

async function cmdTestMail() {
  let cfg;
  try {
    cfg = loadConfig();
  } catch (e) {
    console.error('配置加载失败: ' + e.message);
    return;
  }
  const { sendMail } = require('./mailer');
  const { esc } = require('./html');
  console.log(`正在测试 SMTP 配置 ${cfg.smtp.user} -> ${cfg.mail.to.join(', ')} ...`);
  const html = `<!DOCTYPE html><html><body style="font-family:sans-serif;background:#f5f7fb;padding:24px;">
    <div style="max-width:600px;margin:0 auto;background:#fff;border:1px solid #dfe6f2;border-radius:10px;padding:24px 28px;">
      <h2 style="color:#1e2a52;margin-top:0;">SMTP 配置测试邮件</h2>
      <p style="color:#3a4358;font-size:14px;line-height:1.8;">这是一封来自「GitHub 热门项目自动扫描推送系统」的测试邮件，说明 SMTP 配置正确、发送链路正常。<br>
      如果收到本邮件，即可正常运行 <b>npm run scan</b> 手动执行一次扫描推送。</p>
      <p style="color:#9aa5c0;font-size:12px;">发送时间：${esc(new Date().toLocaleString('zh-CN'))}</p>
    </div></body></html>`;
  const res = await sendMail(cfg, '【GitHub热门项目推送】SMTP 配置测试邮件', html, { attempts: 3 });
  if (res.ok) {
    console.log(`测试邮件发送成功（尝试 ${res.attempts} 次）。`);
  } else {
    console.error(`测试邮件发送失败: ${res.error}`);
  }
}

async function main() {
  const args = process.argv.slice(2);
  const arg = args[0] || '--daemon';

  switch (arg) {
    case '--init':
      cmdInit();
      break;
    case '--once':
      await cmdOnce(args.includes('-v') || args.includes('--verbose'));
      break;
    case '--daemon':
      await cmdDaemon();
      break;
    case '--test-mail':
      await cmdTestMail();
      break;
    case '--logs': {
      const idx = args.indexOf('--limit');
      const limit = idx > -1 ? parseInt(args[idx + 1], 10) : 30;
      cmdLogs(limit);
      break;
    }
    case '--help':
    case '-h':
      console.log(`GitHub 热门项目自动扫描邮件推送系统
用法:
  npm run init            生成配置文件模板 config.json
  npm run scan            手动执行一次扫描并推送
  npm run start           后台常驻，按配置周期自动扫描推送
  npm run test-mail       发送一封 SMTP 配置测试邮件
  npm run logs            查看最近任务执行日志
  node src/index.js --logs --limit 50   查看最近50条日志
`);
      break;
    default:
      console.log(`未知参数: ${arg}，使用 --help 查看帮助`);
  }
}

main().catch((e) => {
  console.error('程序异常退出: ' + (e && e.message));
  process.exit(1);
});