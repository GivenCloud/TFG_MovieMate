import apiClient from '../lib/apiClient'
import type {
  UserResponse,
  UserProfileResponse,
  UserStatsResponse,
  UpdateProfileRequest,
  FollowRequestDto,
  FollowRequestActionResponse,
  NotificationDto,
  RatingResponse,
  ListResponse,
} from '../types'

export const usersApi = {
  // Perfil propio — devuelve UserResponse
  getMe: () =>
    apiClient.get<UserResponse>('/users/me'),

  // Perfil por username — GET /api/users/username/{username}
  getByUsername: (username: string) =>
    apiClient.get<UserResponse>(`/users/username/${username}`),

  // Perfil completo por ID — GET /api/users/{userId} → UserProfileResponse
  getProfileById: (userId: number) =>
    apiClient.get<UserProfileResponse>(`/users/${userId}`),

  // Buscar usuarios — GET /api/users?q=...
  search: (q: string) =>
    apiClient.get<UserResponse[]>('/users', { params: { q } }),

  // Sugerencias — GET /api/users/suggestions
  getSuggestions: () =>
    apiClient.get<UserResponse[]>('/users/suggestions'),

  // Stats — GET /api/users/{userId}/stats
  getStats: (userId: number) =>
    apiClient.get<UserStatsResponse>(`/users/${userId}/stats`),

  // Actualizar perfil — PUT /api/users/me/profile
  updateProfile: (data: UpdateProfileRequest) =>
    apiClient.put<UserResponse>('/users/me/profile', data),

  // Actualizar visibilidad — PUT /api/users/me/public-status
  updatePublicStatus: (isPublic: boolean) =>
    apiClient.put<UserResponse>('/users/me/public-status', { isPublic }),

  // Seguir — POST /api/users/{userId}/follow-requests
  follow: (userId: number) =>
    apiClient.post<void>(`/users/${userId}/follow-requests`),

  // Dejar de seguir — DELETE /api/users/{userId}/followers
  unfollow: (userId: number) =>
    apiClient.delete(`/users/${userId}/followers`),

  // Estado de seguimiento — GET /api/users/{userId}/following-status
  getFollowingStatus: (userId: number) =>
    apiClient.get<boolean>(`/users/${userId}/following-status`),

  // Seguidores / seguidos
  getFollowers: (userId: number) =>
    apiClient.get<UserResponse[]>(`/users/${userId}/followers`),

  getFollowing: (userId: number) =>
    apiClient.get<UserResponse[]>(`/users/${userId}/following`),

  // Solicitudes de seguimiento recibidas
  getFollowRequests: () =>
    apiClient.get<FollowRequestDto[]>('/users/me/follow-requests'),

  // Aceptar solicitud — POST /api/users/follow-requests/{requestId}
  acceptFollowRequest: (requestId: number) =>
    apiClient.post<FollowRequestActionResponse>(`/users/follow-requests/${requestId}`),

  // Rechazar solicitud — DELETE /api/users/follow-requests/{requestId}
  rejectFollowRequest: (requestId: number) =>
    apiClient.delete<FollowRequestActionResponse>(`/users/follow-requests/${requestId}`),

  // Mis listas y ratings
  getMyLists: () =>
    apiClient.get<ListResponse[]>('/users/me/lists'),

  getMyRatings: () =>
    apiClient.get<RatingResponse[]>('/users/me/ratings'),

  // Ratings y listas de otro usuario (público o seguido)
  getRatingsByUserId: (userId: number) =>
    apiClient.get<RatingResponse[]>(`/users/${userId}/ratings`),

  getListsByUserId: (userId: number) =>
    apiClient.get<ListResponse[]>(`/users/${userId}/lists`),

  // Notificaciones — GET /api/users/me/notifications
  getNotifications: () =>
    apiClient.get<NotificationDto[]>('/users/me/notifications'),
}