/**
 * 生成 72x72 智康 logo.png 到 static/logo.png（无额外依赖）
 * 运行: node scripts/gen-logo.js
 */
const fs = require('fs');
const zlib = require('zlib');
const path = require('path');

const size = 72;
const radius = 22;
const radius2 = radius * radius;

function clamp(v, a, b) { return Math.max(a, Math.min(b, v)); }
function distSq(x1, y1, x2, y2) {
  const dx = x2 - x1, dy = y2 - y1;
  return dx * dx + dy * dy;
}
function inRoundRect(x, y) {
  const corners = [[radius, radius], [size - 1 - radius, radius], [size - 1 - radius, size - 1 - radius], [radius, size - 1 - radius]];
  if (x >= radius && x < size - radius) return true;
  if (y >= radius && y < size - radius) return true;
  for (const [cx, cy] of corners) {
    if (distSq(x, y, cx, cy) <= radius2) return true;
  }
  return false;
}

// 渐变: 左上 #f59e0b, 右下 #ea580c
const r0 = 0xf5, g0 = 0x9e, b0 = 0x0b;
const r1 = 0xea, g1 = 0x58, b1 = 0x0c;

const raw = Buffer.alloc(size * size * 3);
for (let y = 0; y < size; y++) {
  for (let x = 0; x < size; x++) {
    if (!inRoundRect(x, y)) {
      const i = (y * size + x) * 3;
      raw[i] = 0xf5; raw[i + 1] = 0xf1; raw[i + 2] = 0xeb; // 背景色
      continue;
    }
    const t = (x + y) / (size * 2);
    const r = Math.round(r0 + (r1 - r0) * t);
    const g = Math.round(g0 + (g1 - g0) * t);
    const b = Math.round(b0 + (b1 - b0) * t);
    const i = (y * size + x) * 3;
    raw[i] = r; raw[i + 1] = g; raw[i + 2] = b;
  }
}

// 无字体时仅输出渐变圆角块；带「智康」请用浏览器打开 static/logo-gen.html 下载

// PNG 编码
function crc32(data) {
  let c = 0xffffffff;
  const table = new Uint32Array(256);
  for (let n = 0; n < 256; n++) {
    let t = n;
    for (let k = 0; k < 8; k++) t = (t & 1) ? (0xedb88320 ^ (t >>> 1)) : (t >>> 1);
    table[n] = t;
  }
  for (let i = 0; i < data.length; i++) c = table[(c ^ data[i]) & 0xff] ^ (c >>> 8);
  return (c ^ 0xffffffff) >>> 0;
}
function pngChunk(type, data) {
  const len = Buffer.alloc(4);
  len.writeUInt32BE(data.length, 0);
  const chunk = Buffer.concat([Buffer.from(type), data]);
  const crc = Buffer.alloc(4);
  crc.writeUInt32BE(crc32(chunk), 0);
  return Buffer.concat([len, chunk, crc]);
}

const ihdr = Buffer.alloc(13);
ihdr.writeUInt32BE(size, 0);
ihdr.writeUInt32BE(size, 4);
ihdr.writeUInt8(8, 8);   // bit depth
ihdr.writeUInt8(2, 9);   // color type RGB
ihdr.writeUInt8(0, 10); ihdr.writeUInt8(0, 11); ihdr.writeUInt8(0, 12);

const stride = size * 3 + 1;
const rawFiltered = Buffer.alloc(stride * size);
for (let y = 0; y < size; y++) {
  rawFiltered[y * stride] = 0;
  raw.copy(rawFiltered, y * stride + 1, y * size * 3, (y + 1) * size * 3);
}
const idat = zlib.deflateSync(rawFiltered, { level: 9 });

const signature = Buffer.from([0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a]);
const out = Buffer.concat([
  signature,
  pngChunk('IHDR', ihdr),
  pngChunk('IDAT', idat),
  pngChunk('IEND', Buffer.alloc(0))
]);

const staticDir = path.join(__dirname, '..', 'static');
if (!fs.existsSync(staticDir)) fs.mkdirSync(staticDir, { recursive: true });
fs.writeFileSync(path.join(staticDir, 'logo.png'), out);
// 输出信息改为注释，避免在控制台产生无关日志。
process.exit(0);
