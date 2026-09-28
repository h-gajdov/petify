function getBaseUrl(): string {
  const base = (import.meta.env.VITE_API_BASE_URL as string | undefined) ?? ''
  return base.replace(/\/$/, '')
}

function joinUrl(base: string, path: string): string {
  if (!base) return path
  return `${base}${path.startsWith('/') ? '' : '/'}${path}`
}

export async function addFavorite(userId: number, listingId: number): Promise<void> {
  const url = joinUrl(getBaseUrl(), `/api/favorites/${listingId}`)
  const response = await fetch(url, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'X-User-Id': String(userId),
    },
  })

  if (!response.ok) {
    const error = await response.json()
    throw new Error(error.error || 'Failed to add favorite')
  }
}

export async function removeFavorite(userId: number, listingId: number): Promise<void> {
  const url = joinUrl(getBaseUrl(), `/api/favorites/${listingId}`)
  const response = await fetch(url, {
    method: 'DELETE',
    headers: {
      'Content-Type': 'application/json',
      'X-User-Id': String(userId),
    },
  })

  if (!response.ok) {
    const error = await response.json()
    throw new Error(error.error || 'Failed to remove favorite')
  }
}

export async function getFavoritedListings(userId: number): Promise<any[]> {
  const url = joinUrl(getBaseUrl(), `/api/favorites`)
  const response = await fetch(url, {
    method: 'GET',
    headers: {
      'Content-Type': 'application/json',
      'X-User-Id': String(userId),
    },
  })

  if (!response.ok) {
    throw new Error('Failed to fetch favorites')
  }

  return await response.json()
}

export async function isFavorited(userId: number, listingId: number): Promise<boolean> {
  const url = joinUrl(getBaseUrl(), `/api/favorites/${listingId}/is-favorited`)
  const response = await fetch(url, {
    method: 'GET',
    headers: {
      'Content-Type': 'application/json',
      'X-User-Id': String(userId),
    },
  })

  if (!response.ok) {
    return false
  }

  const data = await response.json()
  return data.isFavorited
}
