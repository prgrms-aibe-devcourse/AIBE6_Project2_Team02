/** @type {import('next').NextConfig} */

const BACKEND_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080'

const nextConfig = {
  reactStrictMode: true,

  async rewrites() {
    return {
      fallback: [
        {
          source: '/auth/:path*',
          destination: `${BACKEND_URL}/auth/:path*`,
        },
        {
          source: '/mypage/:path*',
          destination: `${BACKEND_URL}/mypage/:path*`,
        },
        {
          source: '/portfolios/:path*',
          destination: `${BACKEND_URL}/portfolios/:path*`,
        },
        {
          source: '/projects/:path*',
          destination: `${BACKEND_URL}/projects/:path*`,
        },
      ],
    }
  },
};

export default nextConfig;
