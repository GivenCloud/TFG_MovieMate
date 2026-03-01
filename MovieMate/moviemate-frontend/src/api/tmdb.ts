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
}