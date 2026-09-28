function getBaseUrl(): string {
  const base = (import.meta.env.VITE_API_BASE_URL as string | undefined) ?? ''
  return base.replace(/\/$/, '')
}

function joinUrl(base: string, path: string): string {
  if (!base) return path
  return `${base}${path.startsWith('/') ? '' : '/'}${path}`
}

export interface UserProfile {
  userId: number
  username: string
  email: string
  firstName: string
  lastName: string
  verified?: boolean
}

export interface Pet {
  animalId: number
  name: string
  sex: string
  dateOfBirth?: string
  photoUrl?: string
  type: string
  species: string
  breed?: string
  locatedName?: string
}

export interface VetClinic {
  clinicId: number
  name: string
  city: string
  address: string
}

export interface OwnerAppointment {
  appointmentId: number
  clinicId: number
  clinicName?: string
  clinicCity?: string
  clinicAddress?: string
  animalId: number
  petName?: string
  petSpecies?: string
  petPhotoUrl?: string
  status: string
  dateTime: string
  notes?: string
}

export interface AppointmentSlot {
  dateTime: string
  label: string
}

export interface ClinicAppointment {
  appointmentId: number
  clinicId: number
  animalId: number
  petName?: string
  petSpecies?: string
  ownerId: number
  ownerName?: string
  status: string
  dateTime: string
  label: string
  notes?: string
}

export interface AppNotification {
  notificationId: number
  type: string
  message: string
  isRead: boolean
  createdAt: string
}

export interface ClinicUnavailableSlot {
  slotId: number
  clinicId: number
  dateTime: string
  label: string
  reason?: string
}

export interface HealthRecord {
  healthRecordId: number
  animalId: number
  animalName?: string
  appointmentId: number
  clinicId?: number
  clinicName?: string
  type: string
  description?: string
  date: string
  appointmentDateTime?: string
}

async function readJsonOrError<T>(response: Response, fallback: string): Promise<T> {
  const text = await response.text()
  let parsed: any = null

  if (text) {
    try {
      parsed = JSON.parse(text)
    } catch {
      parsed = null
    }
  }

  if (!response.ok) {
    throw new Error(parsed?.error || `${fallback}: ${response.status} ${response.statusText}. ${text.slice(0, 200)}`)
  }

  return parsed as T
}

export async function getUserProfile(userId: number): Promise<UserProfile> {
  const url = joinUrl(getBaseUrl(), `/api/users/${userId}`)
  const response = await fetch(url, {
    method: 'GET',
    headers: {
      'Content-Type': 'application/json',
    },
  })

  if (!response.ok) {
    throw new Error(`Failed to fetch user profile: ${response.statusText}`)
  }

  return await response.json()
}

export async function getUserListings(userId: number): Promise<any[]> {
  const url = joinUrl(getBaseUrl(), `/api/listings/my-listings`)
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
    throw new Error(apiError || `Failed to fetch listings: ${response.status} ${response.statusText}`)
  }

  return await response.json()
}

export async function getUserPets(userId: number): Promise<Pet[]> {
  const url = joinUrl(getBaseUrl(), `/api/users/${userId}/pets`)
  const response = await fetch(url, {
    method: 'GET',
    headers: {
      'Content-Type': 'application/json',
      'X-User-Id': String(userId),
    },
  })

  if (!response.ok) {
    throw new Error(`Failed to fetch pets: ${response.statusText}`)
  }

  return await response.json()
}

export async function createPet(
  userId: number,
  data: {
    name: string
    sex: string
    dateOfBirth?: string
    photo?: File
    type: string
    species: string
    breed?: string
    locatedName?: string
  }
): Promise<Pet> {
  const url = joinUrl(getBaseUrl(), `/api/users/${userId}/pets`)
  const formData = new FormData()

  formData.append('name', data.name)
  formData.append('sex', data.sex)
  formData.append('type', data.type)
  formData.append('species', data.species)
  if (data.dateOfBirth) formData.append('dateOfBirth', data.dateOfBirth)
  if (data.breed) formData.append('breed', data.breed)
  if (data.locatedName) formData.append('locatedName', data.locatedName)
  if (data.photo) formData.append('photo', data.photo)

  console.log('🔗 API URL:', url)
  console.log('📦 Request payload:', data)
  console.log('📋 Headers:', {
    'X-User-Id': String(userId),
  })

  const response = await fetch(url, {
    method: 'POST',
    headers: {
      'X-User-Id': String(userId),
    },
    body: formData,
  })

  console.log('📬 Response status:', response.status)
  console.log('📬 Response headers:', response.headers)

  if (!response.ok) {
    const error = await response.json()
    console.error('❌ Error response:', error)
    throw new Error(error.error || `Failed to create pet: ${response.statusText}`)
  }

  const result = await response.json()
  console.log('✅ Pet created successfully:', result)
  return result
}

export async function createListing(
  userId: number,
  data: {
    animalId: number
    description: string
    price: number
  }
): Promise<any> {
  const url = joinUrl(getBaseUrl(), `/api/listings`)
  const response = await fetch(url, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'X-User-Id': String(userId),
    },
    body: JSON.stringify(data),
  })

  if (!response.ok) {
    const error = await response.json()
    throw new Error(error.error || `Failed to create listing: ${response.statusText}`)
  }

  return await response.json()
}

export async function deleteListing(userId: number, listingId: number): Promise<void> {
  const url = joinUrl(getBaseUrl(), `/api/listings/${listingId}`)
  const response = await fetch(url, {
    method: 'DELETE',
    headers: {
      'Content-Type': 'application/json',
      'X-User-Id': String(userId),
    },
  })

  if (!response.ok) {
    const error = await response.json()
    throw new Error(error.error || `Failed to delete listing: ${response.statusText}`)
  }
}

export async function updateListingStatus(
  userId: number,
  listingId: number,
  status: string
): Promise<any> {
  const url = joinUrl(getBaseUrl(), `/api/listings/${listingId}/status`)
  const response = await fetch(url, {
    method: 'PATCH',
    headers: {
      'Content-Type': 'application/json',
      'X-User-Id': String(userId),
    },
    body: JSON.stringify({ status }),
  })

  if (!response.ok) {
    const error = await response.json()
    throw new Error(error.error || `Failed to update listing: ${response.statusText}`)
  }

  return await response.json()
}

export async function getPet(petId: number): Promise<Pet> {
  const url = joinUrl(getBaseUrl(), `/api/pets/${petId}`)
  const response = await fetch(url, {
    method: 'GET',
    headers: {
      'Content-Type': 'application/json',
    },
  })

  if (!response.ok) {
    throw new Error(`Failed to fetch pet: ${response.statusText}`)
  }

  return await response.json()
}

export async function loadUserVerificationStatus(userId: number): Promise<boolean> {
  try {
    const url = joinUrl(getBaseUrl(), `/api/users/${userId}/verified`)
    const response = await fetch(url, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
      },
    })

    if (!response.ok) {
      return false
    }

    const data = await response.json()
    return data.verified || false
  } catch (error) {
    console.error('Failed to load user verification status:', error)
    return false
  }
}

export async function createAppointment(
  userId: number,
  data: {
    clinicId: number
    animalId: number
    dateTime: string
    notes?: string
  }
): Promise<any> {
  const url = joinUrl(getBaseUrl(), `/api/appointments`)
  const response = await fetch(url, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'X-User-Id': String(userId),
    },
    body: JSON.stringify(data),
  })

  if (!response.ok) {
    const error = await response.json()
    throw new Error(error.error || `Failed to create appointment: ${response.statusText}`)
  }

  return await response.json()
}

export async function getPetHealthRecords(petId: number): Promise<HealthRecord[]> {
  const url = joinUrl(getBaseUrl(), `/api/pets/${petId}/health-records`)
  const response = await fetch(url, {
    method: 'GET',
    headers: {
      'Content-Type': 'application/json',
    },
  })

  return await readJsonOrError<HealthRecord[]>(response, 'Failed to fetch health records')
}

export async function createHealthRecord(
  userId: number,
  data: {
    appointmentId: number
    type: string
    description?: string
  }
): Promise<HealthRecord> {
  const url = joinUrl(getBaseUrl(), `/api/health-records`)
  const response = await fetch(url, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'X-User-Id': String(userId),
    },
    body: JSON.stringify(data),
  })

  return await readJsonOrError<HealthRecord>(response, 'Failed to create health record')
}

export async function cancelOwnerAppointment(userId: number, appointmentId: number): Promise<OwnerAppointment> {
  const url = joinUrl(getBaseUrl(), `/api/appointments/my/${appointmentId}/cancel`)
  const response = await fetch(url, {
    method: 'PATCH',
    headers: {
      'Content-Type': 'application/json',
      'X-User-Id': String(userId),
    },
  })

  if (!response.ok) {
    const text = await response.text()
    let apiError = ''
    try {
      const error = JSON.parse(text)
      apiError = error.error || ''
    } catch {
      apiError = ''
    }

    if (apiError) {
      throw new Error(apiError)
    }

    throw new Error(`Failed to cancel appointment: ${response.status} ${response.statusText}. ${text.slice(0, 200)}`)
  }

  return await response.json()
}

export async function getClinics(): Promise<VetClinic[]> {
  const url = joinUrl(getBaseUrl(), `/api/clinics`)
  const response = await fetch(url, {
    method: 'GET',
    headers: {
      'Content-Type': 'application/json',
    },
  })

  const contentType = response.headers.get('content-type') || ''

  if (!response.ok) {
    const text = await response.text()
    throw new Error(`Failed to fetch clinics: ${response.status} ${response.statusText}. ${text.slice(0, 200)}`)
  }

  if (!contentType.includes('application/json')) {
    const text = await response.text()
    throw new Error(`Clinics API returned non-JSON. Check VITE_API_BASE_URL/backend. ${text.slice(0, 200)}`)
  }

  return await response.json()
}

export async function getMyClinic(userId: number): Promise<VetClinic> {
  const url = joinUrl(getBaseUrl(), `/api/clinics/my`)
  const response = await fetch(url, {
    method: 'GET',
    headers: {
      'Content-Type': 'application/json',
      'X-User-Id': String(userId),
    },
  })

  if (!response.ok) {
    const error = await response.json()
    throw new Error(error.error || `Failed to fetch clinic profile: ${response.statusText}`)
  }

  return await response.json()
}

export async function getClinicAvailableSlots(clinicId: number, date: string): Promise<AppointmentSlot[]> {
  const url = joinUrl(getBaseUrl(), `/api/appointments/clinics/${clinicId}/available-slots?date=${encodeURIComponent(date)}`)
  const response = await fetch(url, {
    method: 'GET',
    headers: {
      'Content-Type': 'application/json',
    },
  })

  if (!response.ok) {
    const text = await response.text()
    let apiError = ''
    try {
      const error = JSON.parse(text)
      apiError = error.error || ''
    } catch {
      apiError = ''
    }

    if (apiError) {
      throw new Error(apiError)
    }

    throw new Error(`Failed to fetch available slots: ${response.status} ${response.statusText}. ${text.slice(0, 200)}`)
  }

  return await response.json()
}

export async function getClinicAppointments(clinicId: number, date: string): Promise<ClinicAppointment[]> {
  const url = joinUrl(getBaseUrl(), `/api/appointments/clinics/${clinicId}?date=${encodeURIComponent(date)}`)
  const response = await fetch(url, {
    method: 'GET',
    headers: {
      'Content-Type': 'application/json',
    },
  })

  if (!response.ok) {
    const text = await response.text()
    throw new Error(`Failed to fetch clinic appointments: ${response.status} ${response.statusText}. ${text.slice(0, 200)}`)
  }

  return await response.json()
}

export async function getMyClinicAppointments(userId: number, date: string): Promise<ClinicAppointment[]> {
  const url = joinUrl(getBaseUrl(), `/api/appointments/my-clinic?date=${encodeURIComponent(date)}`)
  const response = await fetch(url, {
    method: 'GET',
    headers: {
      'Content-Type': 'application/json',
      'X-User-Id': String(userId),
    },
  })

  if (!response.ok) {
    const text = await response.text()
    throw new Error(`Failed to fetch clinic appointments: ${response.status} ${response.statusText}. ${text.slice(0, 200)}`)
  }

  return await response.json()
}

export async function markMyClinicAppointmentNoShow(userId: number, appointmentId: number): Promise<ClinicAppointment> {
  const url = joinUrl(getBaseUrl(), `/api/appointments/my-clinic/${appointmentId}/no-show`)
  const response = await fetch(url, {
    method: 'PATCH',
    headers: {
      'Content-Type': 'application/json',
      'X-User-Id': String(userId),
    },
  })

  if (!response.ok) {
    const text = await response.text()
    let apiError = ''
    try {
      const error = JSON.parse(text)
      apiError = error.error || ''
    } catch {
      apiError = ''
    }

    if (apiError) {
      throw new Error(apiError)
    }

    throw new Error(`Failed to mark appointment as no-show: ${response.status} ${response.statusText}. ${text.slice(0, 200)}`)
  }

  return await response.json()
}

export async function getMyClinicAvailableSlots(userId: number, date: string): Promise<AppointmentSlot[]> {
  const url = joinUrl(getBaseUrl(), `/api/appointments/my-clinic/available-slots?date=${encodeURIComponent(date)}`)
  const response = await fetch(url, {
    method: 'GET',
    headers: {
      'Content-Type': 'application/json',
      'X-User-Id': String(userId),
    },
  })

  if (!response.ok) {
    const error = await response.json()
    throw new Error(error.error || `Failed to fetch available slots: ${response.statusText}`)
  }

  return await response.json()
}

export async function getClinicUnavailableSlots(clinicId: number, date: string): Promise<ClinicUnavailableSlot[]> {
  const url = joinUrl(getBaseUrl(), `/api/appointments/clinics/${clinicId}/unavailable-slots?date=${encodeURIComponent(date)}`)
  const response = await fetch(url, {
    method: 'GET',
    headers: {
      'Content-Type': 'application/json',
    },
  })

  if (!response.ok) {
    const text = await response.text()
    throw new Error(`Failed to fetch unavailable slots: ${response.status} ${response.statusText}. ${text.slice(0, 200)}`)
  }

  return await response.json()
}

export async function getMyClinicUnavailableSlots(userId: number, date: string): Promise<ClinicUnavailableSlot[]> {
  const url = joinUrl(getBaseUrl(), `/api/appointments/my-clinic/unavailable-slots?date=${encodeURIComponent(date)}`)
  const response = await fetch(url, {
    method: 'GET',
    headers: {
      'Content-Type': 'application/json',
      'X-User-Id': String(userId),
    },
  })

  if (!response.ok) {
    const text = await response.text()
    throw new Error(`Failed to fetch unavailable slots: ${response.status} ${response.statusText}. ${text.slice(0, 200)}`)
  }

  return await response.json()
}

export async function createClinicUnavailableSlot(
  clinicId: number,
  data: { dateTime: string; reason?: string }
): Promise<ClinicUnavailableSlot> {
  const url = joinUrl(getBaseUrl(), `/api/appointments/clinics/${clinicId}/unavailable-slots`)
  const response = await fetch(url, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(data),
  })

  if (!response.ok) {
    const error = await response.json()
    throw new Error(error.error || `Failed to block slot: ${response.statusText}`)
  }

  return await response.json()
}

export async function createMyClinicUnavailableSlot(
  userId: number,
  data: { dateTime: string; reason?: string }
): Promise<ClinicUnavailableSlot> {
  const url = joinUrl(getBaseUrl(), `/api/appointments/my-clinic/unavailable-slots`)
  const response = await fetch(url, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'X-User-Id': String(userId),
    },
    body: JSON.stringify(data),
  })

  if (!response.ok) {
    const error = await response.json()
    throw new Error(error.error || `Failed to block slot: ${response.statusText}`)
  }

  return await response.json()
}

export async function deleteClinicUnavailableSlot(clinicId: number, slotId: number): Promise<void> {
  const url = joinUrl(getBaseUrl(), `/api/appointments/clinics/${clinicId}/unavailable-slots/${slotId}`)
  const response = await fetch(url, {
    method: 'DELETE',
    headers: {
      'Content-Type': 'application/json',
    },
  })

  if (!response.ok) {
    const error = await response.json()
    throw new Error(error.error || `Failed to unblock slot: ${response.statusText}`)
  }
}

export async function deleteMyClinicUnavailableSlot(userId: number, slotId: number): Promise<void> {
  const url = joinUrl(getBaseUrl(), `/api/appointments/my-clinic/unavailable-slots/${slotId}`)
  const response = await fetch(url, {
    method: 'DELETE',
    headers: {
      'Content-Type': 'application/json',
      'X-User-Id': String(userId),
    },
  })

  if (!response.ok) {
    const error = await response.json()
    throw new Error(error.error || `Failed to unblock slot: ${response.statusText}`)
  }
}

export async function getOwnerAppointments(userId: number): Promise<OwnerAppointment[]> {
  const url = joinUrl(getBaseUrl(), `/api/appointments/my`)
  const response = await fetch(url, {
    method: 'GET',
    headers: {
      'Content-Type': 'application/json',
      'X-User-Id': String(userId),
    },
  })

  if (!response.ok) {
    const text = await response.text()
    throw new Error(`Failed to fetch appointments: ${response.status} ${response.statusText}. ${text.slice(0, 200)}`)
  }

  return await response.json()
}

export async function getMyNotifications(userId: number): Promise<AppNotification[]> {
  const url = joinUrl(getBaseUrl(), `/api/notifications/my`)
  const response = await fetch(url, {
    method: 'GET',
    headers: {
      'Content-Type': 'application/json',
      'X-User-Id': String(userId),
    },
  })

  if (!response.ok) {
    const text = await response.text()
    throw new Error(`Failed to fetch notifications: ${response.status} ${response.statusText}. ${text.slice(0, 200)}`)
  }

  return await response.json()
}
