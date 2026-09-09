
import request from '@/utils/request'

export function searchCollegesApi(params) {
  return request({
    url: '/api/public/colleges',
    method: 'get',
    params
  })
}

export function getCollegeByIdApi(id) {
  return request({
    url: `/api/public/colleges/${id}`,
    method: 'get'
  })
}

export function getMajorsByCollegeApi(id) {
  return request({
    url: `/api/public/colleges/${id}/majors`,
    method: 'get'
  })
}

export function getEnrollmentScoresApi(id, year) {
  return request({
    url: `/api/public/colleges/${id}/scores`,
    method: 'get',
    params: { year }
  })
}

export function getAllProvincesApi() {
  return request({
    url: '/api/public/colleges/provinces',
    method: 'get'
  })
}

export function getAllYearsApi() {
  return request({
    url: '/api/public/colleges/years',
    method: 'get'
  })
}

export function compareCollegesApi(ids) {
  return request({
    url: '/api/public/colleges/compare',
    method: 'post',
    data: ids
  })
}

export function searchMajorsApi(params) {
  return request({
    url: '/api/public/colleges/public/majors',
    method: 'get',
    params
  })
}

export function getMajorByIdApi(id) {
  return request({
    url: `/api/public/colleges/public/majors/${id}`,
    method: 'get'
  })
}

export function compareMajorsApi(ids) {
  return request({
    url: '/api/public/colleges/public/majors/compare',
    method: 'post',
    data: ids
  })
}
