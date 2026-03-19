import { useParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { tmdbApi } from '@/api/tmdb'
import { queryKeys } from '@/lib/queryKeys'
import PosterCard from '@/components/shared/PosterdCard'
import BackButton from '@/components/shared/BackButton'

function PersonSkeleton() {
  return (
    <div className="animate-pulse px-4 lg:px-8 py-8 max-w-5xl mx-auto">
      <div className="flex gap-6 flex-col sm:flex-row">
        <div className="w-36 h-52 rounded-2xl bg-bg-2 shrink-0" />
        <div className="flex-1 space-y-3 pt-2">
          <div className="h-8 bg-bg-2 rounded w-48" />
          <div className="h-4 bg-bg-2 rounded w-32" />
          <div className="h-20 bg-bg-2 rounded" />
        </div>
      </div>
    </div>
  )
}

export default function PersonPage() {
  const { personId } = useParams<{ personId: string }>()
  const id = Number(personId)

  const { data: person, isLoading: personLoading } = useQuery({
    queryKey: queryKeys.tmdb.person(id),
    queryFn: () => tmdbApi.getPersonDetails(id).then((r) => r.data),
    enabled: !!id,
    staleTime: 1000 * 60 * 60,
  })

  const { data: credits = [], isLoading: creditsLoading } = useQuery({
    queryKey: queryKeys.tmdb.personCredits(id),
    queryFn: () => tmdbApi.getPersonCredits(id).then((r) => r.data),
    enabled: !!id,
    staleTime: 1000 * 60 * 60,
  })

  if (personLoading) return <PersonSkeleton />

  if (!person) {
    return (
      <div className="flex flex-col items-center justify-center py-32 text-center">
        <span className="text-5xl mb-4">👤</span>
        <h2 className="text-xl font-bold text-white/80 mb-2">Persona no encontrada</h2>
        <p className="text-sm text-muted">No pudimos cargar los datos de esta persona.</p>
      </div>
    )
  }

  return (
    <div className="pb-12">
      {/* Botón volver */}
      <div className="px-4 lg:px-8 pt-4 pb-1">
        <BackButton />
      </div>

      {/* ── Cabecera ─────────────────────────────────────────── */}
      <div className="px-4 lg:px-8 py-8 max-w-5xl mx-auto">
        <div className="flex gap-6 flex-col sm:flex-row items-start">
          {/* Foto */}
          <div className="shrink-0">
            {person.profileUrl ? (
              <img
                src={person.profileUrl}
                alt={person.name}
                className="w-36 h-52 rounded-2xl object-cover border border-white/[0.1] shadow-xl"
              />
            ) : (
              <div className="w-36 h-52 rounded-2xl bg-bg-2 border border-white/[0.1] flex items-center justify-center text-5xl text-muted">
                👤
              </div>
            )}
          </div>

          {/* Info */}
          <div className="flex-1 min-w-0">
            <h1 className="font-display font-bold italic text-3xl mb-1">{person.name}</h1>

            {person.knownForDepartment && (
              <p className="text-sm text-muted font-mono mb-3">{person.knownForDepartment}</p>
            )}

            <div className="flex flex-wrap gap-x-6 gap-y-1 mb-4 text-xs text-muted font-mono">
              {person.birthday && (
                <span>
                  🎂 {person.birthday}
                  {person.deathday && ` — ${person.deathday}`}
                </span>
              )}
              {person.placeOfBirth && (
                <span>📍 {person.placeOfBirth}</span>
              )}
            </div>

            {person.biography && (
              <div>
                <h2 className="text-xs font-mono text-muted uppercase tracking-wider mb-2">Biografía</h2>
                <p className="text-sm text-white/70 leading-relaxed line-clamp-6">{person.biography}</p>
              </div>
            )}
          </div>
        </div>
      </div>

      {/* ── Filmografía ──────────────────────────────────────── */}
      <div className="px-4 lg:px-8 max-w-5xl mx-auto">
        <h2 className="font-display font-bold italic text-xl mb-5">
          Filmografía
          {credits.length > 0 && (
            <span className="text-sm font-normal font-sans text-muted ml-2">
              ({credits.length} títulos)
            </span>
          )}
        </h2>

        {creditsLoading ? (
          <div className="flex flex-wrap gap-3">
            {Array.from({ length: 10 }).map((_, i) => (
              <div key={i} className="w-36 h-52 rounded-xl bg-bg-2 animate-pulse" />
            ))}
          </div>
        ) : credits.length > 0 ? (
          <div className="flex flex-wrap gap-3">
            {credits.map((c) => (
              <PosterCard key={`${c.contentType}-${c.tmdbId}`} content={c} />
            ))}
          </div>
        ) : (
          <div className="text-center py-16">
            <span className="text-4xl mb-3 block">🎬</span>
            <p className="text-sm text-muted">Sin filmografía disponible</p>
          </div>
        )}
      </div>
    </div>
  )
}
