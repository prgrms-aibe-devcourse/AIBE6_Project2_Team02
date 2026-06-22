'use client'

import { createContext, useCallback, useContext, useMemo, useState } from 'react'
import type { ReactNode } from 'react'

import { Button, Modal } from './ui'

type DialogVariant = 'alert' | 'confirm'

interface DialogState {
  variant: DialogVariant
  title: string
  message: string
  confirmText?: string
  cancelText?: string
  destructive?: boolean
  resolve: (value: boolean) => void
}

interface DialogContextValue {
  alertDialog: (message: string, title?: string) => Promise<void>
  confirmDialog: (
    message: string,
    options?: {
      title?: string
      confirmText?: string
      cancelText?: string
      destructive?: boolean
    },
  ) => Promise<boolean>
}

const DialogContext = createContext<DialogContextValue | null>(null)

export function useDialog() {
  const context = useContext(DialogContext)

  if (!context) {
    throw new Error('useDialog는 DialogProvider 안에서만 사용할 수 있습니다.')
  }

  return context
}

export function DialogProvider({ children }: { children: ReactNode }) {
  const [dialog, setDialog] = useState<DialogState | null>(null)

  const close = useCallback(
    (value: boolean) => {
      dialog?.resolve(value)
      setDialog(null)
    },
    [dialog],
  )

  const alertDialog = useCallback((message: string, title = '알림') => {
    return new Promise<void>((resolve) => {
      setDialog({
        variant: 'alert',
        title,
        message,
        confirmText: '확인',
        resolve: () => resolve(),
      })
    })
  }, [])

  const confirmDialog = useCallback<DialogContextValue['confirmDialog']>(
    (message, options = {}) => {
      return new Promise<boolean>((resolve) => {
        setDialog({
          variant: 'confirm',
          title: options.title ?? '확인',
          message,
          confirmText: options.confirmText ?? '확인',
          cancelText: options.cancelText ?? '취소',
          destructive: options.destructive,
          resolve,
        })
      })
    },
    [],
  )

  const value = useMemo(
    () => ({ alertDialog, confirmDialog }),
    [alertDialog, confirmDialog],
  )

  return (
    <DialogContext.Provider value={value}>
      {children}
      <Modal
        isOpen={dialog !== null}
        onClose={() => close(false)}
        title={dialog?.title ?? ''}
      >
        <div className="space-y-5">
          <p className="whitespace-pre-line text-sm leading-6 text-slate-600">
            {dialog?.message}
          </p>
          <div className="flex justify-end gap-3">
            {dialog?.variant === 'confirm' && (
              <Button type="button" variant="ghost" onClick={() => close(false)}>
                {dialog.cancelText}
              </Button>
            )}
            <Button
              type="button"
              variant={dialog?.destructive ? 'outline' : 'gradient'}
              className={
                dialog?.destructive
                  ? 'border-red-200 text-red-500 hover:bg-red-50'
                  : undefined
              }
              onClick={() => close(true)}
            >
              {dialog?.confirmText ?? '확인'}
            </Button>
          </div>
        </div>
      </Modal>
    </DialogContext.Provider>
  )
}
