import { useState, useRef } from 'react'
import { useParams, useLocation, Link } from 'react-router-dom'
import BackButton from '../../components/shared/BackButton'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import { listsApi } from '../../api/lists'
import { commentsApi } from '../../api/comments'
import { queryKeys } from '../../lib/queryKeys'
import { useAuthStore } from '../../store/authStore'
import PosterCard from '../../components/shared/PosterdCard'
import EmptyState from '../../components/shared/EmptyState'
import AddContentToListDialog from './AddContentToListDialog'
import type { ListResponse, ListType, ListCommentResponse } from '../../types'
import { timeAgo } from '../../lib/utils'

const LIST_TYPE_CONFIG: Record<ListType, { icon: string; label: string }> = {
  FAVORITES: { icon: '❤️',  label: 'Favoritos' },
  WATCHLIST: { icon: '🕐', label: 'Por ver' },
  WATCHED:   { icon: '👁️', label: 'Ya vistas' },
  CUSTOM:    { icon: '📋', label: 'Lista' },
}

function ListDetailSkeleton() {
  return (
    <div className="animate-pulse">
      <div className="px-6 py-5 border-b border-white/[0.06] space-y-2">
        <div className="h-7 bg-bg-3 rounded w-48" />
        <div className="h-4 bg-bg-3 rounded w-72" />
        <div className="h-3 bg-bg-3 rounded w-40 mt-2" />
      </div>
      <div className="px-4 lg:px-6 py-6 flex flex-wrap gap-3">
        {[1, 2, 3, 4, 5, 6].map((i) => (
          <div key={i} className="w-36 aspect-[2/3] bg-bg-3 rounded-xl" />
        ))}
      </div>
    </div>
  )
}

export default function ListDetailPage() {
  const { listId } = useParams<{ listId: string }>()
  const location = useLocation()
  const { isAuthenticated, sessionUser } = useAuthStore()
  const queryClient = useQueryClient()
  const [addDialogOpen, setAddDialogOpen] = useState(false)
  const [commentText, setCommentText] = useState('')
  const commentInputRef = useRef<HTMLTextAreaElement>(null)

  const stateList = location.state?.list as ListResponse | undefined
  const parsedId = Number(listId)

  const { data: list, isLoading } = useQuery({
    queryKey: queryKeys.lists.detail(parsedId),
    queryFn: () => listsApi.getById(parsedId).then((r) => r.data),
    placeholderData: stateList,
    staleTime: 1000 * 60 * 2,
    enabled: !!parsedId,
  })

  const isOwner = isAuthenticated && sessionUser?.username === list?.user?.username

  // ── Comentarios ────────────────────────────────────────────
  const { data: comments = [], isLoading: loadingComments } = useQuery({
    queryKey: queryKeys.comments.byList(parsedId),
    queryFn: () => commentsApi.getByList(parsedId).then((r) => r.data),
    enabled: !!parsedId,
    staleTime: 1000 * 60 * 2,
  })

  const { mutate: postComment, isPending: isPosting } = useMutation({
    mutationFn: (content: string) => commentsApi.createForList(parsedId, { content }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.comments.byList(parsedId) })
      setCommentText('')
    },
    onError: () => toast.error('No se pudo publicar el comentario'),
  })

  const { mutate: deleteComment } = useMutation({
    mutationFn: (commentId: number) => commentsApi.deleteFromList(parsedId, commentId),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: queryKeys.comments.byList(parsedId) }),
    onError: () => toast.error('No se pudo eliminar'),
  })

  const { mutate: removeContent, isPending: isRemoving } = useMutation({
    mutationFn: (tmdbId: number) => listsApi.removeContent(parsedId, tmdbId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.lists.detail(parsedId) })
      queryClient.invalidateQueries({ queryKey: queryKeys.users.lists() })
      queryClient.invalidateQueries({ queryKey: queryKeys.lists.mine() })
      toast.success('Eliminado de la lista')
    },
    onError: () => toast.error('No se pudo eliminar'),
  })

  if (isLoading && !list) return <ListDetailSkeleton />

  if (!list) {
    return (
      <div className="flex flex-col items-center justify-center py-32 text-center">
        <span className="text-5xl mb-4">📋</span>
        <h2 className="text-xl font-bold text-white/80 mb-2">Lista no encontrada</h2>
        <p className="text-sm text-muted">No pudimos cargar la información de esta lista.</p>
      </div>
    )
  }

  const cfg = LIST_TYPE_CONFIG[list.listType]

  return (
    <div className="pb-12">
      {/* Botón volver */}
      <div className="px-4 lg:px-6 pt-4 pb-1">
        <BackButton />
      </div>

      {/* Cabecera */}
      <div className="px-4 lg:px-6 py-5 border-b border-white/[0.06]">
        <div className="flex items-center gap-4 mb-1.5">
          <span className="text-xl shrink-0">{cfg.icon}</span>
          <h1 className="font-display font-bold italic text-2xl">{list.name}</h1>
          {isOwner && (
            <button
              onClick={() => setAddDialogOpen(true)}
              className="shrink-0 flex items-center gap-1.5 px-3.5 py-2 text-sm font-semibold bg-accent hover:bg-accent-light text-bg-0 rounded-xl transition-colors"
            >
              + Añadir
            </button>
          )}
        </div>
        {list.description && (
          <p className="text-sm text-muted mt-1 max-w-xl">{list.description}</p>
        )}
        <div className="flex items-center gap-3 mt-2 text-xs text-muted font-mono flex-wrap">
          <Link
            to={`/profile/${list.user.username}`}
            className="hover:text-white transition-colors"
          >
            @{list.user.username}
          </Link>
          <span>·</span>
          <span>{list.itemCount} título{list.itemCount !== 1 ? 's' : ''}</span>
          <span>·</span>
          <span>{list.isPublic ? '🔓 Pública' : '🔒 Privada'}</span>
          {list.listType !== 'CUSTOM' && (
            <>
              <span>·</span>
              <span className="text-accent/70">{cfg.label}</span>
            </>
          )}
        </div>
      </div>

      {/* Grid de contenidos */}
      <div className="px-4 lg:px-6 py-6">
        {list.contents.length === 0 ? (
          <EmptyState
            icon="🎬"
            title="Lista vacía"
            description={
              isOwner
                ? 'Pulsa "+ Añadir" para agregar títulos a esta lista.'
                : 'Esta lista aún no tiene ningún título.'
            }
          />
        ) : (
          <div className="flex flex-wrap gap-3">
            {list.contents.map((content) => (
              <div key={content.id} className="relative group">
                <PosterCard content={content} />
                {isOwner && (
                  <button
                    onClick={() => {
                      if (!confirm(`¿Eliminar "${content.title}" de esta lista?`)) return
                      removeContent(content.tmdbId)
                    }}
                    disabled={isRemoving}
                    className="absolute top-1.5 right-1.5 z-10 w-6 h-6 bg-red-500/80 hover:bg-red-500 rounded-full flex items-center justify-center text-white text-sm font-bold opacity-0 group-hover:opacity-100 transition-opacity disabled:opacity-50"
                    title="Eliminar de la lista"
                  >
                    ×
                  </button>
                )}
              </div>
            ))}
          </div>
        )}
      </div>

      {/* ── Comentarios ─────────────────────────────────────── */}
      <div className="px-4 lg:px-6 pt-2 pb-6 border-t border-white/[0.06]">
        <h2 className="font-display font-bold italic text-lg mb-4">
          Comentarios {comments.length > 0 && <span className="text-muted font-sans font-normal text-sm">({comments.length})</span>}
        </h2>

        {/* Caja de nuevo comentario */}
        {isAuthenticated && (
          <div className="mb-5">
            <textarea
              ref={commentInputRef}
              value={commentText}
              onChange={(e) => setCommentText(e.target.value)}
              placeholder="Añade un comentario..."
              rows={2}
              className="w-full bg-bg-2 border border-white/[0.08] focus:border-accent/50 rounded-xl px-4 py-3 text-sm text-white/85 placeholder:text-muted resize-none outline-none transition-colors"
            />
            <div className="flex justify-end mt-2">
              <button
                disabled={!commentText.trim() || isPosting}
                onClick={() => commentText.trim() && postComment(commentText.trim())}
                className="px-4 py-1.5 text-sm font-semibold bg-accent hover:bg-accent-light text-bg-0 rounded-xl transition-colors disabled:opacity-40"
              >
                {isPosting ? 'Publicando…' : 'Publicar'}
              </button>
            </div>
          </div>
        )}

        {/* Lista de comentarios */}
        {loadingComments ? (
          <div className="space-y-3">
            {[1, 2].map((i) => (
              <div key={i} className="flex gap-3 animate-pulse">
                <div className="w-8 h-8 rounded-full bg-bg-3 shrink-0" />
                <div className="flex-1 space-y-2">
                  <div className="h-3 bg-bg-3 rounded w-32" />
                  <div className="h-3 bg-bg-3 rounded w-full" />
                </div>
              </div>
            ))}
          </div>
        ) : comments.length === 0 ? (
          <p className="text-sm text-muted text-center py-6">
            Sin comentarios todavía. ¡Sé el primero!
          </p>
        ) : (
          <div className="space-y-4">
            {comments.map((c: ListCommentResponse) => (
              <div key={c.id} className="flex gap-3 group">
                <Link to={`/profile/${c.author.username}`} className="shrink-0">
                  {c.author.avatarUrl ? (
                    <img
                      src={c.author.avatarUrl}
                      alt={c.author.username}
                      className="w-8 h-8 rounded-full object-cover border border-white/[0.08]"
                    />
                  ) : (
                    <div className="w-8 h-8 rounded-full bg-gradient-to-br from-accent/70 to-purple-500/70 flex items-center justify-center text-xs font-bold text-bg-0">
                      {c.author.username[0].toUpperCase()}
                    </div>
                  )}
                </Link>
                <div className="flex-1 min-w-0">
                  <div className="flex items-baseline gap-2 mb-0.5">
                    <Link
                      to={`/profile/${c.author.username}`}
                      className="text-sm font-medium text-white/80 hover:text-accent transition-colors"
                    >
                      @{c.author.username}
                    </Link>
                    <span className="text-xs text-muted">{timeAgo(c.createdAt)}</span>
                  </div>
                  <p className="text-sm text-white/70 break-words">{c.content}</p>
                </div>
                {sessionUser?.username === c.author.username && (
                  <button
                    onClick={() => deleteComment(c.id)}
                    className="shrink-0 opacity-0 group-hover:opacity-100 text-muted hover:text-red-400 text-xs transition-all"
                    title="Eliminar comentario"
                  >
                    ×
                  </button>
                )}
              </div>
            ))}
          </div>
        )}
      </div>

      {isOwner && (
        <AddContentToListDialog
          open={addDialogOpen}
          onClose={() => setAddDialogOpen(false)}
          listId={parsedId}
          listName={list.name}
          existingTmdbIds={new Set(list.contents.map((c) => c.tmdbId))}
        />
      )}
    </div>
  )
}
