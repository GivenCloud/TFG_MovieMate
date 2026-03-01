import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useLogout } from '../../hooks/useAuth'
import { useAuthStore } from '../../store/authStore'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '../ui/dropdown-menu'

export default function Topbar() {
  const [query, setQuery] = useState('')
  const navigate = useNavigate()
  const logout = useLogout()
  const { isAuthenticated, sessionUser } = useAuthStore()

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault()
    if (query.trim()) {
      navigate(`/discover?q=${encodeURIComponent(query.trim())}`)
    }
  }

  return (
    <header className="h-13 flex items-center gap-3 px-6 bg-bg-1 border-b border-white/[0.06] sticky top-0 z-10">
      <form onSubmit={handleSearch} className="flex-1 max-w-md">
        <div className="flex items-center gap-2 bg-bg-2 border border-white/[0.06] rounded-xl px-3.5 py-2 text-sm text-muted hover:border-white/[0.12] transition-colors focus-within:border-accent/50">
          <span>🔍</span>
          <input
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            placeholder="Buscar películas, series, usuarios…"
            className="flex-1 bg-transparent outline-none text-white placeholder:text-muted text-sm"
          />
        </div>
      </form>

      <div className="ml-auto flex items-center gap-2">
        {isAuthenticated && sessionUser ? (
          <>
            <button
              onClick={() => navigate('/notifications')}
              className="w-8 h-8 rounded-full bg-bg-2 border border-white/[0.06] flex items-center justify-center hover:border-white/20 transition-colors"
              aria-label="Notificaciones"
            >
              🔔
            </button>

            <DropdownMenu>
              <DropdownMenuTrigger asChild>
                <button
                  className="w-8 h-8 rounded-full bg-gradient-to-br from-accent to-pink-500 flex items-center justify-center text-xs font-bold text-bg-0 hover:ring-2 hover:ring-accent/40 transition-all"
                  aria-label="Menú de usuario"
                >
                  {sessionUser.username.charAt(0).toUpperCase()}
                </button>
              </DropdownMenuTrigger>
              <DropdownMenuContent align="end" className="w-44 bg-bg-2 border-white/10 text-white">
                <DropdownMenuItem onClick={() => navigate(`/profile/${sessionUser.username}`)}>
                  👤 Mi perfil
                </DropdownMenuItem>
                <DropdownMenuItem onClick={() => navigate('/settings')}>
                  ⚙️ Ajustes
                </DropdownMenuItem>
                <DropdownMenuSeparator className="bg-white/10" />
                <DropdownMenuItem onClick={logout} className="text-red-400 focus:text-red-400">
                  🚪 Cerrar sesión
                </DropdownMenuItem>
              </DropdownMenuContent>
            </DropdownMenu>
          </>
        ) : (
          <button
            onClick={() => navigate('/login')}
            className="text-sm font-medium text-accent hover:text-accent-light transition-colors"
          >
            Iniciar sesión
          </button>
        )}
      </div>
    </header>
  )
}