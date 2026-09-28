<template>
  <main class="listings-main">
    <section class="hero-section">
      <div class="hero-content">
        <div class="hero-left">
          <p class="hero-eyebrow">Petify marketplace</p>
          <h1 class="hero-title">Find your perfect pet</h1>
          <p class="hero-subtitle">Browse trusted listings from owners in the Petify community.</p>

          <div class="search-box">
            <div class="pet-type-selector mb-3">
              <button
                v-for="pet in petTypes"
                :key="pet.value"
                :class="['pet-btn', { active: petType === pet.value }]"
                @click="petType = pet.value"
              >
                <span class="pet-icon">
                  <img v-if="pet.type === 'image'" :src="pet.icon" :alt="pet.label" class="pet-icon-img" />
                  <span v-else>{{ pet.icon }}</span>
                </span>
                <span>{{ pet.label }}</span>
              </button>
            </div>

            <div class="search-inputs">
              <div class="filters">
                <select v-model="selectedBreed" class="form-select" aria-label="Filter by breed">
                  <option value="">All breeds</option>
                  <option v-for="b in breedOptions" :key="b" :value="b">{{ b }}</option>
                </select>

                <select v-model="selectedCity" class="form-select" aria-label="Filter by location">
                  <option value="">All locations</option>
                  <option v-for="c in cityOptions" :key="c" :value="c">{{ c }}</option>
                </select>
              </div>
            </div>
          </div>
        </div>

        <div class="hero-right">
          <aside class="benefits-card" aria-label="Why choose Petify">
            <h3 class="benefits-title">Why Petify?</h3>
            <ul class="benefits-list">
              <li><span class="benefit-icon" aria-hidden="true"></span><span>Verified pet owners</span></li>
              <li><span class="benefit-icon" aria-hidden="true"></span><span>Clear listing history</span></li>
              <li><span class="benefit-icon" aria-hidden="true"></span><span>Community review signals</span></li>
            </ul>
          </aside>
        </div>
      </div>
    </section>

    <!-- Listings Section -->
    <section class="listings-section">
      <div class="container">
        <div class="listings-header">
          <div>
            <div class="view-mode-tabs mb-3">
              <button
                :class="['view-mode-btn', { active: viewMode === 'all' }]"
                @click="viewMode = 'all'; load()"
                type="button"
              >
                All Listings
              </button>
              <button
                v-if="auth.isAuthenticated"
                :class="['view-mode-btn', { active: viewMode === 'recommended' }]"
                @click="viewMode = 'recommended'; load()"
                type="button"
              >
                Recommended
              </button>
            </div>
            <h2 class="listings-title">{{ viewMode === 'recommended' ? 'Your recommended listings' : 'Browse all listings' }}</h2>
            <p class="listings-subtitle">{{ viewMode === 'recommended' ? 'Personalized just for you' : 'Scroll to see available pets' }}</p>
          </div>
          <button class="btn btn-outline-secondary" type="button" @click="reload" :disabled="loading">
            {{ loading ? 'Loading…' : 'Reload' }}
          </button>
        </div>

        <div v-if="error" class="alert alert-warning d-flex align-items-center justify-content-between" role="alert">
          <div>
            <div class="fw-semibold">Couldn't load from API.</div>
            <div class="small">Showing sample listings instead. {{ error }}</div>
          </div>
          <button class="btn btn-sm btn-outline-dark" type="button" @click="reload">Try again</button>
        </div>

        <div v-if="loading" class="text-center py-5 text-muted">
          <p>Loading listings…</p>
        </div>

        <div v-else-if="filteredListings.length === 0" class="text-center py-5 text-muted">
          <p>No listings match your filters.</p>
        </div>

        <section v-else class="grid" aria-label="Pet listings">
          <ListingCard
            v-for="listing in filteredListings"
            :key="listing.id"
            :listing="listing"
            :favorited="listing.favorited"
            @click="goToDetails(listing.id)"
            @view="goToDetails(listing.id)"
            @contact="contactOwner"
            @favorite="handleFavorite"
          />
        </section>
      </div>
    </section>
  </main>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import ListingCard from '@/components/ListingCard.vue'
import type { Listing } from '../types/listing'
import { fetchListings, fetchRecommendedListings } from '../api/listings'
import { mockListings } from '../data/mockListings'
import { useAuthStore } from '../stores/auth'
import { addFavorite, removeFavorite, getFavoritedListings } from '../api/favorites'
import { getPet, getUserProfile } from '../api/profile'

const router = useRouter()
const auth = useAuthStore()

const loading = ref(false)
const error = ref<string | null>(null)
const listings = ref<Listing[]>([])
const favoritedListingIds = ref<Set<number>>(new Set())

const viewMode = ref<'all' | 'recommended'>('all')
const petType = ref('')

// Filters
const selectedBreed = ref('')
const selectedCity = ref('')

const petTypes = [
  { value: '', label: 'All pets', icon: new URL('../img/all_outline.png', import.meta.url).href, type: 'image' },
  { value: 'DOG', label: 'Dogs', icon: new URL('../img/dog_outline.png', import.meta.url).href, type: 'image' },
  { value: 'CAT', label: 'Cats', icon: new URL('../img/cat_outline.png', import.meta.url).href, type: 'image' },
  { value: 'Other', label: 'Other', icon: new URL('../img/bird_outline.png', import.meta.url).href, type: 'image' },
]

// Store pet details cache to avoid repeated API calls
const petDetailsCache = ref<Map<number, any>>(new Map())

// Store owner details cache
const ownerDetailsCache = ref<Map<number, any>>(new Map())

const breedOptions = computed(() => {
  const set = new Set<string>()
  for (const l of listings.value) {
    // Keep breed dropdown relevant to selected pet type
    if (petType.value) {
      if (petType.value === 'Other') {
        if (['DOG', 'CAT'].includes(getListingSpecies(l))) continue
      } else if (getListingSpecies(l) !== petType.value) {
        continue
      }
    }

    // Get breed from cached pet details
    const b = String(l.breed || (l.animalId ? petDetailsCache.value.get(l.animalId)?.breed : '') || '').trim()
    if (b) set.add(b)
  }
  return Array.from(set).sort((a, b) => a.localeCompare(b))
})

const cityOptions = computed(() => {
  const set = new Set<string>()
  for (const l of listings.value) {
    const c = String(l.city || (l.animalId ? petDetailsCache.value.get(l.animalId)?.locatedName : '') || '').trim().toLowerCase()
    if (c) set.add(c)
  }
  return Array.from(set).sort((a, b) => a.localeCompare(b))
})

const filteredListings = computed(() => {
  return listings.value
    .filter((l) => {
      if (!auth.isAuthenticated || !auth.user?.userId) return true
      return Number(l.ownerId) !== Number(auth.user.userId)
    })
    .filter((l) => {
      // Species filter
      if (!petType.value) return true
      const species = getListingSpecies(l)
      if (petType.value === 'Other') return Boolean(species) && !['DOG', 'CAT'].includes(species)
      return species === petType.value
    })
    .filter((l) => {
      // Breed filter - if no breed is selected, show all
      if (!selectedBreed.value || selectedBreed.value === '') return true

      const breed = String(l.breed || (l.animalId ? petDetailsCache.value.get(l.animalId)?.breed : '') || '').trim()
      return breed === selectedBreed.value
    })
    .filter((l) => {
      // City filter
      if (!selectedCity.value || selectedCity.value === '') return true

      const city = String(l.city || (l.animalId ? petDetailsCache.value.get(l.animalId)?.locatedName : '') || '').trim().toLowerCase()
      return city === selectedCity.value.toLowerCase()
    })
    .map((l) => {
      const id = Number((l as any).id || (l as any).listingId)
      return {
        ...l,
        favorited: Number.isFinite(id) && favoritedListingIds.value.has(id),
      }
    })
})

function getListingSpecies(listing: Listing): string {
  return String(listing.species || listing.petType || '').trim().toUpperCase()
}

let abort: AbortController | null = null

async function load() {
  loading.value = true
  error.value = null

  abort?.abort()
  abort = new AbortController()

  try {
    let data: Listing[]

    if (viewMode.value === 'recommended' && auth.isAuthenticated && auth.user?.userId) {
      // Load recommended listings
      data = await fetchRecommendedListings(auth.user.userId, { signal: abort.signal })
    } else {
      // Load all listings
      data = await fetchListings({ signal: abort.signal })
    }

    // Fetch pet images and owner details for each listing
    const listingsWithImages = await Promise.all(
      data.map(async (listing) => {
        const result = { ...listing }

        // The public listings API is backed by v_listings_enriched, so most card data
        // arrives already joined from the view. Fetch only when older responses are sparse.
        try {
          if (
            listing.animalId &&
            (!result.imageUrl || !result.animalName || !result.species || !result.city)
          ) {
            const pet = await getPet(listing.animalId)
            // Cache pet details for breed options
            petDetailsCache.value.set(listing.animalId, pet)
            result.imageUrl = pet.photoUrl || new URL('../img/all_outline.png', import.meta.url).href
            result.animalName = pet.name // Add pet name
            result.species = pet.species || pet.type
            result.petType = pet.species || pet.type
            result.breed = pet.breed || result.breed
            result.city = pet.locatedName || result.city
          } else if (listing.animalId) {
            petDetailsCache.value.set(listing.animalId, {
              name: result.animalName,
              species: result.species,
              breed: result.breed,
              locatedName: result.city,
              photoUrl: result.imageUrl,
            })
          }
        } catch (err) {
          console.error(`Failed to fetch pet ${listing.animalId}:`, err)
          result.imageUrl = new URL('../img/all_outline.png', import.meta.url).href
        }

        // Fetch owner details only if the enriched view did not provide them.
        try {
          if (listing.ownerId && (!result.ownerName || !result.ownerEmail)) {
            const owner = await getUserProfile(listing.ownerId)
            // Cache owner details
            ownerDetailsCache.value.set(listing.ownerId, owner)
            result.ownerName = `${owner.firstName} ${owner.lastName}` // Add owner name
            result.ownerEmail = owner.email
          } else if (listing.ownerId) {
            ownerDetailsCache.value.set(listing.ownerId, {
              firstName: result.ownerName,
              email: result.ownerEmail,
            })
          }
        } catch (err) {
          console.error(`Failed to fetch owner ${listing.ownerId}:`, err)
        }

        return result
      })
    )

    listings.value = listingsWithImages
  } catch (e) {
    const message = e instanceof Error ? e.message : String(e)
    error.value = message
    listings.value = mockListings
  }

  if (auth.isAuthenticated && auth.user?.userId) {
    await loadUserFavorites()
  } else {
    favoritedListingIds.value.clear()
  }

  loading.value = false
}

async function loadUserFavorites() {
  try {
    if (!auth.isAuthenticated || !auth.user?.userId) {
      favoritedListingIds.value.clear()
      return
    }
    const favorites = await getFavoritedListings(auth.user.userId)
    favoritedListingIds.value = new Set(
      favorites.map((f) => Number(f.listingId)).filter((id) => Number.isFinite(id) && id > 0),
    )
  } catch (error) {
    console.error('Failed to load favorites:', error)
    favoritedListingIds.value.clear()
  }
}

function reload() {
  load()
}

function goToDetails(id: string) {
  router.push({ name: 'listing-details', params: { id } })
}

async function handleFavorite(event: { listing: Listing; favorited: boolean }) {
  if (!auth.isAuthenticated || !auth.user?.userId) {
    alert('Please log in to save favorites')
    return
  }

  try {
    const listingId = Number((event.listing as any).id || (event.listing as any).listingId)

    if (event.favorited) {
      await addFavorite(auth.user.userId, listingId)
      favoritedListingIds.value.add(listingId)
    } else {
      await removeFavorite(auth.user.userId, listingId)
      favoritedListingIds.value.delete(listingId)
    }
  } catch (error) {
    console.error('Failed to update favorite:', error)
    alert('Failed to update favorite. Please try again.')
  }
}

function contactOwner(listing: Listing) {
  if (!listing.ownerEmail) {
    alert('This owner does not have a contact email available.')
    return
  }

  const petName = listing.animalName || listing.title || 'your pet'
  const subject = encodeURIComponent(`Question about ${petName} on Petify`)
  const body = encodeURIComponent(`Hi ${listing.ownerName || ''},\n\nI saw your listing for ${petName} on Petify and would like to know more.\n\nThanks!`)
  window.location.href = `mailto:${listing.ownerEmail}?subject=${subject}&body=${body}`
}

onMounted(load)
onBeforeUnmount(() => abort?.abort())

// Reset breed filter when pet type changes
watch(petType, () => {
  selectedBreed.value = ''
})
</script>

<style scoped>
.listings-main {
  margin: 0;
  padding: 0;
}

/* Hero Section */
.hero-section {
  background:
    linear-gradient(135deg, rgba(124, 45, 18, 0.92), rgba(249, 115, 22, 0.88)),
    url('../img/all_outline.png') right -120px center / 620px auto no-repeat;
  color: white;
  padding: 56px 40px 64px;
}

.hero-content {
  align-items: stretch;
  display: grid;
  gap: 32px;
  grid-template-columns: minmax(0, 1.45fr) minmax(280px, 0.55fr);
  margin: 0 auto;
  max-width: 1180px;
  width: 100%;
}

.hero-left {
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.hero-eyebrow {
  color: #bbf7d0;
  font-size: 0.82rem;
  font-weight: 800;
  margin: 0 0 10px;
  text-transform: uppercase;
}

.hero-title {
  font-size: 3rem;
  font-weight: 800;
  line-height: 1.12;
  margin: 0 0 14px;
  max-width: 680px;
}

.hero-subtitle {
  color: #fff7ed;
  font-size: 1.1rem;
  margin: 0;
  max-width: 560px;
}

.search-box {
  background: rgba(255, 255, 255, 0.96);
  border: 1px solid rgba(255, 255, 255, 0.72);
  border-radius: 8px;
  box-shadow: 0 18px 45px rgba(67, 20, 7, 0.22);
  color: #1f2937;
  margin-top: 28px;
  padding: 22px;
}

.pet-type-selector {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}

.pet-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 16px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: white;
  cursor: pointer;
  transition: background 0.18s ease, border-color 0.18s ease, color 0.18s ease;
  font-size: 0.875rem;
  font-weight: 500;
}

.pet-btn:hover {
  border-color: #d97706;
  background: #fef3c7;
}

.pet-btn.active {
  border-color: #d97706;
  background: #d97706;
  color: white;
}

.pet-icon {
  font-size: 1.5rem;
}

.pet-icon-img {
  width: 4.25rem;
  height: 3.4rem;
  object-fit: contain;
}

.search-inputs {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.filters {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  align-items: center;
}

.form-select {
  background-color: #ffffff;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  padding: 12px 16px;
  font-size: 1rem;
  transition: border-color 0.3s ease;
}

.form-select:focus {
  border-color: #d97706;
  outline: none;
  box-shadow: none;
}

.hero-right {
  align-items: center;
  display: flex;
  justify-content: center;
}

.benefits-card {
  align-self: center;
  background: rgba(255, 255, 255, 0.94);
  border: 1px solid rgba(255, 255, 255, 0.76);
  border-radius: 8px;
  box-shadow: 0 18px 45px rgba(67, 20, 7, 0.2);
  color: #111827;
  max-width: 340px;
  padding: 24px;
  width: 100%;
}

.benefits-title {
  color: #111827;
  font-size: 1.2rem;
  font-weight: 800;
  margin-bottom: 16px;
  margin-top: 0;
}

.benefits-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.benefits-list li {
  color: #374151;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 0;
  font-size: 1rem;
  line-height: 1.5;
}

.benefit-icon {
  align-items: center;
  background: #dcfce7;
  border: 1px solid #86efac;
  border-radius: 999px;
  display: inline-flex;
  flex-shrink: 0;
  height: 24px;
  justify-content: center;
  width: 24px;
}

.benefit-icon::after {
  background: #16a34a;
  border-radius: 999px;
  content: '';
  height: 8px;
  width: 8px;
}

/* Listings Section */
.listings-section {
  padding: 60px 40px;
  background: #f9fafb;
}

.listings-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32px;
}

.listings-title {
  font-size: 2rem;
  font-weight: 700;
  margin: 0;
  color: #111827;
}

.listings-subtitle {
  margin: 8px 0 0;
  color: #6b7280;
  font-size: 1rem;
}

.view-mode-tabs {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.view-mode-btn {
  padding: 8px 16px;
  border: 2px solid #e5e7eb;
  border-radius: 8px;
  background: white;
  color: #6b7280;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.view-mode-btn:hover {
  border-color: #d1d5db;
  color: #374151;
}

.view-mode-btn.active {
  border-color: #f97316;
  background: #f97316;
  color: white;
}

/* Grid */
.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 24px;
}

/* Responsive */
@media (max-width: 991.98px) {
  .hero-section {
    padding: 40px 20px;
    min-height: auto;
  }

  .hero-content {
    grid-template-columns: 1fr;
    gap: 32px;
  }

  .hero-left {
    padding-right: 0;
  }

  .hero-title {
    font-size: 2.5rem;
  }

  .hero-subtitle {
    font-size: 1rem;
  }

  .search-box {
    padding: 20px;
  }

  .pet-type-selector {
    grid-template-columns: repeat(4, 1fr);
    gap: 8px;
  }

  .pet-btn {
    padding: 12px 8px;
    font-size: 0.75rem;
  }

  .pet-icon {
    font-size: 1.25rem;
  }

  .filters {
    grid-template-columns: 1fr;
  }

  .listings-section {
    padding: 40px 20px;
  }

  .listings-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .listings-title {
    font-size: 1.5rem;
  }

  .grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }
}
</style>
