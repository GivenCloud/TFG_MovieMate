import apiClient from '../lib/apiClient'

export const notificationsApi = {
  // Marcar una como leída — PATCH /api/notifications/{id}/read
  markAsRead: (id: number) =>
    apiClient.patch(`/notifications/${id}/read`),

  // Marcar todas como leídas — PATCH /api/notifications/read-all
  markAllAsRead: () =>
    apiClient.patch('/notifications/read-all'),

  // Contar no leídas — GET /api/notifications/unread-count
  getUnreadCount: () =>
    apiClient.get('/notifications/unread-count'),
}