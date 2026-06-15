import type { Project } from '..'

export interface ProjectCreateRequest {
  title: string
  description: string
  fullDescription: string
  category: Project['category']
  goals: string[]
  deadline: string
  open: boolean
  techStacks: string[]
  positions: Array<{
    role: string
    total: number
  }>
}
