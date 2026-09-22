'use strict';

function cleanText(s) {
  if (!s) return '';
  return s
    .replace(/[\u0000-\u0008\u000B\u000C\u000E-\u001F\u007F]/g, '')
    .replace(/[\r\n\t]+/g, ' ')
    .replace(/\s+/g, ' ')
    .replace(/[ \t]+(?=[，。；：！？,.!?])/g, '')
    .trim();
}

function stripAds(s) {
  if (!s) return '';
  return s
    .replace(/[\u2705\u2728\u2b50\u2757\u2192➡✅✨⭐🚀🔥💡👍]+/g, '')
    .replace(/^(follow|star|watch|subscribe|fork|sponsor|join|添加|关注|订阅)\s*[：: ]/i, '')
    .replace(/\s{2,}/g, ' ')
    .trim();
}

function truncate(s, maxLen) {
  if (!s) return '';
  return s.length > maxLen ? s.slice(0, maxLen - 1) + '…' : s;
}

function inferCategory(language) {
  const map = {
    javascript: '前端 / JavaScript',
    typescript: '前端 / TypeScript',
    python: 'Python',
    go: 'Go',
    java: 'Java',
    'c++': 'C / C++',
    c: 'C / C++',
    rust: 'Rust',
    ruby: 'Ruby',
    php: 'PHP',
    'c#': '.NET / C#',
    swift: '移动开发 / Swift',
    kotlin: '移动开发 / Kotlin',
    dart: '移动开发 / Dart',
    'jupyter notebook': '数据分析',
    vue: '前端 / Vue',
    react: '前端 / React',
    html: '前端 / HTML',
    css: '前端 / CSS',
    shell: '运维 / Shell',
    dockerfile: 'DevOps / Docker',
    'objective-c': '移动开发 / Objective-C'
  };
  const key = (language || '').toLowerCase().trim();
  return map[key] || (language ? `其他 / ${language}` : '未分类');
}

function summarizeDescription(desc, topics, language) {
  const base = [
    cleanText(desc),
    topics && topics.length ? `技术标签：${topics.slice(0, 6).join('、')}。` : '',
    language ? `主要使用 ${language} 开发。` : ''
  ]
    .filter(Boolean)
    .join(' ');
  return truncate(stripAds(base), 280);
}

function cleanRepos(repos) {
  return repos.map((r) => ({
    rank: r.rank,
    name: r.repo,
    author: r.owner,
    fullName: r.fullName,
    url: r.url,
    category: inferCategory(r.language),
    language: r.language || '未知',
    stars: r.stars || 0,
    forks: r.forks || 0,
    watchers: r.watchers || 0,
    todayStars: r.todayStars || 0,
    summary: summarizeDescription(r.description, r.topics, r.language),
    updatedAt: r.updatedAt || '',
    license: r.license || '未知'
  }));
}

module.exports = { cleanRepos, cleanText, stripAds, truncate, inferCategory, summarizeDescription };