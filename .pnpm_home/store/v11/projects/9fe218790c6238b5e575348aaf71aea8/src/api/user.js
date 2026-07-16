import request from './request'

export function getUserInfo() {
  return request.get('/user/info')
}

export function updateProfile(data) {
  return request.put('/user/profile', data)
}

export function updatePassword(data) {
  return request.put('/user/password', data)
}

export function updateAvatar(data) {
  return request.put('/user/avatar', data)
}
