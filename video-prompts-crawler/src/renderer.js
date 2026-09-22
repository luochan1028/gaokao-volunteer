const fs = require('fs');
const path = require('path');
const zlib = require('zlib');

const W = 720;
const H = 1280;
const FPS = 12;
const DUR = 60;
const FRAMES = FPS * DUR;

const OUT_DIR = path.join(__dirname, '..', 'frames');

const CRC_TABLE = (() => {
  const t = new Uint32Array(256);
  for (let n = 0; n < 256; n++) {
    let c = n;
    for (let k = 0; k < 8; k++) c = c & 1 ? 0xedb88320 ^ (c >>> 1) : c >>> 1;
    t[n] = c >>> 0;
  }
  return t;
})();

function crc32(buf) {
  let c = 0xffffffff;
  for (let i = 0; i < buf.length; i++) c = CRC_TABLE[(c ^ buf[i]) & 0xff] ^ (c >>> 8);
  return (c ^ 0xffffffff) >>> 0;
}

function chunk(type, data) {
  const len = Buffer.alloc(4);
  len.writeUInt32BE(data.length);
  const typeBuf = Buffer.from(type, 'ascii');
  const crcBuf = Buffer.alloc(4);
  crcBuf.writeUInt32BE(crc32(Buffer.concat([typeBuf, data])));
  return Buffer.concat([len, typeBuf, data, crcBuf]);
}

function encodePng(w, h, rgba) {
  const sig = Buffer.from([0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a]);
  const ihdr = Buffer.alloc(13);
  ihdr.writeUInt32BE(w, 0);
  ihdr.writeUInt32BE(h, 4);
  ihdr[8] = 8;
  ihdr[9] = 6;
  const stride = w * 4;
  const raw = Buffer.alloc((stride + 1) * h);
  for (let y = 0; y < h; y++) {
    raw[y * (stride + 1)] = 0;
    rgba.copy(raw, y * (stride + 1) + 1, y * stride, y * stride + stride);
  }
  return Buffer.concat([
    sig,
    chunk('IHDR', ihdr),
    chunk('IDAT', zlib.deflateSync(raw, { level: 6 })),
    chunk('IEND', Buffer.alloc(0))
  ]);
}

function Canvas() {
  const data = new Uint8Array(W * H * 4);
  const blend = (i, r, g, b, a) => {
    const d = a / 255;
    const inv = 1 - d;
    data[i] = Math.round(r * d + data[i] * inv);
    data[i + 1] = Math.round(g * d + data[i + 1] * inv);
    data[i + 2] = Math.round(b * d + data[i + 2] * inv);
    data[i + 3] = Math.min(255, Math.round(a + data[i + 3] * inv));
  };
  const setPx = (x, y, r, g, b, a) => {
    if (x < 0 || y < 0 || x >= W || y >= H || a <= 0) return;
    const i = (y * W + x) << 2;
    if (a >= 255) {
      data[i] = r; data[i + 1] = g; data[i + 2] = b; data[i + 3] = 255;
    } else {
      blend(i, r, g, b, a);
    }
  };
  return {
    data,
    px: setPx,
    rect(x, y, w, h, c, a = 255) {
      for (let yy = Math.max(0, y); yy < Math.min(H, y + h); yy++) {
        for (let xx = Math.max(0, x); xx < Math.min(W, x + w); xx++) {
          setPx(xx, yy, c[0], c[1], c[2], a);
        }
      }
    },
    circle(x, y, r, c, a = 255) {
      const r2 = r * r;
      for (let yy = Math.max(0, Math.floor(y - r)); yy <= Math.min(H - 1, Math.ceil(y + r)); yy++) {
        for (let xx = Math.max(0, Math.floor(x - r)); xx <= Math.min(W - 1, Math.ceil(x + r)); xx++) {
          const dx = xx - x, dy = yy - y;
          if (dx * dx + dy * dy <= r2) setPx(xx, yy, c[0], c[1], c[2], a);
        }
      }
    },
    ellipse(x, y, rx, ry, c, a = 255) {
      for (let yy = Math.max(0, Math.floor(y - ry)); yy <= Math.min(H - 1, Math.ceil(y + ry)); yy++) {
        for (let xx = Math.max(0, Math.floor(x - rx)); xx <= Math.min(W - 1, Math.ceil(x + rx)); xx++) {
          const dx = (xx - x) / rx, dy = (yy - y) / ry;
          if (dx * dx + dy * dy <= 1) setPx(xx, yy, c[0], c[1], c[2], a);
        }
      }
    },
    tri(ax, ay, bx, by, cx, cy, col, a = 255) {
      const minX = Math.max(0, Math.floor(Math.min(ax, bx, cx)));
      const maxX = Math.min(W - 1, Math.ceil(Math.max(ax, bx, cx)));
      const minY = Math.max(0, Math.floor(Math.min(ay, by, cy)));
      const maxY = Math.min(H - 1, Math.ceil(Math.max(ay, by, cy)));
      const area = (bx - ax) * (cy - ay) - (cx - ax) * (by - ay);
      if (area === 0) return;
      for (let yy = minY; yy <= maxY; yy++) {
        for (let xx = minX; xx <= maxX; xx++) {
          const w0 = ((bx - xx) * (cy - yy) - (cx - xx) * (by - yy)) / area;
          const w1 = ((cx - xx) * (ay - yy) - (ax - xx) * (cy - yy)) / area;
          const w2 = 1 - w0 - w1;
          if (w0 >= 0 && w1 >= 0 && w2 >= 0) this.px(xx, yy, col[0], col[1], col[2], a);
        }
      }
    },
    line(x0, y0, x1, y1, wd, c, a = 255) {
      const dx = x1 - x0, dy = y1 - y0;
      const len = Math.hypot(dx, dy) || 1;
      const steps = Math.ceil(len);
      for (let i = 0; i <= steps; i++) {
        const t = i / steps;
        const px = x0 + dx * t, py = y0 + dy * t;
        this.circle(px, py, wd / 2, c, a);
      }
    },
    textPlaceholder(x, y, w, h) {
      this.rect(x, y, w, h, [0, 0, 0], 255);
    },
    clear() { data.fill(0); },
    toBuffer() { return Buffer.from(data); }
  };
}

const P_ORANGE = [245, 158, 76];
const P_ORANGE_D = [214, 120, 44];
const P_BLUE = [59, 130, 246];
const P_BLUE_D = [37, 99, 235];
const P_CREAM = [253, 242, 224];
const P_BROWN = [124, 84, 45];
const P_DARK = [17, 24, 39];
const P_WALL = [30, 41, 59];
const P_WALL_D = [21, 30, 48];
const P_FLOOR = [17, 24, 39];
const P_LAMP = [234, 213, 160];
const P_TREE = [255, 178, 207];
const P_GRAY = [148, 152, 161];
const P_GRAY_D = [105, 109, 118];
const P_RED = [239, 68, 68];

function drawBackground(c, t, lampOn) {

  c.rect(0, 0, W, H, P_DARK);
  c.rect(0, 0, W, 320, P_WALL);
  c.rect(0, 320, W, 240, P_WALL_D);
  const rows = 12;
  for (let i = 0; i < rows; i++) {
    const y = 560 + i * ((H - 560) / rows);
    const shade = 14 + i * 4;
    c.rect(0, y, W, (H - 560) / rows + 1, [shade, shade + 8, shade + 14]);
  }
  c.line(0, 560, W, 560, 4, [10, 12, 20], 255);
  const winX = 500, winY = 70, winW = 180, winH = 230;
  c.rect(winX - 8, winY - 8, winW + 16, winH + 16, [20, 26, 38]);
  c.rect(winX, winY, winW, winH, [8, 14, 30]);
  c.circle(winX + 130, winY + 60, 26, [243, 224, 164]);
  c.line(winX + 130, winY + 60, winX + 130, winY + 46, 6, [243, 224, 164]);
  c.line(winX + 130, winY + 60, winX + 143, winY + 68, 6, [243, 224, 164]);
  c.rect(winX, winY + 110, winW, winH - 110, [12, 20, 36]);
  c.rect(winX + 30, winY + 110, 44, 70, [8, 12, 24]);
  c.rect(winX + 100, winY + 110, 44, 70, [8, 12, 24]);
  c.rect(winX, winY, 6, winH, [30, 36, 50]);
  c.rect(winX + winW - 6, winY, 6, winH, [30, 36, 50]);
  c.rect(winX, winY + winH - 6, winW, 6, [30, 36, 50]);
  if (lampOn) {
    const glow = [255, 240, 190];
    c.rect(280, 30, 160, 12, [220, 220, 230]);
    c.rect(322, 42, 76, 20, [255, 246, 200], 230);
    c.ellipse(360, 120, 260, 200, glow, 12);
  } else {
    c.rect(280, 30, 160, 12, [90, 96, 110]);
    c.rect(322, 42, 76, 20, [120, 124, 136]);
  }
  const deskX = 120, deskY = 810, deskW = 300, deskH = 34;
  c.rect(deskX + 14, deskY, 26, 150, [40, 42, 54]);
  c.rect(deskX + deskW - 40, deskY, 26, 150, [40, 42, 54]);
  c.rect(deskX, deskY, deskW, deskH, [86, 90, 104]);
  c.rect(deskX, deskY + 8, deskW, 10, [120, 124, 136]);
  c.rect(200, 640, 120, 170, [26, 32, 44]);
  c.rect(206, 650, 60, 70, [36, 42, 56]);
  c.rect(206, 730, 60, 20, [40, 44, 58]);
  c.rect(272, 650, 40, 88, [36, 42, 56]);
  c.rect(196, 810, 128, 12, [56, 60, 72]);
  c.rect(150, 560, 90, 240, [60, 40, 90], 220);
  c.rect(460, 620, 90, 160, [60, 40, 90], 220);
  c.rect(512, 640, 30, 46, [40, 24, 60], 230);
  const pot = [30, 32, 44];
  const cupX = 310, cupY = 760;
  c.rect(cupX, cupY, 36, 30, pot);
  c.circle(cupX + 18, cupY + 32, 10, pot);

}

function drawCat(c, opts) {
  const { x, y, scale, sleeping, shock, cry, stone, ghost, cheer, typingArm, headTilt, sweat, blink } = opts;
  const s = scale;
  const gray = stone;
  const col = gray ? P_GRAY : P_ORANGE;
  const colD = gray ? P_GRAY_D : P_ORANGE_D;
  const skin = gray ? [170, 174, 182] : P_CREAM;
  const alpha = ghost ? 110 : 255;
  const ox = x, oy = y;
  if (cheer) {
    c.circle(ox - 40 * s, oy - 130 * s, 16 * s, col, alpha);
    c.circle(ox + 40 * s, oy - 130 * s, 16 * s, col, alpha);
  }
  c.ellipse(ox, oy - 60 * s, 74 * s, 96 * s, col, alpha);
  c.ellipse(ox, oy + 4 * s, 78 * s, 90 * s, P_BLUE, alpha);
  c.ellipse(ox, oy + 30 * s, 60 * s, 72 * s, P_BLUE_D, alpha);
  c.ellipse(ox - 44 * s, oy - 118 * s, 36 * s, 58 * s, col, alpha);
  c.ellipse(ox + 44 * s, oy - 118 * s, 36 * s, 58 * s, col, alpha);

  const earL = [
    [ox - 52 * s, oy - 150 * s],
    [ox - 100 * s, oy - 208 * s],
    [ox - 20 * s, oy - 172 * s]
  ];
  const earR = [
    [ox + 52 * s, oy - 150 * s],
    [ox + 100 * s, oy - 208 * s],
    [ox + 20 * s, oy - 172 * s]
  ];
  c.tri(earL[0][0], earL[0][1], earL[1][0], earL[1][1], earL[2][0], earL[2][1], col, alpha);
  c.tri(earR[0][0], earR[0][1], earR[1][0], earR[1][1], earR[2][0], earR[2][1], col, alpha);
  c.tri(earL[0][0] + 8 * s, earL[0][1] + 8 * s, earL[1][0] + 6 * s, earL[1][1] + 6 * s, earL[2][0] + 4 * s, earL[2][1] + 4 * s, [248, 200, 170]);
  c.circle(ox, oy - 150 * s, 62 * s, col, alpha);
  c.circle(ox, oy - 150 * s, 62 * s, col, alpha);
  c.ellipse(ox - 30 * s, oy - 152 * s, 10 * s, 6 * s, [22, 22, 26], alpha);
  c.ellipse(ox + 30 * s, oy - 152 * s, 10 * s, 6 * s, [22, 22, 26], alpha);
  if (!sleeping) {
    if (blink) {
      c.line(ox - 30 * s, oy - 152 * s - 2, ox - 22 * s, oy - 152 * s + 2, 4 * s, [10, 10, 14], alpha);
      c.line(ox + 30 * s, oy - 152 * s - 2, ox + 22 * s, oy - 152 * s + 2, 4 * s, [10, 10, 14], alpha);
    } else {
      c.circle(ox - 30 * s, oy - 152 * s, 7 * s, [250, 250, 255], alpha);
      c.circle(ox + 30 * s, oy - 152 * s, 7 * s, [250, 250, 255], alpha);
      const px = shock ? 6 : 2;
      c.circle(ox - 30 * s + px, oy - 152 * s - 1 * s, 3.4 * s, [16, 16, 20], alpha);
      c.circle(ox + 30 * s + px, oy - 152 * s - 1 * s, 3.4 * s, [16, 16, 20], alpha);
    }
    c.ellipse(ox - 22 * s, oy - 150 * s, 7 * s, 7 * s, [14, 12, 20], 60);
    c.ellipse(ox + 22 * s, oy - 150 * s, 7 * s, 7 * s, [14, 12, 20], 60);
  } else {
    c.line(ox - 30 * s, oy - 148 * s, ox - 22 * s, oy - 146 * s, 4 * s, [10, 10, 14], alpha);
    c.line(ox - 22 * s, oy - 146 * s, ox - 14 * s, oy - 148 * s, 4 * s, [10, 10, 14], alpha);
    c.line(ox + 22 * s, oy - 146 * s, ox + 30 * s, oy - 148 * s, 4 * s, [10, 10, 14], alpha);
    c.line(ox + 14 * s, oy - 148 * s, ox + 22 * s, oy - 146 * s, 4 * s, [10, 10, 14], alpha);
  }
  c.ellipse(ox + 10 * s, oy - 140 * s, 4 * s, 2.6 * s, [28, 20, 44], alpha);
  c.line(ox - 34 * s, oy - 138 * s, ox - 48 * s, oy - 144 * s, 2.4 * s, [60, 56, 66], alpha);
  c.line(ox - 34 * s, oy - 138 * s, ox - 50 * s, oy - 136 * s, 2.4 * s, [60, 56, 66], alpha);
  c.line(ox + 34 * s, oy - 138 * s, ox + 48 * s, oy - 144 * s, 2.4 * s, [60, 56, 66], alpha);
  c.line(ox + 34 * s, oy - 138 * s, ox + 50 * s, oy - 136 * s, 2.4 * s, [60, 56, 66], alpha);
  if (stone) {
    c.line(ox - 40 * s, oy - 190 * s, ox + 10 * s, oy - 120 * s, 2.6 * s, [60, 62, 72], 255);
    c.line(ox + 8 * s, oy - 118 * s, ox - 24 * s, oy - 70 * s, 2.2 * s, [60, 62, 72], 255);
    c.line(ox + 30 * s, oy - 190 * s, ox + 46 * s, oy - 150 * s, 1.8 * s, [60, 62, 72], 255);
  }
  if (typingArm !== undefined) {
    c.ellipse(ox - 60 * s, oy - 8 * s + typingArm, 13 * s, 9 * s, col, alpha);
  }
  if (cheer) {
    c.circle(ox - 34 * s, oy + 30 * s, 12 * s, [250, 250, 255], alpha);
    c.circle(ox + 34 * s, oy + 30 * s, 12 * s, [250, 250, 255], alpha);
  }
  if (cry) {
    c.circle(ox - 42 * s, oy - 130 * s, 11 * s, [125, 211, 252], 230);
    c.circle(ox + 42 * s, oy - 130 * s, 11 * s, [125, 211, 252], 230);
  }
  if (sweat) {
    c.tri(ox + 70 * s, oy - 160 * s, ox + 82 * s, oy - 160 * s, ox + 76 * s, oy - 148 * s, [125, 211, 252], 240);
  }
  if (ghost) {
    c.ellipse(ox, oy + 70 * s, 40 * s, 12 * s, [180, 230, 255], 90);
  }
}

function drawCorgi(c, opts) {
  const { x, y, scale, happy, deliver, armX, armY } = opts;
  const s = scale;
  const body = [212, 178, 110];
  const bodyD = [178, 142, 78];
  const vest = P_BROWN;
  const vestD = [96, 62, 30];
  c.ellipse(x, y - 40 * s, 66 * s, 76 * s, body);
  c.ellipse(x + 50 * s, y - 96 * s, 8 * s, 12 * s, body);
  c.ellipse(x + 52 * s, y - 94 * s, 6 * s, 8 * s, [248, 220, 180]);
  c.ellipse(x - 40 * s, y - 30 * s, 14 * s, 8 * s, bodyD);
  c.ellipse(x + 38 * s, y - 30 * s, 14 * s, 8 * s, bodyD);
  c.rect(x - 22 * s, y - 78 * s, 44 * s, 80 * s, vest);
  c.rect(x - 22 * s, y - 78 * s, 44 * s, 8 * s, vestD);
  c.tri(x, y - 84 * s, x - 8 * s, y - 58 * s, x + 8 * s, y - 58 * s, P_RED);
  c.ellipse(x, y - 20 * s, 56 * s, 44 * s, body);
  c.ellipse(x - 32 * s, y - 56 * s, 26 * s, 30 * s, body);
  c.ellipse(x + 32 * s, y - 56 * s, 26 * s, 30 * s, body);
  c.ellipse(x - 32 * s, y - 52 * s, 16 * s, 18 * s, [248, 220, 180]);
  c.ellipse(x + 32 * s, y - 52 * s, 16 * s, 18 * s, [248, 220, 180]);
  c.circle(x - 32 * s, y - 70 * s, 5 * s, [24, 22, 28]);
  c.circle(x + 32 * s, y - 70 * s, 5 * s, [24, 22, 28]);
  c.circle(x, y - 60 * s, 6 * s, [24, 22, 28]);
  c.tri(x - 8 * s, y - 54 * s, x + 8 * s, y - 54 * s, x, y - 46 * s, [200, 60, 60]);
  c.line(x + 14 * s, y - 46 * s, x + 30 * s, y - 44 * s, 2 * s, [24, 22, 28]);
  c.line(x - 14 * s, y - 46 * s, x - 30 * s, y - 44 * s, 2 * s, [24, 22, 28]);
  if (deliver) {
    c.line(x + 30 * s, y - 140 * s, armX, armY, 12 * s, body, 255);
    c.circle(armX, armY, 15 * s, body, 255);
    c.rect(armX - 20 * s, armY - 6 * s, 30 * s, 12 * s, [216, 180, 116]);
    c.rect(armX - 12 * s, armY - 6 * s, 14 * s, 12 * s, [250, 226, 170]);
  }
  if (happy) {
    const sway = Math.sin(Date.now() % 500 / 50) * 6;
    c.ellipse(x - 90 * s, y - 20 * s + sway, 10 * s, 16 * s, body);
  }
}

function drawDialogue(c, x, y, w, h, pointerDir) {
  c.rect(x, y, w, h, [252, 250, 244]);
  if (pointerDir === 'up') {
    c.tri(x + w / 2 - 20, y, x + w / 2 + 20, y, x + w / 2, y - 26, [252, 250, 244]);
  } else {
    c.tri(x + 60, y + h, x + 60 + 40, y + h, x + 60 + 20, y + h + 30, [252, 250, 244]);
  }
}

function drawScreen(c, x, y, w, h, text) {
  c.rect(x - 6, y - 6, w + 12, h + 12, [14, 18, 30]);
  c.rect(x, y, w, h, [206, 226, 250]);
  c.rect(x + 6, y + 6, w - 12, h - 12, [226, 240, 255]);
  c.rect(x + 10, y + h - 30, (w - 20) * (text / 100), 12, [95, 140, 220]);
  c.rect(x + 10, y + 14, w - 20, 6, [150, 180, 230]);
  c.rect(x + 10, y + 26, (w - 20) * 0.82, 6, [170, 195, 240]);
  c.rect(x + 10, y + 38, (w - 20) * 0.6, 6, [170, 195, 240]);
}

function sakura(c, t) {
  const petals = [];
  for (let i = 0; i < 10; i++) {
    petals.push({
      x0: 60 + ((i * 97) % 600),
      y0: 380 + ((i * 53) % 500),
      spd: 90 + ((i * 31) % 70),
      ph: (i * 2.1) % 6.28
    });
  }
  for (const p of petals) {
    const cycle = (t - 26) / 10;
    let py = p.y0 + Math.max(0, cycle * p.spd);
    if (py > 1280) py = 380 + ((py - 1280) % 300);
    const px = p.x0 + Math.sin(p.ph + t * 2.2) * 24;
    c.ellipse(px, py, 6, 4, [255, 189, 214], 235);
  }
}

function frame(t) {
  const c = Canvas();
  const lampOn = t < 18 ? false : true;
  c.clear();
  drawBackground(c, t, lampOn);
  const deskX = 120;
  const shock = t >= 18 && t < 24;
  const sleepZone = t >= 3 && t < 8;
  const catStone = t >= 42 && t < 48;
  const ghost = t >= 48 && t < 54;
  const cheerZone = t >= 54;
  const typing = !sleepZone && !shock && !catStone && !ghost && t < 42;
  const bob = Math.sin(t * 2) * 3;
  const headTilt = sleepZone ? Math.sin(t * 2.2) * 4 : 0;
  const typingArm = typing ? Math.abs(Math.sin(t * 9)) * 6 : 0;
  let catScale = 1;
  let catY = 700;
  let catX = 250;
  if (shock) {
    const jump = Math.sin(t * 20) * 10;
    catScale = 1.22 + Math.sin(t * 24) * 0.05;
    catX += Math.sin(t * 26) * 7;
    catY = 660 + jump;
  }
  if (catStone) {
    drawCat(c, { x: catX, y: catY, scale: 1, sleeping: false, shock: false, cry: false, stone: true, ghost: false, cheer: false, typingArm: 0, headTilt: 0, sweat: false, blink: false });
  } else if (ghost) {
    const gy = -Math.min(130, Math.max(0, (t - 48) * 28));
    drawCat(c, { x: catX, y: catY, scale: 1, sleeping: false, shock: false, cry: false, stone: false, ghost: true, cheer: false, typingArm: 0, headTilt: 0, sweat: false, blink: false });
    drawCat(c, { x: catX - 40, y: catY - 200 + gy, scale: 0.92, sleeping: false, shock: false, cry: true, stone: false, ghost: true, cheer: false, typingArm: 0, headTilt: 0, sweat: false, blink: false });
    c.circle(catX - 40, catY - 240 + gy, 10, [180, 230, 255], 200);
    c.circle(catX - 25, catY - 250 + gy, 8, [180, 230, 255], 200);
    c.circle(catX - 60, catY - 250 + gy, 8, [180, 230, 255], 200);
  } else {
    drawCat(c, {
      x: catX + bob * 0.2, y: catY, scale: catScale,
      sleeping: sleepZone, shock, cry: t >= 30 && t < 36,
      stone: false, ghost: false, cheer: cheerZone,
      typingArm, headTilt, sweat: shock, blink: t % 4 < 0.15
    });
  }
  if (!catStone && !ghost) {
    const kbY = 810;
    c.rect(196, kbY - 4, 128, 14, [60, 64, 80]);
    if (shock) {
      for (let i = 0; i < 6; i++) {
        c.circle(215 + i * 20, 806 + Math.sin(t * 30 + i) * 3, 4, [255, 214, 90]);
        c.tri(215 + i * 20, 806, 225 + i * 20, 806, 220 + i * 20, 796, [255, 214, 90]);
      }
    } else if (typing) {
      c.line(215, 808, 215, 800, 3, [120, 126, 140]);
      c.line(245, 808, 245, 800, 3, [120, 126, 140]);
    }
  }
  const corgiX = 480, corgiY = 830;
  if (t >= 13) {
    drawCorgi(c, { x: corgiX, y: corgiY, scale: 1.06, happy: t >= 42, deliver: t >= 24 && t < 30, armX: 250, armY: 720 });
  }
  drawScreen(c, 206, 660, 108, 130, t < 42 ? 3 : 1);
  if (t >= 36 && t < 42) {
    const zx = 180, zy = 560, zw = 360, zh = 300;
    c.rect(zx - 10, zy - 10, zw + 20, zh + 20, [255, 210, 60]);
    c.rect(zx, zy, zw, zh, [240, 250, 255]);
    c.rect(zx + 8, zy + 8, zw - 16, zh - 16, [250, 253, 255]);
    for (let i = 0; i < 6; i++) c.rect(zx + 12, zy + 22 + i * 16, (zw - 24) * (0.9 - i * 0.12), 8, [150, 180, 230]);
    c.rect(zx + 12, zy + zh - 56, zw - 24, 36, [110, 60, 80]);
  }
  if (t >= 42) {
    c.circle(260, 725, 30, [250, 240, 230], 200);
    c.circle(260, 725, 18, [230, 120, 110], 200);
    c.circle(260, 725, 8, [120, 60, 60], 220);
    c.rect(248, 738, 24, 6, [220, 100, 100], 220);
    c.rect(252, 748, 16, 5, [200, 90, 90], 220);
  }
  if (t >= 30) sakura(c, t);
  const dlg = t >= 13 && t < 54;
  if (dlg) {
    let show = false;
    let textIdx = 0;
    const cur = currentDialogue(t);
    if (cur) {
      show = true;
      textIdx = cur;
    }
    if (show) {
      drawDialogue(c, 130, 960, 460, 150, 'up');
      c.rect(150, 978, 420, 114, [245, 242, 236]);
    }
    void textIdx;
  }
  return c.toBuffer();
}

function currentDialogue(t) {
  if (t >= 13 && t < 18) return 0;
  if (t >= 18 && t < 24) return 1;
  if (t >= 24 && t < 30) return 2;
  if (t >= 30 && t < 36) return 3;
  if (t >= 42 && t < 48) return 4;
  if (t >= 48 && t < 54) return 5;
  return null;
}

module.exports = { frame, W, H, FPS, FRAMES, DUR, currentDialogue, OUT_DIR, encodePng };

if (require.main === module) {
  fs.mkdirSync(OUT_DIR, { recursive: true });
  const start = Date.now();
  for (let f = 0; f < FRAMES; f++) {
    const t = f / FPS;
    const png = encodePng(W, H, frame(t));
    fs.writeFileSync(path.join(OUT_DIR, `f_${String(f).padStart(4, '0')}.png`), png);
    if (f % 120 === 0) console.log(`渲染进度 ${f}/${FRAMES}`);
  }
  console.log(`完成 720 帧，耗时 ${((Date.now() - start) / 1000).toFixed(1)}s`);
}