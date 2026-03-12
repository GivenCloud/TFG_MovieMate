import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import { listsApi } from '../../api/lists'
import { queryKeys } from '../../lib/queryKeys'
import EmptyState from '../../components/shared/EmptyState'
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from '../../components/ui/dialog'
import type { ListResponse, ListType, ContentResponse } from '../../types'

// ── Config de tipos de lista ─────────────────────────────────
const LIST_TYPE_CONFIG: Record<ListType, { icon: string; label: string }> = {
  FAVORITES: { icon: '❤️',  label: 'Favoritos' },
  WATCHLIST: { icon: '🕐', label: 'Por ver' },
  WATCHED:   { icon: '👁️', label: 'Ya vistas' },
  CUSTOM:    { icon: '📋', label: 'Lista' },
}

type Filter = 'all' | 'public' | 'private' | 'movies' | 'series'

// ── Tarjeta de lista ─────────────────────────────────────────
function ListCard({ list }: { list: ListResponse }) {
  const previews = list.contents.slice(0, 4)
  const cfg = LIST_TYPE_CONFIG[list.listType]

  return (
    <div className="bg-bg-1 border border-white/[0.06] rounded-xl overflow-hidden hover:border-white/[0.14] transition-all group cursor-pointer">
      {/* Mosaico de posters 2×2 */}
      <div className="grid grid-cols-4 h-24 bg-bg-3">
        {previews.map((c: ContentResponse) => (
          <div key={c.id} className="overflow-hidden border-r border-bg-0 last:border-0">
            {c.posterUrl ? (
              <img
                src={c.posterUrl}
                alt={c.title}
                className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
              />
            ) : (
              <div className="w-full h-full flex items-center justify-center text-xl text-muted">
                🎬
              </div>
            )}
          </div>
        ))}
        {/* Celdas vacías */}
        {Array.from({ length: Math.max(0, 4 - previews.length) }).map((_, i) => (
          <div key={`empty-${i}`} className="bg-bg-3 border-r border-bg-0 last:border-0" />
        ))}
      </div>

      {/* Info */}
      <div className="px-4 py-3">
        <div className="flex items-start justify-between gap-2 mb-1">
          <h3 className="text-sm font-semibold text-white/90 leading-snug line-clamp-1">
            {cfg.icon} {list.name}
          </h3>
        </div>
        {list.description && (
          <p className="text-xs text-muted line-clamp-2 mb-2">{list.description}</p>
        )}
        <div className="flex items-center gap-2 text-[0.65rem] text-muted font-mono flex-wrap">
          <span>{list.itemCount} títulos</span>
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
    </div>
  )
}

// ── Dialog: editar lista ─────────────────────────────────────
function EditListDialog({
  list,
  onClose,
}: {
  list: ListResponse
  onClose: () => void
}) {
  const queryClient = useQueryClient()
  const [name, setName] = useState(list.name)
  const [description, setDescription] = useState(list.description ?? '')
  const [isPublic, setIsPublic] = useState(list.isPublic)
  const [nameError, setNameError] = useState('')

  const update = useMutation({
    mutationFn: () =>
      listsApi.updateList(list.id, {
        name: list.listType === 'CUSTOM' ? name.trim() : list.name,
        description: description.trim() || undefined,
        isPublic,
      }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.users.lists() })
      toast.success('Lista actualizada')
      onClose()
    },
    onError: (err: any) => {
      setNameError(err?.response?.data?.message ?? 'No se pudo actualizar la lista.')
    },
  })

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    if (!name.trim()) { setNameError('El nombre es obligatorio.'); return }
    if (name.trim().length < 2) { setNameError('Mínimo 2 caracteres.'); return }
    setNameError('')
    update.mutate()
  }

  return (
    <Dialog open onOpenChange={(v) => !v && onClose()}>
      <DialogContent className="bg-bg-1 border-white/[0.1] text-white max-w-md">
        <DialogHeader>
          <DialogTitle className="font-display font-bold italic text-xl">Editar lista</DialogTitle>
        </DialogHeader>

        <form onSubmit={handleSubmit} className="space-y-4 mt-2">
          <div>
            <label className="block font-mono text-xs text-muted tracking-wider uppercase mb-1.5">
              Nombre
            </label>
            {list.listType === 'CUSTOM' ? (
              <>
                <input
                  type="text"
                  required
                  maxLength={80}
                  value={name}
                  onChange={(e) => { setName(e.target.value); setNameError('') }}
                  className={`w-full bg-bg-2 border rounded-xl px-3.5 py-2.5 text-sm text-white placeholder:text-muted outline-none transition-all
                    ${nameError
                      ? 'border-red-500/60 focus:border-red-500/80 focus:ring-2 focus:ring-red-500/10'
                      : 'border-white/[0.1] focus:border-accent/50 focus:ring-2 focus:ring-accent/10'
                    }`}
                />
                {nameError && <p className="text-xs text-red-400 mt-1.5">{nameError}</p>}
              </>
            ) : (
              <div className="flex items-center gap-3 bg-bg-2 border border-white/[0.06] rounded-xl px-3.5 py-2.5">
                <span className="text-sm text-white/60 flex-1">{list.name}</span>
                <span className="text-[0.65rem] font-mono text-muted bg-bg-3 px-2 py-0.5 rounded shrink-0">
                  No editable
                </span>
              </div>
            )}
          </div>

          <div>
            <label className="block font-mono text-xs text-muted tracking-wider uppercase mb-1.5">
              Descripción <span className="normal-case">(opcional)</span>
            </label>
            <textarea
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              maxLength={300}
              rows={2}
              placeholder="Describe tu lista..."
              className="w-full bg-bg-2 border border-white/[0.1] rounded-xl px-3.5 py-2.5 text-sm text-white placeholder:text-muted outline-none focus:border-accent/50 focus:ring-2 focus:ring-accent/10 transition-all resize-none"
            />
          </div>

          <div className="flex items-center justify-between bg-bg-2 border border-white/[0.06] rounded-xl px-4 py-3">
            <div>
              <p className="text-sm font-medium text-white/90">
                {isPublic ? '🔓 Lista pública' : '🔒 Lista privada'}
              </p>
              <p className="text-xs text-muted mt-0.5">
                {isPublic ? 'Otros usuarios pueden ver esta lista.' : 'Solo tú puedes ver esta lista.'}
              </p>
            </div>
            <button
              type="button"
              onClick={() => setIsPublic((v) => !v)}
              className={`relative w-11 h-6 rounded-full transition-colors shrink-0
                ${isPublic ? 'bg-accent' : 'bg-bg-3 border border-white/[0.1]'}`}
            >
              <span
                className={`absolute top-0.5 w-5 h-5 bg-white rounded-full shadow transition-transform
                  ${isPublic ? 'translate-x-5' : 'translate-x-0.5'}`}
              />
            </button>
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

// ── Tarjeta de lista propia (con botones edit/delete en CUSTOM) ──
function MyListCard({ list }: { list: ListResponse }) {
  const [editOpen, setEditOpen] = useState(false)
  const queryClient = useQueryClient()

  const deleteMutation = useMutation({
    mutationFn: () => listsApi.deleteList(list.id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.users.lists() })
      toast.success('Lista eliminada')
    },
    onError: (err: any) => {
      toast.error(err?.response?.data?.message ?? 'No se pudo eliminar la lista.')
    },
  })

  const isCustom = list.listType === 'CUSTOM'

  return (
    <>
      <div className="relative group">
        <Link to={`/lists/${list.id}`} state={{ list }} className="block">
          <ListCard list={list} />
        </Link>
        <div className="absolute top-2 right-2 flex gap-1 opacity-0 group-hover:opacity-100 transition-opacity z-10">
          <button
            onClick={(e) => { e.preventDefault(); e.stopPropagation(); setEditOpen(true) }}
            className="w-7 h-7 bg-bg-0/80 hover:bg-accent text-white hover:text-bg-0 rounded-lg flex items-center justify-center text-xs transition-colors"
            title="Editar lista"
          >
            ✏
          </button>
          {isCustom && (
            <button
              onClick={(e) => { e.preventDefault(); e.stopPropagation(); deleteMutation.mutate() }}
              disabled={deleteMutation.isPending}
              className="w-7 h-7 bg-bg-0/80 hover:bg-red-500 text-white rounded-lg flex items-center justify-center text-sm font-bold transition-colors disabled:opacity-50"
              title="Eliminar lista"
            >
              ×
            </button>
          )}
        </div>
      </div>
      {editOpen && <EditListDialog list={list} onClose={() => setEditOpen(false)} />}
    </>
  )
}

// ── Tarjeta "Crear nueva lista" ──────────────────────────────
function CreateListCard({ onClick }: { onClick: () => void }) {
  return (
    <button
      onClick={onClick}
      className="bg-transparent border-2 border-dashed border-white/[0.1] rounded-xl min-h-[168px] flex flex-col items-center justify-center gap-2 hover:border-accent/40 hover:bg-accent/[0.03] transition-all group"
    >
      <span className="text-3xl text-muted group-hover:text-accent transition-colors">+</span>
      <span className="text-sm font-medium text-muted group-hover:text-white/70 transition-colors">
        Crear nueva lista
      </span>
    </button>
  )
}

// ── Dialog: crear lista ──────────────────────────────────────
function CreateListDialog({
  open,
  onClose,
}: {
  open: boolean
  onClose: () => void
}) {
  const queryClient = useQueryClient()
  const [name, setName] = useState('')
  const [description, setDescription] = useState('')
  const [isPublic, setIsPublic] = useState(true)
  const [nameError, setNameError] = useState('')

  const create = useMutation({
    mutationFn: () =>
      listsApi.create({ name: name.trim(), description: description.trim() || undefined, isPublic, listType: 'CUSTOM' }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.users.lists() })
      handleClose()
    },
    onError: (err: any) => {
      const msg = err?.response?.data?.message ?? 'No se pudo crear la lista.'
      setNameError(msg)
    },
  })

  const handleClose = () => {
    setName('')
    setDescription('')
    setIsPublic(true)
    setNameError('')
    onClose()
  }

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    if (!name.trim()) { setNameError('El nombre es obligatorio.'); return }
    if (name.trim().length < 2) { setNameError('Mínimo 2 caracteres.'); return }
    setNameError('')
    create.mutate()
  }

  return (
    <Dialog open={open} onOpenChange={(v) => !v && handleClose()}>
      <DialogContent className="bg-bg-1 border-white/[0.1] text-white max-w-md">
        <DialogHeader>
          <DialogTitle className="font-display font-bold italic text-xl">Nueva lista</DialogTitle>
        </DialogHeader>

        <form onSubmit={handleSubmit} className="space-y-4 mt-2">
          {/* Nombre */}
          <div>
            <label className="block font-mono text-xs text-muted tracking-wider uppercase mb-1.5">
              Nombre
            </label>
            <input
              type="text"
              required
              maxLength={80}
              value={name}
              onChange={(e) => { setName(e.target.value); setNameError('') }}
              placeholder="Mis favoritas de 2024"
              className={`w-full bg-bg-2 border rounded-xl px-3.5 py-2.5 text-sm text-white placeholder:text-muted outline-none transition-all
                ${nameError
                  ? 'border-red-500/60 focus:border-red-500/80 focus:ring-2 focus:ring-red-500/10'
                  : 'border-white/[0.1] focus:border-accent/50 focus:ring-2 focus:ring-accent/10'
                }`}
            />
            {nameError && <p className="text-xs text-red-400 mt-1.5">{nameError}</p>}
          </div>

          {/* Descripción */}
          <div>
            <label className="block font-mono text-xs text-muted tracking-wider uppercase mb-1.5">
              Descripción <span className="normal-case">(opcional)</span>
            </label>
            <textarea
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              maxLength={300}
              rows={2}
              placeholder="Describe tu lista..."
              className="w-full bg-bg-2 border border-white/[0.1] rounded-xl px-3.5 py-2.5 text-sm text-white placeholder:text-muted outline-none focus:border-accent/50 focus:ring-2 focus:ring-accent/10 transition-all resize-none"
            />
          </div>

          {/* Visibilidad */}
          <div className="flex items-center justify-between bg-bg-2 border border-white/[0.06] rounded-xl px-4 py-3">
            <div>
              <p className="text-sm font-medium text-white/90">
                {isPublic ? '🔓 Lista pública' : '🔒 Lista privada'}
              </p>
              <p className="text-xs text-muted mt-0.5">
                {isPublic ? 'Otros usuarios pueden ver esta lista.' : 'Solo tú puedes ver esta lista.'}
              </p>
            </div>
            <button
              type="button"
              onClick={() => setIsPublic((v) => !v)}
              className={`relative w-11 h-6 rounded-full transition-colors shrink-0
                ${isPublic ? 'bg-accent' : 'bg-bg-3 border border-white/[0.1]'}`}
            >
              <span
                className={`absolute top-0.5 w-5 h-5 bg-white rounded-full shadow transition-transform
                  ${isPublic ? 'translate-x-5' : 'translate-x-0.5'}`}
              />
            </button>
          </div>

          <DialogFooter>
            <button
              type="button"
              onClick={handleClose}
              className="px-4 py-2 text-sm text-muted hover:text-white border border-white/[0.1] rounded-xl transition-colors"
            >
              Cancelar
            </button>
            <button
              type="submit"
              disabled={create.isPending}
              className="px-4 py-2 text-sm font-semibold bg-accent hover:bg-accent-light text-bg-0 rounded-xl disabled:opacity-60 transition-colors"
            >
              {create.isPending ? 'Creando…' : 'Crear lista'}
            </button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}

// ── Esqueleto de carga ───────────────────────────────────────
function ListsSkeleton() {
  return (
    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4 px-6 py-6">
      {[1, 2, 3, 4].map((i) => (
        <div key={i} className="bg-bg-1 border border-white/[0.06] rounded-xl overflow-hidden animate-pulse">
          <div className="h-24 bg-bg-3" />
          <div className="px-4 py-3 space-y-2">
            <div className="h-4 bg-bg-3 rounded w-3/4" />
            <div className="h-3 bg-bg-3 rounded w-1/2" />
          </div>
        </div>
      ))}
    </div>
  )
}

type Tab = 'mine' | 'explore'

// ── Página principal ─────────────────────────────────────────
export default function ListsPage() {
  const [tab, setTab] = useState<Tab>('mine')
  const [filter, setFilter] = useState<Filter>('all')
  const [createOpen, setCreateOpen] = useState(false)

  const { data: myLists = [], isLoading: loadingMine } = useQuery({
    queryKey: queryKeys.users.lists(),
    queryFn: () => listsApi.getMine().then((r) => r.data),
    staleTime: 1000 * 60 * 2,
  })

  const { data: publicLists = [], isLoading: loadingPublic } = useQuery({
    queryKey: queryKeys.lists.public(),
    queryFn: () => listsApi.getPublic().then((r) => r.data),
    enabled: tab === 'explore',
    staleTime: 1000 * 60 * 5,
  })

  // Filtrado de mis listas
  const filtered = myLists.filter((l) => {
    if (filter === 'public')  return l.isPublic
    if (filter === 'private') return !l.isPublic
    if (filter === 'movies')  return l.contents.some((c) => c.contentType === 'MOVIE')
    if (filter === 'series')  return l.contents.some((c) => c.contentType === 'TV')
    return true
  })

  const FILTERS: { id: Filter; label: string }[] = [
    { id: 'all',     label: 'Todas' },
    { id: 'public',  label: 'Públicas' },
    { id: 'private', label: 'Privadas' },
    { id: 'movies',  label: 'Películas' },
    { id: 'series',  label: 'Series' },
  ]

  const isLoading = tab === 'mine' ? loadingMine : loadingPublic

  return (
    <div className="pb-12">
      {/* ── Cabecera ─────────────────────────────────────── */}
      <div className="flex items-center justify-between px-4 lg:px-6 py-5 border-b border-white/[0.06]">
        <h1 className="font-display font-bold italic text-2xl">Listas</h1>
        <button
          onClick={() => setCreateOpen(true)}
          className="flex items-center gap-1.5 px-4 py-2 text-sm font-semibold bg-accent hover:bg-accent-light text-bg-0 rounded-xl transition-all hover:-translate-y-0.5 hover:shadow-lg hover:shadow-accent/20"
        >
          + Nueva lista
        </button>
      </div>

      {/* ── Tabs ──────────────────────────────────────────── */}
      <div className="flex gap-1 px-4 lg:px-6 border-b border-white/[0.06]">
        {(['mine', 'explore'] as const).map((t) => (
          <button
            key={t}
            onClick={() => setTab(t)}
            className={`px-4 py-3 text-sm font-medium border-b-2 -mb-px transition-colors
              ${tab === t
                ? 'text-accent border-accent'
                : 'text-muted hover:text-white border-transparent'
              }`}
          >
            {t === 'mine' ? 'Mis listas' : 'Explorar'}
          </button>
        ))}
      </div>

      {/* ── Filtros (solo en "Mis listas") ─────────────────── */}
      {tab === 'mine' && (
        <div className="flex gap-2 px-4 lg:px-6 py-3 border-b border-white/[0.06] overflow-x-auto scrollbar-none">
          {FILTERS.map(({ id, label }) => (
            <button
              key={id}
              onClick={() => setFilter(id)}
              className={`px-3.5 py-1.5 rounded-full text-xs font-medium whitespace-nowrap transition-colors
                ${filter === id
                  ? 'bg-accent text-bg-0'
                  : 'bg-bg-2 text-muted hover:text-white border border-white/[0.06]'
                }`}
            >
              {label}
            </button>
          ))}
        </div>
      )}

      {/* ── Contenido ─────────────────────────────────────── */}
      {isLoading ? (
        <ListsSkeleton />
      ) : tab === 'mine' ? (
        <div className="px-4 lg:px-6 py-6">
          {filtered.length === 0 && filter === 'all' ? (
            <EmptyState
              icon="📋"
              title="Aún no tienes listas"
              description="Crea tu primera lista para organizar películas y series."
              action={
                <button
                  onClick={() => setCreateOpen(true)}
                  className="px-4 py-2 text-sm font-semibold bg-accent hover:bg-accent-light text-bg-0 rounded-xl transition-colors"
                >
                  + Crear lista
                </button>
              }
            />
          ) : filtered.length === 0 ? (
            <EmptyState
              icon="🔍"
              title="No hay listas con ese filtro"
              description="Prueba con otro filtro o crea una nueva lista."
            />
          ) : (
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
              {filtered.map((list) => (
                <MyListCard key={list.id} list={list} />
              ))}
              {filter === 'all' && (
                <CreateListCard onClick={() => setCreateOpen(true)} />
              )}
            </div>
          )}
        </div>
      ) : (
        <div className="px-4 lg:px-6 py-6">
          {publicLists.length === 0 ? (
            <EmptyState
              icon="🌐"
              title="No hay listas públicas"
              description="Aún no hay listas públicas de otros usuarios."
            />
          ) : (
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
              {publicLists.map((list) => (
                <Link key={list.id} to={`/lists/${list.id}`} state={{ list }} className="block">
                  <ListCard list={list} />
                </Link>
              ))}
            </div>
          )}
        </div>
      )}

      <CreateListDialog open={createOpen} onClose={() => setCreateOpen(false)} />
    </div>
  )
}
