const fs = require('fs');
const path = require('path');

const header = `[Script Info]
Title: 加班猫
ScriptType: v4.00+
PlayResX: 720
PlayResY: 1280

[V4+ Styles]
Format: Name, Fontname, Fontsize, PrimaryColour, SecondaryColour, OutlineColour, BackColour, Bold, Italic, Underline, StrikeOut, ScaleX, ScaleY, Spacing, Angle, BorderStyle, Outline, Shadow, Alignment, MarginL, MarginR, MarginV, Encoding
Style: Title,Microsoft YaHei,54,&H00FFFFFF,&H00FFFFFF,&H00000000,&H96000000,1,0,0,0,100,100,0,0,1,5,0,5,0,0,0,1
Style: Dialog,Microsoft YaHei,34,&H00000000,&H00000000,&H00FFFFFF,&H80000000,0,0,0,0,100,100,0,0,1,2,0,7,0,0,0,1

[Events]
Format: Layer, Start, End, Style, Name, MarginL, MarginR, MarginV, Effect, Text
`;

const events = [
  ['Title', 0.6, 3, '{\\pos(360,430)\\an5}凌晨1点 · 第99版PPT'],
  ['Dialog', 13.3, 17.4, '{\\pos(160,1018)\\an7}小猫，方案好了吗？'],
  ['Dialog', 18.3, 23.4, '{\\pos(160,1018)\\an7}马上马上！'],
  ['Dialog', 24.3, 29.4, '{\\pos(160,1018)\\an7}辛苦了，改完这版就下班。'],
  ['Dialog', 30.3, 35.4, '{\\pos(160,1018)\\an7}老板……呜呜……'],
  ['Dialog', 36.6, 41.4, '{\\pos(360,814)\\an5\\c&H3C2850&\\b1}第 1 页 · 共 99 页'],
  ['Dialog', 42.3, 47.4, '{\\pos(160,1018)\\an7}明天继续哦～'],
  ['Dialog', 48.3, 53.4, '{\\pos(160,1018)\\an7}我的鱼干……'],
  ['Title', 54.3, 59.4, '{\\pos(360,860)\\an5}打工人，加油！']
];

function ts(sec) {
  const m = Math.floor(sec / 60);
  const s = Math.floor(sec % 60);
  const cs = Math.floor((sec % 1) * 100);
  const p = n => String(n).padStart(2, '0');
  return `${p(m)}:${p(s)}.${String(cs).padStart(2, '0')}`;
}

const lines = [header];
for (const [style, start, end, text] of events) {
  lines.push(`Dialogue: 0,${ts(start)},${ts(end)},${style},,0,0,0,,${text}`);
}
const content = lines.join('\n');
const out = path.join(__dirname, '..', 'data', 'subtitles', 'overtime.ass');
fs.mkdirSync(path.dirname(out), { recursive: true });
fs.writeFileSync(out, '\ufeff' + content, 'utf8');
console.log('ASS 字幕已生成:', out);