import type { Listing } from '../types/listing'

function getBaseUrl(): string {
  const base = (import.meta.env.VITE_API_BASE_URL as string | undefined) ?? ''
  return base.replace(/\/$/, '')
}

function joinUrl(base: string, path: string): string {
  if (!base) return path
  return `${base}${path.startsWith('/') ? '' : '/'}${path}`
}

type UnknownRecord = Record<string, unknown>

function toNumber(value: unknown): number | undefined {
  if (value === null || value === undefined || value === '') return undefined
  if (typeof value === 'number' && Number.isFinite(value)) return value
  const n = Number(value)
  return Number.isFinite(n) ? n : undefined
}

function toString(value: unknown): string | undefined {
  if (value === null || value === undefined) return undefined
  const s = String(value)
  return s ? s : undefined
}

function mapStatus(status: string | undefined): string | undefined {
  if (!status) return undefined
  switch (status) {
    case 'DRAFT':
      return 'Draft'
    case 'ACTIVE':
      return 'Active'
    case 'SOLD':
      return 'Sold'
    case 'ARCHIVED':
      return 'Archived'
    default:
      return status
  }
}

function titleFromDescription(description: string | undefined): string | undefined {
  if (!description) return undefined
  const firstLine = description.split(/\r?\n/)[0]?.trim()
  if (!firstLine) return undefined
  // Use a short, readable title.
  const max = 52
  return firstLine.length > max ? `${firstLine.slice(0, max - 1).trimEnd()}…` : firstLine
}

function normalizeListingRow(row: unknown): Listing | null {
  if (!row || typeof row !== 'object') return null
  const r = row as UnknownRecord

  const listingId =
    toNumber(r.listing_id) ?? toNumber(r.listingId) ?? toNumber(r.id)

  const ownerId = toNumber(r.owner_id) ?? toNumber(r.ownerId)
  const animalId = toNumber(r.animal_id) ?? toNumber(r.animalId)

  const rawStatus = toString(r.status)
  const price = toNumber(r.price)
  const description = toString(r.description)
  const createdAt = toString(r.created_at) ?? toString(r.createdAt)
  const animalName = toString(r.animal_name) ?? toString(r.animalName)
  const ownerName = toString(r.owner_name) ?? toString(r.ownerName)
  const ownerEmail = toString(r.owner_email) ?? toString(r.ownerEmail)
  const species = toString(r.species)
  const breed = toString(r.breed)
  const city = toString(r.located_name) ?? toString(r.locatedName) ?? toString(r.city)
  const imageUrl = toString(r.photo_url) ?? toString(r.photoUrl) ?? toString(r.imageUrl)

  // If backend doesn't provide a display title, make a reasonable one.
  const title =
    toString(r.title) ??
    animalName ??
    toString(r.name) ??
    titleFromDescription(description) ??
    (listingId !== undefined ? `Listing #${listingId}` : 'Listing')

  // `id` is required by the UI.
  const id = listingId !== undefined ? String(listingId) : toString(r.id) ?? ''
  if (!id) return null

  return {
    id,
    title,
    listingId,
    ownerId,
    animalId,
    status: mapStatus(rawStatus),
    price,
    description,
    createdAt,
    animalName,
    ownerName,
    ownerEmail,
    species,
    petType: species,
    breed,
    city,
    imageUrl,
  }
}

export async function fetchListings(options?: { signal?: AbortSignal }): Promise<Listing[]> {
  const url = joinUrl(getBaseUrl(), '/api/public/listings')
  const res = await fetch(url, {
    method: 'GET',
    headers: { Accept: 'application/json' },
    signal: options?.signal,
  })

  if (!res.ok) {
    throw new Error(`Failed to load listings (${res.status})`)
  }

  const data = (await res.json()) as unknown
  if (!Array.isArray(data)) return []

  return data.map(normalizeListingRow).filter((x): x is Listing => x !== null)
}

export async function fetchListingById(listingId: string | number, options?: { signal?: AbortSignal }): Promise<Listing> {
  const url = joinUrl(getBaseUrl(), `/api/listings/${listingId}`)
  const res = await fetch(url, {
    method: 'GET',
    headers: { Accept: 'application/json' },
    signal: options?.signal,
  })

  if (!res.ok) {
    throw new Error(`Failed to load listing (${res.status})`)
  }

  const data = (await res.json()) as unknown
  const listing = normalizeListingRow(data)
  if (!listing) throw new Error('Invalid listing data')
  return listing
}

export async function fetchUserName(userId: number, options?: { signal?: AbortSignal }): Promise<string | null> {
  try {
    const url = joinUrl(getBaseUrl(), `/api/users/${userId}`)
    const res = await fetch(url, {
      method: 'GET',
      headers: { Accept: 'application/json' },
      signal: options?.signal,
    })

    if (!res.ok) return null

    const data = (await res.json()) as unknown
    if (typeof data === 'object' && data !== null) {
      const user = data as Record<string, unknown>
      const firstName = user.firstName ? String(user.firstName) : ''
      const lastName = user.lastName ? String(user.lastName) : ''
      if (firstName || lastName) {
        return `${firstName} ${lastName}`.trim()
      }
      if (user.username) {
        return String(user.username)
      }
    }
    return null
  } catch {
    return null
  }
}

export async function fetchPetName(petId: number, options?: { signal?: AbortSignal }): Promise<string | null> {
  try {
    const url = joinUrl(getBaseUrl(), `/api/pets/${petId}`)
    const res = await fetch(url, {
      method: 'GET',
      headers: { Accept: 'application/json' },
      signal: options?.signal,
    })

    if (!res.ok) return null

    const data = (await res.json()) as unknown
    if (typeof data === 'object' && data !== null) {
      const pet = data as Record<string, unknown>
      if (pet.name) {
        return String(pet.name)
      }
    }
    return null
  } catch {
    return null
  }
}

export async function fetchRecommendedListings(userId: number, options?: { signal?: AbortSignal }): Promise<Listing[]> {
  const url = joinUrl(getBaseUrl(), '/api/listings/recommendations')
  const res = await fetch(url, {
    method: 'GET',
    headers: {
      Accept: 'application/json',
      'X-User-Id': String(userId)
    },
    signal: options?.signal,
  })

  if (!res.ok) {
    throw new Error(`Failed to fetch recommended listings: ${res.statusText}`)
  }

  const data = (await res.json()) as unknown
  if (!Array.isArray(data)) return []
  return data.map(normalizeListingRow).filter((item) => item !== null) as Listing[]
}
