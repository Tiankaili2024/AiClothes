import request from './request'

export function addFavorite(recordId) {
  return request.post('/favorite/add/' + recordId)
}

export function removeFavorite(recordId) {
  return request.delete('/favorite/remove/' + recordId)
}

export function getFavorites(page, size) {
  return request.get('/favorite/list', { params: { page, size } })
}

export function checkFavorite(recordId) {
  return request.get('/favorite/check/' + recordId)
}
