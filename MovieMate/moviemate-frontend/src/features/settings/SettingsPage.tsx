import { useState, useEffect, useRef } from 'react'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import { useMyProfile, useLogout } from '../../hooks/useAuth'
import { useUpdateProfile } from '../../hooks/useProfile'
import { useAuthStore } from '../../store/authStore'
import { usersApi } from '../../api/users'
import { queryKeys } from '../../lib/queryKeys'
import BackButton from '../../components/shared/BackButton'

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
  const [avatarPreview, setAvatarPreview] = useState<string | null>(null)
  const fileInputRef = useRef<HTMLInputElement>(null)
  const [confirmLogout, setConfirmLogout] = useState(false)
  // Estado optimista para el toggle de privacidad — refleja el cambio de inmediato
  const [isPublicOptimistic, setIsPublicOptimistic] = useState<boolean | null>(null)

  // Cambio de contraseña
  const [currentPwd, setCurrentPwd] = useState('')
  const [newPwd, setNewPwd] = useState('')
  const [confirmPwd, setConfirmPwd] = useState('')

  const { mutate: changePassword, isPending: isChangingPwd } = useMutation({
    mutationFn: () => usersApi.changePassword({ currentPassword: currentPwd, newPassword: newPwd }),
    onSuccess: () => {
      setCurrentPwd('')
      setNewPwd('')
      setConfirmPwd('')
      toast.success('Contraseña actualizada')
    },
    onError: (err: any) =>
      toast.error(err?.response?.data?.message ?? 'Error al cambiar la contraseña'),
  })

  const handleChangePassword = () => {
    if (newPwd !== confirmPwd) {
      toast.error('Las contraseñas nuevas no coinciden')
      return
    }
    if (newPwd.length < 8) {
      toast.error('La nueva contraseña debe tener al menos 8 caracteres')
      return
    }
    changePassword()
  }

  // Inicializa el formulario cuando carga el perfil
  useEffect(() => {
    if (me) {
      setBio(me.bio ?? '')
      setAvatarUrl(me.avatarUrl ?? '')
      setAvatarError(false)
      // Solo inicializa el optimista si aún no se ha fijado (primera carga)
      setIsPublicOptimistic((prev) => prev === null ? me.isPublic : prev)
    }
  }, [me])

  const { mutate: uploadAvatar, isPending: isUploading } = useMutation({
    mutationFn: (file: File) => usersApi.uploadAvatar(file),
    onSuccess: ({ data }) => {
      queryClient.setQueryData(queryKeys.users.me(), data)
      setAvatarUrl(data.avatarUrl ?? '')
      setAvatarPreview(null)
      toast.success('Avatar actualizado')
    },
    onError: () => toast.error('Error al subir el avatar'),
  })

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0]
    if (!file) return
    const preview = URL.createObjectURL(file)
    setAvatarPreview(preview)
    uploadAvatar(file)
  }

  const { mutate: saveProfile, isPending: isSaving } = useUpdateProfile(
    sessionUser?.username ?? ''
  )

  const { mutate: togglePrivacy, isPending: isTogglingPrivacy } = useMutation({
    mutationFn: (isPublic: boolean) => usersApi.updatePublicStatus(isPublic),
    onSuccess: ({ data }) => {
      queryClient.setQueryData(queryKeys.users.me(), data)
      setIsPublicOptimistic(data.isPublic)
      toast.success(data.isPublic ? 'Perfil ahora público' : 'Perfil ahora privado')
    },
    onError: () => {
      // Revertir el estado optimista si falla
      setIsPublicOptimistic(me?.isPublic ?? null)
      toast.error('No se pudo cambiar la privacidad')
    },
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
      {/* Botón volver */}
      <div className="px-4 lg:px-6 pt-4 pb-1">
        <BackButton to={sessionUser ? `/profile/${sessionUser.username}` : '/'} label="Mi perfil" />
      </div>

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
              {/* Preview */}
              <div className="relative group shrink-0">
                <div className="w-16 h-16 rounded-full bg-gradient-to-br from-accent to-pink-500 flex items-center justify-center text-2xl font-bold text-bg-0 overflow-hidden border-2 border-white/10">
                  {(avatarPreview || (avatarUrl && !avatarError)) ? (
                    <img
                      src={avatarPreview ?? avatarUrl}
                      alt="Avatar"
                      className="w-full h-full object-cover"
                      onError={() => { setAvatarError(true); setAvatarPreview(null) }}
                    />
                  ) : (
                    <span>{sessionUser?.username.charAt(0).toUpperCase()}</span>
                  )}
                </div>
                {/* Botón overlay para subir archivo */}
                <button
                  type="button"
                  onClick={() => fileInputRef.current?.click()}
                  disabled={isUploading}
                  className="absolute inset-0 rounded-full bg-black/50 flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity text-white text-xs font-medium"
                >
                  {isUploading ? '...' : '📷'}
                </button>
                <input
                  ref={fileInputRef}
                  type="file"
                  accept="image/*"
                  onChange={handleFileChange}
                  className="hidden"
                />
              </div>

              <div className="flex-1 space-y-2">
                {/* Subir archivo */}
                <div>
                  <label className="block text-xs font-mono text-muted uppercase tracking-wider mb-1.5">
                    Subir imagen
                  </label>
                  <button
                    type="button"
                    onClick={() => fileInputRef.current?.click()}
                    disabled={isUploading}
                    className="flex items-center gap-2 bg-bg-2 border border-white/[0.1] hover:border-accent/40 text-white/70 hover:text-white text-sm px-3.5 py-2 rounded-xl transition-colors disabled:opacity-50"
                  >
                    {isUploading ? '⏳ Subiendo…' : '📎 Elegir archivo'}
                  </button>
                  <p className="text-[0.65rem] text-muted mt-1">JPG, PNG o WebP · máx. 5 MB</p>
                </div>
                {/* URL manual */}
                <div>
                  <label className="block text-xs font-mono text-muted uppercase tracking-wider mb-1.5">
                    O pegar URL
                  </label>
                  <input
                    type="url"
                    value={avatarUrl}
                    onChange={(e) => { setAvatarUrl(e.target.value); setAvatarError(false) }}
                    placeholder="https://ejemplo.com/mi-foto.jpg"
                    className="w-full bg-bg-2 border border-white/[0.1] rounded-xl px-3.5 py-2.5 text-sm text-white placeholder:text-muted outline-none focus:border-accent/50 focus:ring-2 focus:ring-accent/10 transition-all"
                  />
                </div>
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
                  {isPublicOptimistic ? '🔓 Perfil público' : '🔒 Perfil privado'}
                </p>
                <p className="text-xs text-muted mt-0.5">
                  {isPublicOptimistic
                    ? 'Cualquier usuario puede ver tus valoraciones y listas públicas.'
                    : 'Solo tus seguidores aprobados pueden ver tu actividad.'}
                </p>
              </div>
              <button
                onClick={() => {
                  if (!me || isPublicOptimistic === null) return
                  const next = !isPublicOptimistic
                  setIsPublicOptimistic(next)
                  togglePrivacy(next)
                }}
                disabled={isTogglingPrivacy || !me}
                aria-checked={isPublicOptimistic ?? false}
                role="switch"
                className={`relative w-12 h-6 rounded-full transition-colors duration-200 shrink-0 focus:outline-none focus:ring-2 focus:ring-accent/40 disabled:opacity-50
                  ${isPublicOptimistic ? 'bg-accent' : 'bg-white/20'}`}
              >
                <span
                  className={`absolute top-[3px] w-[18px] h-[18px] bg-white rounded-full shadow-md transition-transform duration-200
                    ${isPublicOptimistic ? 'translate-x-[27px]' : 'translate-x-[3px]'}`}
                />
              </button>
            </div>
          </div>
        </section>

        {/* ── Seguridad ──────────────────────────────────── */}
        <section className="bg-bg-1 border border-white/[0.06] rounded-2xl overflow-hidden">
          <div className="px-6 py-4 border-b border-white/[0.06]">
            <h2 className="text-sm font-semibold text-white/90">Seguridad</h2>
            <p className="text-xs text-muted mt-0.5">Cambia tu contraseña de acceso</p>
          </div>

          <div className="px-6 py-5 space-y-4">
            {[
              { label: 'Contraseña actual', value: currentPwd, onChange: setCurrentPwd, placeholder: '••••••••' },
              { label: 'Nueva contraseña', value: newPwd, onChange: setNewPwd, placeholder: 'Mínimo 8 caracteres' },
              { label: 'Confirmar nueva contraseña', value: confirmPwd, onChange: setConfirmPwd, placeholder: '••••••••' },
            ].map(({ label, value, onChange, placeholder }) => (
              <div key={label}>
                <label className="block text-xs font-mono text-muted uppercase tracking-wider mb-1.5">
                  {label}
                </label>
                <input
                  type="password"
                  value={value}
                  onChange={(e) => onChange(e.target.value)}
                  placeholder={placeholder}
                  className="w-full bg-bg-2 border border-white/[0.1] rounded-xl px-3.5 py-2.5 text-sm text-white placeholder:text-muted outline-none focus:border-accent/50 focus:ring-2 focus:ring-accent/10 transition-all"
                />
              </div>
            ))}

            <div className="flex justify-end pt-1">
              <button
                onClick={handleChangePassword}
                disabled={isChangingPwd || !currentPwd || !newPwd || !confirmPwd}
                className="px-5 py-2 text-sm font-semibold bg-accent hover:bg-accent-light text-bg-0 rounded-xl disabled:opacity-60 transition-colors"
              >
                {isChangingPwd ? 'Guardando…' : 'Cambiar contraseña'}
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
