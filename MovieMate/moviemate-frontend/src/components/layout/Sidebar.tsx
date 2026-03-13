import { NavLink, Link, useNavigate } from 'react-router-dom'
import { useAuthStore } from '../../store/authStore'
import { useMyProfile } from '../../hooks/useAuth'
import { useQuery } from '@tanstack/react-query'
import { notificationsApi } from '../../api/notifications'
import { queryKeys } from '../../lib/queryKeys'
import { cn } from '../../lib/utils'

const NAV_MAIN = [
  { to: '/',          icon: '🏠', label: 'Inicio' },
  { to: '/discover',  icon: '🔍', label: 'Descubrir' },
  { to: '/activity',  icon: '📡', label: 'Actividad' },
]

function getNavPersonal(username?: string) {
  return [
    { to: username ? `/profile/${username}?tab=ratings` : '/', icon: '⭐', label: 'Valoraciones', neverActive: true },
    { to: '/lists',     icon: '📋', label: 'Listas' },
    { to: '/watchlist', icon: '🕐', label: 'Por ver' },
    { to: '/favorites', icon: '❤️',  label: 'Favoritos' },
  ]
}

type NavItem = { to: string; icon: string; label: string; neverActive?: boolean }

const INACTIVE_LINK_CLASS = 'flex items-center gap-2.5 px-3 py-2 rounded-lg text-sm transition-all border-l-2 mb-0.5 text-muted hover:text-white hover:bg-bg-2 border-transparent'

function SidebarSection({ label, items }: { label: string; items: NavItem[] }) {
  return (
    <div className="mb-2">
      <p className="px-4 py-1.5 text-[0.6rem] font-bold tracking-[1.2px] uppercase text-muted/60">
        {label}
      </p>
      <div className="px-3">
        {items.map(({ to, icon, label: itemLabel, neverActive }) =>
          neverActive ? (
            <Link key={to} to={to} className={INACTIVE_LINK_CLASS}>
              <span className="w-5 text-center text-[0.9rem] opacity-80">{icon}</span>
              {itemLabel}
            </Link>
          ) : (
            <NavLink
              key={to}
              to={to}
              end={to === '/'}
              className={({ isActive }) => cn(
                'flex items-center gap-2.5 px-3 py-2 rounded-lg text-sm transition-all border-l-2 mb-0.5',
                isActive
                  ? 'text-accent bg-accent/[0.06] border-accent font-medium'
                  : 'text-muted hover:text-white hover:bg-bg-2 border-transparent'
              )}
            >
              <span className="w-5 text-center text-[0.9rem] opacity-80">{icon}</span>
              {itemLabel}
            </NavLink>
          )
        )}
      </div>
    </div>
  )
}

export default function Sidebar() {
  const { isAuthenticated, sessionUser } = useAuthStore()
  const navigate = useNavigate()
  const { data: me } = useMyProfile()

  const navPersonal = getNavPersonal(sessionUser?.username)

  const { data: unreadCount } = useQuery({
    queryKey: queryKeys.notifications.unreadCount(),
    queryFn: () => notificationsApi.getUnreadCount(),
    enabled: isAuthenticated,
    refetchInterval: 60_000,
    select: (res) => res.data,
  })

  return (
    <aside className="w-56 shrink-0 bg-bg-1 border-r border-white/[0.06] hidden lg:flex flex-col h-full">
      {/* Brand */}
      <Link to="/" className="h-13 shrink-0 flex items-center gap-2.5 px-4 border-b border-white/[0.06] hover:bg-bg-2 transition-colors">
        <div className="w-8 h-8 bg-accent rounded-lg flex items-center justify-center text-bg-0 font-bold text-sm shrink-0">
          🎬
        </div>
        <span className="font-display font-bold italic text-[1.1rem] tracking-tight">
          MovieMate
        </span>
      </Link>

      <nav className="flex-1 overflow-y-auto py-4 scrollbar-none">
        <SidebarSection label="Principal" items={NAV_MAIN} />
        <SidebarSection label="Mi espacio" items={navPersonal} />

        {/* Notificaciones con badge de no leídas */}
        <div className="px-3 mt-1">
          <NavLink
            to="/notifications"
            className={({ isActive }) => cn(
              'flex items-center gap-2.5 px-3 py-2 rounded-lg text-sm transition-all border-l-2',
              isActive
                ? 'text-accent bg-accent/[0.06] border-accent font-medium'
                : 'text-muted hover:text-white hover:bg-bg-2 border-transparent'
            )}
          >
            <span className="w-5 text-center text-[0.9rem]">🔔</span>
            <span className="flex-1">Notificaciones</span>
            {unreadCount && unreadCount > 0 && (
              <span className="bg-red-500 text-white text-[0.6rem] font-bold px-1.5 py-0.5 rounded-full min-w-[18px] text-center">
                {unreadCount > 99 ? '99+' : unreadCount}
              </span>
            )}
          </NavLink>
        </div>
      </nav>

      {/* Link admin — solo visible si el usuario es ADMIN */}
      {me?.role === 'ADMIN' && (
        <div className="px-3 pb-2">
          <NavLink
            to="/admin"
            className={({ isActive }) => cn(
              'flex items-center gap-2.5 px-3 py-2 rounded-lg text-sm transition-all border-l-2',
              isActive
                ? 'text-accent bg-accent/[0.06] border-accent font-medium'
                : 'text-muted hover:text-white hover:bg-bg-2 border-transparent'
            )}
          >
            <span className="w-5 text-center text-[0.9rem]">🛡️</span>
            <span className="flex-1">Administración</span>
          </NavLink>
        </div>
      )}

      {/* Usuario en el pie — sessionUser se guarda al hacer login */}
      {sessionUser && (
        <div
          className="px-4 py-3 border-t border-white/[0.06] flex items-center gap-2.5 cursor-pointer hover:bg-bg-2 transition-colors"
          onClick={() => navigate(`/profile/${sessionUser.username}`)}
        >
          <div className="w-8 h-8 rounded-full bg-gradient-to-br from-accent to-pink-500 flex items-center justify-center text-xs font-bold text-bg-0 shrink-0 overflow-hidden">
            {me?.avatarUrl ? (
              <img src={me.avatarUrl} alt={sessionUser.username} className="w-full h-full object-cover" />
            ) : (
              sessionUser.username.charAt(0).toUpperCase()
            )}
          </div>
          <div className="min-w-0">
            <p className="text-sm text-white/90 truncate font-medium">{sessionUser.username}</p>
            <p className="text-xs text-muted font-mono truncate">{sessionUser.email}</p>
          </div>
        </div>
      )}
    </aside>
  )
}