const { execFileSync } = require('child_process');
const path = require('path');
const FF = 'D:/software/huaw-demo/tools/ffmpeg.exe';
const ROOT = path.join(__dirname, '..');
const FONT = 'C:/Windows/Fonts/msyhbd.ttc';

const SUBS = [
  { text: '凌晨1点 · 第99版PPT', start: 0.5, end: 3, x: '(w-text_w)/2', y: 430, size: 54, color: 'white', border: 4 },
  { text: '小猫，方案好了吗？', start: 13.3, end: 17.4, x: 160, y: 1018, size: 34, color: 'black', border: 0 },
  { text: '马上马上！', start: 18.3, end: 23.4, x: 160, y: 1018, size: 34, color: 'black', border: 0 },
  { text: '辛苦了，改完这版就下班。', start: 24.3, end: 29.4, x: 160, y: 1018, size: 34, color: 'black', border: 0 },
  { text: '老板……呜呜……', start: 30.3, end: 35.4, x: 160, y: 1018, size: 34, color: 'black', border: 0 },
  { text: '第 1 页 · 共 99 页', start: 36.6, end: 41.4, x: '(w-text_w)/2', y: 812, size: 34, color: '0x6E3C50', border: 0 },
  { text: '明天继续哦～', start: 42.3, end: 47.4, x: 160, y: 1018, size: 34, color: 'black', border: 0 },
  { text: '我的鱼干……', start: 48.3, end: 53.4, x: 160, y: 1018, size: 34, color: 'black', border: 0 },
  { text: '打工人，加油！', start: 54.3, end: 59.4, x: '(w-text_w)/2', y: 850, size: 62, color: 'white', border: 5 }
];

function drawtext(s) {
  let d = `drawtext=fontfile='${FONT}':text='${s.text}':x=${s.x}:y=${s.y}:fontsize=${s.size}:fontcolor=${s.color}`;
  if (s.border) d += `:borderw=${s.border}:bordercolor=black`;
  d += `:enable='between(t,${s.start},${s.end})'`;
  return d;
}

try {
  const vf = SUBS.map(drawtext).join(',') + ',fps=12,format=yuv420p';
  execFileSync(FF, [
    '-y', '-framerate', '12',
    '-i', path.join(ROOT, 'frames', 'f_%04d.png'),
    '-vf', vf,
    '-map', '0:v', '-r', '12', '-t', '60',
    '-c:v', 'libx264', '-pix_fmt', 'yuv420p', '-crf', '22',
    path.join(ROOT, 'data', 'video_noaudio.mp4')
  ], { stdio: 'pipe' });
  console.log('VIDEO OK');
  execFileSync(FF, [
    '-y',
    '-i', path.join(ROOT, 'data', 'video_noaudio.mp4'),
    '-i', path.join(ROOT, 'data', 'audio.wav'),
    '-c:v', 'copy', '-c:a', 'aac', '-b:a', '128k', '-shortest',
    path.join(ROOT, 'reports', 'overtime-cat.mp4')
  ], { stdio: 'pipe' });
  console.log('MUX OK');
} catch (e) {
  const s = (e.stderr || Buffer.alloc(0)).toString('utf8');
  const bad = s.split('\n').filter(l => /error|Error|failed|Failed|No such/i.test(l)).slice(-6).join(' | ');
  console.log('FAIL:', bad || e.message);
  process.exit(1);
}