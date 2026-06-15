'use client'

import { AnimatePresence, motion } from 'framer-motion'
import { useEffect, useState } from 'react'
import Link from 'next/link'
import { useRouter } from 'next/navigation'
import {
  Briefcase,
  ChevronRight,
  FileText,
  LogOut,
  MessageSquare,
  Pencil,
  Settings,
  Trash2,
  Check,
  X,
  User
} from 'lucide-react'

import { Badge, Button, Card } from '../../components/ui'
import { useAuth } from '../providers'
import type { Portfolio } from '../../types'

type Tab = 'portfolio' | 'project' | 'proposal'
type PortfolioSubTab = 'portfolio' | 'peerReview'
type ProjectSubTab = 'uploaded' | 'participating' | 'applied' | 'completed' | 'viewed'
type ProposalFilter = 'applications' | 'proposals' // 내 프로젝트에 온 지원(applications) / 내 포폴에 온 제안(proposals)

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

export default function MyPage() {
  const { user, loading: authLoading, logout } = useAuth()
  const router = useRouter()

  // 탭 상태 관리
  const activeTabFromStorage = typeof window !== 'undefined' ? localStorage.getItem('mypage_tab') : null;
  const [activeTab, setActiveTab] = useState<Tab>((activeTabFromStorage as Tab) || 'portfolio')
  const [activePortfolioSubTab, setActivePortfolioSubTab] = useState<PortfolioSubTab>('portfolio')
  const [activeProjectSubTab, setActiveProjectSubTab] = useState<ProjectSubTab>('uploaded')
  const [activeProposalFilter, setActiveProposalFilter] = useState<ProposalFilter>('applications')

  // 데이터 상태 관리
  const [portfolio, setPortfolio] = useState<Portfolio | null>(null)
  const [portfolioLoading, setPortfolioLoading] = useState(true)
  const [projects, setProjects] = useState<MyPageProjectResponse[]>([])
  const [applications, setApplications] = useState<MyPageApplicationResponse[]>([])
  const [proposals, setProposals] = useState<MyPageProposalResponse[]>([])
  const [contentLoading, setContentLoading] = useState(false)
  const [avatarError, setAvatarError] = useState(false)

  // 탭 변경 시 로컬스토리지 임시 백업 (새로고침 방어)
  const handleTabChange = (tab: Tab) => {
    setActiveTab(tab)
    if (typeof window !== 'undefined') localStorage.setItem('mypage_tab', tab)
  }

  // 포트폴리오 로딩 (기존 스펙 보존)
  useEffect(() => {
    if (!authLoading && !user) {
      router.replace('/')
      return
    }
    if (!authLoading && user) {
      fetch('/portfolios/me')
        .then(res => res.json())
        .then(res => { if (res.code === "200") setPortfolio(res.data) })
        .catch(() => setPortfolio(null))
        .finally(() => setPortfolioLoading(false))
    }
  }, [authLoading, user, router])

  // [프로젝트 탭 관련 API 페칭]
  useEffect(() => {
    if (!user || activeTab !== 'project') return

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
  }, [activeTab, activeProjectSubTab, user])

  // [제안 탭 관련 API 페칭]
  useEffect(() => {
    if (!user || activeTab !== 'proposal') return

    setContentLoading(true)
    if (activeProposalFilter === 'applications') {
      // 내 프로젝트에 들어온 지원 목록
      fetch('/mypage/projects/applications')
        .then((res) => res.json())
        .then((res) => {
          if (res.code === '200') setApplications(res.data)
        })
        .catch(() => setApplications([]))
        .finally(() => setContentLoading(false))
    } else {
      // 내 포트폴리오에 온 프로젝트 제안 목록
      fetch('/portfolios/me/proposals')
        .then((res) => res.json())
        .then((res) => {
          if (res.code === '200') setProposals(res.data)
        })
        .catch(() => setProposals([]))
        .finally(() => setContentLoading(false))
    }
  }, [activeTab, activeProposalFilter, user])

  // [액션 핸들러 1: 최근 본 프로젝트 개별 삭제]
  const handleDeleteRecentView = async (projectId: number, e: React.MouseEvent) => {
    e.stopPropagation() // 카드 클릭 이벤트(상세 이동) 전파 차단
    if (!confirm('최근 본 프로젝트 내역에서 삭제하시겠습니까?')) return

    try {
      const res = await fetch(`/mypage/projects/recent-views/${projectId}`, { method: 'DELETE' })
      const result = await res.json()
      if (result.code === '200') {
        setProjects((prev) => prev.filter((p) => p.id !== projectId))
      }
    } catch (err) {
      alert('삭제 처리에 실패했습니다.')
    }
  }

  // [액션 핸들러 2: 내가 신청한 지원 취소]
  const handleCancelApplication = async (applicationId: number, e: React.MouseEvent) => {
    e.stopPropagation()
    if (!confirm('프로젝트 지원 신청을 취소하시겠습니까?')) return

    try {
      const res = await fetch(`/mypage/projects/applications/${applicationId}/cancel`, { method: 'PATCH' })
      const result = await res.json()
      if (result.code === '200') {
        alert(result.message)
        // 화면 상태 즉시 갱신 (목록 리로드 대신 상태 업데이트)
        setProjects((prev) => prev.filter((p) => p.id !== applicationId)) // 혹은 간결하게 리스트 최신화 촉구
      }
    } catch (err) {
      alert('지원 취소 처리에 실패했습니다.')
    }
  }

  // [액션 핸들러 3: 들어온 지원 수락/거절]
  const handleApplicationDecision = async (applicationId: number, accept: boolean) => {
    const actionText = accept ? '수락하여 팀원으로 등록' : '거절';
    if (!confirm(`해당 지원서를 ${actionText}하시겠습니까?`)) return

    try {
      const res = await fetch(`/mypage/projects/applications/${applicationId}?accept=${accept}`, { method: 'PATCH' })
      const result = await res.json()
      if (result.code === '200') {
        alert(result.message)
        // 목록에서 실시간 처리완료 업데이트 반영
        setApplications((prev) =>
          prev.map((app) => (app.applicationId === applicationId ? { ...app, status: accept ? 'ACCEPTED' : 'REJECTED' } : app))
        )
      }
    } catch (err) {
      alert('처리에 실패했습니다.')
    }
  }

  // [액션 핸들러 4: 받은 제안 수락/거절]
  const handleProposalDecision = async (proposalId: number, accept: boolean) => {
    const actionText = accept ? '수락하여 팀에 합류' : '거절';
    if (!confirm(`해당 프로젝트 제안을 ${actionText}하시겠습니까?`)) return

    try {
      const res = await fetch(`/portfolios/me/proposals/${proposalId}?accept=${accept}`, { method: 'PATCH' })
      const result = await res.json()
      if (result.code === '200') {
        alert(result.message)
        setProposals((prev) =>
          prev.map((prop) => (prop.proposalId === proposalId ? { ...prop, status: accept ? 'ACCEPTED' : 'REJECTED' } : prop))
        )
      }
    } catch (err) {
      alert('처리에 실패했습니다.')
    }
  }

  if (authLoading) {
    return <div className="container mx-auto px-4 py-20 text-center text-slate-500">로딩 중...</div>
  }

  if (!user) return null

  return (
    <div className="container mx-auto px-4 py-8 max-w-5xl">
      <div className="bg-white rounded-2xl p-8 border border-slate-200 shadow-sm mb-8 flex flex-col md:flex-row items-center gap-6">
        <div className="relative">
          {user?.profileImageUrl && !avatarError ? (
            <img
              src={user.profileImageUrl}
              alt="My Profile"
              referrerPolicy="no-referrer"
              className="w-24 h-24 rounded-full object-cover border-4 border-white shadow-md"
              onError={() => setAvatarError(true)}
            />
          ) : (
            <div className="w-24 h-24 rounded-full bg-slate-200 border-4 border-white shadow-md flex items-center justify-center text-2xl font-bold text-slate-600">
              {user?.nickname?.[0]?.toUpperCase() ?? '?'}
            </div>
          )}
          <button className="absolute bottom-0 right-0 bg-white p-1.5 rounded-full border border-slate-200 shadow-sm hover:bg-slate-50 transition-colors">
            <Settings className="w-4 h-4 text-slate-600" />
          </button>
        </div>
        <div className="flex-1 text-center md:text-left">
          <h1 className="text-2xl font-bold text-slate-900 mb-1">{user?.nickname ?? '...'}</h1>
          {portfolio && (
            <>
              <p className="text-slate-500 mb-3">{portfolio.desiredPosition}</p>
              {portfolio.techStacks.length > 0 && (
                <div className="flex flex-wrap justify-center md:justify-start gap-2">
                  {portfolio.techStacks.map((stack) => (
                    <Badge key={stack} variant="secondary">{stack}</Badge>
                  ))}
                </div>
              )}
            </>
          )}
        </div>
        <div className="flex gap-3 w-full md:w-auto">
          <Button variant="outline" className="flex-1 md:flex-none gap-2">
            <Settings className="w-4 h-4" /> 설정
          </Button>
          <Button
            variant="ghost"
            className="flex-1 md:flex-none gap-2 text-red-600 hover:text-red-700 hover:bg-red-50"
            onClick={async () => { await logout(); router.push('/') }}
          >
            <LogOut className="w-4 h-4" /> 로그아웃
          </Button>
        </div>
      </div>

      {/* Main Content Area */}
      <div className="flex flex-col md:flex-row gap-8">
        {/* Sidebar / Tabs */}
        <div className="w-full md:w-64 flex-shrink-0">
          <div className="bg-white rounded-xl border border-slate-200 overflow-hidden sticky top-24">
            <div className="flex md:flex-col border-b md:border-b-0 border-slate-200">
              <button
                onClick={() => handleTabChange('portfolio')}
                className={`flex-1 md:w-full text-left px-6 py-4 font-medium transition-colors flex items-center justify-between ${
                  activeTab === 'portfolio' ? 'bg-blue-50 text-blue-700 border-b-2 md:border-b-0 md:border-l-4 border-blue-600' : 'text-slate-600 hover:bg-slate-50'
                }`}
              >
                <span className="flex items-center gap-2"><FileText className="w-4 h-4" /> 포트폴리오</span>
                <ChevronRight className="w-4 h-4 hidden md:block opacity-50" />
              </button>
              <button
                onClick={() => handleTabChange('project')}
                className={`flex-1 md:w-full text-left px-6 py-4 font-medium transition-colors flex items-center justify-between ${
                  activeTab === 'project' ? 'bg-blue-50 text-blue-700 border-b-2 md:border-b-0 md:border-l-4 border-blue-600' : 'text-slate-600 hover:bg-slate-50'
                }`}
              >
                <span className="flex items-center gap-2"><Briefcase className="w-4 h-4" /> 프로젝트</span>
                <ChevronRight className="w-4 h-4 hidden md:block opacity-50" />
              </button>
              <button
                onClick={() => handleTabChange('proposal')}
                className={`flex-1 md:w-full text-left px-6 py-4 font-medium transition-colors flex items-center justify-between ${
                  activeTab === 'proposal' ? 'bg-blue-50 text-blue-700 border-b-2 md:border-b-0 md:border-l-4 border-blue-600' : 'text-slate-600 hover:bg-slate-50'
                }`}
              >
                <span className="flex items-center gap-2"><MessageSquare className="w-4 h-4" /> 제안</span>
                <ChevronRight className="w-4 h-4 hidden md:block opacity-50" />
              </button>
            </div>
          </div>
        </div>

        {/* Content Panel */}
        <div className="flex-1">
          <AnimatePresence mode="wait">
            {activeTab === 'portfolio' && (
              <motion.div key="portfolio" initial={{ opacity: 0, y: 10 }} animate={{ opacity: 1, y: 0 }} exit={{ opacity: 0, y: -10 }}>
                <div className="flex gap-2 mb-6 border-b border-slate-200 pb-4 overflow-x-auto">
                  <button onClick={() => setActivePortfolioSubTab('portfolio')} className={`px-4 py-2 rounded-full text-sm font-medium whitespace-nowrap ${activePortfolioSubTab === 'portfolio' ? 'bg-slate-900 text-white' : 'bg-slate-100 text-slate-600'}`}>포폴</button>
                  <button onClick={() => setActivePortfolioSubTab('peerReview')} className={`px-4 py-2 rounded-full text-sm font-medium whitespace-nowrap ${activePortfolioSubTab === 'peerReview' ? 'bg-slate-900 text-white' : 'bg-slate-100 text-slate-600'}`}>내게 달린 피어리뷰</button>
                </div>
                {activePortfolioSubTab === 'portfolio' && (
                  portfolioLoading ? <div className="text-center py-12 text-slate-400">로딩 중...</div> : portfolio ? (
                    <Card className="p-6 space-y-5">
                      <div className="flex items-start justify-between">
                        <div>
                          <h2 className="text-xl font-bold text-slate-900">{portfolio.title}</h2>
                          <p className="text-sm text-blue-600 mt-1">{portfolio.desiredPosition}</p>
                        </div>
                        <Link href="/mypage/portfolio/edit"><Button size="sm" variant="outline" className="gap-1.5"><Pencil className="w-3.5 h-3.5" /> 수정</Button></Link>
                      </div>
                      <p className="text-slate-600 text-sm leading-relaxed">{portfolio.introduction}</p>
                    </Card>
                  ) : (
                    <Card className="p-12 text-center border-dashed">
                      <FileText className="w-12 h-12 text-slate-300 mx-auto mb-4" />
                      <h3 className="text-lg font-medium text-slate-900 mb-2">등록된 포트폴리오가 없습니다</h3>
                      <Link href="/mypage/portfolio/new"><Button>포트폴리오 등록하기</Button></Link>
                    </Card>
                  )
                )}
              </motion.div>
            )}

            {activeTab === 'project' && (
              <motion.div key="project" initial={{ opacity: 0, y: 10 }} animate={{ opacity: 1, y: 0 }} exit={{ opacity: 0, y: -10 }} transition={{ duration: 0.2 }}>
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
                        activeProjectSubTab === subTab.id ? 'bg-slate-900 text-white' : 'bg-slate-100 text-slate-600 hover:bg-slate-200'
                      }`}
                    >
                      {subTab.label}
                    </button>
                  ))}
                </div>

                {contentLoading ? (
                  <div className="text-center py-12 text-slate-400">데이터 로딩 중...</div>
                ) : projects.length > 0 ? (
                  <div className="grid grid-cols-1 gap-4">
                    {projects.map((proj) => (
                      <div
                        key={proj.id}
                        onClick={() => router.push(`/projects/${proj.id}`)}
                        className="bg-white border border-slate-200 rounded-xl p-5 shadow-sm hover:shadow-md transition-all cursor-pointer flex justify-between items-start gap-4"
                      >
                        <div className="space-y-2 flex-1">
                          <div className="flex items-center gap-2 flex-wrap">
                            <span className={`text-xs font-semibold px-2.5 py-0.5 rounded-full ${proj.statusText === 'RECRUITING' ? 'bg-green-100 text-green-700' : 'bg-slate-100 text-slate-600'}`}>
                              {proj.statusText === 'RECRUITING' ? '모집 중' : '수행/종료'}
                            </span>
                            <span className="text-xs text-slate-400">마감일: {proj.deadline || '상시'}</span>
                          </div>
                          <h3 className="text-lg font-bold text-slate-900 hover:text-blue-600 transition-colors">{proj.title}</h3>
                          <p className="text-sm text-slate-500 line-clamp-2">{proj.description}</p>

                          {/* 스택 배지 노출 */}
                          {proj.techStacks.length > 0 && (
                            <div className="flex flex-wrap gap-1.5 pt-1">
                              {proj.techStacks.map((st) => (
                                <Badge key={st} variant="secondary" className="text-xs">{st}</Badge>
                              ))}
                            </div>
                          )}
                        </div>

                        {/* 우측 유동적 제어 영역 (최근본 삭제버튼 혹은 지원취소버튼) */}
                        <div className="flex flex-col items-end gap-2">
                          {activeProjectSubTab === 'viewed' && (
                            <button
                              onClick={(e) => handleDeleteRecentView(proj.id, e)}
                              className="p-1.5 text-slate-400 hover:text-red-600 hover:bg-slate-50 rounded-lg transition-colors"
                              title="삭제"
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
                    <h3 className="text-lg font-medium text-slate-900 mb-2">해당하는 프로젝트가 없습니다</h3>
                    <p className="text-slate-500 mb-6">새로운 프로젝트를 찾거나 직접 만들어보세요.</p>
                    <div className="flex justify-center gap-3">
                      <Link href="/projects"><Button variant="outline">프로젝트 찾기</Button></Link>
                      <Link href="/projects/new"><Button>프로젝트 만들기</Button></Link>
                    </div>
                  </Card>
                )}
              </motion.div>
            )}

            {/* [제안 탭 연동 완공 (필터 2종 쪼개기)] */}
            {activeTab === 'proposal' && (
              <motion.div key="proposal" initial={{ opacity: 0, y: 10 }} animate={{ opacity: 1, y: 0 }} exit={{ opacity: 0, y: -10 }} transition={{ duration: 0.2 }}>
                <div className="flex gap-2 mb-6 border-b border-slate-200 pb-4 overflow-x-auto">
                  <button
                    onClick={() => setActiveProposalFilter('applications')}
                    className={`px-4 py-2 rounded-full text-sm font-medium whitespace-nowrap transition-colors ${
                      activeProposalFilter === 'applications' ? 'bg-slate-900 text-white' : 'bg-slate-100 text-slate-600 hover:bg-slate-200'
                    }`}
                  >
                    내 프로젝트에 들어온 지원
                  </button>
                  <button
                    onClick={() => setActiveProposalFilter('proposals')}
                    className={`px-4 py-2 rounded-full text-sm font-medium whitespace-nowrap transition-colors ${
                      activeProposalFilter === 'proposals' ? 'bg-slate-900 text-white' : 'bg-slate-100 text-slate-600 hover:bg-slate-200'
                    }`}
                  >
                    내 포트폴리오에 온 제안
                  </button>
                </div>

                {contentLoading ? (
                  <div className="text-center py-12 text-slate-400">제안서 내역 확인 중...</div>
                ) : activeProposalFilter === 'applications' ? (
                  // 분기 A: 내 프로젝트에 온 지원 리스트업
                  applications.length > 0 ? (
                    <div className="space-y-4">
                      {applications.map((app) => (
                        <div key={app.applicationId} className="bg-white border border-slate-200 rounded-xl p-5 shadow-sm space-y-3">
                          <div className="flex justify-between items-center">
                            <span className="text-xs text-slate-400">공고: {app.projectTitle}</span>
                            <Badge variant={app.status === 'PENDING' ? 'outline' : app.status === 'ACCEPTED' ? 'default' : 'secondary'}>
                              {app.status}
                            </Badge>
                          </div>

                          {/* 메시지 클릭 시 지원자의 포트폴리오로 상세 라우팅 연동 */}
                          <div
                            onClick={() => router.push(`/portfolios/${app.applicationId}`)} // 엔티티 구조 매핑 기반 라우팅 경로 확보
                            className="bg-slate-50 hover:bg-slate-100 transition-colors p-3 rounded-lg cursor-pointer flex items-start gap-2.5"
                          >
                            <User className="w-4 h-4 text-slate-400 mt-0.5" />
                            <div className="flex-1">
                              <p className="text-sm font-semibold text-slate-900 mb-0.5">{app.applicantName} 님의 지원서</p>
                              <p className="text-sm text-slate-600">"{app.message}" <span className="text-xs text-blue-500 underline ml-1">포폴 보기</span></p>
                            </div>
                          </div>

                          {/* 미결정(PENDING) 상태일 때만 수락/거절 액션 단추 배출 */}
                          {app.status === 'PENDING' && (
                            <div className="flex justify-end gap-2 pt-1">
                              <button onClick={() => handleApplicationDecision(app.applicationId, false)} className="px-3 py-1.5 rounded-lg border border-slate-200 text-xs font-medium hover:bg-slate-50 text-slate-600 flex items-center gap-1"><X className="w-3 h-3" /> 거절</button>
                              <button onClick={() => handleApplicationDecision(app.applicationId, true)} className="px-3 py-1.5 rounded-lg bg-blue-600 text-xs font-medium hover:bg-blue-700 text-white flex items-center gap-1"><Check className="w-3 h-3" /> 팀원 수락</button>
                            </div>
                          )}
                        </div>
                      ))}
                    </div>
                  ) : (
                    <Card className="p-12 text-center border-dashed">
                      <MessageSquare className="w-12 h-12 text-slate-300 mx-auto mb-4" />
                      <h3 className="text-lg font-medium text-slate-900 mb-2">들어온 지원서가 없습니다.</h3>
                    </Card>
                  )
                ) : (
                  // 분기 B: 내 포트폴리오를 보고 리더들이 보낸 스카우트 제안서 리스트업
                  proposals.length > 0 ? (
                    <div className="space-y-4">
                      {proposals.map((prop) => (
                        <div key={prop.proposalId} className="bg-white border border-slate-200 rounded-xl p-5 shadow-sm space-y-3">
                          <div className="flex justify-between items-center">
                            <span className="text-xs text-slate-400">제안자: {prop.proposerName} 리더</span>
                            <Badge variant={prop.status === 'PENDING' ? 'outline' : prop.status === 'ACCEPTED' ? 'default' : 'secondary'}>
                              {prop.status}
                            </Badge>
                          </div>

                          {/* 메시지 클릭 시 캐스팅 제안이 온 프로젝트 상세페이지로 라우팅 */}
                          <div
                            onClick={() => router.push(`/projects/${prop.projectId}`)}
                            className="bg-blue-50/50 hover:bg-blue-50 transition-colors p-3 rounded-lg cursor-pointer flex items-start gap-2.5"
                          >
                            <Briefcase className="w-4 h-4 text-blue-400 mt-0.5" />
                            <div className="flex-1">
                              <p className="text-sm font-bold text-blue-900 mb-0.5">[{prop.projectTitle}] 팀 합류 제안</p>
                              <p className="text-sm text-slate-700">"{prop.message}" <span className="text-xs text-blue-600 underline ml-1">공고 상세 보기</span></p>
                            </div>
                          </div>

                          {prop.status === 'PENDING' && (
                            <div className="flex justify-end gap-2 pt-1">
                              <button onClick={() => handleProposalDecision(prop.proposalId, false)} className="px-3 py-1.5 rounded-lg border border-slate-200 text-xs font-medium hover:bg-slate-50 text-slate-600 flex items-center gap-1"><X className="w-3 h-3" /> 거절</button>
                              <button onClick={() => handleProposalDecision(prop.proposalId, true)} className="px-3 py-1.5 rounded-lg bg-slate-900 text-xs font-medium hover:bg-slate-800 text-white flex items-center gap-1"><Check className="w-3 h-3" /> 제안 수락 (합류)</button>
                            </div>
                          )}
                        </div>
                      ))}
                    </div>
                  ) : (
                    <Card className="p-12 text-center border-dashed">
                      <MessageSquare className="w-12 h-12 text-slate-300 mx-auto mb-4" />
                      <h3 className="text-lg font-medium text-slate-900 mb-2">받은 제안이 없습니다.</h3>
                    </Card>
                  )
                )}
              </motion.div>
            )}
          </AnimatePresence>

          <div className="mt-12 pt-8 border-t border-slate-200 text-center md:text-right">
            <button className="text-sm text-slate-400 hover:text-slate-600 underline underline-offset-4 transition-colors">
              회원 탈퇴
            </button>
          </div>
        </div>
      </div>
    </div>
  )
}