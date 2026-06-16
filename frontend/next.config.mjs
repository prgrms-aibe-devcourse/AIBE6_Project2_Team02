/** @type {import('next').NextConfig} */
const nextConfig = {
  reactStrictMode: true,

  // 백엔드 스프링 부트 서버(8080)와 주소를 매핑해 주는 프록시 설정 추가
  async rewrites() {
    return [
      {
        // 프론트에서 /auth로 보내는 모든 요청을 백엔드로 포워딩
        source: '/auth/:path*',
        destination: 'http://localhost:8080/auth/:path*',
      },
      {
        source: '/mypage/:path*',
        destination: 'http://localhost:8080/mypage/:path*',
      },
      {
        source: '/portfolios/:path*',
        destination: 'http://localhost:8080/portfolios/:path*',
      },
      {
        source: '/projects/:path*',
        destination: 'http://localhost:8080/projects/:path*',
      },
    ];
  },
};

export default nextConfig;