function getBaseUrl(): string {
  const base = (import.meta.env.VITE_API_BASE_URL as string | undefined) ?? ''
  return base.replace(/\/$/, '')
}

function joinUrl(base: string, path: string): string {
  if (!base) return path
  return `${base}${path.startsWith('/') ? '' : '/'}${path}`
}

export async function getAllUsers(userId: number): Promise<any[]> {
  const url = joinUrl(getBaseUrl(), '/api/users/admin/all')
  const response = await fetch(url, {
    method: 'GET',
    headers: {
      'Content-Type': 'application/json',
      'X-User-Id': String(userId),
    },
  })

  if (!response.ok) {
    throw new Error('Failed to fetch all users')
  }

  return await response.json()
}

export interface AdminListingsPage {
  items: any[]
  page: number
  size: number
  totalItems: number
  totalPages: number
  hasNext: boolean
  hasPrevious: boolean
  activeListings: number
  soldListings: number
}

export interface AdminListingsFilters {
  status?: string
  minPrice?: string
  maxPrice?: string
}

export async function getAllListings(
  userId: number,
  page = 0,
  size = 500,
  filters: AdminListingsFilters = {}
): Promise<AdminListingsPage> {
  const params = new URLSearchParams({
    page: String(page),
    size: String(size),
  })
  if (filters.status) {
    params.set('status', filters.status)
  }
  if (filters.minPrice) {
    params.set('minPrice', filters.minPrice)
  }
  if (filters.maxPrice) {
    params.set('maxPrice', filters.maxPrice)
  }
  const url = joinUrl(getBaseUrl(), `/api/users/admin/listings?${params.toString()}`)
  const response = await fetch(url, {
    method: 'GET',
    headers: {
      'Content-Type': 'application/json',
      'X-User-Id': String(userId),
    },
  })

  if (!response.ok) {
    const text = await response.text()
    let apiError = ''
    try {
      apiError = JSON.parse(text).error || ''
    } catch {
      apiError = ''
    }
    throw new Error(apiError || `Failed to fetch listings: ${response.status} ${response.statusText}. ${text.slice(0, 300)}`)
  }

  return await response.json()
}

export async function blockUser(userId: number, targetUserId: number, isBlocked: boolean, reason?: string): Promise<void> {
  const url = joinUrl(getBaseUrl(), `/api/users/admin/${targetUserId}/block`)
  const response = await fetch(url, {
    method: 'PATCH',
    headers: {
      'Content-Type': 'application/json',
      'X-User-Id': String(userId),
    },
    body: JSON.stringify({ isBlocked, blockedReason: reason || '' }),
  })

  if (!response.ok) {
    throw new Error('Failed to update user status')
  }
}

export async function deleteUser(userId: number, targetUserId: number): Promise<void> {
  const url = joinUrl(getBaseUrl(), `/api/users/admin/${targetUserId}`)
  const response = await fetch(url, {
    method: 'DELETE',
    headers: {
      'Content-Type': 'application/json',
      'X-User-Id': String(userId),
    },
  })

  if (!response.ok) {
    throw new Error('Failed to delete user')
  }
}

export async function getClinicApplications(userId: number): Promise<any[]> {
  const url = joinUrl(getBaseUrl(), '/api/admin/clinic-applications')
  const response = await fetch(url, {
    method: 'GET',
    headers: {
      'Content-Type': 'application/json',
      'X-User-Id': String(userId),
    },
  })

  if (!response.ok) {
    throw new Error('Failed to fetch clinic applications')
  }

  return await response.json()
}

export async function getClinicsAdmin(userId: number): Promise<any[]> {
  const url = joinUrl(getBaseUrl(), '/api/clinics')
  const response = await fetch(url, {
    method: 'GET',
    headers: {
      'Content-Type': 'application/json',
      'X-User-Id': String(userId),
    },
  })

  if (!response.ok) {
    throw new Error('Failed to fetch clinics')
  }

  return await response.json()
}

export async function approveClinicApplication(userId: number, applicationId: number): Promise<any> {
  const url = joinUrl(getBaseUrl(), `/api/admin/clinic-applications/${applicationId}/approve`)
  const response = await fetch(url, {
    method: 'PATCH',
    headers: {
      'Content-Type': 'application/json',
      'X-User-Id': String(userId),
    },
  })

  if (!response.ok) {
    const error = await response.json()
    throw new Error(error.error || 'Failed to approve clinic application')
  }

  return await response.json()
}

export async function denyClinicApplication(userId: number, applicationId: number, denialReason: string): Promise<any> {
  const url = joinUrl(getBaseUrl(), `/api/admin/clinic-applications/${applicationId}/deny`)
  const response = await fetch(url, {
    method: 'PATCH',
    headers: {
      'Content-Type': 'application/json',
      'X-User-Id': String(userId),
    },
    body: JSON.stringify({ denialReason }),
  })

  if (!response.ok) {
    const error = await response.json()
    throw new Error(error.error || 'Failed to deny clinic application')
  }

  return await response.json()
}
