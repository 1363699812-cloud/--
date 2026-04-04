import { defineStore } from 'pinia'
import Cookies from 'js-cookie'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: Cookies.get('token') || '',
    userInfo: JSON.parse(localStorage.getItem('userInfo') || '{}'),
  }),
  getters: {
    isLoggedIn: (state) => !!state.token,
    role: (state) => state.userInfo.role || '',
  },
  actions: {
    setLogin(data) {
      this.token = data.token
      this.userInfo = {
        id: data.id,
        username: data.username,
        realName: data.realName,
        role: data.role,
      }
      Cookies.set('token', data.token, { expires: 1 })
      localStorage.setItem('userInfo', JSON.stringify(this.userInfo))
    },
    logout() {
      this.token = ''
      this.userInfo = {}
      Cookies.remove('token')
      localStorage.removeItem('userInfo')
    },
  },
})
