import { useReviews } from '@/hooks/useDetail'
import { formatDate } from '@/lib/utils'
import type { ContentResponse } from '@/types'

const TAG_LABELS: Record<string, string> = {
  INCREIBLE:     '🤩 Increíble',
  RECOMENDADA:   '👍 Recomendada',
  ENTRETENIDA:   '😊 Entretenida',
  REGULAR:       '😐 Regular',
  DECEPCIONANTE: '😞 Decepcionante',
}

interface Props {
  content: ContentResponse
}

export default function ReviewList({ content }: Props) {
  const { data: reviews, isLoading } = useReviews(content.id)

  if (isLoading) {
    return (
      <div className="space-y-4">
        {[1, 2, 3].map((i) => (
          <div key={i} className="bg-bg-2 rounded-xl p-4 animate-pulse">
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

  return (
    <div className="space-y-4">
      {reviews.map((review) => (
        <div key={review.id} className="bg-bg-2 border border-white/[0.06] rounded-xl p-4">
          <div className="flex items-start justify-between gap-3 mb-3">
            <div className="flex items-center gap-2.5">
              {/* Avatar */}
              <div className="w-8 h-8 rounded-full bg-gradient-to-br from-accent to-pink-500 flex items-center justify-center text-xs font-bold text-bg-0 shrink-0">
                {review.user.username.charAt(0).toUpperCase()}
              </div>
              <div>
                <p className="text-sm font-semibold text-white/90">{review.user.username}</p>
                <p className="text-xs text-muted">{formatDate(review.createdAt)}</p>
              </div>
            </div>
            {/* Puntuación */}
            <div className="flex items-center gap-0.5 shrink-0">
              {[1, 2, 3, 4, 5].map((n) => (
                <span
                  key={n}
                  className={`text-xs ${n <= review.rating ? 'text-yellow-400' : 'text-white/15'}`}
                >
                  ★
                </span>
              ))}
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
      ))}
    </div>
  )
}