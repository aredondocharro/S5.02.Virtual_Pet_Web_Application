// src/api/client.ts
const API_BASE = import.meta.env.DEV ? 'http://localhost:8080' : '';

function getToken() {
  return localStorage.getItem('token');
}

function goLoginIf401(status: number) {
  if (status === 401) {
    // limpia sesión y redirige
    localStorage.removeItem('token');
    window.location.href = '/login?reason=expired';
  }
}

async function handle(res: Response) {
  if (!res.ok) {
    goLoginIf401(res.status);

    let msg = `${res.status} ${res.statusText}`;
    try {
      const data = await res.json();
      if (data?.message) msg = data.message;     // tu backend suele mandar { message: ... }
      else if (data?.error) msg = data.error;
    } catch {}
    throw new Error(msg);
  }

  // 204 sin body
  const text = await res.text();
  return text ? JSON.parse(text) : null;
}

export async function apiGet<T>(path: string): Promise<T> {
  const res = await fetch(`${API_BASE}${path}`, {
    headers: { 'Authorization': `Bearer ${getToken() ?? ''}` },
  });
  return handle(res);
}
export async function apiPost<T>(path: string, body?: unknown, authorized = true): Promise<T> {
  const headers: Record<string, string> = { 'Content-Type': 'application/json' };
  if (authorized) headers['Authorization'] = `Bearer ${getToken() ?? ''}`;
  const res = await fetch(`${API_BASE}${path}`, { method: 'POST', headers, body: body ? JSON.stringify(body) : undefined });
  return handle(res);
}
export async function apiPut<T>(path: string, body: unknown): Promise<T> {
  const res = await fetch(`${API_BASE}${path}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${getToken() ?? ''}` },
    body: JSON.stringify(body),
  });
  return handle(res);
}
export async function apiDelete(path: string): Promise<void> {
  const res = await fetch(`${API_BASE}${path}`, {
    method: 'DELETE',
    headers: { 'Authorization': `Bearer ${getToken() ?? ''}` },
  });
  await handle(res);
}
