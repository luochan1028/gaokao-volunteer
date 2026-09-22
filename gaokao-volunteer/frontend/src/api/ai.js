import request from '@/utils/request'

// V2.0 AI助手 API

// 开始对话
export function startDialog(userId) {
  return request({
    url: '/api/ai/dialog/start',
    method: 'post',
    params: { userId }
  })
}

// 发送对话消息
export function sendMessage(data) {
  return request({
    url: '/api/ai/dialog/message',
    method: 'post',
    data
  })
}

// 获取张雪峰家庭八问
export function getFamilyQuestions() {
  return request({
    url: '/api/ai/questions/family',
    method: 'get'
  })
}

// 获取张雪峰专业八问
export function getMajorQuestions() {
  return request({
    url: '/api/ai/questions/major',
    method: 'get'
  })
}

// 获取霍兰德12题
export function getHollandQuestions() {
  return request({
    url: '/api/ai/questions/holland',
    method: 'get'
  })
}

// 生成志愿意向书
export function generateReport(userId) {
  return request({
    url: '/api/ai/report/generate',
    method: 'post',
    params: { userId }
  })
}

// 获取志愿意向书
export function getReport(userId) {
  return request({
    url: `/api/ai/report/${userId}`,
    method: 'get'
  })
}
