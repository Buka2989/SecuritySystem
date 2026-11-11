import { useEffect, useState } from 'react';
import { apiRequest } from '@/services/api';
import { useAuth } from '@/hooks/useAuth';

interface Notice {
  id: string;
  status: string;
  comment: string | null;
}

function readSeenIds(key: string): string[] {
  try {
    const stored: unknown = JSON.parse(localStorage.getItem(key) ?? '[]');
    return Array.isArray(stored) ? stored.filter((id): id is string => typeof id === 'string') : [];
  } catch {
    return [];
  }
}

export function ReviewNotifications() {
  const { token, user } = useAuth();
  const accountId = user?.id;
  const role = user?.role;
  const [banner, setBanner] = useState<{ accountId: string; notice: Notice } | null>(null);

  useEffect(() => {
    setBanner(null);
    if (!token || !accountId || (role !== 'PARENT' && role !== 'TRUSTED_PERSON')) return;

    const owner = accountId;
    const storageKey = `security-system.seen-review-notices.${owner}`;
    let seenIds = new Set(readSeenIds(storageKey));
    let stopped = false;
    let polling = false;

    async function poll() {
      if (polling) return;
      polling = true;
      try {
        const items = await apiRequest<Notice[]>('/notifications', { token });
        if (stopped) return;

        const hasUpdates = items.some((notice) => !seenIds.has(notice.id));
        for (const id of readSeenIds(storageKey)) seenIds.add(id);
        const fresh = items.filter((notice) => !seenIds.has(notice.id));
        seenIds = new Set(items.map((notice) => notice.id));
        if (hasUpdates) window.dispatchEvent(new Event('verification-updated'));
        const latest = fresh[fresh.length - 1];
        if (!latest) return;

        try {
          localStorage.setItem(storageKey, JSON.stringify([...seenIds]));
        } catch {
          // Keep suppressing duplicates for the current session if storage is unavailable.
        }
        setBanner({ accountId: owner, notice: latest });
      } catch {
        // The dashboard still loads persisted verification status independently.
      } finally {
        polling = false;
      }
    }

    void poll();
    const timer = window.setInterval(() => void poll(), 3000);
    return () => {
      stopped = true;
      window.clearInterval(timer);
    };
  }, [accountId, role, token]);

  if (!banner || banner.accountId !== accountId) return null;
  const { notice } = banner;
  return (
    <div className="notice" role="status">
      {notice.status === 'APPROVED' ? 'Связь с учеником подтверждена.' : 'Заявка отклонена.'}
      {notice.comment && ` ${notice.comment}`}
      <button className="btn-secondary" onClick={() => setBanner(null)}>Закрыть уведомление</button>
    </div>
  );
}
