'use client'

import { Badge, Button, Card } from '../../../components/ui'
import { useEffect, useState } from 'react'

import { useRouter } from 'next/navigation'

import { Briefcase, Trash2 } from 'lucide-react'


type ProjectSubTab =
  | 'uploaded'
  | 'participating'
  | 'applied'
  | 'completed'
  | 'viewed'

interface MyPageProjectResponse {
  id: number
  title: string
  description: string
  deadline: string
  leaderNickname: string
  leaderId: number
  recruitingPositions: string[]
  techStacks: string[]
  statusText: string
}

interface ProjectTabProps {
  user: any
}

export default function ProjectTab({ user }: ProjectTabProps) {
  const router = useRouter()
  const [activeProjectSubTab, setActiveProjectSubTab] =
    useState<ProjectSubTab>('uploaded')
  const [projects, setProjects] = useState<MyPageProjectResponse[]>()
  const [contentLoading, setContentLoading] = useState(false)

  useEffect(() => {
    if (!user) return

    const endpointMap: Record<ProjectSubTab, string> = {
      uploaded: '/mypage/projects/owned',
      participating: '/mypage/projects/participating',
      applied: '/mypage/projects/applied',
      completed: '/mypage/projects/completed',
      viewed: '/mypage/projects/recent-views',
    }

    setContentLoading(true)
    fetch(endpointMap[activeProjectSubTab])
      .then((res) => res.json())
      .then((res) => {
        if (res.code === '200') setProjects(res.data)
      })
      .catch(() => setProjects([]))
      .finally(() => setContentLoading(false))
  }, [activeProjectSubTab, user])

  const handleDeleteRecentView = async (
    projectId: number,
    e: React.MouseEvent,
  ) => {
    e.stopPropagation()
    if (!confirm('최근 본 프로젝트 내역에서 삭제하시겠습니까?')) return
    try {
      const res = await fetch(`/mypage/projects/recent-views/${projectId}`, {
        method: 'DELETE',
      })
      const result = await res.json()
      if (result.code === '200') {
        setProjects((prev) => prev?.filter((p) => p.id !== projectId))
      }
    } catch (err) {
      alert('삭제 처리에 실패했습니다.')
    }
  }

  const handleCancelApplication = async (
    applicationId: number,
    e: React.MouseEvent,
  ) => {
    e.stopPropagation()
    if (!confirm('프로젝트 지원 신청을 취소하시겠습니까?')) return
    try {
      const res = await fetch(
        `/mypage/projects/applications/${applicationId}/cancel`,
        { method: 'PATCH' },
      )
      const result = await res.json()
      if (result.code === '200') {
        alert(result.message)
        setProjects((prev) => prev?.filter((p) => p.id !== applicationId))
      }
    } catch (err) {
      alert('지원 취소 처리에 실패했습니다.')
    }
  }

  return (
    <div>
      <div className="flex gap-2 mb-6 border-b border-slate-200 pb-4 overflow-x-auto">
        {[
          { id: 'uploaded', label: '내가 올린 프로젝트' },
          { id: 'participating', label: '내가 참여중인 프로젝트' },
          { id: 'applied', label: '내가 지원한 프로젝트' },
          { id: 'completed', label: '내가 수행한 프로젝트' },
          { id: 'viewed', label: '조회 목록' },
        ].map((subTab) => (
          <button
            key={subTab.id}
            onClick={() => setActiveProjectSubTab(subTab.id as ProjectSubTab)}
            className={`px-4 py-2 rounded-full text-sm font-medium whitespace-nowrap transition-colors ${
              activeProjectSubTab === subTab.id
                ? 'bg-slate-900 text-white'
                : 'bg-slate-100 text-slate-600 hover:bg-slate-200'
            }`}
          >
            {subTab.label}
          </button>
        ))}
      </div>

      {contentLoading ? (
        <div className="text-center py-12 text-slate-400">
          데이터 로딩 중...
        </div>
      ) : projects && projects.length > 0 ? (
        <div className="grid grid-cols-1 gap-4">
          {projects.map((proj) => (
            <div
              key={proj.id}
              onClick={() => router.push(`/projects/${proj.id}`)}
              className="bg-white border border-slate-200 rounded-xl p-5 shadow-sm hover:shadow-md transition-all cursor-pointer flex justify-between items-start gap-4"
            >
              <div className="space-y-2 flex-1">
                <div className="flex items-center gap-2 flex-wrap">
                  <span
                    className={`text-xs font-semibold px-2.5 py-0.5 rounded-full ${proj.statusText === 'RECRUITING' ? 'bg-green-100 text-green-700' : 'bg-slate-100 text-slate-600'}`}
                  >
                    {proj.statusText === 'RECRUITING' ? '모집 중' : '수행/종료'}
                  </span>
                  <span className="text-xs text-slate-400">
                    마감일: {proj.deadline || '상시'}
                  </span>
                </div>
                <h3 className="text-lg font-bold text-slate-900 hover:text-blue-600 transition-colors">
                  {proj.title}
                </h3>
                <p className="text-sm text-slate-500 line-clamp-2">
                  {proj.description}
                </p>
                {proj.techStacks.length > 0 && (
                  <div className="flex flex-wrap gap-1.5 pt-1">
                    {proj.techStacks.map((st) => (
                      <Badge key={st} variant="secondary" className="text-xs">
                        {st}
                      </Badge>
                    ))}
                  </div>
                )}
              </div>

              <div className="flex flex-col items-end gap-2">
                {activeProjectSubTab === 'viewed' && (
                  <button
                    onClick={(e) => handleDeleteRecentView(proj.id, e)}
                    className="p-1.5 text-slate-400 hover:text-red-600 hover:bg-slate-50 rounded-lg transition-colors"
                  >
                    <Trash2 className="w-4 h-4" />
                  </button>
                )}
                {activeProjectSubTab === 'applied' && (
                  <Button
                    size="sm"
                    variant="outline"
                    className="text-xs text-red-500 border-red-200 hover:bg-red-50"
                    onClick={(e) => handleCancelApplication(proj.id, e)}
                  >
                    지원 취소
                  </Button>
                )}
              </div>
            </div>
          ))}
        </div>
      ) : (
        <Card className="p-12 text-center border-dashed">
          <Briefcase className="w-12 h-12 text-slate-300 mx-auto mb-4" />
          <h3 className="text-lg font-medium text-slate-900 mb-2">
            해당하는 프로젝트가 없습니다
          </h3>
          <p className="text-slate-500 mb-6">
            새로운 프로젝트를 찾거나 직접 만들어보세요.
          </p>
          <div className="flex justify-center gap-3">
            <Button variant="outline" onClick={() => router.push('/projects')}>
              프로젝트 찾기
            </Button>
            <Button onClick={() => router.push('/projects/new')}>
              프로젝트 만들기
            </Button>
          </div>
        </Card>
      )}
    </div>
  )
}
