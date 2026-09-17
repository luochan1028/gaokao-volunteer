<template>
  <div class="ai-assistant">
    <!-- 顶部状态栏 -->
    <div class="status-bar">
      <div class="phase-indicator">
        <span v-for="(p, i) in phases" :key="p.key"
          :class="['phase-item', { active: currentPhaseIndex >= i, current: currentPhaseIndex === i }]">
          <span class="phase-num">{{ i + 1 }}</span>
          <span class="phase-label">{{ p.label }}</span>
        </span>
      </div>
    </div>

    <!-- 对话区域 -->
    <div class="chat-area" ref="chatArea">
      <div v-for="(msg, i) in messages" :key="i" :class="['msg-bubble', msg.role]">
        <div class="msg-avatar">{{ msg.role === 'user' ? '我' : 'AI' }}</div>
        <div class="msg-content">
          <div class="msg-text">{{ msg.text }}</div>
          <div v-if="msg.options && msg.options.length" class="msg-options">
            <button v-for="opt in msg.options" :key="opt" @click="sendQuickReply(opt)" class="opt-btn">{{ opt }}</button>
          </div>
        </div>
      </div>
      <div v-if="loading" class="msg-bubble assistant">
        <div class="msg-avatar">AI</div>
        <div class="msg-content"><div class="typing">正在思考...</div></div>
      </div>
    </div>

    <!-- 语音/文字输入区 -->
    <div class="input-area">
      <div class="text-input-wrap">
        <input v-model="inputText" @keyup.enter="sendText" placeholder="输入或说话..."
          class="text-input" :disabled="loading" />
        <button @click="sendText" :disabled="!inputText || loading" class="send-btn">发送</button>
      </div>
      <button @click="toggleRecording" :class="['voice-btn', { recording: isRecording }]">
        <span v-if="isRecording" class="wave-anim">
          <span></span><span></span><span></span>
        </span>
        <span v-else>{{ micIcon }}</span>
      </button>
    </div>
  </div>
</template>

<script>
import { startDialog, sendMessage } from '@/api/ai'
import { useAuthStore } from '@/store/auth'

export default {
  name: 'AIAssistant',
  data() {
    return {
      phases: [
        { key: 'BASE_INFO', label: '基础信息' },
        { key: 'FAMILY_Q', label: '家庭现实' },
        { key: 'MAJOR_Q', label: '专业筛选' },
        { key: 'HOLLAND', label: '性格测试' },
        { key: 'GENERATE', label: '生成方案' }
      ],
      currentPhase: 'BASE_INFO',
      messages: [],
      inputText: '',
      loading: false,
      isRecording: false,
      recognition: null,
      authStore: useAuthStore()
    }
  },
  computed: {
    currentPhaseIndex() {
      const idx = this.phases.findIndex(p => p.key === this.currentPhase)
      return idx >= 0 ? idx : 0
    },
    micIcon() { return '🎤' },
    userId() {
      // 从token或localStorage获取用户ID
      const user = JSON.parse(localStorage.getItem('userInfo') || '{}')
      return user.id || 1
    }
  },
  mounted() {
    this.initSpeechRecognition()
    this.startConversation()
  },
  methods: {
    async startConversation() {
      this.loading = true
      try {
        const res = await startDialog(this.userId)
        if (res.data.code === 200) {
          const data = res.data.data
          this.currentPhase = data.phase
          this.messages.push({ role: 'assistant', text: data.response })
          if (data.options) this.messages[this.messages.length - 1].options = data.options
        }
      } catch (e) {
        this.messages.push({ role: 'assistant', text: '对话初始化失败，请刷新重试。' })
      }
      this.loading = false
      this.$nextTick(() => this.scrollToBottom())
    },

    async sendText() {
      if (!this.inputText || this.loading) return
      const text = this.inputText
      this.inputText = ''
      await this.processUserMessage(text)
    },

    async sendQuickReply(text) {
      if (this.loading) return
      await this.processUserMessage(text)
    },

    async processUserMessage(text) {
      this.messages.push({ role: 'user', text })
      this.loading = true
      this.$nextTick(() => this.scrollToBottom())

      try {
        const res = await sendMessage({
          userId: this.userId,
          text: text,
          phase: this.currentPhase,
          method: 'voice'
        })
        if (res.data.code === 200) {
          const data = res.data.data
          this.currentPhase = data.phase
          this.messages.push({
            role: 'assistant',
            text: data.response,
            options: data.options || []
          })
          if (data.nextAction === 'show_result') {
            setTimeout(() => this.$router.push('/ai/report'), 1500)
          }
        }
      } catch (e) {
        this.messages.push({ role: 'assistant', text: '抱歉，我遇到了一些问题，请重试。' })
      }

      this.loading = false
      this.$nextTick(() => this.scrollToBottom())
    },

    initSpeechRecognition() {
      const SR = window.SpeechRecognition || window.webkitSpeechRecognition
      if (SR) {
        this.recognition = new SR()
        this.recognition.lang = 'zh-CN'
        this.recognition.continuous = false
        this.recognition.interimResults = true
        this.recognition.onresult = (e) => {
          let text = ''
          for (let i = 0; i < e.results.length; i++) {
            text += e.results[i][0].transcript
          }
          this.inputText = text
        }
        this.recognition.onend = () => {
          this.isRecording = false
          if (this.inputText) this.sendText()
        }
        this.recognition.onerror = () => { this.isRecording = false }
      }
    },

    toggleRecording() {
      if (!this.recognition) {
        this.messages.push({ role: 'assistant', text: '浏览器不支持语音识别，请使用文字输入。' })
        return
      }
      if (this.isRecording) {
        this.recognition.stop()
      } else {
        this.recognition.start()
        this.isRecording = true
      }
    },

    scrollToBottom() {
      const el = this.$refs.chatArea
      if (el) el.scrollTop = el.scrollHeight
    }
  }
}
</script>

<style scoped>
.ai-assistant { display: flex; flex-direction: column; height: 100vh; background: #f5f7fa; }
.status-bar { padding: 12px 16px; background: #fff; border-bottom: 1px solid #e4e7ed; }
.phase-indicator { display: flex; justify-content: space-between; }
.phase-item { display: flex; align-items: center; gap: 6px; opacity: 0.4; transition: all .3s; }
.phase-item.active { opacity: 0.7; }
.phase-item.current { opacity: 1; }
.phase-num { width: 22px; height: 22px; border-radius: 50%; background: #dcdfe6; color: #909399; font-size: 12px;
  display: flex; align-items: center; justify-content: center; }
.phase-item.current .phase-num { background: #409eff; color: #fff; }
.phase-label { font-size: 12px; color: #606266; }
.chat-area { flex: 1; overflow-y: auto; padding: 16px; }
.msg-bubble { display: flex; gap: 10px; margin-bottom: 16px; max-width: 85%; }
.msg-bubble.user { margin-left: auto; flex-direction: row-reverse; }
.msg-avatar { width: 36px; height: 36px; border-radius: 50%; display: flex; align-items: center;
  justify-content: center; font-size: 13px; font-weight: bold; flex-shrink: 0; }
.msg-bubble.assistant .msg-avatar { background: #409eff; color: #fff; }
.msg-bubble.user .msg-avatar { background: #67c23a; color: #fff; }
.msg-content { display: flex; flex-direction: column; gap: 8px; }
.msg-text { padding: 12px 16px; border-radius: 12px; font-size: 14px; line-height: 1.6; white-space: pre-wrap; }
.msg-bubble.assistant .msg-text { background: #fff; color: #303133; border-top-left-radius: 4px; }
.msg-bubble.user .msg-text { background: #409eff; color: #fff; border-top-right-radius: 4px; }
.msg-options { display: flex; flex-wrap: wrap; gap: 8px; }
.opt-btn { padding: 6px 16px; border: 1px solid #409eff; border-radius: 20px; background: #fff;
  color: #409eff; font-size: 13px; cursor: pointer; transition: all .2s; }
.opt-btn:hover { background: #409eff; color: #fff; }
.typing { color: #909399; font-style: italic; padding: 12px 16px; background: #fff; border-radius: 12px; }
.input-area { display: flex; align-items: center; gap: 12px; padding: 12px 16px; background: #fff;
  border-top: 1px solid #e4e7ed; }
.text-input-wrap { flex: 1; display: flex; gap: 8px; }
.text-input { flex: 1; padding: 10px 16px; border: 1px solid #dcdfe6; border-radius: 24px; font-size: 14px; outline: none; }
.text-input:focus { border-color: #409eff; }
.send-btn { padding: 10px 20px; background: #409eff; color: #fff; border: none; border-radius: 24px;
  cursor: pointer; font-size: 14px; }
.send-btn:disabled { background: #c0c4cc; cursor: not-allowed; }
.voice-btn { width: 48px; height: 48px; border-radius: 50%; border: none; cursor: pointer;
  display: flex; align-items: center; justify-content: center; font-size: 20px;
  background: #ecf5ff; color: #409eff; transition: all .3s; flex-shrink: 0; }
.voice-btn.recording { background: #f56c6c; color: #fff; animation: pulse 1.5s infinite; }
@keyframes pulse { 0%,100% { transform: scale(1); } 50% { transform: scale(1.1); } }
.wave-anim { display: flex; gap: 3px; align-items: center; }
.wave-anim span { width: 3px; height: 16px; background: #fff; border-radius: 2px; animation: wave .6s infinite ease-in-out; }
.wave-anim span:nth-child(2) { animation-delay: .1s; }
.wave-anim span:nth-child(3) { animation-delay: .2s; }
@keyframes wave { 0%,100% { height: 8px; } 50% { height: 20px; } }
</style>
