import { useState } from 'react'
import { useReviews, useToggleLike } from '@/hooks/useDetail'
import { useAuthStore } from '@/store/authStore'
import { formatDate } from '@/lib/utils'
import type { ContentResponse, RatingResponse } from '@/types'

const PAGE_SIZE = 5

const TAG_LABELS: Record<string, string> = {
  INCREIBLE:     '🤩 Increíble',
  RECOMENDADA:   '👍 Recomendada',
  ENTRETENIDA:   '😊 Entretenida',
  REGULAR:       '😐 Regular',
  DECEPCIONANTE: '😞 Decepcionante',
}

// ── Botón de like por reseña ─────────────────────────────────
function LikeButton({
  review,
  contentId,
}: {
  review: RatingResponse
  contentId: number | undefined
}) {
  const { isAuthenticated } = useAuthStore()
  const { mutate: toggleLike, isPending } = useToggleLike(review.id, contentId)

  const likesCount = review.likesCount ?? 0
  const isLiked = review.likedByCurrentUser ?? false

  return (
    <button
      onClick={() => isAuthenticated && toggleLike()}
      disabled={isPending || !isAuthenticated}
      title={!isAuthenticated ? 'Inicia sesión para dar me gusta' : undefined}
      className={`flex items-center gap-1 text-xs rounded-lg px-2 py-1 transition-colors disabled:cursor-default
        ${isLiked
          ? 'text-red-400 bg-red-500/10 hover:bg-red-500/15'
          : isAuthenticated
            ? 'text-muted hover:text-white hover:bg-white/[0.06]'
            : 'text-muted/40'
        }`}
    >
      <span>{isLiked ? '❤️' : '🤍'}</span>
      {likesCount > 0 && <span className="font-mono">{likesCount}</span>}
    </button>
  )
}

// ── Tarjeta de reseña ────────────────────────────────────────
function ReviewCard({
  review,
  contentId,
}: {
  review: RatingResponse
  contentId: number | undefined
}) {
  return (
    <div className="bg-bg-2 border border-white/[0.06] rounded-xl p-3">
      <div className="flex items-start justify-between gap-3 mb-2">
        <div className="flex items-center gap-2.5">
          {/* Avatar */}
          <div className="w-8 h-8 rounded-full bg-gradient-to-br from-accent to-pink-500 flex items-center justify-center text-xs font-bold text-bg-0 shrink-0 overflow-hidden">
            {review.user.avatarUrl ? (
              <img
                src={review.user.avatarUrl}
                alt={review.user.username}
                className="w-full h-full object-cover"
              />
            ) : (
              review.user.username.charAt(0).toUpperCase()
            )}
          </div>
          <div>
            <p className="text-sm font-semibold text-white/90">{review.user.username}</p>
            <p className="text-xs text-muted">{formatDate(review.createdAt)}</p>
          </div>
        </div>

        {/* Puntuación + like */}
        <div className="flex items-center gap-2 shrink-0">
          <div className="flex items-center gap-0.5">
            {[1, 2, 3, 4, 5].map((n) => (
              <span
                key={n}
                className={`text-xs ${n <= review.rating ? 'text-yellow-400' : 'text-white/15'}`}
              >
                ★
              </span>
            ))}
          </div>
          <LikeButton review={review} contentId={contentId} />
        </div>
      </div>

      {/* Tag emocional */}
      {review.emotionalTag && (
        <span className="inline-block text-xs font-medium bg-accent/10 text-accent border border-accent/20 px-2 py-0.5 rounded-full mb-2">
          {TAG_LABELS[review.emotionalTag] ?? review.emotionalTag}
        </span>
      )}

      {/* Texto de reseña */}
      {review.reviewText && (
        <p className="text-sm text-white/70 leading-relaxed">{review.reviewText}</p>
      )}
    </div>
  )
}

// ── Componente principal ─────────────────────────────────────
interface Props {
  content: ContentResponse
}

export default function ReviewList({ content }: Props) {
  const { data: reviews, isLoading } = useReviews(content.id)
  const [visibleCount, setVisibleCount] = useState(PAGE_SIZE)

  if (isLoading) {
    return (
      <div className="space-y-2">
        {[1, 2, 3].map((i) => (
          <div key={i} className="bg-bg-2 rounded-xl p-3 animate-pulse">
            <div className="flex gap-3 mb-3">
              <div className="w-8 h-8 rounded-full bg-bg-3" />
              <div className="flex-1">
                <div className="h-3 bg-bg-3 rounded w-24 mb-2" />
                <div className="h-2.5 bg-bg-3 rounded w-16" />
              </div>
            </div>
            <div className="h-3 bg-bg-3 rounded w-full mb-1.5" />
            <div className="h-3 bg-bg-3 rounded w-3/4" />
          </div>
        ))}
      </div>
    )
  }

  if (!reviews || reviews.length === 0) {
    return (
      <div className="text-center py-12">
        <span className="text-4xl mb-3 block">💬</span>
        <p className="text-white/60 text-sm">Sé el primero en valorar este contenido</p>
      </div>
    )
  }

  const visible = reviews.slice(0, visibleCount)
  const hasMore = visibleCount < reviews.length
  const remaining = reviews.length - visibleCount

  return (
    <div className="space-y-2">
      {visible.map((review) => (
        <ReviewCard key={review.id} review={review} contentId={content.id} />
      ))}

      {/* Botón "Ver más" */}
      {hasMore && (
        <button
          onClick={() => setVisibleCount((c) => c + PAGE_SIZE)}
          className="w-full py-2.5 text-sm text-muted hover:text-white border border-white/[0.08] hover:border-white/[0.15] rounded-xl transition-colors"
        >
          Ver {Math.min(remaining, PAGE_SIZE)} más
          <span className="text-xs text-muted/60 ml-1">({remaining} restantes)</span>
        </button>
      )}
    </div>
  )
}
