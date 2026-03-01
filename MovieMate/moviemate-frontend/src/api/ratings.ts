import apiClient from '../lib/apiClient'
import type { RatingRequest, RatingResponse } from '../types'

export const ratingsApi = {
  // Ratings de un contenido — GET /api/ratings/{contentId}
  getByContent: (contentId: number) =>
    apiClient.get<RatingResponse[]>(`/ratings/${contentId}`),

  // Mis ratings — GET /api/users/me/ratings
  getMine: () =>
    apiClient.get<RatingResponse[]>('/users/me/ratings'),

  // Crea o actualiza (el backend lo gestiona solo con POST)
  createOrUpdate: (data: RatingRequest) =>
    apiClient.post<RatingResponse>('/ratings', data),

  delete: (ratingId: number) =>
    apiClient.delete(`/ratings/${ratingId}`),

  // Toggle like — un solo endpoint para dar y quitar like
  toggleLike: (ratingId: number) =>
    apiClient.post<void>(`/ratings/${ratingId}/likes`),

  getLikesCount: (ratingId: number) =>
    apiClient.get<number>(`/ratings/${ratingId}/likes`),

  hasLiked: (ratingId: number) =>
    apiClient.get<boolean>(`/ratings/${ratingId}/like-status`),
}