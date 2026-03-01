import apiClient from '../lib/apiClient'
import type { ActivityResponse } from '../types'

// Spring devuelve Page<ActivityResponse> — usamos el campo .content para los items
export const activityApi = {
  // Feed personal — GET /api/feed/personal
  getPersonalFeed: (page = 0, size = 20) =>
    apiClient.get<{ content: ActivityResponse[] }>('/feed/personal', { params: { page, size } }),

  // Feed global — GET /api/feed/global
  getGlobalFeed: (page = 0, size = 20) =>
    apiClient.get<{ content: ActivityResponse[] }>('/feed/global', { params: { page, size } }),
}