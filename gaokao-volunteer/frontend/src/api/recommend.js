
import request from '@/utils/request'

export function generateRecommendationsApi(data) {
  return request({
    url: '/api/recommendations',
    method: 'post',
    data
  })
}

export function calculateProbabilityApi(params) {
  return request({
    url: '/api/recommendations/probability',
    method: 'get',
    params
  })
}
