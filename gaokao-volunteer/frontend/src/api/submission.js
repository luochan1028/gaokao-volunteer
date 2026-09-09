import request from '@/utils/request'

export function preCheckSubmissionApi(planId) {
  return request.get(`/api/submissions/check/${planId}`)
}

export function submitVolunteerApi(planId, verifyCode) {
  return request.post(`/api/submissions/submit/${planId}`, { verifyCode })
}
