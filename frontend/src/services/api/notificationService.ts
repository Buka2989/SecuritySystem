import { apiRequest } from './client';

export interface NotificationItem {
  id: string;
  title: string;
  message: string;
  type: string;
  isRead: boolean;
  createdAt: string;
}

interface ApiNotification {
  id: string;
  title: string;
  message: string;
  type: string;
  is_read?: boolean;
  isRead?: boolean;
  created_at?: string;
  createdAt?: string;
}

function mapNotification(item: ApiNotification): NotificationItem {
  return {
    id: item.id,
    title: item.title,
    message: item.message,
    type: item.type,
    isRead: item.is_read ?? item.isRead ?? false,
    createdAt: item.created_at ?? item.createdAt ?? '',
  };
}

export async function getMyNotifications(
  token: string
): Promise<NotificationItem[]> {
  const result = await apiRequest<ApiNotification[]>(
    '/notifications/me',
    { token }
  );

  return result.map(mapNotification);
}

export async function markNotificationAsRead(
  id: string,
  token: string
): Promise<NotificationItem> {
  const result = await apiRequest<ApiNotification>(
    `/notifications/${id}/read`,
    {
      method: 'PATCH',
      token,
    }
  );

  return mapNotification(result);
}