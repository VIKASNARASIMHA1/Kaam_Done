import axios from 'axios'

const api = axios.create({
  baseURL: '/api'
})

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('taskflow_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

api.interceptors.response.use(
  (response) => response,
  (error) => {
    const isAuthRequest = error.config?.url?.includes('/auth/')
    // Only force a redirect for 401s on protected endpoints (an expired/invalid
    // session). A 401 from /auth/login or /auth/register just means "wrong
    // credentials" or "bad input" - that should stay on the page and show the
    // error message, not trigger a full-page redirect that wipes it out.
    if (error.response && error.response.status === 401 && !isAuthRequest) {
      localStorage.removeItem('taskflow_token')
      localStorage.removeItem('taskflow_user')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

export default api
