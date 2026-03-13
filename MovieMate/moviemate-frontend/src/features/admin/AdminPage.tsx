import { useState } from 'react'
import { toast } from 'sonner'
import { useDebounce } from '../../hooks/useDebounce'
import {
  useAdminUsers,
  useChangeRole,
  useBanUser,
  useAdminReports,
  useResolveReport,
  useDismissReport,
  useAdminRating,
  useAdminComment,
} from '../../hooks/useAdmin'
import type { ReportStatus } from '../../api/admin'
import type { UserResponse, ReportResponse } from '../../types'

type Tab = 'users' | 'reports'

// ── Fila de usuario ───────────────────────────────────────────
function UserRow({ user }: { user: UserResponse }) {
  const { mutate: changeRole, isPending: changingRole } = useChangeRole()
  const { mutate: banUser, isPending: banning } = useBanUser()

  return (
    <div className="flex items-center gap-3 px-4 py-3 border-b border-white/[0.04] hover:bg-bg-2 transition-colors">
      {/* Avatar */}
      <div className="w-8 h-8 rounded-full bg-gradient-to-br from-accent to-pink-500 flex items-center justify-center text-xs font-bold text-bg-0 shrink-0 overflow-hidden">
        {user.avatarUrl ? (
          <img src={user.avatarUrl} alt={user.username} className="w-full h-full object-cover" />
        ) : (
          user.username.charAt(0).toUpperCase()
        )}
      </div>

      {/* Info */}
      <div className="flex-1 min-w-0">
        <div className="flex items-center gap-2">
          <span className="text-sm font-medium text-white/90 truncate">{user.username}</span>
          {user.role === 'ADMIN' && (
            <span className="text-[0.6rem] font-bold bg-accent/20 text-accent px-1.5 py-0.5 rounded-full">
              ADMIN
            </span>
          )}
          {user.banned && (
            <span className="text-[0.6rem] font-bold bg-red-500/20 text-red-400 px-1.5 py-0.5 rounded-full">
              BANEADO
            </span>
          )}
        </div>
        <p className="text-xs text-muted truncate">{user.email}</p>
      </div>

      {/* Acciones */}
      <div className="flex items-center gap-2 shrink-0">
        <button
          onClick={() => changeRole(
            { userId: user.id, role: user.role === 'ADMIN' ? 'USER' : 'ADMIN' },
            { onSuccess: () => toast.success('Rol actualizado') }
          )}
          disabled={changingRole}
          className="text-xs px-3 py-1.5 border border-white/[0.1] rounded-lg hover:border-accent/50 hover:text-accent transition-colors disabled:opacity-40"
        >
          {user.role === 'ADMIN' ? 'Quitar admin' : 'Hacer admin'}
        </button>
        <button
          onClick={() => banUser(
            { userId: user.id, banned: !user.banned },
            { onSuccess: () => toast.success(user.banned ? 'Usuario desbaneado' : 'Usuario baneado') }
          )}
          disabled={banning}
          className={`text-xs px-3 py-1.5 border rounded-lg transition-colors disabled:opacity-40 ${
            user.banned
              ? 'border-green-500/30 text-green-400 hover:bg-green-500/10'
              : 'border-red-500/30 text-red-400 hover:bg-red-500/10'
          }`}
        >
          {user.banned ? 'Desbanear' : 'Banear'}
        </button>
      </div>
    </div>
  )
}

// ── Fila de reporte ───────────────────────────────────────────
const REASON_LABELS: Record<string, string> = {
  SPAM:          '🚫 Spam',
  INAPPROPRIATE: '⚠️ Inapropiado',
  SPOILER:       '🎭 Spoiler',
  OTHER:         '📝 Otro',
}

const STATUS_LABEL: Record<string, string> = {
  PENDING:   'PENDIENTE',
  RESOLVED:  'RESUELTO',
  DISMISSED: 'DESESTIMADO',
}

function ReportContentPreview({ report }: { report: ReportResponse }) {
  const ratingQ  = useAdminRating(report.targetId, report.targetType === 'RATING')
  const commentQ = useAdminComment(report.targetId, report.targetType === 'COMMENT')

  if (ratingQ.isLoading || commentQ.isLoading) {
    return <div className="mt-3 animate-pulse text-xs text-muted">Cargando contenido…</div>
  }

  if (report.targetType === 'RATING') {
    const r = ratingQ.data
    if (!r) return <p className="mt-3 text-xs text-red-400/70">Contenido ya eliminado.</p>
    return (
      <div className="mt-3 bg-bg-0 border border-white/[0.06] rounded-lg p-3 space-y-1.5">
        <div className="flex items-center gap-2">
          <span className="text-xs font-semibold text-white/80">{r.user.username}</span>
          <div className="flex">
            {[1,2,3,4,5].map((n) => (
              <span key={n} className={`text-xs ${n <= r.rating ? 'text-yellow-400' : 'text-white/15'}`}>★</span>
            ))}
          </div>
          {r.content && (
            <span className="text-xs text-muted truncate max-w-[160px]">{r.content.title}</span>
          )}
        </div>
        {r.reviewText && (
          <p className="text-xs text-white/60 leading-relaxed">{r.reviewText}</p>
        )}
      </div>
    )
  }

  const c = commentQ.data
  if (!c) return <p className="mt-3 text-xs text-red-400/70">Contenido ya eliminado.</p>
  return (
    <div className="mt-3 bg-bg-0 border border-white/[0.06] rounded-lg p-3">
      <span className="text-xs font-semibold text-white/80 mr-2">{c.author.username}</span>
      <span className="text-xs text-white/60">{c.content}</span>
    </div>
  )
}

function ReportRow({ report }: { report: ReportResponse }) {
  const [expanded, setExpanded] = useState(false)
  const { mutate: resolve, isPending: resolving } = useResolveReport()
  const { mutate: dismiss, isPending: dismissing } = useDismissReport()

  return (
    <div className="px-4 py-3 border-b border-white/[0.04] hover:bg-bg-2 transition-colors">
      <div className="flex items-start gap-3">
        <div className="flex-1 min-w-0">
          <div className="flex items-center gap-2 flex-wrap mb-1">
            {/* Botón para ver el contenido */}
            <button
              onClick={() => setExpanded((v) => !v)}
              className="text-xs font-medium text-white/90 hover:text-accent transition-colors flex items-center gap-1"
            >
              {report.targetType === 'RATING' ? '⭐ Valoración' : '💬 Comentario'} #{report.targetId}
              <span className="text-muted">{expanded ? '▲' : '▼'}</span>
            </button>
            <span className="text-xs text-muted">{REASON_LABELS[report.reason] ?? report.reason}</span>
            <span className={`text-[0.6rem] font-bold px-1.5 py-0.5 rounded-full ${
              report.status === 'PENDING'   ? 'bg-yellow-500/20 text-yellow-400' :
              report.status === 'RESOLVED'  ? 'bg-green-500/20 text-green-400' :
                                              'bg-white/10 text-muted'
            }`}>
              {STATUS_LABEL[report.status] ?? report.status}
            </span>
          </div>
          <p className="text-xs text-muted">
            Reportado por <span className="text-white/70">{report.reporter.username}</span>
            {' · '}{new Date(report.createdAt).toLocaleDateString('es-ES')}
          </p>

          {/* Preview del contenido */}
          {expanded && <ReportContentPreview report={report} />}
        </div>

        {report.status === 'PENDING' && (
          <div className="flex items-center gap-2 shrink-0">
            <button
              onClick={() => resolve(report.id, { onSuccess: () => toast.success('Reporte resuelto y contenido eliminado') })}
              disabled={resolving}
              className="text-xs px-3 py-1.5 border border-green-500/30 text-green-400 rounded-lg hover:bg-green-500/10 disabled:opacity-40 transition-colors"
            >
              Resolver
            </button>
            <button
              onClick={() => dismiss(report.id, { onSuccess: () => toast.success('Reporte desestimado') })}
              disabled={dismissing}
              className="text-xs px-3 py-1.5 border border-white/[0.1] text-muted rounded-lg hover:border-white/20 disabled:opacity-40 transition-colors"
            >
              Desestimar
            </button>
          </div>
        )}
      </div>
    </div>
  )
}

// ── Página principal ──────────────────────────────────────────
export default function AdminPage() {
  const [tab, setTab] = useState<Tab>('users')
  const [search, setSearch] = useState('')
  const [reportStatus, setReportStatus] = useState<ReportStatus | undefined>('PENDING')
  const debouncedSearch = useDebounce(search, 300)

  const usersQuery = useAdminUsers(debouncedSearch || undefined)
  const reportsQuery = useAdminReports(reportStatus)

  return (
    <div className="max-w-4xl mx-auto px-4 lg:px-6 py-6">
      <h1 className="font-display font-bold italic text-3xl mb-6">Panel de administración</h1>

      {/* Tabs */}
      <div className="flex gap-1 bg-bg-2 rounded-xl p-1 mb-6 w-fit">
        {([['users', '👤 Usuarios'], ['reports', '🚩 Reportes']] as const).map(([t, label]) => (
          <button
            key={t}
            onClick={() => setTab(t)}
            className={`px-4 py-2 text-sm rounded-lg transition-colors ${
              tab === t
                ? 'bg-bg-1 text-white font-medium'
                : 'text-muted hover:text-white'
            }`}
          >
            {label}
          </button>
        ))}
      </div>

      {/* Usuarios */}
      {tab === 'users' && (
        <div className="bg-bg-1 border border-white/[0.06] rounded-xl overflow-hidden">
          <div className="p-4 border-b border-white/[0.06]">
            <input
              type="text"
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              placeholder="Buscar por usuario o email…"
              className="w-full max-w-sm bg-bg-2 border border-white/[0.08] rounded-xl px-3.5 py-2 text-sm text-white placeholder:text-muted outline-none focus:border-accent/50 transition-colors"
            />
          </div>
          {usersQuery.isLoading ? (
            <div className="p-4 text-muted text-sm animate-pulse">Cargando usuarios…</div>
          ) : usersQuery.data?.length === 0 ? (
            <div className="p-4 text-muted text-sm">No se encontraron usuarios.</div>
          ) : (
            <div>
              {usersQuery.data?.map((u) => <UserRow key={u.id} user={u} />)}
            </div>
          )}
        </div>
      )}

      {/* Reportes */}
      {tab === 'reports' && (
        <div className="bg-bg-1 border border-white/[0.06] rounded-xl overflow-hidden">
          <div className="p-4 border-b border-white/[0.06] flex gap-2">
            {(['PENDING', 'RESOLVED', 'DISMISSED', undefined] as const).map((s) => (
              <button
                key={String(s)}
                onClick={() => setReportStatus(s)}
                className={`px-3 py-1.5 text-xs rounded-lg transition-colors ${
                  reportStatus === s
                    ? 'bg-accent text-bg-0 font-medium'
                    : 'text-muted border border-white/[0.08] hover:border-white/20'
                }`}
              >
                {s ?? 'Todos'}
              </button>
            ))}
          </div>
          {reportsQuery.isLoading ? (
            <div className="p-4 text-muted text-sm animate-pulse">Cargando reportes…</div>
          ) : reportsQuery.data?.length === 0 ? (
            <div className="p-4 text-muted text-sm">No hay reportes.</div>
          ) : (
            <div>
              {reportsQuery.data?.map((r) => <ReportRow key={r.id} report={r} />)}
            </div>
          )}
        </div>
      )}
    </div>
  )
}
