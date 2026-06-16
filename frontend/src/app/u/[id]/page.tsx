'use client'

import { useEffect, useState } from 'react'
import { toast } from 'sonner'

import Link from 'next/link'
import { useParams } from 'next/navigation'

import { Calendar, Code2, Github, Globe, MapPin } from 'lucide-react'

import { LoginModal } from '../../../components/LoginModal'
import { Badge, Button, Card, Modal } from '../../../components/ui'
import {
  createProjectProposal,
  fetchMember,
  fetchProjects,
  fetchProposalProjects,
} from '../../../lib/api'
import type { Project, User } from '../../../types'
import type { ProposalProject } from '../../../types/dto/proposal'
import { useAuth } from '../../providers'

const statusMap: Record<string, string> = {
  Open: '모집중',
  Closed: '마감',
}

const categoryMap: Record<string, string> = {
  Web: '웹',
  Mobile: '모바일',
  AI: 'AI',
  Game: '게임',
  Other: '기타',
}

export default function DeveloperProfilePage() {
  const params = useParams()
  const id = params.id as string
  const { user: authUser, loading: authLoading } = useAuth()
  const isMyProfile = authUser !== null && String(authUser.memberId) === id
  const [user, setUser] = useState<User | null>(null)
  const [projects, setProjects] = useState<Project[]>([])
  const [loading, setLoading] = useState(true)
  const [isProposalModalOpen, setIsProposalModalOpen] = useState(false)
  const [isLoginModalOpen, setIsLoginModalOpen] = useState(false)
  const [proposalProjects, setProposalProjects] = useState<ProposalProject[]>(
    [],
  )
  const [selectedProjectId, setSelectedProjectId] = useState('')
  const [proposalMessage, setProposalMessage] = useState('')
  const [proposalLoading, setProposalLoading] = useState(false)
  const [proposalSubmitting, setProposalSubmitting] = useState(false)

  useEffect(() => {
    if (!id) {
      setLoading(false)
      return
    }

    Promise.all([fetchMember(id), fetchProjects()])
      .then(([member, projectData]) => {
        setUser(member)
        setProjects(projectData)
      })
      .catch(() => {
        setUser(null)
        setProjects([])
      })
      .finally(() => setLoading(false))
  }, [id])

  const createdProjects = projects.filter((p) => p.leader.id === id)
  const participatedProjects = projects.filter((p) =>
    p.teamMembers.some((m) => m.id === id),
  )

  const handleOpenProposal = async () => {
    if (authLoading) return
    if (!authUser) {
      setIsLoginModalOpen(true)
      return
    }

    setIsProposalModalOpen(true)
    setProposalLoading(true)
    try {
      const proposalProjectData = await fetchProposalProjects()
      setProposalProjects(proposalProjectData)
      setSelectedProjectId(
        proposalProjectData.length > 0 ? String(proposalProjectData[0].id) : '',
      )
    } catch (error) {
      setProposalProjects([])
      toast.error(
        error instanceof Error
          ? error.message
          : '제안 가능한 프로젝트를 불러오지 못했습니다.',
      )
    } finally {
      setProposalLoading(false)
    }
  }

  const handleSubmitProposal = async (event: React.FormEvent) => {
    event.preventDefault()
    if (!selectedProjectId || !proposalMessage.trim() || proposalSubmitting) {
      return
    }

    setProposalSubmitting(true)
    try {
      await createProjectProposal(id, {
        projectId: Number(selectedProjectId),
        message: proposalMessage.trim(),
      })
      toast.success('프로젝트 제안을 보냈습니다.')
      setIsProposalModalOpen(false)
      setProposalMessage('')
    } catch (error) {
      toast.error(
        error instanceof Error
          ? error.message
          : '프로젝트 제안을 보내지 못했습니다.',
      )
    } finally {
      setProposalSubmitting(false)
    }
  }

  if (loading) {
    return (
      <div className="container mx-auto px-4 py-20 text-center text-slate-500">
        프로필을 불러오는 중...
      </div>
    )
  }

  if (!user) {
    return (
      <div className="container mx-auto px-4 py-20 text-center">
        <h2 className="text-2xl font-bold text-slate-900">
          사용자를 찾을 수 없어요
        </h2>
        <Link
          href="/projects"
          className="text-blue-600 hover:underline mt-4 inline-block"
        >
          프로젝트 목록으로
        </Link>
      </div>
    )
  }

  return (
    <div className="container mx-auto px-4 py-12 max-w-5xl">
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* Left Column: Profile Info */}
        <div className="lg:col-span-1 space-y-6">
          <Card className="p-6 text-center">
            <div className="relative inline-block mb-4">
              <img
                src={user.avatar}
                alt={user.name}
                className="w-32 h-32 rounded-full object-cover border-4 border-white shadow-lg mx-auto"
              />
              <div
                className="absolute bottom-2 right-2 w-4 h-4 bg-green-500 border-2 border-white rounded-full"
                title="프로젝트 참여 가능"
              ></div>
            </div>
            <h1 className="text-2xl font-bold text-slate-900">{user.name}</h1>
            <p className="text-blue-600 font-medium mb-4">{user.role}</p>

            <div className="flex justify-center gap-3 mb-6">
              {isMyProfile ? (
                <Link href="/mypage/portfolio/edit" className="w-full">
                  <Button variant="gradient" className="w-full">
                    포트폴리오 수정
                  </Button>
                </Link>
              ) : (
                <Button
                  variant="gradient"
                  className="w-full"
                  onClick={handleOpenProposal}
                  disabled={authLoading}
                >
                  프로젝트 제안하기
                </Button>
              )}
            </div>

            <div className="space-y-3 text-sm text-slate-600 text-left border-t border-slate-100 pt-6">
              {user.location && (
                <div className="flex items-center gap-3">
                  <MapPin className="w-4 h-4 text-slate-400" />
                  <span>{user.location}</span>
                </div>
              )}
              {user.github && (
                <div className="flex items-center gap-3">
                  <Github className="w-4 h-4 text-slate-400" />
                  <a
                    href={user.github}
                    target="_blank"
                    rel="noreferrer"
                    className="hover:text-blue-600 transition-colors"
                  >
                    {user.github.replace('https://github.com/', '')}
                  </a>
                </div>
              )}
              {user.portfolio && (
                <div className="flex items-center gap-3">
                  <Globe className="w-4 h-4 text-slate-400" />
                  <a
                    href={user.portfolio}
                    target="_blank"
                    rel="noreferrer"
                    className="hover:text-blue-600 transition-colors"
                  >
                    포트폴리오 사이트
                  </a>
                </div>
              )}
              <div className="flex items-center gap-3">
                <Calendar className="w-4 h-4 text-slate-400" />
                <span>2026년 6월 가입</span>
              </div>
            </div>
          </Card>

          <Card className="p-6">
            <h3 className="font-semibold text-slate-900 mb-4 flex items-center gap-2">
              <Code2 className="w-5 h-5 text-blue-600" />
              기술 스택
            </h3>
            <div className="flex flex-wrap gap-2">
              {user.techStack?.map((tech) => (
                <Badge key={tech} variant="secondary">
                  {tech}
                </Badge>
              ))}
              {(!user.techStack || user.techStack.length === 0) && (
                <span className="text-sm text-slate-500">
                  등록된 기술 스택이 없어요.
                </span>
              )}
            </div>
          </Card>
        </div>

        {/* Right Column: Activity & Projects */}
        <div className="lg:col-span-2 space-y-8">
          {/* About */}
          <section>
            <h2 className="text-xl font-bold text-slate-900 mb-4">소개</h2>
            <div className="bg-white rounded-2xl border border-slate-200 p-6 text-slate-600 leading-relaxed">
              {user.bio || '아직 소개글을 작성하지 않았어요.'}
            </div>
          </section>

          {/* Created Projects */}
          <section>
            <div className="flex items-center justify-between mb-4">
              <h2 className="text-xl font-bold text-slate-900">
                만든 프로젝트
              </h2>
              <Badge variant="default">{createdProjects.length}</Badge>
            </div>
            {createdProjects.length > 0 ? (
              <div className="grid gap-4">
                {createdProjects.map((project) => (
                  <Link key={project.id} href={`/projects/${project.id}`}>
                    <Card className="p-5 hover:border-blue-300 transition-colors cursor-pointer group">
                      <div className="flex justify-between items-start mb-2">
                        <h3 className="font-semibold text-lg text-slate-900 group-hover:text-blue-600 transition-colors">
                          {project.title}
                        </h3>
                        <Badge
                          variant={
                            project.recruitmentStatus === 'Open'
                              ? 'default'
                              : 'secondary'
                          }
                        >
                          {statusMap[project.recruitmentStatus]}
                        </Badge>
                      </div>
                      <p className="text-slate-600 text-sm mb-4 line-clamp-2">
                        {project.description}
                      </p>
                      <div className="flex items-center gap-4 text-sm text-slate-500">
                        <span className="flex items-center gap-1">
                          <Code2 className="w-4 h-4" />
                          {categoryMap[project.category]}
                        </span>
                        <span>•</span>
                        <span>{project.techStack.slice(0, 3).join(', ')}</span>
                      </div>
                    </Card>
                  </Link>
                ))}
              </div>
            ) : (
              <div className="bg-slate-50 rounded-2xl border border-slate-200 border-dashed p-8 text-center text-slate-500">
                아직 만든 프로젝트가 없어요.
              </div>
            )}
          </section>

          {/* Participated Projects */}
          <section>
            <div className="flex items-center justify-between mb-4">
              <h2 className="text-xl font-bold text-slate-900">
                참여한 프로젝트
              </h2>
              <Badge variant="secondary">{participatedProjects.length}</Badge>
            </div>
            {participatedProjects.length > 0 ? (
              <div className="grid gap-4">
                {participatedProjects.map((project) => (
                  <Link key={project.id} href={`/projects/${project.id}`}>
                    <Card className="p-5 hover:border-blue-300 transition-colors cursor-pointer group">
                      <div className="flex justify-between items-start mb-2">
                        <h3 className="font-semibold text-lg text-slate-900 group-hover:text-blue-600 transition-colors">
                          {project.title}
                        </h3>
                      </div>
                      <p className="text-slate-600 text-sm mb-4 line-clamp-2">
                        {project.description}
                      </p>
                      <div className="flex items-center gap-2">
                        <img
                          src={project.leader.avatar}
                          alt={project.leader.name}
                          className="w-6 h-6 rounded-full"
                        />
                        <span className="text-sm text-slate-600">
                          {project.leader.name} 리더
                        </span>
                      </div>
                    </Card>
                  </Link>
                ))}
              </div>
            ) : (
              <div className="bg-slate-50 rounded-2xl border border-slate-200 border-dashed p-8 text-center text-slate-500">
                아직 참여한 프로젝트가 없어요.
              </div>
            )}
          </section>
        </div>
      </div>

      <Modal
        isOpen={isProposalModalOpen}
        onClose={() => setIsProposalModalOpen(false)}
        title={`${user.name}님에게 프로젝트 제안`}
      >
        {proposalLoading ? (
          <div className="py-10 text-center text-sm text-slate-500">
            제안 가능한 프로젝트를 불러오는 중...
          </div>
        ) : proposalProjects.length === 0 ? (
          <div className="space-y-4 py-4 text-center">
            <p className="text-sm text-slate-600">
              제안할 수 있는 모집 중 프로젝트가 없습니다.
            </p>
            <p className="text-xs text-slate-500">
              리더 또는 매니저로 참여 중인 모집 프로젝트가 필요합니다.
            </p>
            <Button
              type="button"
              variant="outline"
              onClick={() => setIsProposalModalOpen(false)}
            >
              확인
            </Button>
          </div>
        ) : (
          <form onSubmit={handleSubmitProposal} className="space-y-5">
            <div>
              <label className="mb-2 block text-sm font-medium text-slate-700">
                제안할 프로젝트 <span className="text-red-500">*</span>
              </label>
              <select
                value={selectedProjectId}
                onChange={(event) => setSelectedProjectId(event.target.value)}
                className="h-10 w-full rounded-lg border border-slate-200 bg-white px-3 text-sm focus:border-blue-600 focus:outline-none focus:ring-1 focus:ring-blue-600"
                required
              >
                {proposalProjects.map((project) => (
                  <option key={project.id} value={project.id}>
                    {project.title}
                  </option>
                ))}
              </select>
            </div>

            <div>
              <label className="mb-2 block text-sm font-medium text-slate-700">
                제안 메시지 <span className="text-red-500">*</span>
              </label>
              <textarea
                value={proposalMessage}
                onChange={(event) => setProposalMessage(event.target.value)}
                className="min-h-32 w-full resize-y rounded-lg border border-slate-200 bg-white px-3 py-2 text-sm focus:border-blue-600 focus:outline-none focus:ring-1 focus:ring-blue-600"
                placeholder="프로젝트 소개와 함께 합류를 제안하는 이유를 작성해주세요."
                required
              />
            </div>

            <div className="flex justify-end gap-3 pt-2">
              <Button
                type="button"
                variant="ghost"
                onClick={() => setIsProposalModalOpen(false)}
              >
                취소
              </Button>
              <Button
                type="submit"
                variant="gradient"
                disabled={proposalSubmitting || !proposalMessage.trim()}
              >
                {proposalSubmitting ? '제안 보내는 중...' : '제안 보내기'}
              </Button>
            </div>
          </form>
        )}
      </Modal>

      {isLoginModalOpen && (
        <LoginModal onClose={() => setIsLoginModalOpen(false)} />
      )}
    </div>
  )
}
