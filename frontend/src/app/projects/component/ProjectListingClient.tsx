'use client'

import { motion } from 'framer-motion'
import { useEffect, useMemo, useState } from 'react'
import Link from 'next/link'
import { useRouter, useSearchParams } from 'next/navigation'
import { Clock, Search, Sparkles, Users } from 'lucide-react'

import { PaginationControls } from '../../../components/PaginationControls'
import { Badge, Button, Card } from '../../../components/ui'
import { SearchField } from '../../../components/SearchField'
import { fetchPopularTechStacks, fetchProjects } from '../../../lib/api'
import { formatDate, getTimeValue } from '../../../lib/date'
import { formatProjectMemberCount } from '../../../lib/project'
import { usePaginatedList } from '../../../hooks/usePaginatedList'
import type { Project } from '../../../types'

const categoryMap: Record<string, string> = {
  All: '전체', Web: '웹', Mobile: '모바일', AI: 'AI', Game: '게임', Other: '기타',
}
const statusMap: Record<string, string> = {
  All: '전체', Open: '모집중', Closed: '마감', Completed: '완료', Stopped: '중단',
}

export default function ProjectListingClient() {
  const searchParams = useSearchParams()
  const router = useRouter()
  const initialTech = searchParams.get('tech')

  const [projects, setProjects] = useState<Project[]>([])
  const [popularTechStacks, setPopularTechStacks] = useState<string[]>([])
  const [searchTerm, setSearchTerm] = useState('')
  const [selectedCategory, setSelectedCategory] = useState<string>('All')
  const [selectedTech, setSelectedTech] = useState<string>(initialTech || 'All')
  const [selectedStatus, setSelectedStatus] = useState<string>('Open')
  const [sortBy, setSortBy] = useState<'newest' | 'popularity'>('newest')

  const categories = ['All', 'Web', 'Mobile', 'AI', 'Game', 'Other']
  const statuses = ['All', 'Open', 'Closed']

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

  const featuredProjects = projects.filter(
    (p) => p.featured && p.recruitmentStatus === 'Open',
  )

  const filteredProjects = useMemo(() => {
    return projects
      .filter((p) => {
        const matchesSearch =
          p.title.toLowerCase().includes(searchTerm.toLowerCase()) ||
          p.description.toLowerCase().includes(searchTerm.toLowerCase())
        const matchesCategory =
          selectedCategory === 'All' || p.category === selectedCategory
        const matchesTech =
          selectedTech === 'All' || p.techStack.includes(selectedTech)
        const matchesStatus =
          selectedStatus === 'All' || p.recruitmentStatus === selectedStatus
        return matchesSearch && matchesCategory && matchesTech && matchesStatus
      })
      .sort((a, b) => {
        if (sortBy === 'newest') {
          return getTimeValue(b.createdAt) - getTimeValue(a.createdAt)
        } else {
          return b.popularity - a.popularity
        }
      })
  }, [projects, searchTerm, selectedCategory, selectedTech, selectedStatus, sortBy])

  const projectsPerPage = 6
  const {
    page,
    pageCount,
    paginatedItems: paginatedProjects,
    setPage,
  } = usePaginatedList({
    items: filteredProjects,
    pageSize: projectsPerPage,
    resetDeps: [
      searchTerm,
      selectedCategory,
      selectedTech,
      selectedStatus,
      sortBy,
    ],
  })

  return (
    <div className="container mx-auto px-4 py-8 max-w-7xl">
      <div className="mb-8 text-center max-w-2xl mx-auto">
        <Badge variant="purple" className="mb-4">프로젝트 탐색</Badge>
        <h1 className="text-3xl md:text-4xl font-bold text-slate-900 mb-4 break-keep">프로젝트 찾기</h1>
        <p className="text-slate-500 text-lg break-keep">당신의 스킬을 필요로 하는 팀을 발견하고 여정에 합류하세요.</p>
      </div>

      {/* Horizontal Filters */}
      <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-sm mb-12">
        <div className="flex flex-col md:flex-row gap-4 items-center justify-between">
          <SearchField
            placeholder="키워드 입력..."
            value={searchTerm}
            onChange={setSearchTerm}
          />

          <div className="flex flex-wrap items-center gap-4 w-full md:w-auto">
            {/* Category Segmented Control */}
            <div className="segment-control overflow-x-auto">
              {categories.map((cat) => (
                <button
                  key={cat}
                  onClick={() => setSelectedCategory(cat)}
                  className={`segment-option ${selectedCategory === cat ? 'segment-option-active' : 'segment-option-inactive'}`}
                >
                  {categoryMap[cat]}
                </button>
              ))}
            </div>

            {/* Status Segmented Control */}
            <div className="segment-control">
              {statuses.map((status) => (
                <button
                  key={status}
                  onClick={() => setSelectedStatus(status)}
                  className={`segment-option ${selectedStatus === status ? 'segment-option-active' : 'segment-option-inactive'}`}
                >
                  {statusMap[status]}
                </button>
              ))}
            </div>

            {/* Tech Stack Select */}
            <select
              className="form-field md:w-auto"
              value={selectedTech}
              onChange={(e) => setSelectedTech(e.target.value)}
            >
              <option value="All">전체 기술 스택</option>
              {popularTechStacks.map((tech) => (
                <option key={tech} value={tech}>{tech}</option>
              ))}
            </select>

            {/* Sort Select */}
            <select
              className="form-field md:w-auto"
              value={sortBy}
              onChange={(e) => setSortBy(e.target.value as any)}
            >
              <option value="newest">최신순</option>
              <option value="popularity">인기순</option>
            </select>
          </div>
        </div>
      </div>

      {/* Featured Projects Section */}
      {featuredProjects.length > 0 && (
        <div className="mb-16">
          <h2 className="text-xl font-bold text-slate-900 mb-6 flex items-center gap-2">
            <Sparkles className="h-5 w-5 text-amber-500" /> 추천 프로젝트
          </h2>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {featuredProjects.slice(0, 3).map((project) => (
              <Link key={project.id} href={`/projects/${project.id}`}>
                <Card className="listing-card group flex hover:border-blue-300 hover:shadow-md">
                  <div className="flex justify-between items-start mb-4">
                    <div className="flex gap-2">
                      <Badge variant={project.recruitmentStatus === 'Open' ? 'success' : 'secondary'}>{statusMap[project.recruitmentStatus]}</Badge>
                      <Badge variant="outline">{categoryMap[project.category]}</Badge>
                    </div>
                  </div>

                  <h3 className="text-xl font-bold text-slate-900 mb-2 group-hover:text-blue-600 transition-colors">{project.title}</h3>
                  <p className="text-slate-500 text-sm mb-6 line-clamp-2 flex-1">{project.description}</p>

                  <div className="space-y-4 mt-auto">
                    <div className="flex flex-wrap gap-2">
                      {project.techStack.slice(0, 4).map((tech) => (
                        <span key={tech} className="tech-pill">{tech}</span>
                      ))}
                    </div>
                    <div className="flex items-center justify-between pt-4 border-t border-slate-100">
                      <div className="flex items-center gap-2">
                        <img src={project.leader.avatar} alt={project.leader.name} className="w-6 h-6 rounded-full" />
                        <span className="text-sm font-medium text-slate-700">{project.leader.name}</span>
                      </div>
                      <div className="member-count-badge">
                        <Users className="h-3 w-3" />
                        {formatProjectMemberCount(project.positions)}
                      </div>
                    </div>
                  </div>
                </Card>
              </Link>
            ))}
          </div>
        </div>
      )}

      {/* Main Content */}
      <div>
        <div className="flex justify-between items-center mb-6">
          <h2 className="text-xl font-bold text-slate-900 flex items-center gap-2">
            <Clock className="h-5 w-5 text-blue-500" /> 최신 프로젝트
          </h2>
          <p className="text-sm text-slate-500">
            <span className="font-medium text-slate-900">{filteredProjects.length}</span>개의 프로젝트
          </p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {filteredProjects.length > 0 ? (
            paginatedProjects.map((project, index) => (
              <motion.div
                key={project.id}
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ delay: index * 0.05 }}
              >
                <Card
                  className="listing-card group flex"
                  role="link"
                  tabIndex={0}
                  onClick={() => router.push(`/projects/${project.id}`)}
                  onKeyDown={(event) => {
                    if (event.key === 'Enter' || event.key === ' ') {
                      event.preventDefault()
                      router.push(`/projects/${project.id}`)
                    }
                  }}
                >
                  <div className="flex justify-between items-start mb-4">
                    <div className="flex gap-2">
                      <Badge variant={project.recruitmentStatus === 'Open' ? 'success' : 'secondary'}>{statusMap[project.recruitmentStatus]}</Badge>
                      <Badge variant="outline">{categoryMap[project.category]}</Badge>
                    </div>
                    <div className="flex items-center text-slate-400 text-xs gap-1">
                      <Clock className="h-3 w-3" />
                      {formatDate(project.createdAt)}
                    </div>
                  </div>

                  <h3 className="text-xl font-bold text-slate-900 mb-2 group-hover:text-blue-600 transition-colors">{project.title}</h3>
                  <p className="text-slate-500 text-sm mb-6 line-clamp-2 flex-1">{project.description}</p>

                  <div className="space-y-4 mt-auto">
                    <div className="flex flex-wrap gap-2">
                      {project.techStack.slice(0, 4).map((tech) => (
                        <span key={tech} className="tech-pill">{tech}</span>
                      ))}
                      {project.techStack.length > 4 && (
                        <span className="tech-pill">+{project.techStack.length - 4}</span>
                      )}
                    </div>

                    <div className="flex items-center justify-between pt-4 border-t border-slate-100">
                      <div className="flex items-center gap-2">
                        <Link href={`/u/${project.leader.id}`} className="flex items-center gap-2 hover:opacity-80 transition-opacity" onClick={(e) => e.stopPropagation()}>
                          <img src={project.leader.avatar} alt={project.leader.name} className="w-6 h-6 rounded-full" />
                          <span className="text-sm font-medium text-slate-700 hover:text-blue-600">{project.leader.name}</span>
                        </Link>
                      </div>
                      <div className="member-count-badge">
                        <Users className="h-3 w-3" />
                        {formatProjectMemberCount(project.positions)}
                      </div>
                    </div>
                  </div>
                </Card>
              </motion.div>
            ))
          ) : (
            <div className="col-span-full py-20 text-center border-2 border-dashed border-slate-200 rounded-xl">
              <div className="w-16 h-16 bg-slate-100 rounded-full flex items-center justify-center mx-auto mb-4">
                <Search className="h-8 w-8 text-slate-400" />
              </div>
              <h3 className="text-lg font-semibold text-slate-900 mb-1">프로젝트를 찾을 수 없어요</h3>
              <p className="text-slate-500">검색어나 필터 조건을 변경해보세요.</p>
              <Button
                variant="outline"
                className="mt-4"
                onClick={() => {
                  setSearchTerm('')
                  setSelectedCategory('All')
                  setSelectedTech('All')
                  setSelectedStatus('All')
                }}
              >
                필터 초기화
              </Button>
            </div>
          )}
        </div>

        <PaginationControls
          page={page}
          pageCount={pageCount}
          onPageChange={setPage}
        />
      </div>
    </div>
  )
}
