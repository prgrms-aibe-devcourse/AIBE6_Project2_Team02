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

export const mockUsers: Record<string, User> = {
  u1: {
    id: 'u1',
    name: 'Alex Chen',
    avatar: 'https://i.pravatar.cc/150?u=u1',
    role: '풀스택 개발자',
    bio: '확장 가능한 웹 애플리케이션과 오픈소스 툴을 만드는 데 열정이 있습니다. 항상 함께 협업할 흥미로운 사이드 프로젝트를 찾고 있어요.',
    techStack: ['React', 'TypeScript', 'Node.js', 'PostgreSQL', 'AWS'],
    github: 'https://github.com/alexchen',
    portfolio: 'https://alexchen.dev',
    location: 'San Francisco, CA',
    featured: true,
  },
  u2: {
    id: 'u2',
    name: 'Sarah Jenkins',
    avatar: 'https://i.pravatar.cc/150?u=u2',
    role: '백엔드 개발자',
    bio: '시스템 아키텍처에 관심이 많습니다. API를 설계하고 데이터베이스 쿼리를 최적화하는 것을 좋아합니다.',
    techStack: ['Python', 'Django', 'PostgreSQL', 'Redis', 'Docker'],
    github: 'https://github.com/sjenkins',
    location: 'London, UK',
    featured: true,
  },
  u3: {
    id: 'u3',
    name: 'Emma Watson',
    avatar: 'https://i.pravatar.cc/150?u=u3',
    role: '프로덕트 디자이너',
    bio: '직관적이고 아름다운 사용자 경험을 만듭니다. 사용자의 니즈와 비즈니스 목표 사이의 간극을 좁히는 역할을 합니다.',
    techStack: ['Figma', 'Framer', 'HTML/CSS', 'React'],
    portfolio: 'https://emmaw.design',
    location: 'Berlin, DE',
    featured: true,
  },
  u4: {
    id: 'u4',
    name: 'David Kim',
    avatar: 'https://i.pravatar.cc/150?u=u4',
    role: '모바일 개발자',
    bio: 'React Native와 Flutter를 활용한 크로스 플랫폼 앱 개발에 전문성이 있습니다. 부드러운 애니메이션과 성능 최적화에 관심이 많습니다.',
    techStack: ['React Native', 'Flutter', 'TypeScript', 'Firebase'],
    github: 'https://github.com/davidk',
    location: 'Seoul, KR',
    featured: true,
  },
  u5: {
    id: 'u5',
    name: 'Michael Chang',
    avatar: 'https://i.pravatar.cc/150?u=u5',
    role: 'AI 연구원',
    bio: 'LLM 파인튜닝과 프롬프트 엔지니어링을 연구합니다. AI를 활용해 실생활의 문제를 해결하는 프로덕트를 만들고 싶습니다.',
    techStack: ['Python', 'PyTorch', 'OpenAI API', 'LangChain'],
    github: 'https://github.com/mchang',
    location: 'Seattle, WA',
  },
  u6: {
    id: 'u6',
    name: 'Lisa Ray',
    avatar: 'https://i.pravatar.cc/150?u=u6',
    role: '프론트엔드 개발자',
    bio: '웹 접근성과 인터랙티브한 UI 구현을 중요하게 생각합니다. Three.js를 활용한 3D 웹 경험 구축에도 관심이 있습니다.',
    techStack: ['React', 'Vue.js', 'Three.js', 'Tailwind CSS'],
    portfolio: 'https://lisaray.dev',
    location: 'Toronto, CA',
  },
  u15: {
    id: 'u15',
    name: 'Jordan Park',
    avatar: 'https://i.pravatar.cc/150?u=u15',
    role: '풀스택 개발자',
    bio: '스타트업 환경에서 0에서 1을 만드는 일을 좋아합니다. 빠른 프로토타이핑과 사용자 피드백 기반 개발에 능숙합니다.',
    techStack: ['Next.js', 'TypeScript', 'Supabase', 'Tailwind CSS', 'Vercel'],
    github: 'https://github.com/jordanp',
    location: 'Seoul, KR',
    featured: true,
  },
  u16: {
    id: 'u16',
    name: 'Yuna Lee',
    avatar: 'https://i.pravatar.cc/150?u=u16',
    role: 'UI/UX 디자이너',
    bio: '디자인 시스템 구축과 모션 디자인에 강점이 있습니다. 개발자와의 협업을 통해 디테일을 끝까지 챙깁니다.',
    techStack: ['Figma', 'Framer', 'Protopie', 'After Effects'],
    portfolio: 'https://yunalee.design',
    location: 'Busan, KR',
    featured: true,
  },
  u17: {
    id: 'u17',
    name: 'Daniel Cho',
    avatar: 'https://i.pravatar.cc/150?u=u17',
    role: '백엔드 개발자',
    bio: '대규모 트래픽 처리와 마이크로서비스 아키텍처 경험이 풍부합니다. Go와 Kubernetes로 견고한 시스템을 설계합니다.',
    techStack: ['Go', 'Kubernetes', 'gRPC', 'PostgreSQL', 'Kafka'],
    github: 'https://github.com/danielcho',
    location: 'Seongnam, KR',
  },
  u18: {
    id: 'u18',
    name: 'Priya Sharma',
    avatar: 'https://i.pravatar.cc/150?u=u18',
    role: 'AI/데이터 엔지니어',
    bio: '추천 시스템과 데이터 파이프라인 구축을 전문으로 합니다. 비즈니스 임팩트로 이어지는 모델을 만드는 데 집중합니다.',
    techStack: ['Python', 'TensorFlow', 'Spark', 'Airflow', 'AWS'],
    github: 'https://github.com/priyash',
    location: 'Bangalore, IN',
  },
  u19: {
    id: 'u19',
    name: 'Marco Rossi',
    avatar: 'https://i.pravatar.cc/150?u=u19',
    role: '모바일 개발자',
    bio: '네이티브 iOS 개발 8년차. SwiftUI와 앱 성능 최적화, App Store 런칭 전 과정에 익숙합니다.',
    techStack: ['Swift', 'SwiftUI', 'Combine', 'Firebase'],
    github: 'https://github.com/marcorossi',
    location: 'Milan, IT',
  },
  u20: {
    id: 'u20',
    name: '한지민',
    avatar: 'https://i.pravatar.cc/150?u=u20',
    role: '프론트엔드 개발자',
    bio: '사용자 중심의 인터페이스를 구현하는 것을 좋아합니다. 디자인 시스템과 컴포넌트 라이브러리 구축 경험이 있습니다.',
    techStack: ['React', 'TypeScript', 'Storybook', 'Emotion'],
    github: 'https://github.com/jiminhan',
    location: 'Incheon, KR',
  },
}

export const allUsers = Object.values(mockUsers)

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

export const mockProjects: Project[] = [
  {
    id: '1',
    title: 'DevLink - 사이드 프로젝트 플랫폼',
    description:
      '개발자들이 사이드 프로젝트를 찾고 포트폴리오를 구축할 수 있도록 연결해주는 모던 플랫폼입니다.',
    fullDescription:
      '우리는 개발자들이 사이드 프로젝트를 찾고, 포트폴리오를 쌓으며, 마음이 맞는 크리에이터들과 연결될 수 있는 최고의 플랫폼을 만들고 있습니다. MVP 버전에는 사용자 프로필, 프로젝트 매칭, 내장 채팅 시스템이 포함될 예정입니다.',
    goals: [
      '3분기 말까지 MVP 런칭',
      '초기 활성 사용자 1,000명 확보',
      'AI 기반 프로젝트 매칭 구현',
    ],

    techStack: ['React', 'TypeScript', 'Tailwind CSS', 'Node.js', 'PostgreSQL'],
    positions: [
      { role: '프론트엔드 개발자', filled: 1, total: 3 },
      { role: '백엔드 개발자', filled: 1, total: 2 },
      { role: 'UI/UX 디자이너', filled: 0, total: 1 },
    ],

    recruitmentStatus: 'Open',
    category: 'Web',
    leader: mockUsers['u1'],
    teamMembers: [mockUsers['u2']],
    deadline: '2026-07-15',
    createdAt: '2026-06-01',
    popularity: 156,
    featured: true,
  },
  {
    id: '2',
    title: '에코트랙(EcoTrack) 모바일 앱',
    description:
      '게이미피케이션 챌린지를 통해 일상적인 탄소 발자국을 추적하고 줄이세요.',
    fullDescription:
      '에코트랙은 사용자가 일상 활동을 통해 탄소 발자국을 모니터링할 수 있도록 돕는 모바일 애플리케이션입니다. 스마트홈 기기 및 교통 API와 연동하여 정확한 추적과 실용적인 인사이트를 제공하는 것을 목표로 합니다.',
    goals: [
      '크로스 플랫폼 모바일 앱 개발',
      '대중교통 추적을 위한 Google Maps API 연동',
      '게이미피케이션 시스템 설계',
    ],

    techStack: ['React Native', 'Firebase', 'TypeScript', 'Figma'],
    positions: [
      { role: '모바일 개발자', filled: 1, total: 2 },
      { role: '프로덕트 매니저', filled: 0, total: 1 },
    ],

    recruitmentStatus: 'Open',
    category: 'Mobile',
    leader: mockUsers['u3'],
    teamMembers: [
      {
        id: 'u4',
        name: 'David Kim',
        avatar: 'https://i.pravatar.cc/150?u=u4',
        role: 'React Native 개발자',
      },
    ],

    deadline: '2026-08-01',
    createdAt: '2026-05-20',
    popularity: 89,
    featured: true,
  },
  {
    id: '3',
    title: 'AI 코드 리뷰어',
    description:
      'LLM을 사용하여 PR(Pull Request)을 자동으로 리뷰해주는 오픈소스 CLI 툴입니다.',
    fullDescription:
      '수동 코드 리뷰에 지치셨나요? 우리는 GitHub Actions와 연동되어 OpenAI/Anthropic 모델을 사용해 의미 있는 코드 리뷰 코멘트를 제공하고, 버그를 탐지하며, 최적화 방안을 제안하는 오픈소스 CLI 툴을 만들고 있습니다.',
    goals: [
      '견고한 CLI 인터페이스 구축',
      'GitHub App 연동 구현',
      '정확한 리뷰를 위한 프롬프트 파인튜닝',
    ],

    techStack: ['Python', 'OpenAI API', 'GitHub Actions', 'Docker'],
    positions: [
      { role: 'Python 개발자', filled: 2, total: 4 },
      { role: 'DevOps 엔지니어', filled: 0, total: 1 },
    ],

    recruitmentStatus: 'Open',
    category: 'AI',
    leader: {
      id: 'u5',
      name: 'Michael Chang',
      avatar: 'https://i.pravatar.cc/150?u=u5',
      role: 'AI 연구원',
    },
    teamMembers: [
      {
        id: 'u6',
        name: 'Lisa Ray',
        avatar: 'https://i.pravatar.cc/150?u=u6',
        role: 'Python 개발자',
      },
      {
        id: 'u7',
        name: 'Tom Hardy',
        avatar: 'https://i.pravatar.cc/150?u=u7',
        role: 'Python 개발자',
      },
    ],

    deadline: '2026-06-30',
    createdAt: '2026-06-05',
    popularity: 210,
    featured: true,
  },
  {
    id: '4',
    title: '인디 게임: 네온 나이츠',
    description:
      'Godot 엔진으로 만드는 사이버펑크 테마의 2D 플랫포머 게임입니다.',
    fullDescription:
      '네온 나이츠는 디스토피아적인 사이버펑크 미래를 배경으로 하는 빠른 템포의 2D 플랫포머 게임입니다. 핵심 메커니즘은 완성되었지만, 레벨 디자인, 픽셀 아트, 사운드 엔지니어링에 도움이 필요합니다.',
    goals: [
      '플레이 가능한 10개 레벨 완성',
      'Itch.io에 데모 버전 퍼블리싱',
      '오리지널 신스웨이브 사운드트랙 제작',
    ],

    techStack: ['Godot', 'GDScript', 'Aseprite', 'FMOD'],
    positions: [
      { role: '픽셀 아티스트', filled: 0, total: 2 },
      { role: '사운드 디자이너', filled: 0, total: 1 },
      { role: '레벨 디자이너', filled: 1, total: 2 },
    ],

    recruitmentStatus: 'Open',
    category: 'Game',
    leader: {
      id: 'u8',
      name: 'Sam Porter',
      avatar: 'https://i.pravatar.cc/150?u=u8',
      role: '게임 디렉터',
    },
    teamMembers: [
      {
        id: 'u9',
        name: 'Julia Roberts',
        avatar: 'https://i.pravatar.cc/150?u=u9',
        role: '레벨 디자이너',
      },
    ],

    deadline: '2026-09-01',
    createdAt: '2026-04-15',
    popularity: 120,
  },
  {
    id: '5',
    title: '동네 음식 구조대',
    description: '남은 음식이 있는 식당과 지역 쉼터를 연결해줍니다.',
    fullDescription:
      '음식물 쓰레기를 줄이는 것을 목표로 하는 비영리 웹 애플리케이션입니다. 식당은 영업 종료 후 남은 음식을 게시할 수 있고, 인증된 지역 쉼터는 이를 신청하여 수거 일정을 조율할 수 있습니다.',
    goals: [
      '지역 식당 50곳과 파트너십 체결',
      '실시간 알림 시스템 구축',
      '쉼터를 위한 안전한 인증 절차 마련',
    ],

    techStack: ['Next.js', 'Supabase', 'Tailwind CSS', 'Vercel'],
    positions: [
      { role: '풀스택 개발자', filled: 2, total: 2 },
      { role: '마케팅 리드', filled: 0, total: 1 },
    ],

    recruitmentStatus: 'Closed',
    category: 'Web',
    leader: {
      id: 'u10',
      name: 'Nina Simone',
      avatar: 'https://i.pravatar.cc/150?u=u10',
      role: '파운더',
    },
    teamMembers: [
      {
        id: 'u11',
        name: 'Chris Evans',
        avatar: 'https://i.pravatar.cc/150?u=u11',
        role: '풀스택 개발자',
      },
      {
        id: 'u12',
        name: 'Mark Ruffalo',
        avatar: 'https://i.pravatar.cc/150?u=u12',
        role: '풀스택 개발자',
      },
    ],

    deadline: '2026-05-01',
    createdAt: '2026-03-10',
    popularity: 340,
  },
  {
    id: '6',
    title: '크립토 포트폴리오 트래커',
    description:
      '여러 체인에 흩어진 DeFi 자산을 추적할 수 있는 세련된 대시보드입니다.',
    fullDescription:
      '유명 포트폴리오 트래커들의 오픈소스 대안입니다. 우리는 서버에 데이터를 저장하지 않고 지갑을 연결하여 잔고를 통합해서 보여주는 프라이버시 중심의 셀프 호스팅 대시보드를 만들고 있습니다.',
    goals: [
      'Ethereum, Solana, Polygon 지원',
      'Recharts를 활용한 아름다운 차트 구현',
      '100% 클라이언트 사이드 데이터 처리 보장',
    ],

    techStack: ['Vue.js', 'TypeScript', 'Ethers.js', 'Tailwind CSS'],
    positions: [
      { role: '프론트엔드 개발자', filled: 0, total: 2 },
      { role: 'Web3 엔지니어', filled: 1, total: 2 },
    ],

    recruitmentStatus: 'Open',
    category: 'Web',
    leader: {
      id: 'u13',
      name: 'Vitalik B.',
      avatar: 'https://i.pravatar.cc/150?u=u13',
      role: '블록체인 개발자',
    },
    teamMembers: [
      {
        id: 'u14',
        name: 'Satoshi N.',
        avatar: 'https://i.pravatar.cc/150?u=u14',
        role: 'Web3 엔지니어',
      },
    ],

    deadline: '2026-07-20',
    createdAt: '2026-06-08',
    popularity: 175,
  },
  {
    id: '7',
    title: '소셜 독서 기록 앱 - 북셸프',
    description:
      '읽은 책을 기록하고 친구들과 인사이트를 공유하는 소셜 독서 플랫폼입니다.',
    fullDescription:
      '북셸프는 단순한 독서 기록을 넘어 취향이 맞는 사람들과 책에 대한 생각을 나눌 수 있는 소셜 플랫폼입니다. 독서 통계, 추천 알고리즘, 독서 모임 기능을 제공할 예정입니다.',
    goals: [
      'MVP 출시 및 베타 테스터 200명 모집',
      '도서 데이터 API 연동',
      '소셜 피드 및 팔로우 기능 구현',
    ],

    techStack: ['React', 'TypeScript', 'Node.js', 'MongoDB', 'Tailwind CSS'],
    positions: [
      { role: '프론트엔드 개발자', filled: 1, total: 2 },
      { role: '백엔드 개발자', filled: 0, total: 2 },
      { role: 'UI/UX 디자이너', filled: 0, total: 1 },
    ],

    recruitmentStatus: 'Open',
    category: 'Web',
    leader: mockUsers['u20'],
    teamMembers: [mockUsers['u15']],
    deadline: '2026-08-10',
    createdAt: '2026-06-09',
    popularity: 98,
  },
  {
    id: '8',
    title: '실시간 협업 화이트보드',
    description:
      '원격 팀을 위한 무한 캔버스 기반의 실시간 협업 화이트보드 툴입니다.',
    fullDescription:
      'Figma처럼 부드러운 실시간 협업 경험을 제공하는 화이트보드를 만듭니다. CRDT 기반 동기화, 멀티 커서, 도형/텍스트/이미지 편집을 지원하는 것을 목표로 합니다.',
    goals: [
      'CRDT 기반 실시간 동기화 엔진 구축',
      '60fps 캔버스 렌더링 최적화',
      '음성/화상 통화 연동',
    ],

    techStack: ['React', 'WebRTC', 'Yjs', 'Canvas API', 'WebSocket'],
    positions: [
      { role: '프론트엔드 개발자', filled: 1, total: 3 },
      { role: 'Web3 엔지니어', filled: 0, total: 1 },
    ],

    recruitmentStatus: 'Open',
    category: 'Web',
    leader: mockUsers['u6'],
    teamMembers: [mockUsers['u1']],
    deadline: '2026-09-15',
    createdAt: '2026-06-07',
    popularity: 142,
  },
  {
    id: '9',
    title: 'AI 면접 코칭 서비스',
    description:
      '음성 인식과 LLM을 활용해 모의 면접을 진행하고 피드백을 주는 서비스입니다.',
    fullDescription:
      '취업 준비생을 위한 AI 면접 코칭 서비스입니다. 사용자의 답변을 음성으로 분석하고, 내용/표현/태도에 대한 구체적인 피드백과 개선 가이드를 제공합니다.',
    goals: [
      '실시간 음성 인식 및 분석 파이프라인 구축',
      'LLM 기반 피드백 생성 프롬프트 설계',
      '직군별 면접 질문 데이터셋 구축',
    ],

    techStack: ['Python', 'OpenAI API', 'Whisper', 'FastAPI', 'React'],
    positions: [
      { role: 'AI 엔지니어', filled: 1, total: 2 },
      { role: '프론트엔드 개발자', filled: 0, total: 1 },
    ],

    recruitmentStatus: 'Open',
    category: 'AI',
    leader: mockUsers['u18'],
    teamMembers: [mockUsers['u5']],
    deadline: '2026-08-25',
    createdAt: '2026-06-04',
    popularity: 188,
  },
  {
    id: '10',
    title: '로컬 러닝 크루 매칭 앱',
    description:
      '동네에서 함께 달릴 러닝 메이트와 크루를 찾아주는 위치 기반 모바일 앱입니다.',
    fullDescription:
      '혼자 달리기 지치셨나요? 비슷한 페이스와 목표를 가진 러너들을 위치 기반으로 매칭해주고, 크루 활동과 챌린지를 관리할 수 있는 앱입니다.',
    goals: [
      '위치 기반 매칭 알고리즘 구현',
      '러닝 기록 트래킹 및 통계',
      '크루 채팅 및 일정 관리 기능',
    ],

    techStack: ['React Native', 'TypeScript', 'Firebase', 'Mapbox'],
    positions: [
      { role: '모바일 개발자', filled: 1, total: 2 },
      { role: '백엔드 개발자', filled: 0, total: 1 },
    ],

    recruitmentStatus: 'Open',
    category: 'Mobile',
    leader: mockUsers['u19'],
    teamMembers: [mockUsers['u4']],
    deadline: '2026-09-30',
    createdAt: '2026-05-28',
    popularity: 76,
  },
  {
    id: '11',
    title: '오픈소스 디자인 시스템 - Aurora UI',
    description:
      '접근성과 커스터마이징을 최우선으로 한 React 컴포넌트 라이브러리입니다.',
    fullDescription:
      'Aurora UI는 WAI-ARIA를 완벽히 준수하면서도 디자인 토큰으로 자유롭게 테마를 변경할 수 있는 오픈소스 React 컴포넌트 라이브러리입니다. 문서화와 예제에도 진심입니다.',
    goals: [
      '핵심 컴포넌트 30종 구현',
      'Storybook 기반 문서 사이트 구축',
      '디자인 토큰 시스템 설계',
    ],

    techStack: ['React', 'TypeScript', 'Storybook', 'Tailwind CSS', 'Figma'],
    positions: [
      { role: '프론트엔드 개발자', filled: 2, total: 3 },
      { role: 'UI/UX 디자이너', filled: 1, total: 2 },
    ],

    recruitmentStatus: 'Open',
    category: 'Web',
    leader: mockUsers['u16'],
    teamMembers: [mockUsers['u6'], mockUsers['u20']],
    deadline: '2026-10-01',
    createdAt: '2026-05-15',
    popularity: 224,
  },
  {
    id: '12',
    title: '클라우드 비용 최적화 대시보드',
    description:
      '멀티 클라우드 인프라 비용을 한눈에 보고 절감 포인트를 추천하는 툴입니다.',
    fullDescription:
      'AWS, GCP, Azure에 흩어진 클라우드 비용을 통합해 시각화하고, 유휴 리소스와 절감 기회를 자동으로 탐지해 추천하는 FinOps 대시보드입니다.',
    goals: [
      '멀티 클라우드 비용 API 연동',
      '유휴 리소스 탐지 로직 구현',
      '비용 추이 시각화 대시보드 구축',
    ],

    techStack: ['Go', 'React', 'PostgreSQL', 'Kubernetes', 'Recharts'],
    positions: [
      { role: '백엔드 개발자', filled: 1, total: 2 },
      { role: '프론트엔드 개발자', filled: 0, total: 1 },
    ],

    recruitmentStatus: 'Closed',
    category: 'Web',
    leader: mockUsers['u17'],
    teamMembers: [mockUsers['u2']],
    deadline: '2026-05-20',
    createdAt: '2026-04-02',
    popularity: 134,
  },
]

export const popularTechStacks = [
  'React',
  'TypeScript',
  'Node.js',
  'Python',
  'Next.js',
  'Tailwind CSS',
  'PostgreSQL',
  'AWS',
  'Docker',
  'Figma',
]
