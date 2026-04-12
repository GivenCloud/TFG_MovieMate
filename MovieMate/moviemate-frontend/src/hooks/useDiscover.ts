import { useMemo } from 'react'
import { useQuery } from '@tanstack/react-query'
import { tmdbApi } from '@/api/tmdb'
import type { DiscoverParams } from '@/api/tmdb'
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
export function usePopular(filter: ContentType | 'ALL', page = 1) {
  const trending = useQuery({
    queryKey: [...queryKeys.tmdb.trending(), page],
    queryFn: () => tmdbApi.getTrending(page),
    select: (res) => res.data,
    enabled: filter === 'ALL',
    staleTime: 1000 * 60 * 10,
  })

  const movies = useQuery({
    queryKey: [...queryKeys.tmdb.popularMovies(), page],
    queryFn: () => tmdbApi.getPopularMovies(page),
    select: (res) => res.data,
    enabled: filter === 'MOVIE',
    staleTime: 1000 * 60 * 10,
  })

  const tvShows = useQuery({
    queryKey: [...queryKeys.tmdb.popularTv(), page],
    queryFn: () => tmdbApi.getPopularTvShows(page),
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

// Géneros (caché larga, raramente cambian)
export function useGenres(filter: ContentType | 'ALL') {
  const movies = useQuery({
    queryKey: queryKeys.tmdb.genresMovies(),
    queryFn: () => tmdbApi.getMovieGenres().then((r) => r.data),
    enabled: filter === 'MOVIE' || filter === 'ALL',
    staleTime: 1000 * 60 * 60 * 24, // 24h
  })

  const tv = useQuery({
    queryKey: queryKeys.tmdb.genresTv(),
    queryFn: () => tmdbApi.getTvGenres().then((r) => r.data),
    enabled: filter === 'TV',
    staleTime: 1000 * 60 * 60 * 24,
  })

  return filter === 'TV' ? tv.data ?? [] : movies.data ?? []
}

// Discover con filtros avanzados (reemplaza popular cuando hay filtros activos o tipo concreto)
export function useDiscover(filter: ContentType | 'ALL', params: DiscoverParams, active = true) {
  const movies = useQuery({
    queryKey: queryKeys.tmdb.discoverMovies(params),
    queryFn: () => tmdbApi.discoverMovies(params).then((r) => r.data),
    enabled: active && (filter === 'MOVIE' || filter === 'ALL'),
    staleTime: 1000 * 60 * 5,
  })

  const tv = useQuery({
    queryKey: queryKeys.tmdb.discoverTv(params),
    queryFn: () => tmdbApi.discoverTvShows(params).then((r) => r.data),
    enabled: active && (filter === 'TV' || filter === 'ALL'),
    staleTime: 1000 * 60 * 5,
  })

  // useMemo garantiza referencia estable: sin esto, interleave crea un array nuevo en cada
  // render aunque los datos no hayan cambiado, causando loops infinitos en el efecto de acumulación.
  const interleavedAll = useMemo(
    () => interleave(movies.data ?? [], tv.data ?? []),
    [movies.data, tv.data],
  )

  let data: ContentResponse[]
  let isLoading: boolean

  if (filter === 'MOVIE') {
    data = movies.data ?? []
    isLoading = movies.isLoading
  } else if (filter === 'TV') {
    data = tv.data ?? []
    isLoading = tv.isLoading
  } else {
    data = interleavedAll
    isLoading = movies.isLoading || tv.isLoading
  }

  return { data, isLoading }
}

// Resultados de búsqueda
export function useSearch(query: string, filter: ContentType | 'ALL', page = 1) {
  const movies = useQuery({
    queryKey: [...queryKeys.tmdb.searchMovies(query), page],
    queryFn: () => tmdbApi.searchMovies(query, page),
    select: (res) => res.data,
    enabled: query.length >= 2 && filter !== 'TV',
    staleTime: 1000 * 60 * 5,
  })

  const tvShows = useQuery({
    queryKey: [...queryKeys.tmdb.searchTv(query), page],
    queryFn: () => tmdbApi.searchTvShows(query, page),
    select: (res) => res.data,
    enabled: query.length >= 2 && filter !== 'MOVIE',
    staleTime: 1000 * 60 * 5,
  })

  const interleavedAll = useMemo(
    () => interleave(movies.data ?? [], tvShows.data ?? []),
    [movies.data, tvShows.data],
  )

  let data: ContentResponse[]
  if (filter === 'MOVIE') {
    data = movies.data ?? []
  } else if (filter === 'TV') {
    data = tvShows.data ?? []
  } else {
    data = interleavedAll
  }

  return {
    data,
    isLoading: movies.isLoading || tvShows.isLoading,
  }
}
