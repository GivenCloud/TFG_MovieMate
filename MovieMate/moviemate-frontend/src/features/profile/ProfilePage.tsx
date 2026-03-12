import { useState, useEffect } from 'react'
import { useParams, Link, useSearchParams } from 'react-router-dom'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import { useAuthStore } from '../../store/authStore'
import {
  useUserByUsername,
  useUserProfile,
  useUserStats,
  useMyProfileRatings,
  useMyProfileLists,
  useUserFollowing,
  useUserFollowers,
  useFollowUser,
  useUnfollowUser,
  useUpdateProfile,
} from '../../hooks/useProfile'
import { useCreateRating } from '../../hooks/useDetail'
import { ratingsApi } from '../../api/ratings'
import { queryKeys } from '../../lib/queryKeys'
import PosterCard from '../../components/shared/PosterdCard'
import EmptyState from '../../components/shared/EmptyState'
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from '../../components/ui/dialog'
import type { ContentResponse, ListResponse, UserResponse, RatingResponse, EmotionalTag, Status } from '../../types'
import { timeAgo } from '../../lib/utils'

// ── Skeleton ────────────────────────────────────────────────
function ProfileSkeleton() {
  return (
    <div className="animate-pulse">
      <div className="px-6 py-8 border-b border-white/[0.06]">
        <div className="flex gap-5 items-start">
          <div className="w-20 h-20 rounded-full bg-bg-3 shrink-0" />
          <div className="flex-1 space-y-3 pt-1">
            <div className="h-5 bg-bg-3 rounded w-40" />
            <div className="h-4 bg-bg-3 rounded w-24" />
            <div className="h-4 bg-bg-3 rounded w-64" />
          </div>
        </div>
      </div>
      <div className="flex gap-8 px-6 py-5 border-b border-white/[0.06]">
        {[1, 2, 3, 4].map((i) => (
          <div key={i} className="space-y-1">
            <div className="h-7 w-12 bg-bg-3 rounded" />
            <div className="h-3 w-20 bg-bg-3 rounded" />
          </div>
        ))}
      </div>
    </div>
  )
}

// ── Perfil privado ──────────────────────────────────────────
function PrivateProfile({ username }: { username: string }) {
  return (
    <div className="flex flex-col items-center justify-center py-32 px-4 text-center">
      <div className="text-5xl mb-4">🔒</div>
      <h2 className="text-xl font-bold text-white/80 mb-2">Perfil privado</h2>
      <p className="text-sm text-muted max-w-xs">
        <span className="text-white/70 font-medium">@{username}</span> tiene el perfil en privado.
        Síguele para ver su actividad.
      </p>
    </div>
  )
}

// ── Avatar ───────────────────────────────────────────────────
function AvatarCircle({ user, size = 'lg' }: { user: UserResponse; size?: 'sm' | 'lg' }) {
  const dim = size === 'lg' ? 'w-20 h-20 text-2xl' : 'w-9 h-9 text-sm'
  if (user.avatarUrl) {
    return (
      <img
        src={user.avatarUrl}
        alt={user.username}
        className={`${dim} rounded-full object-cover border-2 border-accent/30 shrink-0`}
      />
    )
  }
  return (
    <div
      className={`${dim} rounded-full bg-gradient-to-br from-accent/70 to-purple-500/70 flex items-center justify-center font-bold text-bg-0 shrink-0`}
    >
      {user.username[0].toUpperCase()}
    </div>
  )
}

// ── Constantes para edición de valoraciones ──────────────────
const EMOTIONAL_TAGS: { value: EmotionalTag; label: string }[] = [
  { value: 'INCREIBLE',     label: '🤩 Increíble' },
  { value: 'RECOMENDADA',   label: '👍 Recomendada' },
  { value: 'ENTRETENIDA',   label: '😊 Entretenida' },
  { value: 'REGULAR',       label: '😐 Regular' },
  { value: 'DECEPCIONANTE', label: '😞 Decepcionante' },
]

const STATUS_OPTIONS: { value: Status; label: string }[] = [
  { value: 'VISTA',        label: '✅ Vista' },
  { value: 'EN_PROGRESO',  label: '▶️ En progreso' },
  { value: 'PAUSADA',      label: '⏸️ Pausada' },
  { value: 'ABANDONADA',   label: '❌ Abandonada' },
  { value: 'POR_VER',      label: '🕐 Por ver' },
]

// ── Dialog: edición rápida de valoración ─────────────────────
function QuickEditRatingDialog({ r, onClose }: { r: RatingResponse; onClose: () => void }) {
  const [rating, setRating] = useState(r.rating)
  const [hovered, setHovered] = useState(0)
  const [reviewText, setReviewText] = useState(r.reviewText ?? '')
  const [emotionalTag, setEmotionalTag] = useState<EmotionalTag>(r.emotionalTag)
  const [status, setStatus] = useState<Status>(r.status)
  const [watchedDate, setWatchedDate] = useState(r.watchedDate ? r.watchedDate.split('T')[0] : '')

  const save = useCreateRating(r.content)

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    save.mutate(
      { rating, reviewText: reviewText.trim() || undefined, emotionalTag, status, watchedDate },
      { onSuccess: onClose }
    )
  }

  return (
    <Dialog open onOpenChange={(v) => !v && onClose()}>
      <DialogContent className="bg-bg-1 border-white/[0.1] text-white max-w-md">
        <DialogHeader>
          <DialogTitle className="font-display font-bold italic text-xl">Editar valoración</DialogTitle>
          <p className="text-sm text-muted mt-0.5">{r.content.title}</p>
        </DialogHeader>
        <form onSubmit={handleSubmit} className="space-y-4 mt-2">
          {/* Puntuación */}
          <div>
            <label className="block font-mono text-xs text-muted tracking-wider uppercase mb-2">
              Puntuación
            </label>
            <div className="flex gap-1 flex-wrap">
              {Array.from({ length: 10 }, (_, i) => i + 1).map((n) => (
                <button
                  key={n}
                  type="button"
                  onClick={() => setRating(n)}
                  onMouseEnter={() => setHovered(n)}
                  onMouseLeave={() => setHovered(0)}
                  className={`w-8 h-8 rounded-lg text-sm font-bold transition-all
                    ${n <= (hovered || rating)
                      ? 'bg-accent text-bg-0'
                      : 'bg-bg-3 text-muted hover:bg-bg-3/80'
                    }`}
                >
                  {n}
                </button>
              ))}
            </div>
          </div>

          {/* Etiqueta emocional */}
          <div>
            <label className="block font-mono text-xs text-muted tracking-wider uppercase mb-2">
              Etiqueta
            </label>
            <div className="flex flex-wrap gap-2">
              {EMOTIONAL_TAGS.map(({ value, label }) => (
                <button
                  key={value}
                  type="button"
                  onClick={() => setEmotionalTag(value)}
                  className={`px-2.5 py-1 rounded-full text-xs font-medium transition-colors
                    ${emotionalTag === value
                      ? 'bg-accent text-bg-0'
                      : 'bg-bg-3 text-muted hover:text-white'
                    }`}
                >
                  {label}
                </button>
              ))}
            </div>
          </div>

          {/* Estado */}
          <div>
            <label className="block font-mono text-xs text-muted tracking-wider uppercase mb-2">
              Estado
            </label>
            <div className="flex flex-wrap gap-2">
              {STATUS_OPTIONS.map(({ value, label }) => (
                <button
                  key={value}
                  type="button"
                  onClick={() => setStatus(value)}
                  className={`px-2.5 py-1 rounded-full text-xs font-medium transition-colors
                    ${status === value
                      ? 'bg-accent/20 text-accent border border-accent/40'
                      : 'bg-bg-3 text-muted hover:text-white'
                    }`}
                >
                  {label}
                </button>
              ))}
            </div>
          </div>

          {/* Fecha */}
          <div>
            <label className="block font-mono text-xs text-muted tracking-wider uppercase mb-1.5">
              Fecha de visionado
            </label>
            <input
              type="date"
              value={watchedDate}
              onChange={(e) => setWatchedDate(e.target.value)}
              className="w-full bg-bg-2 border border-white/[0.1] rounded-xl px-3.5 py-2.5 text-sm text-white outline-none focus:border-accent/50 focus:ring-2 focus:ring-accent/10 transition-all"
            />
          </div>

          {/* Reseña */}
          <div>
            <label className="block font-mono text-xs text-muted tracking-wider uppercase mb-1.5">
              Reseña <span className="normal-case">(opcional)</span>
            </label>
            <textarea
              value={reviewText}
              onChange={(e) => setReviewText(e.target.value)}
              maxLength={1000}
              rows={3}
              placeholder="Tu opinión..."
              className="w-full bg-bg-2 border border-white/[0.1] rounded-xl px-3.5 py-2.5 text-sm text-white placeholder:text-muted outline-none focus:border-accent/50 focus:ring-2 focus:ring-accent/10 transition-all resize-none"
            />
          </div>

          <DialogFooter>
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2 text-sm text-muted hover:text-white border border-white/[0.1] rounded-xl transition-colors"
            >
              Cancelar
            </button>
            <button
              type="submit"
              disabled={save.isPending}
              className="px-4 py-2 text-sm font-semibold bg-accent hover:bg-accent-light text-bg-0 rounded-xl disabled:opacity-60 transition-colors"
            >
              {save.isPending ? 'Guardando…' : 'Guardar'}
            </button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}

// ── Item de valoración con botones de acción ──────────────────
function RatingPosterItem({ r }: { r: RatingResponse }) {
  const [editOpen, setEditOpen] = useState(false)
  const queryClient = useQueryClient()

  const deleteMutation = useMutation({
    mutationFn: () => ratingsApi.delete(r.id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.users.ratings() })
      toast.success('Valoración eliminada')
    },
    onError: () => toast.error('Error al eliminar la valoración'),
  })

  return (
    <div className="relative group">
      <PosterCard content={r.content} userRating={r.rating} />
      <div className="absolute top-1 right-1 flex flex-col gap-1 opacity-0 group-hover:opacity-100 transition-opacity z-10">
        <button
          onClick={(e) => { e.preventDefault(); e.stopPropagation(); setEditOpen(true) }}
          className="w-6 h-6 bg-bg-0/80 hover:bg-accent text-white hover:text-bg-0 rounded-md flex items-center justify-center text-xs transition-colors"
          title="Editar valoración"
        >
          ✏
        </button>
        <button
          onClick={(e) => { e.preventDefault(); e.stopPropagation(); deleteMutation.mutate() }}
          disabled={deleteMutation.isPending}
          className="w-6 h-6 bg-bg-0/80 hover:bg-red-500 text-white rounded-md flex items-center justify-center text-sm font-bold transition-colors disabled:opacity-50"
          title="Eliminar valoración"
        >
          ×
        </button>
      </div>
      {editOpen && <QuickEditRatingDialog r={r} onClose={() => setEditOpen(false)} />}
    </div>
  )
}

// ── Tarjeta de lista ─────────────────────────────────────────
function ListCard({ list }: { list: ListResponse }) {
  const previews = list.contents.slice(0, 4)
  return (
    <div className="bg-bg-1 border border-white/[0.06] rounded-xl overflow-hidden hover:border-white/[0.12] transition-colors cursor-pointer">
      {/* Mini mosaico de posters */}
      <div className="grid grid-cols-4 h-20">
        {previews.length > 0 ? (
          previews.map((c: ContentResponse) => (
            <div key={c.id} className="bg-bg-3 overflow-hidden">
              {c.posterUrl ? (
                <img src={c.posterUrl} alt={c.title} className="w-full h-full object-cover" />
              ) : (
                <div className="w-full h-full flex items-center justify-center text-lg">🎬</div>
              )}
            </div>
          ))
        ) : (
          <div className="col-span-4 bg-bg-3 flex items-center justify-center text-2xl text-muted">
            📋
          </div>
        )}
        {/* Rellena celdas vacías */}
        {Array.from({ length: Math.max(0, 4 - previews.length) }).map((_, i) => (
          <div key={`empty-${i}`} className="bg-bg-3 border-l border-bg-0" />
        ))}
      </div>
      <div className="px-3 py-2.5">
        <p className="text-sm font-semibold text-white/90 truncate">{list.name}</p>
        {list.description && (
          <p className="text-xs text-muted mt-0.5 line-clamp-1">{list.description}</p>
        )}
        <div className="flex items-center gap-2 mt-1.5 text-[0.65rem] text-muted font-mono">
          <span>{list.itemCount} títulos</span>
          <span>·</span>
          <span>{list.isPublic ? '🔓 Pública' : '🔒 Privada'}</span>
        </div>
      </div>
    </div>
  )
}

// ── Fila de usuario (tab Siguiendo) ─────────────────────────
function UserRow({ user }: { user: UserResponse }) {
  return (
    <Link
      to={`/profile/${user.username}`}
      className="flex items-center gap-3 px-6 py-3 hover:bg-bg-1 transition-colors"
    >
      <AvatarCircle user={user} size="sm" />
      <div className="flex-1 min-w-0">
        <p className="text-sm font-semibold text-white/90">{user.username}</p>
        {user.bio && <p className="text-xs text-muted truncate">{user.bio}</p>}
      </div>
      <span className="text-xs text-muted font-mono">{timeAgo(user.createdAt)}</span>
    </Link>
  )
}

// ── Dialog: editar perfil ────────────────────────────────────
function EditProfileDialog({
  open,
  onClose,
  initialBio,
  initialAvatar,
  username,
}: {
  open: boolean
  onClose: () => void
  initialBio: string
  initialAvatar: string
  username: string
}) {
  const [bio, setBio] = useState(initialBio)
  const [avatarUrl, setAvatarUrl] = useState(initialAvatar)
  const update = useUpdateProfile(username, onClose)

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    update.mutate({ bio: bio.trim() || undefined, avatarUrl: avatarUrl.trim() || undefined })
  }

  return (
    <Dialog open={open} onOpenChange={(v) => !v && onClose()}>
      <DialogContent className="bg-bg-1 border-white/[0.1] text-white max-w-md">
        <DialogHeader>
          <DialogTitle className="font-display font-bold italic text-xl">Editar perfil</DialogTitle>
        </DialogHeader>
        <form onSubmit={handleSubmit} className="space-y-4 mt-2">
          <div>
            <label className="block font-mono text-xs text-muted tracking-wider uppercase mb-1.5">
              URL del avatar
            </label>
            <input
              type="url"
              value={avatarUrl}
              onChange={(e) => setAvatarUrl(e.target.value)}
              placeholder="https://..."
              className="w-full bg-bg-2 border border-white/[0.1] rounded-xl px-3.5 py-2.5 text-sm text-white placeholder:text-muted outline-none focus:border-accent/50 focus:ring-2 focus:ring-accent/10 transition-all"
            />
          </div>
          <div>
            <label className="block font-mono text-xs text-muted tracking-wider uppercase mb-1.5">
              Biografía
            </label>
            <textarea
              value={bio}
              onChange={(e) => setBio(e.target.value)}
              maxLength={200}
              rows={3}
              placeholder="Cuéntanos algo sobre ti..."
              className="w-full bg-bg-2 border border-white/[0.1] rounded-xl px-3.5 py-2.5 text-sm text-white placeholder:text-muted outline-none focus:border-accent/50 focus:ring-2 focus:ring-accent/10 transition-all resize-none"
            />
            <p className="text-xs text-muted text-right mt-1">{bio.length}/200</p>
          </div>
          <DialogFooter>
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2 text-sm text-muted hover:text-white border border-white/[0.1] rounded-xl transition-colors"
            >
              Cancelar
            </button>
            <button
              type="submit"
              disabled={update.isPending}
              className="px-4 py-2 text-sm font-semibold bg-accent hover:bg-accent-light text-bg-0 rounded-xl disabled:opacity-60 transition-colors"
            >
              {update.isPending ? 'Guardando…' : 'Guardar'}
            </button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}

// ── Dialog: lista de seguidores/siguiendo ────────────────────
function FollowListDialog({
  open,
  onClose,
  title,
  users,
  isLoading,
}: {
  open: boolean
  onClose: () => void
  title: string
  users: UserResponse[]
  isLoading: boolean
}) {
  return (
    <Dialog open={open} onOpenChange={(v) => !v && onClose()}>
      <DialogContent className="bg-bg-1 border-white/[0.1] text-white max-w-sm p-0 overflow-hidden">
        <DialogHeader className="px-5 pt-5 pb-3 border-b border-white/[0.06]">
          <DialogTitle className="font-display font-bold italic text-xl">{title}</DialogTitle>
        </DialogHeader>
        <div className="max-h-96 overflow-y-auto">
          {isLoading ? (
            <div className="space-y-1 p-3">
              {[1, 2, 3].map((i) => (
                <div key={i} className="flex items-center gap-3 px-3 py-2.5 animate-pulse">
                  <div className="w-9 h-9 rounded-full bg-bg-3 shrink-0" />
                  <div className="space-y-1.5 flex-1">
                    <div className="h-3 bg-bg-3 rounded w-28" />
                    <div className="h-2.5 bg-bg-3 rounded w-40" />
                  </div>
                </div>
              ))}
            </div>
          ) : users.length === 0 ? (
            <p className="text-sm text-muted text-center py-10">Nadie todavía</p>
          ) : (
            <div className="divide-y divide-white/[0.04]">
              {users.map((u) => (
                <Link
                  key={u.id}
                  to={`/profile/${u.username}`}
                  onClick={onClose}
                  className="flex items-center gap-3 px-5 py-3 hover:bg-bg-2 transition-colors"
                >
                  <AvatarCircle user={u} size="sm" />
                  <div className="flex-1 min-w-0">
                    <p className="text-sm font-semibold text-white/90">{u.username}</p>
                    {u.bio && <p className="text-xs text-muted truncate">{u.bio}</p>}
                  </div>
                </Link>
              ))}
            </div>
          )}
        </div>
      </DialogContent>
    </Dialog>
  )
}

// ── Página principal ─────────────────────────────────────────
const TABS = [
  { id: 'activity',  label: 'Actividad' },
  { id: 'ratings',   label: 'Valoraciones' },
  { id: 'lists',     label: 'Listas' },
  { id: 'following', label: 'Siguiendo' },
] as const

type TabId = (typeof TABS)[number]['id']

export default function ProfilePage() {
  const { username = '' } = useParams<{ username: string }>()
  const { sessionUser, isAuthenticated } = useAuthStore()

  const isOwnProfile = isAuthenticated && sessionUser?.username === username
  const [searchParams] = useSearchParams()
  const [activeTab, setActiveTab] = useState<TabId>(
    (searchParams.get('tab') as TabId | null) ?? 'activity'
  )
  const [editOpen, setEditOpen] = useState(false)
  const [followDialog, setFollowDialog] = useState<'followers' | 'following' | null>(null)

  // Sincroniza el tab si el usuario navega al mismo perfil con distinto ?tab=
  useEffect(() => {
    const tab = searchParams.get('tab') as TabId | null
    if (tab) setActiveTab(tab)
  }, [searchParams])

  // ── Datos ────────────────────────────────────────────────
  const userQuery    = useUserByUsername(username)
  const userId       = userQuery.data?.id
  const profileQuery = useUserProfile(userId)
  const statsQuery   = useUserStats(userId)
  const ratingsQuery = useMyProfileRatings(isOwnProfile)
  const listsQuery   = useMyProfileLists(isOwnProfile)
  const followingQuery  = useUserFollowing(userId, activeTab === 'following' || followDialog === 'following')
  const followersQuery  = useUserFollowers(userId, followDialog === 'followers')

  const followMutation   = useFollowUser(userId ?? 0)
  const unfollowMutation = useUnfollowUser(userId ?? 0)

  // ── Estados de carga / error ─────────────────────────────
  const isLoading  = userQuery.isLoading || (!!userId && profileQuery.isLoading)
  const isPrivate  = (profileQuery.error as any)?.response?.status === 403

  if (isLoading) return <ProfileSkeleton />

  if (!userQuery.data) {
    return (
      <EmptyState
        icon="👤"
        title="Usuario no encontrado"
        description={`No existe ningún usuario con el nombre @${username}.`}
      />
    )
  }

  if (isPrivate) return <PrivateProfile username={username} />

  const user    = userQuery.data
  const profile = profileQuery.data
  const stats   = statsQuery.data
  const ratings = ratingsQuery.data ?? []
  const lists   = listsQuery.data ?? []
  const following = followingQuery.data ?? []

  const isFollowing  = profile?.isFollowing ?? false
  const followersCount = profile?.followersCount ?? 0
  const followingCount = profile?.followingCount ?? 0

  const handleFollowToggle = () => {
    if (!userId) return
    if (isFollowing) unfollowMutation.mutate()
    else followMutation.mutate()
  }

  const isPendingFollow = followMutation.isPending || unfollowMutation.isPending

  return (
    <div className="pb-12">
      {/* ── Header ─────────────────────────────────────────── */}
      <div className="px-6 pt-8 pb-6 border-b border-white/[0.06]">
        <div className="flex items-start gap-5 flex-wrap">
          <AvatarCircle user={user} size="lg" />

          <div className="flex-1 min-w-0">
            <div className="flex items-start justify-between gap-4 flex-wrap">
              <div>
                <h1 className="text-xl font-bold text-white/95">{user.username}</h1>
                <p className="text-sm text-muted font-mono mt-0.5">@{user.username}</p>
              </div>

              {/* Botón acción */}
              {isOwnProfile ? (
                <button
                  onClick={() => setEditOpen(true)}
                  className="px-4 py-2 text-sm font-medium border border-white/[0.1] text-white/80 rounded-xl hover:bg-bg-2 transition-colors shrink-0"
                >
                  ✏️ Editar perfil
                </button>
              ) : isAuthenticated && userId ? (
                <button
                  onClick={handleFollowToggle}
                  disabled={isPendingFollow}
                  className={`px-4 py-2 text-sm font-semibold rounded-xl transition-all shrink-0 disabled:opacity-60
                    ${isFollowing
                      ? 'border border-white/[0.15] text-white/70 hover:border-red-400/50 hover:text-red-400'
                      : 'bg-accent hover:bg-accent-light text-bg-0'
                    }`}
                >
                  {isPendingFollow ? '…' : isFollowing ? 'Siguiendo' : 'Seguir'}
                </button>
              ) : null}
            </div>

            {user.bio && (
              <p className="text-sm text-white/70 mt-3 leading-relaxed max-w-lg">{user.bio}</p>
            )}

            <div className="flex items-center gap-5 mt-3 text-sm">
              <button
                onClick={() => setFollowDialog('followers')}
                className="group hover:text-accent transition-colors text-left"
              >
                <span className="font-semibold text-white/90 group-hover:text-accent transition-colors">{followersCount}</span>
                <span className="text-muted ml-1 group-hover:text-accent/70 group-hover:underline transition-colors">seguidores</span>
              </button>
              <button
                onClick={() => setFollowDialog('following')}
                className="group hover:text-accent transition-colors text-left"
              >
                <span className="font-semibold text-white/90 group-hover:text-accent transition-colors">{followingCount}</span>
                <span className="text-muted ml-1 group-hover:text-accent/70 group-hover:underline transition-colors">siguiendo</span>
              </button>
            </div>
          </div>
        </div>
      </div>

      {/* ── Stats ──────────────────────────────────────────── */}
      {stats && (
        <div className="flex gap-8 px-6 py-5 border-b border-white/[0.06] overflow-x-auto scrollbar-none">
          {[
            { val: stats.moviesWatched,  label: 'Películas vistas' },
            { val: stats.seriesWatched,  label: 'Series seguidas' },
            { val: stats.totalRatings,   label: 'Valoraciones' },
            { val: stats.listsCreated,   label: 'Listas creadas' },
          ].map(({ val, label }) => (
            <div key={label} className="text-center shrink-0">
              <p className="font-mono text-2xl font-medium text-accent leading-none">{val}</p>
              <p className="text-xs text-muted mt-1 whitespace-nowrap">{label}</p>
            </div>
          ))}
        </div>
      )}

      {/* ── Tabs ───────────────────────────────────────────── */}
      <div className="flex gap-1 border-b border-white/[0.06] px-6">
        {TABS.map((tab) => (
          <button
            key={tab.id}
            onClick={() => setActiveTab(tab.id)}
            className={`relative px-4 py-3.5 text-sm font-medium transition-colors
              ${activeTab === tab.id
                ? 'text-white after:absolute after:bottom-0 after:inset-x-0 after:h-0.5 after:bg-accent'
                : 'text-muted hover:text-white/70'
              }`}
          >
            {tab.label}
          </button>
        ))}
      </div>

      {/* ── Contenido de tabs ──────────────────────────────── */}
      <div className="px-6 py-6">

        {/* Actividad ─────────────────────────────────────── */}
        {activeTab === 'activity' && (
          <div className="space-y-8">
            {/* Últimas valoraciones */}
            <div>
              <div className="flex items-baseline justify-between mb-4">
                <h2 className="font-display font-bold italic text-xl">Últimas valoraciones</h2>
                {isOwnProfile && ratings.length > 0 && (
                  <button
                    onClick={() => setActiveTab('ratings')}
                    className="text-sm text-accent hover:text-accent-light transition-colors"
                  >
                    Ver todas →
                  </button>
                )}
              </div>
              {ratings.length > 0 ? (
                <div className="flex gap-3 overflow-x-auto scrollbar-none pb-2">
                  {ratings.slice(0, 8).map((r) => (
                    <RatingPosterItem key={r.id} r={r} />
                  ))}
                </div>
              ) : (
                <EmptyState
                  icon="⭐"
                  title={isOwnProfile ? 'Aún no has valorado nada' : 'Sin valoraciones públicas'}
                  description={isOwnProfile ? 'Busca películas o series y deja tu primera reseña.' : undefined}
                />
              )}
            </div>

            {/* Listas destacadas */}
            <div>
              <div className="flex items-baseline justify-between mb-4">
                <h2 className="font-display font-bold italic text-xl">Listas</h2>
                {isOwnProfile && lists.length > 0 && (
                  <button
                    onClick={() => setActiveTab('lists')}
                    className="text-sm text-accent hover:text-accent-light transition-colors"
                  >
                    Ver todas →
                  </button>
                )}
              </div>
              {lists.length > 0 ? (
                <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
                  {lists.slice(0, 4).map((l) => (
                    <Link key={l.id} to={`/lists/${l.id}`} state={{ list: l }} className="block">
                      <ListCard list={l} />
                    </Link>
                  ))}
                </div>
              ) : (
                <EmptyState
                  icon="📋"
                  title={isOwnProfile ? 'Aún no tienes listas' : 'Sin listas públicas'}
                  description={isOwnProfile ? 'Crea tu primera lista para organizar tu contenido.' : undefined}
                />
              )}
            </div>
          </div>
        )}

        {/* Valoraciones ──────────────────────────────────── */}
        {activeTab === 'ratings' && (
          <>
            {isOwnProfile ? (
              ratings.length > 0 ? (
                <div className="grid grid-cols-3 sm:grid-cols-4 md:grid-cols-5 lg:grid-cols-6 xl:grid-cols-7 gap-3">
                  {ratings.map((r) => (
                    <RatingPosterItem key={r.id} r={r} />
                  ))}
                </div>
              ) : (
                <EmptyState
                  icon="⭐"
                  title="Aún no has valorado nada"
                  description="Busca películas o series y deja tu primera reseña."
                />
              )
            ) : (
              <EmptyState
                icon="🔐"
                title="Valoraciones privadas"
                description="Solo el propio usuario puede ver su historial de valoraciones."
              />
            )}
          </>
        )}

        {/* Listas ────────────────────────────────────────── */}
        {activeTab === 'lists' && (
          <>
            {isOwnProfile ? (
              lists.length > 0 ? (
                <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
                  {lists.map((l) => (
                    <Link key={l.id} to={`/lists/${l.id}`} state={{ list: l }} className="block">
                      <ListCard list={l} />
                    </Link>
                  ))}
                </div>
              ) : (
                <EmptyState
                  icon="📋"
                  title="Aún no tienes listas"
                  description="Crea tu primera lista para organizar tu contenido favorito."
                />
              )
            ) : (
              <EmptyState
                icon="🔐"
                title="Listas privadas"
                description="Solo el propio usuario puede ver sus listas."
              />
            )}
          </>
        )}

        {/* Siguiendo ─────────────────────────────────────── */}
        {activeTab === 'following' && (
          <>
            {followingQuery.isLoading ? (
              <div className="space-y-1">
                {[1, 2, 3].map((i) => (
                  <div key={i} className="flex items-center gap-3 px-6 py-3 animate-pulse">
                    <div className="w-9 h-9 rounded-full bg-bg-3" />
                    <div className="space-y-1 flex-1">
                      <div className="h-3 bg-bg-3 rounded w-32" />
                      <div className="h-3 bg-bg-3 rounded w-48" />
                    </div>
                  </div>
                ))}
              </div>
            ) : following.length > 0 ? (
              <div className="divide-y divide-white/[0.04] -mx-6">
                {following.map((u) => (
                  <UserRow key={u.id} user={u} />
                ))}
              </div>
            ) : (
              <EmptyState
                icon="👥"
                title={isOwnProfile ? 'Aún no sigues a nadie' : 'No sigue a nadie todavía'}
                description={isOwnProfile ? 'Descubre cinéfilos interesantes en la sección Descubrir.' : undefined}
              />
            )}
          </>
        )}
      </div>

      {/* ── Dialog editar perfil ────────────────────────── */}
      {isOwnProfile && (
        <EditProfileDialog
          open={editOpen}
          onClose={() => setEditOpen(false)}
          initialBio={user.bio ?? ''}
          initialAvatar={user.avatarUrl ?? ''}
          username={username}
        />
      )}

      {/* ── Dialogs seguidores / siguiendo ──────────────── */}
      <FollowListDialog
        open={followDialog === 'followers'}
        onClose={() => setFollowDialog(null)}
        title="Seguidores"
        users={followersQuery.data ?? []}
        isLoading={followersQuery.isLoading}
      />
      <FollowListDialog
        open={followDialog === 'following'}
        onClose={() => setFollowDialog(null)}
        title="Siguiendo"
        users={followingQuery.data ?? []}
        isLoading={followingQuery.isLoading}
      />
    </div>
  )
}
