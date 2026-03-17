import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { usersApi } from '../api/users'
import { queryKeys } from '../lib/queryKeys'
import type { UpdateProfileRequest } from '../types'

// ── Consultas ─────────────────────────────────────────────────

export function useUserByUsername(username: string) {
  return useQuery({
    queryKey: queryKeys.users.byUsername(username),
    queryFn: () => usersApi.getByUsername(username).then((r) => r.data),
    enabled: !!username,
    staleTime: 1000 * 60 * 5,
  })
}

export function useUserProfile(userId: number | undefined) {
  return useQuery({
    queryKey: queryKeys.users.profileById(userId!),
    queryFn: () => usersApi.getProfileById(userId!).then((r) => r.data),
    enabled: !!userId,
    staleTime: 1000 * 60 * 5,
    // No reintentar si el perfil es privado (403)
    retry: (_count, error: any) => error?.response?.status !== 403,
  })
}

export function useUserStats(userId: number | undefined) {
  return useQuery({
    queryKey: queryKeys.users.stats(userId!),
    queryFn: () => usersApi.getStats(userId!).then((r) => r.data),
    enabled: !!userId,
    staleTime: 1000 * 60 * 5,
  })
}

export function useMyFullStats(enabled: boolean) {
  return useQuery({
    queryKey: queryKeys.users.fullStats(),
    queryFn: () => usersApi.getMyFullStats().then((r) => r.data),
    enabled,
    staleTime: 1000 * 60 * 5,
  })
}

export function useMyProfileRatings(enabled: boolean) {
  return useQuery({
    queryKey: queryKeys.users.ratings(),
    queryFn: () => usersApi.getMyRatings().then((r) => r.data),
    enabled,
    staleTime: 1000 * 60 * 2,
  })
}

export function useMyProfileLists(enabled: boolean) {
  return useQuery({
    queryKey: queryKeys.users.lists(),
    queryFn: () => usersApi.getMyLists().then((r) => r.data),
    enabled,
    staleTime: 1000 * 60 * 2,
  })
}

export function useUserRatings(userId: number | undefined, enabled: boolean) {
  return useQuery({
    queryKey: queryKeys.users.ratingsByUser(userId!),
    queryFn: () => usersApi.getRatingsByUserId(userId!).then((r) => r.data),
    enabled: enabled && !!userId,
    staleTime: 1000 * 60 * 2,
    retry: (_count, error: any) => error?.response?.status !== 403,
  })
}

export function useUserLists(userId: number | undefined, enabled: boolean) {
  return useQuery({
    queryKey: queryKeys.users.listsByUser(userId!),
    queryFn: () => usersApi.getListsByUserId(userId!).then((r) => r.data),
    enabled: enabled && !!userId,
    staleTime: 1000 * 60 * 2,
    retry: (_count, error: any) => error?.response?.status !== 403,
  })
}

export function useUserFollowing(userId: number | undefined, enabled: boolean) {
  return useQuery({
    queryKey: queryKeys.users.following(userId!),
    queryFn: () => usersApi.getFollowing(userId!).then((r) => r.data),
    enabled: enabled && !!userId,
    staleTime: 1000 * 60 * 2,
  })
}

export function useUserFollowers(userId: number | undefined, enabled: boolean) {
  return useQuery({
    queryKey: queryKeys.users.followers(userId!),
    queryFn: () => usersApi.getFollowers(userId!).then((r) => r.data),
    enabled: enabled && !!userId,
    staleTime: 1000 * 60 * 2,
  })
}

// ── Mutaciones ────────────────────────────────────────────────

export function useFollowUser(userId: number) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: () => usersApi.follow(userId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.users.profileById(userId) })
    },
  })
}

export function useUnfollowUser(userId: number) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: () => usersApi.unfollow(userId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.users.profileById(userId) })
    },
  })
}

export function useUpdateProfile(username: string, onSuccess?: () => void) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (data: UpdateProfileRequest) => usersApi.updateProfile(data),
    onSuccess: ({ data }) => {
      queryClient.setQueryData(queryKeys.users.me(), data)
      queryClient.invalidateQueries({ queryKey: queryKeys.users.byUsername(username) })
      onSuccess?.()
    },
  })
}
