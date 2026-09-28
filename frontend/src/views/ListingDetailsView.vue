<template>
  <main class="listing-details-main">
    <!-- Header with back button -->
    <header class="header-section">
      <div class="container">
        <RouterLink to="/" class="back-link">Back to listings</RouterLink>
      </div>
    </header>

    <!-- Loading State -->
    <div v-if="loading" class="container py-5 text-center text-muted">
      <p>Loading listing details…</p>
    </div>

    <!-- Error State -->
    <div v-else-if="error" class="container py-5">
      <div class="alert alert-danger" role="alert">
        <div class="fw-semibold">Failed to load listing</div>
        <div class="small">{{ error }}</div>
        <button class="btn btn-sm btn-outline-danger mt-2" type="button" @click="reload">Try again</button>
      </div>
    </div>

    <!-- Listing Details -->
    <div v-else-if="listing" class="details-container">
      <section class="image-section">
        <div class="container">
          <div class="image-wrapper">
            <img :src="imageSrc" :alt="listing.title || 'Pet listing'" class="main-image" @error="onImageError" />
            <button
              class="favorite-btn"
              type="button"
              @click="toggleFavorite"
              :aria-pressed="isFavorited"
              :title="isFavorited ? 'Remove from favorites' : 'Add to favorites'"
            >
              {{ isFavorited ? '♥' : '♡' }}
            </button>
            <div v-if="listing.status" class="status-badge">{{ listing.status }}</div>
          </div>
        </div>
      </section>

      <section class="details-section">
        <div class="container">
          <div class="details-grid">
            <!-- Left column: Main info -->
            <div class="left-column">
              <!-- Title and price -->
              <div class="header-info">
                <div>
                  <p v-if="animalName || ownerName" class="listing-kicker">
                    <span v-if="animalName">{{ animalName }}</span>
                    <span v-if="animalName && ownerName"> / </span>
                    <span v-if="ownerName">Listed by {{ ownerName }}</span>
                  </p>
                  <h1 class="listing-title">{{ listing.title }}</h1>
                  <p v-if="createdText" class="posted-date">Posted {{ createdText }}</p>
                </div>
                <div v-if="priceText" class="price-display">{{ priceText }}</div>
              </div>

              <!-- Pet characteristics -->
              <div v-if="hasPetInfo" class="pet-info-card">
                <h2 class="section-title">About {{ listing.title }}</h2>
                <div class="pet-details">
                  <div v-if="listing.petType" class="detail-row">
                    <span class="label">Type:</span>
                    <span class="value">{{ listing.petType }}</span>
                  </div>
                  <div v-if="listing.breed" class="detail-row">
                    <span class="label">Breed:</span>
                    <span class="value">{{ listing.breed }}</span>
                  </div>
                  <div v-if="listing.age" class="detail-row">
                    <span class="label">Age:</span>
                    <span class="value">{{ listing.age }} {{ listing.ageUnit || 'years' }}</span>
                  </div>
                  <div v-if="listing.gender" class="detail-row">
                    <span class="label">Gender:</span>
                    <span class="value">{{ listing.gender }}</span>
                  </div>
                  <div v-if="listing.size" class="detail-row">
                    <span class="label">Size:</span>
                    <span class="value">{{ listing.size }}</span>
                  </div>
                </div>
              </div>

              <!-- Health and traits -->
              <div v-if="healthTraits.length > 0" class="health-traits-card">
                <h2 class="section-title">Health & Traits</h2>
                <ul class="traits-list">
                  <li v-for="trait in healthTraits" :key="trait" class="trait-item">{{ trait }}</li>
                </ul>
              </div>

              <div class="health-records-card" id="health-records">
                <h2 class="section-title">Health Records</h2>
                <div v-if="healthRecordsLoading" class="muted-text">Loading health records...</div>
                <div v-else-if="healthRecordsError" class="alert alert-danger">{{ healthRecordsError }}</div>
                <div v-else-if="healthRecords.length === 0" class="muted-text">No health records have been added yet.</div>
                <div v-else class="health-record-list">
                  <article v-for="record in healthRecords" :key="record.healthRecordId" class="health-record-item">
                    <div class="health-record-heading">
                      <strong>{{ record.type }}</strong>
                      <span>{{ formatRecordDate(record.date) }}</span>
                    </div>
                    <p>{{ record.description || 'No description' }}</p>
                    <small v-if="record.clinicName">{{ record.clinicName }}</small>
                  </article>
                </div>
              </div>

              <!-- Description -->
              <div v-if="listing.description" class="description-card">
                <h2 class="section-title">Description</h2>
                <p class="description-text">{{ listing.description }}</p>
              </div>

              <!-- Location -->
              <div v-if="locationParts.length > 0" class="location-card">
                <h2 class="section-title">Location</h2>
                <p class="location-text">{{ locationParts.join(', ') }}</p>
              </div>
            </div>

            <!-- Right column: Contact and owner info -->
            <aside class="right-column">
              <!-- Action buttons -->
              <div class="action-buttons">
                <button class="btn-primary" type="button" @click="contactOwner">
                  <span class="btn-text">Contact Owner</span>
                </button>
                <button class="btn-secondary" type="button" @click="seeOwner">
                  <span class="btn-text">See Owner</span>
                </button>
                <button class="btn-secondary" type="button" @click="toggleFavorite">
                  <span class="btn-text">{{ isFavorited ? 'Saved' : 'Save' }}</span>
                </button>
              </div>

              <!-- Owner info placeholder -->
              <div class="owner-card">
                <h3 class="owner-title">Listing Details</h3>
                <div class="owner-info">
                  <div v-if="ownerName" class="info-row">
                    <span class="label">Owner:</span>
                    <span class="value">{{ ownerName }}</span>
                  </div>
                  <div v-if="ownerEmail" class="info-row">
                    <span class="label">Email:</span>
                    <span class="value">{{ ownerEmail }}</span>
                  </div>
                  <div v-if="animalName" class="info-row">
                    <span class="label">Pet Name:</span>
                    <button class="pet-link" type="button" @click="scrollToHealthRecords">{{ animalName }}</button>
                  </div>
                </div>
              </div>

              <!-- Share -->
              <div class="share-section">
                <p class="share-title">Share this listing</p>
                <div class="share-buttons">
                  <button class="share-btn" type="button" @click="copyLink" :title="copyLinkText">Copy</button>
                </div>
              </div>
            </aside>
          </div>
        </div>
      </section>
    </div>

    <!-- Related Listings Section -->
    <section v-if="relatedListings.length > 0" class="related-listings-section">
      <div class="container">
        <h2 class="related-title">More Listings</h2>
        <div class="related-listings-grid">
          <article
            v-for="relatedListing in relatedListings"
            :key="relatedListing.id"
            class="related-listing-card"
            @click="goToListing(relatedListing.id)"
            role="button"
            tabindex="0"
            @keydown.enter="goToListing(relatedListing.id)"
          >
            <div class="related-image-wrapper">
              <img
                :src="getRelatedListingImage(relatedListing)"
                :alt="relatedListing.title || 'Pet listing'"
                class="related-image"
                @error="(e) => handleRelatedImageError(e)"
              />
              <button
                class="related-favorite-btn"
                type="button"
                @click.stop="toggleRelatedFavorite(relatedListing.id)"
                :aria-pressed="isRelatedFavorited(relatedListing.id)"
                :title="isRelatedFavorited(relatedListing.id) ? 'Remove from favorites' : 'Add to favorites'"
              >
                {{ isRelatedFavorited(relatedListing.id) ?'♥' : '♡' }}
              </button>
              <div v-if="relatedListing.status" class="related-badge">{{ relatedListing.status }}</div>
            </div>
            <div class="related-content">
              <h3 class="related-listing-title">{{ relatedListing.title }}</h3>
              <p v-if="relatedListing.petType" class="related-pet-type">{{ relatedListing.petType }}</p>
              <div v-if="relatedListing.price" class="related-price">${{ Number(relatedListing.price).toFixed(2) }}</div>
            </div>
          </article>
        </div>
      </div>
    </section>
  </main>
</template>

<script setup lang="ts">
import { computed, ref, onBeforeUnmount, onMounted, watch } from 'vue'
import { useRoute, useRouter, RouterLink } from 'vue-router'
import type { Listing } from '../types/listing'
import { fetchListingById, fetchUserName, fetchPetName, fetchListings } from '../api/listings'
import { getPet, getPetHealthRecords, getUserProfile, type HealthRecord } from '../api/profile'
import { useAuthStore } from '../stores/auth'
import { addFavorite, removeFavorite, getFavoritedListings } from '../api/favorites'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const id = computed(() => String(route.params.id || ''))
const loading = ref(false)
const error = ref<string | null>(null)
const listing = ref<Listing | null>(null)
const ownerName = ref<string | null>(null)
const ownerEmail = ref<string | null>(null)
const animalName = ref<string | null>(null)
const isFavorited = ref(false)
const imageBroken = ref(false)
const copyLinkText = ref('Copy link')
const relatedListings = ref<Listing[]>([])
const relatedFavoritedIds = ref<Set<string | number>>(new Set())
const userFavoritedIds = ref<Set<number>>(new Set())
const healthRecords = ref<HealthRecord[]>([])
const healthRecordsLoading = ref(false)
const healthRecordsError = ref('')

let abort: AbortController | null = null

const placeholderImage = new URL('../img/all_outline.png', import.meta.url).href

const imageSrc = computed(() => {
  if (imageBroken.value) return placeholderImage
  return (
    listing.value?.imageUrl ||
    listing.value?.image ||
    listing.value?.photos?.[0] ||
    listing.value?.images?.[0] ||
    placeholderImage
  )
})

const priceText = computed(() => {
  const price = listing.value?.price ?? listing.value?.fee ?? listing.value?.adoptionFee
  const currency = listing.value?.currency || 'USD'
  if (price === null || price === undefined) return ''
  const num = Number(price)
  if (Number.isFinite(num)) {
    try {
      return new Intl.NumberFormat(undefined, { style: 'currency', currency }).format(num)
    } catch {
      return `$${num}`
    }
  }
  return String(price)
})

const createdText = computed(() => {
  const raw = listing.value?.createdAt
  if (!raw) return ''
  const d = new Date(raw)
  if (Number.isNaN(d.getTime())) return ''
  try {
    return new Intl.DateTimeFormat(undefined, { month: 'short', day: 'numeric', year: 'numeric' }).format(d)
  } catch {
    return ''
  }
})

const hasPetInfo = computed(() => {
  if (!listing.value) return false
  return !!(listing.value.petType || listing.value.breed || listing.value.age || listing.value.gender || listing.value.size)
})

const healthTraits = computed(() => {
  if (!listing.value) return []
  const traits: string[] = []
  if (listing.value.vaccinated) traits.push('Vaccinated')
  if (listing.value.neutered) traits.push('Neutered')
  if (listing.value.spayed) traits.push('Spayed')
  if (Array.isArray(listing.value.tags)) {
    traits.push(...listing.value.tags.slice(0, 5))
  }
  return traits
})

const locationParts = computed(() => {
  const parts: string[] = []
  if (listing.value?.city) parts.push(listing.value.city)
  if (listing.value?.state) parts.push(listing.value.state)
  if (listing.value?.country) parts.push(listing.value.country)
  return parts
})

async function load() {
  loading.value = true
  error.value = null
  listing.value = null
  ownerName.value = null
  ownerEmail.value = null
  animalName.value = null
  isFavorited.value = false
  relatedListings.value = []
  healthRecords.value = []
  healthRecordsError.value = ''

  abort?.abort()
  abort = new AbortController()

  try {
    const data = await fetchListingById(id.value, { signal: abort.signal })
    listing.value = data

    // Fetch owner, animal names, and pet image in parallel
    const [ownerNameResult, ownerProfileResult, animalNameResult, petResult] = await Promise.all([
      data.ownerId ? fetchUserName(data.ownerId, { signal: abort.signal }) : Promise.resolve(null),
      data.ownerId ? getUserProfile(data.ownerId).catch(() => null) : Promise.resolve(null),
      data.animalId ? fetchPetName(data.animalId, { signal: abort.signal }) : Promise.resolve(null),
      data.animalId ? getPet(data.animalId) : Promise.resolve(null),
    ])

    ownerName.value = ownerProfileResult
      ? `${ownerProfileResult.firstName || ''} ${ownerProfileResult.lastName || ''}`.trim() || ownerProfileResult.username || ownerNameResult
      : ownerNameResult
    ownerEmail.value = ownerProfileResult?.email || null
    animalName.value = animalNameResult

    // Add pet image to listing
    if (listing.value && petResult?.photoUrl) {
      listing.value.imageUrl = petResult.photoUrl
    }

    if (data.animalId) {
      await loadHealthRecords(data.animalId)
    }

    // Fetch related listings
    const allListings = await fetchListings({ signal: abort.signal })
    // Filter out the current listing and get up to 6 related listings
    const related = allListings
      .filter((l) => l.id !== id.value)
      .slice(0, 6)

    // Fetch pet images for related listings
    const listingsWithImages = await Promise.all(
      related.map(async (listing) => {
        try {
          if (listing.animalId) {
            const pet = await getPet(listing.animalId)
            return {
              ...listing,
              imageUrl: pet.photoUrl || new URL('../img/all_outline.png', import.meta.url).href,
            }
          }
        } catch (err) {
          console.error(`Failed to fetch pet ${listing.animalId}:`, err)
        }
        return {
          ...listing,
          imageUrl: new URL('../img/all_outline.png', import.meta.url).href,
        }
      })
    )

    relatedListings.value = listingsWithImages

    // Load user's favorites to check if this listing is favorited
    await loadUserFavorites()
  } catch (e) {
    const message = e instanceof Error ? e.message : String(e)
    error.value = message
  } finally {
    loading.value = false
  }
}

async function loadHealthRecords(animalId: number) {
  try {
    healthRecordsLoading.value = true
    healthRecords.value = await getPetHealthRecords(animalId)
    healthRecordsError.value = ''
  } catch (error) {
    healthRecords.value = []
    healthRecordsError.value = error instanceof Error ? error.message : 'Failed to load health records'
  } finally {
    healthRecordsLoading.value = false
  }
}

function formatRecordDate(value: string) {
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  return date.toLocaleDateString('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
  })
}

function scrollToHealthRecords() {
  document.getElementById('health-records')?.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

function checkFavorite() {
  if (!auth.isAuthenticated || !listing.value?.listingId) {
    isFavorited.value = false
    return
  }

  // Check if current listing is in user's favorites
  const listingId = Number(listing.value.listingId)
  isFavorited.value = userFavoritedIds.value.has(listingId)
}

async function loadUserFavorites() {
  if (!auth.isAuthenticated || !auth.user?.userId) {
    userFavoritedIds.value.clear()
    relatedFavoritedIds.value.clear()
    return
  }

  try {
    const favorites = await getFavoritedListings(auth.user.userId)
    userFavoritedIds.value.clear()

    favorites.forEach((fav: any) => {
      userFavoritedIds.value.add(Number(fav.listingId || fav.id))
    })

    // Also populate related favorites
    relatedListings.value.forEach((listing) => {
      if (userFavoritedIds.value.has(Number(listing.id || listing.listingId))) {
        relatedFavoritedIds.value.add(listing.id)
      }
    })

    checkFavorite()
  } catch (error) {
    console.error('Failed to load favorites:', error)
    userFavoritedIds.value.clear()
  }
}

function reload() {
  load()
}

function onImageError() {
  imageBroken.value = true
}

async function toggleFavorite() {
  if (!auth.isAuthenticated || !auth.user?.userId || !listing.value?.listingId) {
    alert('Please log in to save favorites')
    return
  }

  try {
    const listingId = Number(listing.value.listingId)
    if (isFavorited.value) {
      await removeFavorite(auth.user.userId, listingId)
      isFavorited.value = false
    } else {
      await addFavorite(auth.user.userId, listingId)
      isFavorited.value = true
    }
  } catch (error) {
    console.error('Failed to update favorite:', error)
    alert('Failed to update favorite. Please try again.')
  }
}

function contactOwner() {
  if (!ownerEmail.value) {
    alert('Owner contact email is not available')
    return
  }
  const petName = animalName.value || listing.value?.title || 'your pet'
  const subject = encodeURIComponent(`Question about ${petName} on Petify`)
  const body = encodeURIComponent(`Hi ${ownerName.value || ''},\n\nI saw your listing for ${petName} on Petify and would like to know more.\n\nThanks!`)
  window.location.href = `mailto:${ownerEmail.value}?subject=${subject}&body=${body}`
}

function seeOwner() {
  if (!listing.value?.ownerId) {
    alert('Owner information not available')
    return
  }
  router.push({ name: 'owner-profile', params: { ownerId: listing.value.ownerId } })
}

function copyLink() {
  const url = window.location.href
  navigator.clipboard
    .writeText(url)
    .then(() => {
      copyLinkText.value = '✓ Copied!'
      setTimeout(() => {
        copyLinkText.value = 'Copy link'
      }, 2000)
    })
    .catch(() => {
      alert('Failed to copy link')
    })
}

function getRelatedListingImage(relatedListing: Listing): string {
  return relatedListing.imageUrl || new URL('../img/all_outline.png', import.meta.url).href
}

function handleRelatedImageError(e: Event) {
  const img = e.target as HTMLImageElement
  img.src = new URL('../img/all_outline.png', import.meta.url).href
}

function goToListing(listingId: string | number) {
  router.push({ name: 'listing-details', params: { id: listingId } })
}

function isRelatedFavorited(listingId: string | number): boolean {
  return relatedFavoritedIds.value.has(listingId)
}

async function toggleRelatedFavorite(listingId: string | number) {
  if (!auth.isAuthenticated || !auth.user?.userId) {
    alert('Please log in to save favorites')
    return
  }

  try {
    const id = Number(listingId)
    if (isRelatedFavorited(listingId)) {
      await removeFavorite(auth.user.userId, id)
      relatedFavoritedIds.value.delete(listingId)
    } else {
      await addFavorite(auth.user.userId, id)
      relatedFavoritedIds.value.add(listingId)
    }
  } catch (error) {
    console.error('Failed to update favorite:', error)
    alert('Failed to update favorite. Please try again.')
  }
}

onMounted(load)
onBeforeUnmount(() => abort?.abort())

// Watch for route changes and reload listing
watch(id, () => {
  load()
})
</script>

<style scoped>
.listing-details-main {
  background: #f5f7fb;
  min-height: 100vh;
  padding-bottom: 60px;
}

/* Header Section */
.header-section {
  background: white;
  border-bottom: 1px solid #e5e7eb;
  padding: 20px 0;
  position: sticky;
  top: 0;
  z-index: 10;
}

.back-link {
  display: inline-flex;
  align-items: center;
  color: #d97706;
  text-decoration: none;
  font-weight: 600;
  font-size: 1rem;
  transition: all 0.2s ease;
}

.back-link:hover {
  color: #b45309;
  gap: 4px;
}

/* Image Section */
.image-section {
  background:
    linear-gradient(135deg, rgba(17, 24, 39, 0.78), rgba(249, 115, 22, 0.56)),
    #111827;
  padding: 42px 0;
}

.image-wrapper {
  aspect-ratio: 4 / 3;
  background: #f3f4f6;
  border: 1px solid rgba(255, 255, 255, 0.24);
  border-radius: 8px;
  box-shadow: 0 24px 70px rgba(17, 24, 39, 0.34);
  margin: 0 auto;
  max-width: 760px;
  overflow: hidden;
  position: relative;
  width: 100%;
}

.main-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.favorite-btn {
  position: absolute;
  top: 16px;
  right: 16px;
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.95);
  border: 2px solid #e5e7eb;
  font-size: 1.5rem;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.favorite-btn:hover {
  background: #fef3c7;
  border-color: #d97706;
  transform: scale(1.1);
}

.status-badge {
  position: absolute;
  bottom: 16px;
  left: 16px;
  background: #d97706;
  color: white;
  padding: 8px 16px;
  border-radius: 8px;
  font-weight: 600;
  font-size: 0.875rem;
}

/* Details Section */
.details-section {
  padding: 36px 0 20px;
}

.details-grid {
  display: grid;
  grid-template-columns: 1fr 360px;
  gap: 40px;
  align-items: start;
}

.left-column {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.right-column {
  display: flex;
  flex-direction: column;
  gap: 16px;
  position: sticky;
  top: 100px;
}

/* Header Info */
.header-info {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 20px;
}

.listing-title {
  color: #111827;
  font-size: 2.35rem;
  font-weight: 850;
  line-height: 1.12;
  margin: 0 0 12px 0;
}

.listing-kicker {
  color: #ea580c;
  font-size: 0.85rem;
  font-weight: 800;
  margin: 0 0 8px;
}

.posted-date {
  color: #6b7280;
  font-size: 0.875rem;
  margin: 0;
}

.price-display {
  background: #fff7ed;
  border: 1px solid #fed7aa;
  border-radius: 8px;
  color: #ea580c;
  font-size: 1.8rem;
  font-weight: 900;
  padding: 10px 14px;
  white-space: nowrap;
}

/* Cards */
.pet-info-card,
.health-traits-card,
.health-records-card,
.description-card,
.location-card,
.owner-card {
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  box-shadow: 0 10px 26px rgba(17, 24, 39, 0.04);
  padding: 24px;
}

.health-record-list {
  display: grid;
  gap: 12px;
}

.health-record-item {
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #fbfdff;
  padding: 14px;
}

.health-record-heading {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  color: #111827;
}

.health-record-heading span,
.health-record-item small,
.muted-text {
  color: #6b7280;
}

.health-record-item p {
  color: #374151;
  margin: 8px 0 4px;
}

.pet-link {
  background: transparent;
  border: 0;
  color: #d97706;
  cursor: pointer;
  font-weight: 700;
  padding: 0;
  text-align: right;
}

.pet-link:hover {
  color: #b45309;
  text-decoration: underline;
}

.section-title {
  font-size: 1.25rem;
  font-weight: 700;
  margin: 0 0 16px 0;
  color: #111827;
}

.pet-details {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f3f4f6;
}

.detail-row:last-child {
  border-bottom: none;
  padding-bottom: 0;
}

.detail-row .label {
  font-weight: 600;
  color: #6b7280;
  font-size: 0.875rem;
}

.detail-row .value {
  color: #111827;
  font-weight: 500;
}

.traits-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.trait-item {
  background: #ecfdf5;
  border: 1px solid #bbf7d0;
  color: #166534;
  padding: 8px 12px;
  border-radius: 6px;
  font-size: 0.875rem;
  font-weight: 500;
}

.description-text {
  color: #374151;
  line-height: 1.6;
  margin: 0;
  white-space: pre-wrap;
  word-break: break-word;
}

.location-text {
  color: #374151;
  font-size: 1.125rem;
  margin: 0;
}

/* Action Buttons */
.action-buttons {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  box-shadow: 0 14px 34px rgba(17, 24, 39, 0.08);
  padding: 16px;
}

.action-buttons .btn-secondary:last-child {
  grid-column: 1 / -1;
}

.btn-primary,
.btn-secondary {
  padding: 12px 16px;
  border-radius: 8px;
  border: none;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
  text-align: center;
  font-size: 1rem;
}

.btn-primary {
  background: #f97316;
  color: white;
}

.btn-primary:hover {
  background: #ea580c;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(217, 119, 6, 0.2);
}

.btn-secondary {
  background: white;
  color: #111827;
  border: 1px solid #d1d5db;
}

.btn-secondary:hover {
  background: #f9fafb;
  border-color: #f97316;
}

.btn-text {
  display: block;
}

/* Owner Card */
.owner-title {
  font-size: 1.125rem;
  font-weight: 700;
  margin: 0 0 16px 0;
  color: #111827;
}

.owner-info {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
}

.info-row .label {
  font-size: 0.875rem;
  color: #6b7280;
  font-weight: 600;
}

.info-row .value {
  color: #374151;
  font-weight: 500;
}

/* Share Section */
.share-section {
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 18px;
}

.share-title {
  font-size: 0.875rem;
  font-weight: 600;
  color: #6b7280;
  margin: 0 0 12px 0;
}

.share-buttons {
  display: flex;
  gap: 8px;
}

.share-btn {
  flex: 1;
  padding: 10px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: white;
  color: #111827;
  font-size: 0.92rem;
  font-weight: 800;
  cursor: pointer;
  transition: all 0.2s ease;
}

.share-btn:hover {
  border-color: #d97706;
  background: #fef3c7;
}

/* Related Listings Section */
.related-listings-section {
  background: white;
  padding: 60px 0;
  border-top: 1px solid #e5e7eb;
}

.related-title {
  font-size: 2rem;
  font-weight: 700;
  color: #111827;
  margin: 0 0 40px 0;
  letter-spacing: -0.5px;
}

.related-listings-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 24px;
}

.related-listing-card {
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
}

.related-listing-card:hover {
  border-color: #d97706;
  box-shadow: 0 8px 24px rgba(217, 119, 6, 0.15);
  transform: translateY(-4px);
}

.related-image-wrapper {
  position: relative;
  width: 100%;
  height: 200px;
  background: #f3f4f6;
  overflow: hidden;
}

.related-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.related-badge {
  position: absolute;
  top: 12px;
  right: 12px;
  background: #d97706;
  color: white;
  padding: 6px 12px;
  border-radius: 6px;
  font-weight: 600;
  font-size: 0.75rem;
  text-transform: uppercase;
}

.related-favorite-btn {
  position: absolute;
  top: 12px;
  left: 12px;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.95);
  border: 2px solid #e5e7eb;
  font-size: 1.25rem;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  padding: 0;
}

.related-favorite-btn:hover {
  background: #fef3c7;
  border-color: #d97706;
  transform: scale(1.1);
}

.related-content {
  padding: 16px;
}

.related-listing-title {
  font-size: 1.1rem;
  font-weight: 700;
  margin: 0 0 8px 0;
  color: #111827;
  line-height: 1.4;
}

.related-pet-type {
  font-size: 0.875rem;
  color: #6b7280;
  margin: 0 0 8px 0;
}

.related-price {
  font-size: 1.25rem;
  font-weight: 700;
  color: #d97706;
  margin: 0;
}

/* Responsive */
@media (max-width: 991.98px) {
  .listing-title {
    font-size: 2rem;
  }

  .header-info {
    flex-direction: column;
    align-items: flex-start;
  }

  .price-display {
    font-size: 1.5rem;
  }

  .details-grid {
    grid-template-columns: 1fr;
    gap: 24px;
  }

  .right-column {
    position: static;
    grid-column: 1;
  }

  .image-section {
    padding: 20px 0;
  }

  .details-section {
    padding: 20px 0;
  }
}

@media (max-width: 576px) {
  .header-section {
    padding: 16px 0;
  }

  .back-link {
    font-size: 0.875rem;
  }

  .listing-title {
    font-size: 1.5rem;
  }

  .pet-info-card,
  .health-traits-card,
  .health-records-card,
  .description-card,
  .location-card,
  .owner-card,
  .share-section {
    padding: 16px;
  }

  .section-title {
    font-size: 1rem;
  }

  .action-buttons {
    grid-column: 1;
    flex-direction: row;
  }

  .btn-primary,
  .btn-secondary {
    flex: 1;
    padding: 10px 12px;
    font-size: 0.875rem;
  }
}
</style>
