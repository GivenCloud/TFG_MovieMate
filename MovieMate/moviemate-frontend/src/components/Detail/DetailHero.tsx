import { getYear } from '@/lib/utils'
import type { ContentResponse } from '@/types'

interface Props {
  content: ContentResponse
}

export default function DetailHero({ content }: Props) {
  return (
    <div className="relative">
      {/* Backdrop */}
      <div className="relative h-[420px] overflow-hidden">
        {content.backdropUrl ? (
          <img
            src={content.backdropUrl}
            alt=""
            aria-hidden="true"
            className="w-full h-full object-cover object-top brightness-[0.3] saturate-150 scale-105"
          />
        ) : (
          <div className="w-full h-full bg-gradient-to-br from-[#1a0a2e] to-[#0a1628]" />
        )}
        {/* Gradients de fusión */}
        <div className="absolute inset-0 bg-gradient-to-t from-bg-0 via-bg-0/40 to-transparent" />
        <div className="absolute inset-0 bg-gradient-to-r from-bg-0/80 to-transparent" />
      </div>

      {/* Contenido superpuesto al backdrop */}
      <div className="absolute inset-0 flex items-end">
        <div className="flex gap-8 px-8 pb-8 w-full max-w-5xl">
          {/* Poster flotante */}
          <div className="shrink-0 w-44 hidden sm:block">
            <div className="aspect-[2/3] rounded-2xl overflow-hidden border border-white/10 shadow-[0_30px_60px_rgba(0,0,0,0.8)] -translate-y-8">
              {content.posterUrl ? (
                <img
                  src={content.posterUrl}
                  alt={`Poster de ${content.title}`}
                  className="w-full h-full object-cover"
                />
              ) : (
                <div className="w-full h-full bg-bg-3 flex items-center justify-center text-4xl">
                  🎬
                </div>
              )}
            </div>
          </div>

          {/* Info básica */}
          <div className="flex-1 pb-2">
            {/* Badge de tipo */}
            <span className="inline-block font-mono text-[0.6rem] tracking-widest uppercase text-accent border border-accent/40 bg-accent/10 px-2 py-0.5 rounded mb-3">
              {content.contentType === 'MOVIE' ? 'Película' : 'Serie'}
            </span>

            <h1 className="font-display font-bold italic text-4xl leading-tight mb-3">
              {content.title}
            </h1>

            {/* Metadata */}
            <div className="flex items-center gap-3 text-sm text-white/60 mb-4 flex-wrap">
              <span>{getYear(content.releaseDate)}</span>
              {content.genres.slice(0, 3).map((g) => (
                <span key={g} className="bg-white/[0.08] px-2.5 py-0.5 rounded-full text-xs">
                  {g}
                </span>
              ))}
            </div>

            {/* Puntuaciones */}
            <div className="flex items-center gap-4 mb-5">
              <div className="flex items-center gap-2">
                <div className="flex flex-col">
                  <span className="text-[0.6rem] font-mono text-muted uppercase tracking-wider">TMDB</span>
                  <span className="text-xl font-bold text-yellow-400 font-mono leading-none">
                    {content.tmdbRating.toFixed(1)}
                  </span>
                </div>
                <div className="w-px h-8 bg-white/10" />
                <div className="flex flex-col">
                  <span className="text-[0.6rem] font-mono text-muted uppercase tracking-wider">MovieMate</span>
                  <span className="text-xl font-bold text-accent font-mono leading-none">
                    {content.appRating > 0 ? content.appRating.toFixed(1) : '—'}
                  </span>
                </div>
                {content.appVoteCount > 0 && (
                  <span className="text-xs text-muted font-mono">
                    {content.appVoteCount} valoraciones
                  </span>
                )}
              </div>
            </div>

            {/* Sinopsis */}
            {content.synopsis && (
              <p className="text-sm text-white/70 leading-relaxed max-w-lg line-clamp-3">
                {content.synopsis}
              </p>
            )}
          </div>
        </div>
      </div>
    </div>
  )
}