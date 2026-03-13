import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import { listsApi } from '../../api/lists'
import { queryKeys } from '../../lib/queryKeys'
import PosterCard from '../../components/shared/PosterdCard'
import EmptyState from '../../components/shared/EmptyState'
import AddContentToListDialog from './AddContentToListDialog'

type SpecialListType = 'WATCHLIST' | 'FAVORITES'

const CONFIG: Record<SpecialListType, {
  icon: string
  title: string
  subtitle: string
  emptyTitle: string
  emptyDesc: string
}> = {
  WATCHLIST: {
    icon: '🕐',
    title: 'Por ver',
    subtitle: 'Películas y series que quieres ver',
    emptyTitle: 'Tu lista de pendientes está vacía',
    emptyDesc: 'Busca una película o serie y añádela con el botón "Añadir a lista".',
  },
  FAVORITES: {
    icon: '❤️',
    title: 'Favoritos',
    subtitle: 'Tu colección de favoritos de siempre',
    emptyTitle: 'Aún no tienes favoritos',
    emptyDesc: 'Busca una película o serie y añádela con el botón "Añadir a lista".',
  },
}

function SpecialListSkeleton() {
  return (
    <div className="animate-pulse">
      <div className="px-6 py-5 border-b border-white/[0.06] space-y-2">
        <div className="h-7 bg-bg-3 rounded w-40" />
        <div className="h-3 bg-bg-3 rounded w-56 mt-1" />
      </div>
      <div className="px-6 py-6 flex flex-wrap gap-3">
        {[1, 2, 3, 4, 5, 6].map((i) => (
          <div key={i} className="w-36 aspect-[2/3] bg-bg-3 rounded-xl" />
        ))}
      </div>
    </div>
  )
}

export default function SpecialListPage({ listType }: { listType: SpecialListType }) {
  const queryClient = useQueryClient()
  const cfg = CONFIG[listType]
  const [addDialogOpen, setAddDialogOpen] = useState(false)

  const { data: lists = [], isLoading } = useQuery({
    queryKey: queryKeys.users.lists(),
    queryFn: () => listsApi.getMine().then((r) => r.data),
    staleTime: 1000 * 60 * 2,
  })

  const list = lists.find((l) => l.listType === listType)

  const { mutate: removeContent, isPending: isRemoving } = useMutation({
    mutationFn: (tmdbId: number) => {
      if (!list) return Promise.reject(new Error('Lista no disponible'))
      return listsApi.removeContent(list.id, tmdbId)
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.users.lists() })
      queryClient.invalidateQueries({ queryKey: queryKeys.lists.mine() })
      if (list) queryClient.invalidateQueries({ queryKey: queryKeys.lists.detail(list.id) })
      toast.success('Eliminado de la lista')
    },
    onError: () => toast.error('No se pudo eliminar'),
  })

  if (isLoading) return <SpecialListSkeleton />

  return (
    <div className="pb-12">
      {/* Cabecera */}
      <div className="px-4 lg:px-6 py-5 border-b border-white/[0.06]">
        <div className="flex items-center gap-3 mb-1">
          <span className="text-xl">{cfg.icon}</span>
          <h1 className="font-display font-bold italic text-2xl">{cfg.title}</h1>
          {list && (
            <button
              onClick={() => setAddDialogOpen(true)}
              className="ml-auto shrink-0 flex items-center gap-1.5 px-3.5 py-2 text-sm font-semibold bg-accent hover:bg-accent-light text-bg-0 rounded-xl transition-colors"
            >
              + Añadir
            </button>
          )}
        </div>
        <p className="text-sm text-muted mt-0.5">{cfg.subtitle}</p>
        <div className="flex items-center gap-3 mt-2 text-xs text-muted font-mono">
          <span>{list?.itemCount ?? 0} título{(list?.itemCount ?? 0) !== 1 ? 's' : ''}</span>
          {list && (
            <>
              <span>·</span>
              <span>{list.isPublic ? '🔓 Pública' : '🔒 Privada'}</span>
            </>
          )}
        </div>
      </div>

      {/* Contenido */}
      <div className="px-4 lg:px-6 py-6">
        {!list || list.contents.length === 0 ? (
          <EmptyState
            icon={cfg.icon}
            title={cfg.emptyTitle}
            description={cfg.emptyDesc}
          />
        ) : (
          <div className="flex flex-wrap gap-3">
            {list.contents.map((content) => (
              <div key={content.id} className="relative group">
                <PosterCard content={content} />
                <button
                  onClick={() => removeContent(content.tmdbId)}
                  disabled={isRemoving}
                  className="absolute top-1.5 right-1.5 z-10 w-6 h-6 bg-red-500/80 hover:bg-red-500 rounded-full flex items-center justify-center text-white text-sm font-bold opacity-0 group-hover:opacity-100 transition-opacity disabled:opacity-50"
                  title="Eliminar de la lista"
                >
                  ×
                </button>
              </div>
            ))}
          </div>
        )}
      </div>

      {list && (
        <AddContentToListDialog
          open={addDialogOpen}
          onClose={() => setAddDialogOpen(false)}
          listId={list.id}
          listName={list.name}
          existingTmdbIds={new Set(list.contents.map((c) => c.tmdbId))}
        />
      )}
    </div>
  )
}
