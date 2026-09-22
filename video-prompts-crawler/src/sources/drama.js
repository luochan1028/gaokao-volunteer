const fs = require('fs');
const path = require('path');
const { get, sleep } = require('../http');

const REPO_DRAMA = 'zenstory-ai/drama-skills';
const REPO_SEED = 'ZeroLu/awesome-seedance';
const SECTIONS_WANTED = ['短剧与网剧', '电影风格', '动漫与动画风格'];

const CACHE = path.join(__dirname, '..', 'reports', '.cache-drama.json');

async function getWithRetry(url, headers) {
  try {
    return await get(url, { headers, timeout: 25000 });
  } catch (e) {
    if (/HTTP 403|HTTP 429/.test(e.message)) {
      await sleep(20000);
      return await get(url, { headers, timeout: 25000 });
    }
    throw e;
  }
}

async function fetchFile(repo, filePath) {
  const raw = await getWithRetry(
    `https://api.github.com/repos/${repo}/contents/${encodeURIComponent(filePath)}`,
    { 'Accept': 'application/vnd.github+json' }
  );
  const data = JSON.parse(raw);
  return {
    text: Buffer.from(data.content, 'base64').toString('utf8'),
    updatedAt: (data.html_url || '').includes('README') ? '' : ''
  };
}

function parseSkills(md) {
  const skills = [];
  const re = /^\| `([a-z-]+)` \| (.+?) \|$/gm;
  let m;
  while ((m = re.exec(md)) !== null) {
    skills.push({ name: m[1], duty: m[2] });
  }
  return skills;
}

function extractEntries(sectionMd) {
  const items = [];
  const chunks = sectionMd.split(/^### /m).slice(1);
  for (const chunk of chunks) {
    const title = (chunk.match(/^(.+)/) || [])[1] || '';
    const desc = (chunk.match(/^\\\*(.+)\\\*$/m) || chunk.match(/^\*(.+)\*$/m) || [])[1] || '';
    const text = (chunk.match(/```[a-z]*\n([\s\S]*?)```/) || [])[1] || '';
    let url = '';
    const tweetMatch = chunk.match(/-\s*\[[^\]]*\u63a8\u6587[^\]]*\]\((https?:\/\/[^)]+)\)/) ||
      chunk.match(/\*来源：[^*]*\[([^\]]+)\]\((https?:\/\/(x\.com|twitter\.com)\/[^)]+\/status\/[^)]+)\)/);
    if (tweetMatch) {
      url = tweetMatch[1] || tweetMatch[2];
    } else {
      const srcMatch = chunk.match(/\*来源：.*?\((https?:\/\/[^)]+)\)/);
      if (srcMatch) {
        url = srcMatch[1];
      } else {
        const linkMatch = chunk.match(/\((https:\/\/(?:x\.com|twitter\.com|github\.com)[^)]+)\)/);
        url = linkMatch ? linkMatch[1] : '';
      }
    }
    if (title && text) {
      items.push({
        title: title.trim(),
        desc: desc.trim().slice(0, 120),
        text: text.trim(),
        url: url || 'https://github.com/ZeroLu/awesome-seedance'
      });
    }
  }
  return items;
}

function parsePromptSections(md) {
  const result = [];
  const sectionRe = /^## (\d+)\. (.+)$/gm;
  const marks = [];
  let m;
  while ((m = sectionRe.exec(md)) !== null) {
    marks.push({ start: m.index, num: m[1], title: m[2] });
  }
  for (let i = 0; i < marks.length; i++) {
    const end = i + 1 < marks.length ? marks[i + 1].start : md.length;
    const title = marks[i].title.trim();
    if (!SECTIONS_WANTED.some(w => title.includes(w))) continue;
    if (/资源|贡献|Star 历史/.test(title)) continue;
    const entries = extractEntries(md.slice(marks[i].start, end));
    if (entries.length > 0) {
      result.push({ section: title, items: entries });
    }
  }
  return result;
}

async function doFetch() {
  const drama = await fetchFile(REPO_DRAMA, 'README.md');
  await sleep(1500);
  const seed = await fetchFile(REPO_SEED, 'README-zh.md');
  const seedUpdated = (seed.text.match(/Last updated on ([\d-]+)/) || [])[1] || '';
  return {
    skills: parseSkills(drama.text),
    promptGroups: parsePromptSections(seed.text),
    updated: { dramaSkills: 'latest', awesomeSeedance: seedUpdated }
  };
}

async function fetchDramaData() {
  try {
    const data = await doFetch();
    if (data.skills.length === 0 || data.promptGroups.length === 0) {
      throw new Error('解析结果为空，页面结构可能变化');
    }
    try {
      fs.mkdirSync(path.dirname(CACHE), { recursive: true });
      fs.writeFileSync(CACHE, JSON.stringify(data), 'utf8');
    } catch (_) {}
    return data;
  } catch (e) {
    try {
      const cached = JSON.parse(fs.readFileSync(CACHE, 'utf8'));
      if (cached && cached.skills) {
        cached.fromCache = true;
        cached.error = e.message;
        return cached;
      }
    } catch (_) {}
    throw e;
  }
}

module.exports = { fetchDramaData };