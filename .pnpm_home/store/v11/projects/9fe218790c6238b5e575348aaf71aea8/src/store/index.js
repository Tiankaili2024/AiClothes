import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getUser, setUser, removeUser, getToken, setToken, removeToken } from '../utils/auth'

export const useUserStore = defineStore('user', () => {
  const user = ref(getUser())
  const token = ref(getToken())

  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => user.value?.role === 1)

  function login(userData, tokenStr) {
    user.value = userData
    token.value = tokenStr
    setUser(userData)
    setToken(tokenStr)
  }

  function logout() {
    user.value = null
    token.value = null
    removeUser()
    removeToken()
  }

  function updateUser(userData) {
    user.value = userData
    setUser(userData)
  }

  return { user, token, isLoggedIn, isAdmin, login, logout, updateUser }
})
