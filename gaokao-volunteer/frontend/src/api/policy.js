import request from '@/utils/request'

export function getPoliciesApi(province) {
  return request.get('/api/public/policies', { params: { province } })
}

export function getPolicyByIdApi(id) {
  return request.get(`/api/public/policies/${id}`)
}
