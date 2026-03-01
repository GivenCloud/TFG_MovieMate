import { Link } from 'react-router-dom'
import type { ContentResponse } from '../../types'

interface Props {
  content: ContentResponse
  userRating?: number
}

export default function PosterCard({ content, userRating }: Props) {
  return (
    <Link
      to={`/content/${content.id}`}
      className="group shrink-0 w-36 cursor-pointer focus:outline-none focus-visible:ring-2 focus-visible:ring-accent rounded-xl"
    >
      <div className="relative aspect-[2/3] rounded-xl overflow-hidden bg-bg-3 border border-white/[0.06] mb-2">
        {content.posterUrl ? (
          <img
            src={content.posterUrl}
            alt={`Poster de ${content.title}`}
            loading="lazy"
            className="w-full h-full object-cover transition-transform duration-300 group-hover:scale-105"
          />
        ) : (
          <div className="w-full h-full flex items-center justify-center text-4xl bg-bg-3">
            🎬
          </div>
        )}

        <div className="absolute inset-0 bg-gradient-to-t from-black/80 via-transparent to-transparent" />

        {/* Puntuación */}
        <div className="absolute bottom-1.5 left-2 flex items-center gap-1 text-yellow-400 text-[0.65rem] font-mono font-semibold">
          ⭐ {content.appRating > 0
            ? content.appRating.toFixed(1)
            : content.tmdbRating.toFixed(1)}
        </div>

        {/* Tipo */}
        <div className="absolute top-1.5 right-1.5 text-[10px] font-semibold bg-black/60 backdrop-blur-sm text-white/70 px-1.5 py-0.5 rounded">
          {content.contentType === 'MOVIE' ? 'Film' : 'Serie'}
        </div>

        {/* Hover overlay */}
        <div className="absolute inset-0 bg-black/60 opacity-0 group-hover:opacity-100 transition-opacity flex flex-col items-center justify-center gap-2">
          <span className="text-xs font-semibold bg-accent text-bg-0 px-3 py-1.5 rounded-lg">
            + Lista
          </span>
          <span className="text-xs font-medium text-white/80">Ver ficha →</span>
        </div>
      </div>

      <p className="text-sm font-semibold text-white/90 leading-tight mb-1 line-clamp-2">
        {content.title}
      </p>
      <p className="text-[0.65rem] text-muted font-mono">
        {content.releaseDate ? new Date(content.releaseDate).getFullYear() : '—'}
        {content.genres[0] ? ` · ${content.genres[0]}` : ''}
      </p>

      {userRating != null && (
        <div className="flex gap-0.5 mt-1">
          {[1, 2, 3, 4, 5].map((n) => (
            <span
              key={n}
              className={`text-[0.6rem] ${n <= userRating ? 'text-yellow-400' : 'text-white/20'}`}
            >
              ★
            </span>
          ))}
        </div>
      )}
    </Link>
  )
}