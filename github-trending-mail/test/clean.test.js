'use strict';

const { cleanRepos, cleanText, inferCategory, summarizeDescription, truncate } = require('../src/clean');

let failed = 0;
function t(name, cond) {
  if (cond) console.log(`PASS ${name}`);
  else { console.log(`FAIL ${name}`); failed = 1; }
}

t('cleanText 压空白/去乱码', cleanText('a\u0000\u001fb  c  d') === 'ab c d');
t('cleanText trim', cleanText('  你好  ') === '你好');
t('inferCategory python', inferCategory('Python') === 'Python');
t('inferCategory 未知', inferCategory('') === '未分类');
t('truncate 超过截断', truncate('abcdefghij', 5) === 'abcd…');
t('truncate 未超原样', truncate('abc', 5) === 'abc');

const raw = {
  rank: 1,
  repo: 'demo-proj',
  owner: 'someone',
  fullName: 'someone/demo-proj',
  url: 'https://github.com/someone/demo-proj',
  language: 'Go',
  stars: 12345,
  forks: 678,
  watchers: 90,
  todayStars: 123,
  description: '  一个\r\n示例  项目✅  ',
  topics: ['ai', 'go'],
  updatedAt: '2026-09-18',
  license: 'Apache-2.0'
};
const cleaned = cleanRepos([raw])[0];
t('clean 字段映射', cleaned.fullName === 'someone/demo-proj');
t('clean 数字保留', cleaned.stars === 12345 && cleaned.forks === 678 && cleaned.watchers === 90);
t('clean 今日增量', cleaned.todayStars === 123);
t('clean 协议', cleaned.license === 'Apache-2.0');
t('clean 分类', cleaned.category === 'Go');
t('clean 描述清洗去广告emoji', !cleaned.summary.includes('✅') && cleaned.summary.includes('示例 项目'));
t('summary 含技术标签', cleaned.summary.includes('ai'));

const s = summarizeDescription('x'.repeat(500), [], '');
t('summary 超长截断到280+', s.length <= 280);

process.exit(failed);