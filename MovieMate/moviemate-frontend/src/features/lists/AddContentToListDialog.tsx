import { useState, useEffect } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import { tmdbApi } from '../../api/tmdb'
import { listsApi } from '../../api/lists'
import { queryKeys } from '../../lib/queryKeys'
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from '../../components/ui/dialog'
import type { ContentResponse } from '../../types'

interface Props {
  open: boolean
  onClose: () => void
  listId: number
  listName: string
  existingTmdbIds: Set<number>
}

function useDebounce(value: string, delay: number) {
  const [debounced, setDebounced] = useState(value)
  useEffect(() => {
    const timer = setTimeout(() => setDebounced(value), delay)
    return () => clearTimeout(timer)
  }, [value, delay])
  return debounced
}

export default function AddContentToListDialog({
  open,
  onClose,
  listId,
  listName,
  existingTmdbIds,
}: Props) {
  const [query, setQuery] = useState('')
  const debouncedQuery = useDebounce(query.trim(), 300)
  const queryClient = useQueryClient()

  const { data: movies = [], isFetching: fetchingMovies } = useQuery({
    queryKey: queryKeys.tmdb.searchMovies(debouncedQuery),
    queryFn: () => tmdbApi.searchMovies(debouncedQuery).then((r) => r.data),
    enabled: debouncedQuery.length >= 2,
    staleTime: 1000 * 60 * 5,
  })

  const { data: tvShows = [], isFetching: fetchingTv } = useQuery({
    queryKey: queryKeys.tmdb.searchTv(debouncedQuery),
    queryFn: () => tmdbApi.searchTvShows(debouncedQuery).then((r) => r.data),
    enabled: debouncedQuery.length >= 2,
    staleTime: 1000 * 60 * 5,
  })

  const results: ContentResponse[] = []
  const seen = new Set<string>()
  for (const item of [...movies, ...tvShows]) {
    const key = `${item.tmdbId}-${item.contentType}`
    if (!seen.has(key)) {
      seen.add(key)
      results.push(item)
    }
  }

  const isFetching = fetchingMovies || fetchingTv

  const { mutate: addContent, isPending } = useMutation({
    mutationFn: (tmdbId: number) =>
      listsApi.addContent(listId, { tmdbId }),
    onSuccess: (_data, tmdbId) => {
      queryClient.invalidateQueries({ queryKey: queryKeys.lists.detail(listId) })
      queryClient.invalidateQueries({ queryKey: queryKeys.lists.mine() })
      queryClient.invalidateQueries({ queryKey: queryKeys.users.lists() })
      const title = results.find((r) => r.tmdbId === tmdbId)?.title ?? 'Título'
      toast.success(`"${title}" añadido a ${listName}`)
    },
    onError: (err: any) => {
      toast.error(err?.response?.data?.message ?? 'Error al añadir')
    },
  })

  const handleClose = () => {
    setQuery('')
    onClose()
  }

  return (
    <Dialog open={open} onOpenChange={(v) => !v && handleClose()}>
      <DialogContent className="bg-bg-1 border-white/[0.1] text-white max-w-lg">
        <DialogHeader>
          <DialogTitle className="font-display font-bold italic text-xl">
            Añadir contenido
          </DialogTitle>
        </DialogHeader>

        {/* Buscador */}
        <div className="relative mt-1">
          <span className="absolute left-3.5 top-1/2 -translate-y-1/2 text-muted text-sm">🔍</span>
          <input
            type="text"
            autoFocus
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            placeholder="Buscar película o serie…"
            className="w-full bg-bg-2 border border-white/[0.1] rounded-xl pl-9 pr-4 py-2.5 text-sm text-white placeholder:text-muted outline-none focus:border-accent/50 focus:ring-2 focus:ring-accent/10 transition-all"
          />
          {isFetching && (
            <span className="absolute right-3.5 top-1/2 -translate-y-1/2 text-xs text-muted animate-pulse">
              Buscando…
            </span>
          )}
        </div>

        {/* Resultados */}
        <div className="mt-2 max-h-80 overflow-y-auto space-y-1.5 pr-1 scrollbar-thin scrollbar-thumb-white/10">
          {debouncedQuery.length < 2 ? (
            <p className="text-center text-sm text-muted py-8">
              Escribe al menos 2 caracteres para buscar
            </p>
          ) : results.length === 0 && !isFetching ? (
            <p className="text-center text-sm text-muted py-8">
              Sin resultados para "{debouncedQuery}"
            </p>
          ) : (
            results.map((item) => {
              const alreadyAdded = existingTmdbIds.has(item.tmdbId)
              return (
                <div
                  key={`${item.tmdbId}-${item.contentType}`}
                  className="flex items-center gap-3 bg-bg-2 border border-white/[0.06] rounded-xl p-2.5"
                >
                  {/* Poster pequeño */}
                  <div className="w-9 h-[54px] rounded-lg overflow-hidden bg-bg-3 shrink-0">
                    {item.posterUrl ? (
                      <img
                        src={item.posterUrl}
                        alt={item.title}
                        className="w-full h-full object-cover"
                      />
                    ) : (
                      <div className="w-full h-full flex items-center justify-center text-lg">🎬</div>
                    )}
                  </div>

                  {/* Info */}
                  <div className="flex-1 min-w-0">
                    <p className="text-sm font-semibold text-white/90 line-clamp-1">{item.title}</p>
                    <p className="text-xs text-muted font-mono">
                      {item.contentType === 'MOVIE' ? 'Película' : 'Serie'}
                      {item.releaseDate ? ` · ${new Date(item.releaseDate).getFullYear()}` : ''}
                    </p>
                  </div>

                  {/* Botón */}
                  {alreadyAdded ? (
                    <span className="text-xs text-muted shrink-0">✓ En lista</span>
                  ) : (
                    <button
                      onClick={() => addContent(item.tmdbId)}
                      disabled={isPending}
                      className="shrink-0 px-3 py-1.5 text-xs font-semibold bg-accent hover:bg-accent-light text-bg-0 rounded-lg transition-colors disabled:opacity-50"
                    >
                      + Añadir
                    </button>
                  )}
                </div>
              )
            })
          )}
        </div>
      </DialogContent>
    </Dialog>
  )
}
