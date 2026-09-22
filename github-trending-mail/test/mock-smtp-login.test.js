'use strict';

const net = require('net');
const { sendSmtp } = require('../src/mailer');

const port = 12526;
let loginSteps = 0;

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
        sock.write('250-mock.local\r\n250-AUTH LOGIN\r\n250 SIZE 33554432\r\n');
      } else if (cmd === 'AUTH' && line.toUpperCase() === 'AUTH LOGIN') {
        loginSteps = 1;
        sock.write('334 VXNlcm5hbWU6\r\n');
      } else if (loginSteps === 1) {
        loginSteps = 2;
        sock.write('334 UGFzc3dvcmQ6\r\n');
      } else if (loginSteps === 2) {
        loginSteps = 3;
        sock.write('235 2.7.0 Authentication successful\r\n');
      } else if (cmd === 'MAIL') sock.write('250 2.1.0 Ok\r\n');
      else if (cmd === 'RCPT') sock.write('250 2.1.5 Ok\r\n');
      else if (cmd === 'DATA') sock.write('354 End data with <CR><LF>.<CR><LF>\r\n');
      else if (line === '.') sock.write('250 2.0.0 Ok: queued\r\n');
      else if (cmd === 'QUIT') { sock.write('221 2.0.0 Bye\r\n'); sock.end(); }
    }
  });
});

server.listen(port, '127.0.0.1', async () => {
  const res = await sendSmtp(
    { host: '127.0.0.1', port, secure: false, user: 'u@t.com', pass: 'p' },
    'u@t.com',
    ['x@t.com'],
    's',
    '<p>b</p>',
    5000
  );
  console.log('SEND_RESULT:', res, 'LOGIN_STEPS(expect 3):', loginSteps);
  server.close();
  process.exit(res === true && loginSteps === 3 ? 0 : 1);
});