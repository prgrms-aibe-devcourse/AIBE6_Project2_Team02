'use client'

import { createContext, useContext, useEffect, useState } from 'react'
import { Toaster } from 'sonner'
import { useScreenInit } from '../useScreenInit'
import { fetchMe, logout as logoutApi, type AuthUser } from '../lib/auth'

interface AuthContextValue {
  user: AuthUser | null
  loading: boolean
  logout: () => Promise<void>
}

const AuthContext = createContext<AuthContextValue>({
  user: null,
  loading: true,
  logout: async () => {},
})

export function useAuth() {
  return useContext(AuthContext)
}

export function Providers({ children }: { children: React.ReactNode }) {
  useScreenInit()

  const [user, setUser] = useState<AuthUser | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    fetchMe().then((me) => {
      setUser(me)
      setLoading(false)
    })
  }, [])

  const logout = async () => {
    await logoutApi()
    setUser(null)
  }

  return (
    <AuthContext.Provider value={{ user, loading, logout }}>
      <Toaster position="bottom-right" />
      {children}
    </AuthContext.Provider>
  )
}
