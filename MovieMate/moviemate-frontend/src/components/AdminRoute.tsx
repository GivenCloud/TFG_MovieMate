import { Navigate } from 'react-router-dom'
import { useAuthStore } from '../store/authStore'
import { useMyProfile } from '../hooks/useAuth'

export default function AdminRoute({ children }: { children: React.ReactNode }) {
  const { isAuthenticated, isInitialized } = useAuthStore()
  const { data: me, isLoading } = useMyProfile()

  if (!isInitialized || isLoading) {
    return (
      <div className="flex items-center justify-center h-screen bg-bg-0">
        <div className="w-8 h-8 border-2 border-accent border-t-transparent rounded-full animate-spin" />
      </div>
    )
  }

  if (!isAuthenticated) return <Navigate to="/login" replace />
  if (me?.role !== 'ADMIN') return <Navigate to="/" replace />

  return <>{children}</>
}
