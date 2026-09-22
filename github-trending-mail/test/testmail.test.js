'use strict';

const net = require('net');
const fs = require('fs');
const path = require('path');
const { execFileSync } = require('child_process');
const { sendMail } = require('../src/mailer');

async function main() {
  const port = 12528;
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
        else if (line === '.') sock.write('250 2.0.0 queued\r\n');
        else if (cmd === 'QUIT') { sock.write('221 bye\r\n'); sock.end(); }
      }
    });
  });
  await new Promise((r) => server.listen(port, '127.0.0.1', r));

  const cfg = {
    smtp: { host: '127.0.0.1', port, secure: false, user: 'u@t.com', pass: 'p' },
    mail: { from: 'u@t.com', to: ['to@t.com'] }
  };
  const res = await sendMail(cfg, '【GitHub热门项目推送】SMTP 配置测试邮件', '<p>test</p>');
  console.log('SEND:', JSON.stringify(res));
  server.close();
  process.exit(res.ok ? 0 : 1);
}

main().catch((e) => { console.error('FAIL', e); process.exit(1); });