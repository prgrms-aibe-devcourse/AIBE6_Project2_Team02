'use client'

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { Briefcase, Check, MessageSquare, User, X } from 'lucide-react';
import { useDialog } from '../../../components/DialogProvider';
import { PaginationControls } from '../../../components/PaginationControls';
import { Badge, Card } from '../../../components/ui';

type ProposalFilter = 'applications' | 'proposals'

interface MyPageApplicationResponse {
  applicationId: number
  projectId: number
  projectTitle: string
  applicantName: string
  message: string
  status: string
  createdAt: string
  applicantId: number
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

const API_BASE = process.env.NEXT_PUBLIC_API_URL ?? 'http://localhost:8080'

export default function ProposalTabComponent({ user }: ProposalTabProps) {
  const router = useRouter()
  const { alertDialog, confirmDialog } = useDialog()
  const [activeProposalFilter, setActiveProposalFilter] =
      useState<ProposalFilter>('applications')
  const [applications, setApplications] = useState<MyPageApplicationResponse[]>(
      [],
  )
  const [proposals, setProposals] = useState<MyPageProposalResponse[]>([])
  const [contentLoading, setContentLoading] = useState(false)

  // 서버 페이징 상태 관리 필드
  const [page, setPage] = useState(0)
  const [pageCount, setPageCount] = useState(0)

  useEffect(() => {
    if (!user) return
    setContentLoading(true)

    if (activeProposalFilter === 'applications') {
      fetch(`${API_BASE}/mypage/projects/applications?page=${page}&size=5`, {
        credentials: 'include',
      })
          .then((res) => res.json())
          .then((res) => {
            if (res.code === '200' && res.data) {
              setApplications(res.data.content || [])
              setPageCount(res.data.totalPages || 0)
            } else {
              setApplications([])
              setPageCount(0)
            }
          })
          .catch(() => {
            setApplications([])
            setPageCount(0)
          })
          .finally(() => setContentLoading(false))
    } else {
      fetch(`${API_BASE}/portfolios/me/proposals?page=${page}&size=5`, {
        credentials: 'include',
      })
          .then((res) => res.json())
          .then((res) => {
            if (res.code === '200' && res.data) {
              setProposals(res.data.content || [])
              setPageCount(res.data.totalPages || 0)
            } else {
              setProposals([])
              setPageCount(0)
            }
          })
          .catch(() => {
            setProposals([])
            setPageCount(0)
          })
          .finally(() => setContentLoading(false))
    }
  }, [activeProposalFilter, page, user])

  useEffect(() => {
    setPage(0)
  }, [activeProposalFilter])

  const handleApplicationDecision = async (
      applicationId: number,
      accept: boolean,
  ) => {
    if (
      !(await confirmDialog(`해당 지원서를 ${accept ? '수락' : '거절'}하시겠습니까?`, {
        title: accept ? '지원서 수락' : '지원서 거절',
        confirmText: accept ? '수락' : '거절',
        destructive: !accept,
      }))
    )
      return
    try {
      const res = await fetch(
          `${API_BASE}/mypage/projects/applications/${applicationId}?accept=${accept}`,
          {
            method: 'PATCH',
            credentials: 'include',
          },
      )
      const result = await res.json()
      if (result.code === '200') {
        await alertDialog(result.message)
        setApplications((prev) =>
            prev.filter((app) => app.applicationId !== applicationId),
        )
      }
    } catch (err) {
      await alertDialog('처리에 실패했습니다.')
    }
  }

  const handleProposalDecision = async (
      proposalId: number,
      accept: boolean,
  ) => {
    if (
      !(await confirmDialog(`해당 프로젝트 제안을 ${accept ? '수락' : '거절'}하시겠습니까?`, {
        title: accept ? '프로젝트 제안 수락' : '프로젝트 제안 거절',
        confirmText: accept ? '수락' : '거절',
        destructive: !accept,
      }))
    )
      return
    try {
      const res = await fetch(
          `${API_BASE}/portfolios/me/proposals/${proposalId}?accept=${accept}`,
          {
            method: 'PATCH',
            credentials: 'include',
          },
      )
      const result = await res.json()
      if (result.code === '200') {
        await alertDialog(result.message)
        setProposals((prev) =>
            prev.filter((prop) => prop.proposalId !== proposalId),
        )
      }
    } catch (err) {
      await alertDialog('처리에 실패했습니다.')
    }
  }

  return (
      <div>
        <div className="flex gap-2 mb-6 border-b border-slate-200 pb-4 overflow-x-auto">
          <button
              onClick={() => setActiveProposalFilter('applications')}
              className={`tab-pill ${activeProposalFilter === 'applications' ? 'tab-pill-active' : 'tab-pill-inactive'}`}
          >
            내 프로젝트에 들어온 지원
          </button>
          <button
              onClick={() => setActiveProposalFilter('proposals')}
              className={`tab-pill ${activeProposalFilter === 'proposals' ? 'tab-pill-active' : 'tab-pill-inactive'}`}
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

                        {/* 🎯 교정 완료: `app.applicationId` 대신 이번에 새로 내려준 유저 고유 식별자인 `app.applicantId`를 바인딩하여 포폴/상세조회 억까 해제! */}
                        <div
                            onClick={() => router.push(`/u/${app.applicantId}`)}
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
                                  onClick={(e) => {
                                    e.stopPropagation()
                                    handleApplicationDecision(app.applicationId, false)
                                  }}
                                  className="px-3 py-1.5 rounded-lg border border-slate-200 text-xs font-medium text-slate-600 hover:bg-slate-50"
                              >
                                <X className="w-3 h-3 inline mr-1" />
                                거절
                              </button>
                              <button
                                  onClick={(e) => {
                                    e.stopPropagation()
                                    handleApplicationDecision(app.applicationId, true)
                                  }}
                                  className="px-3 py-1.5 rounded-lg bg-blue-600 text-xs font-medium text-white hover:bg-blue-700"
                              >
                                <Check className="w-3 h-3 inline mr-1" />
                                팀원 수락
                              </button>
                            </div>
                        )}
                      </div>
                  ))}

                  <div className="mt-8 flex justify-center w-full">
                    <PaginationControls
                        page={page}
                        pageCount={pageCount}
                        onPageChange={setPage}
                    />
                  </div>
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
                  제안자: {prop.proposerName}
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
                              onClick={(e) => {
                                e.stopPropagation()
                                handleProposalDecision(prop.proposalId, false)
                              }}
                              className="px-3 py-1.5 rounded-lg border border-slate-200 text-xs font-medium text-slate-600 hover:bg-slate-50"
                          >
                            <X className="w-3 h-3 inline mr-1" />
                            거절
                          </button>
                          <button
                              onClick={(e) => {
                                e.stopPropagation()
                                handleProposalDecision(prop.proposalId, true)
                              }}
                              className="px-3 py-1.5 rounded-lg bg-slate-900 text-xs font-medium text-white hover:bg-slate-800"
                          >
                            <Check className="w-3 h-3 inline mr-1" />
                            제안 수락 (합류)
                          </button>
                        </div>
                    )}
                  </div>
              ))}

              <div className="mt-8 flex justify-center w-full">
                <PaginationControls
                    page={page}
                    pageCount={pageCount}
                    onPageChange={setPage}
                />
              </div>
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
