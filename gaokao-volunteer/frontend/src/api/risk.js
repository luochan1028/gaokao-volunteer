import request from '@/utils/request'

export function assessRiskApi(planId) {
  return request.get('/api/risk/assess', { params: { planId } })
}
