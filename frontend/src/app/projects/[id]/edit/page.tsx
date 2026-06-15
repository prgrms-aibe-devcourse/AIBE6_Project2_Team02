'use client'

import { useEffect, useState } from 'react'
import { toast } from 'sonner'

import { useParams, useRouter } from 'next/navigation'

import { ArrowLeft, Plus, X } from 'lucide-react'

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
  const today = new Date()
  const minimumDeadline = [
    today.getFullYear(),
    String(today.getMonth() + 1).padStart(2, '0'),
    String(today.getDate()).padStart(2, '0'),
  ].join('-')

  const [loading, setLoading] = useState(true)
  const [submitting, setSubmitting] = useState(false)
  const [form, setForm] = useState<ProjectUpdateRequest>({
    title: '',
    description: '',
    fullDescription: '',
    category: 'Web',
    goals: [''],
    deadline: '',
    open: true,
  })

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
          goals: project.goals.length > 0 ? project.goals : [''],
          deadline: project.deadline,
          open: project.recruitmentStatus === 'Open',
        })
      })
      .catch(() => {
        toast.error('프로젝트 정보를 불러오지 못했습니다.')
        router.replace(`/projects/${id}`)
      })
      .finally(() => setLoading(false))
  }, [id, router])

  const handleGoalChange = (index: number, value: string) => {
    const goals = [...form.goals]
    goals[index] = value
    setForm({ ...form, goals })
  }

  const handleAddGoal = () => {
    setForm({ ...form, goals: [...form.goals, ''] })
  }

  const handleRemoveGoal = (index: number) => {
    setForm({
      ...form,
      goals: form.goals.filter((_, goalIndex) => goalIndex !== index),
    })
  }

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault()
    if (submitting) return

    if (
      !form.title.trim() ||
      !form.description.trim() ||
      !form.fullDescription.trim() ||
      !form.deadline
    ) {
      toast.error('필수 항목을 모두 입력해주세요.')
      return
    }

    if (form.deadline < minimumDeadline) {
      toast.error('모집 마감일은 오늘보다 이전으로 설정할 수 없습니다.')
      return
    }

    setSubmitting(true)
    try {
      await updateProject(id, {
        ...form,
        title: form.title.trim(),
        description: form.description.trim(),
        fullDescription: form.fullDescription.trim(),
        goals: form.goals.map((goal) => goal.trim()).filter(Boolean),
      })
      toast.success('프로젝트가 수정되었습니다.')
      router.push(`/projects/${id}`)
    } catch (error) {
      toast.error(
        error instanceof Error
          ? error.message
          : '프로젝트 수정에 실패했습니다.',
      )
    } finally {
      setSubmitting(false)
    }
  }

  if (loading) {
    return (
      <div className="container mx-auto px-4 py-20 text-center text-slate-500">
        프로젝트 정보를 불러오는 중...
      </div>
    )
  }

  return (
    <div className="container mx-auto max-w-3xl px-4 py-8">
      <button
        type="button"
        onClick={() => router.back()}
        className="mb-6 flex items-center gap-2 text-slate-500 transition-colors hover:text-slate-900"
      >
        <ArrowLeft className="h-4 w-4" />
        뒤로 가기
      </button>

      <div className="mb-8">
        <h1 className="mb-2 text-3xl font-bold text-slate-900">
          프로젝트 수정
        </h1>
        <p className="text-slate-500">
          변경할 프로젝트 정보를 확인하고 항목별로 수정해주세요.
        </p>
      </div>

      <form onSubmit={handleSubmit} className="space-y-8">
        <Card className="p-6 md:p-8">
          <h2 className="mb-2 flex items-center gap-2 text-xl font-bold text-slate-900">
            <span className="flex h-6 w-6 items-center justify-center rounded-full bg-blue-100 text-sm text-blue-600">
              1
            </span>
            기본 정보
          </h2>
          <p className="mb-6 text-sm text-slate-500">
            프로젝트 목록과 상세 화면에 가장 먼저 표시되는 정보입니다.
          </p>

          <div className="space-y-6">
            <div>
              <label className="mb-2 block text-sm font-medium text-slate-700">
                프로젝트 제목 <span className="text-red-500">*</span>
              </label>
              <p className="mb-2 text-xs text-slate-500">
                프로젝트의 목적을 쉽게 파악할 수 있는 제목을 작성해주세요.
              </p>
              <Input
                value={form.title}
                onChange={(event) =>
                  setForm({ ...form, title: event.target.value })
                }
                placeholder="예: 실시간 협업 화이트보드"
                required
              />
            </div>

            <div>
              <label className="mb-2 block text-sm font-medium text-slate-700">
                한 줄 소개 <span className="text-red-500">*</span>
              </label>
              <p className="mb-2 text-xs text-slate-500">
                프로젝트 목록에서 보일 핵심 내용을 한 문장으로 작성해주세요.
              </p>
              <Input
                value={form.description}
                onChange={(event) =>
                  setForm({ ...form, description: event.target.value })
                }
                placeholder="프로젝트를 한 문장으로 소개해주세요."
                required
              />
            </div>

            <div>
              <label className="mb-2 block text-sm font-medium text-slate-700">
                상세 설명 <span className="text-red-500">*</span>
              </label>
              <p className="mb-2 text-xs text-slate-500">
                프로젝트 배경, 해결하려는 문제와 주요 기능을 구체적으로
                작성해주세요.
              </p>
              <textarea
                value={form.fullDescription}
                onChange={(event) =>
                  setForm({ ...form, fullDescription: event.target.value })
                }
                className="min-h-40 w-full resize-y rounded-lg border border-slate-200 bg-white px-3 py-2 text-sm focus:border-blue-600 focus:outline-none focus:ring-1 focus:ring-blue-600"
                placeholder="프로젝트의 배경과 진행 방향을 상세히 작성해주세요."
                required
              />
            </div>
          </div>
        </Card>

        <Card className="p-6 md:p-8">
          <h2 className="mb-2 flex items-center gap-2 text-xl font-bold text-slate-900">
            <span className="flex h-6 w-6 items-center justify-center rounded-full bg-blue-100 text-sm text-blue-600">
              2
            </span>
            분류 및 모집 일정
          </h2>
          <p className="mb-6 text-sm text-slate-500">
            프로젝트 분야와 팀원 모집 기간 및 진행 상태를 설정합니다.
          </p>

          <div className="space-y-6">
            <div className="grid grid-cols-1 gap-6 md:grid-cols-2">
              <div>
                <label className="mb-2 block text-sm font-medium text-slate-700">
                  카테고리 <span className="text-red-500">*</span>
                </label>
                <p className="mb-2 text-xs text-slate-500">
                  프로젝트와 가장 가까운 분야를 선택해주세요.
                </p>
                <select
                  value={form.category}
                  onChange={(event) =>
                    setForm({
                      ...form,
                      category: event.target
                        .value as ProjectUpdateRequest['category'],
                    })
                  }
                  className="h-10 w-full rounded-lg border border-slate-200 bg-white px-3 text-sm focus:border-blue-600 focus:outline-none focus:ring-1 focus:ring-blue-600"
                >
                  <option value="Web">웹</option>
                  <option value="Mobile">모바일</option>
                  <option value="AI">AI / 데이터</option>
                  <option value="Game">게임</option>
                  <option value="Other">기타</option>
                </select>
              </div>

              <div>
                <label className="mb-2 block text-sm font-medium text-slate-700">
                  모집 마감일 <span className="text-red-500">*</span>
                </label>
                <p className="mb-2 text-xs text-slate-500">
                  오늘 이후의 팀원 모집 마감일을 선택해주세요.
                </p>
                <Input
                  type="date"
                  min={minimumDeadline}
                  value={form.deadline}
                  onChange={(event) =>
                    setForm({ ...form, deadline: event.target.value })
                  }
                  required
                />
              </div>
            </div>

            <div>
              <label className="mb-2 block text-sm font-medium text-slate-700">
                모집 상태
              </label>
              <p className="mb-3 text-xs text-slate-500">
                모집을 마감하면 새로운 지원자가 프로젝트에 지원할 수 없습니다.
              </p>
              <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
                <label
                  className={`cursor-pointer rounded-lg border p-4 ${
                    form.open
                      ? 'border-blue-500 bg-blue-50'
                      : 'border-slate-200'
                  }`}
                >
                  <input
                    type="radio"
                    name="recruitmentStatus"
                    checked={form.open}
                    onChange={() => setForm({ ...form, open: true })}
                    className="mr-2"
                  />
                  <span className="font-medium text-slate-900">모집 중</span>
                  <span className="mt-1 block pl-6 text-xs text-slate-500">
                    새로운 팀원의 지원을 받습니다.
                  </span>
                </label>
                <label
                  className={`cursor-pointer rounded-lg border p-4 ${
                    !form.open
                      ? 'border-blue-500 bg-blue-50'
                      : 'border-slate-200'
                  }`}
                >
                  <input
                    type="radio"
                    name="recruitmentStatus"
                    checked={!form.open}
                    onChange={() => setForm({ ...form, open: false })}
                    className="mr-2"
                  />
                  <span className="font-medium text-slate-900">모집 마감</span>
                  <span className="mt-1 block pl-6 text-xs text-slate-500">
                    더 이상 새로운 지원을 받지 않습니다.
                  </span>
                </label>
              </div>
            </div>
          </div>
        </Card>

        <Card className="p-6 md:p-8">
          <div className="mb-2 flex items-center justify-between gap-4">
            <h2 className="flex items-center gap-2 text-xl font-bold text-slate-900">
              <span className="flex h-6 w-6 items-center justify-center rounded-full bg-blue-100 text-sm text-blue-600">
                3
              </span>
              프로젝트 목표
            </h2>
            <Button
              type="button"
              variant="ghost"
              size="sm"
              onClick={handleAddGoal}
              className="h-8 text-blue-600"
            >
              <Plus className="mr-1 h-4 w-4" />
              목표 추가
            </Button>
          </div>
          <p className="mb-6 text-sm text-slate-500">
            팀이 함께 달성하려는 구체적인 결과를 항목별로 작성해주세요.
          </p>

          <div className="space-y-3">
            {form.goals.map((goal, index) => (
              <div key={index} className="flex items-start gap-3">
                <div className="mt-2.5 h-1.5 w-1.5 shrink-0 rounded-full bg-slate-400" />
                <div className="flex-1">
                  <Input
                    value={goal}
                    onChange={(event) =>
                      handleGoalChange(index, event.target.value)
                    }
                    placeholder="예: 3개월 안에 MVP를 완성하고 배포하기"
                  />
                </div>
                {form.goals.length > 1 && (
                  <button
                    type="button"
                    onClick={() => handleRemoveGoal(index)}
                    className="mt-0.5 p-2 text-slate-400 transition-colors hover:text-red-500"
                    aria-label={`${index + 1}번째 목표 삭제`}
                  >
                    <X className="h-4 w-4" />
                  </button>
                )}
              </div>
            ))}
          </div>
        </Card>

        <div className="flex items-center justify-end gap-4 border-t border-slate-200 pt-4">
          <Button type="button" variant="ghost" onClick={() => router.back()}>
            취소
          </Button>
          <Button
            type="submit"
            variant="gradient"
            className="px-8"
            disabled={submitting}
          >
            {submitting ? '수정 중...' : '수정 완료'}
          </Button>
        </div>
      </form>
    </div>
  )
}
