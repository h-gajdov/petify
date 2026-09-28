import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import type { LoginRequest, SignupRequest } from '../api/auth'
import { login as apiLogin, signup as apiSignup } from '../api/auth'

const STORAGE_KEY = 'petify.auth.user'

interface User {
  userId: number
  username: string
  email: string
  firstName: string
  lastName: string
  userType: string
  verified: boolean
}

export const useAuthStore = defineStore('auth', () => {
  const user = ref<User | null>(null)

  // Load user from localStorage on store initialization
  const storedUser = localStorage.getItem(STORAGE_KEY)
  if (storedUser) {
    try {
      user.value = JSON.parse(storedUser)
    } catch (e) {
      localStorage.removeItem(STORAGE_KEY)
    }
  }

  const isAuthenticated = computed(() => !!user.value)
  const token = computed(() => user.value ? `user_${user.value.userId}` : '')

  function setUser(userData: User | null) {
    user.value = userData
    if (userData) {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(userData))
    } else {
      localStorage.removeItem(STORAGE_KEY)
    }
  }

  async function login(payload: LoginRequest) {
    const result = await apiLogin(payload)
    if (result.user) {
      setUser(result.user)
    } else {
      throw new Error('Login failed: No user data received')
    }
  }

  async function signup(payload: SignupRequest): Promise<{ tokenReturned: boolean }> {
    const result = await apiSignup(payload)
    if (result.user) {
      setUser(result.user)
      return { tokenReturned: true }
    }
    // Registration successful but need to login
    return { tokenReturned: false }
  }

  function logout() {
    setUser(null)
  }

  return {
    user,
    token,
    isAuthenticated,
    login,
    signup,
    logout,
  }
})
