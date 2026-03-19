import apiClient from '../lib/apiClient'
import type { CommentResponse, CommentRequest, ListCommentResponse } from '../types'

export const commentsApi = {
  getByRating: (ratingId: number) =>
    apiClient.get<CommentResponse[]>(`/ratings/${ratingId}/comments`),

  create: (ratingId: number, data: CommentRequest) =>
    apiClient.post<CommentResponse>(`/ratings/${ratingId}/comments`, data),

  delete: (ratingId: number, commentId: number) =>
    apiClient.delete(`/ratings/${ratingId}/comments/${commentId}`),

  getByList: (listId: number) =>
    apiClient.get<ListCommentResponse[]>(`/lists/${listId}/comments`),

  createForList: (listId: number, data: CommentRequest) =>
    apiClient.post<ListCommentResponse>(`/lists/${listId}/comments`, data),

  deleteFromList: (listId: number, commentId: number) =>
    apiClient.delete(`/lists/${listId}/comments/${commentId}`),
}
