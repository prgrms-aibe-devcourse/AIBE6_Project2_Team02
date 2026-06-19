'use client'

import { motion } from 'framer-motion';
import type { MouseEvent } from 'react';
import { useEffect, useRef, useState } from 'react';
import Link from 'next/link';
import { useRouter, useSearchParams } from 'next/navigation';
import { BookmarkPlus, Clock, Search, Sparkles, Users } from 'lucide-react';
import { LoginModal } from '../../../components/LoginModal';
import { PaginationControls } from '../../../components/PaginationControls';
import { SearchField } from '../../../components/SearchField';
import { Badge, Button, Card } from '../../../components/ui';
import {
  addProjectBookmark,
  fetchPopularTechStacks,
  fetchProjectBookmark,
  fetchProjects,
  removeProjectBookmark,
} from '../../../lib/api';
import { formatDate } from '../../../lib/date';
import { formatProjectMemberCount } from '../../../lib/project';
import type { Project } from '../../../types';
import { useAuth } from '../../providers';

const categoryMap: Record<string, string> = {
  All: '전체',
  Web: '웹',
  Mobile: '모바일',
  AI: 'AI',
  Game: '게임',
  Other: '기타',
}
const statusMap: Record<string, string> = {
  All: '전체',
  RECRUITING: '모집중',
  CLOSED: '인원 마감',
  COMPLETED: '완료',
  STOPPED: '중단',
}
const categories = ['All', 'Web', 'Mobile', 'AI', 'Game', 'Other']
const statuses = ['All', 'RECRUITING', 'CLOSED']

export default function ProjectListingClient() {
  const searchParams = useSearchParams()
  const router = useRouter()
  const { user: authUser, loading: authLoading } = useAuth()
  const initialTech = searchParams.get('tech')
  const projectRequestIdRef = useRef(0)

  // 🎯 [수정] 서버 페이징 상태 관리를 위한 최적화 명시
  const [paginatedProjects, setPaginatedProjects] = useState<Project[]>([])
  const [featuredProjects, setFeaturedProjects] = useState<Project[]>([]) // 異붿쿇 ?꾨줈?앺듃 遺꾨━ 蹂닿?
  const [popularTechStacks, setPopularTechStacks] = useState<string[]>([])

  const [page, setPage] = useState(0)
  const [pageCount, setPageCount] = useState(0)
  const [totalElements, setTotalElements] = useState(0) // 珥??꾨줈?앺듃 媛쒖닔 ?쒖떆??
  const [contentLoading, setContentLoading] = useState(false)
  const [bookmarkedProjectIds, setBookmarkedProjectIds] = useState<Set<string>>(new Set())
  const [bookmarkingProjectIds, setBookmarkingProjectIds] = useState<Set<string>>(new Set())
  const [isLoginModalOpen, setIsLoginModalOpen] = useState(false)

  // 필터 상태 변수들
  const [searchTerm, setSearchTerm] = useState('')
  const [selectedCategory, setSelectedCategory] = useState<string>('All')
  const [selectedTech, setSelectedTech] = useState<string>(initialTech || 'All')
  const [selectedStatus, setSelectedStatus] = useState<string>('RECRUITING')

  const changeSearchTerm = (value: string) => {
    setPage(0)
    setSearchTerm(value)
  }

  const changeCategory = (value: string) => {
    setPage(0)
    setSelectedCategory(value)
  }

  const changeTech = (value: string) => {
    setPage(0)
    setSelectedTech(value)
  }

  const changeStatus = (value: string) => {
    setPage(0)
    setSelectedStatus(value)
  }

  const handleToggleBookmark = async (
    event: MouseEvent<HTMLButtonElement>,
    projectId: string,
  ) => {
    event.preventDefault()
    event.stopPropagation()

    if (authLoading || bookmarkingProjectIds.has(projectId)) return
    if (!authUser) {
      setIsLoginModalOpen(true)
      return
    }

    setBookmarkingProjectIds((current) => new Set(current).add(projectId))
    try {
      const isBookmarked = bookmarkedProjectIds.has(projectId)
      const nextBookmarked = isBookmarked
        ? await removeProjectBookmark(projectId)
        : await addProjectBookmark(projectId)

      setBookmarkedProjectIds((current) => {
        const next = new Set(current)
        if (nextBookmarked) next.add(projectId)
        else next.delete(projectId)
        return next
      })
    } finally {
      setBookmarkingProjectIds((current) => {
        const next = new Set(current)
        next.delete(projectId)
        return next
      })
    }
  }

  // 인기 기술 스택 로드 (최초 1회만)
  useEffect(() => {
    fetchPopularTechStacks()
      .then(setPopularTechStacks)
      .catch(() => setPopularTechStacks([]))
  }, [])

  useEffect(() => {
    setContentLoading(true)
    const requestId = ++projectRequestIdRef.current
    const requestStatus = selectedStatus === 'All' ? 'RECRUITING,CLOSED' : selectedStatus

    fetchProjects({
      page,
      size: 6,
      search: searchTerm,
      category: selectedCategory,
      tech: selectedTech,
      status: requestStatus,
    })
      .then((pageData) => {
        if (requestId !== projectRequestIdRef.current) return
        if (pageData && pageData.content) {
          setPaginatedProjects(pageData.content)
          setPageCount(pageData.totalPages)
          setTotalElements(pageData.totalElements)


          setFeaturedProjects(
            selectedStatus === 'RECRUITING'
              ? pageData.content.filter(
                  (p) => p.featured && p.recruitmentStatus === 'RECRUITING',
                )
              : [],
          )
        } else {
          setPaginatedProjects([])
          setPageCount(0)
          setTotalElements(0)
          setFeaturedProjects([])
        }
      })
      .catch((err) => {
        if (requestId !== projectRequestIdRef.current) return
        console.error('프로젝트 로드 에러:', err)
        setPaginatedProjects([])
        setPageCount(0)
        setTotalElements(0)
        setFeaturedProjects([])
      })
      .finally(() => {
        if (requestId === projectRequestIdRef.current) {
          setContentLoading(false)
        }
      })
  }, [page, searchTerm, selectedCategory, selectedTech, selectedStatus])

  useEffect(() => {
    if (authLoading || !authUser) {
      setBookmarkedProjectIds(new Set())
      return
    }

    const projectIds = Array.from(
      new Set([...paginatedProjects, ...featuredProjects].map((project) => project.id)),
    )

    if (projectIds.length === 0) {
      setBookmarkedProjectIds(new Set())
      return
    }

    Promise.all(
      projectIds.map((projectId) =>
        fetchProjectBookmark(projectId)
          .then((bookmarked) => [projectId, bookmarked] as const)
          .catch(() => [projectId, false] as const),
      ),
    ).then((entries) => {
      setBookmarkedProjectIds(
        new Set(entries.filter(([, bookmarked]) => bookmarked).map(([projectId]) => projectId)),
      )
    })
  }, [authLoading, authUser, paginatedProjects, featuredProjects])


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
            onChange={changeSearchTerm}
          />

          <div className="flex flex-wrap items-center gap-4 w-full md:w-auto">
            {/* Category Segmented Control */}
            <div className="segment-control overflow-x-auto">
              {categories.map((cat) => (
                <button
                  key={cat}
                  onClick={() => changeCategory(cat)}
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
                  onClick={() => changeStatus(status)}
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
              onChange={(e) => changeTech(e.target.value)}
            >
              <option value="All">전체 기술 스택</option>
              {popularTechStacks.map((tech) => (
                <option key={tech} value={tech}>{tech}</option>
              ))}
            </select>

          </div>
        </div>
      </div>

      {/* Featured Projects Section */}
      {!contentLoading && featuredProjects.length > 0 && (
        <div className="mb-16">
          <h2 className="text-xl font-bold text-slate-900 mb-6 flex items-center gap-2">
            <Sparkles className="h-5 w-5 text-amber-500" /> 추천 프로젝트
          </h2>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {featuredProjects.slice(0, 3).map((project) => (
              <Link key={project.id} href={`/projects/${project.id}`}>
                <Card className="listing-card group flex hover:border-blue-300 hover:shadow-md h-full">
                  <div className="flex justify-between items-start gap-3 mb-4">
                    <div className="flex items-center gap-2">
                      <Badge variant={project.recruitmentStatus === 'RECRUITING' ? 'success' : 'secondary'}>{statusMap[project.recruitmentStatus] || project.recruitmentStatus}</Badge>
                      <Badge variant="outline">{categoryMap[project.category] || project.category}</Badge>
                      <button
                        type="button"
                        className={`rounded-md border p-1.5 transition-colors ${
                          bookmarkedProjectIds.has(project.id)
                            ? 'border-blue-200 bg-blue-50 text-blue-600'
                            : 'border-slate-200 text-slate-400 hover:text-blue-600'
                        }`}
                        disabled={bookmarkingProjectIds.has(project.id)}
                        onClick={(event) => handleToggleBookmark(event, project.id)}
                        aria-label={
                          bookmarkedProjectIds.has(project.id)
                              ? '북마크 해제'
                              : '북마크 추가'
                        }
                      >
                        <BookmarkPlus className="h-3.5 w-3.5" />
                      </button>
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
                      {project.techStack?.slice(0, 4).map((tech) => (
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
            {/* 🎯 [수정] 백엔드 매핑 전체 결과 수(totalElements) 출력 */}
            <span className="font-medium text-slate-900">{totalElements}</span>개의 프로젝트
          </p>
        </div>

        {contentLoading ? (
            <div className="text-center py-24 text-slate-400 font-medium">
              조건에 맞는 프로젝트 로드 중...
            </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {paginatedProjects.length > 0 ? (
              paginatedProjects.map((project, index) => (
                <motion.div
                  key={project.id}
                  initial={{ opacity: 0, y: 20 }}
                  animate={{ opacity: 1, y: 0 }}
                  transition={{ delay: index * 0.05 }}
                >
                  <Card
                    className="listing-card group flex h-full"
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
                    <div className="flex justify-between items-start gap-3 mb-4">
                      <div className="flex items-center gap-2">
                        <Badge variant={project.recruitmentStatus === 'RECRUITING' ? 'success' : 'secondary'}>{statusMap[project.recruitmentStatus] || project.recruitmentStatus}</Badge>
                        <Badge variant="outline">{categoryMap[project.category] || project.category}</Badge>
                        <button
                          type="button"
                          className={`rounded-md border p-1.5 transition-colors ${
                            bookmarkedProjectIds.has(project.id)
                              ? 'border-blue-200 bg-blue-50 text-blue-600'
                              : 'border-slate-200 text-slate-400 hover:text-blue-600'
                          }`}
                          disabled={bookmarkingProjectIds.has(project.id)}
                          onClick={(event) => handleToggleBookmark(event, project.id)}
                          aria-label={
                            bookmarkedProjectIds.has(project.id)
                                ? '북마크 해제'
                                : '북마크 추가'
                          }
                        >
                          <BookmarkPlus className="h-3.5 w-3.5" />
                        </button>
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
                        {project.techStack?.slice(0, 4).map((tech) => (
                          <span key={tech} className="tech-pill">{tech}</span>
                        ))}
                        {project.techStack?.length > 4 && (
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
                    setPage(0)
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
        )}

        <div className="mt-12">
          <PaginationControls
            page={page}
            pageCount={pageCount}
            onPageChange={setPage}
          />
        </div>
      </div>
      {isLoginModalOpen && (
        <LoginModal onClose={() => setIsLoginModalOpen(false)} />
      )}
    </div>
  )
}
