'use client'

import { useEffect, useState } from 'react'
import { useRouter } from 'next/navigation'
import Link from 'next/link'
import { ArrowLeft, Check, Code2, Globe, Github, BookOpen } from 'lucide-react'
import { Button, Card, Input } from '../../../../components/ui'
import { fetchAllTechStacks, fetchMyPortfolio, updateMyPortfolio } from '../../../../lib/api'
import type { TechStackItem } from '../../../../types/tech-stack'
import { useAuth } from '../../../providers'
import { leaderPositionOptions } from '../../../../constants/project'

export default function PortfolioEditPage() {
  const router = useRouter()
  const { user, loading: authLoading } = useAuth()

  const [techStacks, setTechStacks] = useState<TechStackItem[]>([])
  const [selectedNames, setSelectedNames] = useState<string[]>([])
  const [submitting, setSubmitting] = useState(false)
  const [loading, setLoading] = useState(true)
  const [errors, setErrors] = useState<{ title?: string; desiredPosition?: string; general?: string }>({})

  const [form, setForm] = useState({
    title: '',
    introduction: '',
    githubUrl: '',
    blogUrl: '',
    deployUrl: '',
    desiredPosition: '',
    isPublished: true,
  })

  useEffect(() => {
    if (authLoading) return
    if (!user) { router.replace('/'); return }

    Promise.all([fetchMyPortfolio(), fetchAllTechStacks()])
      .then(([portfolio, stacks]) => {
        setForm({
          title: portfolio.title ?? '',
          introduction: portfolio.introduction ?? '',
          githubUrl: portfolio.githubUrl ?? '',
          blogUrl: portfolio.blogUrl ?? '',
          deployUrl: portfolio.deployUrl ?? '',
          desiredPosition: portfolio.desiredPosition ?? '',
          isPublished: portfolio.isPublished,
        })
        setSelectedNames(portfolio.techStacks ?? [])
        setTechStacks(stacks)
      })
      .catch(() => router.replace('/mypage'))
      .finally(() => setLoading(false))
  }, [authLoading, user, router])

  const toggleTechStack = (name: string) => {
    setSelectedNames((prev) =>
      prev.includes(name) ? prev.filter((v) => v !== name) : [...prev, name]
    )
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setErrors({})
    setSubmitting(true)
    try {
      await updateMyPortfolio({
        title: form.title,
        introduction: form.introduction,
        githubUrl: form.githubUrl || null,
        blogUrl: form.blogUrl || null,
        deployUrl: form.deployUrl || null,
        desiredPosition: form.desiredPosition,
        techStacks: selectedNames,
        isPublished: form.isPublished,
      })
      router.push('/mypage')
    } catch (e) {
      const msg = e instanceof Error ? e.message : '수정 중 오류가 발생했습니다.'
      if (msg.includes('제목')) setErrors({ title: msg })
      else if (msg.includes('포지션')) setErrors({ desiredPosition: msg })
      else setErrors({ general: msg })
    } finally {
      setSubmitting(false)
    }
  }

  if (authLoading || loading) {
    return <div className="container mx-auto px-4 py-20 text-center text-slate-500">로딩 중...</div>
  }

  return (
    <div className="container mx-auto px-4 py-12 max-w-5xl">
      <div className="mb-8">
        <Link
          href="/mypage"
          className="inline-flex items-center gap-2 text-sm text-slate-500 hover:text-slate-900 transition-colors mb-4"
        >
          <ArrowLeft className="w-4 h-4" /> 마이페이지로 돌아가기
        </Link>
        <h1 className="text-2xl font-bold text-slate-900">포트폴리오 수정</h1>
        <p className="text-slate-500 mt-1">포트폴리오 정보를 수정하세요.</p>
      </div>

      <form onSubmit={handleSubmit}>
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">

          {/* Left: 링크 & 공개 설정 */}
          <div className="lg:col-span-1 space-y-6">
            <Card className="p-6">
              <h3 className="font-semibold text-slate-900 mb-4 flex items-center gap-2">
                <Globe className="w-4 h-4 text-blue-600" /> 링크
              </h3>
              <div className="space-y-3">
                <div>
                  <label className="text-xs font-medium text-slate-500 mb-1 block">GitHub</label>
                  <div className="relative">
                    <Github className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
                    <Input placeholder="https://github.com/username" className="pl-9"
                      value={form.githubUrl} onChange={(e) => setForm({ ...form, githubUrl: e.target.value })} />
                  </div>
                </div>
                <div>
                  <label className="text-xs font-medium text-slate-500 mb-1 block">블로그</label>
                  <div className="relative">
                    <BookOpen className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
                    <Input placeholder="https://blog.example.com" className="pl-9"
                      value={form.blogUrl} onChange={(e) => setForm({ ...form, blogUrl: e.target.value })} />
                  </div>
                </div>
                <div>
                  <label className="text-xs font-medium text-slate-500 mb-1 block">배포 URL</label>
                  <div className="relative">
                    <Globe className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
                    <Input placeholder="https://myproject.vercel.app" className="pl-9"
                      value={form.deployUrl} onChange={(e) => setForm({ ...form, deployUrl: e.target.value })} />
                  </div>
                </div>
              </div>
            </Card>

            <Card className="p-6">
              <h3 className="font-semibold text-slate-900 mb-4">공개 설정</h3>
              <label className="flex items-center justify-between cursor-pointer">
                <span className="text-sm text-slate-600">포트폴리오 공개</span>
                <button
                  type="button"
                  onClick={() => setForm({ ...form, isPublished: !form.isPublished })}
                  className={`relative inline-flex h-6 w-11 items-center rounded-full transition-colors ${
                    form.isPublished ? 'bg-blue-600' : 'bg-slate-200'
                  }`}
                >
                  <span className={`inline-block h-4 w-4 rounded-full bg-white shadow transition-transform ${
                    form.isPublished ? 'translate-x-6' : 'translate-x-1'
                  }`} />
                </button>
              </label>
              <p className="text-xs text-slate-400 mt-2">
                {form.isPublished ? '다른 사용자에게 포트폴리오가 공개됩니다.' : '나만 볼 수 있는 비공개 상태입니다.'}
              </p>
            </Card>
          </div>

          {/* Right: 기본 정보 & 기술 스택 */}
          <div className="lg:col-span-2 space-y-6">
            <Card className="p-6">
              <h3 className="font-semibold text-slate-900 mb-4">기본 정보</h3>
              <div className="space-y-4">
                <div>
                  <label className="text-sm font-medium text-slate-700 mb-1.5 block">
                    제목 <span className="text-red-500">*</span>
                  </label>
                  <Input placeholder="포트폴리오 제목을 입력해주세요"
                    value={form.title} onChange={(e) => setForm({ ...form, title: e.target.value })}
                    className={errors.title ? 'border-red-400' : ''} />
                  {errors.title && <p className="text-xs text-red-500 mt-1">{errors.title}</p>}
                </div>
                <div>
                  <label className="text-sm font-medium text-slate-700 mb-1.5 block">
                    희망 포지션 <span className="text-red-500">*</span>
                  </label>
                  <select
                    value={form.desiredPosition}
                    onChange={(e) => setForm({ ...form, desiredPosition: e.target.value })}
                    className="w-full rounded-lg border border-slate-200 bg-white px-3 py-2 text-sm focus:border-blue-600 focus:outline-none focus:ring-1 focus:ring-blue-600"
                  >
                    <option value="">포지션을 선택해주세요</option>
                    {leaderPositionOptions.map((opt) => (
                      <option key={opt.value} value={opt.value}>{opt.label}</option>
                    ))}
                  </select>
                  {errors.desiredPosition && <p className="text-xs text-red-500 mt-1">{errors.desiredPosition}</p>}
                </div>
                <div>
                  <label className="text-sm font-medium text-slate-700 mb-1.5 block">소개</label>
                  <textarea rows={5}
                    placeholder="자신을 소개해주세요."
                    className="w-full rounded-lg border border-slate-200 bg-white px-3 py-2 text-sm placeholder:text-slate-500 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-blue-600 resize-none"
                    value={form.introduction} onChange={(e) => setForm({ ...form, introduction: e.target.value })} />
                </div>
              </div>
            </Card>

            <Card className="p-6">
              <h3 className="font-semibold text-slate-900 mb-1 flex items-center gap-2">
                <Code2 className="w-4 h-4 text-blue-600" /> 기술 스택
              </h3>
              <p className="text-xs text-slate-400 mb-4">사용할 수 있는 기술 스택을 선택해주세요.</p>
              <div className="flex flex-wrap gap-2">
                {techStacks.map((ts) => {
                  const selected = selectedNames.includes(ts.name)
                  return (
                    <button key={ts.id} type="button" onClick={() => toggleTechStack(ts.name)}
                      className={`inline-flex items-center gap-1.5 px-3 py-1.5 rounded-full text-sm font-medium border transition-colors ${
                        selected
                          ? 'bg-blue-600 text-white border-blue-600'
                          : 'bg-white text-slate-600 border-slate-200 hover:border-blue-400 hover:text-blue-600'
                      }`}
                    >
                      {selected && <Check className="w-3 h-3" />}
                      {ts.name}
                    </button>
                  )
                })}
              </div>
            </Card>

            {errors.general && <p className="text-sm text-red-500">{errors.general}</p>}

            <div className="flex justify-end gap-3 pt-2">
              <Link href="/mypage">
                <Button type="button" variant="outline">취소</Button>
              </Link>
              <Button type="submit" disabled={submitting}>
                {submitting ? '저장 중...' : '수정 완료'}
              </Button>
            </div>
          </div>
        </div>
      </form>
    </div>
  )
}
