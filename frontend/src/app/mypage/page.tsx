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
} from 'lucide-react'

import { Badge, Button, Card } from '../../components/ui'
import type { Portfolio } from '../../types'
import { leaderPositionOptions } from '../../constants/project'
import { useAuth } from '../providers'

import ProjectTab from './components/ProjectTab'
import ProposalTab from './components/ProposalTab'

type Tab = 'portfolio' | 'project' | 'proposal'
type PortfolioSubTab = 'portfolio' | 'peerReview'

export default function MyPage() {
  const positionLabel = (value: string | null) =>
  leaderPositionOptions.find((o) => o.value === value)?.label ?? value ?? ''
  const { user, loading: authLoading, logout } = useAuth()
  const router = useRouter()

  const activeTabFromStorage =
    typeof window !== 'undefined' ? localStorage.getItem('mypage_tab') : null
  const [activeTab, setActiveTab] = useState<Tab>(
    (activeTabFromStorage as Tab) || 'portfolio',
  )
  const [activePortfolioSubTab, setActivePortfolioSubTab] =
    useState<PortfolioSubTab>('portfolio')

  const [portfolio, setPortfolio] = useState<Portfolio | null>(null)
  const [portfolioLoading, setPortfolioLoading] = useState(true)
  const [avatarError, setAvatarError] = useState(false)

  const handleTabChange = (tab: Tab) => {
    setActiveTab(tab)
    if (typeof window !== 'undefined') localStorage.setItem('mypage_tab', tab)
  }

  useEffect(() => {
    if (!authLoading && !user) {
      router.replace('/')
      return
    }
    if (!authLoading && user) {
      fetch('/portfolios/me')
        .then((res) => res.json())
        .then((res) => {
          if (res.code === '200') setPortfolio(res.data)
        })
        .catch(() => setPortfolio(null))
        .finally(() => setPortfolioLoading(false))
    }
  }, [authLoading, user, router])

  if (authLoading)
    return (
      <div className="container mx-auto px-4 py-20 text-center text-slate-500">
        로딩 중...
      </div>
    )
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
          <button className="absolute bottom-0 right-0 bg-white p-1.5 rounded-full border border-slate-200 shadow-sm hover:bg-slate-50">
            <Settings className="w-4 h-4 text-slate-600" />
          </button>
        </div>
        <div className="flex-1 text-center md:text-left">
          <h1 className="text-2xl font-bold text-slate-900 mb-1">
            {user?.nickname ?? '...'}
          </h1>
          {portfolio && (
            <>
              <p className="text-slate-500 mb-3">{positionLabel(portfolio.desiredPosition)}</p>
              {portfolio.techStacks.length > 0 && (
                <div className="flex flex-wrap justify-center md:justify-start gap-2">
                  {portfolio.techStacks.map((stack) => (
                    <Badge key={stack} variant="secondary">
                      {stack}
                    </Badge>
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
            onClick={async () => {
              await logout()
              router.push('/')
            }}
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
                className={`flex-1 md:w-full text-left px-6 py-4 font-medium flex items-center justify-between ${activeTab === 'portfolio' ? 'bg-blue-50 text-blue-700 border-l-4 border-blue-600' : 'text-slate-600 hover:bg-slate-50'}`}
              >
                <span className="flex items-center gap-2">
                  <FileText className="w-4 h-4" /> 포트폴리오
                </span>
                <ChevronRight className="w-4 h-4 hidden md:block opacity-50" />
              </button>
              <button
                onClick={() => handleTabChange('project')}
                className={`flex-1 md:w-full text-left px-6 py-4 font-medium flex items-center justify-between ${activeTab === 'project' ? 'bg-blue-50 text-blue-700 border-l-4 border-blue-600' : 'text-slate-600 hover:bg-slate-50'}`}
              >
                <span className="flex items-center gap-2">
                  <Briefcase className="w-4 h-4" /> 프로젝트
                </span>
                <ChevronRight className="w-4 h-4 hidden md:block opacity-50" />
              </button>
              <button
                onClick={() => handleTabChange('proposal')}
                className={`flex-1 md:w-full text-left px-6 py-4 font-medium flex items-center justify-between ${activeTab === 'proposal' ? 'bg-blue-50 text-blue-700 border-l-4 border-blue-600' : 'text-slate-600 hover:bg-slate-50'}`}
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
              >
                <div className="flex gap-2 mb-6 border-b border-slate-200 pb-4 overflow-x-auto">
                  <button
                    onClick={() => setActivePortfolioSubTab('portfolio')}
                    className={`px-4 py-2 rounded-full text-sm font-medium whitespace-nowrap ${activePortfolioSubTab === 'portfolio' ? 'bg-slate-900 text-white' : 'bg-slate-100 text-slate-600'}`}
                  >
                    포폴
                  </button>
                  <button
                    onClick={() => setActivePortfolioSubTab('peerReview')}
                    className={`px-4 py-2 rounded-full text-sm font-medium whitespace-nowrap ${activePortfolioSubTab === 'peerReview' ? 'bg-slate-900 text-white' : 'bg-slate-100 text-slate-600'}`}
                  >
                    내게 달린 피어리뷰
                  </button>
                </div>
                {activePortfolioSubTab === 'portfolio' &&
                  (portfolioLoading ? (
                    <div className="text-center py-12 text-slate-400">
                      로딩 중...
                    </div>
                  ) : portfolio ? (
                    <Card className="p-6 space-y-5">
                      <div className="flex items-start justify-between">
                        <div>
                          <h2 className="text-xl font-bold text-slate-900">{portfolio.title}</h2>
                          {portfolio.desiredPosition && (
                            <p className="text-sm text-blue-600 mt-1">{positionLabel(portfolio.desiredPosition)}</p>
                          )}
                        </div>
                        <Link href="/mypage/portfolio/edit">
                          <Button
                            size="sm"
                            variant="outline"
                            className="gap-1.5"
                          >
                            <Pencil className="w-3.5 h-3.5" /> 수정
                          </Button>
                        </Link>
                      </div>
                      <p className="text-slate-600 text-sm leading-relaxed">
                        {portfolio.introduction}
                      </p>
                    </Card>
                  ) : (
                    <Card className="p-12 text-center border-dashed">
                      <FileText className="w-12 h-12 text-slate-300 mx-auto mb-4" />
                      <h3 className="text-lg font-medium text-slate-900 mb-2">
                        등록된 포트폴리오가 없습니다
                      </h3>
                      <Link href="/mypage/portfolio/new">
                        <Button>포트폴리오 등록하기</Button>
                      </Link>
                    </Card>
                  ))}
              </motion.div>
            )}
            
            {activeTab === 'project' && <ProjectTab user={user} />}
            {activeTab === 'proposal' && <ProposalTab user={user} />}
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
