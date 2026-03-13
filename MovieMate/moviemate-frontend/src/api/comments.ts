import apiClient from '../lib/apiClient'
import type { CommentResponse, CommentRequest } from '../types'

export const commentsApi = {
  getByRating: (ratingId: number) =>
    apiClient.get<CommentResponse[]>(`/ratings/${ratingId}/comments`),

  create: (ratingId: number, data: CommentRequest) =>
    apiClient.post<CommentResponse>(`/ratings/${ratingId}/comments`, data),

  delete: (ratingId: number, commentId: number) =>
    apiClient.delete(`/ratings/${ratingId}/comments/${commentId}`),
}
