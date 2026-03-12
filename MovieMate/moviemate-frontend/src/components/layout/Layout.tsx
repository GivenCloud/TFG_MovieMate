import { Outlet, NavLink } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import Sidebar from './Sidebar'
import Topbar from './Topbar'
import { useWebSocket } from '@/hooks/useWebSocket'
import { useAuthStore } from '@/store/authStore'
import { notificationsApi } from '@/api/notifications'
import { queryKeys } from '@/lib/queryKeys'
import { cn } from '@/lib/utils'

function BottomNavBar() {
  const { isAuthenticated, sessionUser } = useAuthStore()

  const { data: unreadCount } = useQuery({
    queryKey: queryKeys.notifications.unreadCount(),
    queryFn: () => notificationsApi.getUnreadCount(),
    enabled: isAuthenticated,
    refetchInterval: 60_000,
    select: (res) => res.data,
  })

  const item = ({ isActive }: { isActive: boolean }) =>
    cn(
      'flex flex-col items-center gap-0.5 px-3 py-1.5 text-[0.6rem] font-medium transition-colors',
      isActive ? 'text-accent' : 'text-muted'
    )

  return (
    <nav className="lg:hidden fixed bottom-0 inset-x-0 bg-bg-1 border-t border-white/[0.06] flex items-center justify-around h-14 z-40">
      <NavLink to="/" end className={item}>
        <span className="text-lg leading-none">🏠</span>
        <span>Inicio</span>
      </NavLink>
      <NavLink to="/discover" className={item}>
        <span className="text-lg leading-none">🔍</span>
        <span>Descubrir</span>
      </NavLink>
      <NavLink to="/activity" className={item}>
        <span className="text-lg leading-none">📡</span>
        <span>Actividad</span>
      </NavLink>
      <NavLink to="/lists" className={item}>
        <span className="text-lg leading-none">📋</span>
        <span>Listas</span>
      </NavLink>
      <NavLink
        to={isAuthenticated && sessionUser ? `/notifications` : '/login'}
        className={({ isActive }) =>
          cn(
            'flex flex-col items-center gap-0.5 px-3 py-1.5 text-[0.6rem] font-medium transition-colors relative',
            isActive ? 'text-accent' : 'text-muted'
          )
        }
      >
        <span className="text-lg leading-none relative">
          🔔
          {unreadCount && unreadCount > 0 && (
            <span className="absolute -top-1 -right-1.5 bg-red-500 text-white text-[0.5rem] font-bold min-w-[14px] h-3.5 flex items-center justify-center rounded-full px-0.5">
              {unreadCount > 9 ? '9+' : unreadCount}
            </span>
          )}
        </span>
        <span>Notif.</span>
      </NavLink>
    </nav>
  )
}

export default function Layout() {
  // Conecta al broker STOMP para recibir notificaciones push en tiempo real
  useWebSocket()

  return (
    <div className="flex h-screen bg-bg-0 text-white overflow-hidden">
      <Sidebar />
      <div className="flex flex-col flex-1 min-w-0">
        <Topbar />
        {/* El scroll ocurre aquí dentro, no en el layout entero */}
        <main className="flex-1 overflow-y-auto pb-14 lg:pb-0">
          <Outlet />
        </main>
      </div>
      <BottomNavBar />
    </div>
  )
}