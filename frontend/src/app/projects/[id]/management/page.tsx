'use client'

import React, { useEffect, useState } from 'react'
import { toast } from 'sonner'

import Link from 'next/link'
import { useParams, useRouter } from 'next/navigation'

import {
  ArrowLeft,
  Code2,
  ExternalLink,
  Pencil,
  RefreshCw,
  Star,
  Trash2,
  UserX,
  Users,
} from 'lucide-react'

import { Badge, Button, Card } from '../../../../components/ui'
import { useDialog } from '../../../../components/DialogProvider'
import {
  deleteProject,
  fetchApplicant,
  fetchApplicantTOteam,
  fetchProjectPermissions,
  fetchProject_manage,
  kickProjectMember,
  leaveProject,
  rejectApplicant,
  updateMemberRole,
  updateProjectStatus,
} from '../../../../lib/api'
import type { Applicant, Project_manage } from '../../../../types'
import { useAuth } from '../../../providers'

const recruitmentOptions = [
  { value: 'RECRUITING', label: '모집 중' },
  { value: 'CLOSED', label: '인원 모집 마감' },
]

const progressOptions = [
  { value: 'IN_PROGRESS', label: '프로젝트 진행 중' },
  { value: 'COMPLETED', label: '프로젝트 완료' },
  { value: 'DISBANDED', label: '프로젝트 중단' },
  { value: 'CANCELLED', label: '프로젝트 취소' },
]

const statusLabelMap: Record<string, string> = {
  RECRUITING: '모집 중',
  CLOSED: '인원 모집 마감',
  IN_PROGRESS: '프로젝트 진행 중',
  COMPLETED: '프로젝트 완료',
  DISBANDED: '프로젝트 중단',
  CANCELLED: '프로젝트 취소',
}

const statusVariantMap: Record<string, 'success' | 'purple' | 'secondary'> = {
  RECRUITING: 'success',
  CLOSED: 'secondary',
  IN_PROGRESS: 'purple',
  COMPLETED: 'secondary',
  DISBANDED: 'secondary',
  CANCELLED: 'secondary',
}

const categoryMap: Record<string, string> = {
  Web: '웹',
  Mobile: '모바일',
  AI: 'AI',
  Game: '게임',
  Other: '기타',
}

const roleMap: Record<string, string> = {
  LEADER: '리더',
  MANAGER: '매니저',
  MEMBER: '멤버',
}

const positionMap: Record<string, string> = {
  BACKEND: '백엔드 개발자',
  FRONTEND: '프론트엔드 개발자',
  FULL_STACK: '풀스택 개발자',
  DESIGNER: '디자이너',
  PRODUCT_MANAGER: '프로덕트 매니저',
}

export default function ProjectManagementPage() {
  const params = useParams()
  const id = params?.id as string
  const router = useRouter()
  const { user: authUser } = useAuth()
  const { confirmDialog } = useDialog()
  const [project, setProject] = useState<Project_manage | null>(null)
  const [applicants, setApplicants] = useState<Applicant[]>([])
  const [loading, setLoading] = useState(true)
  const [canEdit, setCanEdit] = useState(false)
  const [isLeader, setIsLeader] = useState(false)
  const [selectedStatus, setSelectedStatus] = useState('RECRUITING')
  const [currentStatus, setCurrentStatus] = useState('RECRUITING')
  const [statusLoading, setStatusLoading] = useState(false)
  const [approvingId, setApprovingId] = useState<string | null>(null)
  const [rejectingId, setRejectingId] = useState<string | null>(null)
  const [kickingId, setKickingId] = useState<string | null>(null)
  const [roleChangingId, setRoleChangingId] = useState<string | null>(null)
  const [applicantsRefreshing, setApplicantsRefreshing] = useState(false)
  const [pageRefreshing, setPageRefreshing] = useState(false)
  const [leaving, setLeaving] = useState(false)
  const [deleting, setDeleting] = useState(false)

  const refreshAll = () => {
    setPageRefreshing(true)
    Promise.all([
      fetchProject_manage(id).then((res) => {
        setProject(res)
        setSelectedStatus(res.recruitmentStatus ?? 'RECRUITING')
        setCurrentStatus(res.recruitmentStatus ?? 'RECRUITING')
      }),
      fetchApplicant(id).then((res) =>
        setApplicants((res as any).Applicant ?? res ?? []),
      ),
    ])
      .catch(() => {})
      .finally(() => setPageRefreshing(false))
  }

  useEffect(() => {
    if (!id) {
      setLoading(false)
      return
    }
    fetchProject_manage(id)
      .then((res) => {
        setProject(res)
        setSelectedStatus(res.recruitmentStatus ?? 'RECRUITING')
        setCurrentStatus(res.recruitmentStatus ?? 'RECRUITING')
      })
      .catch(() => setProject(null))
      .finally(() => setLoading(false))
  }, [id])

  useEffect(() => {
    if (!id) return
    fetchApplicant(id)
      .then((res) => setApplicants((res as any).Applicant ?? res ?? []))
      .catch(() => setApplicants([]))
  }, [id])

  useEffect(() => {
    if (!id) return
    fetchProjectPermissions(id)
      .then((p) => {
        setCanEdit(p.canEdit)
        setIsLeader(p.isLeader)
      })
      .catch(() => {
        setCanEdit(false)
        setIsLeader(false)
      })
  }, [id])

  const handleStatusChange = (e: React.FormEvent) => {
    e.preventDefault()
    setStatusLoading(true)
    updateProjectStatus(id, selectedStatus)
      .then(() => {
        setCurrentStatus(selectedStatus)
        toast.success('프로젝트 상태가 변경되었습니다.')
        router.refresh()
      })
      .catch(() => toast.error('상태 변경에 실패했습니다.'))
      .finally(() => setStatusLoading(false))
  }

  const handleReject = (applicantId: string, applicantName: string) => {
    setRejectingId(applicantId)
    rejectApplicant(id, applicantId)
      .then(() => {
        setApplicants((prev) => prev.filter((a) => a.id !== applicantId))
        toast.success(`${applicantName}님의 지원을 거절했습니다.`)
      })
      .catch(() => toast.error('거절에 실패했습니다.'))
      .finally(() => setRejectingId(null))
  }

  const handleApprove = (applicantId: string) => {
    setApprovingId(applicantId)
    fetchApplicantTOteam(applicantId, id)
      .then(() => {
        toast.success('팀원으로 승인했습니다.')
        Promise.all([fetchProject_manage(id), fetchApplicant(id)]).then(
          ([proj, apps]) => {
            setProject(proj)
            setApplicants((apps as any).Applicant ?? apps ?? [])
          },
        )
      })
      .catch(() => toast.error('승인에 실패했습니다.'))
      .finally(() => setApprovingId(null))
  }

  const handleKick = async (memberId: string, memberName: string) => {
    if (
      !(await confirmDialog(`${memberName}님을 프로젝트에서 방출하시겠습니까?`, {
        title: '프로젝트 멤버 방출',
        confirmText: '방출',
        destructive: true,
      }))
    )
      return
    setKickingId(memberId)
    kickProjectMember(id, memberId)
      .then(() => {
        toast.success(`${memberName}님을 방출했습니다.`)
        fetchProject_manage(id).then((proj) => setProject(proj))
      })
      .catch(() => toast.error('방출에 실패했습니다.'))
      .finally(() => setKickingId(null))
  }

  const handleDelete = async () => {
    if (
      !(await confirmDialog('프로젝트를 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.', {
        title: '프로젝트 삭제',
        confirmText: '삭제',
        destructive: true,
      }))
    )
      return
    setDeleting(true)
    deleteProject(id)
      .then(() => {
        toast.success('프로젝트가 삭제되었습니다.')
        router.push('/projects')
      })
      .catch(() => toast.error('삭제에 실패했습니다.'))
      .finally(() => setDeleting(false))
  }

  const handleLeave = async () => {
    if (
      !(await confirmDialog('프로젝트에서 탈퇴하시겠습니까?', {
        title: '프로젝트 탈퇴',
        confirmText: '탈퇴',
        destructive: true,
      }))
    )
      return
    setLeaving(true)
    leaveProject(id)
      .then(() => {
        toast.success('프로젝트에서 탈퇴했습니다.')
        router.push('/projects')
      })
      .catch(() => toast.error('탈퇴에 실패했습니다.'))
      .finally(() => setLeaving(false))
  }

  const handleRoleChange = (
    memberId: string,
    memberName: string,
    role: string,
  ) => {
    setRoleChangingId(memberId)
    updateMemberRole(id, memberId, role)
      .then(() => toast.success(`${memberName}님 권한을 변경했습니다.`))
      .catch(() => toast.error('권한 변경에 실패했습니다.'))
      .finally(() => setRoleChangingId(null))
  }

  if (loading) {
    return (
      <div className="container mx-auto px-4 py-20 text-center text-slate-500">
        불러오는 중...
      </div>
    )
  }

  if (!project) {
    return (
      <div className="container mx-auto px-4 py-20 text-center">
        <h2 className="text-2xl font-bold text-slate-900 mb-4">
          프로젝트를 찾을 수 없어요
        </h2>
        <Button onClick={() => router.push('/projects')}>
          프로젝트 목록으로
        </Button>
      </div>
    )
  }

  const leaderEntry = {
    id: project.leader.id,
    name: project.leader.name,
    avatar: project.leader.avatar,
    role: 'LEADER',
    position: project.leader.role ?? '',
    isLeader: true,
  }
  const displayMembers = [
    leaderEntry,
    ...project.pmResponses
      .filter((m) => m.id !== project.leader.id)
      .map((m) => ({ ...m, isLeader: false })),
  ]
  const isRecruiting = currentStatus === 'RECRUITING'
  const isCompleted = currentStatus === 'COMPLETED'
  const isProjectPhase = ['IN_PROGRESS', 'COMPLETED', 'DISBANDED', 'CANCELLED'].includes(currentStatus)
  const showProgressSelect = currentStatus === 'CLOSED' || isProjectPhase

  return (
    <div className="bg-slate-50 min-h-screen pb-20">
      {/* Header */}
      <div className="bg-white border-b border-slate-200 pt-8 pb-8">
        <div className="container mx-auto px-4 max-w-5xl">
          <div className="flex items-center justify-between mb-6">
            <Link
              href={`/projects/${id}`}
              className="inline-flex items-center text-sm text-slate-500 hover:text-slate-900 transition-colors"
            >
              <ArrowLeft className="h-4 w-4 mr-1" /> 프로젝트로 돌아가기
            </Link>
            <button
              className="inline-flex items-center gap-1.5 text-xs text-slate-500 hover:text-slate-900 transition-colors disabled:opacity-50"
              disabled={pageRefreshing}
              onClick={refreshAll}
            >
              <RefreshCw className={`h-3.5 w-3.5 ${pageRefreshing ? 'animate-spin' : ''}`} />
              새로고침
            </button>
          </div>

          <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
            <div>
              <div className="flex items-center gap-2 mb-2">
                <Badge variant={statusVariantMap[currentStatus] ?? 'secondary'}>
                  {statusLabelMap[currentStatus] ?? currentStatus}
                </Badge>
                <Badge variant="purple">
                  {categoryMap[project.category] ?? project.category}
                </Badge>
              </div>
              <h1 className="text-2xl md:text-3xl font-bold text-slate-900">
                {project.title}
              </h1>
            </div>
            <div className="flex flex-col gap-2 shrink-0">
              {canEdit && (
                <Button
                  size="sm"
                  variant="outline"
                  className="gap-2"
                  onClick={() => router.push(`/projects/${id}/edit`)}
                >
                  <Pencil className="h-4 w-4" />
                  프로젝트 수정
                </Button>
              )}
              {!isLeader && (
                <Button
                  size="sm"
                  variant="outline"
                  className="gap-2 text-red-500 border-red-200 hover:bg-red-50 hover:text-red-700"
                  disabled={leaving}
                  onClick={handleLeave}
                >
                  <UserX className="h-4 w-4" />
                  {leaving ? '처리중...' : '팀 탈퇴'}
                </Button>
              )}
              {isLeader && currentStatus === 'CANCELLED' && (
                <Button
                  size="sm"
                  variant="outline"
                  className="gap-2 text-red-600 border-red-300 hover:bg-red-50 hover:text-red-800"
                  disabled={deleting}
                  onClick={handleDelete}
                >
                  <Trash2 className="h-4 w-4" />
                  {deleting ? '삭제 중...' : '프로젝트 삭제'}
                </Button>
              )}
            </div>
          </div>
        </div>
      </div>

      <div className="container mx-auto px-4 max-w-5xl mt-8 space-y-6">
        {/* 상태 변경 */}
        {isLeader && (
          <Card className="p-6">
            <h2 className="text-base font-semibold text-slate-900 mb-4">
              프로젝트 상태 변경
            </h2>
            <form onSubmit={handleStatusChange} className="space-y-3">
              <div className="flex items-center gap-3">
                <div className="flex-1 max-w-xs">
                  <label className="block text-xs text-slate-500 mb-1">모집 상태</label>
                  <select
                    value={recruitmentOptions.some((o) => o.value === selectedStatus) ? selectedStatus : 'CLOSED'}
                    onChange={(e) => setSelectedStatus(e.target.value)}
                    disabled={['IN_PROGRESS', 'COMPLETED', 'CANCELLED'].includes(currentStatus)}
                    className="w-full rounded-lg border border-slate-200 bg-white px-3 py-2 text-sm text-slate-900 focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500 disabled:bg-slate-100 disabled:text-slate-400 disabled:cursor-not-allowed"
                  >
                    {recruitmentOptions.map((opt) => (
                      <option key={opt.value} value={opt.value}>
                        {opt.label}
                      </option>
                    ))}
                  </select>
                </div>
                {showProgressSelect && (
                  <div className="flex-1 max-w-xs">
                    <label className="block text-xs text-slate-500 mb-1">진행 상태</label>
                    <select
                      value={progressOptions.some((o) => o.value === selectedStatus) ? selectedStatus : ''}
                      onChange={(e) => setSelectedStatus(e.target.value)}
                      className="w-full rounded-lg border border-slate-200 bg-white px-3 py-2 text-sm text-slate-900 focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
                    >
                      <option value="" disabled>선택하세요</option>
                      {progressOptions.map((opt) => (
                        <option key={opt.value} value={opt.value}>
                          {opt.label}
                        </option>
                      ))}
                    </select>
                  </div>
                )}
                <div className="self-end">
                  <Button
                    type="submit"
                    size="sm"
                    variant="gradient"
                    disabled={statusLoading || !selectedStatus}
                  >
                    {statusLoading ? '변경 중...' : '변경 적용'}
                  </Button>
                </div>
              </div>
            </form>
          </Card>
        )}

        {/* 참여자 목록 */}
        <Card className="p-6">
          <h2 className="text-base font-semibold text-slate-900 mb-5 flex items-center gap-2">
            <Users className="h-5 w-5 text-blue-600" /> 참여자 목록
            <span className="ml-1 bg-blue-100 text-blue-700 text-xs font-semibold px-2 py-0.5 rounded-full">
              {displayMembers.length}
            </span>
          </h2>

          {displayMembers.length > 0 ? (
            <div className="overflow-x-auto">
              <table className="w-full text-sm">
                <thead>
                  <tr className="border-b border-slate-100">
                    <th className="text-left py-2 px-3 text-xs font-semibold text-slate-500 uppercase tracking-wider w-1/3">
                      이름
                    </th>
                    <th className="text-left py-2 px-3 text-xs font-semibold text-slate-500 uppercase tracking-wider">
                      포지션
                    </th>
                    <th className="text-left py-2 px-3 text-xs font-semibold text-slate-500 uppercase tracking-wider">
                      권한
                    </th>
                    <th className="text-center py-2 px-3 text-xs font-semibold text-slate-500 uppercase tracking-wider">
                      방출
                    </th>
                    <th className="text-center py-2 px-3 text-xs font-semibold text-slate-500 uppercase tracking-wider">
                      리뷰
                    </th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-50">
                  {displayMembers.map((member) => (
                    <tr
                      key={member.id}
                      className="hover:bg-slate-50 transition-colors"
                    >
                      <td className="py-3 px-3">
                        <div className="flex items-center gap-3">
                          <img
                            src={member.avatar}
                            alt={member.name}
                            className="w-8 h-8 rounded-full border border-slate-200 object-cover"
                          />
                          <Link
                            href={`/portfolio/${member.id}`}
                            className="font-medium text-slate-900 hover:text-blue-600 transition-colors"
                          >
                            {member.name}
                          </Link>
                        </div>
                      </td>
                      <td className="py-3 px-3 text-slate-600">
                        {positionMap[member.position] ?? member.position}
                      </td>
                      <td className="py-3 px-3">
                        {member.isLeader ? (
                          <Badge variant="purple">리더</Badge>
                        ) : isLeader ? (
                          <select
                            defaultValue={member.role}
                            className="text-xs rounded-md border border-slate-200 bg-white px-2 py-1 text-slate-700 focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
                            onChange={(e) =>
                              handleRoleChange(
                                member.id,
                                member.name,
                                e.target.value,
                              )
                            }
                            disabled={roleChangingId === member.id}
                          >
                            <option value="MANAGER">매니저</option>
                            <option value="MEMBER">멤버</option>
                          </select>
                        ) : (
                          <Badge
                            variant={
                              member.role === 'MANAGER'
                                ? 'success'
                                : 'secondary'
                            }
                          >
                            {roleMap[member.role] ?? member.role}
                          </Badge>
                        )}
                      </td>
                      <td className="py-3 px-3 text-center">
                        {!member.isLeader && isLeader && (
                          <button
                            className="inline-flex items-center gap-1 text-xs text-red-500 hover:text-red-700 hover:bg-red-50 px-2 py-1 rounded-md transition-colors disabled:opacity-50"
                            disabled={kickingId === member.id}
                            onClick={() => handleKick(member.id, member.name)}
                          >
                            <UserX className="h-3.5 w-3.5" />
                            {kickingId === member.id ? '처리중...' : '방출'}
                          </button>
                        )}
                      </td>
                      <td className="py-3 px-3 text-center">
                        {isCompleted &&
                        String(authUser?.memberId) !== member.id ? (
                          <Link
                            href={`/projects/${id}/review/${member.id}`}
                            className="inline-flex items-center gap-1 text-xs text-blue-600 hover:text-blue-800 hover:bg-blue-50 px-2 py-1 rounded-md transition-colors"
                          >
                            <Star className="h-3.5 w-3.5" />
                            리뷰
                          </Link>
                        ) : (
                          <span className="text-xs text-slate-300">
                            완료 후 작성
                          </span>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : (
            <p className="text-sm text-slate-500 text-center py-6">
              아직 팀원이 없습니다.
            </p>
          )}
        </Card>

        {/* 지원자 목록 */}
        {isRecruiting && isLeader && (
          <Card className="p-6">
            <div className="flex items-center justify-between mb-5">
              <h2 className="text-base font-semibold text-slate-900 flex items-center gap-2">
                <Users className="h-5 w-5 text-emerald-600" /> 지원자 목록
                <span className="ml-1 bg-emerald-100 text-emerald-700 text-xs font-semibold px-2 py-0.5 rounded-full">
                  {applicants.length}
                </span>
              </h2>
              <button
                className="inline-flex items-center gap-1.5 text-xs text-slate-500 hover:text-slate-900 transition-colors disabled:opacity-50"
                disabled={applicantsRefreshing}
                onClick={() => {
                  setApplicantsRefreshing(true)
                  fetchApplicant(id)
                    .then((res) =>
                      setApplicants((res as any).Applicant ?? res ?? []),
                    )
                    .catch(() => {})
                    .finally(() => setApplicantsRefreshing(false))
                }}
              >
                <RefreshCw
                  className={`h-3.5 w-3.5 ${applicantsRefreshing ? 'animate-spin' : ''}`}
                />
                새로고침
              </button>
            </div>

            {applicants.length > 0 ? (
              <div className="space-y-4">
                {applicants.map((applicant) => (
                  <div
                    key={applicant.id}
                    className="border border-slate-200 rounded-xl p-5 hover:border-blue-200 hover:bg-blue-50/30 transition-colors"
                  >
                    <div className="flex items-start gap-4">
                      <img
                        src={applicant.profileImageUrl}
                        alt={applicant.nickname}
                        className="w-12 h-12 rounded-full border-2 border-white shadow-sm object-cover shrink-0"
                      />
                      <div className="flex-1 min-w-0">
                        <div className="flex items-center gap-2 mb-1">
                          <span className="font-semibold text-slate-900">
                            {applicant.nickname}
                          </span>
                          {applicant.position && (
                            <span className="text-xs text-slate-500 bg-slate-100 px-2 py-0.5 rounded-full">
                              {positionMap[applicant.position] ?? applicant.position}
                            </span>
                          )}
                        </div>
                        {applicant.techStacks?.length > 0 && (
                          <div className="flex flex-wrap gap-1 mb-2">
                            {applicant.techStacks.map((tech) => (
                              <span
                                key={tech}
                                className="text-xs bg-blue-50 text-blue-700 px-2 py-0.5 rounded-md font-medium"
                              >
                                {tech}
                              </span>
                            ))}
                          </div>
                        )}
                        {applicant.message && (
                          <p className="text-sm text-slate-600 line-clamp-2 leading-relaxed">
                            {applicant.message}
                          </p>
                        )}
                      </div>
                      <div className="flex flex-col gap-2 shrink-0">
                        <Link
                          href={`/portfolio/${applicant.id}`}
                          className="inline-flex items-center gap-1 text-xs text-blue-600 hover:text-blue-800 px-3 py-1.5 border border-blue-200 rounded-lg hover:bg-blue-50 transition-colors"
                        >
                          <ExternalLink className="h-3.5 w-3.5" />
                          포트폴리오
                        </Link>
                        <button
                          className="px-3 py-1.5 text-xs font-medium bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors disabled:opacity-50"
                          disabled={approvingId === applicant.id}
                          onClick={() => handleApprove(applicant.id)}
                        >
                          {approvingId === applicant.id ? '처리중...' : '승인'}
                        </button>
                        <button
                          className="px-3 py-1.5 text-xs font-medium border border-slate-200 text-slate-600 rounded-lg hover:bg-slate-100 transition-colors disabled:opacity-50"
                          disabled={rejectingId === applicant.id}
                          onClick={() => handleReject(applicant.id, applicant.nickname)}
                        >
                          {rejectingId === applicant.id ? '처리중...' : '거절'}
                        </button>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            ) : (
              <p className="text-sm text-slate-500 text-center py-8">
                아직 지원자가 없습니다.
              </p>
            )}
          </Card>
        )}

        {/* 프로젝트 정보 */}
        <Card className="p-6">
          <h2 className="text-base font-semibold text-slate-900 mb-4 flex items-center gap-2">
            <Code2 className="h-5 w-5 text-slate-400" /> 프로젝트 정보
          </h2>
          <div className="grid grid-cols-2 sm:grid-cols-4 gap-4 text-sm">
            <div className="flex flex-col items-center justify-center py-3 border border-slate-100 rounded-lg bg-slate-50">
              <span className="text-xs text-slate-500 mb-1">모집구분</span>
              <span className="font-semibold text-slate-900">
                {categoryMap[project.category] ?? project.category}
              </span>
            </div>
            <div className="flex flex-col items-center justify-center py-3 border border-slate-100 rounded-lg bg-slate-50">
              <span className="text-xs text-slate-500 mb-1">모집인원</span>
              <span className="font-semibold text-slate-900">
                {project.positions.reduce((s, p) => s + p.total, 0)}명
              </span>
            </div>
            <div className="flex flex-col items-center justify-center py-3 border border-slate-100 rounded-lg bg-slate-50">
              <span className="text-xs text-slate-500 mb-1">등록일</span>
              <span className="font-semibold text-slate-900">
                {new Date(project.createdAt).toLocaleDateString('ko-KR')}
              </span>
            </div>
            <div className="flex flex-col items-center justify-center py-3 border border-slate-100 rounded-lg bg-slate-50">
              <span className="text-xs text-slate-500 mb-1">모집 마감일</span>
              <span className="font-semibold text-slate-900">
                {new Date(project.deadline).toLocaleDateString('ko-KR')}
              </span>
            </div>
          </div>

          {project.techStack.length > 0 && (
            <div className="mt-4 pt-4 border-t border-slate-100">
              <p className="text-xs text-slate-500 mb-2">기술 스택</p>
              <div className="flex flex-wrap gap-2">
                {project.techStack.map((tech) => (
                  <span
                    key={tech}
                    className="px-2.5 py-1 bg-slate-100 text-slate-700 text-xs font-medium rounded-md"
                  >
                    {tech}
                  </span>
                ))}
              </div>
            </div>
          )}
        </Card>
      </div>
    </div>
  )
}
