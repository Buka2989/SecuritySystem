/** Shared JSON client; same-origin /api is proxied by Nginx or Vite. */

export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? '/api';

export class ApiError extends Error {
  status: number;

  constructor(message: string, status: number) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
  }
}

/** Render both domain errors and field-validation responses as readable messages. */
export async function responseError(response: Response): Promise<ApiError> {
  const text = await response.text().catch(() => '');
  let message = text || response.statusText || 'Request failed';
  try {
    const { detail } = JSON.parse(text);
    if (typeof detail === 'string') message = detail;
    else if (Array.isArray(detail)) {
      message = detail.map((error: { loc?: string[]; msg?: string }) => {
        const field = error.loc?.slice(1).join('.');
        return `${field ? `${field}: ` : ''}${error.msg || 'Invalid value'}`;
      }).join('; ');
    }
  } catch { /* Non-JSON proxy errors still retain their original message. */ }
  return new ApiError(message, response.status);
}

interface RequestOptions {
  method?: 'GET' | 'POST' | 'PATCH' | 'PUT' | 'DELETE';
  body?: unknown;
  token?: string | null;
}

export async function apiRequest<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const { method = 'GET', body, token } = options;

  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
  };
  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }

  const response = await fetch(`${API_BASE_URL}${path}`, {
    method,
    headers,
    body: body ? JSON.stringify(body) : undefined,
  });

  if (!response.ok) {
    throw await responseError(response);
  }

  if (response.status === 204) {
    return undefined as T;
  }

  return (await response.json()) as T;
}
