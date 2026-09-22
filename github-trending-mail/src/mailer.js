'use strict';

const net = require('net');
const tls = require('tls');

function readResponse(sock, timeoutMs) {
  return new Promise((resolve, reject) => {
    let buf = '';
    let timer = setTimeout(() => reject(new Error('SMTP 响应超时')), timeoutMs || 30000);
    const onData = (chunk) => {
      buf += chunk.toString('utf8');
      if (/\r\n$/.test(buf)) {
        clearTimeout(timer);
        sock.removeListener('data', onData);
        resolve(buf.trim());
      }
    };
    sock.on('data', onData);
    sock.on('error', () => {});
  });
}

function writeLine(sock, line) {
  return new Promise((resolve, reject) => {
    sock.write(line + '\r\n', 'utf8', (err) => (err ? reject(err) : resolve()));
  });
}

function parseCode(text) {
  const m = text.match(/^(\d{3})/);
  return m ? parseInt(m[1], 10) : 0;
}

function base64(s) {
  return Buffer.from(s, 'utf8').toString('base64');
}

async function connect(smtp, timeoutMs) {
  const { host, port, secure, user, pass } = smtp;
  const connTimeout = timeoutMs || 30000;

  const capabilities = { starttls: false, plain: false, login: false, size: 0 };

  if (secure) {
    const tlsSock = await new Promise((resolve, reject) => {
      const sock = tls.connect({ host, port, rejectUnauthorized: false }, () => resolve(sock));
      let timer = setTimeout(() => {
        sock.destroy();
        reject(new Error(`TLS 连接到 ${host}:${port} 超时`));
      }, connTimeout);
      sock.on('error', (e) => {
        clearTimeout(timer);
        reject(new Error(`TLS 连接失败: ${e.message}`));
      });
      sock.on('secureConnect', () => clearTimeout(timer));
    });
    let resp = await readResponse(tlsSock, connTimeout);
    if (parseCode(resp) >= 400) throw new Error(`SMTP 服务器返回 ${resp}`);
    resp = await ehlo(tlsSock);
    if (parseCode(resp) >= 400) throw new Error(`EHLO 失败: ${resp}`);
    parseCapabilities(resp, capabilities);
    await auth(tlsSock, user, pass, capabilities, connTimeout);
    return tlsSock;
  }

  let sock = await new Promise((resolve, reject) => {
    const sock = net.connect({ host, port }, () => resolve(sock));
    let timer = setTimeout(() => {
      sock.destroy();
      reject(new Error(`连接到 ${host}:${port} 超时`));
    }, connTimeout);
    sock.on('error', (e) => {
      clearTimeout(timer);
      reject(new Error(`SMTP 连接失败: ${e.message}`));
    });
    sock.on('connect', () => clearTimeout(timer));
  });

  let resp = await readResponse(sock, connTimeout);
  const code0 = parseCode(resp);
  if (code0 >= 400) throw new Error(`SMTP 服务器返回 ${resp}`);

  resp = await ehlo(sock);
  if (parseCode(resp) >= 400) {
    throw new Error(`EHLO 失败: ${resp}`);
  }
  parseCapabilities(resp, capabilities);

  if (capabilities.starttls) {
    await writeLine(sock, 'STARTTLS');
    resp = await readResponse(sock, connTimeout);
    if (parseCode(resp) >= 400) throw new Error(`STARTTLS 失败: ${resp}`);
    sock = await upgradeTls(sock, host);
    resp = await ehlo(sock);
    if (parseCode(resp) >= 400) throw new Error(`EHLO(TLS) 失败: ${resp}`);
    parseCapabilities(resp, capabilities);
  }

  await auth(sock, user, pass, capabilities, connTimeout);
  return sock;
}

function ehlo(sock) {
  return new Promise((resolve, reject) => {
    sock.write('EHLO github-trending-mail\r\n', 'utf8', (err) => {
      if (err) return reject(err);
      let buf = '';
      let settled = false;
      const onData = (chunk) => {
        buf += chunk.toString('utf8');
        if (/\r\n\r\n/.test(buf) || /\r\n$/.test(buf)) {
          const lines = buf.trim().split('\r\n');
          const ok = lines.every((l) => /^\d{3}-/.test(l) || /^\d{3} /.test(l));
          if (ok && lines.length > 0) {
            settled = true;
            sock.removeListener('data', onData);
            resolve(buf.trim());
          }
        }
      };
      sock.on('data', onData);
      setTimeout(() => {
        if (!settled) {
          sock.removeListener('data', onData);
          resolve(buf.trim());
        }
      }, 10000);
    });
  });
}

function parseCapabilities(resp, caps) {
  for (const line of resp.split('\r\n')) {
    const m = line.match(/^\d{3}[- ](\S.*)$/);
    if (!m) continue;
    const s = m[1].toUpperCase();
    if (s.startsWith('STARTTLS')) caps.starttls = true;
    if (s.startsWith('AUTH')) {
      caps.plain = /PLAIN/i.test(s);
      caps.login = /LOGIN/i.test(s);
    }
    const sm = s.match(/^SIZE\s+(\d+)/);
    if (sm) caps.size = parseInt(sm[1], 10);
  }
}

function upgradeTls(sock, host) {
  return new Promise((resolve, reject) => {
    const tlsSock = tls.connect({ socket: sock, host, servername: host, rejectUnauthorized: false });
    tlsSock.on('secureConnect', () => resolve(tlsSock));
    tlsSock.on('error', (e) => reject(new Error(`TLS 握手失败: ${e.message}`)));
  });
}

async function auth(sock, user, pass, caps, timeoutMs) {
  if (caps.plain) {
    await writeLine(sock, `AUTH PLAIN ${base64(`\u0000${user}\u0000${pass}`)}`);
    const r = await readResponse(sock, timeoutMs);
    if (parseCode(r) >= 400) throw new Error(`AUTH PLAIN 认证失败: ${r}`);
  } else if (caps.login) {
    await writeLine(sock, `AUTH LOGIN`);
    let r = await readResponse(sock, timeoutMs);
    if (parseCode(r) !== 334) throw new Error(`AUTH LOGIN 失败: ${r}`);
    await writeLine(sock, base64(user));
    r = await readResponse(sock, timeoutMs);
    if (parseCode(r) !== 334) throw new Error(`AUTH LOGIN 用户名失败: ${r}`);
    await writeLine(sock, base64(pass));
    r = await readResponse(sock, timeoutMs);
    if (parseCode(r) >= 400) throw new Error(`AUTH LOGIN 认证失败: ${r}`);
  } else {
    throw new Error('SMTP 服务器不支持任何 AUTH 方式');
  }
}

async function sendSmtp(smtp, from, toList, subject, htmlText, timeoutMs) {
  const sock = await connect(smtp, timeoutMs);
  try {
    await writeLine(sock, `MAIL FROM:<${from}>`);
    let r = await readResponse(sock, timeoutMs);
    if (parseCode(r) >= 400) throw new Error(`MAIL FROM 失败: ${r}`);
    for (const to of toList) {
      await writeLine(sock, `RCPT TO:<${to}>`);
      r = await readResponse(sock, timeoutMs);
      if (parseCode(r) >= 400) throw new Error(`RCPT TO <${to}> 失败: ${r}`);
    }
    await writeLine(sock, 'DATA');
    r = await readResponse(sock, timeoutMs);
    if (parseCode(r) !== 354) throw new Error(`DATA 失败: ${r}`);

    const dateStr = new Date().toUTCString();
    const headers = [
      `From: =?UTF-8?B?${base64('GitHub热门项目推送')}?= <${from}>`,
      `To: ${toList.join(', ')}`,
      `Subject: =?UTF-8?B?${base64(subject)}?=`,
      `Date: ${dateStr}`,
      'MIME-Version: 1.0',
      'Content-Type: text/html; charset=UTF-8',
      'Content-Transfer-Encoding: 8bit',
      ''
    ].join('\r\n');

    const body = htmlText.replace(/\r\n/g, '\n').replace(/\n/g, '\r\n');
    const dotEscaped = body.replace(/^\./gm, '..');
    await writeLine(sock, headers + '\r\n');
    await new Promise((resolve, reject) => {
      sock.write(dotEscaped + '\r\n.\r\n', 'utf8', (err) => (err ? reject(err) : resolve()));
    });
    r = await readResponse(sock, timeoutMs);
    if (parseCode(r) >= 400) throw new Error(`邮件写入失败: ${r}`);
    await writeLine(sock, 'QUIT');
    return true;
  } finally {
    sock.end();
    sock.destroy();
  }
}

async function sendMail(cfg, subject, htmlText, options) {
  const smtp = cfg.smtp;
  const to = cfg.mail.to;
  const from = cfg.mail.from;
  const maxAttempts = (options && options.attempts) || 3;
  let lastErr;
  for (let i = 1; i <= maxAttempts; i++) {
    try {
      await sendSmtp(smtp, from, to, subject, htmlText);
      return { ok: true, attempts: i };
    } catch (e) {
      lastErr = e;
      if (i < maxAttempts) {
        const wait = 1500 * i;
        await new Promise((r) => setTimeout(r, wait));
      }
    }
  }
  return { ok: false, attempts: maxAttempts, error: lastErr ? lastErr.message : '未知错误' };
}

module.exports = { sendMail, sendSmtp };