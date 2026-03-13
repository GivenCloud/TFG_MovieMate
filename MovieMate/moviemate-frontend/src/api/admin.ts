import apiClient from '../lib/apiClient'
import type { UserResponse, ReportResponse, RatingResponse, CommentResponse } from '../types'

export type ReportStatus = 'PENDING' | 'RESOLVED' | 'DISMISSED'
export type ReportReason = 'SPAM' | 'INAPPROPRIATE' | 'SPOILER' | 'OTHER'
export type TargetType = 'RATING' | 'COMMENT'

export interface ReportRequest {
  targetType: TargetType
  targetId: number
  reason: ReportReason
}

export const adminApi = {
  // Usuarios
  listUsers: (q?: string) =>
    apiClient.get<UserResponse[]>('/admin/users', { params: q ? { q } : undefined }),

  changeRole: (userId: number, role: 'USER' | 'ADMIN') =>
    apiClient.put<UserResponse>(`/admin/users/${userId}/role`, { role }),

  banUser: (userId: number, banned: boolean) =>
    apiClient.put<UserResponse>(`/admin/users/${userId}/ban`, { banned }),

  // Contenido
  getRating: (ratingId: number) =>
    apiClient.get<RatingResponse>(`/admin/ratings/${ratingId}`),

  deleteRating: (ratingId: number) =>
    apiClient.delete(`/admin/ratings/${ratingId}`),

  getComment: (commentId: number) =>
    apiClient.get<CommentResponse>(`/admin/comments/${commentId}`),

  deleteComment: (commentId: number) =>
    apiClient.delete(`/admin/comments/${commentId}`),

  // Reportes
  getReports: (status?: ReportStatus) =>
    apiClient.get<ReportResponse[]>('/admin/reports', { params: status ? { status } : undefined }),

  resolveReport: (reportId: number) =>
    apiClient.put<ReportResponse>(`/admin/reports/${reportId}/resolve`),

  dismissReport: (reportId: number) =>
    apiClient.put<ReportResponse>(`/admin/reports/${reportId}/dismiss`),
}

export const reportsApi = {
  create: (data: ReportRequest) =>
    apiClient.post<ReportResponse>('/reports', data),
}
