import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useReviews, useToggleLike } from '@/hooks/useDetail'
import { useComments, useCreateComment, useDeleteComment } from '@/hooks/useComments'
import { useAuthStore } from '@/store/authStore'
import { formatDate } from '@/lib/utils'
import ReportDialog from '@/features/moderation/ReportDialog'
import type { ContentResponse, RatingResponse } from '@/types'

const PAGE_SIZE = 5

const TAG_LABELS: Record<string, string> = {
  INCREIBLE:     '🤩 Increíble',
  RECOMENDADA:   '👍 Recomendada',
  ENTRETENIDA:   '😊 Entretenida',
  REGULAR:       '😐 Regular',
  DECEPCIONANTE: '😞 Decepcionante',
}

// ── Botón de like ─────────────────────────────────────────────
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
      className={`flex items-center gap-1.5 text-xs rounded-lg px-2 py-1 transition-all disabled:cursor-default
        ${isLiked
          ? 'text-red-400 bg-red-500/20 border border-red-500/30 hover:bg-red-500/25'
          : isAuthenticated
            ? 'text-white/40 border border-transparent hover:text-white/70 hover:bg-white/[0.06]'
            : 'text-white/20 border border-transparent'
        }`}
    >
      <svg
        xmlns="http://www.w3.org/2000/svg"
        className="w-3.5 h-3.5"
        viewBox="0 0 24 24"
        fill={isLiked ? 'currentColor' : 'none'}
        stroke="currentColor"
        strokeWidth={isLiked ? 0 : 2}
      >
        <path strokeLinecap="round" strokeLinejoin="round" d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z" />
      </svg>
      {likesCount > 0 && <span className="font-mono tabular-nums">{likesCount}</span>}
    </button>
  )
}

// ── Sección de comentarios ────────────────────────────────────
function CommentSection({ ratingId }: { ratingId: number }) {
  const { isAuthenticated, sessionUser } = useAuthStore()
  const { data: comments = [], isLoading } = useComments(ratingId)
  const { mutate: createComment, isPending: isCreating } = useCreateComment(ratingId)
  const { mutate: deleteComment } = useDeleteComment(ratingId)
  const [text, setText] = useState('')

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    const trimmed = text.trim()
    if (!trimmed || trimmed.length > 1000) return
    createComment({ content: trimmed }, { onSuccess: () => setText('') })
  }

  return (
    <div className="mt-2 pt-2 border-t border-white/[0.06] space-y-2">
      {isLoading ? (
        <div className="text-xs text-muted animate-pulse px-1">Cargando comentarios…</div>
      ) : comments.length > 0 ? (
        <div className="space-y-1.5">
          {comments.map((c) => (
            <div key={c.id} className="flex items-start gap-2 group">
              {/* Avatar */}
              <Link to={`/profile/${c.author.username}`} className="shrink-0">
                <div className="w-6 h-6 rounded-full bg-gradient-to-br from-accent to-pink-500 flex items-center justify-center text-[0.6rem] font-bold text-bg-0 overflow-hidden">
                  {c.author.avatarUrl ? (
                    <img src={c.author.avatarUrl} alt={c.author.username} className="w-full h-full object-cover" />
                  ) : (
                    c.author.username.charAt(0).toUpperCase()
                  )}
                </div>
              </Link>
              <div className="flex-1 min-w-0">
                <div className="flex items-baseline gap-1.5 flex-wrap">
                  <Link
                    to={`/profile/${c.author.username}`}
                    className="text-xs font-semibold text-white/90 hover:text-accent transition-colors"
                  >
                    {c.author.username}
                  </Link>
                  <span className="text-[0.65rem] text-muted">{formatDate(c.createdAt)}</span>
                </div>
                <p className="text-xs text-white/70 leading-relaxed break-words">{c.content}</p>
              </div>
              {/* Botón eliminar (propio o admin) */}
              {isAuthenticated && sessionUser?.username === c.author.username && (
                <button
                  onClick={() => deleteComment(c.id)}
                  className="opacity-0 group-hover:opacity-100 text-muted hover:text-red-400 text-xs transition-all shrink-0 mt-0.5"
                  title="Eliminar comentario"
                >
                  ×
                </button>
              )}
            </div>
          ))}
        </div>
      ) : (
        <p className="text-xs text-muted/60 px-1">Sin comentarios todavía.</p>
      )}

      {/* Formulario */}
      {isAuthenticated ? (
        <form onSubmit={handleSubmit} className="flex gap-2 items-end">
          <textarea
            value={text}
            onChange={(e) => setText(e.target.value)}
            placeholder="Escribe un comentario…"
            rows={1}
            maxLength={1000}
            className="flex-1 bg-bg-0 border border-white/[0.08] rounded-lg px-3 py-1.5 text-xs text-white placeholder:text-muted/60 outline-none focus:border-accent/50 resize-none transition-colors"
          />
          <button
            type="submit"
            disabled={isCreating || !text.trim()}
            className="px-3 py-1.5 text-xs font-medium bg-accent text-bg-0 rounded-lg hover:bg-accent-light disabled:opacity-40 disabled:cursor-not-allowed transition-colors shrink-0"
          >
            {isCreating ? '…' : 'Enviar'}
          </button>
        </form>
      ) : (
        <p className="text-xs text-muted/60 px-1">
          <Link to="/login" className="text-accent hover:underline">Inicia sesión</Link> para comentar.
        </p>
      )}
    </div>
  )
}

// ── Tarjeta de reseña ─────────────────────────────────────────
function ReviewCard({
  review,
  contentId,
  expanded,
  onToggleComments,
}: {
  review: RatingResponse
  contentId: number | undefined
  expanded: boolean
  onToggleComments: (id: number) => void
}) {
  const { isAuthenticated, sessionUser } = useAuthStore()
  const [reportOpen, setReportOpen] = useState(false)
  const isOwnReview = sessionUser?.username === review.user.username

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

      {/* Botón comentarios + reportar */}
      <div className="mt-2 flex items-center gap-3">
        <button
          onClick={() => onToggleComments(review.id)}
          className="text-xs text-muted hover:text-white transition-colors flex items-center gap-1"
        >
          <span>💬</span>
          <span>{expanded ? 'Ocultar comentarios' : 'Comentarios'}</span>
        </button>
        {isAuthenticated && !isOwnReview && (
          <button
            onClick={() => setReportOpen(true)}
            className="text-xs text-muted/50 hover:text-red-400 transition-colors flex items-center gap-1"
            title="Reportar valoración"
          >
            <span>🚩</span>
          </button>
        )}
      </div>

      {/* Sección de comentarios expandible */}
      {expanded && <CommentSection ratingId={review.id} />}

      {/* Dialog de reporte */}
      {reportOpen && (
        <ReportDialog
          open={reportOpen}
          onClose={() => setReportOpen(false)}
          targetType="RATING"
          targetId={review.id}
        />
      )}
    </div>
  )
}

// ── Componente principal ──────────────────────────────────────
interface Props {
  content: ContentResponse
}

export default function ReviewList({ content }: Props) {
  const { data: reviews, isLoading } = useReviews(content.id)
  const [visibleCount, setVisibleCount] = useState(PAGE_SIZE)
  const [expandedRatingId, setExpandedRatingId] = useState<number | null>(null)

  const handleToggleComments = (id: number) => {
    setExpandedRatingId((prev) => (prev === id ? null : id))
  }

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
        <ReviewCard
          key={review.id}
          review={review}
          contentId={content.id}
          expanded={expandedRatingId === review.id}
          onToggleComments={handleToggleComments}
        />
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
