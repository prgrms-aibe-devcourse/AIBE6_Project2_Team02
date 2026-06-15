'use client'

import { AnimatePresence, motion } from 'framer-motion'
import { useEffect, useState } from 'react'

import Link from 'next/link'
import { useRouter } from 'next/navigation'

import {
  Briefcase,
  ChevronRight,
  ExternalLink,
  FileText,
  Github,
  Globe,
  LogOut,
  MessageSquare,
  Pencil,
  Settings,
} from 'lucide-react'

import { Badge, Button, Card } from '../../components/ui'
import { fetchMyPortfolio } from '../../lib/api'
import { useAuth } from '../providers'
import type { Portfolio } from '../../types'

type Tab = 'portfolio' | 'project' | 'proposal'
type PortfolioSubTab = 'portfolio' | 'peerReview'
type ProjectSubTab =
  | 'uploaded'
  | 'participating'
  | 'applied'
  | 'completed'
  | 'viewed'

export default function MyPage() {
  const { user, loading: authLoading, logout } = useAuth()
  const router = useRouter()
  const [activeTab, setActiveTab] = useState<Tab>('portfolio')
  const [activePortfolioSubTab, setActivePortfolioSubTab] =
    useState<PortfolioSubTab>('portfolio')
  const [activeProjectSubTab, setActiveProjectSubTab] =
    useState<ProjectSubTab>('uploaded')
  const [portfolio, setPortfolio] = useState<Portfolio | null>(null)
  const [portfolioLoading, setPortfolioLoading] = useState(true)
  const [avatarError, setAvatarError] = useState(false)

  useEffect(() => {
    if (!authLoading && !user) {
      router.replace('/')
      return
    }
    if (!authLoading && user) {
      fetchMyPortfolio()
        .then(setPortfolio)
        .catch(() => setPortfolio(null))
        .finally(() => setPortfolioLoading(false))
    }
  }, [authLoading, user, router])

  if (authLoading) {
    return <div className="container mx-auto px-4 py-20 text-center text-slate-500">로딩 중...</div>
  }

  if (!user) return null

  return (
    <div className="container mx-auto px-4 py-8 max-w-5xl">
      {/* Profile Header */}
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
          <h1 className="text-2xl font-bold text-slate-900 mb-1">
            {user?.nickname ?? '...'}
          </h1>
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
                onClick={() => setActiveTab('portfolio')}
                className={`flex-1 md:w-full text-left px-6 py-4 font-medium transition-colors flex items-center justify-between ${
                  activeTab === 'portfolio'
                    ? 'bg-blue-50 text-blue-700 border-b-2 md:border-b-0 md:border-l-4 border-blue-600'
                    : 'text-slate-600 hover:bg-slate-50'
                }`}
              >
                <span className="flex items-center gap-2">
                  <FileText className="w-4 h-4" /> 포트폴리오
                </span>
                <ChevronRight className="w-4 h-4 hidden md:block opacity-50" />
              </button>
              <button
                onClick={() => setActiveTab('project')}
                className={`flex-1 md:w-full text-left px-6 py-4 font-medium transition-colors flex items-center justify-between ${
                  activeTab === 'project'
                    ? 'bg-blue-50 text-blue-700 border-b-2 md:border-b-0 md:border-l-4 border-blue-600'
                    : 'text-slate-600 hover:bg-slate-50'
                }`}
              >
                <span className="flex items-center gap-2">
                  <Briefcase className="w-4 h-4" /> 프로젝트
                </span>
                <ChevronRight className="w-4 h-4 hidden md:block opacity-50" />
              </button>
              <button
                onClick={() => setActiveTab('proposal')}
                className={`flex-1 md:w-full text-left px-6 py-4 font-medium transition-colors flex items-center justify-between ${
                  activeTab === 'proposal'
                    ? 'bg-blue-50 text-blue-700 border-b-2 md:border-b-0 md:border-l-4 border-blue-600'
                    : 'text-slate-600 hover:bg-slate-50'
                }`}
              >
                <span className="flex items-center gap-2">
                  <MessageSquare className="w-4 h-4" /> 제안
                </span>
                <ChevronRight className="w-4 h-4 hidden md:block opacity-50" />
              </button>
            </div>
          </div>
        </div>

        {/* Content Panel */}
        <div className="flex-1">
          <AnimatePresence mode="wait">
            {activeTab === 'portfolio' && (
              <motion.div
                key="portfolio"
                initial={{ opacity: 0, y: 10 }}
                animate={{ opacity: 1, y: 0 }}
                exit={{ opacity: 0, y: -10 }}
                transition={{ duration: 0.2 }}
              >
                <div className="flex gap-2 mb-6 border-b border-slate-200 pb-4 overflow-x-auto">
                  <button
                    onClick={() => setActivePortfolioSubTab('portfolio')}
                    className={`px-4 py-2 rounded-full text-sm font-medium whitespace-nowrap transition-colors ${
                      activePortfolioSubTab === 'portfolio'
                        ? 'bg-slate-900 text-white'
                        : 'bg-slate-100 text-slate-600 hover:bg-slate-200'
                    }`}
                  >
                    포폴
                  </button>
                  <button
                    onClick={() => setActivePortfolioSubTab('peerReview')}
                    className={`px-4 py-2 rounded-full text-sm font-medium whitespace-nowrap transition-colors ${
                      activePortfolioSubTab === 'peerReview'
                        ? 'bg-slate-900 text-white'
                        : 'bg-slate-100 text-slate-600 hover:bg-slate-200'
                    }`}
                  >
                    내게 달린 피어리뷰
                  </button>
                </div>

                {activePortfolioSubTab === 'portfolio' && (
                  portfolioLoading ? (
                    <div className="text-center py-12 text-slate-400">로딩 중...</div>
                  ) : portfolio ? (
                    <Card className="p-6 space-y-5">
                      <div className="flex items-start justify-between">
                        <div>
                          <h2 className="text-xl font-bold text-slate-900">{portfolio.title}</h2>
                          {portfolio.desiredPosition && (
                            <p className="text-sm text-blue-600 mt-1">{portfolio.desiredPosition}</p>
                          )}
                        </div>
                        <Link href="/mypage/portfolio/edit">
                          <Button size="sm" variant="outline" className="gap-1.5">
                            <Pencil className="w-3.5 h-3.5" /> 수정
                          </Button>
                        </Link>
                      </div>

                      {portfolio.introduction && (
                        <p className="text-slate-600 text-sm leading-relaxed">{portfolio.introduction}</p>
                      )}

                      {portfolio.techStacks.length > 0 && (
                        <div>
                          <p className="text-xs font-medium text-slate-500 mb-2">기술 스택</p>
                          <div className="flex flex-wrap gap-2">
                            {portfolio.techStacks.map((stack) => (
                              <Badge key={stack} variant="secondary">{stack}</Badge>
                            ))}
                          </div>
                        </div>
                      )}

                      {(portfolio.githubUrl || portfolio.blogUrl || portfolio.deployUrl) && (
                        <div>
                          <p className="text-xs font-medium text-slate-500 mb-2">링크</p>
                          <div className="flex flex-wrap gap-3">
                            {portfolio.githubUrl && (
                              <a href={portfolio.githubUrl} target="_blank" rel="noreferrer"
                                className="flex items-center gap-1.5 text-sm text-slate-600 hover:text-slate-900 transition-colors">
                                <Github className="w-4 h-4" /> GitHub
                                <ExternalLink className="w-3 h-3 opacity-50" />
                              </a>
                            )}
                            {portfolio.blogUrl && (
                              <a href={portfolio.blogUrl} target="_blank" rel="noreferrer"
                                className="flex items-center gap-1.5 text-sm text-slate-600 hover:text-slate-900 transition-colors">
                                <Globe className="w-4 h-4" /> 블로그
                                <ExternalLink className="w-3 h-3 opacity-50" />
                              </a>
                            )}
                            {portfolio.deployUrl && (
                              <a href={portfolio.deployUrl} target="_blank" rel="noreferrer"
                                className="flex items-center gap-1.5 text-sm text-slate-600 hover:text-slate-900 transition-colors">
                                <Globe className="w-4 h-4" /> 배포 URL
                                <ExternalLink className="w-3 h-3 opacity-50" />
                              </a>
                            )}
                          </div>
                        </div>
                      )}

                      <div className="flex items-center gap-2 pt-2 border-t border-slate-100">
                        <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${
                          portfolio.isPublished
                            ? 'bg-green-100 text-green-700'
                            : 'bg-slate-100 text-slate-500'
                        }`}>
                          {portfolio.isPublished ? '공개' : '비공개'}
                        </span>
                      </div>
                    </Card>
                  ) : (
                    <Card className="p-12 text-center border-dashed">
                      <FileText className="w-12 h-12 text-slate-300 mx-auto mb-4" />
                      <h3 className="text-lg font-medium text-slate-900 mb-2">
                        등록된 포트폴리오가 없습니다
                      </h3>
                      <p className="text-slate-500 mb-6">
                        나만의 멋진 포트폴리오를 등록하고 팀 제안을 받아보세요.
                      </p>
                      <Link href="/mypage/portfolio/new">
                        <Button>포트폴리오 등록하기</Button>
                      </Link>
                    </Card>
                  )
                )}

                {activePortfolioSubTab === 'peerReview' && (
                  <Card className="p-12 text-center border-dashed">
                    <MessageSquare className="w-12 h-12 text-slate-300 mx-auto mb-4" />
                    <h3 className="text-lg font-medium text-slate-900 mb-2">
                      아직 받은 피어리뷰가 없습니다
                    </h3>
                    <p className="text-slate-500">
                      프로젝트를 완료하고 동료들로부터 리뷰를 받아보세요.
                    </p>
                  </Card>
                )}
              </motion.div>
            )}

            {activeTab === 'project' && (
              <motion.div
                key="project"
                initial={{ opacity: 0, y: 10 }}
                animate={{ opacity: 1, y: 0 }}
                exit={{ opacity: 0, y: -10 }}
                transition={{ duration: 0.2 }}
              >
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
                      onClick={() =>
                        setActiveProjectSubTab(subTab.id as ProjectSubTab)
                      }
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

                <Card className="p-12 text-center border-dashed">
                  <Briefcase className="w-12 h-12 text-slate-300 mx-auto mb-4" />
                  <h3 className="text-lg font-medium text-slate-900 mb-2">
                    해당하는 프로젝트가 없습니다
                  </h3>
                  <p className="text-slate-500 mb-6">
                    새로운 프로젝트를 찾거나 직접 만들어보세요.
                  </p>
                  <div className="flex justify-center gap-3">
                    <Link href="/projects">
                      <Button variant="outline">프로젝트 찾기</Button>
                    </Link>
                    <Link href="/projects/new">
                      <Button>프로젝트 만들기</Button>
                    </Link>
                  </div>
                </Card>
              </motion.div>
            )}

            {activeTab === 'proposal' && (
              <motion.div
                key="proposal"
                initial={{ opacity: 0, y: 10 }}
                animate={{ opacity: 1, y: 0 }}
                exit={{ opacity: 0, y: -10 }}
                transition={{ duration: 0.2 }}
              >
                <Card className="p-12 text-center border-dashed">
                  <MessageSquare className="w-12 h-12 text-slate-300 mx-auto mb-4" />
                  <h3 className="text-lg font-medium text-slate-900 mb-2">
                    받은 제안이 없습니다
                  </h3>
                  <p className="text-slate-500">
                    포트폴리오를 업데이트하면 더 많은 제안을 받을 수 있습니다.
                  </p>
                </Card>
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
