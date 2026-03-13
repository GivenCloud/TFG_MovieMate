import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import { tmdbApi } from '@/api/tmdb'
import { ratingsApi } from '@/api/ratings'
import { queryKeys } from '@/lib/queryKeys'
import type { ContentResponse, RatingRequest } from '@/types'

// Carga el contenido sincronizando con BD para tener stats actualizadas (appRating, appVoteCount)
export function useSyncContent(
  tmdbId: number,
  contentType: 'MOVIE' | 'TV',
  enabled: boolean
) {
  return useQuery({
    queryKey: queryKeys.tmdb.sync(tmdbId, contentType),
    queryFn: () =>
      contentType === 'MOVIE'
        ? tmdbApi.syncMovie(tmdbId)
        : tmdbApi.syncTvShow(tmdbId),
    select: (res) => res.data,
    enabled,
    staleTime: 0,         // siempre refresca al montar para tener appRating/appVoteCount frescos
    gcTime: 1000 * 60 * 5,
  })
}

// Reseñas del contenido (por contentId de BD)
export function useReviews(contentId: number | undefined) {
  return useQuery({
    queryKey: queryKeys.ratings.byContent(contentId!),
    queryFn: () => ratingsApi.getByContent(contentId!),
    select: (res) => res.data,
    enabled: !!contentId,
    staleTime: 1000 * 60 * 2,
  })
}

// Valoración del usuario autenticado para este contenido concreto
// Filtra del array de mis ratings usando el contentId de BD
export function useMyRatingForContent(contentId: number | undefined, enabled: boolean) {
  return useQuery({
    queryKey: queryKeys.users.ratings(),
    queryFn: () => ratingsApi.getMine().then((r) => r.data),
    select: (ratings) => ratings.find((r) => r.content.id === contentId),
    enabled: enabled && !!contentId,
    staleTime: 1000 * 60 * 2,
  })
}

// Crear o actualizar valoración
export function useCreateRating(content: ContentResponse) {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (data: Omit<RatingRequest, 'tmdbId' | 'contentType'>) =>
      ratingsApi.createOrUpdate({
        ...data,
        tmdbId: content.tmdbId,
        contentType: content.contentType,
      }),
    onSuccess: () => {
      if (content.id) {
        queryClient.invalidateQueries({ queryKey: queryKeys.ratings.byContent(content.id) })
      }
      queryClient.invalidateQueries({ queryKey: queryKeys.users.ratings() })
      // Invalidar sync para que appRating/appVoteCount se actualicen en el aside
      queryClient.invalidateQueries({ queryKey: queryKeys.tmdb.sync(content.tmdbId, content.contentType) })
      toast.success('Valoración guardada')
    },
    onError: (err: any) => {
      toast.error(err?.response?.data?.message || 'Error al guardar la valoración')
    },
  })
}

// Eliminar valoración
export function useDeleteRating(contentId: number | undefined) {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (ratingId: number) => ratingsApi.delete(ratingId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.users.ratings() })
      if (contentId) {
        queryClient.invalidateQueries({ queryKey: queryKeys.ratings.byContent(contentId) })
      }
      // Invalida todos los sync de contenido para refrescar appRating/appVoteCount
      queryClient.invalidateQueries({ queryKey: ['tmdb', 'sync'] })
      toast.success('Valoración eliminada')
    },
    onError: (err: any) => {
      toast.error(err?.response?.data?.message || 'Error al eliminar la valoración')
    },
  })
}

// Toggle like en una reseña
export function useToggleLike(ratingId: number, contentId: number | undefined) {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: () => ratingsApi.toggleLike(ratingId),
    onSuccess: () => {
      if (contentId) {
        queryClient.invalidateQueries({ queryKey: queryKeys.ratings.byContent(contentId) })
      }
    },
  })
}
