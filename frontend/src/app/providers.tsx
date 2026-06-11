'use client'

import { Toaster } from 'sonner'

import { useScreenInit } from '../useScreenInit'

export function Providers({ children }: { children: React.ReactNode }) {
  useScreenInit()

  return (
    <>
      <Toaster position="bottom-right" />
      {children}
    </>
  )
}
