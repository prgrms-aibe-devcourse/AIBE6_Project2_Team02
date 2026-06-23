# DevLink

사이드 프로젝트 팀빌딩을 위한 풀스택 웹 애플리케이션입니다. 개발자는 프로젝트 공고를 탐색하고 지원할 수 있고, 프로젝트 리더는 지원자를 관리하거나 포트폴리오 기반으로 팀원을 제안할 수 있습니다.

## 주요 기능

- 프로젝트 공고 탐색, 상세 조회, 생성, 수정, 삭제
- 프로젝트 지원, 지원자 승인/거절, 팀원 역할 변경, 팀원 내보내기
- 포트폴리오 생성/수정, 포트폴리오 탐색, 프로젝트 제안
- 프로젝트/포트폴리오 북마크
- 마이페이지 프로젝트 현황: 내가 만든 프로젝트, 참여중, 지원중, 완료, 최근 조회
- 상호 리뷰, 신고, 관리자 숨김/복구 및 회원 제재
- Google/GitHub/Kakao OAuth2 로그인 및 JWT 쿠키 인증
- Firebase Cloud Messaging 기반 알림
- Gemini API 기반 프로젝트 설명, 포트폴리오 소개, 지원 동기 초안 생성

## 핵심 사용자 플로우

### 프로젝트 지원 플로우

```text
로그인
→ 프로젝트 찾기에서 공고 탐색
→ 프로젝트 상세에서 모집 포지션과 기술 스택 확인
→ 지원 동기 작성 후 지원
→ 프로젝트 리더가 지원자 관리 화면에서 승인 또는 거절
→ 승인된 사용자는 프로젝트 팀원으로 참여
```

### 포트폴리오 기반 제안 플로우

```text
로그인
→ 내 포트폴리오 작성 및 공개
→ 다른 사용자가 포트폴리오 목록에서 후보 탐색
→ 포트폴리오 상세에서 프로젝트 합류 제안 전송
→ 제안을 받은 사용자가 마이페이지에서 수락 또는 거절
→ 수락 시 해당 프로젝트 팀원으로 합류
```

### 프로젝트 운영 플로우

```text
프로젝트 생성
→ 지원자 승인과 팀원 구성
→ 모집 상태 및 프로젝트 상태 관리
→ 프로젝트 완료 처리
→ 참여자 간 상호 리뷰 작성
→ 작성된 리뷰를 사용자별 리뷰 목록에서 확인
```

### 신고 및 관리자 처리 플로우

```text
사용자가 프로젝트 또는 포트폴리오 신고
→ 관리자가 신고 목록에서 내용 확인
→ 신고 승인 시 대상 숨김 처리
→ 필요 시 회원 제재 또는 숨김 해제
```

## 도메인 관계

서비스의 중심 도메인은 `Member`, `Project`, `Portfolio`입니다. 회원은 프로젝트를 만들거나 지원할 수 있고, 포트폴리오를 공개해 다른 프로젝트 리더에게 합류 제안을 받을 수 있습니다.

### 회원과 인증

`Member`는 서비스의 사용자 단위입니다. OAuth 로그인 정보는 `OauthAccount`로 분리되어 있으며, 하나의 회원은 여러 OAuth 계정을 가질 수 있습니다. 회원은 일반 사용자와 관리자로 구분되고, 신고 처리 결과에 따라 정지, 영구 정지, 탈퇴 상태를 가질 수 있습니다.

### 프로젝트와 팀 구성

`Project`는 프로젝트 공고와 운영 상태를 관리합니다. 프로젝트는 한 명의 리더 회원을 가지며, 실제 팀 구성은 `ProjectMember`를 통해 관리됩니다. `ProjectMember`에는 참여 포지션, 프로젝트 내 역할, 참여 상태가 저장되어 리더, 매니저, 일반 팀원을 구분할 수 있습니다.

프로젝트 지원은 `ProjectApplication`으로 관리됩니다. 지원자는 원하는 포지션과 지원 메시지를 제출하고, 리더가 승인하거나 거절합니다. 지원이 승인되면 해당 회원은 프로젝트 팀원으로 등록됩니다.

### 포트폴리오와 프로젝트 제안

`Portfolio`는 회원과 1:1로 연결됩니다. 회원은 자신의 소개, 희망 포지션, 링크, 기술 스택을 등록하고 공개 여부를 설정할 수 있습니다.

프로젝트 리더는 공개된 포트폴리오를 보고 `ProjectProposal`을 보낼 수 있습니다. 제안은 프로젝트, 포트폴리오, 제안자를 함께 참조하며, 수락되면 포트폴리오 소유자가 해당 프로젝트 팀원으로 합류합니다.

### 기술 스택

`TechStack`은 회원, 프로젝트, 포트폴리오에서 공통으로 사용하는 기술 태그입니다. 각각 `MemberTechStack`, `ProjectTechStack`, `PortfolioTechStack` 조인 엔티티로 연결해 검색과 필터링에 활용합니다.

### 리뷰와 활동 기록

프로젝트가 완료되면 참여자 간 상호 리뷰를 작성할 수 있습니다. `Review`는 프로젝트, 작성자, 대상자를 함께 저장해 같은 프로젝트 안에서 누가 누구를 평가했는지 추적합니다.

최근 조회한 프로젝트는 `ProjectView`로 관리됩니다. 이를 통해 마이페이지에서 사용자가 다시 확인하고 싶은 프로젝트 목록을 제공할 수 있습니다.

### 북마크, 신고, 알림

`Bookmark`는 회원이 프로젝트나 포트폴리오를 저장하는 기능입니다. 대상이 두 종류이기 때문에 `targetType`, `targetId` 조합으로 참조합니다.

`Report`도 프로젝트와 포트폴리오를 모두 신고할 수 있도록 `targetType`, `targetId` 구조를 사용합니다. 신고는 관리자가 승인하거나 반려할 수 있고, 승인 결과에 따라 대상 숨김이나 회원 제재로 이어집니다.

`Notification`은 지원, 제안, 리뷰 등 사용자 액션에 대한 알림을 저장합니다. 알림은 수신 회원, 알림 타입, 이동할 URL, 읽음 여부, FCM 전송 여부를 함께 관리합니다.

## 기술 스택

### Frontend

- Next.js
- React 18
- TypeScript
- Tailwind CSS
- Framer Motion
- Firebase Messaging

### Backend

- Java 17
- Spring Boot 4
- Spring Security + OAuth2 Client
- Spring Data JPA
- Querydsl
- H2, MySQL Driver
- JWT
- Firebase Admin SDK

## 프로젝트 구조

```text
.
├── backend/
│   ├── src/main/java/com/backend/
│   │   ├── BackendApplication.java
│   │   └── common/
│   │       ├── domain/
│   │       │   ├── bookmark/       # 프로젝트/포트폴리오 북마크
│   │       │   ├── member/         # 회원, OAuth 계정, 관리자 회원 관리
│   │       │   ├── notification/   # 알림 조회, 읽음 처리
│   │       │   ├── portfolio/      # 포트폴리오와 프로젝트 제안
│   │       │   ├── project/        # 프로젝트 공고, 지원, 팀 관리
│   │       │   ├── report/         # 신고 생성, 관리자 신고 처리
│   │       │   ├── review/         # 프로젝트 완료 후 상호 리뷰
│   │       │   └── techstack/      # 기술 스택 조회
│   │       └── global/
│   │           ├── ai/             # Gemini 기반 AI 초안 생성
│   │           ├── config/         # Web, Querydsl 설정
│   │           ├── exception/      # 전역 예외 처리
│   │           ├── fcm/            # Firebase Cloud Messaging
│   │           ├── init/           # 애플리케이션 초기화 로직
│   │           ├── rsdata/         # 공통 API 응답 포맷
│   │           └── security/       # Spring Security, JWT, OAuth2
│   ├── src/main/resources/         # Spring profile, datasource, OAuth 설정
│   └── build.gradle.kts
│
└── frontend/
    ├── public/                     # 정적 파일, 아이콘, 서비스 워커
    ├── src/
    │   ├── app/                    # Next.js App Router 페이지
    │   │   ├── admin/              # 관리자 화면
    │   │   ├── login/              # 로그인 진입 화면
    │   │   ├── mypage/             # 마이페이지와 내 포트폴리오 관리
    │   │   ├── portfolio/          # 포트폴리오 목록/상세
    │   │   └── projects/           # 프로젝트 목록/상세/생성/관리
    │   ├── components/             # 공통 UI 컴포넌트
    │   ├── constants/              # 화면 옵션과 상수
    │   ├── hooks/                  # 재사용 React 훅
    │   ├── lib/                    # API 클라이언트, 인증, 날짜, Firebase
    │   └── types/                  # 공통 타입, DTO, enum 타입
    ├── package.json
    └── next.config.mjs
```

백엔드는 도메인 단위로 `controller`, `service`, `repository`, `entity`, `dto`를 나누는 구조입니다. `common/domain`에는 비즈니스 기능이 모여 있고, 인증/예외/응답 포맷/외부 연동처럼 여러 도메인에서 공유하는 코드는 `common/global` 아래에 둡니다.

프론트엔드는 Next.js App Router 기반입니다. URL과 직접 연결되는 화면은 `src/app` 아래에 위치하고, 여러 화면에서 재사용하는 요소는 `components`, `lib`, `hooks`, `types`로 분리합니다.

## 로컬 실행

```bash
cd backend
./gradlew bootRun
```

```bash
cd frontend
npm install
npm run dev
```

백엔드는 `http://localhost:8080`, 프론트엔드는 `http://localhost:3000`에서 실행됩니다.

## 환경 설정

로컬 실행 시 필요한 주요 환경변수입니다. OAuth, Firebase, Gemini는 사용하는 기능에 맞게 값을 채웁니다.

### Frontend

```env
NEXT_PUBLIC_API_URL=http://localhost:8080

NEXT_PUBLIC_FIREBASE_API_KEY=
NEXT_PUBLIC_FIREBASE_AUTH_DOMAIN=
NEXT_PUBLIC_FIREBASE_PROJECT_ID=
NEXT_PUBLIC_FIREBASE_MESSAGING_SENDER_ID=
NEXT_PUBLIC_FIREBASE_APP_ID=
NEXT_PUBLIC_FIREBASE_VAPID_KEY=
```

### Backend

```yaml
app:
  frontend-url: http://localhost:3000
  jwt:
    secret: local-dev-secret-key-local-dev-secret-key
  cookie:
    secure: false
    same-site: Lax

GOOGLE_CLIENT_ID: your-google-client-id
GOOGLE_CLIENT_SECRET: your-google-client-secret
GITHUB_CLIENT_ID: your-github-client-id
GITHUB_CLIENT_SECRET: your-github-client-secret
KAKAO_CLIENT_ID: your-kakao-client-id
KAKAO_CLIENT_SECRET: your-kakao-client-secret
GEMINI_API_KEY:
```

OAuth 로그인을 테스트하려면 각 Provider 콘솔에 redirect URI를 등록해야 합니다.

## 주요 API 영역

프론트엔드에서 실제 호출하는 주요 API를 도메인별로 정리했습니다.

### Auth

| Method | Endpoint | Description |
| --- | --- | --- |
| GET | `/oauth2/authorization/{provider}` | OAuth 로그인 시작 |
| GET | `/auth/me` | 현재 로그인 사용자 조회 |
| POST | `/auth/logout` | 로그아웃 |

### Projects

| Method | Endpoint | Description |
| --- | --- | --- |
| GET | `/projects` | 프로젝트 목록 조회 |
| GET | `/projects/{id}` | 프로젝트 상세 조회 |
| POST | `/projects` | 프로젝트 생성 |
| PUT | `/projects/{id}` | 프로젝트 수정 |
| DELETE | `/projects/{projectId}` | 프로젝트 삭제 |
| POST | `/projects/{id}/applications` | 프로젝트 지원 |
| GET | `/projects/{id}/permissions` | 프로젝트 상세 화면 권한 조회 |
| GET | `/projects/man/{id}` | 프로젝트 관리 화면 상세 조회 |
| GET | `/projects/manage/{id}` | 프로젝트 지원자 목록 조회 |
| POST | `/projects/manageToTeam/{id}` | 지원자 승인 및 팀원 등록 |
| POST | `/projects/{projectId}/applications/{memberId}/reject` | 지원자 거절 |
| PATCH | `/projects/{projectId}/status` | 프로젝트 상태 변경 |
| DELETE | `/projects/{projectId}/members/{memberId}` | 팀원 내보내기 |
| DELETE | `/projects/{projectId}/members/me` | 프로젝트 탈퇴 |
| PATCH | `/projects/{projectId}/members/{memberId}/role` | 팀원 권한 변경 |

### Portfolios

| Method | Endpoint | Description |
| --- | --- | --- |
| GET | `/portfolios` | 공개 포트폴리오 목록 조회 |
| GET | `/portfolios/{memberId}` | 포트폴리오 상세 조회 |
| GET | `/portfolios/me` | 내 포트폴리오 조회 |
| POST | `/portfolios` | 내 포트폴리오 생성 |
| PATCH | `/portfolios/me` | 내 포트폴리오 수정 |
| GET | `/portfolios/proposal-projects` | 제안 가능한 내 프로젝트 목록 조회 |
| POST | `/portfolios/{memberId}/proposals` | 프로젝트 합류 제안 |
| GET | `/portfolios/me/proposals` | 내 포트폴리오에 온 제안 조회 |
| GET | `/portfolios/{memberId}/proposals/sent` | 특정 포트폴리오에 보낸 대기 중 제안 조회 |
| PATCH | `/portfolios/me/proposals/{proposalId}` | 받은 제안 수락 또는 거절 |
| DELETE | `/portfolios/proposals/{proposalId}` | 보낸 제안 취소 |

### Members and Tech Stacks

| Method | Endpoint | Description |
| --- | --- | --- |
| GET | `/members` | 회원 목록 조회 |
| GET | `/members/{id}` | 회원 상세 조회 |
| DELETE | `/members/me` | 회원 탈퇴 |
| PATCH | `/members/me/nickname` | 내 닉네임 수정 |
| GET | `/tech-stacks` | 인기 기술 스택 목록 조회 |
| GET | `/tech-stacks/all` | 전체 기술 스택 목록 조회 |

### My Page

| Method | Endpoint | Description |
| --- | --- | --- |
| GET | `/mypage/projects/owned` | 내가 만든 프로젝트 목록 조회 |
| GET | `/mypage/projects/participating` | 내가 참여 중인 프로젝트 목록 조회 |
| GET | `/mypage/projects/applied` | 내가 지원한 프로젝트 목록 조회 |
| GET | `/mypage/projects/completed` | 내가 완료/해산한 프로젝트 목록 조회 |
| GET | `/mypage/projects/recent-views` | 최근 조회한 프로젝트 목록 조회 |
| DELETE | `/mypage/projects/recent-views/{projectId}` | 최근 조회한 프로젝트 삭제 |
| GET | `/mypage/projects/applications` | 내 프로젝트에 들어온 지원 목록 조회 |
| PATCH | `/mypage/projects/applications/{applicationId}` | 지원 수락 또는 거절 |
| PATCH | `/mypage/projects/applications/{applicationId}/cancel` | 내가 신청한 지원 취소 |

### Bookmarks

| Method | Endpoint | Description |
| --- | --- | --- |
| GET | `/bookmarks/projects` | 북마크한 프로젝트 목록 조회 |
| GET | `/bookmarks/projects/{projectId}` | 프로젝트 북마크 여부 조회 |
| POST | `/bookmarks/projects/{projectId}` | 프로젝트 북마크 추가 |
| DELETE | `/bookmarks/projects/{projectId}` | 프로젝트 북마크 해제 |
| GET | `/bookmarks/portfolios` | 북마크한 포트폴리오 목록 조회 |
| GET | `/bookmarks/portfolios/{memberId}` | 포트폴리오 북마크 여부 조회 |
| POST | `/bookmarks/portfolios/{memberId}` | 포트폴리오 북마크 추가 |
| DELETE | `/bookmarks/portfolios/{memberId}` | 포트폴리오 북마크 해제 |

### Reviews, Reports, Notifications

| Method | Endpoint | Description |
| --- | --- | --- |
| POST | `/reviews` | 리뷰 작성 |
| GET | `/reviews/check-access` | 리뷰 작성 가능 여부 확인 |
| GET | `/reviews/users/{userId}` | 사용자 리뷰 목록 조회 |
| POST | `/reports` | 프로젝트 또는 포트폴리오 신고 |
| GET | `/reports/check` | 신고 여부 확인 |
| GET | `/notifications/me` | 내 알림 목록 조회 |
| GET | `/notifications/me/unread-count` | 읽지 않은 알림 개수 조회 |
| PATCH | `/notifications/{notificationId}/read` | 알림 읽음 처리 |

### Admin

| Method | Endpoint | Description |
| --- | --- | --- |
| GET | `/admin/reports` | 신고 목록 조회 |
| PATCH | `/admin/reports/{reportId}/resolve` | 신고 승인 |
| PATCH | `/admin/reports/{reportId}/reject` | 신고 반려 |
| GET | `/admin/projects/hidden` | 숨김 처리된 프로젝트 목록 조회 |
| PATCH | `/admin/projects/{projectId}/unhide` | 프로젝트 숨김 해제 |
| GET | `/admin/portfolios/hidden` | 숨김 처리된 포트폴리오 목록 조회 |
| PATCH | `/admin/portfolios/{portfolioId}/unhide` | 포트폴리오 숨김 해제 |
| GET | `/admin/members` | 회원 검색 |
| PATCH | `/admin/members/{memberId}/suspend` | 회원 정지 |
| PATCH | `/admin/members/{memberId}/activate` | 회원 활성화 |

### AI

| Method | Endpoint | Description |
| --- | --- | --- |
| POST | `/ai/projectDescription` | 프로젝트 설명 초안 생성 |
| POST | `/ai/portfolioIntroduction` | 포트폴리오 소개글 초안 생성 |
| POST | `/ai/applicationMotivation` | 지원 동기 초안 생성 |

## 공통 API 응답 포맷

백엔드 API는 대부분 `RsData<T>` 형식으로 응답합니다. 응답은 `code`, `message`, `data` 구조를 사용하며, 프론트엔드 API 클라이언트는 성공 시 `data`를 반환합니다.

```json
{
  "code": "200",
  "message": "프로젝트 조회 성공",
  "data": {
    "id": 1,
    "title": "DevLink - 사이드 프로젝트 플랫폼"
  }
}
```

`data`가 필요 없는 응답에서는 생략되거나 `null`로 내려올 수 있습니다.

```json
{
  "code": "200",
  "message": "프로젝트가 삭제되었습니다."
}
```

에러 응답도 같은 형태를 사용합니다.

```json
{
  "code": "401",
  "message": "인증에 실패했습니다. 로그인이 필요합니다."
}
```

프론트엔드의 API 클라이언트는 응답의 `data`를 반환하고, HTTP 상태가 실패인 경우 `message`를 에러 메시지로 사용합니다.

## 개발 메모

- 프론트엔드는 `NEXT_PUBLIC_API_URL`을 기준으로 백엔드 API를 호출하며, 인증은 `access_token` HTTP-only 쿠키를 사용합니다.
- 로컬 HTTP 환경에서는 쿠키 인증 테스트를 위해 `app.cookie.secure: false` 설정이 필요합니다.
- `application-local.yml`, `application-secret.yml`, OAuth 설정 파일, Firebase 서비스 계정 파일은 Git에 포함하지 않습니다.
