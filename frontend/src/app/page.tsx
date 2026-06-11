'use client'

import { motion } from 'framer-motion'
import React from 'react'

import Link from 'next/link'
import { useRouter } from 'next/navigation'

import { ArrowRight, Code, Rocket, Users } from 'lucide-react'

import { Badge, Button, Card } from '../components/ui'
import { mockProjects, popularTechStacks } from '../data/mock'

const categoryMap: Record<string, string> = {
  Web: '웹',
  Mobile: '모바일',
  AI: 'AI',
  Game: '게임',
  Other: '기타',
}

export default function LandingPage() {
  const router = useRouter()
  const featuredProjects = mockProjects.filter((p) => p.featured).slice(0, 3)

  const containerVariants = {
    hidden: {
      opacity: 0,
    },
    visible: {
      opacity: 1,
      transition: {
        staggerChildren: 0.1,
      },
    },
  }

  const itemVariants = {
    hidden: {
      opacity: 0,
      y: 20,
    },
    visible: {
      opacity: 1,
      y: 0,
    },
  }

  return (
    <div className="flex flex-col w-full">
      {/* Hero Section */}
      <section className="relative overflow-hidden bg-white pt-24 pb-32">
        {/* Abstract Background Shapes */}
        <div className="absolute top-0 left-1/2 -translate-x-1/2 w-full max-w-7xl h-full overflow-hidden pointer-events-none">
          <div className="absolute -top-[20%] -left-[10%] w-[50%] h-[50%] rounded-full bg-blue-100/50 blur-3xl" />
          <div className="absolute top-[20%] -right-[10%] w-[40%] h-[40%] rounded-full bg-purple-100/50 blur-3xl" />
        </div>

        <div className="container relative z-10 mx-auto px-4 text-center">
          <motion.div
            initial={{
              opacity: 0,
              y: 30,
            }}
            animate={{
              opacity: 1,
              y: 0,
            }}
            transition={{
              duration: 0.6,
            }}
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
              실현하세요. 포트폴리오를 쌓고, 새로운 기술을 배우며 함께
              런칭하세요.
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

      {/* Featured Projects */}
      <section className="py-20 bg-slate-50">
        <div className="container mx-auto px-4">
          <div className="flex items-end justify-between mb-12">
            <div>
              <h2 className="text-3xl font-bold text-slate-900 mb-2">
                추천 프로젝트
              </h2>
              <p className="text-slate-500">
                당신의 스킬을 기다리고 있는 엄선된 팀들을 만나보세요.
              </p>
            </div>
            <Link
              href="/projects"
              className="hidden md:flex items-center text-blue-600 font-medium hover:text-blue-700"
            >
              전체 보기 <ArrowRight className="ml-1 h-4 w-4" />
            </Link>
          </div>

          <motion.div
            variants={containerVariants}
            initial="hidden"
            whileInView="visible"
            viewport={{
              once: true,
              margin: '-100px',
            }}
            className="grid grid-cols-1 md:grid-cols-3 gap-6"
          >
            {featuredProjects.map((project) => (
              <motion.div key={project.id} variants={itemVariants}>
                <Link href={`/projects/${project.id}`}>
                  <Card className="h-full flex flex-col hover:shadow-md transition-shadow hover:border-blue-200 group cursor-pointer">
                    <div className="p-6 flex-1 flex flex-col">
                      <div className="flex justify-between items-start mb-4">
                        <Badge
                          variant={
                            project.category === 'AI' ? 'purple' : 'default'
                          }
                        >
                          {categoryMap[project.category] || project.category}
                        </Badge>
                        <span className="text-xs text-slate-400 font-medium">
                          {project.positions.reduce(
                            (acc, p) => acc + (p.total - p.filled),
                            0,
                          )}
                          자리 남음
                        </span>
                      </div>
                      <h3 className="text-xl font-bold text-slate-900 mb-2 group-hover:text-blue-600 transition-colors">
                        {project.title}
                      </h3>
                      <p className="text-slate-500 text-sm mb-6 line-clamp-2 flex-1">
                        {project.description}
                      </p>
                      <div className="flex flex-wrap gap-2 mb-6">
                        {project.techStack.slice(0, 3).map((tech) => (
                          <span
                            key={tech}
                            className="text-xs font-medium bg-slate-100 text-slate-600 px-2 py-1 rounded-md"
                          >
                            {tech}
                          </span>
                        ))}
                        {project.techStack.length > 3 && (
                          <span className="text-xs font-medium bg-slate-100 text-slate-600 px-2 py-1 rounded-md">
                            +{project.techStack.length - 3}
                          </span>
                        )}
                      </div>
                      <div className="flex items-center gap-3 pt-4 border-t border-slate-100 mt-auto">
                        <div
                          className="hover:opacity-80 transition-opacity"
                          onClick={(e) => {
                            e.preventDefault()
                            e.stopPropagation()
                            router.push(`/u/${project.leader.id}`)
                          }}
                        >
                          <img
                            src={project.leader.avatar}
                            alt={project.leader.name}
                            className="w-8 h-8 rounded-full bg-slate-200"
                          />
                        </div>
                        <div className="text-sm">
                          <div
                            className="font-medium text-slate-900 hover:text-blue-600 transition-colors block cursor-pointer"
                            onClick={(e) => {
                              e.preventDefault()
                              e.stopPropagation()
                              router.push(`/u/${project.leader.id}`)
                            }}
                          >
                            {project.leader.name}
                          </div>
                          <p className="text-slate-500 text-xs">
                            {project.leader.role}
                          </p>
                        </div>
                      </div>
                    </div>
                  </Card>
                </Link>
              </motion.div>
            ))}
          </motion.div>
          <div className="mt-8 text-center md:hidden">
            <Link href="/projects">
              <Button variant="outline" className="w-full">
                전체 보기
              </Button>
            </Link>
          </div>
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
            <p className="text-slate-400">
              더 이상 혼자 개발하지 마세요. 열정적인 커뮤니티에 합류하여 의미
              있는 프로덕트를 만드세요.
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-8 max-w-5xl mx-auto">
            <div className="bg-slate-800/50 border border-slate-700 p-8 rounded-2xl backdrop-blur-sm">
              <div className="w-12 h-12 bg-blue-500/20 text-blue-400 rounded-xl flex items-center justify-center mb-6">
                <Code className="h-6 w-6" />
              </div>
              <h3 className="text-xl font-bold mb-3">포트폴리오 쌓기</h3>
              <p className="text-slate-400 leading-relaxed">
                의미 있는 프로젝트에 기여하며 실전 경험을 쌓으세요. 단순한
                튜토리얼이 아닌, 실제 서비스되는 프로덕트를 자랑할 수 있습니다.
              </p>
            </div>

            <div className="bg-slate-800/50 border border-slate-700 p-8 rounded-2xl backdrop-blur-sm">
              <div className="w-12 h-12 bg-purple-500/20 text-purple-400 rounded-xl flex items-center justify-center mb-6">
                <Users className="h-6 w-6" />
              </div>
              <h3 className="text-xl font-bold mb-3">동료 찾기</h3>
              <p className="text-slate-400 leading-relaxed">
                서로의 부족한 점을 채워줄 수 있는 팀원을 만나보세요. 훌륭한
                개발자에게는 훌륭한 디자이너와 기획자가 필요합니다.
              </p>
            </div>

            <div className="bg-slate-800/50 border border-slate-700 p-8 rounded-2xl backdrop-blur-sm">
              <div className="w-12 h-12 bg-emerald-500/20 text-emerald-400 rounded-xl flex items-center justify-center mb-6">
                <Rocket className="h-6 w-6" />
              </div>
              <h3 className="text-xl font-bold mb-3">더 빠른 출시</h3>
              <p className="text-slate-400 leading-relaxed">
                사이드 프로젝트를 중도 포기하지 마세요. 헌신적인 팀원들과 명확한
                목표를 가지고 끝까지 완주할 수 있습니다.
              </p>
            </div>
          </div>
        </div>
      </section>
    </div>
  )
}
