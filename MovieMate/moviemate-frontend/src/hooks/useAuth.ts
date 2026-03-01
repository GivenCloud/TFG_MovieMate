import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import { authApi } from '../api/auth'
import { useAuthStore } from '../store/authStore'
import { queryKeys } from '../lib/queryKeys'
import type { LoginRequest, RegisterRequest } from '../types'

export function useLogin() {
  const { setAuth } = useAuthStore()
  const navigate = useNavigate()
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (data: LoginRequest) => authApi.login(data),
    onSuccess: ({ data }) => {
      setAuth(data.username, data.email, data.token)
      // Limpia la caché anterior (por si había otro usuario)
      queryClient.clear()
      navigate('/')
    },
  })
}

export function useRegister() {
  const { setAuth } = useAuthStore()
  const navigate = useNavigate()

  return useMutation({
    mutationFn: (data: RegisterRequest) => authApi.register(data),
    onSuccess: ({ data }) => {
      setAuth(data.username, data.email, data.token)
      navigate('/')
    },
  })
}

export function useLogout() {
  const { logout } = useAuthStore()
  const navigate = useNavigate()
  const queryClient = useQueryClient()

  return () => {
    logout()
    queryClient.clear()
    navigate('/login')
  }
}

// Hook para obtener el perfil completo del usuario autenticado
// Solo fetcha si hay sesión activa
export function useMyProfile() {
  const { isAuthenticated } = useAuthStore()

  return useQuery({
    queryKey: queryKeys.users.me(),
    queryFn: () => authApi.getMe(),
    enabled: isAuthenticated,
    select: (res) => res.data,
    // El perfil no cambia frecuentemente — 10 min de stale time
    staleTime: 1000 * 60 * 10,
  })
}