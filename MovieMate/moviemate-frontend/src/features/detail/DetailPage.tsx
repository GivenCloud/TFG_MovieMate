import { useParams, useLocation, useNavigate, Link } from 'react-router-dom'
import { useSyncContent, useReviews, useWatchProviders, useContentCredits } from '@/hooks/useDetail'
import { useAuthStore } from '@/store/authStore'
import DetailHero from '@/components/Detail/DetailHero'
import RatingWidget from '@/components/Detail/RatingWidget'
import AddToListButton from '@/components/Detail/AddToListButton'
import ReviewList from '@/components/Detail/ReviewList'
import SeasonAccordion from '@/components/Detail/SeasonAccordion'
import { toSlug } from '@/lib/utils'
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

  // Siempre fetchamos para tener stats actualizadas (appRating, appVoteCount)
  // stateContent se usa como fallback mientras carga para evitar el esqueleto
  const { data: syncedContent, isLoading } = useSyncContent(
    parsedTmdbId,
    parsedType,
    true
  )

  const content = syncedContent ?? stateContent

  // Las stats se derivan de las reseñas reales (misma caché que ReviewList, cero coste extra)
  const { data: reviews = [] } = useReviews(content?.id)
  const { data: providers } = useWatchProviders(parsedTmdbId, parsedType)
  const { data: cast = [] } = useContentCredits(parsedTmdbId, parsedType)
  const communityCount = reviews.length
  const communityAvg = communityCount > 0
    ? reviews.reduce((sum, r) => sum + r.rating, 0) / communityCount
    : 0

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
      <div className="px-8 py-8 max-w-5xl mx-auto">
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
          {/* Temporadas y episodios (solo series) */}
          <div className="space-y-8">
            {parsedType === 'TV' && (
              <div>
                <h2 className="font-display font-bold italic text-xl mb-5">
                  Temporadas y episodios
                </h2>
                <SeasonAccordion tmdbId={parsedTmdbId} />
              </div>
            )}

            {/* Reseñas */}
            <div>
              <h2 className="font-display font-bold italic text-xl mb-5">
                Reseñas de la comunidad
              </h2>
              <ReviewList content={content} />
            </div>
          </div>

          {/* Info adicional */}
          <aside className="space-y-5">
            {/* Cast */}
            {cast.length > 0 && (
              <div>
                <h3 className="text-xs font-mono text-muted uppercase tracking-wider mb-3">
                  Reparto y equipo
                </h3>
                <div className="space-y-2">
                  {cast.map((member) => (
                    <Link
                      key={`${member.personId}-${member.character ?? member.job}`}
                      to={`/person/${member.personId}/${toSlug(member.name)}`}
                      className="flex items-center gap-2.5 group"
                    >
                      <div className="w-8 h-8 rounded-full bg-bg-3 shrink-0 overflow-hidden border border-white/[0.06]">
                        {member.profileUrl ? (
                          <img
                            src={member.profileUrl}
                            alt={member.name}
                            className="w-full h-full object-cover"
                          />
                        ) : (
                          <div className="w-full h-full flex items-center justify-center text-xs text-muted">
                            {member.name.charAt(0)}
                          </div>
                        )}
                      </div>
                      <div className="flex-1 min-w-0">
                        <p className="text-xs font-medium text-white/90 truncate group-hover:text-accent transition-colors">
                          {member.name}
                        </p>
                        <p className="text-[0.65rem] text-muted truncate">
                          {member.character ?? member.job}
                        </p>
                      </div>
                    </Link>
                  ))}
                </div>
              </div>
            )}

            {content.genres?.length > 0 && (
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

            {/* Estadísticas del contenido */}
            <div>
              <h3 className="text-xs font-mono text-muted uppercase tracking-wider mb-2">
                Estadísticas
              </h3>
              <div className="space-y-2">
                <div className="flex items-center justify-between text-sm">
                  <span className="text-muted">Valoraciones</span>
                  <span className="font-mono text-white/70">
                    {communityCount > 0 ? communityCount : '—'}
                  </span>
                </div>
                <div className="flex items-center justify-between text-sm">
                  <span className="text-muted">Nota MovieMate</span>
                  <span className="font-mono text-accent font-semibold">
                    {communityCount > 0
                      ? <>{communityAvg.toFixed(1)}<span className="text-white/30 font-normal">/5</span></>
                      : '—'}
                  </span>
                </div>
                <div className="flex items-center justify-between text-sm">
                  <span className="text-muted">Nota TMDB</span>
                  <span className="font-mono text-yellow-400 font-semibold">
                    {content.tmdbRating.toFixed(1)}<span className="text-white/30 font-normal">/10</span>
                  </span>
                </div>
              </div>
            </div>

            {/* ¿Dónde ver? */}
            {providers && (providers.flatrate?.length || providers.rent?.length || providers.buy?.length) ? (
              <div>
                <h3 className="text-xs font-mono text-muted uppercase tracking-wider mb-3">
                  ¿Dónde ver?
                </h3>

                {providers.flatrate && providers.flatrate.length > 0 && (
                  <div className="mb-3">
                    <p className="text-[0.65rem] text-muted font-mono uppercase tracking-wider mb-1.5">
                      Streaming
                    </p>
                    <div className="flex flex-wrap gap-2">
                      {providers.flatrate.map((p) => (
                        <img
                          key={p.providerId}
                          src={p.logoUrl}
                          alt={p.providerName}
                          title={p.providerName}
                          className="w-9 h-9 rounded-lg object-cover border border-white/10"
                        />
                      ))}
                    </div>
                  </div>
                )}

                {providers.rent && providers.rent.length > 0 && (
                  <div className="mb-3">
                    <p className="text-[0.65rem] text-muted font-mono uppercase tracking-wider mb-1.5">
                      Alquiler
                    </p>
                    <div className="flex flex-wrap gap-2">
                      {providers.rent.map((p) => (
                        <img
                          key={p.providerId}
                          src={p.logoUrl}
                          alt={p.providerName}
                          title={p.providerName}
                          className="w-9 h-9 rounded-lg object-cover border border-white/10 opacity-80"
                        />
                      ))}
                    </div>
                  </div>
                )}

                {providers.buy && providers.buy.length > 0 && (
                  <div className="mb-3">
                    <p className="text-[0.65rem] text-muted font-mono uppercase tracking-wider mb-1.5">
                      Compra
                    </p>
                    <div className="flex flex-wrap gap-2">
                      {providers.buy.map((p) => (
                        <img
                          key={p.providerId}
                          src={p.logoUrl}
                          alt={p.providerName}
                          title={p.providerName}
                          className="w-9 h-9 rounded-lg object-cover border border-white/10 opacity-80"
                        />
                      ))}
                    </div>
                  </div>
                )}

                {providers.link && (
                  <a
                    href={providers.link}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="text-xs text-accent hover:text-accent-light transition-colors"
                  >
                    Ver en JustWatch →
                  </a>
                )}
              </div>
            ) : null}
          </aside>
        </div>
      </div>
    </div>
  )
}
