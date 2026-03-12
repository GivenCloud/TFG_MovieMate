import apiClient from '../lib/apiClient'
import type { ContentResponse } from '../types'

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

  // Sync: obtiene detalles completos Y guarda en BD.
  // Se usa cuando DetailPage se abre por URL directa (sin location.state).
  syncMovie: (tmdbId: number) =>
    apiClient.post<ContentResponse>(`/tmdb/movies/${tmdbId}/sync`),

  syncTvShow: (tmdbId: number) =>
    apiClient.post<ContentResponse>(`/tmdb/tv/${tmdbId}/sync`),
}