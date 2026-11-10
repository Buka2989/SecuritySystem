import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { useAuth } from '@/hooks/useAuth';
import {
  getMyNotifications,
  markNotificationAsRead,
  type NotificationItem,
} from '@/services/api/notificationService';

function formatDate(value: string): string {
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return '';
  return new Intl.DateTimeFormat('ru-RU', {
    day: '2-digit',
    month: 'short',
    hour: '2-digit',
    minute: '2-digit',
  }).format(date);
}

export function NotificationCenter() {
  const { token } = useAuth();
  const [items, setItems] = useState<NotificationItem[]>([]);
  const [isOpen, setIsOpen] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const rootRef = useRef<HTMLDivElement>(null);

  const loadNotifications = useCallback(async (showLoader = false) => {
    if (!token) return;
    if (showLoader) setIsLoading(true);
    try {
      const result = await getMyNotifications(token);
      setItems(result);
      setError(null);
    } catch {
      setError('Не удалось загрузить уведомления. Попробуйте обновить.');
    } finally {
      if (showLoader) setIsLoading(false);
    }
  }, [token]);

  useEffect(() => {
    void loadNotifications(true);
    const intervalId = window.setInterval(() => void loadNotifications(), 30000);
    return () => window.clearInterval(intervalId);
  }, [loadNotifications]);

  useEffect(() => {
    function handlePointerDown(event: MouseEvent) {
      if (rootRef.current && !rootRef.current.contains(event.target as Node)) {
        setIsOpen(false);
      }
    }
    function handleEscape(event: KeyboardEvent) {
      if (event.key === 'Escape') setIsOpen(false);
    }
    document.addEventListener('mousedown', handlePointerDown);
    document.addEventListener('keydown', handleEscape);
    return () => {
      document.removeEventListener('mousedown', handlePointerDown);
      document.removeEventListener('keydown', handleEscape);
    };
  }, []);

  const unreadCount = useMemo(() => items.filter((item) => !item.isRead).length, [items]);

  async function handleMarkRead(item: NotificationItem) {
    if (!token || item.isRead) return;
    try {
      const updated = await markNotificationAsRead(item.id, token);
      setItems((current) => current.map((entry) => entry.id === item.id ? updated : entry));
    } catch {
      setError('Не удалось отметить уведомление прочитанным.');
    }
  }

  return (
    <div className="notification-center" ref={rootRef}>
      <button
        type="button"
        className={`notification-trigger${isOpen ? ' is-open' : ''}`}
        aria-label={`Уведомления, непрочитанных: ${unreadCount}`}
        aria-expanded={isOpen}
        onClick={() => {
          const next = !isOpen;
          setIsOpen(next);
          if (next) void loadNotifications(true);
        }}
      >
        <svg viewBox="0 0 24 24" aria-hidden="true" className="notification-bell-icon">
          <path d="M18 8a6 6 0 0 0-12 0c0 7-3 7-3 9h18c0-2-3-2-3-9M10 21h4" />
        </svg>
        <span className="notification-trigger__label">Уведомления</span>
        {unreadCount > 0 && <span className="notification-count">{unreadCount > 99 ? '99+' : unreadCount}</span>}
      </button>

      {isOpen && (
        <section className="notification-panel" aria-label="Центр уведомлений">
          <div className="notification-panel__header">
            <div>
              <h2>Уведомления</h2>
              <p>{unreadCount ? `${unreadCount} непрочитанных` : 'Все уведомления прочитаны'}</p>
            </div>
            <button type="button" className="notification-refresh" onClick={() => void loadNotifications(true)} aria-label="Обновить уведомления">↻</button>
          </div>

          {error && <p className="notification-error" role="alert">{error}</p>}
          {isLoading && items.length === 0 ? (
            <p className="notification-empty">Загрузка...</p>
          ) : items.length === 0 ? (
            <div className="notification-empty">
              <span className="notification-empty__icon">♧</span>
              <strong>Пока нет уведомлений</strong>
              <span>Здесь появятся обновления по вашим заявкам.</span>
            </div>
          ) : (
            <ul className="notification-list">
              {items.map((item) => (
                <li key={item.id} className={`notification-item${item.isRead ? '' : ' is-unread'}`}>
                  <span className="notification-item__dot" aria-hidden="true" />
                  <div className="notification-item__content">
                    <div className="notification-item__title-row">
                      <strong>{item.title}</strong>
                      <time>{formatDate(item.createdAt)}</time>
                    </div>
                    <p>{item.message}</p>
                    {!item.isRead && (
                      <button type="button" className="notification-mark-read" onClick={() => void handleMarkRead(item)}>
                        Отметить прочитанным
                      </button>
                    )}
                  </div>
                </li>
              ))}
            </ul>
          )}
        </section>
      )}
    </div>
  );
}
