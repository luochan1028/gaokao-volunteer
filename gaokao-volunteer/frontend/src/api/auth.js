
import request from '@/utils/request'

export function loginApi(phone, password) {
  return request({
    url: '/api/auth/login',
    method: 'post',
    data: { phone, password }
  })
}

export function registerApi(phone, password, verificationCode) {
  return request({
    url: '/api/auth/register',
    method: 'post',
    data: { phone, password, verificationCode }
  })
}

export function getUserInfoApi() {
  return request({
    url: '/api/auth/me',
    method: 'get'
  })
}

export function updateUserInfoApi(data) {
  return request({
    url: '/api/auth/me',
    method: 'put',
    data
  })
}

export function checkPhoneExistsApi(phone) {
  return request({
    url: `/api/auth/check-phone/${phone}`,
    method: 'get'
  })
}

export function changePasswordApi(oldPassword, newPassword) {
  return request({
    url: '/api/auth/change-password',
    method: 'post',
    data: { oldPassword, newPassword }
  })
}

export function sendCodeApi(phone) {
  return request({
    url: '/api/auth/send-code',
    method: 'post',
    data: { phone }
  })
}
