import apiClient from '../lib/apiClient'
import type { ContentResponse } from '../types'

export interface GenreDto {
  id: number
  name: string
}

export interface DiscoverParams {
  genre?: number
  year?: number
  minRating?: number
  sortBy?: string
  page?: number
}

// TmdbController devuelve la entidad Content directamente
// que tiene la misma forma que ContentResponse
export const tmdbApi = {
  // Películas populares — para la HomePage y DiscoverPage
  getPopularMovies: (page = 1) =>
    apiClient.get<ContentResponse[]>('/tmdb/movies/popular', { params: { page } }),

  // Series populares
  getPopularTvShows: (page = 1) =>
    apiClient.get<ContentResponse[]>('/tmdb/tv/popular', { params: { page } }),

  // Búsqueda de películas
  searchMovies: (query: string, page = 1) =>
    apiClient.get<ContentResponse[]>('/tmdb/movies', { params: { query, page } }),

  // Búsqueda de series
  searchTvShows: (query: string, page = 1) =>
    apiClient.get<ContentResponse[]>('/tmdb/tv', { params: { query, page } }),

  // Trending (mix películas + series) — GET /api/tmdb/trending
  getTrending: (page = 1) =>
    apiClient.get<ContentResponse[]>('/tmdb/trending', { params: { page } }),

  // Géneros
  getMovieGenres: () =>
    apiClient.get<GenreDto[]>('/tmdb/genres/movies'),

  getTvGenres: () =>
    apiClient.get<GenreDto[]>('/tmdb/genres/tv'),

  // Discover con filtros
  discoverMovies: (params: DiscoverParams = {}) =>
    apiClient.get<ContentResponse[]>('/tmdb/discover/movies', {
      params: {
        genre: params.genre,
        year: params.year,
        minRating: params.minRating,
        sortBy: params.sortBy,
        page: params.page ?? 1,
      },
    }),

  discoverTvShows: (params: DiscoverParams = {}) =>
    apiClient.get<ContentResponse[]>('/tmdb/discover/tv', {
      params: {
        genre: params.genre,
        year: params.year,
        minRating: params.minRating,
        sortBy: params.sortBy,
        page: params.page ?? 1,
      },
    }),

  // Sync: obtiene detalles completos Y guarda en BD.
  // Se usa cuando DetailPage se abre por URL directa (sin location.state).
  syncMovie: (tmdbId: number) =>
    apiClient.post<ContentResponse>(`/tmdb/movies/${tmdbId}/sync`),

  syncTvShow: (tmdbId: number) =>
    apiClient.post<ContentResponse>(`/tmdb/tv/${tmdbId}/sync`),
}