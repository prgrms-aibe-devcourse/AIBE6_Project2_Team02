import { useEffect, useRef, useState } from 'react'
import { toast } from 'sonner'

const COOLDOWN_SECONDS = 60

interface UseAiDraftOptions {
  // AI 초안을 생성하는 비동기 호출
  generate: () => Promise<string>
  // 생성된 초안을 적용 (textarea 채우기 등)
  onResult: (draft: string) => void
  // 호출 전 유효성 검사. 문자열을 반환하면 에러 토스트로 표시하고 호출 중단
  validate?: () => string | null
}

export function useAiDraft({ generate, onResult, validate }: UseAiDraftOptions) {
  const [generating, setGenerating] = useState(false)
  const [cooldown, setCooldown] = useState(0)
  const timerRef = useRef<ReturnType<typeof setInterval> | null>(null)

  useEffect(() => {
    return () => {
      if (timerRef.current) clearInterval(timerRef.current)
    }
  }, [])

  const startCooldown = () => {
    setCooldown(COOLDOWN_SECONDS)
    if (timerRef.current) clearInterval(timerRef.current)
    timerRef.current = setInterval(() => {
      setCooldown((prev) => {
        if (prev <= 1) {
          if (timerRef.current) clearInterval(timerRef.current)
          return 0
        }
        return prev - 1
      })
    }, 1000)
  }

  const run = async () => {
    if (generating || cooldown > 0) return

    const error = validate?.()
    if (error) {
      toast.error(error)
      return
    }

    setGenerating(true)
    try {
      const draft = await generate()
      onResult(draft)
      toast.success('AI가 초안을 작성했어요.')
      startCooldown()
    } catch (err) {
      toast.error(
        err instanceof Error ? err.message : 'AI 초안 생성에 실패했습니다.',
      )
    } finally {
      setGenerating(false)
    }
  }

  // 버튼에 표시할 라벨
  const label = generating
    ? '작성 중...'
    : cooldown > 0
      ? `${cooldown}초 후 재시도`
      : 'AI 초안 작성'

  const disabled = generating || cooldown > 0

  return { run, label, disabled, generating, cooldown }
}
