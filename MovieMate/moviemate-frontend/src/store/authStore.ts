import { create } from 'zustand'
import { persist, createJSONStorage } from 'zustand/middleware'

// Lo que guardamos de la sesión tras el login
interface SessionUser {
  username: string
  email: string
}

interface AuthState {
  sessionUser: SessionUser | null
  token: string | null
  isAuthenticated: boolean
  isInitialized: boolean    // evita flash de login al recargar
}

interface AuthActions {
  setAuth: (username: string, email: string, token: string) => void
  logout: () => void
  setInitialized: () => void
}

type AuthStore = AuthState & AuthActions

export const useAuthStore = create<AuthStore>()(
  persist(
    (set) => ({
      // Estado inicial
      sessionUser: null,
      token: null,
      isAuthenticated: false,
      isInitialized: false,

      // Acciones
      setAuth: (username, email, token) => {
        localStorage.setItem('mm_token', token)
        set({ sessionUser: { username, email }, token, isAuthenticated: true })
      },

      logout: () => {
        localStorage.removeItem('mm_token')
        set({ sessionUser: null, token: null, isAuthenticated: false })
      },

      setInitialized: () => set({ isInitialized: true }),
    }),
    {
      name: 'mm-auth',
      storage: createJSONStorage(() => localStorage),
      // Solo persiste estos campos (no isInitialized)
      partialize: (state) => ({
        sessionUser: state.sessionUser,
        token: state.token,
        isAuthenticated: state.isAuthenticated,
      }),
    }
  )
)