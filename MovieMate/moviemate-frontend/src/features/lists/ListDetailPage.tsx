import { useState } from 'react'
import { useParams, useLocation, Link } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import { listsApi } from '../../api/lists'
import { queryKeys } from '../../lib/queryKeys'
import { useAuthStore } from '../../store/authStore'
import PosterCard from '../../components/shared/PosterdCard'
import EmptyState from '../../components/shared/EmptyState'
import AddContentToListDialog from './AddContentToListDialog'
import type { ListResponse, ListType } from '../../types'

const LIST_TYPE_CONFIG: Record<ListType, { icon: string; label: string }> = {
  FAVORITES: { icon: '❤️',  label: 'Favoritos' },
  WATCHLIST: { icon: '🕐', label: 'Por ver' },
  WATCHED:   { icon: '👁️', label: 'Ya vistas' },
  CUSTOM:    { icon: '📋', label: 'Lista' },
}

function ListDetailSkeleton() {
  return (
    <div className="animate-pulse">
      <div className="px-6 py-5 border-b border-white/[0.06] space-y-2">
        <div className="h-7 bg-bg-3 rounded w-48" />
        <div className="h-4 bg-bg-3 rounded w-72" />
        <div className="h-3 bg-bg-3 rounded w-40 mt-2" />
      </div>
      <div className="px-4 lg:px-6 py-6 flex flex-wrap gap-3">
        {[1, 2, 3, 4, 5, 6].map((i) => (
          <div key={i} className="w-36 aspect-[2/3] bg-bg-3 rounded-xl" />
        ))}
      </div>
    </div>
  )
}

export default function ListDetailPage() {
  const { listId } = useParams<{ listId: string }>()
  const location = useLocation()
  const { isAuthenticated, sessionUser } = useAuthStore()
  const queryClient = useQueryClient()
  const [addDialogOpen, setAddDialogOpen] = useState(false)

  const stateList = location.state?.list as ListResponse | undefined
  const parsedId = Number(listId)

  const { data: list, isLoading } = useQuery({
    queryKey: queryKeys.lists.detail(parsedId),
    queryFn: () => listsApi.getById(parsedId).then((r) => r.data),
    placeholderData: stateList,
    staleTime: 1000 * 60 * 2,
    enabled: !!parsedId,
  })

  const isOwner = isAuthenticated && sessionUser?.username === list?.user?.username

  const { mutate: removeContent, isPending: isRemoving } = useMutation({
    mutationFn: (tmdbId: number) => listsApi.removeContent(parsedId, tmdbId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.lists.detail(parsedId) })
      queryClient.invalidateQueries({ queryKey: queryKeys.users.lists() })
      queryClient.invalidateQueries({ queryKey: queryKeys.lists.mine() })
      toast.success('Eliminado de la lista')
    },
    onError: () => toast.error('No se pudo eliminar'),
  })

  if (isLoading && !list) return <ListDetailSkeleton />

  if (!list) {
    return (
      <div className="flex flex-col items-center justify-center py-32 text-center">
        <span className="text-5xl mb-4">📋</span>
        <h2 className="text-xl font-bold text-white/80 mb-2">Lista no encontrada</h2>
        <p className="text-sm text-muted">No pudimos cargar la información de esta lista.</p>
      </div>
    )
  }

  const cfg = LIST_TYPE_CONFIG[list.listType]

  return (
    <div className="pb-12">
      {/* Cabecera */}
      <div className="px-4 lg:px-6 py-5 border-b border-white/[0.06]">
        <div className="flex items-center gap-4 mb-1.5">
          <span className="text-xl shrink-0">{cfg.icon}</span>
          <h1 className="font-display font-bold italic text-2xl">{list.name}</h1>
          {isOwner && (
            <button
              onClick={() => setAddDialogOpen(true)}
              className="shrink-0 flex items-center gap-1.5 px-3.5 py-2 text-sm font-semibold bg-accent hover:bg-accent-light text-bg-0 rounded-xl transition-colors"
            >
              + Añadir
            </button>
          )}
        </div>
        {list.description && (
          <p className="text-sm text-muted mt-1 max-w-xl">{list.description}</p>
        )}
        <div className="flex items-center gap-3 mt-2 text-xs text-muted font-mono flex-wrap">
          <Link
            to={`/profile/${list.user.username}`}
            className="hover:text-white transition-colors"
          >
            @{list.user.username}
          </Link>
          <span>·</span>
          <span>{list.itemCount} título{list.itemCount !== 1 ? 's' : ''}</span>
          <span>·</span>
          <span>{list.isPublic ? '🔓 Pública' : '🔒 Privada'}</span>
          {list.listType !== 'CUSTOM' && (
            <>
              <span>·</span>
              <span className="text-accent/70">{cfg.label}</span>
            </>
          )}
        </div>
      </div>

      {/* Grid de contenidos */}
      <div className="px-4 lg:px-6 py-6">
        {list.contents.length === 0 ? (
          <EmptyState
            icon="🎬"
            title="Lista vacía"
            description={
              isOwner
                ? 'Pulsa "+ Añadir" para agregar títulos a esta lista.'
                : 'Esta lista aún no tiene ningún título.'
            }
          />
        ) : (
          <div className="flex flex-wrap gap-3">
            {list.contents.map((content) => (
              <div key={content.id} className="relative group">
                <PosterCard content={content} />
                {isOwner && (
                  <button
                    onClick={() => removeContent(content.tmdbId)}
                    disabled={isRemoving}
                    className="absolute top-1.5 right-1.5 z-10 w-6 h-6 bg-red-500/80 hover:bg-red-500 rounded-full flex items-center justify-center text-white text-sm font-bold opacity-0 group-hover:opacity-100 transition-opacity disabled:opacity-50"
                    title="Eliminar de la lista"
                  >
                    ×
                  </button>
                )}
              </div>
            ))}
          </div>
        )}
      </div>

      {isOwner && (
        <AddContentToListDialog
          open={addDialogOpen}
          onClose={() => setAddDialogOpen(false)}
          listId={parsedId}
          listName={list.name}
          existingTmdbIds={new Set(list.contents.map((c) => c.tmdbId))}
        />
      )}
    </div>
  )
}
