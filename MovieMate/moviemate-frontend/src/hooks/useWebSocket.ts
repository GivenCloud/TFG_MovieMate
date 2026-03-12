import { useEffect, useRef } from 'react'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import { useQueryClient } from '@tanstack/react-query'
import { useAuthStore } from '@/store/authStore'
import { queryKeys } from '@/lib/queryKeys'
import type { NotificationDto } from '@/types'

// Deriva la URL base del WebSocket quitando el prefijo /api de la variable de entorno
function getWsUrl(): string {
  const apiBase = (import.meta.env.VITE_API_BASE_URL as string) ?? 'http://localhost:8080/api'
  return apiBase.replace(/\/api\/?$/, '') + '/ws'
}

/**
 * Se conecta al broker STOMP del backend y escucha notificaciones push
 * del canal privado /user/queue/notifications.
 *
 * Solo se activa cuando el usuario está autenticado.
 * Cuando llega una notificación:
 *   1. La prepende a la caché de notificaciones (actualización inmediata)
 *   2. Invalida el contador de no leídas (fuerza refetch al servidor)
 */
export function useWebSocket() {
  const { token, isAuthenticated } = useAuthStore()
  const queryClient = useQueryClient()
  const clientRef = useRef<Client | null>(null)

  useEffect(() => {
    if (!isAuthenticated || !token) return

    const client = new Client({
      webSocketFactory: () => new SockJS(getWsUrl()),
      connectHeaders: {
        Authorization: `Bearer ${token}`,
      },
      reconnectDelay: 5000,
      // Silencia los logs internos de STOMP en producción
      debug: import.meta.env.DEV ? (msg) => console.debug('[STOMP]', msg) : () => {},

      onConnect: () => {
        client.subscribe('/user/queue/notifications', (frame) => {
          try {
            const notification: NotificationDto = JSON.parse(frame.body)

            // 1. Prepender en la lista de notificaciones (si ya está cacheada)
            queryClient.setQueryData<NotificationDto[]>(
              queryKeys.users.notifications(),
              (old) => (old ? [notification, ...old] : [notification])
            )

            // 2. Refrescar el contador de no leídas desde el servidor
            queryClient.invalidateQueries({
              queryKey: queryKeys.notifications.unreadCount(),
            })
          } catch {
            // Frame malformado — ignorar
          }
        })
      },

      onStompError: (frame) => {
        console.error('[STOMP] Error del broker:', frame.headers['message'])
      },
    })

    client.activate()
    clientRef.current = client

    return () => {
      client.deactivate()
      clientRef.current = null
    }
  }, [isAuthenticated, token, queryClient])
}
