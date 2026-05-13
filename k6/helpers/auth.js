import http from 'k6/http';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

/**
 * 로그인 후 accessToken 반환
 * 사용: const token = login('user@test.com', 'password123');
 */
export function login(email, password) {
  const res = http.post(
    `${BASE_URL}/api/auth/login`,
    JSON.stringify({ email, password }),
    { headers: { 'Content-Type': 'application/json' } }
  );

  if (res.status !== 200) {
    console.error(`[auth] 로그인 실패 (${res.status}): ${res.body}`);
    return null;
  }

  let body;
  try {
    body = JSON.parse(res.body);
  } catch (e) {
    throw new Error(`[auth] 응답 파싱 실패 (status=${res.status}): ${res.body}`);
  }

  if (!body || !body.data || !body.data.accessToken) {
    throw new Error(`[auth] accessToken 없음 (status=${res.status}): ${res.body}`);
  }

  return body.data.accessToken;
}

/**
 * Authorization 헤더 반환
 * 사용: authHeader(token)
 */
export function authHeader(token) {
  if (!token || typeof token !== 'string' || !token.trim()) {
    throw new Error('authHeader: missing or empty token');
  }
  return { Authorization: `Bearer ${token}` };
}

export { BASE_URL };
