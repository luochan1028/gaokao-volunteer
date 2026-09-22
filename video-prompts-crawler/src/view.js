const fs = require('fs');
const path = require('path');
const { execSync } = require('child_process');

const ROOT = path.join(__dirname, '..');
const arg = process.argv[2];
const mdPath = arg
  ? (path.isAbsolute(arg) ? arg : path.join(ROOT, arg))
  : path.join(ROOT, 'reports', 'latest.md');
const htmlPath = mdPath.replace(/\.md$/i, '.html');

function esc(s) {
  return s.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
}

function inline(s) {
  let out = esc(s);
  out = out.replace(/`([^`]+)`/g, '<code>$1</code>');
  out = out.replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>');
  out = out.replace(/(^|[^*])\*([^*\n]+)\*(?!\*)/g, '$1<em>$2</em>');
  out = out.replace(/\[([^\]]+)\]\((https?:\/\/[^)]+)\)/g, '<a href="$2" target="_blank">$1</a>');
  return out;
}

function render(md) {
  const lines = md.split('\n');
  const out = [];
  let inCode = false;
  let inList = false;
  let listTag = '';
  let tableBuf = [];

  const closeList = () => {
    if (inList) {
      out.push(`</${listTag}>`);
      inList = false;
    }
  };
  const flushTable = () => {
    if (tableBuf.length === 0) return;
    const rows = tableBuf.filter(r => !/^\|[\s:|-]+\|$/.test(r.trim()));
    const cells = rows.map(r => r.trim().replace(/^\||\|$/g, '').split('|').map(c => c.trim()));
    if (cells.length > 0) {
      out.push('<table>');
      out.push('<thead><tr>' + cells[0].map(c => `<th>${inline(c)}</th>`).join('') + '</tr></thead>');
      if (cells.length > 1) {
        out.push('<tbody>');
        for (const r of cells.slice(1)) {
          out.push('<tr>' + r.map(c => `<td>${inline(c)}</td>`).join('') + '</tr>');
        }
        out.push('</tbody>');
      }
      out.push('</table>');
    }
    tableBuf = [];
  };

  for (const raw of lines) {
    const line = raw.replace(/\s+$/, '');
    if (/^```/.test(line.trim())) {
      flushTable();
      closeList();
      out.push(inCode ? '</code></pre>' : '<pre><code>');
      inCode = !inCode;
      continue;
    }
    if (inCode) {
      out.push(esc(raw));
      continue;
    }
    if (/^\|.*\|$/.test(line.trim())) {
      closeList();
      tableBuf.push(line);
      continue;
    }
    flushTable();
    if (/^#{1,6} /.test(line)) {
      closeList();
      const level = line.match(/^#+/)[0].length;
      out.push(`<h${level}>${inline(line.replace(/^#+ /, ''))}</h${level}>`);
      continue;
    }
    if (/^>\s?/.test(line)) {
      closeList();
      out.push(`<blockquote>${inline(line.replace(/^>\s?/, '')) || '&nbsp;'}</blockquote>`);
      continue;
    }
    if (/^(---|\*\*\*)$/.test(line.trim())) {
      closeList();
      out.push('<hr>');
      continue;
    }
    const ulMatch = line.match(/^[-*] (.*)/);
    const olMatch = line.match(/^(\d+)[.、] (.*)/);
    if (ulMatch) {
      if (!inList || listTag !== 'ul') {
        closeList();
        out.push('<ul>');
        inList = true;
        listTag = 'ul';
      }
      out.push(`<li>${inline(ulMatch[1])}</li>`);
      continue;
    }
    if (olMatch) {
      if (!inList || listTag !== 'ol') {
        closeList();
        out.push('<ol>');
        inList = true;
        listTag = 'ol';
      }
      out.push(`<li>${inline(olMatch[2])}</li>`);
      continue;
    }
    closeList();
    if (line.trim() === '') {
      out.push('');
    } else {
      out.push(`<p>${inline(line)}</p>`);
    }
  }
  flushTable();
  closeList();
  if (inCode) out.push('</code></pre>');
  return out.join('\n');
}

const md = fs.readFileSync(mdPath, 'utf8');
const titleMatch = md.match(/^#\s+(.+)$/m);
const pageTitle = titleMatch ? titleMatch[1] : '提示词手册';
const html = `<!DOCTYPE html>
<html lang="zh-CN">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>${pageTitle}</title>
<style>
  :root { color-scheme: dark; }
  * { box-sizing: border-box; }
  body {
    margin: 0; padding: 0;
    background: #0d1117; color: #e6edf3;
    font-family: "Segoe UI", "Microsoft YaHei", sans-serif;
    line-height: 1.75; font-size: 15px;
  }
  .wrap { max-width: 960px; margin: 0 auto; padding: 40px 28px 80px; }
  h1 { font-size: 30px; padding-bottom: 14px; border-bottom: 3px solid #2f81f7; color: #ffffff; }
  h2 { font-size: 22px; margin-top: 44px; padding: 10px 16px; background: #161b22; border-left: 5px solid #2f81f7; border-radius: 6px; color: #ffffff; }
  h3 { font-size: 18px; margin-top: 30px; color: #79c0ff; }
  h4 { font-size: 16px; color: #7ee787; margin-bottom: 6px; }
  blockquote {
    margin: 14px 0; padding: 10px 18px;
    background: #161b22; border-left: 4px solid #d29922;
    border-radius: 0 8px 8px 0; color: #d8dee6;
  }
  pre {
    background: #010409; border: 1px solid #30363d; border-radius: 10px;
    padding: 16px 18px; overflow-x: auto; margin: 10px 0 18px;
  }
  code { font-family: Consolas, "Cascadia Mono", monospace; font-size: 13.5px; color: #a5d6ff; }
  p > code, li > code, td > code {
    background: #1f2937; color: #ffa657; padding: 2px 7px; border-radius: 5px; font-size: 13px;
  }
  a { color: #58a6ff; text-decoration: none; }
  a:hover { text-decoration: underline; }
  table { border-collapse: collapse; width: 100%; margin: 14px 0; font-size: 14px; }
  th { background: #21262d; color: #ffffff; }
  th, td { border: 1px solid #30363d; padding: 8px 12px; text-align: left; }
  tr:nth-child(even) td { background: #11161d; }
  hr { border: none; border-top: 1px dashed #30363d; margin: 36px 0; }
  em { color: #8b949e; }
  ul, ol { padding-left: 26px; }
  li { margin: 5px 0; }
</style>
</head>
<body><div class="wrap">
${render(md)}
</div></body>
</html>`;

fs.writeFileSync(htmlPath, html, 'utf8');
console.log(`预览页已生成: ${htmlPath}`);
execSync(`start msedge "file:///${htmlPath.replace(/\\/g, '/')}"`, { shell: 'cmd.exe' });
console.log('已在浏览器打开');