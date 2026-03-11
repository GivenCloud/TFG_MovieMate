import { useState, useEffect, useRef } from 'react'
import { Link } from 'react-router-dom'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import { toSlug } from '../../lib/utils'
import { listsApi } from '../../api/lists'
import { queryKeys } from '../../lib/queryKeys'
import { useAuthStore } from '../../store/authStore'
import type { ContentResponse } from '../../types'

interface Props {
  content: ContentResponse
  userRating?: number
}

export default function PosterCard({ content, userRating }: Props) {
  const { isAuthenticated } = useAuthStore()
  const [listOpen, setListOpen] = useState(false)
  const dropdownRef = useRef<HTMLDivElement>(null)
  const queryClient = useQueryClient()

  const slug = toSlug(content.title)
  const url = slug
    ? `/content/${content.contentType}/${content.tmdbId}/${slug}`
    : `/content/${content.contentType}/${content.tmdbId}`

  // Listas: se cargan solo cuando se abre el dropdown (lazy)
  const { data: lists } = useQuery({
    queryKey: queryKeys.lists.mine(),
    queryFn: () => listsApi.getMine().then((r) => r.data),
    enabled: listOpen && isAuthenticated,
    staleTime: 1000 * 60 * 2,
  })

  const { mutate: addToList, isPending } = useMutation({
    mutationFn: (listId: number) =>
      listsApi.addContent(listId, { tmdbId: content.tmdbId }),
    onSuccess: (_data, listId) => {
      // Invalida ambas claves que usan la misma lista
      queryClient.invalidateQueries({ queryKey: queryKeys.lists.mine() })
      queryClient.invalidateQueries({ queryKey: queryKeys.users.lists() })
      const listName = lists?.find((l) => l.id === listId)?.name ?? 'la lista'
      toast.success(`"${content.title}" añadido a ${listName}`)
      setListOpen(false)
    },
    onError: (err: any) => {
      toast.error(err?.response?.data?.message ?? 'Error al añadir a la lista')
    },
  })

  // Cierra el dropdown al hacer clic fuera
  useEffect(() => {
    if (!listOpen) return
    const handler = (e: MouseEvent) => {
      if (dropdownRef.current && !dropdownRef.current.contains(e.target as Node)) {
        setListOpen(false)
      }
    }
    document.addEventListener('mousedown', handler)
    return () => document.removeEventListener('mousedown', handler)
  }, [listOpen])

  const handleListToggle = (e: React.MouseEvent) => {
    e.preventDefault()
    e.stopPropagation()
    setListOpen((v) => !v)
  }

  const handleAddToList = (e: React.MouseEvent, listId: number) => {
    e.preventDefault()
    e.stopPropagation()
    addToList(listId)
  }

  const handleDropdownClick = (e: React.MouseEvent) => {
    e.preventDefault()
    e.stopPropagation()
  }

  return (
    <Link
      to={url}
      state={{ content }}
      className="group relative block shrink-0 w-36 cursor-pointer focus:outline-none focus-visible:ring-2 focus-visible:ring-accent rounded-xl"
    >
      <div className="relative aspect-[2/3] rounded-xl overflow-hidden bg-bg-3 border border-white/[0.06] mb-2">
        {content.posterUrl ? (
          <img
            src={content.posterUrl}
            alt={`Poster de ${content.title}`}
            loading="lazy"
            className="w-full h-full object-cover transition-transform duration-300 group-hover:scale-105"
          />
        ) : (
          <div className="w-full h-full flex items-center justify-center text-4xl bg-bg-3">
            🎬
          </div>
        )}

        {/* Gradiente inferior */}
        <div className="absolute inset-0 bg-gradient-to-t from-black/80 via-transparent to-transparent" />

        {/* Badge de valoración */}
        <div className="absolute bottom-1.5 left-2 flex items-center gap-1 text-yellow-400 text-[0.65rem] font-mono font-semibold">
          ⭐ {content.appRating > 0
            ? content.appRating.toFixed(1)
            : content.tmdbRating.toFixed(1)}
        </div>

        {/* Badge de tipo */}
        <div className="absolute top-1.5 right-1.5 text-[10px] font-semibold bg-black/60 backdrop-blur-sm text-white/70 px-1.5 py-0.5 rounded">
          {content.contentType === 'MOVIE' ? 'Film' : 'Serie'}
        </div>

        {/* Overlay de hover */}
        <div className="absolute inset-0 bg-black/60 opacity-0 group-hover:opacity-100 transition-opacity flex flex-col items-center justify-center gap-2 p-2">
          <span className="text-xs font-semibold bg-accent text-bg-0 px-3 py-1.5 rounded-lg pointer-events-none">
            Ver ficha →
          </span>
          {isAuthenticated && (
            <button
              onClick={handleListToggle}
              className={`text-xs font-semibold backdrop-blur-sm text-white px-3 py-1.5 rounded-lg transition-colors
                ${listOpen
                  ? 'bg-white/30'
                  : 'bg-white/15 hover:bg-white/25'
                }`}
            >
              + Lista
            </button>
          )}
        </div>
      </div>

      <p className="text-sm font-semibold text-white/90 leading-tight mb-1 line-clamp-2">
        {content.title}
      </p>
      <p className="text-[0.65rem] text-muted font-mono">
        {content.releaseDate ? new Date(content.releaseDate).getFullYear() : '—'}
        {content.genres[0] ? ` · ${content.genres[0]}` : ''}
      </p>

      {userRating != null && (
        <div className="flex gap-0.5 mt-1">
          {[1, 2, 3, 4, 5].map((n) => (
            <span
              key={n}
              className={`text-[0.6rem] ${n <= userRating ? 'text-yellow-400' : 'text-white/20'}`}
            >
              ★
            </span>
          ))}
        </div>
      )}

      {/* ─── Dropdown de listas ─────────────────────────────────
          Posicionado con absolute respecto al Link (fuera del
          overflow-hidden del poster), por encima del resto con z-30.
      ─────────────────────────────────────────────────────────── */}
      {listOpen && (
        <div
          ref={dropdownRef}
          onClick={handleDropdownClick}
          className="absolute left-0 top-full mt-1 w-52 bg-bg-2 border border-white/[0.1] rounded-xl shadow-2xl z-30 overflow-hidden"
        >
          {!lists ? (
            <p className="px-4 py-3 text-sm text-muted animate-pulse">Cargando listas…</p>
          ) : lists.length === 0 ? (
            <p className="px-4 py-3 text-sm text-muted">No tienes listas aún</p>
          ) : (
            <>
              <p className="px-4 pt-3 pb-1.5 text-[0.6rem] font-mono text-muted uppercase tracking-widest">
                Añadir a lista
              </p>
              {lists.map((list) => (
                <button
                  key={list.id}
                  onClick={(e) => handleAddToList(e, list.id)}
                  disabled={isPending}
                  className="w-full text-left px-4 py-2.5 text-sm hover:bg-bg-3 text-white/80 hover:text-white transition-colors flex items-center gap-2.5 disabled:opacity-50"
                >
                  <span className="text-[0.75rem] shrink-0">
                    {list.listType === 'FAVORITES' ? '❤️'
                      : list.listType === 'WATCHLIST' ? '🕐'
                      : list.listType === 'WATCHED' ? '👁️'
                      : '📋'}
                  </span>
                  <span className="truncate flex-1">{list.name}</span>
                  <span className="text-[0.6rem] text-muted font-mono shrink-0">
                    {list.itemCount}
                  </span>
                </button>
              ))}
            </>
          )}
        </div>
      )}
    </Link>
  )
}
