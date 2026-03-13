import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { commentsApi } from '../api/comments'
import { queryKeys } from '../lib/queryKeys'
import type { CommentRequest } from '../types'

export function useComments(ratingId: number | undefined) {
  return useQuery({
    queryKey: queryKeys.comments.byRating(ratingId!),
    queryFn: () => commentsApi.getByRating(ratingId!).then((r) => r.data),
    enabled: !!ratingId,
    staleTime: 1000 * 60,
  })
}

export function useCreateComment(ratingId: number) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (data: CommentRequest) => commentsApi.create(ratingId, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.comments.byRating(ratingId) })
    },
  })
}

export function useDeleteComment(ratingId: number) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (commentId: number) => commentsApi.delete(ratingId, commentId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.comments.byRating(ratingId) })
    },
  })
}
