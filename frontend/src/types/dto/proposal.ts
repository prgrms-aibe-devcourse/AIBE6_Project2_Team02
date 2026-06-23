export interface ProposalProject {
  id: number
  title: string
  positions: ProposalPosition[]
}

export interface ProposalPosition {
  role: string
  filled: number
  total: number
}

export interface ProjectProposalCreateRequest {
  projectId: number
  position: string
  message: string
}

export interface SentProjectProposal {
  proposalId: number
  projectId: number
  projectTitle: string
}
