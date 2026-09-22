'use strict';

const net = require('net');
const { sendSmtp } = require('../src/mailer');

const port = 12525;
let gotAuth = false;
let gotData = false;

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
      if (cmd === 'EHLO') {
        sock.write('250-mock.local\r\n250-AUTH PLAIN LOGIN\r\n250 SIZE 33554432\r\n');
      } else if (cmd === 'AUTH') {
        gotAuth = true;
        sock.write('235 2.7.0 Authentication successful\r\n');
      } else if (cmd === 'MAIL') {
        sock.write('250 2.1.0 Ok\r\n');
      } else if (cmd === 'RCPT') {
        sock.write('250 2.1.5 Ok\r\n');
      } else if (cmd === 'DATA') {
        gotData = true;
        sock.write('354 End data with <CR><LF>.<CR><LF>\r\n');
      } else if (cmd === 'QUIT') {
        sock.write('221 2.0.0 Bye\r\n');
        sock.end();
      } else if (line === '.') {
        sock.write('250 2.0.0 Ok: queued\r\n');
      }
    }
  });
});

server.listen(port, '127.0.0.1', async () => {
  const res = await sendSmtp(
    {
      host: '127.0.0.1',
      port,
      secure: false,
      user: 'sender@test.com',
      pass: 'secret'
    },
    'sender@test.com',
    ['rcpt1@test.com', 'rcpt2@test.com'],
    'subject test',
    '<h1>Hello</h1><p>邮件内容测试</p>',
    5000
  );
  console.log('SEND_RESULT:', res);
  console.log('AUTH_DONE:', gotAuth, 'DATA_SENT:', gotData);
  server.close();

  let code = 0;
  const mk = (name, target, src) => {
    const r = src === '.' ? gotData : eval(src);
    if (r === target) console.log(`PASS ${name}`);
    else { console.log(`FAIL ${name} expect ${target} got ${r}`); code = 1; }
  };
  mk('AUTH handled', true, 'gotAuth');
  mk('DATA handled', true, 'gotData');
  process.exit(code);
});