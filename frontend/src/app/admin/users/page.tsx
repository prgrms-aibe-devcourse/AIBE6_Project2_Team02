'use client'

import { motion } from 'framer-motion'
import { useState } from 'react'
import { toast } from 'sonner'

import {
  AlertCircle,
  Calendar,
  CheckCircle2,
  Search,
  ShieldAlert,
  User as UserIcon,
  X,
} from 'lucide-react'

import { Badge, Button, Card } from '../../../components/ui'
import { activateMember, searchMembers, suspendMember } from '../../../lib/api'
import { formatDate } from '../../../lib/date'
import type { User } from '../../../types'

export default function AdminUsersPage() {
  const [users, setUsers] = useState<User[]>([])
  const [hasSearched, setHasSearched] = useState(false)
  const [loading, setLoading] = useState(false)
  const [activeTab, setActiveTab] = useState<'all' | 'suspended'>('all')
  const [searchKeyword, setSearchKeyword] = useState('')
  const [suspensionDays, setSuspensionDays] = useState<Record<string, number>>(
    {},
  )

  const loadData = async (keyword?: string) => {
    setLoading(true)

    try {
      const data = await searchMembers(keyword)

      const mappedUsers: User[] = (data || []).map((m: any) => ({
        id: String(m.id),
        name: m.nickname,
        avatar: m.profileImageUrl,
        status: m.status,
        suspensionUntil: m.suspensionUntil,
        role: 'USER',
      }))

      setUsers(mappedUsers)
    } catch (err) {
      console.error('Failed to load users:', err)
      toast.error('유저 목록을 불러오는데 실패했습니다.')
    } finally {
      setLoading(false)
    }
  }

  const handleSearch = async (e: React.FormEvent) => {
    e.preventDefault()

    const keyword = searchKeyword.trim()

    if (!keyword) {
      toast.error('검색어를 입력해주세요.')
      return
    }

    setHasSearched(true)
    await loadData(keyword)
  }

  const handleSuspend = async (userId: string) => {
    const days = suspensionDays[userId] || 7
    if (!confirm(`${days}일간 해당 유저의 활동을 제한하시겠습니까?`)) return

    try {
      await suspendMember(userId, days)
      toast.success('유저 정지 기한이 업데이트되었습니다.')
      loadData(searchKeyword)
    } catch (err) {
      console.error('Failed to suspend user:', err)
      toast.error('유저 정지에 실패했습니다.')
    }
  }

  const handleActivate = async (userId: string) => {
    if (!confirm('해당 유저의 정지 기한을 초기화하시겠습니까?')) return

    try {
      await activateMember(userId)
      toast.success('유저 정지가 해제되었습니다.')
      loadData(searchKeyword)
    } catch (err) {
      console.error('Failed to activate user:', err)
      toast.error('정지 해제에 실패했습니다.')
    }
  }

  const filteredUsers = users.filter((user) => {
    if (activeTab === 'all') return true
    return user.status === 'SUSPENDED' || user.status === 'BANNED'
  })

  const suspendedCount = users.filter(
    (u) => u.status === 'SUSPENDED' || u.status === 'BANNED',
  ).length

  if (loading && users.length === 0) {
    return (
      <div className="container mx-auto px-4 py-20 text-center text-slate-500">
        유저 정보를 불러오는 중...
      </div>
    )
  }

  return (
    <div className="container mx-auto px-4 py-8 max-w-6xl">
      <div className="mb-8 flex items-center gap-3">
        <div className="w-12 h-12 rounded-xl bg-blue-100 text-blue-600 flex items-center justify-center">
          <UserIcon className="w-6 h-6" />
        </div>
        <div>
          <h1 className="text-2xl font-bold text-slate-900">유저 관리</h1>
          <p className="text-slate-500 text-sm mt-1">
            유저 활동을 모니터링하고 이용 제한 기한을 관리합니다.
          </p>
        </div>
      </div>

      <div className="mb-8 flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div className="flex gap-2 p-1 bg-slate-100 rounded-lg w-fit">
          <button
            onClick={() => setActiveTab('all')}
            className={`px-4 py-2 rounded-md text-sm font-medium transition-colors ${
              activeTab === 'all'
                ? 'bg-white text-slate-900 shadow-sm'
                : 'text-slate-500 hover:text-slate-700'
            }`}
          >
            전체 유저 ({users.length})
          </button>
          <button
            onClick={() => setActiveTab('suspended')}
            className={`px-4 py-2 rounded-md text-sm font-medium transition-colors ${
              activeTab === 'suspended'
                ? 'bg-white text-slate-900 shadow-sm'
                : 'text-slate-500 hover:text-slate-700'
            }`}
          >
            정지된 유저 ({suspendedCount})
          </button>
        </div>

        <form
          onSubmit={handleSearch}
          className="relative flex items-center w-full max-w-md"
        >
          <div className="absolute left-3 text-slate-400">
            <Search size={18} />
          </div>
          <input
            type="text"
            placeholder="닉네임으로 유저 검색"
            value={searchKeyword}
            onChange={(e) => setSearchKeyword(e.target.value)}
            className="w-full pl-10 pr-10 py-2.5 bg-white border border-slate-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 transition-all shadow-sm"
          />
          {searchKeyword && (
            <button
              type="button"
              onClick={() => {
                setSearchKeyword('')
                setUsers([])
                setHasSearched(false)
              }}
              className="absolute right-3 text-slate-400 hover:text-slate-600 transition-colors"
            >
              <X size={16} />
            </button>
          )}
        </form>
      </div>

      <div className="text-sm font-medium text-slate-500 mb-6">
        {activeTab === 'all' ? '전체' : '정지 유저'} 검색 결과:{' '}
        <span className="text-slate-900 font-bold">{filteredUsers.length}</span>
        명
      </div>

      {!hasSearched ? (
        <div className="text-center py-20 bg-white rounded-2xl border border-slate-200 border-dashed">
          <div className="w-16 h-16 bg-slate-50 text-slate-300 rounded-full flex items-center justify-center mx-auto mb-4">
            <Search size={32} />
          </div>
          <p className="text-slate-500 font-medium">
            닉네임을 입력하고 검색해주세요.
          </p>
        </div>
      ) : filteredUsers.length === 0 ? (
        <div className="text-center py-20 bg-white rounded-2xl border border-slate-200 border-dashed">
          <div className="w-16 h-16 bg-slate-50 text-slate-300 rounded-full flex items-center justify-center mx-auto mb-4">
            <AlertCircle size={32} />
          </div>
          <p className="text-slate-500 font-medium">
            검색 결과와 일치하는 유저가 없습니다.
          </p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {filteredUsers.map((user) => {
            const suspended = user.status === 'SUSPENDED'
            const banned = user.status === 'BANNED'
            return (
              <motion.div
                key={user.id}
                initial={{ opacity: 0, y: 10 }}
                animate={{ opacity: 1, y: 0 }}
              >
                <Card
                  className={`overflow-hidden border-slate-200 hover:shadow-md transition-all h-full flex flex-col ${suspended || banned ? 'border-red-200 bg-red-50/30' : ''}`}
                >
                  <div className="p-5 flex-1 flex flex-col">
                    <div className="flex justify-between items-start mb-4">
                      <Badge
                        variant={suspended || banned ? 'outline' : 'secondary'}
                        className={
                          suspended || banned
                            ? 'bg-red-50 text-red-700 border-red-100'
                            : 'bg-slate-100 text-slate-600'
                        }
                      >
                        {suspended || banned ? '활동 제한 중' : '정상 활동'}
                      </Badge>
                    </div>

                    <div className="flex items-center gap-4 mb-6">
                      <div className="w-14 h-14 rounded-full overflow-hidden border-2 border-white shadow-sm bg-slate-100 flex-shrink-0">
                        {user.avatar ? (
                          <img
                            src={user.avatar}
                            alt={user.name}
                            className="w-full h-full object-cover"
                          />
                        ) : (
                          <div className="w-full h-full flex items-center justify-center text-slate-400">
                            <UserIcon size={24} />
                          </div>
                        )}
                      </div>
                      <div className="min-w-0">
                        <h3 className="font-bold text-slate-900 text-lg truncate">
                          {user.name}
                        </h3>
                        <p className="text-xs text-slate-500 truncate">
                          {user.bio || '등록된 소개가 없습니다.'}
                        </p>
                      </div>
                    </div>

                    {suspended && user.suspensionUntil && (
                      <div className="mb-6 p-3 bg-red-50 rounded-xl border border-red-100 flex items-center gap-3">
                        <Calendar className="w-4 h-4 text-red-500 flex-shrink-0" />
                        <div className="min-w-0">
                          <div className="text-[10px] text-red-600 font-bold uppercase tracking-wider">
                            제한 기한
                          </div>
                          <div className="text-xs text-red-700 font-semibold truncate">
                            {formatDate(user.suspensionUntil)}까지
                          </div>
                        </div>
                      </div>
                    )}

                    {banned && (
                      <div className="mb-6 p-3 bg-red-50 rounded-xl border border-red-100 flex items-center gap-3">
                        <Calendar className="w-4 h-4 text-red-500 flex-shrink-0" />
                        <div className="min-w-0">
                          <div className="text-[10px] text-red-600 font-bold uppercase tracking-wider">
                            제한 기한
                          </div>
                          <div className="text-xs text-red-700 font-semibold truncate">
                            영구정지
                          </div>
                        </div>
                      </div>
                    )}

                    <div className="mt-auto space-y-3">
                      <div className="flex items-center gap-2">
                        <select
                          className="flex-1 h-9 px-3 bg-white border border-slate-200 rounded-lg text-xs focus:outline-none focus:ring-2 focus:ring-blue-500/20 transition-all"
                          value={suspensionDays[user.id] || 7}
                          onChange={(e) =>
                            setSuspensionDays({
                              ...suspensionDays,
                              [user.id]: parseInt(e.target.value),
                            })
                          }
                        >
                          <option value={1}>1일 정지</option>
                          <option value={3}>3일 정지</option>
                          <option value={7}>7일 정지</option>
                          <option value={14}>14일 정지</option>
                          <option value={30}>30일 정지</option>
                          <option value={999}>영구 정지</option>
                        </select>
                        <Button
                          variant={suspended || banned ? 'outline' : 'default'}
                          size="sm"
                          className={`h-9 px-4 text-xs font-bold ${suspended || banned ? 'border-red-200 text-red-600 hover:bg-red-50' : 'bg-red-600 hover:bg-red-700 text-white border-none'}`}
                          onClick={() => handleSuspend(user.id)}
                        >
                          <ShieldAlert size={14} className="mr-1.5" />
                          {suspended || banned ? '기한 변경' : '활동 제한'}
                        </Button>
                      </div>

                      {(suspended || banned) && (
                        <Button
                          variant="ghost"
                          size="sm"
                          className="w-full h-9 text-xs text-emerald-600 hover:bg-emerald-50 hover:text-emerald-700"
                          onClick={() => handleActivate(user.id)}
                        >
                          <CheckCircle2 size={14} className="mr-1.5" />
                          제한 즉시 해제
                        </Button>
                      )}
                    </div>
                  </div>
                </Card>
              </motion.div>
            )
          })}
        </div>
      )}
    </div>
  )
}
