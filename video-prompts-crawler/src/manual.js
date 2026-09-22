const STEPS = [
  {
    id: 1,
    title: '选题与剧本创作',
    goal: '确定题材与钩子，产出可拍的单集剧本。短剧的生死线是前 3 秒钩子和每 15 秒一个情绪节拍。',
    prompts: [
      {
        name: '单集剧本生成（发给 Claude / GPT）',
        text: [
          '你是资深竖屏短剧编剧。请基于以下创意创作一集短剧剧本：',
          '【题材】：逆袭爽剧（可选：甜宠 / 悬疑 / 穿越 / 复仇）',
          '【核心创意】：（一句话填入你的点子）',
          '【目标时长】：90 秒，约 12-15 个镜头，9:16 竖屏',
          '【硬性要求】：',
          '1. 前 3 秒必须有强钩子（冲突 / 反转 / 悬念画面）',
          '2. 每 15 秒安排一个情绪节拍点（打脸 / 误会 / 升级）',
          '3. 台词全部使用口语化短句，每句不超过 12 字，方便 AI 口型对齐',
          '4. 结尾留钩子引出下一集',
          '5. 输出表格：镜号 | 景别 | 画面描述 | 台词 | 预估时长'
        ].join('\n')
      },
      {
        name: '小说改编（有原著时）',
        text: [
          '你是短剧改编编剧。以下是我提供的小说片段，请完成：',
          '1. 提取核心剧情单元与人物关系，给出改编价值评估；',
          '2. 按每集 90 秒的节奏切分分集大纲（列出每集钩子）；',
          '3. 输出第 1 集的可拍剧本（格式：镜号 | 景别 | 画面 | 台词 | 时长）。',
          '【原著片段】：（粘贴小说内容）'
        ].join('\n')
      }
    ],
    skillLink: { name: 'drama-skills：short-drama-write / short-drama-develop（剧本与分集改编技能）', url: 'https://github.com/zenstory-ai/drama-skills' }
  },
  {
    id: 2,
    title: '视觉设定与角色一致性',
    goal: '给每个角色和场景建立"连续性锁"——一段可反复粘贴进提示词的造型短语，保证跨镜头人物不走样。',
    prompts: [
      {
        name: '角色设定卡（发给即梦 / Midjourney 等图片模型）',
        text: [
          '角色三视图设定板，正面、侧面、背面全身像，',
          '【角色描述】：25 岁女性，黑色长直发，白色连衣裙，气质倔强清冷，',
          '纯白背景，均匀柔和光照，全身完整入画，高清细节，电影写实风格',
          '（提示：先生成设定板，选中效果最好的一张作为全剧角色参考图）'
        ].join('\n')
      },
      {
        name: '连续性锁短语模板（写在剧本旁，每镜复用）',
        text: [
          '真千金：烈焰红唇，黑天鹅高定礼裙，长发盘起，金耳坠，气场全开（脸不变，礼服不变）',
          '男主：黑色大衣，湿漉漉短发，身高 185，冷峻眉眼（参考 @图片1 的人物形象）',
          '（要点：把发型 / 服装 / 配饰 / 气质写死成一句短语，每一镜的视频提示词原样携带）'
        ].join('\n')
      }
    ],
    skillLink: { name: 'drama-skills：short-drama-assets（人物 / 场景 / 道具资产与连续性决策）', url: 'https://github.com/zenstory-ai/drama-skills' }
  },
  {
    id: 3,
    title: '分镜脚本',
    goal: '把剧本拆成逐镜计划：景别、运镜、画面、声音、时长。对白按中文约 4 字 / 秒估时，放不下就拆镜。',
    prompts: [
      {
        name: '分镜表生成（发给 Claude / GPT）',
        text: [
          '基于以下剧本输出分镜表，每镜包含：',
          '镜号 | 景别（极致特写 / 面部特写 / 中近景 / 中景 / 全景 / 远景）|',
          '运镜（推 / 拉 / 摇 / 移 / 跟拍 / 环绕 / 一镜到底）| 画面内容（人物动作与表情）|',
          '台词 / 音效 | 时长（秒）。',
          '对白按中文 4 字 / 秒估时，台词放不下则延长镜头或拆镜；',
          '关键情绪点使用特写，转场处标注运镜衔接方式。',
          '【剧本】：（粘贴第 1 步输出的剧本）'
        ].join('\n')
      }
    ],
    skillLink: { name: 'drama-skills：short-drama-storyboard（场次视觉计划与冻结关键帧）', url: 'https://github.com/zenstory-ai/drama-skills' }
  },
  {
    id: 4,
    title: '关键帧图片生成（首帧 / 图生视频底图）',
    goal: '视频模型普遍支持图生视频：先生成每镜首帧图，再让图动起来，角色一致性远好于纯文生视频。',
    prompts: [
      {
        name: '首帧图提示词模板（9:16 竖屏）',
        text: [
          '【风格】电影质感短剧画面，9:16 竖屏',
          '【画面】雨夜街道，女主角（参考上图角色设定：白色连衣裙，黑色长发湿透）背对镜头回望，',
          '霓虹灯在湿地面的倒影，浅景深',
          '【光照】冷色调路灯光 + 轮廓光，情绪压抑',
          '（每镜一张；生成后挑选与角色设定卡一致度过关的图作为该镜首帧）'
        ].join('\n')
      },
      {
        name: '多参考图角色一致性（即梦 Seedance / 可灵支持多图参考）',
        text: [
          '场景参考 @图片1 的雨夜街道，人物形象严格参考 @图片2 的女主角，',
          '保持服装发型完全一致，生成该角色在雨中回望的全身画面'
        ].join('\n')
      }
    ],
    skillLink: { name: 'drama-skills：short-drama-image-prompts（参考板与定点修改说明）', url: 'https://github.com/zenstory-ai/drama-skills' }
  },
  {
    id: 5,
    title: '视频生成提示词（核心步骤）',
    goal: '使用 Seedance（即梦）/ 可灵 / Veo 等模型，按"主体 + 场景 + 动作 + 运镜 + 分时段 + 音频 + 风格"公式逐镜生成。',
    prompts: [
      {
        name: '提示词结构公式（Seedance 2.0 官方写法）',
        text: [
          '[主体/人物设定] + [场景/环境] + [动作/运动描述] +',
          '[运镜语言] + [分时段描述] + [转场/特效] + [音频/音效设计] + [风格/氛围]'
        ].join('\n')
      },
      {
        name: '分时段短剧提示词模板（可直接照抄替换内容）',
        text: [
          '【风格】热门中国网剧风格，极致快切节奏，高颜值滤镜，9:16 竖屏，电影质感',
          '【人物】男主（参考 @图片1：黑色大衣，湿发，红眼眶），女主（参考 @图片2：白色连衣裙）',
          '【全局】人物一致性稳定，脸不变，服装不变；口型与台词逐字一致',
          '[00-05s] 雨街中景：女主决然转身离去，男主冲上前拉住她手腕，',
          '快速剪辑组合：雨滴特写 + 两人对视 + 男主眼眶泛红',
          '台词（口型）："你凭什么替我做决定！"',
          '[05-10s] 男主近景，雨水顺脸颊滑落，低声（哽咽）："因为我爱你。"',
          '背景：雨声音效 + 低沉弦乐渐起，结尾闪电定格'
        ].join('\n')
      },
      {
        name: '@ 引用语法速查（多模态参考，Seedance 2.0）',
        text: [
          '@图片1 作为首帧 | @图片2 作为尾帧 | 参考 @图片1 的人物形象',
          '场景参考 @图片3 | 参考 @视频1 的运镜效果 | 参考 @视频1 的动作编排',
          '旁白音色参考 @视频1 | 背景BGM参考 @音频1 | 穿着 @图片2 的服装',
          '（规则：每个引用必须说明用途；素材总数不超过 12 个；支持 4-15 秒时长）'
        ].join('\n')
      }
    ],
    skillLink: { name: 'seedance2-skill：官方提示词撰写指南（中文版全文）', url: 'https://github.com/dexhunter/seedance2-skill/blob/main/zh/SKILL.md' }
  },
  {
    id: 6,
    title: '配音与音效',
    goal: '台词用 TTS 生成（剪映 / 海螺 / MiniMax Audio），情绪用括号标注；音效与 BGM 在视频提示词里同步给出，或后期补。',
    prompts: [
      {
        name: 'TTS 情绪配音模板',
        text: [
          '（用低沉压抑、克制愤怒的男声，语速偏慢）你凭什么替我做决定！',
          '（用带哭腔、气声的女声，语速快）因为我爱你啊！',
          '（要点：一条台词一次生成，情绪写在括号里；数字 / 英文改成中文读法避免念错）'
        ].join('\n')
      },
      {
        name: '音效设计清单模板（交给视频提示词或后期）',
        text: [
          '镜 3：雨声持续 + 皮鞋踩水声；镜 5：巴掌音效 + BGM 骤停；',
          '镜 8：弦乐渐强 + 闪电雷声压尾；全片响度统一 -14 LUFS'
        ].join('\n')
      }
    ],
    skillLink: { name: 'drama-skills：short-drama-produce（TTS / 音乐任务确认后生产）', url: 'https://github.com/zenstory-ai/drama-skills' }
  },
  {
    id: 7,
    title: '剪辑成片',
    goal: '逐镜检查可用素材，按剪辑单拼接：字幕逐字取自台词，统一响度，导出 9:16 成片。',
    prompts: [
      {
        name: '剪辑指导（发给 Claude 生成剪辑单，或自己照做）',
        text: [
          '以下是一集短剧的分镜表和已生成素材清单，请输出剪辑单：',
          '每镜给出：素材编号、入点 / 出点（秒）、取舍理由、镜序、字幕文本（逐字取自台词）、',
          '转场方式；标注需要补拍 / 重roll的镜头及原因。',
          '【分镜表】：（粘贴）',
          '【素材清单】：（粘贴生成结果）'
        ].join('\n')
      }
    ],
    skillLink: { name: 'drama-skills：short-drama-edit（剪辑单与渲染成片）', url: 'https://github.com/zenstory-ai/drama-skills' }
  }
];

const TOOLKIT = {
  intro: '不想手动逐步做？drama-skills（2k+ stars）把上面七步做成了 11 个 Agent 技能，装进 Claude Code 后一句话跑通"点子 → 剧本 → 分镜 → 提示词 → 生成 → 剪辑"全流程，人物一致性、对白估时、确认后才花钱生成等工程问题都已内置。',
  install: [
    'git clone https://github.com/zenstory-ai/drama-skills',
    '按仓库 README 将 skills 目录复制到 ~/.claude/skills（Windows 为 %USERPROFILE%\\.claude\\skills）',
    '在 Claude Code 中执行：/short-drama dashboard'
  ],
  skillsNote: '五份创作事实文件贯穿全流程：剧本.md、视觉设定.md、分镜.md、图片提示词.md、视频提示词.md（成片阶段增加 剪辑单.md）——改哪份文件就是改哪一层决定。'
};

const RESOURCES = [
  { name: 'zenstory-ai/drama-skills', url: 'https://github.com/zenstory-ai/drama-skills', desc: 'AI 短剧全流程 11 技能套件（中文）' },
  { name: 'dexhunter/seedance2-skill', url: 'https://github.com/dexhunter/seedance2-skill', desc: 'Seedance 2.0 提示词撰写指南（含中文版）' },
  { name: 'ZeroLu/awesome-seedance', url: 'https://github.com/ZeroLu/awesome-seedance', desc: 'Seedance 高保真提示词合集（中文版含短剧专章）' },
  { name: 'YouMind-OpenLab/awesome-seedance-2-prompts', url: 'https://github.com/YouMind-OpenLab/awesome-seedance-2-prompts', desc: '2000+ 视频提示词（电影 / 动漫 / 广告 / UGC）' }
];

module.exports = { STEPS, TOOLKIT, RESOURCES };