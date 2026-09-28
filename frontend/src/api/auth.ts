export interface LoginRequest {
  username: string
  password: string
}

export interface SignupRequest {
  username: string
  email: string
  password: string
  firstName: string
  lastName: string
}

export interface AuthResult {
  token?: string
  user?: {
    userId: number
    username: string
    email: string
    firstName: string
    lastName: string
    userType: string
    verified: boolean
  }
  message?: string
}

function getBaseUrl(): string {
  const base = (import.meta.env.VITE_API_BASE_URL as string | undefined) ?? ''
  return base.replace(/\/$/, '')
}

function joinUrl(base: string, path: string): string {
  if (!base) return path
  return `${base}${path.startsWith('/') ? '' : '/'}${path}`
}

function extractToken(data: unknown): string | null {
  if (!data || typeof data !== 'object') return null
  const rec = data as Record<string, unknown>
  const candidates = [rec.token, rec.accessToken, rec.jwt, rec.idToken]
  for (const c of candidates) {
    if (typeof c === 'string' && c.trim()) return c
  }
  // Sometimes APIs wrap: { data: { token: '...' } }
  const nested = rec.data
  if (nested && typeof nested === 'object') {
    const n = nested as Record<string, unknown>
    const nestedCandidates = [n.token, n.accessToken, n.jwt, n.idToken]
    for (const c of nestedCandidates) {
      if (typeof c === 'string' && c.trim()) return c
    }
  }
  return null
}

async function postJson<T>(path: string, body: unknown, options?: { signal?: AbortSignal }): Promise<T> {
  const url = joinUrl(getBaseUrl(), path)
  const res = await fetch(url, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Accept: 'application/json',
    },
    body: JSON.stringify(body),
    signal: options?.signal,
  })

  const text = await res.text()
  let json: unknown = null
  try {
    json = text ? (JSON.parse(text) as unknown) : null
  } catch {
    json = null
  }

  if (!res.ok) {
    const message =
      (json && typeof json === 'object' && typeof (json as any).message === 'string' && (json as any).message) ||
      text ||
      `Request failed (${res.status})`
    throw new Error(message)
  }

  return json as T
}

export async function login(payload: LoginRequest, options?: { signal?: AbortSignal }): Promise<AuthResult> {
  const data = await postJson<any>('/api/auth/login', payload, options)

  // Check if there's an error message
  if (data.message && data.message !== "Login successful") {
    throw new Error(data.message)
  }

  // Extract user information from the response
  if (data.userId && data.username) {
    return {
      user: {
        userId: data.userId,
        username: data.username,
        email: data.email,
        firstName: data.firstName,
        lastName: data.lastName,
        userType: data.userType,
        verified: data.verified || false  // Use verified status from API response
      },
      message: data.message
    }
  }

  // If we don't get user data, something went wrong
  throw new Error('Login failed: Invalid response from server')
}

export async function signup(payload: SignupRequest, options?: { signal?: AbortSignal }): Promise<AuthResult> {
  const data = await postJson<any>('/api/auth/signup', payload, options)

  // Check if there's an error message
  if (data.message && data.message !== "User registered successfully") {
    throw new Error(data.message)
  }

  // Extract user information from the response if successful
  if (data.userId && data.username) {
    return {
      user: {
        userId: data.userId,
        username: data.username,
        email: data.email,
        firstName: data.firstName,
        lastName: data.lastName,
        userType: data.userType,
        verified: data.verified || false  // Use verified status from API response
      },
      message: data.message
    }
  }

  // If registration was successful but no user data returned, that's okay
  return { message: data.message || "Registration successful" }
}
