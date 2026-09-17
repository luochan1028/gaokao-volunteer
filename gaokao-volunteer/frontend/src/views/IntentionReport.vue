<template>
  <div class="report-page">
    <div v-if="loading" class="loading-wrap">
      <div class="loading-spinner"></div>
      <p>正在生成志愿意向方案...</p>
    </div>

    <div v-else-if="report" class="report-content">
      <!-- 头部 -->
      <div class="report-header">
        <h1>高考志愿填报意向书</h1>
        <div class="meta">
          <span>版本 v{{ report.version }}</span>
          <span>{{ report.timestamp }}</span>
          <span>数据版本: {{ report.dataVersion }}</span>
        </div>
        <div class="disclaimer">{{ report.disclaimer }}</div>
      </div>

      <!-- 1. 学生画像摘要 -->
      <section class="report-section">
        <h2>1. 学生画像摘要</h2>
        <pre class="info-block">{{ report.studentSummary }}</pre>
      </section>

      <!-- 2. 家庭现实结论 -->
      <section class="report-section">
        <h2>2. 家庭现实结论</h2>
        <pre class="info-block">{{ report.familyConclusion }}</pre>
      </section>

      <!-- 3. 性格与职业倾向 -->
      <section class="report-section">
        <h2>3. 性格与职业倾向</h2>
        <pre class="info-block">{{ report.personalityResult }}</pre>
      </section>

      <!-- 4. 推荐专业TOP10 -->
      <section class="report-section">
        <h2>4. 推荐专业 TOP10</h2>
        <div class="card-list">
          <div v-for="(item, i) in report.recommendedMajors.slice(0, 10)" :key="i" class="rec-card">
            <div class="rec-header">
              <span class="rec-rank">#{{ i + 1 }}</span>
              <span class="rec-school">{{ item.schoolName }}</span>
              <span class="rec-group">{{ item.majorGroup }}</span>
              <span :class="['rec-prob', item.admissionProb]">{{ item.admissionProb }}</span>
            </div>
            <div class="rec-majors">{{ item.majorList.join(' / ') }}</div>
            <div class="rec-score">匹配度: {{ item.matchScore }}分</div>
            <div class="rec-reasons">
              <p v-if="item.realityReason"><span class="tag">现实</span>{{ item.realityReason }}</p>
              <p v-if="item.personalityReason"><span class="tag">性格</span>{{ item.personalityReason }}</p>
              <p v-if="item.careerReason"><span class="tag">职业</span>{{ item.careerReason }}</p>
            </div>
            <div v-if="item.riskWarnings.length" class="rec-risks">
              <span v-for="(r, j) in item.riskWarnings" :key="j" class="risk-tag">{{ r }}</span>
            </div>
            <div class="rec-oneline">{{ item.oneSentence }}</div>
          </div>
        </div>
      </section>

      <!-- 5. 慎选专业 -->
      <section v-if="report.cautiousMajors.length" class="report-section">
        <h2>5. 慎选专业 TOP5</h2>
        <ul class="cautious-list">
          <li v-for="(c, i) in report.cautiousMajors" :key="i">{{ c }}</li>
        </ul>
      </section>

      <!-- 6. 院校专业组方案 -->
      <section class="report-section">
        <h2>6. 院校专业组方案</h2>
        <div class="plan-grid">
          <div class="plan-col chong">
            <h3>冲 ({{ report.chongPlans.length }})</h3>
            <div v-for="(item, i) in report.chongPlans" :key="'c'+i" class="plan-item">
              <div class="plan-school">{{ item.schoolName }}</div>
              <div class="plan-major">{{ item.majorGroup }}</div>
              <div class="plan-score">{{ item.matchScore }}分</div>
            </div>
          </div>
          <div class="plan-col wen">
            <h3>稳 ({{ report.wenPlans.length }})</h3>
            <div v-for="(item, i) in report.wenPlans" :key="'w'+i" class="plan-item">
              <div class="plan-school">{{ item.schoolName }}</div>
              <div class="plan-major">{{ item.majorGroup }}</div>
              <div class="plan-score">{{ item.matchScore }}分</div>
            </div>
          </div>
          <div class="plan-col bao">
            <h3>保 ({{ report.baoPlans.length }})</h3>
            <div v-for="(item, i) in report.baoPlans" :key="'b'+i" class="plan-item">
              <div class="plan-school">{{ item.schoolName }}</div>
              <div class="plan-major">{{ item.majorGroup }}</div>
              <div class="plan-score">{{ item.matchScore }}分</div>
            </div>
          </div>
        </div>
      </section>

      <!-- 7. 城市与路径分析 -->
      <section class="report-section">
        <h2>7. 城市与路径分析</h2>
        <p class="info-block">{{ report.cityAnalysis }}</p>
      </section>

      <!-- 8. 家长vs学生冲突点 -->
      <section v-if="report.conflictPoints.length" class="report-section">
        <h2>8. 家长 vs 学生冲突点</h2>
        <div v-for="(c, i) in report.conflictPoints" :key="i" class="conflict-item">{{ c }}</div>
      </section>

      <!-- 9. 待确认问题清单 -->
      <section v-if="report.pendingQuestions.length" class="report-section">
        <h2>9. 待确认问题清单</h2>
        <ul class="pending-list">
          <li v-for="(q, i) in report.pendingQuestions" :key="i">{{ q }}</li>
        </ul>
      </section>

      <!-- 操作按钮 -->
      <div class="report-actions">
        <button @click="regenerate" class="action-btn primary">重新生成</button>
        <button @click="exportText" class="action-btn">导出文本</button>
        <button @click="printReport" class="action-btn">打印/PDF</button>
      </div>
    </div>

    <div v-else class="empty-state">
      <p>暂无意向书数据</p>
      <button @click="generate" class="action-btn primary">生成意向书</button>
    </div>
  </div>
</template>

<script>
import { generateReport, getReport } from '@/api/ai'

export default {
  name: 'IntentionReport',
  data() {
    return { report: null, loading: false }
  },
  computed: {
    userId() {
      const user = JSON.parse(localStorage.getItem('userInfo') || '{}')
      return user.id || 1
    }
  },
  mounted() { this.load() },
  methods: {
    async load() {
      this.loading = true
      try {
        const res = await getReport(this.userId)
        if (res.data.code === 200) this.report = res.data.data
      } catch (e) { console.error(e) }
      this.loading = false
    },
    async generate() {
      this.loading = true
      try {
        const res = await generateReport(this.userId)
        if (res.data.code === 200) this.report = res.data.data
      } catch (e) { console.error(e) }
      this.loading = false
    },
    regenerate() { this.generate() },
    exportText() {
      const r = this.report
      let text = `高考志愿填报意向书 v${r.version}\n${r.timestamp}\n\n`
      text += `免责声明：${r.disclaimer}\n\n`
      text += `1. 学生画像摘要\n${r.studentSummary}\n\n`
      text += `2. 家庭现实结论\n${r.familyConclusion}\n\n`
      text += `3. 性格与职业倾向\n${r.personalityResult}\n\n`
      text += `4. 推荐专业TOP10\n`
      r.recommendedMajors.forEach((m, i) => {
        text += `  #${i+1} ${m.schoolName} - ${m.majorGroup} (${m.admissionProb}) 匹配度${m.matchScore}\n`
        text += `    ${m.oneSentence}\n`
      })
      text += `\n5. 慎选专业\n${r.cautiousMajors.join('\n')}\n\n`
      text += `6. 院校专业组方案\n冲: ${r.chongPlans.map(m => m.schoolName).join(', ')}\n`
      text += `稳: ${r.wenPlans.map(m => m.schoolName).join(', ')}\n`
      text += `保: ${r.baoPlans.map(m => m.schoolName).join(', ')}\n\n`
      text += `7. 城市分析\n${r.cityAnalysis}\n\n`
      if (r.conflictPoints.length) text += `8. 冲突点\n${r.conflictPoints.join('\n')}\n\n`
      if (r.pendingQuestions.length) text += `9. 待确认\n${r.pendingQuestions.join('\n')}\n`

      const blob = new Blob([text], { type: 'text/plain;charset=utf-8' })
      const a = document.createElement('a')
      a.href = URL.createObjectURL(blob)
      a.download = `志愿意向书_v${r.version}.txt`
      a.click()
    },
    printReport() { window.print() }
  }
}
</script>

<style scoped>
.report-page { max-width: 800px; margin: 0 auto; padding: 20px; background: #fff; min-height: 100vh; }
.loading-wrap { display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 80px; }
.loading-spinner { width: 48px; height: 48px; border: 4px solid #ecf5ff; border-top: 4px solid #409eff; border-radius: 50%; animation: spin 1s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }
.report-header { text-align: center; margin-bottom: 32px; padding-bottom: 20px; border-bottom: 2px solid #409eff; }
.report-header h1 { font-size: 24px; color: #303133; margin: 0 0 12px; }
.meta { display: flex; justify-content: center; gap: 16px; font-size: 12px; color: #909399; }
.disclaimer { margin-top: 12px; padding: 8px 16px; background: #fff4e5; border-radius: 8px; font-size: 12px; color: #e65100; }
.report-section { margin-bottom: 28px; }
.report-section h2 { font-size: 18px; color: #303133; margin: 0 0 12px; padding-left: 12px; border-left: 4px solid #409eff; }
.info-block { background: #f5f7fa; padding: 16px; border-radius: 8px; font-size: 14px; line-height: 1.8; white-space: pre-wrap; margin: 0; }
.card-list { display: flex; flex-direction: column; gap: 12px; }
.rec-card { padding: 16px; border: 1px solid #e4e7ed; border-radius: 12px; }
.rec-header { display: flex; align-items: center; gap: 8px; margin-bottom: 8px; }
.rec-rank { font-weight: bold; color: #409eff; }
.rec-school { font-size: 16px; font-weight: bold; }
.rec-group { color: #606266; font-size: 14px; }
.rec-prob { margin-left: auto; padding: 2px 12px; border-radius: 12px; font-size: 12px; }
.rec-prob.冲 { background: #fef0f0; color: #f56c6c; }
.rec-prob.稳 { background: #ecf5ff; color: #409eff; }
.rec-prob.保 { background: #f0f9ff; color: #67c23a; }
.rec-majors { font-size: 13px; color: #606266; margin-bottom: 4px; }
.rec-score { font-size: 12px; color: #909399; }
.rec-reasons p { font-size: 13px; margin: 4px 0; }
.tag { display: inline-block; padding: 0 6px; background: #ecf5ff; color: #409eff; border-radius: 4px; font-size: 11px; margin-right: 4px; }
.rec-risks { margin: 8px 0; }
.risk-tag { display: inline-block; padding: 2px 8px; background: #fef0f0; color: #f56c6c; border-radius: 4px; font-size: 12px; margin-right: 4px; }
.rec-oneline { margin-top: 8px; padding-top: 8px; border-top: 1px dashed #e4e7ed; font-size: 13px; color: #606266; font-style: italic; }
.cautious-list { list-style: disc; padding-left: 24px; }
.cautious-list li { padding: 4px 0; color: #e6a23c; }
.plan-grid { display: grid; grid-template-columns: 1fr 1fr 1fr; gap: 16px; }
.plan-col { padding: 16px; border-radius: 12px; }
.plan-col.chong { background: #fef0f0; }
.plan-col.wen { background: #ecf5ff; }
.plan-col.bao { background: #f0f9ff; }
.plan-col h3 { font-size: 16px; margin: 0 0 12px; text-align: center; }
.plan-item { padding: 8px; background: #fff; border-radius: 8px; margin-bottom: 8px; }
.plan-school { font-weight: bold; font-size: 14px; }
.plan-major { font-size: 12px; color: #909399; }
.plan-score { font-size: 11px; color: #c0c4cc; }
.conflict-item { padding: 12px 16px; background: #fff4e5; border-radius: 8px; margin-bottom: 8px; font-size: 14px; color: #e65100; }
.pending-list { list-style: disc; padding-left: 24px; }
.pending-list li { padding: 4px 0; color: #909399; }
.report-actions { display: flex; gap: 12px; justify-content: center; margin-top: 32px; }
.action-btn { padding: 10px 24px; border: 1px solid #dcdfe6; border-radius: 24px; background: #fff; color: #606266; cursor: pointer; font-size: 14px; }
.action-btn.primary { background: #409eff; color: #fff; border-color: #409eff; }
.empty-state { text-align: center; padding: 80px; }
@media print { .report-actions { display: none; } }
</style>
