import apiClient from '../lib/apiClient'
import type { ListRequest, ListResponse, AddToListRequest } from '../types'

export const listsApi = {
  // Mis listas — GET /api/users/me/lists
  getMine: () =>
    apiClient.get<ListResponse[]>('/users/me/lists'),

  // Listas públicas — GET /api/lists/public
  getPublic: () =>
    apiClient.get<ListResponse[]>('/lists/public'),

  create: (data: ListRequest) =>
    apiClient.post<ListResponse>('/lists', data),

  // Añadir contenido — POST /api/lists/{listId}/content
  addContent: (listId: number, data: AddToListRequest) =>
    apiClient.post<ListResponse>(`/lists/${listId}/content`, data),

  // Eliminar contenido — DELETE /api/lists/{listId}/content/{tmdbId}
  removeContent: (listId: number, tmdbId: number) =>
    apiClient.delete(`/lists/${listId}/content/${tmdbId}`),
}