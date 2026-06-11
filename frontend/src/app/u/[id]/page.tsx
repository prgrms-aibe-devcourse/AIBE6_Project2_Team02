'use client'

import { motion } from 'framer-motion'
import React from 'react'

import Link from 'next/link'
import { useParams } from 'next/navigation'

import { Calendar, Code2, Github, Globe, Mail, MapPin } from 'lucide-react'

import { Badge, Button, Card } from '../../../components/ui'
import { mockProjects, mockUsers } from '../../../data/mock'

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

  // Fallback to u1 if not found, just for prototype purposes
  const user = id && mockUsers[id] ? mockUsers[id] : mockUsers['u1']

  const createdProjects = mockProjects.filter((p) => p.leader.id === user.id)
  const participatedProjects = mockProjects.filter((p) =>
    p.teamMembers.some((m) => m.id === user.id),
  )

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
              <Button variant="gradient" className="w-full">
                프로젝트 제안하기
              </Button>
              <Button variant="outline" size="icon">
                <Mail className="w-4 h-4" />
              </Button>
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
              <Badge variant="primary" className="bg-blue-100 text-blue-700">
                {createdProjects.length}
              </Badge>
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
                              ? 'primary'
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
    </div>
  )
}
