'use client'

import { motion } from 'framer-motion'
import { useState } from 'react'
import { toast } from 'sonner'

import { CheckCircle2, FolderX, ShieldAlert, UserX } from 'lucide-react'

import { Badge, Button, Card } from '../../../components/ui'

// Mock Data (Ideally these would come from an API or a shared data file)
const mockUsers: Record<string, { name: string; avatar: string }> = {
  u1: { name: '김철수', avatar: 'https://i.pravatar.cc/150?u=u1' },
  u2: { name: '이영희', avatar: 'https://i.pravatar.cc/150?u=u2' },
  u3: { name: '박지민', avatar: 'https://i.pravatar.cc/150?u=u3' },
  u5: { name: '최범석', avatar: 'https://i.pravatar.cc/150?u=u5' },
  u6: { name: '정예린', avatar: 'https://i.pravatar.cc/150?u=u6' },
  u7: { name: '강현우', avatar: 'https://i.pravatar.cc/150?u=u7' },
}

const mockProjects = [
  { id: '4', title: 'AI 기반 식단 관리 서비스' },
  { id: '5', title: '개발자 커뮤니티 플랫폼' },
]

type ReportReason = '음란' | '분탕' | '기타'
interface BaseReport {
  id: string
  reason: ReportReason
  detail: string
  reporterId: string
  createdAt: string
  status: 'pending' | 'resolved'
}
interface UserReport extends BaseReport {
  type: 'user'
  targetUserId: string
}
interface ProjectReport extends BaseReport {
  type: 'project'
  targetProjectId: string
}

const mockUserReports: UserReport[] = [
  {
    id: 'ur1',
    type: 'user',
    reason: '분탕',
    detail:
      '프로젝트 채팅방에서 지속적으로 다른 팀원들에게 공격적인 언행을 합니다.',
    reporterId: 'u1',
    targetUserId: 'u5',
    createdAt: '2026-06-09T10:00:00Z',
    status: 'pending',
  },
  {
    id: 'ur2',
    type: 'user',
    reason: '기타',
    detail: '포트폴리오 링크가 악성 사이트로 연결됩니다.',
    reporterId: 'u3',
    targetUserId: 'u7',
    createdAt: '2026-06-08T15:30:00Z',
    status: 'pending',
  },
]

const mockProjectReports: ProjectReport[] = [
  {
    id: 'pr1',
    type: 'project',
    reason: '음란',
    detail: '프로젝트 설명에 부적절한 이미지 링크가 포함되어 있습니다.',
    reporterId: 'u2',
    targetProjectId: '4',
    createdAt: '2026-06-10T09:15:00Z',
    status: 'pending',
  },
  {
    id: 'pr2',
    type: 'project',
    reason: '분탕',
    detail:
      '실제 프로젝트가 아니라 특정 사용자를 비방하기 위해 만들어진 가짜 프로젝트입니다.',
    reporterId: 'u6',
    targetProjectId: '5',
    createdAt: '2026-06-07T11:20:00Z',
    status: 'pending',
  },
]

export default function AdminReportsPage() {
  const [userReports, setUserReports] = useState<UserReport[]>(mockUserReports)
  const [projectReports, setProjectReports] =
    useState<ProjectReport[]>(mockProjectReports)
  const [activeTab, setActiveTab] = useState<'user' | 'project'>('user')

  const handleResolve = (id: string, type: 'user' | 'project') => {
    if (type === 'user') {
      setUserReports((prev) => prev.filter((r) => r.id !== id))
    } else {
      setProjectReports((prev) => prev.filter((r) => r.id !== id))
    }
    toast.success('신고가 처리되었습니다.')
  }

  const getReasonBadgeColor = (reason: ReportReason) => {
    switch (reason) {
      case '음란':
        return 'bg-red-100 text-red-700 border-red-200'
      case '분탕':
        return 'bg-orange-100 text-orange-700 border-orange-200'
      case '기타':
        return 'bg-slate-100 text-slate-700 border-slate-200'
    }
  }

  return (
    <div className="container mx-auto px-4 py-8 max-w-6xl">
      <div className="mb-8 flex items-center gap-3">
        <div className="w-12 h-12 rounded-xl bg-red-100 text-red-600 flex items-center justify-center">
          <ShieldAlert className="w-6 h-6" />
        </div>
        <div>
          <h1 className="text-2xl font-bold text-slate-900">
            관리자 신고 센터
          </h1>
          <p className="text-slate-500 text-sm mt-1">
            플랫폼 내 접수된 유저 및 프로젝트 신고 내역을 관리합니다.
          </p>
        </div>
      </div>

      {/* Mobile Tabs */}
      <div className="md:hidden flex space-x-2 mb-6 bg-slate-100 p-1 rounded-lg">
        <button
          onClick={() => setActiveTab('user')}
          className={`flex-1 py-2 text-sm font-medium rounded-md transition-colors ${
            activeTab === 'user'
              ? 'bg-white text-slate-900 shadow-sm'
              : 'text-slate-500 hover:text-slate-700'
          }`}
        >
          유저 신고 ({userReports.length})
        </button>
        <button
          onClick={() => setActiveTab('project')}
          className={`flex-1 py-2 text-sm font-medium rounded-md transition-colors ${
            activeTab === 'project'
              ? 'bg-white text-slate-900 shadow-sm'
              : 'text-slate-500 hover:text-slate-700'
          }`}
        >
          프로젝트 신고 ({projectReports.length})
        </button>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
        {/* User Reports Column */}
        <div
          className={`space-y-4 ${activeTab === 'user' ? 'block' : 'hidden md:block'}`}
        >
          <div className="flex items-center justify-between border-b border-slate-200 pb-4">
            <h2 className="text-lg font-semibold text-slate-900 flex items-center gap-2">
              <UserX className="w-5 h-5 text-slate-400" />
              유저 신고 목록
            </h2>
            <Badge variant="secondary" className="bg-slate-100">
              {userReports.length}건
            </Badge>
          </div>

          <div className="space-y-4">
            {userReports.length === 0 ? (
              <div className="text-center py-12 bg-slate-50 rounded-xl border border-slate-200 border-dashed">
                <CheckCircle2 className="w-8 h-8 text-emerald-500 mx-auto mb-2" />
                <p className="text-slate-500 font-medium">
                  처리 대기 중인 유저 신고가 없습니다.
                </p>
              </div>
            ) : (
              userReports.map((report) => {
                const targetUser = mockUsers[report.targetUserId]
                const reporter = mockUsers[report.reporterId]
                return (
                  <motion.div
                    key={report.id}
                    layout
                    initial={{ opacity: 0 }}
                    animate={{ opacity: 1 }}
                    exit={{ opacity: 0 }}
                  >
                    <Card className="p-5 border-l-4 border-l-red-500 hover:shadow-md transition-shadow">
                      <div className="flex justify-between items-start mb-4">
                        <span
                          className={`px-2.5 py-1 rounded-full text-xs font-semibold border ${getReasonBadgeColor(
                            report.reason,
                          )}`}
                        >
                          {report.reason}
                        </span>
                        <span className="text-xs text-slate-400">
                          {new Date(report.createdAt).toLocaleDateString()}
                        </span>
                      </div>

                      <div className="space-y-3">
                        <div className="bg-slate-50 rounded-lg p-3 border border-slate-100">
                          <div className="text-xs text-slate-500 mb-1">
                            신고당한 사람
                          </div>
                          <div className="flex items-center gap-2">
                            <img
                              src={
                                targetUser?.avatar ||
                                'https://i.pravatar.cc/150'
                              }
                              alt=""
                              className="w-6 h-6 rounded-full"
                            />
                            <span className="font-medium text-slate-900 text-sm">
                              {targetUser?.name || '알 수 없는 유저'}
                            </span>
                          </div>
                        </div>

                        <div>
                          <div className="text-xs text-slate-500 mb-1">
                            신고 상세사유
                          </div>
                          <p className="text-sm text-slate-700 bg-white border border-slate-200 rounded-lg p-3 leading-relaxed">
                            {report.detail}
                          </p>
                        </div>

                        <div className="flex items-center justify-between pt-2">
                          <div className="flex items-center gap-2">
                            <span className="text-xs text-slate-500">
                              신고자:
                            </span>
                            <span className="text-xs font-medium text-slate-700">
                              {reporter?.name || '알 수 없는 유저'}
                            </span>
                          </div>
                          <Button
                            size="sm"
                            variant="outline"
                            onClick={() => handleResolve(report.id, 'user')}
                            className="h-8 text-xs"
                          >
                            처리 완료
                          </Button>
                        </div>
                      </div>
                    </Card>
                  </motion.div>
                )
              })
            )}
          </div>
        </div>

        {/* Project Reports Column */}
        <div
          className={`space-y-4 ${activeTab === 'project' ? 'block' : 'hidden md:block'}`}
        >
          <div className="flex items-center justify-between border-b border-slate-200 pb-4">
            <h2 className="text-lg font-semibold text-slate-900 flex items-center gap-2">
              <FolderX className="w-5 h-5 text-slate-400" />
              프로젝트 신고 목록
            </h2>
            <Badge variant="secondary" className="bg-slate-100">
              {projectReports.length}건
            </Badge>
          </div>

          <div className="space-y-4">
            {projectReports.length === 0 ? (
              <div className="text-center py-12 bg-slate-50 rounded-xl border border-slate-200 border-dashed">
                <CheckCircle2 className="w-8 h-8 text-emerald-500 mx-auto mb-2" />
                <p className="text-slate-500 font-medium">
                  처리 대기 중인 프로젝트 신고가 없습니다.
                </p>
              </div>
            ) : (
              projectReports.map((report) => {
                const targetProject = mockProjects.find(
                  (p) => p.id === report.targetProjectId,
                )
                const reporter = mockUsers[report.reporterId]
                return (
                  <motion.div
                    key={report.id}
                    layout
                    initial={{ opacity: 0 }}
                    animate={{ opacity: 1 }}
                    exit={{ opacity: 0 }}
                  >
                    <Card className="p-5 border-l-4 border-l-orange-500 hover:shadow-md transition-shadow">
                      <div className="flex justify-between items-start mb-4">
                        <span
                          className={`px-2.5 py-1 rounded-full text-xs font-semibold border ${getReasonBadgeColor(
                            report.reason,
                          )}`}
                        >
                          {report.reason}
                        </span>
                        <span className="text-xs text-slate-400">
                          {new Date(report.createdAt).toLocaleDateString()}
                        </span>
                      </div>

                      <div className="space-y-3">
                        <div className="bg-slate-50 rounded-lg p-3 border border-slate-100">
                          <div className="text-xs text-slate-500 mb-1">
                            신고당한 프로젝트
                          </div>
                          <div className="font-medium text-slate-900 text-sm truncate">
                            {targetProject?.title || '알 수 없는 프로젝트'}
                          </div>
                        </div>

                        <div>
                          <div className="text-xs text-slate-500 mb-1">
                            신고 상세사유
                          </div>
                          <p className="text-sm text-slate-700 bg-white border border-slate-200 rounded-lg p-3 leading-relaxed">
                            {report.detail}
                          </p>
                        </div>

                        <div className="flex items-center justify-between pt-2">
                          <div className="flex items-center gap-2">
                            <span className="text-xs text-slate-500">
                              신고자:
                            </span>
                            <span className="text-xs font-medium text-slate-700">
                              {reporter?.name || '알 수 없는 유저'}
                            </span>
                          </div>
                          <Button
                            size="sm"
                            variant="outline"
                            onClick={() => handleResolve(report.id, 'project')}
                            className="h-8 text-xs"
                          >
                            처리 완료
                          </Button>
                        </div>
                      </div>
                    </Card>
                  </motion.div>
                )
              })
            )}
          </div>
        </div>
      </div>
    </div>
  )
}
