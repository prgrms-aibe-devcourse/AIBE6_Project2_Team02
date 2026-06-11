import type { Project, User } from '../types'

const API_BASE = process.env.NEXT_PUBLIC_API_URL ?? 'http://localhost:8080'

async function fetchJson<T>(path: string): Promise<T> {
  const response = await fetch(`${API_BASE}${path}`, {
    cache: 'no-store',
  })

  if (!response.ok) {
    throw new Error(`API request failed: ${response.status}`)
  }

  return await response.json() as Promise<T>
}

export function fetchProjects() {
  return fetchJson<Project[]>('/api/projects')
}

export function fetchProject(id: string) {
  return fetchJson<Project>(`/api/projects/${id}`)
}

export function fetchMembers() {
  return fetchJson<User[]>('/api/members')
}

export function fetchMember(id: string) {
  return fetchJson<User>(`/api/members/${id}`)
}

export function fetchPopularTechStacks() {
  return fetchJson<string[]>('/api/tech-stacks')
}
