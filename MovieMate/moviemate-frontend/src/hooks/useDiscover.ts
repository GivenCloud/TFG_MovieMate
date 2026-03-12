import { useQuery } from '@tanstack/react-query'
import { tmdbApi } from '@/api/tmdb'
import { queryKeys } from '@/lib/queryKeys'
import type { ContentResponse, ContentType } from '@/types'

function interleave<T>(a: T[], b: T[]): T[] {
  const result: T[] = []
  const max = Math.max(a.length, b.length)
  for (let i = 0; i < max; i++) {
    if (i < a.length) result.push(a[i])
    if (i < b.length) result.push(b[i])
  }
  return result
}

// Populares (sin búsqueda activa)
export function usePopular(filter: ContentType | 'ALL') {
  const trending = useQuery({
    queryKey: queryKeys.tmdb.trending(),
    queryFn: () => tmdbApi.getTrending(),
    select: (res) => res.data,
    enabled: filter === 'ALL',
    staleTime: 1000 * 60 * 10,
  })

  const movies = useQuery({
    queryKey: queryKeys.tmdb.popularMovies(),
    queryFn: () => tmdbApi.getPopularMovies(),
    select: (res) => res.data,
    enabled: filter === 'MOVIE',
    staleTime: 1000 * 60 * 10,
  })

  const tvShows = useQuery({
    queryKey: queryKeys.tmdb.popularTv(),
    queryFn: () => tmdbApi.getPopularTvShows(),
    select: (res) => res.data,
    enabled: filter === 'TV',
    staleTime: 1000 * 60 * 10,
  })

  let data: ContentResponse[]
  let isLoading: boolean

  if (filter === 'ALL') {
    data = trending.data ?? []
    isLoading = trending.isLoading
  } else if (filter === 'MOVIE') {
    data = movies.data ?? []
    isLoading = movies.isLoading
  } else {
    data = tvShows.data ?? []
    isLoading = tvShows.isLoading
  }

  return { data, isLoading }
}

// Resultados de búsqueda
export function useSearch(query: string, filter: ContentType | 'ALL') {
  const movies = useQuery({
    queryKey: queryKeys.tmdb.searchMovies(query),
    queryFn: () => tmdbApi.searchMovies(query),
    select: (res) => res.data,
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

  let data: ContentResponse[]
  if (filter === 'MOVIE') {
    data = movies.data ?? []
  } else if (filter === 'TV') {
    data = tvShows.data ?? []
  } else {
    // ALL: interleave para mostrar mezcla de películas y series
    data = interleave(movies.data ?? [], tvShows.data ?? [])
  }

  return {
    data,
    isLoading: movies.isLoading || tvShows.isLoading,
  }
}
