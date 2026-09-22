'use strict';

const TRENDING_URL = 'https://github.com/trending?since=daily';
const API_BASE = 'https://api.github.com';
const UA = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126 Safari/537.36';

function decodeEntities(s) {
  if (!s) return '';
  return s
    .replace(/&quot;/g, '"')
    .replace(/&#39;/g, "'")
    .replace(/&lt;/g, '<')
    .replace(/&gt;/g, '>')
    .replace(/&amp;/g, '&')
    .replace(/&#x([0-9a-fA-F]+);/g, (_, h) => String.fromCodePoint(parseInt(h, 16)))
    .replace(/\s+/g, ' ')
    .trim();
}

function parseNum(str) {
  if (!str) return 0;
  const t = str.replace(/[,\s]/g, '');
  const m = t.match(/^([\d.]+)([kKmM]?)$/);
  if (!m) return 0;
  let v = parseFloat(m[1]);
  if (m[2].toLowerCase() === 'k') v *= 1000;
  if (m[2].toLowerCase() === 'm') v *= 1000000;
  return Math.round(v);
}

async function myFetch(url, opts = {}, timeoutMs = 30000, attempts = 3) {
  let lastErr;
  for (let i = 0; i < attempts; i++) {
    const ctrl = new AbortController();
    const timer = setTimeout(() => ctrl.abort(), timeoutMs);
    try {
      const res = await fetch(url, { ...opts, signal: ctrl.signal });
      if (!res.ok) throw new Error(`HTTP ${res.status} ${res.statusText} @ ${url}`);
      return await res.text();
    } catch (e) {
      lastErr = e;
      await new Promise((r) => setTimeout(r, 800 * (i + 1)));
    } finally {
      clearTimeout(timer);
    }
  }
  throw lastErr;
}

async function fetchTrendingPage() {
  const html = await myFetch(TRENDING_URL);
  if (!html) throw new Error('GitHub Trending 页面返回空内容');
  const blocks = html.split(/<article class="Box-row">/).slice(1);
  const repos = [];
  for (const b of blocks) {
    const end = b.indexOf('</article>');
    const item = end > 0 ? b.slice(0, end) : b;

    const nameMatch = item.match(/<h2[^>]*>[\s\S]*?href="\/([^"?#]+\/[^"?#]+)"/);
    if (!nameMatch) continue;
    const fullName = nameMatch[1].replace(/·/g, '·');
    const parts = fullName.split('/').filter(Boolean);
    if (parts.length !== 2) continue;
    const [owner, repo] = parts;

    const langMatch = item.match(/itemprop="programmingLanguage">([^<]+)</);
    const descMatch = item.match(/<p class="col-9[^"]*"[^>]*>([\s\S]*?)<\/p>/);
    const starMatch = item.match(/\/stargazers"[\s\S]*?<\/svg>\s*([\d,]+[kKmM]?)/);
    const forkMatch = item.match(/\/forks"[\s\S]*?<\/svg>\s*([\d,]+[kKmM]?)/);
    const todayMatch = item.match(/([\d,]+(?:\.\d)?[kKmM]?)\s+stars?\s+today/);

    repos.push({
      rank: repos.length + 1,
      owner,
      repo,
      fullName,
      url: `https://github.com/${fullName}`,
      language: langMatch ? decodeEntities(langMatch[1]) : '',
      description: descMatch ? decodeEntities(descMatch[1]) : '',
      stars: starMatch ? parseNum(starMatch[1]) : 0,
      forks: forkMatch ? parseNum(forkMatch[1]) : 0,
      todayStars: todayMatch ? parseNum(todayMatch[1]) : 0
    });
  }
  if (repos.length === 0) throw new Error('Trending 页面解析失败：未提取到任何项目');
  return repos;
}

async function fetchSearchTop(topN, timeoutMs = 30000) {
  const since = new Date(Date.now() - 7 * 86400000).toISOString().slice(0, 10);
  const url = `${API_BASE}/search/repositories?q=created:>${since}&sort=stars&order=desc&per_page=${topN}`;
  const text = await myFetch(url, { headers: { 'User-Agent': UA, Accept: 'application/vnd.github+json' } }, timeoutMs);
  const json = JSON.parse(text);
  if (!json.items || json.items.length === 0) throw new Error('Search API 未返回任何项目');
  return json.items.map((it, idx) => ({
    rank: idx + 1,
    owner: it.owner ? it.owner.login : '',
    repo: it.name,
    fullName: it.full_name,
    url: it.html_url,
    language: it.language || '',
    description: it.description || '',
    stars: it.stargazers_count || 0,
    forks: it.forks_count || 0,
    todayStars: 0,
    watchers: it.subscribers_count || 0,
    updatedAt: it.updated_at ? it.updated_at.slice(0, 10) : '',
    license: it.license ? it.license.spdx_id : '',
    topics: it.topics || []
  }));
}

async function fetchRepoDetail(fullName, timeoutMs = 30000) {
  const url = `${API_BASE}/repos/${fullName}`;
  const text = await myFetch(url, { headers: { 'User-Agent': UA, Accept: 'application/vnd.github+json' } }, timeoutMs);
  return JSON.parse(text);
}

// 领域订阅：限定近 pushedDays 天内有更新、star 数下限之上的项目，按 star 总数排序
function buildCategoryQuery(cat, topic, nowMs = Date.now()) {
  const minStars = cat.minStars || 500;
  const pushedDays = cat.pushedDays || 90;
  const since = new Date(nowMs - pushedDays * 86400000).toISOString().slice(0, 10);
  return `topic:${topic} stars:>${minStars} pushed:>${since}`;
}

function mapSearchItem(it, idx) {
  return {
    rank: idx + 1,
    owner: it.owner ? it.owner.login : '',
    repo: it.name,
    fullName: it.full_name,
    url: it.html_url,
    language: it.language || '',
    description: it.description || '',
    stars: it.stargazers_count || 0,
    forks: it.forks_count || 0,
    todayStars: 0,
    watchers: 0,
    updatedAt: it.updated_at ? it.updated_at.slice(0, 10) : '',
    license: it.license && it.license.spdx_id ? it.license.spdx_id : '',
    topics: Array.isArray(it.topics) ? it.topics : []
  };
}

// GitHub 搜索的 OR 不支持在 topic 限定符之间使用，因此每个 topic 独立搜索后合并去重
// 直接使用 Search API 返回字段（含协议/更新时间/标签），不再逐个调详情接口，规避匿名配额限制
async function fetchCategoryTop(cat, topN, timeoutMs = 30000) {
  const merged = new Map();
  const perPage = Math.min(Math.max(topN * 2, 10), 30);
  for (const topic of cat.topics) {
    const url = `${API_BASE}/search/repositories?q=${encodeURIComponent(buildCategoryQuery(cat, topic))}&sort=stars&order=desc&per_page=${perPage}`;
    try {
      const text = await myFetch(url, { headers: { 'User-Agent': UA, Accept: 'application/vnd.github+json' } }, timeoutMs);
      const json = JSON.parse(text);
      for (const it of json.items || []) {
        const mapped = mapSearchItem(it, 0);
        const prev = merged.get(mapped.fullName);
        if (!prev || mapped.stars > prev.stars) merged.set(mapped.fullName, mapped);
      }
    } catch (e) {
      // 单个 topic 搜索失败（限流等）不中断整个领域，继续其余 topic
    }
    await new Promise((r) => setTimeout(r, 300));
  }
  const repos = [...merged.values()].sort((a, b) => b.stars - a.stars);
  if (repos.length === 0) throw new Error(`领域「${cat.label}」各 topic 搜索均未返回项目`);
  return repos.slice(0, topN);
}

async function collectCategories(categories, timeoutMs = 30000) {
  const seen = new Set();
  const sections = [];
  for (const cat of categories) {
    const n = cat.topN || 5;
    let repos = [];
    let note = '';
    try {
      // 多取几个候选，用于跨领域去重后仍能补足 TOP N
      const all = await fetchCategoryTop(cat, Math.min(n + 5, 30), timeoutMs);
      repos = all.filter((r) => !seen.has(r.fullName)).slice(0, n);
    } catch (e) {
      note = e.message;
    }
    for (const r of repos) seen.add(r.fullName);
    sections.push({
      key: cat.key || cat.label,
      label: cat.label,
      repos: repos.map((r, i) => ({ ...r, rank: i + 1 })),
      note: repos.length === 0 ? `本领域本次未获取到有效项目：${note || '无匹配结果'}` : ''
    });
  }
  return { source: 'github-search-api', sections };
}

async function enrichWithApi(repos, timeoutMs = 30000) {
  const out = [];
  for (const r of repos) {
    try {
      const d = await fetchRepoDetail(r.fullName, timeoutMs);
      out.push({
        ...r,
        watchers: d.subscribers_count || 0,
        updatedAt: d.updated_at ? d.updated_at.slice(0, 10) : '',
        license: d.license && d.license.spdx_id ? d.license.spdx_id : '',
        description: r.description || d.description || '',
        topics: Array.isArray(d.topics) ? d.topics : []
      });
    } catch (e) {
      out.push({ ...r, watchers: 0, updatedAt: '', license: '', topics: [] });
    }
    await new Promise((res) => setTimeout(res, 350));
  }
  return out;
}

async function collect(topN, timeoutMs = 30000) {
  let repos = null;
  let source = 'github-trending';
  try {
    repos = await fetchTrendingPage();
  } catch (e) {
    source = 'github-search-api';
    repos = await fetchSearchTop(topN, timeoutMs);
  }
  repos = repos.slice(0, topN).map((r, i) => ({ ...r, rank: i + 1 }));
  return { source, repos: await enrichWithApi(repos, timeoutMs) };
}

module.exports = { collect, fetchTrendingPage, fetchSearchTop, collectCategories, fetchCategoryTop, buildCategoryQuery };