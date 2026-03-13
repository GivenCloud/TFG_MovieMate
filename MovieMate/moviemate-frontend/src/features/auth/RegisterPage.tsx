import { useState } from 'react'
import { Link, Navigate } from 'react-router-dom'
import { useRegister } from '../../hooks/useAuth'
import { useAuthStore } from '../../store/authStore'

export default function RegisterPage() {
  const { isAuthenticated } = useAuthStore()
  const register = useRegister()
  const [form, setForm] = useState({ username: '', email: '', password: '', confirmPassword: '' })
  const [errors, setErrors] = useState<Record<string, string>>({})
  const [showPassword, setShowPassword] = useState(false)
  const [showConfirm, setShowConfirm] = useState(false)

  if (isAuthenticated) return <Navigate to="/" replace />

  const validate = () => {
    const next: Record<string, string> = {}
    if (!form.username.trim()) next.username = 'El nombre de usuario es obligatorio.'
    else if (form.username.length < 3) next.username = 'Mínimo 3 caracteres.'
    else if (!/^[a-zA-Z0-9_]+$/.test(form.username)) next.username = 'Solo letras, números y guión bajo.'
    if (!form.email.trim()) next.email = 'El email es obligatorio.'
    else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) next.email = 'Email no válido.'
    if (!form.password) next.password = 'La contraseña es obligatoria.'
    else if (form.password.length < 8) next.password = 'Mínimo 8 caracteres.'
    if (form.confirmPassword !== form.password) next.confirmPassword = 'Las contraseñas no coinciden.'
    return next
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    const fieldErrors = validate()
    if (Object.keys(fieldErrors).length > 0) {
      setErrors(fieldErrors)
      return
    }
    setErrors({})
    try {
      await register.mutateAsync({
        username: form.username,
        email: form.email,
        password: form.password,
      })
    } catch (err: any) {
      const msg = err?.response?.data?.message || 'No se pudo crear la cuenta.'
      // El backend puede indicar qué campo está duplicado
      const lower = msg.toLowerCase()
      if (lower.includes('usuario') || lower.includes('username')) {
        setErrors({ username: msg })
      } else if (lower.includes('email')) {
        setErrors({ email: msg })
      } else {
        setErrors({ form: msg })
      }
    }
  }

  const field = (key: keyof typeof form) => ({
    value: form[key],
    onChange: (e: React.ChangeEvent<HTMLInputElement>) => {
      setForm((f) => ({ ...f, [key]: e.target.value }))
      if (errors[key]) setErrors((prev) => { const next = { ...prev }; delete next[key]; return next })
    },
  })

  return (
    <div className="min-h-screen grid grid-cols-1 lg:grid-cols-2 bg-bg-0">
      {/* Panel izquierdo — presentación (solo desktop) */}
      <div className="hidden lg:flex flex-col items-center justify-center px-8 bg-bg-1 border-r border-white/[0.06] relative overflow-hidden">
        <div className="absolute w-[500px] h-[500px] rounded-full bg-accent/[0.06] blur-3xl top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 pointer-events-none" />
        <div className="relative w-full max-w-xs">
          <div className="flex items-center gap-3 mb-8">
            <div className="w-11 h-11 bg-accent rounded-xl flex items-center justify-center text-xl shrink-0">
              🎬
            </div>
            <span className="font-display font-bold italic text-2xl tracking-tight text-white">MovieMate</span>
          </div>
          <p className="font-mono text-xs text-white/50 tracking-wider uppercase mb-10">
            Tu diario cinematográfico social
          </p>
          {[
            { icon: '🎬', title: 'Películas y series juntas',  desc: 'Un solo lugar para todo tu contenido audiovisual.' },
            { icon: '⭐', title: 'Valora y reseña',             desc: 'Comparte tu opinión y descubre la de la comunidad.' },
            { icon: '📋', title: 'Listas personalizadas',       desc: 'Organiza tu colección como quieras.' },
            { icon: '👥', title: 'Red social cinéfila',         desc: 'Sigue a personas y descubre nuevo contenido.' },
          ].map(({ icon, title, desc }) => (
            <div key={title} className="flex gap-3 mb-5">
              <div className="w-8 h-8 rounded-lg bg-bg-3 border border-white/[0.06] flex items-center justify-center text-sm shrink-0 mt-0.5">
                {icon}
              </div>
              <div>
                <p className="text-sm font-semibold text-white/90">{title}</p>
                <p className="text-xs text-white/50 mt-0.5">{desc}</p>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Panel derecho — formulario */}
      <div className="flex items-center justify-center px-6 py-12">
        <div className="w-full max-w-sm">
          <div className="flex items-center gap-2 mb-8 lg:hidden">
            <div className="w-9 h-9 bg-accent rounded-xl flex items-center justify-center">🎬</div>
            <span className="font-display font-bold italic text-xl">MovieMate</span>
          </div>

          <h1 className="font-display font-bold italic text-3xl mb-1 text-white">Crea tu cuenta</h1>
          <p className="text-sm text-muted mb-8">Únete a la comunidad cinéfila</p>

          <form onSubmit={handleSubmit} className="space-y-4" noValidate>
            {/* Usuario */}
            <div>
              <label className="block font-mono text-xs text-muted tracking-wider uppercase mb-1.5">
                Nombre de usuario
              </label>
              <input
                type="text"
                required
                autoComplete="username"
                placeholder="cinefilo99"
                {...field('username')}
                className={`w-full bg-bg-2 border rounded-xl px-3.5 py-2.5 text-sm text-white placeholder:text-white/30 outline-none transition-all
                  ${errors.username
                    ? 'border-red-500/60 focus:border-red-500/80 focus:ring-2 focus:ring-red-500/10'
                    : 'border-white/[0.1] focus:border-accent/50 focus:ring-2 focus:ring-accent/10'
                  }`}
              />
              {errors.username && (
                <p className="text-xs text-red-400 mt-1.5">{errors.username}</p>
              )}
            </div>

            {/* Email */}
            <div>
              <label className="block font-mono text-xs text-muted tracking-wider uppercase mb-1.5">
                Email
              </label>
              <input
                type="email"
                required
                autoComplete="email"
                placeholder="usuario@email.com"
                {...field('email')}
                className={`w-full bg-bg-2 border rounded-xl px-3.5 py-2.5 text-sm text-white placeholder:text-white/30 outline-none transition-all
                  ${errors.email
                    ? 'border-red-500/60 focus:border-red-500/80 focus:ring-2 focus:ring-red-500/10'
                    : 'border-white/[0.1] focus:border-accent/50 focus:ring-2 focus:ring-accent/10'
                  }`}
              />
              {errors.email && (
                <p className="text-xs text-red-400 mt-1.5">{errors.email}</p>
              )}
            </div>

            {/* Contraseña */}
            <div>
              <label className="block font-mono text-xs text-muted tracking-wider uppercase mb-1.5">
                Contraseña
              </label>
              <div className="relative">
                <input
                  type={showPassword ? 'text' : 'password'}
                  required
                  autoComplete="new-password"
                  placeholder="Mínimo 8 caracteres"
                  {...field('password')}
                  className={`w-full bg-bg-2 border rounded-xl px-3.5 py-2.5 pr-10 text-sm text-white placeholder:text-white/30 outline-none transition-all
                    ${errors.password
                      ? 'border-red-500/60 focus:border-red-500/80 focus:ring-2 focus:ring-red-500/10'
                      : 'border-white/[0.1] focus:border-accent/50 focus:ring-2 focus:ring-accent/10'
                    }`}
                />
                <button
                  type="button"
                  onClick={() => setShowPassword((v) => !v)}
                  className="absolute right-3 top-1/2 -translate-y-1/2 text-white/60 hover:text-white transition-colors"
                  tabIndex={-1}
                  aria-label={showPassword ? 'Ocultar contraseña' : 'Mostrar contraseña'}
                >
                  {showPassword ? (
                    <svg xmlns="http://www.w3.org/2000/svg" className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                      <path strokeLinecap="round" strokeLinejoin="round" d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.88 9.88l-3.29-3.29m7.532 7.532l3.29 3.29M3 3l3.59 3.59m0 0A9.953 9.953 0 0112 5c4.478 0 8.268 2.943 9.543 7a10.025 10.025 0 01-4.132 5.411m0 0L21 21" />
                    </svg>
                  ) : (
                    <svg xmlns="http://www.w3.org/2000/svg" className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                      <path strokeLinecap="round" strokeLinejoin="round" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                      <path strokeLinecap="round" strokeLinejoin="round" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                    </svg>
                  )}
                </button>
              </div>
              {errors.password && (
                <p className="text-xs text-red-400 mt-1.5">{errors.password}</p>
              )}
            </div>

            {/* Confirmar contraseña */}
            <div>
              <label className="block font-mono text-xs text-muted tracking-wider uppercase mb-1.5">
                Confirmar contraseña
              </label>
              <div className="relative">
                <input
                  type={showConfirm ? 'text' : 'password'}
                  required
                  autoComplete="new-password"
                  placeholder="••••••••"
                  {...field('confirmPassword')}
                  className={`w-full bg-bg-2 border rounded-xl px-3.5 py-2.5 pr-10 text-sm text-white placeholder:text-white/30 outline-none transition-all
                    ${errors.confirmPassword
                      ? 'border-red-500/60 focus:border-red-500/80 focus:ring-2 focus:ring-red-500/10'
                      : 'border-white/[0.1] focus:border-accent/50 focus:ring-2 focus:ring-accent/10'
                    }`}
                />
                <button
                  type="button"
                  onClick={() => setShowConfirm((v) => !v)}
                  className="absolute right-3 top-1/2 -translate-y-1/2 text-white/60 hover:text-white transition-colors"
                  tabIndex={-1}
                  aria-label={showConfirm ? 'Ocultar contraseña' : 'Mostrar contraseña'}
                >
                  {showConfirm ? (
                    <svg xmlns="http://www.w3.org/2000/svg" className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                      <path strokeLinecap="round" strokeLinejoin="round" d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.88 9.88l-3.29-3.29m7.532 7.532l3.29 3.29M3 3l3.59 3.59m0 0A9.953 9.953 0 0112 5c4.478 0 8.268 2.943 9.543 7a10.025 10.025 0 01-4.132 5.411m0 0L21 21" />
                    </svg>
                  ) : (
                    <svg xmlns="http://www.w3.org/2000/svg" className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                      <path strokeLinecap="round" strokeLinejoin="round" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                      <path strokeLinecap="round" strokeLinejoin="round" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                    </svg>
                  )}
                </button>
              </div>
              {errors.confirmPassword && (
                <p className="text-xs text-red-400 mt-1.5">{errors.confirmPassword}</p>
              )}
            </div>

            {/* Error general de API */}
            {errors.form && (
              <p className="text-sm text-red-400 bg-red-400/10 border border-red-400/20 rounded-lg px-3 py-2">
                {errors.form}
              </p>
            )}

            <button
              type="submit"
              disabled={register.isPending}
              className="w-full bg-accent hover:bg-accent-light disabled:opacity-60 disabled:cursor-not-allowed text-bg-0 font-semibold rounded-xl py-2.5 text-sm transition-all hover:-translate-y-0.5 hover:shadow-lg hover:shadow-accent/20"
            >
              {register.isPending ? 'Creando cuenta…' : 'Crear cuenta'}
            </button>
          </form>

          <p className="text-center text-sm text-muted mt-6">
            ¿Ya tienes cuenta?{' '}
            <Link to="/login" className="text-accent hover:text-accent-light transition-colors">
              Inicia sesión
            </Link>
          </p>
        </div>
      </div>
    </div>
  )
}
