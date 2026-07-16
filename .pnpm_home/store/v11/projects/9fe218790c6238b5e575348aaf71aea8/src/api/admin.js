import request from './request'

export function getDashboard() {
  return request.get('/admin/dashboard')
}

export function listUsers(page, size, keyword) {
  return request.get('/admin/users', { params: { page, size, keyword } })
}

export function toggleUserStatus(userId, status) {
  return request.put('/admin/user/status/' + userId, { status })
}

export function getConfig() {
  return request.get('/admin/config')
}

export function updateConfig(configs) {
  return request.put('/admin/config', configs)
}
