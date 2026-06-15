import type {
  Portfolio,
  PortfolioUpdateRequest,
  Project,
  ProjectProposal,
  ReportResponse,
  RsData,
  User,
} from '../types'
import type { ProjectCreateRequest } from '../types/dto/project'

const API_BASE = 'http://localhost:8080'

async function fetchRsDataJson<T>(
  path: string,
  init: RequestInit = {},
): Promise<T> {
  const response = await fetch(`${API_BASE}${path}`, {
    credentials: 'include',
    cache: 'no-store',
    ...init,
    headers: {
      ...(init.body ? { 'Content-Type': 'application/json' } : {}),
      ...init.headers,
    },
  })

  const rsData = (await response.json().catch(() => null)) as RsData<T> | null

  if (!response.ok) {
    throw new Error(
      rsData?.message ?? `API request failed: ${response.status}`,
    )
  }

  if (!rsData) {
    throw new Error('API response was empty.')
  }

  return rsData.data
}

export function fetchProjects() {
  return fetchRsDataJson<Project[]>('/projects')
}

export function fetchProject(id: string) {
  return fetchRsDataJson<Project>(`/projects/${id}`)
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
      errorBody?.message ?? `Project creation failed: ${response.status}`,
    )
  }

  return (await response.json()) as Project
}

export function fetchMembers() {
  return fetchRsDataJson<User[]>('/members')
}

export function fetchMember(id: string) {
  return fetchRsDataJson<User>(`/members/${id}`)
}

export function fetchPopularTechStacks() {
  return fetchRsDataJson<string[]>('/tech-stacks')
}

export function fetchMyPortfolio() {
  return fetchRsDataJson<Portfolio>('/portfolios/me')
}

export function updateMyPortfolio(payload: PortfolioUpdateRequest) {
  return fetchRsDataJson<Portfolio>('/portfolios/me', {
    method: 'PUT',
    body: JSON.stringify(payload),
  })
}

export function fetchMyProjectProposals() {
  return fetchRsDataJson<ProjectProposal[]>('/portfolios/me/proposals')
}

export function fetchUserReports() {
  return fetchRsDataJson<ReportResponse[]>(
      '/admin/reports?targetType=PORTFOLIO',
  )
}

export function fetchProjectReports() {
  return fetchRsDataJson<ReportResponse[]>('/admin/reports?targetType=PROJECT')
}
