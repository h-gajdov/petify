<template>
  <main class="admin-listings">
    <section class="header-section">
      <div class="container">
        <h1 class="page-title">Listings</h1>
        <p class="page-subtitle">
          Browse all listings in the system. Results are paginated (500 per page).
        </p>
      </div>
    </section>

    <section class="container listings-body">
      <div v-if="errorMessage" class="alert alert-danger">{{ errorMessage }}</div>

      <section class="panel">
        <div class="panel-header">
          <div>
            <h2>All Listings</h2>
            <p v-if="pageData" class="panel-subtitle">
              Total: {{ pageData.totalItems }} | Active: {{ pageData.activeListings }} | Sold: {{ pageData.soldListings }}
            </p>
          </div>

          <div class="panel-actions">
            <button class="btn btn-sm btn-outline-secondary" type="button" :disabled="isLoading" @click="refresh">
              Refresh
            </button>
          </div>
        </div>

        <div class="pagination-bar" v-if="pageData">
          <button
            class="btn btn-sm btn-outline-secondary"
            type="button"
            :disabled="isLoading || !pageData.hasPrevious"
            @click="goPrevious"
          >
            Previous
          </button>

          <div class="page-meta">
            Page {{ pageData.page + 1 }} / {{ Math.max(1, pageData.totalPages) }} (size: {{ pageData.size }})
          </div>

          <button
            class="btn btn-sm btn-outline-secondary"
            type="button"
            :disabled="isLoading || !pageData.hasNext"
            @click="goNext"
          >
            Next
          </button>
        </div>

        <div v-if="pageData" class="filter-bar">
          <div class="filter-field search-field">
            <label for="listingSearch">Search</label>
            <input
              id="listingSearch"
              v-model="filters.search"
              class="form-control form-control-sm"
              placeholder="Pet, owner, or description"
            />
          </div>

          <div class="filter-field">
            <label for="listingStatus">Status</label>
            <select id="listingStatus" v-model="filters.status" class="form-select form-select-sm">
              <option value="">All</option>
              <option v-for="status in availableStatuses" :key="status" :value="status">{{ status }}</option>
            </select>
          </div>

          <div class="filter-field">
            <label for="minPrice">Min price</label>
            <input id="minPrice" v-model="filters.minPrice" class="form-control form-control-sm" type="number" min="0" step="1" @change="applyFilters" />
          </div>

          <div class="filter-field">
            <label for="maxPrice">Max price</label>
            <input id="maxPrice" v-model="filters.maxPrice" class="form-control form-control-sm" type="number" min="0" step="1" @change="applyFilters" />
          </div>

          <button class="btn btn-sm btn-outline-secondary clear-filters" type="button" @click="clearFilters">
            Clear
          </button>
        </div>

        <p v-if="pageData && hasActiveFilters" class="filter-summary">
          Showing {{ filteredListings.length }} of {{ pageData.totalItems }} listings.
        </p>

        <div v-if="isLoading" class="alert alert-info">Loading listings...</div>

        <div v-if="!isLoading && (!pageData || pageData.items.length === 0)" class="empty-state">
          No listings found.
        </div>

        <div v-else-if="!isLoading && filteredListings.length === 0" class="empty-state">
          No listings match the selected filters.
        </div>

        <div v-if="!isLoading && filteredListings.length" class="listings-grid">
          <article v-for="listing in filteredListings" :key="listing.listingId" class="listing-card">
            <div class="listing-card-header">
              <div>
                <RouterLink v-if="listing.listingId" class="listing-title" :to="`/listing/${listing.listingId}`">
                  {{ getAnimalName(listing.animalId) }}
                </RouterLink>
                <h3 v-else class="listing-title">{{ getAnimalName(listing.animalId) }}</h3>
                <p class="listing-owner">{{ getOwnerName(listing.ownerId) }}</p>
              </div>
              <span class="badge" :class="statusClass(listing.status)">{{ listing.status }}</span>
            </div>

            <p class="listing-description">{{ listing.description || 'No description' }}</p>

            <div class="listing-footer">
              <span class="listing-price">{{ formatPrice(listing.price) }}</span>
              <span class="listing-date">{{ formatDate(listing.createdAt) }}</span>
            </div>
          </article>
        </div>

        <div class="pagination-bar" v-if="pageData">
          <button
            class="btn btn-sm btn-outline-secondary"
            type="button"
            :disabled="isLoading || !pageData.hasPrevious"
            @click="goPrevious"
          >
            Previous
          </button>

          <div class="page-meta">
            Page {{ pageData.page + 1 }} / {{ Math.max(1, pageData.totalPages) }}
          </div>

          <button
            class="btn btn-sm btn-outline-secondary"
            type="button"
            :disabled="isLoading || !pageData.hasNext"
            @click="goNext"
          >
            Next
          </button>
        </div>
      </section>
    </section>
  </main>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { getAllListings, getAllUsers, type AdminListingsPage } from '../api/admin'
import { getPet } from '../api/profile'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const auth = useAuthStore()

const pageData = ref<AdminListingsPage | null>(null)
const page = ref(0)
const isLoading = ref(false)
const errorMessage = ref('')
const ownerNameMap = ref<Record<number, string>>({})
const animalNameMap = ref<Record<number, string>>({})
const filters = ref({
  search: '',
  status: '',
  minPrice: '',
  maxPrice: '',
})

const PAGE_SIZE = 500
const availableStatuses = ['ACTIVE', 'SOLD', 'DRAFT', 'ARCHIVED']

let latestListingsRequest = 0

const hasActiveFilters = computed(() => {
  return Boolean(filters.value.search.trim() || filters.value.status || filters.value.minPrice || filters.value.maxPrice)
})

const filteredListings = computed(() => {
  const listings = pageData.value?.items || []
  const search = filters.value.search.trim().toLowerCase()

  return listings.filter((listing: any) => {
    const matchesSearch = !search || [
      getAnimalName(listing.animalId),
      getOwnerName(listing.ownerId),
      listing.description || '',
      listing.status || '',
    ].join(' ').toLowerCase().includes(search)

    return matchesSearch
  })
})

async function loadPage(targetPage: number) {
  if (!auth.user?.userId) return

  const requestId = ++latestListingsRequest
  isLoading.value = true
  errorMessage.value = ''
  try {
    const data = await getAllListings(auth.user.userId, targetPage, PAGE_SIZE, {
      status: filters.value.status,
      minPrice: filters.value.minPrice,
      maxPrice: filters.value.maxPrice,
    })
    if (requestId !== latestListingsRequest) return
    pageData.value = data
    page.value = data.page
    await loadNamesForPage(data.items)
  } catch (error) {
    if (requestId !== latestListingsRequest) return
    pageData.value = null
    errorMessage.value = error instanceof Error ? error.message : 'Failed to load listings'
  } finally {
    if (requestId === latestListingsRequest) {
      isLoading.value = false
    }
  }
}

async function loadNamesForPage(listings: any[]) {
  const ownerIds = Array.from(new Set(
    listings
      .map((listing) => Number(listing.ownerId))
      .filter((ownerId) => Number.isFinite(ownerId) && !ownerNameMap.value[ownerId])
  ))
  const animalIds = Array.from(new Set(
    listings
      .map((listing) => Number(listing.animalId))
      .filter((animalId) => Number.isFinite(animalId) && !animalNameMap.value[animalId])
  ))

  if (ownerIds.length > 0 && auth.user?.userId) {
    try {
      const users = await getAllUsers(auth.user.userId)
      const nextOwnerMap = { ...ownerNameMap.value }
      users.forEach((user: any) => {
        if (user.userId) {
          nextOwnerMap[Number(user.userId)] = `${user.firstName || ''} ${user.lastName || ''}`.trim() || user.username || `Owner #${user.userId}`
        }
      })
      ownerNameMap.value = nextOwnerMap
    } catch (error) {
      console.error('Failed to load owner names:', error)
    }
  }

  if (animalIds.length > 0) {
    const entries = await Promise.all(animalIds.map(async (animalId) => {
      try {
        const pet = await getPet(animalId)
        return [animalId, pet.name || `Pet #${animalId}`] as const
      } catch {
        return [animalId, `Pet #${animalId}`] as const
      }
    }))
    animalNameMap.value = {
      ...animalNameMap.value,
      ...Object.fromEntries(entries),
    }
  }
}

function getOwnerName(ownerId: number | undefined) {
  if (!ownerId) return 'Unknown owner'
  return ownerNameMap.value[ownerId] || 'Loading owner...'
}

function getAnimalName(animalId: number | undefined) {
  if (!animalId) return 'Unknown pet'
  return animalNameMap.value[animalId] || 'Loading pet...'
}

function clearFilters() {
  filters.value = {
    search: '',
    status: '',
    minPrice: '',
    maxPrice: '',
  }
  void loadPage(0)
}

async function applyFilters() {
  await loadPage(0)
}

async function refresh() {
  await loadPage(page.value)
}

async function goNext() {
  if (!pageData.value?.hasNext) return
  await loadPage(page.value + 1)
}

async function goPrevious() {
  if (!pageData.value?.hasPrevious) return
  await loadPage(Math.max(0, page.value - 1))
}

function formatDate(value: string | undefined) {
  if (!value) return ''
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  return date.toLocaleString('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
  })
}

function formatPrice(value: unknown) {
  if (value === null || value === undefined) return ''
  const numberValue = Number(value)
  if (!Number.isFinite(numberValue)) return String(value)
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: 'USD',
  }).format(numberValue)
}

function statusClass(status: string) {
  const normalized = String(status || '').toUpperCase()
  if (normalized === 'ACTIVE') return 'bg-success'
  if (normalized === 'SOLD') return 'bg-secondary'
  if (normalized === 'PENDING') return 'bg-warning'
  return 'bg-dark'
}

watch(
  () => filters.value.status,
  async () => {
    if (!auth.isAuthenticated || auth.user?.userType !== 'ADMIN') return
    await loadPage(0)
  }
)

onMounted(async () => {
  if (!auth.isAuthenticated) {
    router.push('/login')
    return
  }
  if (auth.user?.userType !== 'ADMIN') {
    router.push('/')
    return
  }

  await loadPage(0)
})
</script>

<style scoped>
.admin-listings {
  min-height: 100vh;
  background: #f7fafc;
  padding-bottom: 48px;
}

.header-section {
  background: white;
  border-bottom: 1px solid #e2e8f0;
  padding: 32px 0;
}

.page-title {
  color: #1a202c;
  font-size: 2rem;
  margin: 0;
}

.page-subtitle {
  color: #718096;
  margin: 8px 0 0;
}

.listings-body {
  padding-top: 28px;
}

.panel {
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 24px;
}

.panel-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
}

.panel-header h2 {
  color: #1a202c;
  margin: 0;
}

.panel-subtitle {
  color: #718096;
  margin: 6px 0 0;
}

.panel-actions {
  display: flex;
  gap: 10px;
}

.pagination-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin: 12px 0;
}

.page-meta {
  color: #718096;
  font-weight: 600;
}

.filter-bar {
  align-items: end;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  display: grid;
  gap: 12px;
  grid-template-columns: minmax(220px, 1fr) 140px 120px 120px auto;
  margin: 16px 0;
  padding: 14px;
}

.filter-field {
  display: grid;
  gap: 6px;
}

.filter-field label {
  color: #4a5568;
  font-size: 0.82rem;
  font-weight: 700;
}

.clear-filters {
  min-width: 72px;
}

.filter-summary {
  color: #718096;
  font-size: 0.9rem;
  margin: -4px 0 14px;
}

.listings-grid {
  display: grid;
  gap: 12px;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
}

.listing-card {
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  gap: 14px;
  min-height: 190px;
  padding: 16px;
}

.listing-card-header {
  align-items: flex-start;
  display: flex;
  gap: 12px;
  justify-content: space-between;
}

.listing-title {
  color: #1a202c;
  display: block;
  font-size: 1.05rem;
  font-weight: 800;
  line-height: 1.25;
  text-decoration: none;
}

.listing-title:hover {
  color: #f97316;
}

.listing-owner,
.listing-date {
  color: #718096;
  margin: 4px 0 0;
}

.listing-description {
  color: #4a5568;
  display: -webkit-box;
  line-height: 1.45;
  margin: 0;
  overflow: hidden;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 3;
}

.listing-footer {
  align-items: center;
  border-top: 1px solid #edf2f7;
  display: flex;
  justify-content: space-between;
  margin-top: auto;
  padding-top: 12px;
}

.listing-price {
  color: #f97316;
  font-size: 1.05rem;
  font-weight: 800;
}

.badge {
  border-radius: 4px;
  color: white;
  font-size: 0.78rem;
  font-weight: 700;
  padding: 4px 8px;
}

.bg-success {
  background: #16a34a;
}

.bg-secondary {
  background: #64748b;
}

.bg-warning {
  background: #f59e0b;
}

.bg-dark {
  background: #111827;
}

.empty-state {
  color: #718096;
  padding: 20px;
  text-align: center;
}

@media (max-width: 900px) {
  .filter-bar {
    grid-template-columns: 1fr 1fr;
  }

  .search-field {
    grid-column: 1 / -1;
  }
}

@media (max-width: 560px) {
  .filter-bar,
  .pagination-bar,
  .panel-header {
    align-items: stretch;
    grid-template-columns: 1fr;
  }

  .pagination-bar,
  .panel-header {
    flex-direction: column;
  }
}
</style>
