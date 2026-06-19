'use client'

import { createContext, useContext, useEffect, useState } from 'react'
import { Toaster } from 'sonner'

import { usePathname, useRouter } from 'next/navigation'

import { type AuthUser, fetchMe, logout as logoutApi } from '../lib/auth'
import { useScreenInit } from '../useScreenInit'

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

  const router = useRouter()
  const [user, setUser] = useState<AuthUser | null>(null)
  const [loading, setLoading] = useState(true)
  const pathname = usePathname()

  useEffect(() => {
    fetchMe().then((me) => {
      setUser(me)
      setLoading(false)
      if (
        me &&
        me.role === 'ROLE_ADMIN' &&
        (pathname === '/' || pathname === '/login')
      ) {
        router.replace('/admin/reports')
      }
    })
  }, [pathname, router])

  const logout = async () => {
    await logoutApi()
    setUser(null)
    router.push('/')
  }

  return (
    <AuthContext.Provider value={{ user, loading, logout }}>
      <Toaster position="bottom-right" />
      {children}
    </AuthContext.Provider>
  )
}
