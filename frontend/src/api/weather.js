import request from './request'

export function getWeather(city) {
  return request.get('/weather/current', { params: { city } })
}
