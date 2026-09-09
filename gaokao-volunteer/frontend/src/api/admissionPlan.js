import request from '@/utils/request'

export function searchAdmissionPlansApi(params) {
  return request.get('/api/public/admission-plans', { params })
}

export function getAdmissionBatchesApi() {
  return request.get('/api/public/admission-plans/batches')
}
