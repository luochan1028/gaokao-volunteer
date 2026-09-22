'use strict';

const assert = require('assert');
const { buildCategoryQuery } = require('../src/github');
const { buildCategoriesHtml } = require('../src/html');
const { buildTemplate } = require('../src/config');

let pass = 0;
function ok(cond, label) {
  if (cond) {
    pass++;
    console.log('PASS', label);
  } else {
    console.error('FAIL', label);
    process.exit(1);
  }
}

// buildCategoryQuery：默认 minStars/pushedDays
{
  const NOW = new Date('2026-09-19T00:00:00Z').getTime();
  const q = buildCategoryQuery({ topics: ['finance'] }, 'finance', NOW);
  ok(q === 'topic:finance stars:>500 pushed:>2026-06-21', 'query 默认下限/活跃窗口');
}
// buildCategoryQuery：自定义 minStars/pushedDays
{
  const NOW = new Date('2026-09-19T00:00:00Z').getTime();
  const q = buildCategoryQuery({ minStars: 1000, pushedDays: 30 }, 'video-player', NOW);
  ok(q === 'topic:video-player stars:>1000 pushed:>2026-08-20', 'query 自定义下限/窗口');
}
// buildCategoriesHtml：分板块渲染 + 空板块提示
{
  const html = buildCategoriesHtml({
    dateStr: '2026年09月19日',
    pushedAt: '2026-09-19 13:00:00',
    sections: [
      {
        label: '财经',
        repos: [
          { rank: 1, fullName: 'a/fin', url: 'https://github.com/a/fin', summary: '量化交易框架', category: 'Python', language: 'Python', stars: 12000, forks: 900, watchers: 0, todayStars: 0, updatedAt: '2026-09-18', license: 'MIT' }
        ],
        note: ''
      },
      { label: '视频', repos: [], note: '本领域本次未获取到有效项目：限流' }
    ]
  });
  ok(html.includes('财经') && html.includes('视频'), 'html 含两个板块标题');
  ok(html.includes('a/fin') && html.includes('量化交易框架'), 'html 含项目卡片');
  ok(!html.includes('0 Watch'), 'html watchers=0 时不显示 Watch 徽标');
  ok(html.includes('未获取到有效项目'), 'html 空板块显示提示');
  ok(html.includes('MIT'), 'html 显示协议');
}
// 配置模板包含 categories 字段且默认为空数组（全站模式）
{
  const tpl = buildTemplate({});
  ok(Array.isArray(tpl.categories) && tpl.categories.length === 0, '模板 categories 默认空');
}

console.log(`categories.test 全部通过（${pass} 项）`);
