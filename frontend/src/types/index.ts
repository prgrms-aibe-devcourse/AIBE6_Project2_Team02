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
  resultCode: string
  msg: string
  data: T
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
