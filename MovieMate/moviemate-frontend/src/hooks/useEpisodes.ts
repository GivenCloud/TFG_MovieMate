import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { episodesApi } from '@/api/episodes'
import { queryKeys } from '@/lib/queryKeys'
import { useAuthStore } from '@/store/authStore'

/** Resumen de temporadas de una serie (solo para TV) */
export function useTvSeasons(tmdbId: number | undefined) {
  return useQuery({
    queryKey: queryKeys.tmdb.tvSeasons(tmdbId!),
    queryFn: () => episodesApi.getTvSeasonsSummary(tmdbId!),
    enabled: !!tmdbId,
    staleTime: 1000 * 60 * 60, // 1h
  })
}

/** Episodios de una temporada concreta (lazy — se carga al abrir el acordeón) */
export function useSeasonDetail(tmdbId: number | undefined, seasonNumber: number | null) {
  return useQuery({
    queryKey: queryKeys.tmdb.seasonDetail(tmdbId!, seasonNumber!),
    queryFn: () => episodesApi.getSeasonDetails(tmdbId!, seasonNumber!),
    enabled: !!tmdbId && seasonNumber !== null,
    staleTime: 1000 * 60 * 60, // 1h
  })
}

/** Episodios vistos por el usuario autenticado para una serie */
export function useWatchedEpisodes(tmdbSeriesId: number | undefined) {
  const { isAuthenticated } = useAuthStore()
  return useQuery({
    queryKey: queryKeys.episodes.watched(tmdbSeriesId!),
    queryFn: () => episodesApi.getWatchedEpisodes(tmdbSeriesId!),
    enabled: !!tmdbSeriesId && isAuthenticated,
    staleTime: 1000 * 60 * 5, // 5 min
    select: (data) => new Set(data),
  })
}

/** Toggle episodio visto + actualización optimista */
export function useToggleEpisodeWatched(tmdbSeriesId: number) {
  const queryClient = useQueryClient()
  const watchedKey = queryKeys.episodes.watched(tmdbSeriesId)

  return useMutation({
    mutationFn: ({ seasonNumber, episodeNumber }: { seasonNumber: number; episodeNumber: number }) =>
      episodesApi.toggleEpisodeWatched(tmdbSeriesId, seasonNumber, episodeNumber),

    onMutate: async ({ seasonNumber, episodeNumber }) => {
      await queryClient.cancelQueries({ queryKey: watchedKey })
      const prev = queryClient.getQueryData<Set<string>>(watchedKey)
      const key = `${seasonNumber}-${episodeNumber}`

      queryClient.setQueryData<Set<string>>(watchedKey, (old = new Set()) => {
        const next = new Set(old)
        if (next.has(key)) next.delete(key)
        else next.add(key)
        return next
      })
      return { prev }
    },

    onError: (_err, _vars, context) => {
      if (context?.prev) queryClient.setQueryData(watchedKey, context.prev)
    },

    onSettled: () => {
      queryClient.invalidateQueries({ queryKey: watchedKey })
    },
  })
}

/** Marcar/desmarcar toda una temporada */
export function useToggleSeasonWatched(tmdbSeriesId: number) {
  const queryClient = useQueryClient()
  const watchedKey = queryKeys.episodes.watched(tmdbSeriesId)

  return useMutation({
    mutationFn: ({
      seasonNumber,
      episodeNumbers,
      markAll,
    }: {
      seasonNumber: number
      episodeNumbers: number[]
      markAll: boolean
    }) =>
      markAll
        ? episodesApi.markSeasonWatched(tmdbSeriesId, seasonNumber, episodeNumbers)
        : episodesApi.unmarkSeasonWatched(tmdbSeriesId, seasonNumber),

    onSettled: () => {
      queryClient.invalidateQueries({ queryKey: watchedKey })
    },
  })
}
