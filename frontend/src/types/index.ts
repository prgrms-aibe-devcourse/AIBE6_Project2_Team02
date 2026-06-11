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
