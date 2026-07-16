import request from './request'
export function generateOutfit(data) {
  return request.post('/outfit/generate', data)
}
export function quickRefine(data) {
  return request.post('/outfit/refine', data)
}
export function getRecords(page, size) {
  return request.get('/outfit/records', { params: { page, size } })
}
export function getSessionRecords(sessionId) {
  return request.get('/outfit/session/' + sessionId)
}
export function deleteRecord(id) {
  return request.delete('/outfit/delete/' + id)
}
export function clearRecords() {
  return request.delete('/outfit/clear')
}
export function getTodayCount() {
  return request.get('/outfit/today-count')
}
export function getUserPortrait() {
  return request.get('/outfit/user-portrait')
}