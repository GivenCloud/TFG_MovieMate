import apiClient from '../lib/apiClient'
import type { AuthResponse, LoginRequest, RegisterRequest, UserResponse } from '../types'

export const authApi = {
  login: (data: LoginRequest) =>
    apiClient.post<AuthResponse>('/auth/login', data),

  register: (data: RegisterRequest) =>
    apiClient.post<AuthResponse>('/auth/register', data),

  getMe: () =>
    apiClient.get<UserResponse>('/users/me'),
}