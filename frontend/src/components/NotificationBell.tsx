'use client';

import { useEffect, useRef, useState } from 'react';
import { Bell } from 'lucide-react';
import { useRouter } from 'next/navigation';
import {
  fetchMyNotifications,
  fetchUnreadNotificationCount,
  markNotificationAsRead,
} from '../lib/api';
import type { NotificationResponse } from '../types';

export function NotificationBell() {
  const [isOpen, setIsOpen] = useState(false);
  const [notifications, setNotifications] = useState<NotificationResponse[]>([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const containerRef = useRef<HTMLDivElement>(null);
  const router = useRouter();

  useEffect(() => {
    const loadUnreadCount = () => {
      fetchUnreadNotificationCount()
        .then(setUnreadCount)
        .catch(() => {});
    };

    loadUnreadCount();
    const interval = setInterval(loadUnreadCount, 15000);
    return () => clearInterval(interval);
  }, []);

  useEffect(() => {
    function handleClickOutside(event: MouseEvent) {
      if (containerRef.current && !containerRef.current.contains(event.target as Node)) {
        setIsOpen(false);
      }
    }
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const handleToggle = () => {
    if (!isOpen) {
      fetchMyNotifications()
        .then(setNotifications)
        .catch(() => {});
    }
    setIsOpen((prev) => !prev);
  };

  const handleNotificationClick = (notification: NotificationResponse) => {
    setIsOpen(false);

    if (!notification.isRead) {
      markNotificationAsRead(notification.id)
        .then(() => {
          setUnreadCount((prev) => Math.max(0, prev - 1));
          setNotifications((prev) =>
            prev.map((n) =>
              n.id === notification.id ? { ...n, isRead: true } : n,
            ),
          );
        })
        .catch(() => {});
    }

    if (notification.targetUrl) {
      router.push(notification.targetUrl);
    }
  };

  return (
    <div className="relative" ref={containerRef}>
      <button
        onClick={handleToggle}
        className="relative text-slate-500 hover:text-slate-900 transition-colors"
      >
        <Bell className="h-5 w-5" />
        {unreadCount > 0 && (
          <span className="absolute -top-1 -right-1 flex h-4 min-w-[16px] items-center justify-center rounded-full bg-red-500 px-1 text-[10px] font-medium text-white">
            {unreadCount > 9 ? '9+' : unreadCount}
          </span>
        )}
      </button>

      {isOpen && (
        <div className="absolute right-0 mt-2 w-80 rounded-lg border border-slate-200 bg-white shadow-lg z-50">
          <div className="px-4 py-3 border-b border-slate-100">
            <span className="text-sm font-semibold text-slate-900">알림</span>
          </div>
          <div className="max-h-96 overflow-y-auto">
            {notifications.length === 0 ? (
              <div className="px-4 py-8 text-center text-sm text-slate-400">
                알림이 없습니다.
              </div>
            ) : (
              notifications.map((notification) => (
                <button
                  key={notification.id}
                  onClick={() => handleNotificationClick(notification)}
                  className={`w-full text-left px-4 py-3 border-b border-slate-50 hover:bg-slate-50 transition-colors ${
                    notification.isRead ? 'bg-white' : 'bg-blue-50/50'
                  }`}
                >
                  <p className="text-sm font-medium text-slate-900">
                    {notification.title}
                  </p>
                  <p className="text-sm text-slate-500 mt-0.5">
                    {notification.message}
                  </p>
                </button>
              ))
            )}
          </div>
        </div>
      )}
    </div>
  );
}
