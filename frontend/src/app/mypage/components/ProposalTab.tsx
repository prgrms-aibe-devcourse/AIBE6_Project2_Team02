'use client'

import { Badge, Card } from '../../../components/ui'
import { useEffect, useState } from 'react'

import { useRouter } from 'next/navigation'

import { Briefcase, Check, MessageSquare, User, X } from 'lucide-react'

type ProposalFilter = 'applications' | 'proposals'

interface MyPageApplicationResponse {
  applicationId: number
  projectId: number
  projectTitle: string
  applicantName: string
  message: string
  status: string
  createdAt: string
}

interface MyPageProposalResponse {
  proposalId: number
  projectId: number
  projectTitle: string
  proposerName: string
  message: string
  status: string
  createdAt: string
}

interface ProposalTabProps {
  user: any
}

export default function ProposalTab({ user }: ProposalTabProps) {
  const router = useRouter()
  const [activeProposalFilter, setActiveProposalFilter] =
    useState<ProposalFilter>('applications')
  const [applications, setApplications] = useState<MyPageApplicationResponse[]>(
    [],
  )
  const [proposals, setProposals] = useState<MyPageProposalResponse[]>([])
  const [contentLoading, setContentLoading] = useState(false)

  useEffect(() => {
    if (!user) return
    setContentLoading(true)

    if (activeProposalFilter === 'applications') {
      fetch('/mypage/projects/applications')
        .then((res) => res.json())
        .then((res) => {
          if (res.code === '200') setApplications(res.data)
        })
        .catch(() => setApplications([]))
        .finally(() => setContentLoading(false))
    } else {
      fetch('/portfolios/me/proposals')
        .then((res) => res.json())
        .then((res) => {
          if (res.code === '200') setProposals(res.data)
        })
        .catch(() => setProposals([]))
        .finally(() => setContentLoading(false))
    }
  }, [activeProposalFilter, user])

  const handleApplicationDecision = async (
    applicationId: number,
    accept: boolean,
  ) => {
    if (!confirm(`해당 지원서를 ${accept ? '수락' : '거절'}하시겠습니까?`))
      return
    try {
      const res = await fetch(
        `/mypage/projects/applications/${applicationId}?accept=${accept}`,
        { method: 'PATCH' },
      )
      const result = await res.json()
      if (result.code === '200') {
        alert(result.message)
        setApplications((prev) =>
          prev.map((app) =>
            app.applicationId === applicationId
              ? { ...app, status: accept ? 'ACCEPTED' : 'REJECTED' }
              : app,
          ),
        )
      }
    } catch (err) {
      alert('처리에 실패했습니다.')
    }
  }

  const handleProposalDecision = async (
    proposalId: number,
    accept: boolean,
  ) => {
    if (
      !confirm(`해당 프로젝트 제안을 ${accept ? '수락' : '거절'}하시겠습니까?`)
    )
      return
    try {
      const res = await fetch(
        `/portfolios/me/proposals/${proposalId}?accept=${accept}`,
        { method: 'PATCH' },
      )
      const result = await res.json()
      if (result.code === '200') {
        alert(result.message)
        setProposals((prev) =>
          prev.map((prop) =>
            prop.proposalId === proposalId
              ? { ...prop, status: accept ? 'ACCEPTED' : 'REJECTED' }
              : prop,
          ),
        )
      }
    } catch (err) {
      alert('처리에 실패했습니다.')
    }
  }

  return (
    <div>
      <div className="flex gap-2 mb-6 border-b border-slate-200 pb-4 overflow-x-auto">
        <button
          onClick={() => setActiveProposalFilter('applications')}
          className={`px-4 py-2 rounded-full text-sm font-medium whitespace-nowrap ${activeProposalFilter === 'applications' ? 'bg-slate-900 text-white' : 'bg-slate-100 text-slate-600'}`}
        >
          내 프로젝트에 들어온 지원
        </button>
        <button
          onClick={() => setActiveProposalFilter('proposals')}
          className={`px-4 py-2 rounded-full text-sm font-medium whitespace-nowrap ${activeProposalFilter === 'proposals' ? 'bg-slate-900 text-white' : 'bg-slate-100 text-slate-600'}`}
        >
          내 포트폴리오에 온 제안
        </button>
      </div>

      {contentLoading ? (
        <div className="text-center py-12 text-slate-400">내역 확인 중...</div>
      ) : activeProposalFilter === 'applications' ? (
        applications.length > 0 ? (
          <div className="space-y-4">
            {applications.map((app) => (
              <div
                key={app.applicationId}
                className="bg-white border border-slate-200 rounded-xl p-5 shadow-sm space-y-3"
              >
                <div className="flex justify-between items-center">
                  <span className="text-xs text-slate-400">
                    공고: {app.projectTitle}
                  </span>
                  <Badge
                    variant={
                      app.status === 'PENDING'
                        ? 'outline'
                        : app.status === 'ACCEPTED'
                          ? 'default'
                          : 'secondary'
                    }
                  >
                    {app.status}
                  </Badge>
                </div>
                <div
                  onClick={() =>
                    router.push(`/portfolios/${app.applicationId}`)
                  }
                  className="bg-slate-50 hover:bg-slate-100 transition-colors p-3 rounded-lg cursor-pointer flex items-start gap-2.5"
                >
                  <User className="w-4 h-4 text-slate-400 mt-0.5" />
                  <div className="flex-1">
                    <p className="text-sm font-semibold text-slate-900 mb-0.5">
                      {app.applicantName} 님의 지원서
                    </p>
                    <p className="text-sm text-slate-600">
                      "{app.message}"{' '}
                      <span className="text-xs text-blue-500 underline ml-1">
                        포폴 보기
                      </span>
                    </p>
                  </div>
                </div>
                {app.status === 'PENDING' && (
                  <div className="flex justify-end gap-2 pt-1">
                    <button
                      onClick={() =>
                        handleApplicationDecision(app.applicationId, false)
                      }
                      className="px-3 py-1.5 rounded-lg border border-slate-200 text-xs font-medium text-slate-600 hover:bg-slate-50"
                    >
                      <X className="w-3 h-3 inline mr-1" />
                      거절
                    </button>
                    <button
                      onClick={() =>
                        handleApplicationDecision(app.applicationId, true)
                      }
                      className="px-3 py-1.5 rounded-lg bg-blue-600 text-xs font-medium text-white hover:bg-blue-700"
                    >
                      <Check className="w-3 h-3 inline mr-1" />
                      팀원 수락
                    </button>
                  </div>
                )}
              </div>
            ))}
          </div>
        ) : (
          <Card className="p-12 text-center border-dashed">
            <MessageSquare className="w-12 h-12 text-slate-300 mx-auto mb-4" />
            <h3 className="text-lg font-medium text-slate-900">
              들어온 지원서가 없습니다.
            </h3>
          </Card>
        )
      ) : proposals.length > 0 ? (
        <div className="space-y-4">
          {proposals.map((prop) => (
            <div
              key={prop.proposalId}
              className="bg-white border border-slate-200 rounded-xl p-5 shadow-sm space-y-3"
            >
              <div className="flex justify-between items-center">
                <span className="text-xs text-slate-400">
                  제안자: {prop.proposerName} 리더
                </span>
                <Badge
                  variant={
                    prop.status === 'PENDING'
                      ? 'outline'
                      : prop.status === 'ACCEPTED'
                        ? 'default'
                        : 'secondary'
                  }
                >
                  {prop.status}
                </Badge>
              </div>
              <div
                onClick={() => router.push(`/projects/${prop.projectId}`)}
                className="bg-blue-50/50 hover:bg-blue-50 transition-colors p-3 rounded-lg cursor-pointer flex items-start gap-2.5"
              >
                <Briefcase className="w-4 h-4 text-blue-400 mt-0.5" />
                <div className="flex-1">
                  <p className="text-sm font-bold text-blue-900 mb-0.5">
                    [{prop.projectTitle}] 팀 합류 제안
                  </p>
                  <p className="text-sm text-slate-700">
                    "{prop.message}"{' '}
                    <span className="text-xs text-blue-600 underline ml-1">
                      공고 상세 보기
                    </span>
                  </p>
                </div>
              </div>
              {prop.status === 'PENDING' && (
                <div className="flex justify-end gap-2 pt-1">
                  <button
                    onClick={() =>
                      handleProposalDecision(prop.proposalId, false)
                    }
                    className="px-3 py-1.5 rounded-lg border border-slate-200 text-xs font-medium text-slate-600 hover:bg-slate-50"
                  >
                    <X className="w-3 h-3 inline mr-1" />
                    거절
                  </button>
                  <button
                    onClick={() =>
                      handleProposalDecision(prop.proposalId, true)
                    }
                    className="px-3 py-1.5 rounded-lg bg-slate-900 text-xs font-medium text-white hover:bg-slate-800"
                  >
                    <Check className="w-3 h-3 inline mr-1" />
                    제안 수락 (합류)
                  </button>
                </div>
              )}
            </div>
          ))}
        </div>
      ) : (
        <Card className="p-12 text-center border-dashed">
          <MessageSquare className="w-12 h-12 text-slate-300 mx-auto mb-4" />
          <h3 className="text-lg font-medium text-slate-900">
            받은 제안이 없습니다.
          </h3>
        </Card>
      )}
    </div>
  )
}
