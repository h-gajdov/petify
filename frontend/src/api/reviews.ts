function getBaseUrl(): string {
  const base = (import.meta.env.VITE_API_BASE_URL as string | undefined) ?? ''
  return base.replace(/\/$/, '')
}

function joinUrl(base: string, path: string): string {
  if (!base) return path
  return `${base}${path.startsWith('/') ? '' : '/'}${path}`
}

export interface Review {
  reviewId: number
  reviewerId: number
  reviewerName: string
  reviewerUsername: string
  rating: number
  comment: string
  createdAt: string
  updatedAt?: string
}

export async function createReview(
  targetUserId: number,
  userId: number,
  rating: number,
  comment: string
): Promise<Review> {
  const url = joinUrl(getBaseUrl(), `/api/reviews/${targetUserId}`)
  const response = await fetch(url, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'X-User-Id': String(userId),
    },
    body: JSON.stringify({
      rating,
      comment,
    }),
  })

  if (!response.ok) {
    const error = await response.json()
    throw new Error(error.error || 'Failed to create review')
  }

  return await response.json()
}

export async function getReviewsByOwner(targetUserId: number): Promise<Review[]> {
  const url = joinUrl(getBaseUrl(), `/api/reviews/${targetUserId}`)
  const response = await fetch(url, {
    method: 'GET',
    headers: {
      'Content-Type': 'application/json',
    },
  })

  if (!response.ok) {
    const text = await response.text()
    throw new Error(`Failed to fetch reviews: ${response.status} ${response.statusText}. ${text.slice(0, 160)}`)
  }

  return await readJsonResponse<Review[]>(response, 'reviews')
}

export async function getReviewsLeftByUser(reviewerId: number): Promise<Review[]> {
  const url = joinUrl(getBaseUrl(), `/api/reviews/by/${reviewerId}`)
  const response = await fetch(url, {
    method: 'GET',
    headers: {
      'Content-Type': 'application/json',
    },
  })

  if (!response.ok) {
    const text = await response.text()
    throw new Error(`Failed to fetch reviews left by user: ${response.status} ${response.statusText}. ${text.slice(0, 160)}`)
  }

  return await readJsonResponse<Review[]>(response, 'reviews left by user')
}

export async function getReviewsByClinic(clinicId: number): Promise<Review[]> {
  const url = joinUrl(getBaseUrl(), `/api/reviews/clinics/${clinicId}`)
  const response = await fetch(url, {
    method: 'GET',
    headers: {
      'Content-Type': 'application/json',
    },
  })

  if (!response.ok) {
    throw new Error('Failed to fetch clinic reviews')
  }

  return await response.json()
}

export async function createClinicReview(
  clinicId: number,
  userId: number,
  rating: number,
  comment: string
): Promise<Review> {
  const url = joinUrl(getBaseUrl(), `/api/reviews/clinics/${clinicId}`)
  const response = await fetch(url, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'X-User-Id': String(userId),
    },
    body: JSON.stringify({
      rating,
      comment,
    }),
  })

  if (!response.ok) {
    const error = await response.json()
    throw new Error(error.error || 'Failed to create clinic review')
  }

  return await response.json()
}

export async function getMyClinicReview(clinicId: number, userId: number): Promise<Review | null> {
  const url = joinUrl(getBaseUrl(), `/api/reviews/clinics/${clinicId}/mine`)
  const response = await fetch(url, {
    method: 'GET',
    headers: {
      'Content-Type': 'application/json',
      'X-User-Id': String(userId),
    },
  })

  if (response.status === 204) {
    return null
  }

  if (!response.ok) {
    const error = await response.json()
    throw new Error(error.error || 'Failed to fetch clinic review')
  }

  return await response.json()
}

export async function updateReview(
  reviewId: number,
  userId: number,
  rating: number,
  comment: string
): Promise<Review> {
  const url = joinUrl(getBaseUrl(), `/api/reviews/${reviewId}`)
  const response = await fetch(url, {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
      'X-User-Id': String(userId),
    },
    body: JSON.stringify({
      rating,
      comment,
    }),
  })

  if (!response.ok) {
    const error = await response.json()
    throw new Error(error.error || 'Failed to update review')
  }

  return await response.json()
}

export async function deleteReview(reviewId: number, userId: number): Promise<void> {
  const url = joinUrl(getBaseUrl(), `/api/reviews/${reviewId}`)
  const response = await fetch(url, {
    method: 'DELETE',
    headers: {
      'Content-Type': 'application/json',
      'X-User-Id': String(userId),
    },
  })

  if (!response.ok) {
    const error = await response.json()
    throw new Error(error.error || 'Failed to delete review')
  }
}

async function readJsonResponse<T>(response: Response, label: string): Promise<T> {
  const contentType = response.headers.get('content-type') || ''
  const text = await response.text()

  if (!contentType.includes('application/json')) {
    throw new Error(`Expected JSON for ${label}, but backend returned non-JSON. ${text.slice(0, 160)}`)
  }

  return JSON.parse(text) as T
}
