
import request from '@/utils/request'

export function addToVolunteerListApi(collegeId, majorId, name) {
  return request({
    url: '/api/volunteer/list',
    method: 'post',
    params: { collegeId, majorId, name }
  })
}

export function removeFromVolunteerListApi(id) {
  return request({
    url: `/api/volunteer/list/${id}`,
    method: 'delete'
  })
}

export function getUserVolunteerListApi() {
  return request({
    url: '/api/volunteer/list',
    method: 'get'
  })
}

export function savePlanApi(data) {
  return request({
    url: '/api/volunteer/plan',
    method: 'post',
    data
  })
}

export function updatePlanApi(id, name, description) {
  return request({
    url: `/api/volunteer/plan/${id}`,
    method: 'put',
    params: { name, description }
  })
}

export function deletePlanApi(id) {
  return request({
    url: `/api/volunteer/plan/${id}`,
    method: 'delete'
  })
}

export function getUserPlansApi() {
  return request({
    url: '/api/volunteer/plan',
    method: 'get'
  })
}

export function getPlanByIdApi(id) {
  return request({
    url: `/api/volunteer/plan/${id}`,
    method: 'get'
  })
}

export function generateReportApi(id) {
  return request({
    url: `/api/volunteer/plan/${id}/report`,
    method: 'get'
  })
}

export function copyPlanApi(id, newName) {
  return request({
    url: `/api/volunteer/plan/${id}/copy`,
    method: 'post',
    params: { newName }
  })
}

export function setDefaultPlanApi(id) {
  return request({
    url: `/api/volunteer/plan/${id}/default`,
    method: 'post'
  })
}
