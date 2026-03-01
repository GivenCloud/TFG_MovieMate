import apiClient from '../lib/apiClient'
import type { ContentResponse } from '../types'

export const contentApi = {
  // Obtiene un contenido por su ID interno de BD
  // Se usa al navegar a la ficha de detalle desde una valoración o lista
  getById: (contentId: number) =>
    apiClient.get<ContentResponse>(`/content/${contentId}`),

  // Obtiene todo el contenido en BD (caché local)
  getAll: () =>
    apiClient.get<ContentResponse[]>('/content'),
}