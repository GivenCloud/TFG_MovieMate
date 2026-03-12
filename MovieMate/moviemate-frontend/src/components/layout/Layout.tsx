import { Outlet } from 'react-router-dom'
import Sidebar from './Sidebar'
import Topbar from './Topbar'
import { useWebSocket } from '@/hooks/useWebSocket'

export default function Layout() {
  // Conecta al broker STOMP para recibir notificaciones push en tiempo real
  useWebSocket()

  return (
    <div className="flex h-screen bg-bg-0 text-white overflow-hidden">
      <Sidebar />
      <div className="flex flex-col flex-1 min-w-0">
        <Topbar />
        {/* El scroll ocurre aquí dentro, no en el layout entero */}
        <main className="flex-1 overflow-y-auto">
          <Outlet />
        </main>
      </div>
    </div>
  )
}