import { useQuery } from '@tanstack/react-query'
import { tmdbApi } from '@/api/tmdb'
import { queryKeys } from '@/lib/queryKeys'
import type { ContentType } from '@/types'

// Populares (sin búsqueda activa)
export function usePopular(filter: ContentType | 'ALL') {
  const movies = useQuery({
    queryKey: queryKeys.tmdb.popularMovies(),
    queryFn: () => tmdbApi.getPopularMovies(),
    select: (res) => res.data,
    enabled: filter === 'ALL' || filter === 'MOVIE',
    staleTime: 1000 * 60 * 10, // populares cambian poco — 10 min
  })

  const tvShows = useQuery({
    queryKey: queryKeys.tmdb.popularTv(),
    queryFn: () => tmdbApi.getPopularTvShows(),
    select: (res) => res.data,
    enabled: filter === 'ALL' || filter === 'TV',
    staleTime: 1000 * 60 * 10,
  })

  const combined = [
    ...(filter !== 'TV' ? (movies.data ?? []) : []),
    ...(filter !== 'MOVIE' ? (tvShows.data ?? []) : []),
  ]

  return {
    data: combined,
    isLoading: movies.isLoading || tvShows.isLoading,
  }
}

// Resultados de búsqueda
export function useSearch(query: string, filter: ContentType | 'ALL') {
  const movies = useQuery({
    queryKey: queryKeys.tmdb.searchMovies(query),
    queryFn: () => tmdbApi.searchMovies(query),
    select: (res) => res.data,
    // Solo fetcha si hay query Y el filtro incluye películas
    enabled: query.length >= 2 && filter !== 'TV',
    staleTime: 1000 * 60 * 5,
  })

  const tvShows = useQuery({
    queryKey: queryKeys.tmdb.searchTv(query),
    queryFn: () => tmdbApi.searchTvShows(query),
    select: (res) => res.data,
    enabled: query.length >= 2 && filter !== 'MOVIE',
    staleTime: 1000 * 60 * 5,
  })

  const combined = [
    ...(filter !== 'TV' ? (movies.data ?? []) : []),
    ...(filter !== 'MOVIE' ? (tvShows.data ?? []) : []),
  ]

  return {
    data: combined,
    isLoading: movies.isLoading || tvShows.isLoading,
  }
}