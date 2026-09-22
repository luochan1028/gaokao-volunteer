const fs = require('fs');
const path = require('path');
const { execFileSync } = require('child_process');

const FFMPEG = path.join('D:', 'software', 'huaw-demo', 'tools', 'ffmpeg.exe');
const ROOT = path.join(__dirname, '..');
const DATA = path.join(ROOT, 'data');
const SUBTITLES = path.join(DATA, 'subtitles');
const FONT = 'C:/Windows/Fonts/msyhbd.ttc';

const SUBS = [
  { name: 'sub_title1.txt', text: '凌晨1点 · 第99版PPT', start: 0.5, end: 3, x: '(w-text_w)/2', y: 420, size: 56, color: 'white', border: 4 },
  { name: 'sub_corgi1.txt', text: '小猫，方案好了吗？', start: 13.2, end: 17.5, x: 160, y: 1015, size: 32, color: 'black', border: 0 },
  { name: 'sub_cat1.txt', text: '马上马上！', start: 18.2, end: 23.5, x: 160, y: 1015, size: 32, color: 'black', border: 0 },
  { name: 'sub_corgi2.txt', text: '辛苦了，改完这版就下班。', start: 24.2, end: 29.5, x: 160, y: 1015, size: 32, color: 'black', border: 0 },
  { name: 'sub_cat2.txt', text: '老板……呜呜……', start: 30.2, end: 35.5, x: 160, y: 1015, size: 32, color: 'black', border: 0 },
  { name: 'sub_screen.txt', text: '第 1 页 · 共 99 页', start: 36.5, end: 41.5, x: '(w-text_w)/2', y: 812, size: 34, color: '0x6e3c50', border: 0 },
  { name: 'sub_corgi3.txt', text: '明天继续哦～', start: 42.2, end: 47.5, x: 160, y: 1015, size: 32, color: 'black', border: 0 },
  { name: 'sub_cat3.txt', text: '我的鱼干……', start: 48.2, end: 53.5, x: 160, y: 1015, size: 32, color: 'black', border: 0 },
  { name: 'sub_end.txt', text: '打工人，加油！', start: 54.2, end: 59.5, x: '(w-text_w)/2', y: 850, size: 64, color: 'white', border: 5 }
];

function bom(s) {
  return '\ufeff' + s;
}

function writeSubs() {
  fs.mkdirSync(SUBTITLES, { recursive: true });
  for (const s of SUBS) {
    fs.writeFileSync(path.join(SUBTITLES, s.name), bom(s.text) + '\n', 'utf8');
  }
}

function buildFilter() {
  const parts = ['[0:v]'];
  for (const s of SUBS) {
    const file = path.join(SUBTITLES, s.name).replace(/\\/g, '/');
    let d = `drawtext=fontfile='${FONT}':textfile='${file}':x=${s.x}:y=${s.y}:fontsize=${s.size}:fontcolor=${s.color}`;
    if (s.border) d += `:borderw=${s.border}:bordercolor=black@0.85`;
    d += `:enable='between(t,${s.start},${s.end})'`;
    parts.push(d);
  }
  parts.push('fps=12,format=yuv420p[v]');
  fs.writeFileSync(path.join(DATA, 'video.filter'), parts.join(',\n'), 'utf8');
}

function ff(args) {
  execFileSync(FFMPEG, args, { stdio: 'inherit' });
}

function audio() {
  ff(['-y', '-f', 'lavfi', '-i', "aevalsrc='sin(2*PI*75*t)*exp(-5*t)':d=1.5", '-c:a', 'pcm_s16le', path.join(DATA, 'thud.wav')]);
  ff(['-y', '-f', 'lavfi', '-i', "aevalsrc='sin(2*PI*990*t)*exp(-12*t)':d=0.4", '-c:a', 'pcm_s16le', path.join(DATA, 'ding.wav')]);
  ff(['-y', '-f', 'lavfi', '-i', "aevalsrc='sin(2*PI*880*t)*exp(-4*t)+0.5*sin(2*PI*1320*t)*exp(-5*t)':d=1.4", '-c:a', 'pcm_s16le', path.join(DATA, 'chime.wav')]);
  ff([
    '-y',
    '-i', path.join(DATA, 'thud.wav'),
    '-i', path.join(DATA, 'ding.wav'),
    '-i', path.join(DATA, 'chime.wav'),
    '-filter_complex',
    "[0:a]adelay=36000|36000[t];[1:a]adelay=8600|8600[d];[2:a]adelay=55000|55000[e];[t][d][e]amix=inputs=3:normalize=0,apad=whole_dur=60[a]",
    '-map', '[a]', '-t', '60', '-c:a', 'pcm_s16le',
    path.join(DATA, 'audio.wav')
  ]);
}

function video() {
  const out = path.join(ROOT, 'reports', 'overtime-cat.mp4');
  ff([
    '-y',
    '-framerate', '12',
    '-i', path.join(ROOT, 'frames', 'f_%04d.png'),
    '-filter_complex_script', path.join(DATA, 'video.filter'),
    '-map', '[v]', '-r', '12', '-t', '60',
    '-c:v', 'libx264', '-pix_fmt', 'yuv420p', '-crf', '22',
    path.join(DATA, 'video_noaudio.mp4')
  ]);
  ff([
    '-y',
    '-i', path.join(DATA, 'video_noaudio.mp4'),
    '-i', path.join(DATA, 'audio.wav'),
    '-c:v', 'copy', '-c:a', 'aac', '-b:a', '128k', '-shortest',
    out
  ]);
  console.log(`成片已生成: ${out}`);
}

function main() {
  writeSubs();
  buildFilter();
  if (process.argv[2] === '--audio-only') {
    audio();
  } else {
    audio();
    video();
  }
}

main();