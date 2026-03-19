import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { activityApi } from '../../api/activity'
import { useAuthStore } from '../../store/authStore'
import { timeAgo, toSlug } from '../../lib/utils'
import EmptyState from '../../components/shared/EmptyState'
import BackButton from '../../components/shared/BackButton'
import type { ActivityResponse } from '../../types'

type Tab = 'personal' | 'global'

const TAG_LABELS: Record<string, string> = {
  INCREIBLE:     '🤩 Increíble',
  RECOMENDADA:   '👍 Recomendada',
  ENTRETENIDA:   '😊 Entretenida',
  REGULAR:       '😐 Regular',
  DECEPCIONANTE: '😞 Decepcionante',
}

// ── Esqueleto de carga ───────────────────────────────────────
function ActivitySkeleton() {
  return (
    <div className="divide-y divide-white/[0.04]">
      {[1, 2, 3, 4, 5].map((i) => (
        <div key={i} className="flex gap-3 px-6 py-4 animate-pulse">
          <div className="w-8 h-8 rounded-full bg-bg-3 shrink-0" />
          <div className="flex-1 space-y-2">
            <div className="h-3.5 bg-bg-3 rounded w-3/4" />
            <div className="h-16 bg-bg-3 rounded-xl" />
            <div className="h-2.5 bg-bg-3 rounded w-20" />
          </div>
        </div>
      ))}
    </div>
  )
}

// ── Item de actividad ────────────────────────────────────────
function ActivityItem({ activity }: { activity: ActivityResponse }) {
  const { user, type, createdAt, rating, list, targetUser, content } = activity

  const contentUrl = content
    ? (() => {
        const slug = toSlug(content.title)
        return slug
          ? `/content/${content.contentType}/${content.tmdbId}/${slug}`
          : `/content/${content.contentType}/${content.tmdbId}`
      })()
    : null

  const renderAction = () => {
    switch (type) {
      case 'RATING_CREATED':
      case 'RATING_UPDATED':
        return (
          <>
            <span className="text-muted">
              {type === 'RATING_CREATED' ? 'valoró' : 'actualizó su valoración de'}
            </span>{' '}
            {content && contentUrl && (
              <Link
                to={contentUrl}
                state={{ content }}
                className="font-semibold text-white/90 hover:text-accent transition-colors"
              >
                {content.title}
              </Link>
            )}
            {rating && (
              <span className="ml-1.5 text-yellow-400 tracking-tight">
                {'★'.repeat(rating.rating)}
                <span className="text-white/15">{'★'.repeat(5 - rating.rating)}</span>
              </span>
            )}
          </>
        )

      case 'LIST_CREATED':
        return (
          <>
            <span className="text-muted">creó la lista</span>{' '}
            <span className="font-semibold text-white/90">
              {list ? `"${list.name}"` : 'una lista'}
            </span>
          </>
        )

      case 'LIST_UPDATED':
        return (
          <>
            <span className="text-muted">actualizó la lista</span>{' '}
            <span className="font-semibold text-white/90">
              {list ? `"${list.name}"` : 'una lista'}
            </span>
          </>
        )

      case 'FOLLOW':
        return (
          <>
            <span className="text-muted">empezó a seguir a</span>{' '}
            {targetUser && (
              <Link
                to={`/profile/${targetUser.username}`}
                className="font-semibold text-white/90 hover:text-accent transition-colors"
              >
                @{targetUser.username}
              </Link>
            )}
          </>
        )

      case 'COMMENT_ADDED_TO_LIST':
        return (
          <>
            <span className="text-muted">comentó en la lista</span>{' '}
            <span className="font-semibold text-white/90">
              {list ? `"${list.name}"` : 'una lista'}
            </span>
          </>
        )

      default:
        return null
    }
  }

  return (
    <div className="flex gap-3 px-6 py-4 hover:bg-bg-1/60 transition-colors">
      {/* Avatar */}
      <Link to={`/profile/${user.username}`} className="shrink-0 mt-0.5">
        <div className="w-8 h-8 rounded-full bg-gradient-to-br from-accent to-pink-500 flex items-center justify-center text-xs font-bold text-bg-0 overflow-hidden hover:ring-2 hover:ring-accent/40 transition-all">
          {user.avatarUrl ? (
            <img src={user.avatarUrl} alt={user.username} className="w-full h-full object-cover" />
          ) : (
            user.username.charAt(0).toUpperCase()
          )}
        </div>
      </Link>

      <div className="flex-1 min-w-0">
        {/* Línea de acción */}
        <p className="text-sm leading-snug mb-2">
          <Link
            to={`/profile/${user.username}`}
            className="font-semibold text-white/90 hover:text-accent transition-colors"
          >
            @{user.username}
          </Link>{' '}
          {renderAction()}
        </p>

        {/* Card de contenido — solo para valoraciones */}
        {(type === 'RATING_CREATED' || type === 'RATING_UPDATED') && content && contentUrl && (
          <Link
            to={contentUrl}
            state={{ content }}
            className="flex gap-3 bg-bg-2 border border-white/[0.06] rounded-xl p-3 mb-2 hover:border-white/[0.14] transition-colors group"
          >
            {content.posterUrl && (
              <div className="w-10 h-14 rounded-lg overflow-hidden shrink-0 bg-bg-3">
                <img
                  src={content.posterUrl}
                  alt={content.title}
                  className="w-full h-full object-cover"
                />
              </div>
            )}
            <div className="flex-1 min-w-0">
              <p className="text-xs font-semibold text-white/80 group-hover:text-white truncate mb-0.5">
                {content.title}
              </p>
              <p className="text-[0.65rem] text-muted font-mono mb-1.5">
                {content.releaseDate ? new Date(content.releaseDate).getFullYear() : ''}
                {' · '}
                {content.contentType === 'MOVIE' ? 'Película' : 'Serie'}
              </p>
              {rating?.emotionalTag && (
                <span className="inline-block text-[0.65rem] font-medium bg-accent/10 text-accent border border-accent/20 px-1.5 py-0.5 rounded-full">
                  {TAG_LABELS[rating.emotionalTag] ?? rating.emotionalTag}
                </span>
              )}
              {rating?.reviewText && (
                <p className="text-xs text-white/50 mt-1.5 line-clamp-2 leading-relaxed">
                  {rating.reviewText}
                </p>
              )}
            </div>
          </Link>
        )}

        {/* Timestamp */}
        <p className="text-[0.65rem] text-muted font-mono">{timeAgo(createdAt)}</p>
      </div>
    </div>
  )
}

// ── Página principal ─────────────────────────────────────────
export default function ActivityPage() {
  const { isAuthenticated } = useAuthStore()
  const [tab, setTab] = useState<Tab>(isAuthenticated ? 'personal' : 'global')
  const [globalSize, setGlobalSize] = useState(20)
  const [personalSize, setPersonalSize] = useState(20)

  const {
    data: globalData,
    isLoading: loadingGlobal,
    isFetching: fetchingGlobal,
  } = useQuery({
    queryKey: ['feed', 'global', globalSize],
    queryFn: () => activityApi.getGlobalFeed(0, globalSize),
    select: (res) => res.data,
    staleTime: 1000 * 60,
  })

  const {
    data: personalData,
    isLoading: loadingPersonal,
    isFetching: fetchingPersonal,
  } = useQuery({
    queryKey: ['feed', 'personal', personalSize],
    queryFn: () => activityApi.getPersonalFeed(0, personalSize),
    select: (res) => res.data,
    enabled: isAuthenticated && tab === 'personal',
    staleTime: 1000 * 60,
  })

  const items = tab === 'global'
    ? (globalData?.content ?? [])
    : (personalData?.content ?? [])

  const isLoading  = tab === 'global' ? loadingGlobal  : loadingPersonal
  const isFetching = tab === 'global' ? fetchingGlobal : fetchingPersonal
  const size       = tab === 'global' ? globalSize     : personalSize
  const setSize    = tab === 'global' ? setGlobalSize  : setPersonalSize
  const hasMore    = items.length >= size

  return (
    <div className="pb-12">
      {/* Botón volver */}
      <div className="px-4 lg:px-6 pt-4 pb-1">
        <BackButton to="/" label="Inicio" />
      </div>

      {/* Cabecera */}
      <div className="px-4 lg:px-6 py-5 border-b border-white/[0.06]">
        <h1 className="font-display font-bold italic text-2xl">Actividad</h1>
        <p className="text-sm text-muted mt-0.5">
          {tab === 'personal'
            ? 'Lo último de las personas que sigues'
            : 'Lo más reciente de la comunidad'}
        </p>
      </div>

      {/* Tabs */}
      <div className="flex gap-2 px-4 lg:px-6 py-3 border-b border-white/[0.06]">
        {isAuthenticated && (
          <button
            onClick={() => setTab('personal')}
            className={`px-3.5 py-1.5 rounded-full text-xs font-medium transition-colors
              ${tab === 'personal'
                ? 'bg-accent text-bg-0'
                : 'bg-bg-2 text-muted hover:text-white border border-white/[0.06]'}`}
          >
            Para ti
          </button>
        )}
        <button
          onClick={() => setTab('global')}
          className={`px-3.5 py-1.5 rounded-full text-xs font-medium transition-colors
            ${tab === 'global'
              ? 'bg-accent text-bg-0'
              : 'bg-bg-2 text-muted hover:text-white border border-white/[0.06]'}`}
        >
          Global
        </button>
      </div>

      {/* Contenido */}
      {isLoading ? (
        <ActivitySkeleton />
      ) : items.length === 0 ? (
        tab === 'personal' ? (
          <EmptyState
            icon="📡"
            title="Nada por aquí aún"
            description="Sigue a otros usuarios para ver su actividad en tu feed personal."
          />
        ) : (
          <EmptyState
            icon="📡"
            title="Sin actividad reciente"
            description="Todavía no hay actividad en la comunidad."
          />
        )
      ) : (
        <>
          <div className="max-w-2xl mx-auto divide-y divide-white/[0.04]">
            {items.map((activity, i) => (
              <ActivityItem
                key={`${activity.user.id}-${activity.type}-${activity.createdAt}-${i}`}
                activity={activity}
              />
            ))}
          </div>

          {hasMore && (
            <div className="flex justify-center py-6">
              <button
                onClick={() => setSize((s) => s + 20)}
                disabled={isFetching}
                className="px-6 py-2 text-sm font-medium border border-white/[0.1] text-muted hover:text-white hover:border-white/20 rounded-xl transition-colors disabled:opacity-50"
              >
                {isFetching ? 'Cargando…' : 'Ver más'}
              </button>
            </div>
          )}
        </>
      )}
    </div>
  )
}
