import { createContext, useContext, useState } from 'react'
import api from '../api'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [username, setUsername] = useState(localStorage.getItem('wz_username'))

  async function login(usernameInput, password) {
    const res = await api.post('/auth/login', { username: usernameInput, password })
    localStorage.setItem('wz_token', res.data.token)
    localStorage.setItem('wz_username', res.data.username)
    setUsername(res.data.username)
  }

  async function register(usernameInput, email, password) {
    const res = await api.post('/auth/register', { username: usernameInput, email, password })
    localStorage.setItem('wz_token', res.data.token)
    localStorage.setItem('wz_username', res.data.username)
    setUsername(res.data.username)
  }

  function logout() {
    localStorage.removeItem('wz_token')
    localStorage.removeItem('wz_username')
    setUsername(null)
  }

  return (
    <AuthContext.Provider value={{ username, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  return useContext(AuthContext)
}
