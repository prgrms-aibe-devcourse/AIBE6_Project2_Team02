'use client'

import React, { useEffect, useState } from 'react'
import { toast } from 'sonner'

import Link from 'next/link'
import { useParams, useRouter } from 'next/navigation'

import {
  ArrowLeft,
  BookmarkPlus,
  Calendar,
  CheckCircle2,
  Code2,
  Pencil,
  Share2,
  Target,
  Users,
} from 'lucide-react'

import { Badge, Button, Card, Modal } from '../../../../components/ui'
import {
  fetchApplicant,
  fetchApplicantTOteam,
  fetchChangeStatus,
  fetchProjectPermissions,
  fetchProject_manage,
} from '../../../../lib/api'
import type { Applicant, Project_manage } from '../../../../types'

const categoryMap: Record<string, string> = {
  Web: '웹',
  Mobile: '모바일',
  AI: 'AI',
  Game: '게임',
  Other: '기타',
}

const statusMap: Record<string, string> = {
  Open: '모집중',
  Closed: '마감',
}

export default function ProjectDetailPage() {
  const params = useParams()
  const id = params?.id as string
  const router = useRouter()
  const [project, setProject] = useState<Project_manage | null>(null)
  const [member2, setMember2] = useState<Applicant[]>([])
  const [loading, setLoading] = useState(true)
  const [isApplyModalOpen, setIsApplyModalOpen] = useState(false)
  const [selectedRole, setSelectedRole] = useState<string>('')
  const [canEdit, setCanEdit] = useState(false)
  const [isMember, setIsMember] = useState(false)

  useEffect(() => {
    if (!id) {
      setLoading(false)
      return
    }

    fetchProject_manage(id)
      .then((res) => setProject(res))
      .catch(() => setProject(null))
      .finally(() => setLoading(false))
  }, [id, member2])

  useEffect(() => {
    if (!id) {
      setLoading(false)
      return
    }
    fetchApplicant(id)
      .then((res) => setMember2(res.Applicant))
      .catch(() => setMember2([]))
      .finally(() => setLoading(false))
  }, [id])

  useEffect(() => {
    if (!id) return

    fetchProjectPermissions(id)
      .then((permission) => {
        setCanEdit(permission.canEdit)
        setIsMember(permission.isMember)
      })
      .catch(() => {
        setCanEdit(false)
        setIsMember(false)
      })
  }, [id])

  if (loading) {
    return (
      <div className="container mx-auto px-4 py-20 text-center text-slate-500">
        프로젝트를 불러오는 중...
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

  const handleApply = (e: React.FormEvent) => {
    e.preventDefault()
    setIsApplyModalOpen(false)
    toast.success('지원이 완료되었습니다!', {
      description: `${project.title}의 ${selectedRole} 포지션에 지원했습니다. 프로젝트 리더가 프로필을 검토할 예정입니다.`,
    })
  }
  const cantoTeam = (ap_id: string, id: string) => {
    fetchApplicantTOteam(ap_id, id)
      .then((res) => setMember2(res.Applicant))
      .catch(() => setMember2([]))
      .finally(() => setLoading(false))
  }

  const disAgree = (ap_id: string) => {
    /* fetch(ap_id)
      .then((res) => setMember2(res.Applicant))
      .catch(() => setMember2([]))
      .finally(() => setLoading(false))*/
  }

  const handleStatus = (e: React.FormEvent) => {
    e.preventDefault()
    console.log(e.target.project_staus.value)
    fetchChangeStatus(e.target.project_staus.value, id)
      .then((res) => setProject(res))
      .catch(() => setProject(null))
      .finally(() => setLoading(false))
  }

  return (
    <div className="bg-slate-50 min-h-screen pb-20">
      {/* Header Banner */}
      <div className="bg-white border-b border-slate-200 pt-8 pb-12">
        <div className="container mx-auto px-4 max-w-5xl">
          <Link
            href="/projects"
            className="inline-flex items-center text-sm text-slate-500 hover:text-slate-900 mb-6 transition-colors"
          >
            <ArrowLeft className="h-4 w-4 mr-1" /> 프로젝트 목록으로
          </Link>

          <div className="flex flex-col md:flex-row md:items-start justify-between gap-6">
            <div className="flex-1">
              <div className="flex flex-wrap gap-2 mb-4">
                <Badge
                  variant={
                    project.recruitmentStatus === 'Open'
                      ? 'success'
                      : 'secondary'
                  }
                >
                  {statusMap[project.recruitmentStatus]}
                </Badge>
                <Badge variant="purple">{categoryMap[project.category]}</Badge>
              </div>
              <h1 className="text-3xl md:text-4xl font-bold text-slate-900 mb-4">
                {project.title}
              </h1>
              <p className="text-lg text-slate-600 max-w-3xl">
                {project.description}
              </p>
            </div>

            <div className="flex flex-row md:flex-col gap-3 shrink-0">
              {canEdit && (
                <Button
                  size="lg"
                  variant="outline"
                  className="w-full md:w-48 gap-2"
                  onClick={() => router.push(`/projects/${id}/edit`)}
                >
                  <Pencil className="h-4 w-4" />
                  프로젝트 수정
                </Button>
              )}
              <Button
                size="lg"
                variant="gradient"
                className={`w-full md:w-48 ${canEdit ? 'hidden' : ''}`}
                disabled={isMember || project.recruitmentStatus === 'Closed'}
                onClick={() => setIsApplyModalOpen(true)}
              >
                지원하기
              </Button>
              <div className="flex gap-2">
                <Button
                  size="icon"
                  variant="outline"
                  className="flex-1 md:flex-none"
                  onClick={() => toast('링크가 복사되었습니다')}
                >
                  <Share2 className="h-4 w-4" />
                </Button>
                <Button
                  size="icon"
                  variant="outline"
                  className="flex-1 md:flex-none"
                  onClick={() => toast.success('프로젝트를 저장했습니다')}
                >
                  <BookmarkPlus className="h-4 w-4" />
                </Button>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Content Grid */}
      <div className="container mx-auto px-4 max-w-5xl mt-8">
        <div className="inline-block grid-cols-1 lg:grid-cols-3 gap-8">
          {/* Main Column */}
          <div className="lg:col-span-2 space-y-8">
            <Card className="p-8">
              <h2 className="text-xl font-bold text-slate-900 mb-4 flex items-center gap-2">
                <Target className="h-5 w-5 text-blue-600" /> 프로젝트 소개
              </h2>
              <div className="prose prose-slate max-w-none">
                <p className="text-slate-600 leading-relaxed whitespace-pre-line">
                  {project.fullDescription}
                </p>
              </div>
              <div className="flex flex-wrap gap-2 mb-4">
                <form onSubmit={handleStatus}>
                  <h3 className="text-lg font-bold text-slate-900 mt-8 mb-4">
                    프로젝트 상태변화
                  </h3>
                  <select name="project_staus" id="project_staus">
                    <option value="RECRUITING">RECRUITING</option>
                    <option value="IN_PROGRESS">IN_PROGRESS</option>
                    <option value="COMPLETED">COMPLETED</option>
                    <option value="DISBANDED">DISBANDED</option>
                    <option value="CANCELLED">CANCELLED</option>
                  </select>
                  <button
                    type="submit"
                    className="w-10 h-10 border-radius: 50% m-1 border-2"
                  >
                    확인
                  </button>
                </form>
              </div>
              <h3 className="text-lg font-bold text-slate-900 mt-8 mb-4">
                프로젝트 목표
              </h3>

              <ul className="space-y-3">
                {project.goals.map((goal, i) => (
                  <li key={i} className="flex items-start gap-3 text-slate-600">
                    <CheckCircle2 className="h-5 w-5 text-emerald-500 shrink-0 mt-0.5" />
                    <span>{goal}</span>
                  </li>
                ))}
              </ul>
            </Card>

            <Card className="p-8">
              <h2 className="text-xl font-bold text-slate-900 mb-6 flex items-center gap-2">
                <Users className="h-5 w-5 text-blue-600" /> 모집 포지션
              </h2>
              <div className="space-y-4">
                {project.positions.map((pos, i) => {
                  const isOpen = pos.filled < pos.total
                  return (
                    <div
                      key={i}
                      className="flex items-center justify-between p-4 rounded-xl border border-slate-100 bg-slate-50/50"
                    >
                      <div>
                        <h4 className="font-semibold text-slate-900">
                          {pos.role}
                        </h4>
                        <p className="text-sm text-slate-500 mt-1">
                          {pos.total}명 중 {pos.filled}명 모집 완료
                        </p>
                      </div>
                      {isOpen ? (
                        <Button
                          variant="outline"
                          size="sm"
                          className={canEdit ? 'hidden' : undefined}
                          disabled={isMember}
                          onClick={() => {
                            setSelectedRole(pos.role)
                            setIsApplyModalOpen(true)
                          }}
                        >
                          지원
                        </Button>
                      ) : (
                        <Badge variant="secondary">모집 완료</Badge>
                      )}
                    </div>
                  )
                })}
                {project.positions.length === 0 && (
                  <p className="py-6 text-center text-sm text-slate-500">
                    등록된 모집 포지션이 없습니다.
                  </p>
                )}
              </div>
            </Card>

            <Card className="p-8">
              <h3 className="font-bold text-slate-900 mb-4 flex items-center gap-2">
                <Code2 className="h-4 w-4 text-slate-400" /> 기술 스택
              </h3>

              {project.techStack.map((tech) => (
                <span
                  key={tech}
                  className="px-3 py-1.5 bg-slate-100 text-slate-700 text-sm font-medium rounded-lg"
                >
                  {tech}
                </span>
              ))}
            </Card>

            <Card className="p-8">
              <h3 className="font-bold text-slate-900 mb-4 flex items-center gap-2">
                <Calendar className="h-4 w-4 text-slate-400" /> 일정
              </h3>
              <div className="space-y-3 text-sm">
                <div className="flex justify-between py-2 border-b border-slate-100">
                  <span className="text-slate-500">등록일</span>
                  <span className="font-medium text-slate-900">
                    {new Date(project.createdAt).toLocaleDateString()}
                  </span>
                </div>
                <div className="flex justify-between py-2">
                  <span className="text-slate-500">마감일</span>
                  <span className="font-medium text-slate-900">
                    {new Date(project.deadline).toLocaleDateString()}
                  </span>
                </div>
              </div>
            </Card>

            <Card className="p-8">
              <h3 className="font-bold text-slate-900 mb-4">팀</h3>

              <div className="mb-6">
                <p className="text-xs font-semibold text-slate-500 uppercase tracking-wider mb-3">
                  프로젝트 리더
                </p>
                <div className="flex items-center gap-4">
                  <Link
                    href={`/u/${project.leader.id}`}
                    className="hover:opacity-80 transition-opacity"
                  >
                    <img
                      src={project.leader.avatar}
                      alt={project.leader.name}
                      className="w-12 h-12 rounded-full border-2 border-white shadow-sm"
                    />
                  </Link>
                  <div className="flex items-center gap-4">
                    <Link
                      href={`/u/${project.leader.id}`}
                      className="font-medium text-slate-900 hover:text-blue-600 transition-colors"
                    >
                      {project.leader.name}
                    </Link>
                    <p className="text-sm text-slate-500">
                      {project.leader.role}
                    </p>
                  </div>
                </div>
              </div>
              <div>
                <h3 className="font-medium text-slate-900 mb-4">팀원</h3>
                <div className="inline-block gap-3">
                  {project.pmResponses.map((member) => (
                    <div className="flex items-center gap-4" key={member.id}>
                      <Link
                        href={`/u/${member.id}`}
                        className="hover:opacity-80 transition-opacity"
                      >
                        <img
                          src={member.avatar}
                          alt={member.name}
                          title={member.name}
                          className="w-10 h-10 rounded-full border-2 border-white shadow-sm"
                        />
                      </Link>
                      <div>{member.name}</div>
                      <p className="text-sm text-slate-500">{member.role}</p>
                      <p className="text-sm text-slate-500">
                        {member.position}
                      </p>
                      <button className="w-20 h-10 border-radius: 50% m-1 border-2">
                        리뷰쓰기
                      </button>
                    </div>
                  ))}
                  {project.teamMembers.length === 0 && (
                    <span className="text-sm text-slate-500 py-2">
                      아직 팀원이 없어요. 첫 번째 팀원이 되어보세요!
                    </span>
                  )}
                </div>
              </div>
            </Card>
            <Card className="p-8">
              <div className="inline-block gap-3">
                <h3 className="font-medium text-slate-900 mb-4">지원자</h3>
                <div className="inline-block gap-3">
                  {member2.map((applicantlist) => (
                    <div
                      className="flex items-center gap-4"
                      key={applicantlist.id}
                    >
                      <div>
                        <Link
                          href={`/u/${applicantlist.id}`}
                          className="hover:opacity-80 transition-opacity"
                        >
                          <img
                            src={applicantlist.profileImageUrl}
                            alt={applicantlist.nickname}
                            title={applicantlist.nickname}
                            className="w-10 h-10 rounded-full border-2 border-white shadow-sm"
                          />
                        </Link>
                      </div>
                      <div>{applicantlist.nickname}</div>
                      <div>
                        <button
                          className="w-10 h-10 border-radius: 50% m-1 border-2"
                          onClick={() =>
                            cantoTeam(applicantlist.id, project.id)
                          }
                        >
                          승인
                        </button>
                        <button
                          className="w-10 h-10 border-radius: 50% m-1 border-2"
                          onClick={() => disAgree(applicantlist.id)}
                        >
                          거절
                        </button>
                      </div>
                    </div>
                  ))}
                  {project.teamMembers.length === 0 && (
                    <span className="text-sm text-slate-500 py-2">
                      아직 팀원이 없어요. 첫 번째 팀원이 되어보세요!
                    </span>
                  )}
                </div>
              </div>
            </Card>
          </div>
        </div>
      </div>

      {/* Apply Modal */}
      <Modal
        isOpen={isApplyModalOpen}
        onClose={() => setIsApplyModalOpen(false)}
        title={`${project.title} 지원하기`}
      >
        <form onSubmit={handleApply} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-slate-700 mb-1">
              포지션 선택
            </label>
            <select
              className="w-full rounded-lg border border-slate-200 bg-white px-3 py-2 text-sm focus:border-blue-600 focus:outline-none focus:ring-1 focus:ring-blue-600"
              value={selectedRole}
              onChange={(e) => setSelectedRole(e.target.value)}
              required
            >
              <option value="" disabled>
                포지션을 선택하세요...
              </option>
              {project.positions
                .filter((p) => p.filled < p.total)
                .map((p) => (
                  <option key={p.role} value={p.role}>
                    {p.role}
                  </option>
                ))}
            </select>
          </div>
          <div>
            <label className="block text-sm font-medium text-slate-700 mb-1">
              왜 이 프로젝트에 적합한가요?
            </label>
            <textarea
              className="w-full rounded-lg border border-slate-200 bg-white px-3 py-2 text-sm focus:border-blue-600 focus:outline-none focus:ring-1 focus:ring-blue-600 min-h-[100px] resize-y"
              placeholder="관련 경험과 이 프로젝트에 참여하고 싶은 이유를 간단히 적어주세요..."
              required
            />
          </div>
          <div className="bg-blue-50 text-blue-800 text-sm p-3 rounded-lg flex gap-2">
            <CheckCircle2 className="h-5 w-5 shrink-0 text-blue-600" />
            <p>
              지원 시 DevLink 프로필과 포트폴리오가 프로젝트 리더에게 함께
              전달됩니다.
            </p>
          </div>
          <div className="pt-4 flex justify-end gap-3">
            <Button
              type="button"
              variant="ghost"
              onClick={() => setIsApplyModalOpen(false)}
            >
              취소
            </Button>
            <Button type="submit" variant="gradient">
              지원서 제출
            </Button>
          </div>
        </form>
      </Modal>
    </div>
  )
}
