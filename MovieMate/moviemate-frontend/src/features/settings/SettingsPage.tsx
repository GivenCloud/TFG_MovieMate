import { useState, useEffect } from 'react'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import { useMyProfile, useLogout } from '../../hooks/useAuth'
import { useUpdateProfile } from '../../hooks/useProfile'
import { useAuthStore } from '../../store/authStore'
import { usersApi } from '../../api/users'
import { queryKeys } from '../../lib/queryKeys'

// ── Esqueleto de carga ───────────────────────────────────────
function SettingsSkeleton() {
  return (
    <div className="max-w-2xl px-6 py-6 space-y-4 animate-pulse">
      {[1, 2, 3].map((i) => (
        <div key={i} className="bg-bg-1 border border-white/[0.06] rounded-2xl overflow-hidden">
          <div className="px-6 py-4 border-b border-white/[0.06]">
            <div className="h-4 bg-bg-3 rounded w-32" />
          </div>
          <div className="px-6 py-5 space-y-3">
            <div className="h-10 bg-bg-3 rounded-xl" />
            <div className="h-20 bg-bg-3 rounded-xl" />
          </div>
        </div>
      ))}
    </div>
  )
}

// ── Campo readonly ───────────────────────────────────────────
function ReadonlyField({ label, value }: { label: string; value: string }) {
  return (
    <div>
      <label className="block text-xs font-mono text-muted uppercase tracking-wider mb-1.5">
        {label}
      </label>
      <div className="flex items-center gap-3 bg-bg-2 border border-white/[0.06] rounded-xl px-3.5 py-2.5">
        <span className="text-sm text-white/60 flex-1">{value}</span>
        <span className="text-[0.65rem] font-mono text-muted bg-bg-3 px-2 py-0.5 rounded shrink-0">
          No editable
        </span>
      </div>
    </div>
  )
}

// ── Página principal ─────────────────────────────────────────
export default function SettingsPage() {
  const logout = useLogout()
  const { sessionUser } = useAuthStore()
  const queryClient = useQueryClient()

  const { data: me, isLoading } = useMyProfile()

  const [bio, setBio] = useState('')
  const [avatarUrl, setAvatarUrl] = useState('')
  const [avatarError, setAvatarError] = useState(false)
  const [confirmLogout, setConfirmLogout] = useState(false)

  // Inicializa el formulario cuando carga el perfil
  useEffect(() => {
    if (me) {
      setBio(me.bio ?? '')
      setAvatarUrl(me.avatarUrl ?? '')
      setAvatarError(false)
    }
  }, [me])

  const { mutate: saveProfile, isPending: isSaving } = useUpdateProfile(
    sessionUser?.username ?? ''
  )

  const { mutate: togglePrivacy, isPending: isTogglingPrivacy } = useMutation({
    mutationFn: (isPublic: boolean) => usersApi.updatePublicStatus(isPublic),
    onSuccess: ({ data }) => {
      queryClient.setQueryData(queryKeys.users.me(), data)
      toast.success(data.isPublic ? 'Perfil ahora público' : 'Perfil ahora privado')
    },
    onError: () => toast.error('No se pudo cambiar la privacidad'),
  })

  const handleSaveProfile = () => {
    saveProfile(
      {
        bio: bio.trim() || undefined,
        avatarUrl: avatarUrl.trim() || undefined,
      },
      {
        onSuccess: () => toast.success('Perfil actualizado'),
        onError: (err: any) =>
          toast.error(err?.response?.data?.message ?? 'Error al guardar el perfil'),
      }
    )
  }

  if (isLoading) return <SettingsSkeleton />

  return (
    <div className="pb-12">
      {/* Cabecera */}
      <div className="px-4 lg:px-6 py-5 border-b border-white/[0.06]">
        <h1 className="font-display font-bold italic text-2xl">Ajustes</h1>
        <p className="text-sm text-muted mt-0.5">Gestiona tu cuenta y preferencias</p>
      </div>

      <div className="max-w-2xl mx-auto px-4 lg:px-6 py-6 space-y-5">

        {/* ── Perfil público ─────────────────────────────── */}
        <section className="bg-bg-1 border border-white/[0.06] rounded-2xl overflow-hidden">
          <div className="px-6 py-4 border-b border-white/[0.06]">
            <h2 className="text-sm font-semibold text-white/90">Perfil público</h2>
            <p className="text-xs text-muted mt-0.5">Información visible en tu perfil</p>
          </div>

          <div className="px-6 py-5 space-y-5">
            {/* Avatar */}
            <div className="flex items-start gap-4">
              <div className="w-16 h-16 rounded-full bg-gradient-to-br from-accent to-pink-500 flex items-center justify-center text-2xl font-bold text-bg-0 shrink-0 overflow-hidden border-2 border-white/10">
                {avatarUrl && !avatarError ? (
                  <img
                    src={avatarUrl}
                    alt="Avatar"
                    className="w-full h-full object-cover"
                    onError={() => setAvatarError(true)}
                  />
                ) : (
                  <span>{sessionUser?.username.charAt(0).toUpperCase()}</span>
                )}
              </div>
              <div className="flex-1">
                <label className="block text-xs font-mono text-muted uppercase tracking-wider mb-1.5">
                  URL del avatar
                </label>
                <input
                  type="url"
                  value={avatarUrl}
                  onChange={(e) => { setAvatarUrl(e.target.value); setAvatarError(false) }}
                  placeholder="https://ejemplo.com/mi-foto.jpg"
                  className="w-full bg-bg-2 border border-white/[0.1] rounded-xl px-3.5 py-2.5 text-sm text-white placeholder:text-muted outline-none focus:border-accent/50 focus:ring-2 focus:ring-accent/10 transition-all"
                />
                <p className="text-[0.65rem] text-muted mt-1">
                  Enlace directo a una imagen (JPG, PNG, WebP)
                </p>
              </div>
            </div>

            {/* Bio */}
            <div>
              <div className="flex items-center justify-between mb-1.5">
                <label className="text-xs font-mono text-muted uppercase tracking-wider">Bio</label>
                <span className={`text-xs font-mono ${bio.length >= 190 ? 'text-yellow-400' : 'text-muted'}`}>
                  {bio.length}/200
                </span>
              </div>
              <textarea
                value={bio}
                onChange={(e) => setBio(e.target.value.slice(0, 200))}
                rows={3}
                placeholder="Cuéntanos algo sobre ti…"
                className="w-full bg-bg-2 border border-white/[0.1] rounded-xl px-3.5 py-2.5 text-sm text-white placeholder:text-muted outline-none focus:border-accent/50 focus:ring-2 focus:ring-accent/10 transition-all resize-none"
              />
            </div>

            <div className="flex justify-end">
              <button
                onClick={handleSaveProfile}
                disabled={isSaving}
                className="px-5 py-2 text-sm font-semibold bg-accent hover:bg-accent-light text-bg-0 rounded-xl disabled:opacity-60 transition-colors"
              >
                {isSaving ? 'Guardando…' : 'Guardar cambios'}
              </button>
            </div>
          </div>
        </section>

        {/* ── Privacidad ─────────────────────────────────── */}
        <section className="bg-bg-1 border border-white/[0.06] rounded-2xl overflow-hidden">
          <div className="px-6 py-4 border-b border-white/[0.06]">
            <h2 className="text-sm font-semibold text-white/90">Privacidad</h2>
            <p className="text-xs text-muted mt-0.5">Controla quién puede ver tu actividad</p>
          </div>

          <div className="px-6 py-5">
            <div className="flex items-center justify-between gap-4">
              <div>
                <p className="text-sm font-medium text-white/90">
                  {me?.isPublic ? '🔓 Perfil público' : '🔒 Perfil privado'}
                </p>
                <p className="text-xs text-muted mt-0.5">
                  {me?.isPublic
                    ? 'Cualquier usuario puede ver tus valoraciones y listas públicas.'
                    : 'Solo tus seguidores aprobados pueden ver tu actividad.'}
                </p>
              </div>
              <button
                onClick={() => me && togglePrivacy(!me.isPublic)}
                disabled={isTogglingPrivacy || !me}
                className={`relative w-11 h-6 rounded-full transition-colors shrink-0 disabled:opacity-60
                  ${me?.isPublic ? 'bg-accent' : 'bg-bg-3 border border-white/[0.1]'}`}
              >
                <span
                  className={`absolute top-0.5 w-5 h-5 bg-white rounded-full shadow transition-transform
                    ${me?.isPublic ? 'translate-x-5' : 'translate-x-0.5'}`}
                />
              </button>
            </div>
          </div>
        </section>

        {/* ── Cuenta ─────────────────────────────────────── */}
        <section className="bg-bg-1 border border-white/[0.06] rounded-2xl overflow-hidden">
          <div className="px-6 py-4 border-b border-white/[0.06]">
            <h2 className="text-sm font-semibold text-white/90">Cuenta</h2>
            <p className="text-xs text-muted mt-0.5">Información de tu cuenta (no editable desde aquí)</p>
          </div>

          <div className="px-6 py-5 space-y-4">
            <ReadonlyField label="Nombre de usuario" value={`@${me?.username ?? ''}`} />
            <ReadonlyField label="Correo electrónico" value={me?.email ?? ''} />
            <ReadonlyField
              label="Miembro desde"
              value={
                me?.createdAt
                  ? new Date(me.createdAt).toLocaleDateString('es-ES', {
                      day: 'numeric',
                      month: 'long',
                      year: 'numeric',
                    })
                  : '—'
              }
            />
          </div>
        </section>

        {/* ── Zona de peligro ────────────────────────────── */}
        <section className="bg-bg-1 border border-red-500/20 rounded-2xl overflow-hidden">
          <div className="px-6 py-4 border-b border-white/[0.06]">
            <h2 className="text-sm font-semibold text-red-400">Zona de peligro</h2>
          </div>

          <div className="px-6 py-5">
            <div className="flex items-center justify-between gap-4">
              <div>
                <p className="text-sm font-medium text-white/90">Cerrar sesión</p>
                <p className="text-xs text-muted mt-0.5">
                  Se cerrará la sesión en este dispositivo.
                </p>
              </div>

              {!confirmLogout ? (
                <button
                  onClick={() => setConfirmLogout(true)}
                  className="px-4 py-2 text-sm font-semibold border border-red-500/30 hover:bg-red-500/10 text-red-400 rounded-xl transition-colors shrink-0"
                >
                  Cerrar sesión
                </button>
              ) : (
                <div className="flex items-center gap-2 shrink-0">
                  <span className="text-xs text-muted">¿Seguro?</span>
                  <button
                    onClick={logout}
                    className="px-3 py-1.5 text-xs font-semibold bg-red-500 hover:bg-red-600 text-white rounded-lg transition-colors"
                  >
                    Confirmar
                  </button>
                  <button
                    onClick={() => setConfirmLogout(false)}
                    className="px-3 py-1.5 text-xs text-muted border border-white/10 hover:text-white rounded-lg transition-colors"
                  >
                    Cancelar
                  </button>
                </div>
              )}
            </div>
          </div>
        </section>

      </div>
    </div>
  )
}
