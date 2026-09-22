'use strict';

const net = require('net');
const fs = require('fs');
const os = require('os');
const path = require('path');

// 测试日志写入临时目录，避免污染真实 logs/
process.env.GTM_LOG_DIR = fs.mkdtempSync(path.join(os.tmpdir(), 'gtm-e2e-logs-'));

const { runOnce } = require('../src/pipeline');

async function e2eViaRealSendMail() {
  const port = 12527;
  let received = '';
  const server = net.createServer((sock) => {
    sock.write('220 mock.local ESMTP ready\r\n');
    let buf = '';
    sock.on('data', (d) => {
      buf += d.toString('utf8');
      let idx;
      while ((idx = buf.indexOf('\r\n')) >= 0) {
        const line = buf.slice(0, idx);
        buf = buf.slice(idx + 2);
        const cmd = line.split(' ')[0].toUpperCase();
        if (cmd === 'EHLO') sock.write('250-mock.local\r\n250-AUTH PLAIN\r\n250 SIZE 33554432\r\n');
        else if (cmd === 'AUTH') sock.write('235 2.7.0 ok\r\n');
        else if (cmd === 'MAIL') sock.write('250 2.1.0 Ok\r\n');
        else if (cmd === 'RCPT') sock.write('250 2.1.5 Ok\r\n');
        else if (cmd === 'DATA') sock.write('354 End\r\n');
        else if (line === '.') { received += 'DATA_EOT'; sock.write('250 2.0.0 queued\r\n'); }
        else received += line + '\n';
        if (cmd === 'QUIT') { sock.write('221 bye\r\n'); sock.end(); }
      }
    });
  });

  await new Promise((r) => server.listen(port, '127.0.0.1', r));

  const cfg = {
    schedule: { enabled: true, time: '09:00', frequency: 'daily' },
    topN: 10,
    mail: { from: 'sender@test.com', to: ['r1@test.com', 'r2@test.com'], subjectPrefix: '【GitHub每日热门项目推送】' },
    smtp: { host: '127.0.0.1', port, secure: false, user: 'sender@test.com', pass: 'x' },
    log: { enabled: true, keepDays: 30 },
    github: { timeoutMs: 30000 }
  };

  const rec = await runOnce(cfg);
  console.log('REC_STATUS:', rec.status, '| projects:', rec.projectCount, '| mail:', rec.mailResult, '| source:', rec.source);
  console.log('RECEIVED_SUBJECT_LINE:', received.split('\n').find((l) => l.startsWith('Subject:')) ? 'YES' : 'NO');
  console.log('RECEIVED_HAS_EMAIL_CONTENT:', received.includes('DATA_EOT') ? 'YES' : 'NO');
  console.log('RECEIVED_HAS_HTML_TABLE:', received.includes('热度数据') ? 'YES' : 'NO');

  const hasTaskLog = fs.existsSync(path.join(process.env.GTM_LOG_DIR, 'tasks.jsonl'));
  console.log('TASK_LOG_WRITTEN:', hasTaskLog);
  server.close();
  const ok = rec.status === 'done' && rec.projectCount > 0 && rec.mailResult.startsWith('推送成功') && hasTaskLog;
  process.exit(ok ? 0 : 1);
}

e2eViaRealSendMail().catch((e) => {
  console.error('E2E FAIL', e);
  process.exit(1);
});