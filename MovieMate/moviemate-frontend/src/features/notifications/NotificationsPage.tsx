import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { notificationsApi } from '../../api/notifications'
import { usersApi } from '../../api/users'
import { queryKeys } from '../../lib/queryKeys'
import { timeAgo } from '../../lib/utils'
import EmptyState from '../../components/shared/EmptyState'
import type { NotificationDto, NotificationType } from '../../types'

// ── Config por tipo de notificación ─────────────────────────
const TYPE_CONFIG: Record<
  NotificationType,
  { icon: string; bg: string; text: (username: string) => string }
> = {
  FOLLOWER: {
    icon: '👤',
    bg: 'rgba(167,139,250,0.15)',
    text: (u) => `@${u} ha empezado a seguirte.`,
  },
  FOLLOW_REQUEST: {
    icon: '👤',
    bg: 'rgba(167,139,250,0.15)',
    text: (u) => `@${u} quiere seguirte.`,
  },
  FOLLOW_REQUEST_ACCEPTED: {
    icon: '✅',
    bg: 'rgba(74,222,128,0.15)',
    text: (u) => `@${u} ha aceptado tu solicitud de seguimiento.`,
  },
  REVIEW_LIKE: {
    icon: '❤️',
    bg: 'rgba(248,113,113,0.15)',
    text: (u) => `@${u} ha dado me gusta a tu reseña.`,
  },
}

type Filter = 'all' | 'unread' | 'follows' | 'likes'

const FOLLOW_TYPES: NotificationType[] = [
  'FOLLOWER',
  'FOLLOW_REQUEST',
  'FOLLOW_REQUEST_ACCEPTED',
]

// ── Esqueleto de carga ───────────────────────────────────────
function NotifSkeleton() {
  return (
    <div className="divide-y divide-white/[0.04]">
      {[1, 2, 3, 4].map((i) => (
        <div key={i} className="flex items-center gap-4 px-6 py-4 animate-pulse">
          <div className="w-10 h-10 rounded-full bg-bg-3 shrink-0" />
          <div className="flex-1 space-y-2">
            <div className="h-3.5 bg-bg-3 rounded w-64" />
            <div className="h-3 bg-bg-3 rounded w-24" />
          </div>
        </div>
      ))}
    </div>
  )
}

// ── Página ───────────────────────────────────────────────────
export default function NotificationsPage() {
  const queryClient = useQueryClient()
  const [filter, setFilter] = useState<Filter>('all')

  // Senderids / requestIds ya procesados en esta sesión (evita doble acción)
  const [followed, setFollowed] = useState<Set<number>>(new Set())
  const [processed, setProcessed] = useState<Set<number>>(new Set())

  // ── Queries ───────────────────────────────────────────────
  const { data: notifications = [], isLoading } = useQuery({
    queryKey: queryKeys.users.notifications(),
    queryFn: () => usersApi.getNotifications().then((r) => r.data),
    staleTime: 1000 * 30,
  })

  // ── Mutations ─────────────────────────────────────────────
  const markRead = useMutation({
    mutationFn: (id: number) => notificationsApi.markAsRead(id),
    // Actualización optimista: flip read = true en la caché
    onMutate: async (id) => {
      await queryClient.cancelQueries({ queryKey: queryKeys.users.notifications() })
      const prev = queryClient.getQueryData<NotificationDto[]>(queryKeys.users.notifications())
      queryClient.setQueryData<NotificationDto[]>(
        queryKeys.users.notifications(),
        (old = []) => old.map((n) => (n.id === id ? { ...n, read: true } : n))
      )
      return { prev }
    },
    onError: (_err, _id, ctx) => {
      if (ctx?.prev) queryClient.setQueryData(queryKeys.users.notifications(), ctx.prev)
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.notifications.unreadCount() })
    },
  })

  const markAllRead = useMutation({
    mutationFn: () => notificationsApi.markAllAsRead(),
    onSuccess: () => {
      queryClient.setQueryData<NotificationDto[]>(
        queryKeys.users.notifications(),
        (old = []) => old.map((n) => ({ ...n, read: true }))
      )
      queryClient.invalidateQueries({ queryKey: queryKeys.notifications.unreadCount() })
    },
  })

  const followUser = useMutation({
    mutationFn: (userId: number) => usersApi.follow(userId),
    onSuccess: (_data, userId) => {
      setFollowed((prev) => new Set(prev).add(userId))
    },
  })

  const acceptRequest = useMutation({
    mutationFn: (requestId: number) => usersApi.acceptFollowRequest(requestId),
    onSuccess: (_data, requestId) => {
      setProcessed((prev) => new Set(prev).add(requestId))
    },
  })

  const rejectRequest = useMutation({
    mutationFn: (requestId: number) => usersApi.rejectFollowRequest(requestId),
    onSuccess: (_data, requestId) => {
      setProcessed((prev) => new Set(prev).add(requestId))
    },
  })

  // ── Filtrado ──────────────────────────────────────────────
  const unreadCount = notifications.filter((n) => !n.read).length

  const filtered = notifications.filter((n) => {
    if (filter === 'unread') return !n.read
    if (filter === 'follows') return FOLLOW_TYPES.includes(n.type)
    if (filter === 'likes') return n.type === 'REVIEW_LIKE'
    return true
  })

  const FILTERS: { id: Filter; label: string | ((c: number) => string) }[] = [
    { id: 'all',     label: 'Todas' },
    { id: 'unread',  label: (c) => `No leídas${c > 0 ? ` (${c})` : ''}` },
    { id: 'follows', label: 'Seguimientos' },
    { id: 'likes',   label: 'Me gustas' },
  ]

  // ── Click en un item: marcar como leído ───────────────────
  const handleItemClick = (notif: NotificationDto) => {
    if (!notif.read) markRead.mutate(notif.id)
  }

  return (
    <div className="pb-12">
      {/* ── Topbar interno ───────────────────────────────── */}
      <div className="flex items-center justify-between px-6 py-5 border-b border-white/[0.06]">
        <h1 className="font-display font-bold italic text-2xl">Notificaciones</h1>
        {unreadCount > 0 && (
          <button
            onClick={() => markAllRead.mutate()}
            disabled={markAllRead.isPending}
            className="text-sm text-muted hover:text-white border border-white/[0.1] px-3 py-1.5 rounded-lg transition-colors disabled:opacity-50"
          >
            {markAllRead.isPending ? '…' : 'Marcar todo como leído'}
          </button>
        )}
      </div>

      {/* ── Filtros ───────────────────────────────────────── */}
      <div className="flex gap-2 px-6 py-3 border-b border-white/[0.06] overflow-x-auto scrollbar-none">
        {FILTERS.map(({ id, label }) => {
          const text = typeof label === 'function' ? label(unreadCount) : label
          return (
            <button
              key={id}
              onClick={() => setFilter(id)}
              className={`px-3.5 py-1.5 rounded-full text-xs font-medium whitespace-nowrap transition-colors
                ${filter === id
                  ? 'bg-accent text-bg-0'
                  : 'bg-bg-2 text-muted hover:text-white border border-white/[0.06]'
                }`}
            >
              {text}
            </button>
          )
        })}
      </div>

      {/* ── Lista ─────────────────────────────────────────── */}
      {isLoading ? (
        <NotifSkeleton />
      ) : filtered.length === 0 ? (
        <EmptyState
          icon="🔔"
          title={filter === 'unread' ? 'Todo al día' : 'Sin notificaciones'}
          description={
            filter === 'unread'
              ? 'No tienes notificaciones sin leer.'
              : 'Cuando alguien te siga o dé me gusta a tus reseñas aparecerá aquí.'
          }
        />
      ) : (
        <div className="divide-y divide-white/[0.04]">
          {filtered.map((notif) => {
            const cfg = TYPE_CONFIG[notif.type]
            const username = notif.senderUsername ?? `usuario_${notif.senderId}`
            const isUnread = !notif.read

            return (
              <div
                key={notif.id}
                onClick={() => handleItemClick(notif)}
                className={`flex items-center gap-4 px-6 py-4 transition-colors cursor-pointer
                  ${isUnread ? 'bg-accent/[0.03] hover:bg-accent/[0.05]' : 'hover:bg-bg-1'}`}
              >
                {/* Icono circular */}
                <div
                  className="w-10 h-10 rounded-full flex items-center justify-center text-lg shrink-0"
                  style={{ background: cfg.bg }}
                >
                  {notif.senderAvatarUrl ? (
                    <img
                      src={notif.senderAvatarUrl}
                      alt={username}
                      className="w-full h-full rounded-full object-cover"
                    />
                  ) : (
                    cfg.icon
                  )}
                </div>

                {/* Cuerpo */}
                <div className="flex-1 min-w-0">
                  <p className="text-sm text-white/85 leading-snug">
                    <Link
                      to={`/profile/${username}`}
                      onClick={(e) => e.stopPropagation()}
                      className="font-semibold hover:text-accent transition-colors"
                    >
                      @{username}
                    </Link>{' '}
                    {/* Elimina el "@username " que ya renderizamos como Link */}
                    {cfg.text(username).slice(username.length + 2)}
                  </p>
                  <p className="text-xs text-muted font-mono mt-1">{timeAgo(notif.createdAt)}</p>
                </div>

                {/* Acciones por tipo */}
                <div
                  className="flex items-center gap-2 shrink-0"
                  onClick={(e) => e.stopPropagation()}
                >
                  {notif.type === 'FOLLOWER' && !followed.has(notif.senderId) && (
                    <button
                      onClick={() => followUser.mutate(notif.senderId)}
                      disabled={followUser.isPending}
                      className="px-3 py-1 text-xs font-semibold bg-accent hover:bg-accent-light text-bg-0 rounded-lg transition-colors disabled:opacity-60"
                    >
                      Seguir
                    </button>
                  )}
                  {notif.type === 'FOLLOWER' && followed.has(notif.senderId) && (
                    <span className="px-3 py-1 text-xs text-muted border border-white/[0.1] rounded-lg">
                      Siguiendo
                    </span>
                  )}

                  {notif.type === 'FOLLOW_REQUEST' && !processed.has(notif.referenceId) && (
                    <>
                      <button
                        onClick={() => acceptRequest.mutate(notif.referenceId)}
                        disabled={acceptRequest.isPending}
                        className="px-3 py-1 text-xs font-semibold bg-accent hover:bg-accent-light text-bg-0 rounded-lg transition-colors disabled:opacity-60"
                      >
                        Aceptar
                      </button>
                      <button
                        onClick={() => rejectRequest.mutate(notif.referenceId)}
                        disabled={rejectRequest.isPending}
                        className="px-3 py-1 text-xs text-muted border border-white/[0.1] hover:text-white rounded-lg transition-colors disabled:opacity-60"
                      >
                        Rechazar
                      </button>
                    </>
                  )}
                  {notif.type === 'FOLLOW_REQUEST' && processed.has(notif.referenceId) && (
                    <span className="px-3 py-1 text-xs text-muted border border-white/[0.1] rounded-lg">
                      Procesada
                    </span>
                  )}
                </div>

                {/* Punto de no leído */}
                {isUnread && (
                  <div className="w-2 h-2 rounded-full bg-red-500 shrink-0" />
                )}
              </div>
            )
          })}
        </div>
      )}
    </div>
  )
}
