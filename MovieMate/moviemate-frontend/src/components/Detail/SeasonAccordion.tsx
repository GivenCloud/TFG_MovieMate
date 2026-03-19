import { useState } from 'react'
import { useTvSeasons, useSeasonDetail, useWatchedEpisodes, useToggleEpisodeWatched, useToggleSeasonWatched } from '@/hooks/useEpisodes'
import { useAuthStore } from '@/store/authStore'
import type { SeasonSummary } from '@/types'

interface Props {
  tmdbId: number
}

export default function SeasonAccordion({ tmdbId }: Props) {
  const { isAuthenticated } = useAuthStore()
  const { data: seasons = [], isLoading } = useTvSeasons(tmdbId)
  const { data: watched = new Set<string>() } = useWatchedEpisodes(tmdbId)
  const [openSeason, setOpenSeason] = useState<number | null>(null)

  if (isLoading) {
    return (
      <div className="space-y-2">
        {[1, 2, 3].map((i) => (
          <div key={i} className="h-12 bg-bg-2 rounded-xl animate-pulse" />
        ))}
      </div>
    )
  }

  if (seasons.length === 0) return null

  return (
    <div className="space-y-2">
      {seasons.map((season) => (
        <SeasonRow
          key={season.seasonNumber}
          season={season}
          tmdbId={tmdbId}
          isOpen={openSeason === season.seasonNumber}
          onToggle={() =>
            setOpenSeason((prev) =>
              prev === season.seasonNumber ? null : season.seasonNumber
            )
          }
          watched={watched}
          isAuthenticated={isAuthenticated}
        />
      ))}
    </div>
  )
}

interface SeasonRowProps {
  season: SeasonSummary
  tmdbId: number
  isOpen: boolean
  onToggle: () => void
  watched: Set<string>
  isAuthenticated: boolean
}

function SeasonRow({ season, tmdbId, isOpen, onToggle, watched, isAuthenticated }: SeasonRowProps) {
  const { data: detail } = useSeasonDetail(isOpen ? tmdbId : undefined, isOpen ? season.seasonNumber : null)
  const toggleEpisode = useToggleEpisodeWatched(tmdbId)
  const toggleSeason = useToggleSeasonWatched(tmdbId)

  const watchedCount = Array.from(watched).filter((k) =>
    k.startsWith(`${season.seasonNumber}-`)
  ).length

  const total = season.episodeCount ?? 0
  const progress = total > 0 ? Math.round((watchedCount / total) * 100) : 0
  const allWatched = watchedCount === total && total > 0

  function handleSeasonToggle(e: React.MouseEvent) {
    e.stopPropagation()
    if (!detail) return
    const epNumbers = detail.episodes.map((ep) => ep.episodeNumber)
    toggleSeason.mutate({
      seasonNumber: season.seasonNumber,
      episodeNumbers: epNumbers,
      markAll: !allWatched,
    })
  }

  return (
    <div className="rounded-xl border border-white/[0.07] bg-bg-1 overflow-hidden">
      {/* Cabecera del acordeón */}
      <button
        onClick={onToggle}
        className="w-full flex items-center gap-3 px-4 py-3 text-left hover:bg-white/[0.04] transition-colors"
      >
        {/* Poster de temporada */}
        <div className="w-8 h-12 rounded shrink-0 overflow-hidden bg-bg-3">
          {season.posterUrl ? (
            <img src={season.posterUrl} alt={season.name} className="w-full h-full object-cover" />
          ) : (
            <div className="w-full h-full flex items-center justify-center text-muted text-xs">
              {season.seasonNumber}
            </div>
          )}
        </div>

        {/* Info */}
        <div className="flex-1 min-w-0">
          <p className="text-sm font-medium text-white/90 truncate">{season.name}</p>
          <div className="flex items-center gap-2 mt-0.5">
            <span className="text-[0.65rem] text-muted">{season.episodeCount} episodios</span>
            {isAuthenticated && total > 0 && (
              <>
                <span className="text-white/20">·</span>
                <span
                  className={`text-[0.65rem] font-mono ${
                    allWatched ? 'text-green-400' : 'text-accent'
                  }`}
                >
                  {watchedCount}/{total}
                </span>
              </>
            )}
          </div>
          {/* Barra de progreso */}
          {isAuthenticated && total > 0 && (
            <div className="mt-1.5 h-0.5 w-full bg-bg-3 rounded-full overflow-hidden">
              <div
                className={`h-full rounded-full transition-all duration-300 ${
                  allWatched ? 'bg-green-400' : 'bg-accent'
                }`}
                style={{ width: `${progress}%` }}
              />
            </div>
          )}
        </div>

        {/* Botón marcar toda la temporada */}
        {isAuthenticated && detail && (
          <button
            onClick={handleSeasonToggle}
            title={allWatched ? 'Desmarcar temporada' : 'Marcar temporada como vista'}
            className={`shrink-0 text-xs px-2.5 py-1 rounded-lg border transition-colors ${
              allWatched
                ? 'border-green-400/40 text-green-400 hover:bg-green-400/10'
                : 'border-white/10 text-muted hover:text-white hover:border-white/20'
            }`}
          >
            {allWatched ? '✓ Vista' : 'Ver todo'}
          </button>
        )}

        {/* Chevron */}
        <span
          className={`text-muted text-xs transition-transform duration-200 ${
            isOpen ? 'rotate-180' : ''
          }`}
        >
          ▼
        </span>
      </button>

      {/* Episodios */}
      {isOpen && (
        <div className="border-t border-white/[0.06] divide-y divide-white/[0.04]">
          {!detail ? (
            <div className="px-4 py-8 flex justify-center">
              <div className="w-5 h-5 border-2 border-accent border-t-transparent rounded-full animate-spin" />
            </div>
          ) : (
            detail.episodes.map((ep) => {
              const key = `${season.seasonNumber}-${ep.episodeNumber}`
              const isWatched = watched.has(key)

              return (
                <div
                  key={ep.episodeNumber}
                  className="flex items-start gap-3 px-4 py-3"
                >
                  {/* Thumbnail del episodio */}
                  <div className="w-20 h-12 rounded shrink-0 overflow-hidden bg-bg-3 mt-0.5">
                    {ep.stillUrl ? (
                      <img
                        src={ep.stillUrl}
                        alt={ep.name}
                        className="w-full h-full object-cover"
                      />
                    ) : (
                      <div className="w-full h-full flex items-center justify-center text-muted text-xs font-mono">
                        E{ep.episodeNumber}
                      </div>
                    )}
                  </div>

                  {/* Info del episodio */}
                  <div className="flex-1 min-w-0">
                    <div className="flex items-baseline gap-2">
                      <span className="text-[0.65rem] font-mono text-muted shrink-0">
                        E{ep.episodeNumber}
                      </span>
                      <p className="text-xs font-medium text-white/85 truncate">{ep.name}</p>
                      {ep.runtime && (
                        <span className="text-[0.65rem] text-muted shrink-0 ml-auto">
                          {ep.runtime} min
                        </span>
                      )}
                    </div>
                    {ep.overview && (
                      <p className="text-[0.65rem] text-muted leading-relaxed mt-0.5 line-clamp-2">
                        {ep.overview}
                      </p>
                    )}
                  </div>

                  {/* Checkbox visto */}
                  {isAuthenticated && (
                    <button
                      onClick={() =>
                        toggleEpisode.mutate({
                          seasonNumber: season.seasonNumber,
                          episodeNumber: ep.episodeNumber,
                        })
                      }
                      className={`shrink-0 w-6 h-6 rounded-full border-2 flex items-center justify-center transition-all mt-1 ${
                        isWatched
                          ? 'bg-green-400 border-green-400 text-bg-0'
                          : 'border-white/20 text-transparent hover:border-white/40'
                      }`}
                    >
                      <span className="text-[0.6rem] font-bold">✓</span>
                    </button>
                  )}
                </div>
              )
            })
          )}
        </div>
      )}
    </div>
  )
}
