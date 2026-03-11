import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import { tmdbApi } from '@/api/tmdb'
import { ratingsApi } from '@/api/ratings'
import { queryKeys } from '@/lib/queryKeys'
import type { ContentResponse, RatingRequest } from '@/types'

// Carga el contenido por sync cuando no viene por location.state
export function useSyncContent(
  tmdbId: number,
  contentType: 'MOVIE' | 'TV',
  enabled: boolean           // false si ya tenemos el content en state
) {
  return useQuery({
    queryKey: queryKeys.tmdb.sync(tmdbId, contentType),
    queryFn: () =>
      contentType === 'MOVIE'
        ? tmdbApi.syncMovie(tmdbId)
        : tmdbApi.syncTvShow(tmdbId),
    select: (res) => res.data,
    enabled,
    staleTime: 1000 * 60 * 10,
  })
}

// Reseñas del contenido (necesita el contentId de BD)
export function useReviews(contentId: number | undefined) {
  return useQuery({
    queryKey: queryKeys.ratings.byContent(contentId!),
    queryFn: () => ratingsApi.getByContent(contentId!),
    select: (res) => res.data,
    enabled: !!contentId,
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
      // Invalida las reseñas del contenido y mis ratings
      if (content.id) {
        queryClient.invalidateQueries({ queryKey: queryKeys.ratings.byContent(content.id) })
      }
      queryClient.invalidateQueries({ queryKey: queryKeys.users.ratings() })
      toast.success('Valoración guardada')
    },
    onError: (err: any) => {
      toast.error(err?.response?.data?.message || 'Error al guardar la valoración')
    },
  })
}