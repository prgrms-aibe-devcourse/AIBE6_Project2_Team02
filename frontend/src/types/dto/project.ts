import type { Project } from '..'
import type { PositionType } from '../enums/project'

export interface ProjectCreateRequest {
  title: string
  description: string
  fullDescription: string
  category: Project['category']
  goals: string[]
  deadline: string
  open: boolean
  leaderPosition: PositionType
  techStacks: string[]
  positions: Array<{
    role: string
    total: number
  }>
}

export interface ProjectUpdateRequest {
  title: string
  description: string
  fullDescription: string
  category: Project['category']
  goals: string[]
  deadline: string
  open: boolean
  positions: Array<{
    role: string
    total: number
  }>
}

export interface ProjectPermissionResponse {
  canEdit: boolean
  isMember: boolean
}
