export type ListingStatus = 'Available' | 'Pending' | 'Adopted' | string

export interface Listing {
  id: string
  title?: string

  // Backend (DB) fields
  listingId?: number
  ownerId?: number
  animalId?: number
  status?: ListingStatus
  price?: number
  description?: string
  createdAt?: string

  petType?: string
  species?: string
  breed?: string
  age?: number
  ageUnit?: string
  gender?: string
  size?: string

  city?: string
  state?: string
  country?: string

  currency?: string

  imageUrl?: string
  tags?: string[]

  // Owner and animal info
  ownerName?: string
  ownerEmail?: string
  animalName?: string

  // Backwards/alternate fields supported by `ListingCard`
  name?: string
  photos?: string[]
  images?: string[]
  image?: string
  fee?: number
  adoptionFee?: number
  vaccinated?: boolean
  neutered?: boolean
  spayed?: boolean
}
