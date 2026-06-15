export interface PortfolioCreateRequest {
  title: string
  introduction?: string
  desiredPosition: string
  githubUrl?: string
  blogUrl?: string
  deployUrl?: string
  isPublished: boolean
  techStackIds: number[]
}
