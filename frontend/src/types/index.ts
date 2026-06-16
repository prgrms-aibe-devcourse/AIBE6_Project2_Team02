export type Position = {
  role: string
  filled: number
  total: number
}

export type User = {
  id: string
  name: string
  avatar: string
  role: string
  bio?: string
  techStack?: string[]
  github?: string
  portfolio?: string
  location?: string
  featured?: boolean
}

export type Project = {
  id: string
  title: string
  description: string
  fullDescription: string
  goals: string[]
  techStack: string[]
  positions: Position[]
  recruitmentStatus: 'Open' | 'Closed'
  category: 'Web' | 'Mobile' | 'AI' | 'Game' | 'Other'
  leader: User
  teamMembers: User[]
  deadline: string
  createdAt: string
  popularity: number
  featured?: boolean
}

export interface RsData<T> {
  code: string
  message: string
  data: T
}

export interface PortfolioLink {
  linkType: string
  url: string
}

export interface Portfolio {
  id: number
  title: string
  introduction: string
  links: PortfolioLink[]
  desiredPosition: string | null
  isPublished: boolean
  techStacks: string[]
}

export interface PortfolioUpdateRequest {
  title: string
  introduction: string
  portfolioLinks: PortfolioLink[]
  desiredPosition: string | null
  isPublished: boolean
  techStacks: string[]
}

export interface ProjectProposal {
  proposalId: number
  projectId: number
  projectTitle: string
  proposerName: string
  message: string
  status: 'PENDING' | 'ACCEPTED' | 'REJECTED' | 'CANCELLED'
  createdAt: string
}

export interface CreateReviewRequest {
  projectId: number
  revieweeId: number
  content: Record<string, string>
}

export type ReportTargetType = 'PORTFOLIO' | 'PROJECT'
export type ReportStatus = 'PENDING' | 'REVIEWED'

export interface ReportResponse {
  reportId: number
  reporterId: number
  targetType: ReportTargetType
  targetId: number
  reasonType: string
  reasonDetail: string
  status: ReportStatus
  createdAt: string
  reviewedAt: string | null
}

export interface ReviewResponse {
  reviewId: number
  projectId: number
  content: Record<string, string>
  createdAt: string
}
