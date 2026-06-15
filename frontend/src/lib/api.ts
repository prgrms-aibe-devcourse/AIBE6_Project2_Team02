import type { Project, ReportResponse, RsData, User } from '../types'

const API_BASE = 'http://localhost:8080'

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

async function fetchRsDataJson<T>(path: string): Promise<T> {
  const response = await fetch(`${API_BASE}${path}`, {
    cache: 'no-store',
  })

  if (!response.ok) {
    throw new Error(`API request failed: ${response.status}`)
  }

  const rsData = (await response.json()) as RsData<T>
  return rsData.data
}

export function fetchProjects() {
  return fetchRsDataJson<Project[]>('/api/projects')
}

export function fetchProject(id: string) {
  return fetchRsDataJson<Project>(`/api/projects/${id}`)
}

export async function createProject(
  payload: ProjectCreateRequest,
): Promise<Project> {
  const response = await fetch(`${API_BASE}/projects`, {
    method: 'POST',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(payload),
  })

  if (!response.ok) {
    const errorBody = (await response.json().catch(() => null)) as
      | Partial<RsData<unknown>>
      | null

    throw new Error(
      errorBody?.msg ?? `Project creation failed: ${response.status}`,
    )
  }

  return (await response.json()) as Project
}

export function fetchMembers() {
  return fetchRsDataJson<User[]>('/api/members')
}

export function fetchMember(id: string) {
  return fetchRsDataJson<User>(`/api/members/${id}`)
}

export function fetchPopularTechStacks() {
  return fetchRsDataJson<string[]>('/api/tech-stacks')
}

export function fetchUserReports() {
  return fetchRsDataJson<ReportResponse[]>(
      '/admin/reports?targetType=PORTFOLIO',
  )
}

export function fetchProjectReports() {
  return fetchRsDataJson<ReportResponse[]>('/admin/reports?targetType=PROJECT')
}
