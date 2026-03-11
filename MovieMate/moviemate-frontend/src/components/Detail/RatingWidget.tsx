import { useState } from 'react'
import { cn } from '@/lib/utils'
import type { EmotionalTag, Status, ContentResponse } from '@/types'
import { useCreateRating } from '@/hooks/useDetail'
import StarRating from '@/components/shared/StarRating'

const EMOTIONAL_TAGS: { value: EmotionalTag; label: string; emoji: string }[] = [
  { value: 'INCREIBLE',     label: 'Increíble',     emoji: '🤩' },
  { value: 'RECOMENDADA',   label: 'Recomendada',   emoji: '👍' },
  { value: 'ENTRETENIDA',   label: 'Entretenida',   emoji: '😊' },
  { value: 'REGULAR',       label: 'Regular',       emoji: '😐' },
  { value: 'DECEPCIONANTE', label: 'Decepcionante', emoji: '😞' },
]

const STATUSES: { value: Status; label: string }[] = [
  { value: 'VISTA',       label: 'Vista' },
  { value: 'EN_PROGRESO', label: 'En progreso' },
  { value: 'PAUSADA',     label: 'Pausada' },
  { value: 'ABANDONADA',  label: 'Abandonada' },
  { value: 'POR_VER',     label: 'Por ver' },
]

interface Props {
  content: ContentResponse
}

export default function RatingWidget({ content }: Props) {
  const [rating, setRating] = useState(0)
  const [tag, setTag] = useState<EmotionalTag | null>(null)
  const [status, setStatus] = useState<Status>('VISTA')
  const [review, setReview] = useState('')
  const [watchedDate, setWatchedDate] = useState(
    new Date().toISOString().split('T')[0]  // hoy por defecto
  )
  const [open, setOpen] = useState(false)

  const { mutate, isPending } = useCreateRating(content)

  const handleSubmit = () => {
    if (!rating || !tag) return
    mutate(
      { rating, emotionalTag: tag, status, reviewText: review || undefined, watchedDate },
      { onSuccess: () => setOpen(false) }
    )
  }

  if (!open) {
    return (
      <button
        onClick={() => setOpen(true)}
        className="flex items-center gap-2 bg-accent hover:bg-accent-light text-bg-0 font-semibold text-sm px-5 py-2.5 rounded-xl transition-all hover:-translate-y-0.5 hover:shadow-lg hover:shadow-accent/20"
      >
        ⭐ Valorar
      </button>
    )
  }

  return (
    <div className="bg-bg-2 border border-white/[0.08] rounded-2xl p-5 w-full max-w-md">
      <div className="flex items-center justify-between mb-4">
        <h3 className="font-display font-bold italic text-lg">Tu valoración</h3>
        <button onClick={() => setOpen(false)} className="text-muted hover:text-white text-lg">
          ✕
        </button>
      </div>

      {/* Estrellas */}
      <div className="mb-5">
        <p className="text-xs text-muted font-mono uppercase tracking-wider mb-2">Puntuación</p>
        <StarRating value={rating} onChange={setRating} size="lg" />
      </div>

      {/* Tag emocional */}
      <div className="mb-5">
        <p className="text-xs text-muted font-mono uppercase tracking-wider mb-2">¿Cómo te pareció?</p>
        <div className="flex gap-2 flex-wrap">
          {EMOTIONAL_TAGS.map((t) => (
            <button
              key={t.value}
              onClick={() => setTag(t.value)}
              className={cn(
                'flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-medium border transition-all',
                tag === t.value
                  ? 'bg-accent/15 border-accent/50 text-accent'
                  : 'bg-bg-3 border-white/[0.06] text-muted hover:text-white hover:border-white/20'
              )}
            >
              <span>{t.emoji}</span>
              {t.label}
            </button>
          ))}
        </div>
      </div>

      {/* Estado */}
      <div className="mb-5">
        <p className="text-xs text-muted font-mono uppercase tracking-wider mb-2">Estado</p>
        <div className="flex gap-2 flex-wrap">
          {STATUSES.map((s) => (
            <button
              key={s.value}
              onClick={() => setStatus(s.value)}
              className={cn(
                'px-3 py-1.5 rounded-lg text-xs font-medium border transition-all',
                status === s.value
                  ? 'bg-accent/15 border-accent/50 text-accent'
                  : 'bg-bg-3 border-white/[0.06] text-muted hover:text-white hover:border-white/20'
              )}
            >
              {s.label}
            </button>
          ))}
        </div>
      </div>

      {/* Fecha vista */}
      <div className="mb-5">
        <p className="text-xs text-muted font-mono uppercase tracking-wider mb-2">Fecha</p>
        <input
          type="date"
          value={watchedDate}
          onChange={(e) => setWatchedDate(e.target.value)}
          className="bg-bg-3 border border-white/[0.08] rounded-lg px-3 py-2 text-sm text-white outline-none focus:border-accent/50 transition-colors"
        />
      </div>

      {/* Reseña opcional */}
      <div className="mb-5">
        <p className="text-xs text-muted font-mono uppercase tracking-wider mb-2">
          Reseña <span className="text-muted/60">(opcional)</span>
        </p>
        <textarea
          value={review}
          onChange={(e) => setReview(e.target.value)}
          rows={3}
          placeholder="¿Qué te pareció?"
          className="w-full bg-bg-3 border border-white/[0.08] rounded-lg px-3 py-2 text-sm text-white placeholder:text-muted outline-none focus:border-accent/50 transition-colors resize-none"
        />
      </div>

      <button
        onClick={handleSubmit}
        disabled={!rating || !tag || isPending}
        className="w-full bg-accent hover:bg-accent-light disabled:opacity-50 disabled:cursor-not-allowed text-bg-0 font-semibold text-sm py-2.5 rounded-xl transition-all"
      >
        {isPending ? 'Guardando…' : 'Guardar valoración'}
      </button>
    </div>
  )
}