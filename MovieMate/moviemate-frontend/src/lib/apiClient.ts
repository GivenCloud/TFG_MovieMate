import axios, { AxiosError } from 'axios'

// Lee la URL base del fichero .env correspondiente al entorno
const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  headers: { 'Content-Type': 'application/json' },
  timeout: 15000, // 15 segundos máximo por petición
})

// ── Interceptor de REQUEST ──────────────────────────────────────
// Añade el token JWT automáticamente en cada petición
apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('mm_token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// ── Interceptor de RESPONSE ─────────────────────────────────────
// Maneja errores globales sin romper el router de React
apiClient.interceptors.response.use(
  (response) => response,
  (error: AxiosError) => {
    if (error.response?.status === 401) {
      // Token expirado o inválido: limpia la sesión
      localStorage.removeItem('mm_token')
      // Emite un evento personalizado que el componente raíz escucha
      // (No usamos window.location.href para no romper el historial del router)
      window.dispatchEvent(new CustomEvent('mm:unauthorized'))
    }
    // Rechaza con el error original para que React Query lo propague
    return Promise.reject(error)
  }
)

export default apiClient