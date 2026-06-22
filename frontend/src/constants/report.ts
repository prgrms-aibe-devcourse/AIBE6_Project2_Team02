import type { ReportReasonType, ReportTargetType } from '../types'

export const REPORT_TARGET_MAP: Record<ReportTargetType, string> = {
  PORTFOLIO: '포트폴리오',
  PROJECT: '프로젝트',
}

export const REPORT_REASON_MAP: Record<ReportReasonType, string> = {
  SPAM: '스팸/부적절한 홍보',
  ABUSE: '욕설/비하 발언',
  ADVERTISEMENT: '광고성 콘텐츠',
  INAPPROPRIATE_CONTENT: '부적절한 내용',
  FRAUD: '사기/허위 정보',
  ETC: '기타',
}

export const getReportTargetLabel = (type: ReportTargetType): string => {
  return REPORT_TARGET_MAP[type] || type
}

export const getReportReasonLabel = (type: string): string => {
  return REPORT_REASON_MAP[type as ReportReasonType] || type
}
