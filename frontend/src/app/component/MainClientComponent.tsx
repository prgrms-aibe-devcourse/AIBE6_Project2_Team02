'use client'

import { motion } from 'framer-motion'
import { useEffect, useState } from 'react'

import Link from 'next/link'
import { useRouter, useSearchParams } from 'next/navigation'

import {
  ArrowLeft,
  ArrowRight,
  Ban,
  Clock,
  Code,
  Rocket,
  Users,
} from 'lucide-react'

import { Badge, Button, Card } from '../../components/ui'
import { fetchPopularTechStacks, fetchProjects } from '../../lib/api'
import type { Project } from '../../types'

const statusMap: Record<string, string> = {
  All: '전체',
  Open: '모집중',
  Closed: '마감',
  Completed: '완료',
  Stopped: '중단',
}
const categoryMap: Record<string, string> = {
  Web: '웹',
  Mobile: '모바일',
  AI: 'AI',
  Game: '게임',
  Other: '기타',
}
const errorMessageMap: Record<string, string> = {
  WITHDRAWN: '탈퇴한 회원입니다.',
  SUSPENDED: '이용이 정지된 계정입니다.',
  BANNED: '영구 정지된 계정입니다.',
}

export default function MainClientComponent() {
  const router = useRouter()
  const searchParams = useSearchParams()
  const [errorMsg, setErrorMsg] = useState<string | null>(
    errorMessageMap[searchParams.get('error') ?? ''] ?? null,
  )
  const [projects, setProjects] = useState<Project[]>([])
  const [popularTechStacks, setPopularTechStacks] = useState<string[]>([])
  const [projectPage, setProjectPage] = useState(0)

  useEffect(() => {
    if (!errorMsg) return
    router.replace('/')
    const timer = setTimeout(() => setErrorMsg(null), 4000)
    return () => clearTimeout(timer)
  }, [errorMsg])

  useEffect(() => {
    Promise.all([fetchProjects(), fetchPopularTechStacks()])
      .then(([projectData, techStacks]) => {
        setProjects(projectData)
        setPopularTechStacks(techStacks)
      })
      .catch(() => {
        setProjects([])
        setPopularTechStacks([])
      })
  }, [])

  const projectsPerPage = 6
  const openProjects = projects.filter(
    (project) => project.recruitmentStatus === 'Open',
  )
  const projectPageCount = Math.ceil(openProjects.length / projectsPerPage)
  const latestProjects = openProjects.slice(
    projectPage * projectsPerPage,
    (projectPage + 1) * projectsPerPage,
  )

  const containerVariants = {
    hidden: { opacity: 0 },
    visible: { opacity: 1, transition: { staggerChildren: 0.1 } },
  }

  return (
    <div className="flex flex-col w-full">
      {errorMsg && (
        <div className="bg-red-50 border-b border-red-200 text-red-700 text-sm py-3 px-4 flex items-center justify-center gap-2">
          <Ban className="w-4 h-4 flex-shrink-0" />
          {errorMsg}
        </div>
      )}
      {/* Hero Section */}
      <section className="relative overflow-hidden bg-white pt-24 pb-32">
        <div className="absolute top-0 left-1/2 -translate-x-1/2 w-full max-w-7xl h-full overflow-hidden pointer-events-none">
          <div className="absolute -top-[20%] -left-[10%] w-[50%] h-[50%] rounded-full bg-blue-100/50 blur-3xl" />
          <div className="absolute top-[20%] -right-[10%] w-[40%] h-[40%] rounded-full bg-purple-100/50 blur-3xl" />
        </div>
        <div className="container relative z-10 mx-auto px-4 text-center">
          <motion.div
            initial={{ opacity: 0, y: 30 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.6 }}
            className="max-w-3xl mx-auto"
          >
            <Badge variant="purple" className="mb-6 px-4 py-1 text-sm">
              🚀 개발자 사이드 프로젝트를 위한 최고의 플랫폼
            </Badge>
            <h1 className="text-5xl md:text-7xl font-extrabold text-slate-900 tracking-tight mb-8 leading-tight break-keep">
              당신의 다음 <br />
              <span className="text-transparent bg-clip-text bg-gradient-to-r from-blue-600 to-purple-600">
                사이드 프로젝트 팀
              </span>
              을 찾아보세요
            </h1>
            <p className="text-xl text-slate-600 mb-10 max-w-2xl mx-auto leading-relaxed break-keep">
              수많은 개발자, 디자이너, 기획자들과 함께 새로운 아이디어를
              실현하세요.
            </p>
            <div className="flex flex-col sm:flex-row items-center justify-center gap-4">
              <Link href="/projects/new">
                <Button
                  size="lg"
                  variant="gradient"
                  className="w-full sm:w-auto group"
                >
                  프로젝트 만들기
                  <ArrowRight className="ml-2 h-4 w-4 group-hover:translate-x-1 transition-transform" />
                </Button>
              </Link>
              <Link href="/projects">
                <Button
                  size="lg"
                  variant="outline"
                  className="w-full sm:w-auto bg-white"
                >
                  프로젝트 찾기
                </Button>
              </Link>
            </div>
          </motion.div>
        </div>
      </section>

      {/* Latest Projects */}
      <section className="py-20 bg-slate-50">
        <div className="container mx-auto px-4">
          <div className="flex items-end justify-between mb-12">
            <div>
              <h2 className="text-3xl font-bold text-slate-900 mb-2">
                최신 프로젝트
              </h2>
              <p className="text-slate-500">
                새롭게 등록된 프로젝트를 확인해보세요.
              </p>
            </div>
            <Link
              href="/projects"
              className="md:flex items-center text-blue-600 font-medium hover:text-blue-700"
            >
              전체 보기 <ArrowRight className="ml-1 h-4 w-4" />
            </Link>
          </div>
          <motion.div
            variants={containerVariants}
            whileInView="visible"
            viewport={{ once: true, margin: '-100px' }}
            className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6"
          >
            {latestProjects.map((project, index) => (
              <motion.div
                key={project.id}
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ delay: index * 0.05 }}
              >
                <Card
                  className="listing-card group flex"
                  onClick={() => router.push(`/projects/${project.id}`)}
                >
                  <div className="flex justify-between items-start mb-4">
                    <div className="flex gap-2">
                      <Badge
                        variant={
                          project.recruitmentStatus === 'Open'
                            ? 'success'
                            : 'secondary'
                        }
                      >
                        {statusMap[project.recruitmentStatus]}
                      </Badge>
                      <Badge variant="outline">
                        {categoryMap[project.category]}
                      </Badge>
                    </div>
                    <div className="flex items-center text-slate-400 text-xs gap-1">
                      <Clock className="h-3 w-3" />
                      {new Date(project.createdAt).toLocaleDateString()}
                    </div>
                  </div>
                  <h3 className="text-xl font-bold text-slate-900 mb-2 group-hover:text-blue-600 transition-colors">
                    {project.title}
                  </h3>
                  <p className="text-slate-500 text-sm mb-6 line-clamp-2 flex-1">
                    {project.description}
                  </p>
                  <div className="space-y-4 mt-auto">
                    <div className="flex flex-wrap gap-2">
                      {project.techStack.slice(0, 4).map((tech, techIndex) => (
                        <span
                          key={`${project.id}-${tech}-${techIndex}`}
                          className="tech-pill"
                        >
                          {tech}
                        </span>
                      ))}
                      {project.techStack.length > 4 && (
                        <span className="tech-pill">
                          +{project.techStack.length - 4}
                        </span>
                      )}
                    </div>
                    <div className="flex items-center justify-between pt-4 border-t border-slate-100">
                      <div className="flex items-center gap-2">
                        <Link
                          href={`/u/${project.leader.id}`}
                          className="flex items-center gap-2 hover:opacity-80 transition-opacity"
                          onClick={(e) => e.stopPropagation()}
                        >
                          <img
                            src={project.leader.avatar}
                            alt={project.leader.name}
                            className="w-6 h-6 rounded-full"
                          />
                          <span className="text-sm font-medium text-slate-700 hover:text-blue-600">
                            {project.leader.name}
                          </span>
                        </Link>
                      </div>
                      <div className="flex items-center gap-1 text-xs font-medium text-blue-600 bg-blue-50 px-2 py-1 rounded-md">
                        <Users className="h-3 w-3" />
                        {project.positions.reduce(
                          (acc, p) => acc + p.filled,
                          0,
                        )}
                        /
                        {project.positions.reduce((acc, p) => acc + p.total, 0)}
                        명
                      </div>
                    </div>
                  </div>
                </Card>
              </motion.div>
            ))}
          </motion.div>
          {projectPageCount > 1 && (
            <div className="mt-10 flex items-center justify-center gap-3">
              <Button
                type="button"
                variant="outline"
                size="sm"
                disabled={projectPage === 0}
                onClick={() => setProjectPage((page) => page - 1)}
              >
                <ArrowLeft className="mr-1 h-4 w-4" />
                이전
              </Button>
              <span className="min-w-16 text-center text-sm text-slate-500">
                {projectPage + 1} / {projectPageCount}
              </span>
              <Button
                type="button"
                variant="outline"
                size="sm"
                disabled={projectPage + 1 >= projectPageCount}
                onClick={() => setProjectPage((page) => page + 1)}
              >
                다음
                <ArrowRight className="ml-1 h-4 w-4" />
              </Button>
            </div>
          )}
        </div>
      </section>

      {/* Popular Tech Stacks */}
      <section className="py-20 bg-white">
        <div className="container mx-auto px-4 text-center">
          <h2 className="text-2xl font-bold text-slate-900 mb-8">
            기술 스택으로 탐색
          </h2>
          <div className="flex flex-wrap justify-center gap-3 max-w-4xl mx-auto">
            {popularTechStacks.map((tech) => (
              <button
                key={tech}
                onClick={() =>
                  router.push(`/projects?tech=${encodeURIComponent(tech)}`)
                }
                className="px-4 py-2 rounded-full border border-slate-200 bg-white text-slate-700 font-medium hover:border-blue-600 hover:text-blue-600 hover:bg-blue-50 transition-all shadow-sm"
              >
                {tech}
              </button>
            ))}
          </div>
        </div>
      </section>

      {/* Benefits Section */}
      <section className="py-24 bg-slate-900 text-white overflow-hidden relative">
        <div className="absolute top-0 right-0 w-1/2 h-full bg-gradient-to-l from-blue-600/20 to-transparent pointer-events-none" />
        <div className="container mx-auto px-4 relative z-10">
          <div className="text-center max-w-2xl mx-auto mb-16">
            <h2 className="text-3xl md:text-4xl font-bold mb-4">
              왜 DevLink인가요?
            </h2>
            <p className="text-slate-400">더 이상 혼자 개발하지 마세요.</p>
          </div>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-8 max-w-5xl mx-auto">
            <div className="bg-slate-800/50 border border-slate-700 p-8 rounded-2xl backdrop-blur-sm">
              <div className="w-12 h-12 bg-blue-500/20 text-blue-400 rounded-xl flex items-center justify-center mb-6">
                <Code className="h-6 w-6" />
              </div>
              <h3 className="text-xl font-bold mb-3">포트폴리오 쌓기</h3>
              <p className="text-slate-400 leading-relaxed">
                의미 있는 프로젝트에 기여하며 실전 경험을 쌓으세요.
              </p>
            </div>
            <div className="bg-slate-800/50 border border-slate-700 p-8 rounded-2xl backdrop-blur-sm">
              <div className="w-12 h-12 bg-purple-500/20 text-purple-400 rounded-xl flex items-center justify-center mb-6">
                <Users className="h-6 w-6" />
              </div>
              <h3 className="text-xl font-bold mb-3">동료 찾기</h3>
              <p className="text-slate-400 leading-relaxed">
                서로의 부족한 점을 채워줄 수 있는 팀원을 만나보세요.
              </p>
            </div>
            <div className="bg-slate-800/50 border border-slate-700 p-8 rounded-2xl backdrop-blur-sm">
              <div className="w-12 h-12 bg-emerald-500/20 text-emerald-400 rounded-xl flex items-center justify-center mb-6">
                <Rocket className="h-6 w-6" />
              </div>
              <h3 className="text-xl font-bold mb-3">더 빠른 출시</h3>
              <p className="text-slate-400 leading-relaxed">
                사이드 프로젝트를 중도 포기하지 마세요.
              </p>
            </div>
          </div>
        </div>
      </section>
    </div>
  )
}
