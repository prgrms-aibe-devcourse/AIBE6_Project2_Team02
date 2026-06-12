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

export type ReportReason = '음란' | '분탕' | '기타'

export interface BaseReport {
  id: string
  reason: ReportReason
  detail: string
  reporterId: string
  createdAt: string
  status: 'pending' | 'resolved'
}

export interface UserReport extends BaseReport {
  type: 'user'
  targetUserId: string
}

export interface ProjectReport extends BaseReport {
  type: 'project'
  targetProjectId: string
}
