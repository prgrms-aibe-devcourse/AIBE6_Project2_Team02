'use client'

import { useEffect, useState } from 'react'
import { toast } from 'sonner'

import { useParams, useRouter } from 'next/navigation'

import { ArrowLeft } from 'lucide-react'

import { Button, Card, Input } from '../../../../components/ui'
import {
  fetchProject,
  fetchProjectPermissions,
  updateProject,
} from '../../../../lib/api'
import type { ProjectUpdateRequest } from '../../../../types/dto/project'

export default function ProjectEditPage() {
  const params = useParams()
  const id = params.id as string
  const router = useRouter()
  const [loading, setLoading] = useState(true)
  const [submitting, setSubmitting] = useState(false)
  const [form, setForm] = useState<ProjectUpdateRequest>({
    title: '',
    description: '',
    fullDescription: '',
    category: 'Web',
    goals: [],
    deadline: '',
    open: true,
  })
  const [goalsText, setGoalsText] = useState('')

  useEffect(() => {
    Promise.all([fetchProject(id), fetchProjectPermissions(id)])
      .then(([project, permission]) => {
        if (!permission.canEdit) {
          toast.error('프로젝트 수정 권한이 없습니다.')
          router.replace(`/projects/${id}`)
          return
        }

        setForm({
          title: project.title,
          description: project.description,
          fullDescription: project.fullDescription,
          category: project.category,
          goals: project.goals,
          deadline: project.deadline,
          open: project.recruitmentStatus === 'Open',
        })
        setGoalsText(project.goals.join('\n'))
      })
      .catch(() => {
        toast.error('프로젝트 정보를 불러오지 못했습니다.')
        router.replace(`/projects/${id}`)
      })
      .finally(() => setLoading(false))
  }, [id, router])

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault()
    if (submitting) return

    setSubmitting(true)
    try {
      await updateProject(id, {
        ...form,
        goals: goalsText
          .split('\n')
          .map((goal) => goal.trim())
          .filter(Boolean),
      })
      toast.success('프로젝트가 수정되었습니다.')
      router.push(`/projects/${id}`)
    } catch (error) {
      toast.error(
        error instanceof Error ? error.message : '프로젝트 수정에 실패했습니다.',
      )
    } finally {
      setSubmitting(false)
    }
  }

  if (loading) {
    return <div className="container mx-auto px-4 py-20 text-center">불러오는 중...</div>
  }

  return (
    <div className="container mx-auto max-w-3xl px-4 py-8">
      <button
        type="button"
        onClick={() => router.back()}
        className="mb-6 flex items-center gap-2 text-slate-500 hover:text-slate-900"
      >
        <ArrowLeft className="h-4 w-4" />
        돌아가기
      </button>

      <Card className="p-6 md:p-8">
        <h1 className="mb-6 text-2xl font-bold">프로젝트 수정</h1>
        <form onSubmit={handleSubmit} className="space-y-5">
          <Input
            value={form.title}
            onChange={(event) => setForm({ ...form, title: event.target.value })}
            placeholder="프로젝트 제목"
            required
          />
          <Input
            value={form.description}
            onChange={(event) =>
              setForm({ ...form, description: event.target.value })
            }
            placeholder="프로젝트 요약"
            required
          />
          <textarea
            value={form.fullDescription}
            onChange={(event) =>
              setForm({ ...form, fullDescription: event.target.value })
            }
            className="min-h-40 w-full rounded-lg border border-slate-200 p-3"
            placeholder="프로젝트 상세 설명"
            required
          />
          <select
            value={form.category}
            onChange={(event) =>
              setForm({
                ...form,
                category: event.target.value as ProjectUpdateRequest['category'],
              })
            }
            className="h-10 w-full rounded-lg border border-slate-200 px-3"
          >
            <option value="Web">Web</option>
            <option value="Mobile">Mobile</option>
            <option value="AI">AI</option>
            <option value="Game">Game</option>
            <option value="Other">Other</option>
          </select>
          <textarea
            value={goalsText}
            onChange={(event) => setGoalsText(event.target.value)}
            className="min-h-28 w-full rounded-lg border border-slate-200 p-3"
            placeholder="목표를 줄바꿈으로 구분해 입력하세요"
          />
          <Input
            type="date"
            value={form.deadline}
            onChange={(event) =>
              setForm({ ...form, deadline: event.target.value })
            }
            required
          />
          <label className="flex items-center gap-2 text-sm">
            <input
              type="checkbox"
              checked={form.open}
              onChange={(event) =>
                setForm({ ...form, open: event.target.checked })
              }
            />
            모집 진행
          </label>
          <div className="flex justify-end gap-3">
            <Button type="button" variant="outline" onClick={() => router.back()}>
              취소
            </Button>
            <Button type="submit" variant="gradient" disabled={submitting}>
              {submitting ? '수정 중...' : '수정 완료'}
            </Button>
          </div>
        </form>
      </Card>
    </div>
  )
}
