import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { adminApi, reportsApi } from '../api/admin'
import type { ReportRequest } from '../api/admin'
import type { ReportStatus } from '../api/admin'

const ADMIN_KEYS = {
  users: (q?: string) => ['admin', 'users', q ?? ''] as const,
  reports: (status?: ReportStatus) => ['admin', 'reports', status ?? 'ALL'] as const,
}

// ── Usuarios ───────────────────────────────────────────────────

export function useAdminUsers(q?: string) {
  return useQuery({
    queryKey: ADMIN_KEYS.users(q),
    queryFn: () => adminApi.listUsers(q).then((r) => r.data),
    staleTime: 1000 * 30,
  })
}

export function useChangeRole() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ userId, role }: { userId: number; role: 'USER' | 'ADMIN' }) =>
      adminApi.changeRole(userId, role),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['admin', 'users'] }),
  })
}

export function useBanUser() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ userId, banned }: { userId: number; banned: boolean }) =>
      adminApi.banUser(userId, banned),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['admin', 'users'] }),
  })
}

// ── Reportes ───────────────────────────────────────────────────

export function useAdminReports(status?: ReportStatus) {
  return useQuery({
    queryKey: ADMIN_KEYS.reports(status),
    queryFn: () => adminApi.getReports(status).then((r) => r.data),
    staleTime: 1000 * 30,
  })
}

export function useResolveReport() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (reportId: number) => adminApi.resolveReport(reportId),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['admin', 'reports'] }),
  })
}

export function useDismissReport() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (reportId: number) => adminApi.dismissReport(reportId),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['admin', 'reports'] }),
  })
}

export function useAdminRating(id: number, enabled: boolean) {
  return useQuery({
    queryKey: ['admin', 'rating', id],
    queryFn: () => adminApi.getRating(id).then((r) => r.data),
    enabled,
    staleTime: 1000 * 60,
  })
}

export function useAdminComment(id: number, enabled: boolean) {
  return useQuery({
    queryKey: ['admin', 'comment', id],
    queryFn: () => adminApi.getComment(id).then((r) => r.data),
    enabled,
    staleTime: 1000 * 60,
  })
}

// ── Moderación de contenido ────────────────────────────────────

export function useAdminDeleteRating() {
  return useMutation({
    mutationFn: (ratingId: number) => adminApi.deleteRating(ratingId),
  })
}

export function useAdminDeleteComment() {
  return useMutation({
    mutationFn: (commentId: number) => adminApi.deleteComment(commentId),
  })
}

// ── Reportar contenido (usuario normal) ───────────────────────

export function useCreateReport() {
  return useMutation({
    mutationFn: (data: ReportRequest) => reportsApi.create(data),
  })
}
