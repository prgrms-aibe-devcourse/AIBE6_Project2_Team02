'use client'

import { motion } from 'framer-motion'
import { useEffect, useState } from 'react'
import { toast } from 'sonner'

import { CheckCircle2, FolderX, ShieldAlert, UserX } from 'lucide-react'

import { Badge, Button, Card } from '../../../components/ui'
import { fetchProjectReports, fetchUserReports } from '../../../lib/api'
import type { ReportResponse } from '../../../types'

export default function AdminReportsPage() {
  const [userReports, setUserReports] = useState<ReportResponse[]>([])
  const [projectReports, setProjectReports] = useState<ReportResponse[]>([])
  const [loading, setLoading] = useState(true)
  const [activeTab, setActiveTab] = useState<'user' | 'project'>('user')

  useEffect(() => {
    const loadData = async () => {
      setLoading(true)
      try {
        const [uReports, pReports] = await Promise.all([
          fetchUserReports(),
          fetchProjectReports(),
        ])
        setUserReports(uReports || [])
        setProjectReports(pReports || [])
      } catch (err) {
        console.error('Failed to load reports:', err)
        toast.error('신고 내역을 불러오는데 실패했습니다.')
      } finally {
        setLoading(false)
      }
    }
    loadData()
  }, [])

  const handleResolve = (id: number) => {
    // 실제 앱에서는 여기서 API를 호출하여 상태를 변경해야 함
    setUserReports((prev) => prev.filter((r) => r.reportId !== id))
    setProjectReports((prev) => prev.filter((r) => r.reportId !== id))
    toast.success('신고가 처리되었습니다.')
  }

  const getReasonBadgeColor = (reasonType: string) => {
    // 백엔드 Enum 값에 따라 색상 분기 (예시)
    if (reasonType === 'OBSCENE')
      return 'bg-red-100 text-red-700 border-red-200'
    if (reasonType === 'DISRUPTIVE')
      return 'bg-orange-100 text-orange-700 border-orange-200'
    return 'bg-slate-100 text-slate-700 border-slate-200'
  }

  if (loading) {
    return (
      <div className="container mx-auto px-4 py-20 text-center text-slate-500">
        신고 내역을 불러오는 중...
      </div>
    )
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
              userReports.map((report) => (
                <motion.div
                  key={report.reportId}
                  layout
                  initial={{ opacity: 0 }}
                  animate={{ opacity: 1 }}
                >
                  <Card className="p-5 border-l-4 border-l-red-500 hover:shadow-md transition-shadow">
                    <div className="flex justify-between items-start mb-4">
                      <span
                        className={`px-2.5 py-1 rounded-full text-xs font-semibold border ${getReasonBadgeColor(report.reasonType)}`}
                      >
                        {report.reasonType}
                      </span>
                      <span className="text-xs text-slate-400">
                        {new Date(report.createdAt).toLocaleDateString()}
                      </span>
                    </div>

                    <div className="space-y-3">
                      <div className="bg-slate-50 rounded-lg p-3 border border-slate-100">
                        <div className="text-xs text-slate-500 mb-1">
                          신고당한 유저 ID
                        </div>
                        <div className="font-medium text-slate-900 text-sm">
                          {report.targetId}
                        </div>
                      </div>

                      <div>
                        <div className="text-xs text-slate-500 mb-1">
                          신고 상세사유
                        </div>
                        <p className="text-sm text-slate-700 bg-white border border-slate-200 rounded-lg p-3 leading-relaxed">
                          {report.reasonDetail}
                        </p>
                      </div>

                      <div className="flex items-center justify-between pt-2">
                        <div className="flex items-center gap-2">
                          <span className="text-xs text-slate-500">
                            신고자 ID:
                          </span>
                          <span className="text-xs font-medium text-slate-700">
                            {report.reporterId}
                          </span>
                        </div>
                        <Button
                          size="sm"
                          variant="outline"
                          onClick={() => handleResolve(report.reportId)}
                          className="h-8 text-xs"
                        >
                          처리 완료
                        </Button>
                      </div>
                    </div>
                  </Card>
                </motion.div>
              ))
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
              projectReports.map((report) => (
                <motion.div
                  key={report.reportId}
                  layout
                  initial={{ opacity: 0 }}
                  animate={{ opacity: 1 }}
                >
                  <Card className="p-5 border-l-4 border-l-orange-500 hover:shadow-md transition-shadow">
                    <div className="flex justify-between items-start mb-4">
                      <span
                        className={`px-2.5 py-1 rounded-full text-xs font-semibold border ${getReasonBadgeColor(report.reasonType)}`}
                      >
                        {report.reasonType}
                      </span>
                      <span className="text-xs text-slate-400">
                        {new Date(report.createdAt).toLocaleDateString()}
                      </span>
                    </div>

                    <div className="space-y-3">
                      <div className="bg-slate-50 rounded-lg p-3 border border-slate-100">
                        <div className="text-xs text-slate-500 mb-1">
                          신고당한 프로젝트 ID
                        </div>
                        <div className="font-medium text-slate-900 text-sm">
                          {report.targetId}
                        </div>
                      </div>

                      <div>
                        <div className="text-xs text-slate-500 mb-1">
                          신고 상세사유
                        </div>
                        <p className="text-sm text-slate-700 bg-white border border-slate-200 rounded-lg p-3 leading-relaxed">
                          {report.reasonDetail}
                        </p>
                      </div>

                      <div className="flex items-center justify-between pt-2">
                        <div className="flex items-center gap-2">
                          <span className="text-xs text-slate-500">
                            신고자 ID:
                          </span>
                          <span className="text-xs font-medium text-slate-700">
                            {report.reporterId}
                          </span>
                        </div>
                        <Button
                          size="sm"
                          variant="outline"
                          onClick={() => handleResolve(report.reportId)}
                          className="h-8 text-xs"
                        >
                          처리 완료
                        </Button>
                      </div>
                    </div>
                  </Card>
                </motion.div>
              ))
            )}
          </div>
        </div>
      </div>
    </div>
  )
}
