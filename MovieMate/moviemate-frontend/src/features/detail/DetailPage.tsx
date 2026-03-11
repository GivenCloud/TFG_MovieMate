import { useParams, useLocation, useNavigate } from 'react-router-dom'
import { useSyncContent } from '@/hooks/useDetail'
import { useAuthStore } from '@/store/authStore'
import DetailHero from '@/components/Detail/DetailHero'
import RatingWidget from '@/components/Detail/RatingWidget'
import AddToListButton from '@/components/Detail/AddToListButton'
import ReviewList from '@/components/Detail/ReviewList'
import type { ContentResponse, ContentType } from '../../types'

function DetailSkeleton() {
  return (
    <div className="animate-pulse">
      <div className="h-[420px] bg-bg-2" />
      <div className="px-8 py-8 space-y-4">
        <div className="h-8 bg-bg-3 rounded w-1/2" />
        <div className="h-4 bg-bg-3 rounded w-1/3" />
        <div className="h-20 bg-bg-3 rounded" />
      </div>
    </div>
  )
}

export default function DetailPage() {
  const { tmdbId, contentType } = useParams<{ tmdbId: string; contentType: string; slug: string }>()
  const location = useLocation()
  const navigate = useNavigate()
  const { isAuthenticated } = useAuthStore()

  // Si el usuario viene de PosterCard o del hero, el contenido ya está en el state
  const stateContent = location.state?.content as ContentResponse | undefined

  const parsedTmdbId = Number(tmdbId)
  const parsedType = (contentType?.toUpperCase() ?? 'MOVIE') as ContentType

  // Solo fetchamos si no tenemos el contenido en state
  const { data: syncedContent, isLoading } = useSyncContent(
    parsedTmdbId,
    parsedType,
    !stateContent
  )

  const content = stateContent ?? syncedContent

  if (isLoading && !content) return <DetailSkeleton />

  if (!content) {
    return (
      <div className="flex flex-col items-center justify-center py-32 text-center">
        <span className="text-5xl mb-4">🎬</span>
        <h2 className="text-xl font-bold text-white/80 mb-2">Contenido no encontrado</h2>
        <p className="text-sm text-muted">No pudimos cargar la información de este contenido.</p>
      </div>
    )
  }

  return (
    <div className="pb-12">
      {/* Hero — backdrop + poster + info */}
      <DetailHero content={content} />

      {/* Acciones y valoración */}
      <div className="px-8 py-8 max-w-5xl">
        <div className="flex gap-3 flex-wrap mb-8">
          {/* RatingWidget gestiona internamente el estado de auth */}
          <RatingWidget content={content} />

          {/* AddToListButton solo para usuarios autenticados */}
          {isAuthenticated ? (
            <AddToListButton content={content} />
          ) : (
            <button
              onClick={() => navigate('/login')}
              className="flex items-center gap-2 bg-white/[0.08] hover:bg-white/[0.14] border border-white/[0.1] text-white font-medium text-sm px-5 py-2.5 rounded-xl transition-colors"
            >
              📋 Añadir a lista
            </button>
          )}
        </div>

        {/* Layout de dos columnas en desktop */}
        <div className="grid grid-cols-1 lg:grid-cols-[1fr_340px] gap-8">
          {/* Reseñas */}
          <div>
            <h2 className="font-display font-bold italic text-xl mb-5">
              Reseñas de la comunidad
            </h2>
            <ReviewList content={content} />
          </div>

          {/* Info adicional */}
          <aside className="space-y-5">
            {content.genres.length > 0 && (
              <div>
                <h3 className="text-xs font-mono text-muted uppercase tracking-wider mb-2">
                  Géneros
                </h3>
                <div className="flex gap-2 flex-wrap">
                  {content.genres.map((g) => (
                    <span
                      key={g}
                      className="bg-bg-2 border border-white/[0.06] text-white/70 text-xs px-2.5 py-1 rounded-full"
                    >
                      {g}
                    </span>
                  ))}
                </div>
              </div>
            )}

            {content.synopsis && (
              <div>
                <h3 className="text-xs font-mono text-muted uppercase tracking-wider mb-2">
                  Sinopsis
                </h3>
                <p className="text-sm text-white/70 leading-relaxed">{content.synopsis}</p>
              </div>
            )}

            {/* Estadísticas del contenido */}
            <div>
              <h3 className="text-xs font-mono text-muted uppercase tracking-wider mb-2">
                Estadísticas
              </h3>
              <div className="space-y-2">
                <div className="flex items-center justify-between text-sm">
                  <span className="text-muted">Valoraciones</span>
                  <span className="font-mono text-white/70">
                    {content.appVoteCount > 0 ? content.appVoteCount : '—'}
                  </span>
                </div>
                <div className="flex items-center justify-between text-sm">
                  <span className="text-muted">Nota MovieMate</span>
                  <span className="font-mono text-accent font-semibold">
                    {content.appRating > 0 ? content.appRating.toFixed(1) : '—'}
                  </span>
                </div>
                <div className="flex items-center justify-between text-sm">
                  <span className="text-muted">Nota TMDB</span>
                  <span className="font-mono text-yellow-400 font-semibold">
                    {content.tmdbRating.toFixed(1)}
                  </span>
                </div>
              </div>
            </div>
          </aside>
        </div>
      </div>
    </div>
  )
}
