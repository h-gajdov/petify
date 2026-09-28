<template xmlns="http://www.w3.org/1999/html">
  <div class="profile-container">
    <!-- Header with back button -->
    <header class="header-section header-simple">
      <div class="container">
        <RouterLink to="/" class="back-link">← Back to listings</RouterLink>
      </div>
    </header>

    <!-- Loading State -->
    <div v-if="isLoading" class="container py-5 text-center text-muted">
      <p>Loading owner profile…</p>
    </div>

    <!-- Error State -->
    <div v-else-if="error" class="container py-5">
      <div class="alert alert-danger" role="alert">
        <div class="fw-semibold">Failed to load owner profile</div>
        <div class="small">{{ error }}</div>
        <button class="btn btn-sm btn-outline-danger mt-2" type="button" @click="reload">
          Try again
        </button>
      </div>
    </div>

    <!-- Owner Profile Content -->
    <div v-else-if="ownerInfo" class="profile-container">
      <!-- Owner Info Card -->
      <section class="header-section">
        <div class="container">
          <div class="profile-card">
            <div class="profile-content">
              <div class="profile-info">
                <div style="display: flex; align-items: center; gap: 12px;">
                  <h1 class="profile-name">{{ ownerInfo.firstName }} {{ ownerInfo.lastName }}</h1>
                  <span v-if="ownerInfo.verified" class="verified-badge">
                    <img src="@/img/star.png" alt="verified" class="badge-star" /> Top 10
                  </span>
                </div>
                <p class="profile-username">@{{ ownerInfo.username }}</p>
                <p class="profile-email">
                  <i class="bi bi-envelope"></i>
                  <a :href="`mailto:${ownerInfo.email}`">{{ ownerInfo.email }}</a>
                </p>
              </div>
              <div class="profile-badge">
                <button class="btn btn-primary btn-sm" type="button" @click="contactOwner">
                  <i class="bi bi-envelope"></i> Contact Owner
                </button>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- Tabs for listings and pets -->
      <section class="main-content">
        <div class="container">
          <div class="tabs-container">
            <ul class="nav nav-tabs nav-fill" role="tablist">
              <li class="nav-item" role="presentation">
                <button
                  class="nav-link"
                  :class="{ active: activeTab === 'listings' }"
                  @click="activeTab = 'listings'"
                  type="button"
                  role="tab"
                >
                  <i class="bi bi-bookmark-fill"></i> Active Listings ({{ activeOwnerListings.length }})
                </button>
              </li>
              <li class="nav-item" role="presentation">
                <button
                  class="nav-link"
                  :class="{ active: activeTab === 'pets' }"
                  @click="activeTab = 'pets'"
                  type="button"
                  role="tab"
                >
                  <i class="bi bi-paw-fill"></i> Pets ({{ ownerPets.length }})
                </button>
              </li>
              <li class="nav-item" role="presentation">
                <button
                  class="nav-link"
                  :class="{ active: activeTab === 'reviews' }"
                  @click="activeTab = 'reviews'"
                  type="button"
                  role="tab"
                >
                  <i class="bi bi-star-fill"></i> Reviews ({{ ownerReviews.length }})
                </button>
              </li>
            </ul>

            <!-- Listings Tab -->
            <div v-if="activeTab === 'listings'" class="tab-content-section">
              <h2 class="section-title">Active Listings</h2>

              <div v-if="activeOwnerListings.length === 0" class="empty-state">
                <p>This owner doesn't have active listings right now.</p>
              </div>

              <div v-else class="grid-container">
                <div v-for="listing in activeOwnerListings" :key="listing.listingId" class="listing-card-wrapper">
                  <RouterLink :to="`/listing/${listing.listingId}`" class="listing-link">
                    <div class="listing-card">
                      <div class="listing-status" :class="statusClass(listing.status)">
                        {{ listing.status || 'Active' }}
                      </div>

                      <h3 class="listing-title">{{ getPetName(listing.animalId) }}</h3>
                      <p class="listing-description">{{ listing.description }}</p>

                      <div class="listing-footer">
                        <span class="listing-price" v-if="hasPrice(listing.price)">
                          ${{ formatPrice(listing.price) }}
                        </span>
                        <span class="listing-date">
                          {{ formatDate(listing.createdAt) }}
                        </span>
                      </div>
                    </div>
                  </RouterLink>
                </div>
              </div>
            </div>

            <!-- Pets Tab -->
            <div v-if="activeTab === 'pets'" class="tab-content-section">
              <h2 class="section-title">Owner's Pets</h2>

              <div v-if="ownerPets.length === 0" class="empty-state">
                <p>This owner hasn't added any pets yet.</p>
              </div>

              <div v-else class="grid-container">
                <div v-for="pet in ownerPets" :key="pet.animalId" class="pet-card-wrapper">
                  <div class="pet-card">
                    <div class="pet-image-wrapper">
                      <img
                        v-if="pet.photoUrl"
                        :src="pet.photoUrl"
                        :alt="pet.name"
                        class="pet-image"
                        @error="onPetImageError"
                      />
                      <img v-else src="@/img/all_outline.png" :alt="`${pet.name} placeholder`" class="pet-image-placeholder-img" />
                    </div>
                    <div class="pet-header">
                      <h3 class="pet-name">{{ pet.name }}</h3>
                    </div>
                    <div class="pet-details">
                      <div v-if="pet.species" class="pet-detail-row">
                        <span class="label">Species</span>
                        <span class="value">{{ pet.species }}</span>
                      </div>
                      <div v-if="pet.breed" class="pet-detail-row">
                        <span class="label">Breed</span>
                        <span class="value">{{ pet.breed }}</span>
                      </div>
                      <div v-if="pet.sex" class="pet-detail-row">
                        <span class="label">Sex</span>
                        <span class="value">{{ pet.sex }}</span>
                      </div>
                      <div v-if="pet.dateOfBirth" class="pet-detail-row">
                        <span class="label">DOB</span>
                        <span class="value">{{ formatDate(pet.dateOfBirth) }}</span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <!-- Reviews Tab -->
            <div v-if="activeTab === 'reviews'" class="tab-content-section">
              <h2 class="section-title">Reviews ({{ ownerReviews.length }})</h2>

              <!-- Add Review Form (only if logged in and not own profile) -->
              <div
                v-if="auth.isAuthenticated && ownerInfo && auth.user?.userId !== ownerInfo.userId"
                class="form-card"
              >
                <h3 class="section-title" style="font-size: 1.3rem; margin-top: 0">Leave a Review</h3>
                <form @submit.prevent="submitReview">
                  <div class="form-group">
                    <label class="form-label">Rating</label>
                    <div class="rating-input">
                      <button
                        v-for="i in 5"
                        :key="i"
                        type="button"
                        :class="['star-btn', { active: newReview.rating === i }]"
                        @click="newReview.rating = i"
                      >
                        <img
                          :src="starImg"
                          :alt="`${i} star rating`"
                          class="star-btn-img"
                          :style="{ opacity: i <= newReview.rating ? 1 : 0.3 }"
                        />


                      </button>
                    </div>
                  </div>

                  <div class="form-group">
                    <label class="form-label" for="comment">Comment</label>
                    <textarea
                      id="comment"
                      v-model="newReview.comment"
                      class="form-control"
                      placeholder="Share your experience with this owner..."
                      rows="4"
                    ></textarea>
                  </div>

                  <div v-if="reviewError" class="alert alert-danger" role="alert">
                    {{ reviewError }}
                  </div>

                  <div class="form-actions">
                    <button
                      type="submit"
                      class="btn btn-primary"
                      :disabled="isSubmittingReview || newReview.rating === 0"
                    >
                      <span v-if="isSubmittingReview">Submitting...</span>
                      <span v-else>Submit Review</span>
                    </button>
                  </div>
                </form>
              </div>

              <!-- Reviews List -->
              <div v-if="ownerReviews.length === 0" class="empty-state">
                <p>No reviews yet. Be the first to leave a review!</p>
              </div>

              <div v-else class="reviews-list">
                <div v-for="review in ownerReviews" :key="review.reviewId" class="review-card">
                  <div class="review-header">
                    <div class="reviewer-info">
                      <h4 class="reviewer-name">{{ review.reviewerName }}</h4>
                      <p class="reviewer-username">@{{ review.reviewerUsername }}</p>
                    </div>
                    <div class="review-actions">
                      <div class="rating">
                        <img
                          v-for="i in Number(review.rating || 0)"
                          :key="i"
                          src="@/img/star.png"
                          alt="star"
                          class="review-star"
                        />
                      </div>
                      <button
                        v-if="
                          auth.isAuthenticated &&
                          (auth.user?.userId === review.reviewerId || auth.user?.userId === ownerInfo?.userId)
                        "
                        type="button"
                        class="delete-btn"
                        @click="deleteReview(review.reviewId)"
                      >
                        <img src="@/img/trashcan.png" alt="delete" class="delete-btn-img" />
                      </button>
                    </div>
                  </div>
                  <p class="review-comment">{{ review.comment }}</p>
                  <p class="review-date">{{ formatDate(review.createdAt) }}</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>
      </div>
    </div>
</template>

<script setup lang="ts">
import starImg from '@/img/star.png'
import { ref, onMounted, onBeforeUnmount, watch, computed } from 'vue'
import { useRoute, RouterLink } from 'vue-router'
import { getUserProfile, getUserListings, getUserPets, loadUserVerificationStatus } from '../api/profile'
import { createReview, getReviewsByOwner, deleteReview as deleteReviewAPI } from '../api/reviews'
import { useAuthStore } from '../stores/auth'

const route = useRoute()
const auth = useAuthStore()

const isLoading = ref(false)
const error = ref<string | null>(null)

const activeTab = ref<'listings' | 'pets' | 'reviews'>('listings')

const ownerInfo = ref<any>(null)
const ownerListings = ref<any[]>([])
const ownerPets = ref<any[]>([])
const ownerReviews = ref<any[]>([])

const isSubmittingReview = ref(false)
const reviewError = ref<string | null>(null)
const newReview = ref({
  rating: 0,
  comment: '',
})

// Create a map of petId to pet name
const petNameMap = computed(() => {
  const map: Record<number, string> = {}
  ownerPets.value.forEach((pet) => {
    map[pet.animalId] = pet.name
  })
  return map
})

const activeOwnerListings = computed(() => {
  return ownerListings.value.filter((listing) => String(listing.status || 'ACTIVE').toUpperCase() === 'ACTIVE')
})

// Get pet name for listing
function getPetName(animalId: number): string {
  return petNameMap.value[animalId] || 'Unknown Pet'
}

let abort: AbortController | null = null

function extractArray(res: any): any[] {
  if (Array.isArray(res)) return res
  if (res?.content && Array.isArray(res.content)) return res.content
  if (res?.data && Array.isArray(res.data)) return res.data
  return []
}

async function load() {
  isLoading.value = true
  error.value = null

  ownerInfo.value = null
  ownerListings.value = []
  ownerPets.value = []
  ownerReviews.value = []

  abort?.abort()
  abort = new AbortController()

  try {
    const id = Number(route.params.ownerId)
    if (Number.isNaN(id)) throw new Error('Invalid owner ID')

    // NOTE: AbortController is kept to cancel UI state updates on route change;
    // if your API layer supports fetch signals, pass abort.signal inside those functions.
    const [userInfo, listingsRes, petsRes, reviewsRes] = await Promise.all([
      getUserProfile(id),
      getUserListings(id),
      getUserPets(id),
      getReviewsByOwner(id),
    ])

    ownerInfo.value = userInfo
    ownerListings.value = extractArray(listingsRes)
    ownerPets.value = extractArray(petsRes)
    ownerReviews.value = extractArray(reviewsRes)

    // Load verification status
    await loadOwnerVerification()
  } catch (e) {
    const message = e instanceof Error ? e.message : String(e)
    error.value = message
    console.error('Failed to load owner profile:', message)
  } finally {
    isLoading.value = false
  }
}

function reload() {
  load()
}

watch(
  () => route.params.ownerId,
  () => {
    // if user opens a different owner profile without leaving the page
    load()
  }
)

function onPetImageError(event: Event) {
  const img = event.target as HTMLImageElement
  img.style.display = 'none'
}

function formatDate(dateString: string): string {
  if (!dateString) return ''
  try {
    const date = new Date(dateString)
    if (Number.isNaN(date.getTime())) return ''
    return new Intl.DateTimeFormat(undefined, {
      month: 'short',
      day: 'numeric',
      year: 'numeric',
    }).format(date)
  } catch {
    return ''
  }
}

function hasPrice(price: unknown): boolean {
  const n = typeof price === 'number' ? price : Number(price)
  return Number.isFinite(n) && n > 0
}

function formatPrice(price: unknown): string {
  const n = typeof price === 'number' ? price : Number(price)
  if (!Number.isFinite(n)) return ''
  return n.toFixed(2)
}

function statusClass(status: unknown): string {
  const s = String(status ?? 'active').toLowerCase()
  return `status-${s}`
}

function contactOwner() {
  if (!ownerInfo.value?.email) {
    alert('Owner email not available')
    return
  }
  window.location.href = `mailto:${ownerInfo.value.email}`
}

async function submitReview() {
  if (!auth.isAuthenticated || !auth.user?.userId) {
    alert('Please log in to submit a review')
    return
  }

  if (newReview.value.rating === 0) {
    reviewError.value = 'Please select a rating'
    return
  }

  isSubmittingReview.value = true
  reviewError.value = null

  try {
    await createReview(
      ownerInfo.value.userId,
      auth.user.userId,
      newReview.value.rating,
      newReview.value.comment
    )

    // Reset form and reload reviews
    newReview.value.rating = 0
    newReview.value.comment = ''
    await loadReviews()
  } catch (err) {
    reviewError.value = err instanceof Error ? err.message : 'Failed to submit review'
  } finally {
    isSubmittingReview.value = false
  }
}

async function loadReviews() {
  if (!ownerInfo.value?.userId) return
  try {
    const res = await getReviewsByOwner(ownerInfo.value.userId)
    ownerReviews.value = extractArray(res)
  } catch (err) {
    console.error('Failed to load reviews:', err)
  }
}

async function loadOwnerVerification() {
  if (!ownerInfo.value?.userId) return

  try {
    const isVerified = await loadUserVerificationStatus(ownerInfo.value.userId)
    if (ownerInfo.value) {
      ownerInfo.value.verified = isVerified
    }
    console.log(`✅ Owner verification status loaded: ${isVerified}`)
  } catch (error) {
    console.error('Failed to load owner verification status:', error)
  }
}

async function deleteReview(reviewId: number) {
  if (!auth.isAuthenticated || !auth.user?.userId) {
    alert('Please log in to delete a review')
    return
  }

  if (confirm('Are you sure you want to delete this review?')) {
    try {
      await deleteReviewAPI( reviewId, auth.user.userId)
      await loadReviews()
    } catch (err) {
      alert(err instanceof Error ? err.message : 'Failed to delete review')
    }
  }
}

onMounted(load)
onBeforeUnmount(() => abort?.abort())
</script>

<style scoped>
.profile-container {
  background: linear-gradient(135deg, #f5f7fa 0%, #f0f3f8 100%);
  min-height: 100vh;
  padding-bottom: 60px;
}

/* Header Section */
.header-section {
  background: linear-gradient(135deg, #f97316 0%, #ea580c 100%);
  padding: 40px 0;
  margin-bottom: 40px;
  box-shadow: 0 10px 30px rgba(249, 115, 22, 0.2);
}

.header-section.header-simple {
  background: white;
  padding: 20px 0;
  margin-bottom: 0;
  box-shadow: none;
  border-bottom: 1px solid #e2e8f0;
}

.back-link {
  display: inline-flex;
  align-items: center;
  color: #f97316;
  text-decoration: none;
  font-weight: 600;
  font-size: 0.95rem;
  transition: all 0.2s ease;
}

.back-link:hover {
  color: #ea580c;
  transform: translateX(-4px);
}

.profile-card {
  background: white;
  border-radius: 16px;
  padding: 30px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.profile-content {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 30px;
}

.profile-info {
  flex: 1;
}

.profile-name {
  font-size: 2.5rem;
  font-weight: 700;
  color: #1a202c;
  margin: 0 0 12px 0;
  letter-spacing: -0.5px;
}

.profile-username {
  font-size: 1.1rem;
  color: #718096;
  margin: 0 0 8px 0;
  font-weight: 500;
}

.profile-email {
  font-size: 1rem;
  color: #4a5568;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.profile-email a {
  color: #4a5568;
  text-decoration: none;
  transition: color 0.2s ease;
}

.profile-email a:hover {
  color: #2d3748;
}

.profile-badge {
  display: flex;
  align-items: center;
}

.verified-badge {
  background: linear-gradient(135deg, #fbbf24 0%, #f59e0b 100%);
  color: white;
  padding: 6px 14px;
  border-radius: 20px;
  font-size: 0.85rem;
  font-weight: 600;
  white-space: nowrap;
  display: flex;
  align-items: center;
  gap: 6px;
  box-shadow: 0 2px 8px rgba(245, 158, 11, 0.3);
}

.badge-star {
  width: 18px;
  height: 18px;
  object-fit: contain;
  filter: brightness(0) invert(1);
}

/* Main Content */
.main-content {
  padding: 0;
}

.tabs-container {
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

/* Tabs */
.nav-tabs {
  border-bottom: 2px solid #e2e8f0;
  background: #f7fafc;
  padding: 0;
  margin: 0;
}

.nav-tabs .nav-link {
  color: #718096;
  border: none;
  border-bottom: 3px solid transparent;
  font-weight: 600;
  padding: 16px 24px;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 0.95rem;
}

.nav-tabs .nav-link:hover {
  color: #2d3748;
  background: #edf2f7;
}

.nav-tabs .nav-link.active {
  color: #f97316;
  border-bottom-color: #f97316;
  background: white;
}

/* Tab Content */
.tab-content-section {
  padding: 40px;
  animation: fadeIn 0.3s ease-in;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.section-title {
  font-size: 1.8rem;
  font-weight: 700;
  color: #1a202c;
  margin: 0 0 30px 0;
  letter-spacing: -0.5px;
}

/* Empty State */
.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #718096;
}

.empty-state p {
  font-size: 1.1rem;
  margin: 0;
}

/* Grid Container */
.grid-container {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 24px;
}

/* Listing Card */
.listing-card-wrapper {
  height: 100%;
}

.listing-card {
  background: white;
  border: none;
  border-radius: 16px;
  padding: 24px;
  height: 100%;
  display: flex;
  flex-direction: column;
  gap: 16px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  position: relative;
}

.listing-card:hover {
  box-shadow: 0 16px 32px rgba(249, 115, 22, 0.12);
  transform: translateY(-8px);
}

.listing-link {
  text-decoration: none;
  display: block;
  height: 100%;
}

.listing-status {
  position: absolute;
  top: 16px;
  right: 16px;
  padding: 8px 14px;
  border-radius: 8px;
  font-size: 0.75rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.status-active {
  background: #d1fae5;
  color: #065f46;
}

.status-pending {
  background: #fef3c7;
  color: #92400e;
}

.status-adopted {
  background: #dbeafe;
  color: #0c2d6b;
}

.listing-title {
  font-size: 1.3rem;
  font-weight: 700;
  color: #1a202c;
  margin: 0;
  line-height: 1.4;
}

.listing-description {
  color: #4a5568;
  font-size: 0.95rem;
  line-height: 1.6;
  margin: 0;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.listing-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 16px;
  margin-top: auto;
}

.listing-price {
  font-size: 1.5rem;
  font-weight: 800;
  color: #f97316;
  letter-spacing: -0.5px;
}

.listing-date {
  color: #a0aec0;
  font-size: 0.85rem;
  font-weight: 500;
}

.listing-actions {
  display: flex;
  gap: 8px;
  margin-top: auto;
}

.listing-actions .form-select {
  flex: 1;
}

/* Pet Card */
.pet-card-wrapper {
  height: 100%;
}

.pet-card {
  background: white;
  border: none;
  border-radius: 16px;
  padding: 0;
  height: 100%;
  display: flex;
  flex-direction: column;
  gap: 16px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

.pet-card:hover {
  box-shadow: 0 16px 32px rgba(249, 115, 22, 0.12);
  transform: translateY(-8px);
}

.pet-image-wrapper {
  width: 100%;
  height: 220px;
  background: linear-gradient(135deg, #f5f7fa 0%, #f0f3f8 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.pet-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.pet-image-placeholder {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
  display: flex;
  align-items: center;
  justify-content: center;
}

.pet-emoji {
  font-size: 3rem;
}

.pet-image-placeholder-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.pet-header {
  border-bottom: none;
  padding: 0 20px 0 20px;
  padding-top: 16px;
}

.pet-name {
  font-size: 1.3rem;
  font-weight: 700;
  margin: 0;
  color: #1a202c;
  line-height: 1.4;
}

.pet-details {
  list-style: none;
  padding: 0 20px 20px 20px;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
  flex: 1;
}

.pet-detail-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: none;
  font-size: 0.9rem;
}

.pet-detail-row:last-child {
  border-bottom: none;
}

.pet-detail-row .label {
  color: #718096;
  font-weight: 600;
  text-transform: capitalize;
  font-size: 0.9rem;
}

.pet-detail-row .value {
  color: #2d3748;
  font-weight: 500;
}

/* Review Card */
.review-card {
  background: white;
  border: none;
  border-radius: 16px;
  padding: 24px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.review-card:hover {
  box-shadow: 0 12px 24px rgba(249, 115, 22, 0.1);
  transform: translateY(-4px);
}

/* Form Card */
.form-card {
  background: white;
  border: none;
  border-radius: 16px;
  padding: 32px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  margin-bottom: 32px;
}

.form-group {
  margin-bottom: 20px;
}

.form-label {
  display: block;
  font-weight: 600;
  color: #2d3748;
  margin-bottom: 8px;
  font-size: 0.95rem;
}

.rating-input {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.star-btn {
  background: white;
  border: 2px solid #e2e8f0;
  border-radius: 8px;
  padding: 10px 14px;
  font-size: 1.5rem;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.star-btn:hover {
  border-color: #f97316;
  background: #fff8f1;
}

.star-btn.active {
  border-color: #f97316;
  background: #fff8f1;
}

.star-btn-img {
  width: 1.5rem;
  height: 1.5rem;
  object-fit: contain;
}

.form-control {
  width: 100%;
  padding: 12px;
  border: 1.5px solid #e2e8f0;
  border-radius: 8px;
  font-family: inherit;
  font-size: 0.95rem;
  color: #1a202c;
  resize: vertical;
  transition: all 0.2s ease;
}

.form-control:focus {
  outline: none;
  border-color: #f97316;
  box-shadow: 0 0 0 3px rgba(249, 115, 22, 0.1);
}

.form-actions {
  display: flex;
  gap: 12px;
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid #e2e8f0;
}

.reviews-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* Buttons */
.btn {
  border-radius: 8px;
  font-weight: 600;
  padding: 10px 20px;
  transition: all 0.2s ease;
  font-size: 0.95rem;
  border: none;
  cursor: pointer;
}

.btn-primary {
  background: linear-gradient(135deg, #f97316 0%, #ea580c 100%);
  color: white;
}

.btn-primary:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(249, 115, 22, 0.3);
}

.btn-sm {
  padding: 6px 12px;
  font-size: 0.85rem;
}

.btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-outline-danger {
  border: 1.5px solid #f56565;
  color: #f56565;
  background: transparent;
}

.btn-outline-danger:hover {
  background: #fff5f5;
  border-color: #e53e3e;
}

/* Alerts */
.alert {
  padding: 16px;
  border-radius: 8px;
  margin-bottom: 16px;
}

.alert-danger {
  background: #fee2e2;
  color: #991b1b;
  border: 1px solid #fecaca;
}


.review-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
  flex-wrap: wrap;
  gap: 12px;
}

.reviewer-info {
  flex: 1;
}

.reviewer-name {
  font-size: 1rem;
  font-weight: 700;
  margin: 0 0 4px 0;
  color: #111827;
}

.reviewer-username {
  font-size: 0.875rem;
  color: #6b7280;
  margin: 0;
}

.review-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.rating {
  font-size: 1rem;
  color: #f59e0b;
  display: flex;
  gap: 6px;
  align-items: center;
}

.review-star {
  width: 1.5rem;
  height: 1.5rem;
  object-fit: contain;
}

.delete-btn {
  background: none;
  border: none;
  cursor: pointer;
  opacity: 0.6;
  transition: opacity 0.2s ease;
  padding: 4px 8px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.delete-btn:hover {
  opacity: 1;
}

.delete-btn-img {
  width: 1.25rem;
  height: 1.25rem;
  object-fit: contain;
}

.review-comment {
  color: #374151;
  line-height: 1.6;
  margin: 0 0 12px 0;
  white-space: pre-wrap;
  word-break: break-word;
}

.review-date {
  font-size: 0.875rem;
  color: #9ca3af;
  margin: 0;
}

/* Responsive */
@media (max-width: 768px) {
  .owner-card {
    padding: 24px;
  }

  .owner-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .owner-name {
    font-size: 1.75rem;
  }

  .contact-actions {
    width: 100%;
  }

  .btn-contact {
    flex: 1;
  }

  .tab-content {
    padding: 24px;
  }

  .listings-grid,
  .pets-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 576px) {
  .header-section {
    padding: 12px 0;
  }

  .back-link {
    font-size: 0.875rem;
  }

  .owner-card {
    padding: 16px;
  }

  .owner-name {
    font-size: 1.5rem;
  }

  .owner-username,
  .owner-email {
    font-size: 0.875rem;
  }

  .tabs-header {
    flex-direction: column;
  }

  .tab-button {
    padding: 16px;
    border-right: 3px solid transparent;
    border-bottom: none;
  }

  .tab-button.active {
    border-right-color: #d97706;
    border-bottom-color: transparent;
  }

  .tab-content {
    padding: 16px;
  }

  .section-title {
    font-size: 1.25rem;
  }
}
</style>
