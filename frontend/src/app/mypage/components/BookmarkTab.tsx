'use client'

import Link from 'next/link'
import { useEffect, useState } from 'react'
import type { ReactNode } from 'react'
import { Briefcase, FileText, Users } from 'lucide-react'

import { Badge, Card } from '../../../components/ui'
import { formatPositionLabel } from '../../../constants/project'
import { formatProjectMemberCount } from '../../../lib/project'
import { fetchBookmarkedPortfolios, fetchBookmarkedProjects } from '../../../lib/api'
import type { Project, User } from '../../../types'

type BookmarkFilter = 'projects' | 'portfolios'

interface BookmarkedProject {
  bookmarkedAt: string
  project: Project
}

interface BookmarkedPortfolio {
  bookmarkedAt: string
  portfolio: User
}

const categoryMap: Record<string, string> = {
  Web: '웹',
  Mobile: '모바일',
  AI: 'AI',
  Game: '게임',
  Other: '기타',
}

const statusMap: Record<string, string> = {
  RECRUITING: '모집중',
  CLOSED: '인원 마감',
  IN_PROGRESS: '진행 중',
  COMPLETED: '완료',
  DISBANDED: '중단',
  CANCELLED: '취소',
}

export default function BookmarkTab() {
  const [activeFilter, setActiveFilter] = useState<BookmarkFilter>('projects')
  const [projects, setProjects] = useState<BookmarkedProject[]>([])
  const [portfolios, setPortfolios] = useState<BookmarkedPortfolio[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    setLoading(true)

    Promise.all([fetchBookmarkedProjects(), fetchBookmarkedPortfolios()])
      .then(([projectData, portfolioData]) => {
        setProjects(projectData)
        setPortfolios(portfolioData)
      })
      .catch(() => {
        setProjects([])
        setPortfolios([])
      })
      .finally(() => setLoading(false))
  }, [])

  if (loading) {
    return (
      <div className="py-12 text-center text-slate-400">
        북마크를 불러오는 중...
      </div>
    )
  }

  return (
    <div>
      <div className="mb-6 flex gap-2 overflow-x-auto border-b border-slate-200 pb-4">
        <button
          onClick={() => setActiveFilter('projects')}
          className={`tab-pill ${activeFilter === 'projects' ? 'tab-pill-active' : 'tab-pill-inactive'}`}
        >
          프로젝트 북마크
        </button>
        <button
          onClick={() => setActiveFilter('portfolios')}
          className={`tab-pill ${activeFilter === 'portfolios' ? 'tab-pill-active' : 'tab-pill-inactive'}`}
        >
          포트폴리오 북마크
        </button>
      </div>

      {activeFilter === 'projects' ? (
        projects.length > 0 ? (
          <div className="grid grid-cols-1 gap-4">
            {projects.map(({ project }) => (
              <Link key={project.id} href={`/projects/${project.id}`}>
                <Card className="p-5 transition-shadow hover:shadow-md">
                  <div className="mb-3 flex items-start justify-between gap-3">
                    <div className="flex flex-wrap items-center gap-2">
                      <Badge
                        variant={
                          project.recruitmentStatus === 'RECRUITING'
                            ? 'success'
                            : 'secondary'
                        }
                      >
                        {statusMap[project.recruitmentStatus] ??
                          project.recruitmentStatus}
                      </Badge>
                      <Badge variant="outline">
                        {categoryMap[project.category] ?? project.category}
                      </Badge>
                    </div>
                    <div className="member-count-badge shrink-0">
                      <Users className="h-3 w-3" />
                      {formatProjectMemberCount(project.positions)}
                    </div>
                  </div>
                  <h3 className="mb-2 text-lg font-bold text-slate-900">
                    {project.title}
                  </h3>
                  <p className="line-clamp-2 text-sm text-slate-500">
                    {project.description}
                  </p>
                  <div className="mt-4 flex flex-wrap gap-2">
                    {project.techStack?.slice(0, 4).map((tech) => (
                      <span key={tech} className="tech-pill">
                        {tech}
                      </span>
                    ))}
                  </div>
                </Card>
              </Link>
            ))}
          </div>
        ) : (
          <EmptyBookmark
            icon={<Briefcase className="h-10 w-10 text-slate-300" />}
            title="북마크한 프로젝트가 없습니다"
            description="관심 있는 프로젝트를 북마크하면 이곳에서 다시 볼 수 있습니다."
          />
        )
      ) : portfolios.length > 0 ? (
        <div className="grid grid-cols-1 gap-4">
          {portfolios.map(({ portfolio }) => (
            <Link key={portfolio.id} href={`/portfolio/${portfolio.id}`}>
              <Card className="p-5 transition-shadow hover:shadow-md">
                <div className="flex items-start gap-4">
                  <img
                    src={portfolio.avatar}
                    alt={portfolio.name}
                    className="h-12 w-12 rounded-full object-cover"
                  />
                  <div className="min-w-0 flex-1">
                    <h3 className="font-bold text-slate-900">
                      {portfolio.name}
                    </h3>
                    <p className="mt-1 text-sm text-blue-600">
                      {formatPositionLabel(portfolio.role)}
                    </p>
                    <p className="mt-2 line-clamp-2 text-sm text-slate-500">
                      {portfolio.bio}
                    </p>
                    <div className="mt-4 flex flex-wrap gap-2">
                      {portfolio.techStack?.slice(0, 4).map((tech) => (
                        <span key={tech} className="tech-pill">
                          {tech}
                        </span>
                      ))}
                    </div>
                  </div>
                </div>
              </Card>
            </Link>
          ))}
        </div>
      ) : (
        <EmptyBookmark
          icon={<FileText className="h-10 w-10 text-slate-300" />}
          title="북마크한 포트폴리오가 없습니다"
          description="관심 있는 포트폴리오를 북마크하면 이곳에서 다시 볼 수 있습니다."
        />
      )}
    </div>
  )
}

function EmptyBookmark({
  icon,
  title,
  description,
}: {
  icon: ReactNode
  title: string
  description: string
}) {
  return (
    <Card className="border-dashed p-12 text-center">
      <div className="mx-auto mb-4 flex h-16 w-16 items-center justify-center rounded-full bg-slate-100">
        {icon}
      </div>
      <h3 className="mb-2 text-lg font-medium text-slate-900">{title}</h3>
      <p className="text-sm text-slate-500">{description}</p>
    </Card>
  )
}
