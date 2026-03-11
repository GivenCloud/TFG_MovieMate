import { useEffect } from 'react'
import { Routes, Route, Navigate, useNavigate } from 'react-router-dom'
import { useAuthStore } from './store/authStore'
import Layout from './components/layout/Layout'
import HomePage from './features/home/HomePage'
import DiscoverPage from './features/discover/DiscoverPage'
import DetailPage from './features/detail/DetailPage'
// import ProfilePage from './features/profile/ProfilePage'
// import ListsPage from './features/lists/ListsPage'
// import NotificationsPage from './features/notifications/NotificationsPage'
import LoginPage from './features/auth/LoginPage'
// import RegisterPage from './features/auth/RegisterPage'

// Protege rutas que requieren autenticación.
// Muestra un spinner mientras Zustand se inicializa desde localStorage
// para evitar el flash de redirección al login.
function PrivateRoute({ children }: { children: React.ReactNode }) {
  const { isAuthenticated, isInitialized } = useAuthStore()

  if (!isInitialized) {
    return (
      <div className="flex items-center justify-center h-screen bg-bg-0">
        <div className="w-8 h-8 border-2 border-accent border-t-transparent rounded-full animate-spin" />
      </div>
    )
  }

  return isAuthenticated ? children : <Navigate to="/login" replace />
}

export default function App() {
  const { logout, isInitialized, setInitialized } = useAuthStore()
  const navigate = useNavigate()

  // Marca la sesión como inicializada en el primer render
  useEffect(() => {
    if (!isInitialized) setInitialized()
  }, [])

  // Escucha el evento de sesión expirada que emite el interceptor de Axios.
  // Usar un evento personalizado (en vez de window.location.href)
  // preserva el historial del router de React.
  useEffect(() => {
    const handleUnauthorized = () => {
      logout()
      navigate('/login', { replace: true })
    }
    window.addEventListener('mm:unauthorized', handleUnauthorized)
    return () => window.removeEventListener('mm:unauthorized', handleUnauthorized)
  }, [logout, navigate])

  return (
    <Routes>
      {/* Rutas públicas — sin sidebar ni topbar */}
      <Route path="/login" element={<LoginPage />} />
      {/* <Route path="/register" element={<RegisterPage />} /> */}

      {/* Rutas con layout (sidebar + topbar) */}
      <Route element={<Layout />}>
        <Route path="/" element={<HomePage />} />
        <Route path="/discover" element={<DiscoverPage />} />
        <Route path="/content/:contentType/:tmdbId/:slug?" element={<DetailPage />} />
        {/* <Route path="/profile/:username" element={<ProfilePage />} /> */}

        {/* Requieren login */}
        {/* <Route
          path="/lists"
          element={<PrivateRoute><ListsPage /></PrivateRoute>}
        /> */}
        {/* <Route
          path="/notifications"
          element={<PrivateRoute><NotificationsPage /></PrivateRoute>}
        /> */}
      </Route>

      {/* Ruta desconocida → inicio */}
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}