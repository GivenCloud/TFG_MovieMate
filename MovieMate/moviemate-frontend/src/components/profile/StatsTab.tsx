import { Link } from 'react-router-dom'
import { useMyFullStats } from '@/hooks/useProfile'
import { useSeriesProgress } from '@/hooks/useEpisodes'
import { toSlug } from '@/lib/utils'
import type { FullStatsDto, SeriesProgressDto } from '@/types'

const MONTHS = ['Ene', 'Feb', 'Mar', 'Abr', 'May', 'Jun', 'Jul', 'Ago', 'Sep', 'Oct', 'Nov', 'Dic']

interface Props {
  userId: number | undefined
}

export default function StatsTab({ userId }: Props) {
  const { data: stats, isLoading: statsLoading } = useMyFullStats(!!userId)
  const { data: progress = [], isLoading: progressLoading } = useSeriesProgress(!!userId)

  const isLoading = statsLoading || progressLoading

  if (isLoading) return <StatsSkeleton />

  const hasStats = stats && stats.totalRatings > 0
  const hasProgress = progress.length > 0

  if (!hasStats && !hasProgress) {
    return (
      <div className="flex flex-col items-center justify-center py-20 text-center">
        <span className="text-4xl mb-3">📊</span>
        <p className="text-white/60 text-sm">Aún no hay estadísticas.<br />¡Empieza a valorar películas y series!</p>
      </div>
    )
  }

  return (
    <div className="space-y-8">
      {hasStats && stats && (
        <>
          {/* Resumen numérico */}
          <SummaryCards stats={stats} />

          <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
            <RatingDistributionChart stats={stats} />
            {stats.topGenres.length > 0 && <TopGenresChart stats={stats} />}
          </div>

          {stats.monthlyActivity.length > 0 && <MonthlyActivityChart stats={stats} />}
        </>
      )}

      {/* Progreso de series */}
      {hasProgress && <SeriesProgressSection progress={progress} />}
    </div>
  )
}

// ── Tarjetas de resumen ───────────────────────────────────────────
function SummaryCards({ stats }: { stats: FullStatsDto }) {
  const hours = Math.round(stats.totalWatchTime / 60)
  const cards = [
    { label: 'Valoraciones', value: stats.totalRatings, icon: '⭐' },
    { label: 'Películas vistas', value: stats.moviesWatched, icon: '🎬' },
    { label: 'Series vistas', value: stats.seriesWatched, icon: '📺' },
    { label: 'Horas vistas', value: hours, icon: '⏱️' },
    { label: 'Nota media', value: stats.averageRating > 0 ? stats.averageRating.toFixed(2) : '—', icon: '📈' },
    { label: 'Likes recibidos', value: stats.likesReceived, icon: '❤️' },
    { label: 'Listas', value: stats.listsCreated, icon: '📋' },
    { label: 'Seguidores', value: stats.followersCount, icon: '👥' },
  ]

  return (
    <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
      {cards.map((c) => (
        <div key={c.label} className="bg-bg-2 border border-white/[0.06] rounded-xl px-4 py-3 text-center">
          <div className="text-xl mb-1">{c.icon}</div>
          <div className="text-xl font-bold text-white font-mono">{c.value}</div>
          <div className="text-[0.65rem] text-muted mt-0.5">{c.label}</div>
        </div>
      ))}
    </div>
  )
}

// ── Distribución de notas ─────────────────────────────────────────
function RatingDistributionChart({ stats }: { stats: FullStatsDto }) {
  const maxCount = Math.max(...stats.ratingDistribution.map((d) => d.count), 1)
  const STAR_LABELS = ['★', '★★', '★★★', '★★★★', '★★★★★']

  return (
    <div>
      <h3 className="text-xs font-mono text-muted uppercase tracking-wider mb-4">
        Distribución de notas
      </h3>
      <div className="flex items-end justify-between gap-2 h-28">
        {stats.ratingDistribution.map(({ rating, count }) => {
          const pct = (count / maxCount) * 100
          return (
            <div key={rating} className="flex-1 flex flex-col items-center gap-1">
              <span className="text-[0.6rem] font-mono text-muted">{count > 0 ? count : ''}</span>
              <div className="w-full flex items-end" style={{ height: '72px' }}>
                <div
                  className="w-full rounded-t-sm transition-all duration-500"
                  style={{
                    height: `${Math.max(pct * 0.72, count > 0 ? 4 : 0)}px`,
                    background: `hsl(${40 + rating * 16}, 70%, ${45 + rating * 4}%)`,
                  }}
                />
              </div>
              <span className="text-[0.6rem] text-accent/80 font-mono whitespace-nowrap">
                {STAR_LABELS[rating - 1]}
              </span>
            </div>
          )
        })}
      </div>
    </div>
  )
}

// ── Top géneros ───────────────────────────────────────────────────
function TopGenresChart({ stats }: { stats: FullStatsDto }) {
  const maxCount = Math.max(...stats.topGenres.map((g) => g.count), 1)

  return (
    <div>
      <h3 className="text-xs font-mono text-muted uppercase tracking-wider mb-4">
        Géneros más vistos
      </h3>
      <div className="space-y-2.5">
        {stats.topGenres.map(({ genre, count }) => (
          <div key={genre} className="flex items-center gap-3">
            <span className="text-xs text-white/70 w-28 truncate shrink-0">{genre}</span>
            <div className="flex-1 bg-bg-3 rounded-full h-1.5 overflow-hidden">
              <div
                className="bg-accent h-full rounded-full transition-all duration-500"
                style={{ width: `${(count / maxCount) * 100}%` }}
              />
            </div>
            <span className="text-xs font-mono text-muted w-6 text-right shrink-0">{count}</span>
          </div>
        ))}
      </div>
    </div>
  )
}

// ── Actividad mensual ─────────────────────────────────────────────
function MonthlyActivityChart({ stats }: { stats: FullStatsDto }) {
  // Genera los últimos 12 meses (incluyendo el actual)
  const now = new Date()
  const months: { year: number; month: number; count: number }[] = []
  for (let i = 11; i >= 0; i--) {
    const d = new Date(now.getFullYear(), now.getMonth() - i, 1)
    const year = d.getFullYear()
    const month = d.getMonth() + 1
    const found = stats.monthlyActivity.find((m) => m.year === year && m.month === month)
    months.push({ year, month, count: found?.count ?? 0 })
  }

  const maxCount = Math.max(...months.map((m) => m.count), 1)

  return (
    <div>
      <h3 className="text-xs font-mono text-muted uppercase tracking-wider mb-4">
        Actividad mensual (últimos 12 meses)
      </h3>
      <div className="flex items-end gap-1.5 h-24">
        {months.map(({ year, month, count }) => {
          const pct = (count / maxCount) * 100
          return (
            <div key={`${year}-${month}`} className="flex-1 flex flex-col items-center gap-1 group relative">
              {/* Tooltip */}
              <div className="absolute bottom-full mb-1 left-1/2 -translate-x-1/2 bg-bg-0 border border-white/10 text-white text-[0.6rem] px-1.5 py-0.5 rounded whitespace-nowrap opacity-0 group-hover:opacity-100 transition-opacity pointer-events-none z-10">
                {count} valoración{count !== 1 ? 'es' : ''}
              </div>
              <div className="w-full flex items-end" style={{ height: '60px' }}>
                <div
                  className="w-full rounded-t-sm transition-all duration-500"
                  style={{
                    height: `${Math.max(pct * 0.6, count > 0 ? 3 : 0)}px`,
                    backgroundColor: count > 0 ? 'var(--color-accent, #e8c97a)' : 'rgba(255,255,255,0.06)',
                    opacity: count > 0 ? 0.7 + (pct / 100) * 0.3 : 1,
                  }}
                />
              </div>
              <span className="text-[0.55rem] text-muted">{MONTHS[month - 1]}</span>
            </div>
          )
        })}
      </div>
    </div>
  )
}

// ── Progreso de series ────────────────────────────────────────────
function SeriesProgressSection({ progress }: { progress: SeriesProgressDto[] }) {
  return (
    <div>
      <h3 className="text-xs font-mono text-muted uppercase tracking-wider mb-4">
        Progreso de series 📺
      </h3>
      <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
        {progress.map((s) => (
          <Link
            key={s.tmdbSeriesId}
            to={`/content/tv/${s.tmdbSeriesId}/${toSlug(s.title)}`}
            className="flex items-center gap-3 bg-bg-2 border border-white/[0.06] rounded-xl p-3 hover:border-white/[0.14] transition-colors group"
          >
            {/* Poster */}
            <div className="w-10 h-14 rounded-lg overflow-hidden shrink-0 bg-bg-3">
              {s.posterUrl ? (
                <img src={s.posterUrl} alt={s.title} className="w-full h-full object-cover" />
              ) : (
                <div className="w-full h-full flex items-center justify-center text-lg">📺</div>
              )}
            </div>
            {/* Info */}
            <div className="flex-1 min-w-0">
              <p className="text-sm font-medium text-white/90 truncate group-hover:text-accent transition-colors">
                {s.title}
              </p>
              <p className="text-xs text-muted mt-0.5 font-mono">
                {s.watchedCount} ep. {s.watchedCount === 1 ? 'visto' : 'vistos'}
              </p>
            </div>
            <span className="text-xs text-muted shrink-0">→</span>
          </Link>
        ))}
      </div>
    </div>
  )
}

// ── Skeleton ─────────────────────────────────────────────────────
function StatsSkeleton() {
  return (
    <div className="space-y-8 animate-pulse">
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
        {[1, 2, 3, 4, 5, 6, 7, 8].map((i) => (
          <div key={i} className="h-20 bg-bg-2 rounded-xl" />
        ))}
      </div>
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
        <div className="h-40 bg-bg-2 rounded-xl" />
        <div className="h-40 bg-bg-2 rounded-xl" />
      </div>
      <div className="h-32 bg-bg-2 rounded-xl" />
    </div>
  )
}
