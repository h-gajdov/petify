<template>
  <main class="admin-clients">
    <section class="header-section">
      <div class="container">
        <h1 class="page-title">Clients</h1>
        <p class="page-subtitle">Review client history and block accounts when review behavior requires moderation.</p>
      </div>
    </section>

    <section class="container moderation-body">
      <div v-if="errorMessage" class="alert alert-danger">{{ errorMessage }}</div>

      <section class="panel">
        <div class="panel-header">
          <h2>Client Review Moderation</h2>
          <input v-model="searchQuery" class="form-control search-input" placeholder="Search clients..." />
        </div>

        <div v-if="isClientsLoading" class="alert alert-info">Loading clients and owners...</div>

        <div class="client-layout">
          <div class="client-list">
            <div v-if="!isClientsLoading && filteredClients.length === 0" class="empty-state compact">
              No clients or owners found.
            </div>
            <button
              v-for="client in filteredClients"
              :key="client.userId"
              type="button"
              class="client-row"
              :class="{ active: selectedClient?.userId === client.userId }"
              @click="selectClient(client)"
            >
              <span>{{ client.firstName }} {{ client.lastName }}</span>
              <small>@{{ client.username }} | {{ client.userType }}</small>
              <small class="review-stats">
                {{ getClientReviewCount(client.userId) }} reviews for them | {{ getClientAverageRating(client.userId).toFixed(1) }} stars
              </small>
              <small v-if="client.isBlocked" class="blocked">Blocked: {{ client.blockedReason || 'No reason' }}</small>
            </button>
          </div>

          <div class="review-panel">
            <div v-if="!selectedClient" class="empty-state">Select a client or owner to inspect reviews.</div>
            <template v-else>
              <div class="item-header">
                <div>
                  <h3>{{ selectedClient.firstName }} {{ selectedClient.lastName }}</h3>
                  <p>@{{ selectedClient.username }} | {{ selectedClient.email }}</p>
                </div>
                <button
                  v-if="!selectedClient.isBlocked"
                  class="btn btn-sm btn-outline-danger"
                  type="button"
                  @click="blockSelectedClient"
                >
                  Block
                </button>
                <button
                  v-else
                  class="btn btn-sm btn-success"
                  type="button"
                  @click="unblockSelectedClient"
                >
                  Unblock
                </button>
              </div>

              <div class="review-columns">
                <section>
                  <h4>Reviews for them</h4>
                  <div v-if="reviewsForSelected.length === 0" class="empty-state compact">No reviews received.</div>
                  <article v-for="review in reviewsForSelected" :key="review.reviewId" class="review-card">
                    <div class="rating">{{ '★'.repeat(Number(review.rating || 0)) }}</div>
                    <p>{{ review.comment || 'No comment' }}</p>
                    <small>By @{{ review.reviewerUsername }} on {{ formatDate(review.createdAt) }}</small>
                  </article>
                </section>

                <section>
                  <h4>Reviews left by them</h4>
                  <div v-if="reviewsBySelected.length === 0" class="empty-state compact">No reviews left.</div>
                  <article v-for="review in reviewsBySelected" :key="review.reviewId" class="review-card">
                    <div class="rating">{{ '★'.repeat(Number(review.rating || 0)) }}</div>
                    <p>{{ review.comment || 'No comment' }}</p>
                    <small>{{ formatDate(review.createdAt) }}</small>
                  </article>
                </section>
              </div>
            </template>
          </div>
        </div>
      </section>
    </section>
  </main>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { blockUser, getAllUsers } from '../api/admin'
import { getReviewsByOwner, getReviewsLeftByUser, type Review } from '../api/reviews'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const auth = useAuthStore()

const users = ref<any[]>([])
const selectedClient = ref<any | null>(null)
const reviewsForSelected = ref<Review[]>([])
const reviewsBySelected = ref<Review[]>([])
const clientReviewStats = ref<Record<number, { count: number; average: number }>>({})
const searchQuery = ref('')
const errorMessage = ref('')
const isClientsLoading = ref(false)

const filteredClients = computed(() => {
  const query = searchQuery.value.trim().toLowerCase()
  return users.value
    .filter((user) => ['CLIENT', 'OWNER'].includes(normalizeUserType(user)))
    .filter((user) => {
      if (!query) return true
      return `${user.firstName || ''} ${user.lastName || ''} ${user.username || ''} ${user.email || ''}`.toLowerCase().includes(query)
    })
})

async function loadUsers() {
  if (!auth.user?.userId) return
  isClientsLoading.value = true
  errorMessage.value = ''
  try {
    const allUsers = await getAllUsers(auth.user.userId)
    users.value = allUsers
      .map((user) => ({
        ...user,
        userType: normalizeUserType(user),
        isBlocked: normalizeBlockedStatus(user),
        blockedReason: user.blockedReason || '',
      }))
      .filter((user) => ['CLIENT', 'OWNER'].includes(user.userType))
    await loadClientReviewStats()
  } finally {
    isClientsLoading.value = false
  }
}

function normalizeUserType(user: any) {
  return String(user.userType || 'CLIENT').toUpperCase()
}

function normalizeBlockedStatus(user: any) {
  return Boolean(user.isBlocked ?? user.blocked)
}

async function selectClient(client: any) {
  selectedClient.value = {
    ...client,
    isBlocked: normalizeBlockedStatus(client),
    blockedReason: client.blockedReason || '',
  }
  errorMessage.value = ''
  try {
    const [received, left] = await Promise.all([
      getReviewsByOwner(client.userId),
      getReviewsLeftByUser(client.userId),
    ])
    reviewsForSelected.value = received
    reviewsBySelected.value = left
  } catch (error) {
    reviewsForSelected.value = []
    reviewsBySelected.value = []
    errorMessage.value = error instanceof Error ? error.message : 'Failed to load review history'
  }
}

async function loadClientReviewStats() {
  const clients = users.value.filter((user) => ['CLIENT', 'OWNER'].includes(normalizeUserType(user)))
  const entries = await Promise.all(clients.map(async (client) => {
    try {
      const reviews = await getReviewsByOwner(client.userId)
      const average = reviews.length > 0
        ? reviews.reduce((sum, review) => sum + Number(review.rating || 0), 0) / reviews.length
        : 0
      return [client.userId, { count: reviews.length, average }] as const
    } catch {
      return [client.userId, { count: 0, average: 0 }] as const
    }
  }))

  clientReviewStats.value = Object.fromEntries(entries)
}

function getClientReviewCount(userId: number) {
  return clientReviewStats.value[userId]?.count || 0
}

function getClientAverageRating(userId: number) {
  return clientReviewStats.value[userId]?.average || 0
}

async function blockSelectedClient() {
  if (!auth.user?.userId || !selectedClient.value) return
  const reason = window.prompt('Reason for blocking this client?', 'Review policy violation')
  if (reason === null) return
  await blockUser(auth.user.userId, selectedClient.value.userId, true, reason)
  selectedClient.value = { ...selectedClient.value, isBlocked: true, blocked: true, blockedReason: reason }
  users.value = users.value.map((user) => user.userId === selectedClient.value.userId ? selectedClient.value : user)
}

async function unblockSelectedClient() {
  if (!auth.user?.userId || !selectedClient.value) return
  await blockUser(auth.user.userId, selectedClient.value.userId, false)
  selectedClient.value = { ...selectedClient.value, isBlocked: false, blocked: false, blockedReason: '' }
  users.value = users.value.map((user) => user.userId === selectedClient.value.userId ? selectedClient.value : user)
}

function formatDate(value: string) {
  return new Date(value).toLocaleDateString('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
  })
}

onMounted(async () => {
  if (!auth.isAuthenticated) {
    router.push('/login')
    return
  }
  if (auth.user?.userType !== 'ADMIN') {
    router.push('/')
    return
  }

  try {
    await loadUsers()
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : 'Failed to load clients'
  }
})
</script>

<style scoped>
.admin-clients {
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

.moderation-body {
  padding-top: 28px;
}

.panel,
.review-panel {
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
}

.panel {
  padding: 24px;
}

.panel-header,
.item-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.panel-header {
  align-items: center;
  margin-bottom: 18px;
}

.client-layout {
  display: grid;
  grid-template-columns: 300px minmax(0, 1fr);
  gap: 18px;
}

.client-list {
  display: grid;
  gap: 8px;
  max-height: 620px;
  overflow: auto;
}

.client-row {
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  color: #1a202c;
  padding: 12px;
  text-align: left;
}

.client-row.active {
  border-color: #f97316;
  box-shadow: 0 0 0 3px rgba(249, 115, 22, 0.12);
}

.client-row span,
.client-row small {
  display: block;
}

.client-row span {
  font-weight: 800;
}

.client-row small,
.review-panel p,
.empty-state {
  color: #718096;
}

.client-row .blocked {
  color: #b91c1c;
  font-weight: 700;
}

.review-stats {
  color: #f97316;
  font-weight: 700;
  margin-top: 4px;
}

.review-panel {
  padding: 18px;
}

.review-columns {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
  margin-top: 18px;
}

.review-card {
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  margin-bottom: 10px;
  padding: 12px;
}

.rating {
  color: #f97316;
  letter-spacing: 0;
}

.empty-state {
  padding: 20px;
  text-align: center;
}

.empty-state.compact {
  border: 1px dashed #cbd5e0;
  border-radius: 8px;
  padding: 12px;
}

.search-input {
  max-width: 240px;
}

@media (max-width: 900px) {
  .client-layout,
  .review-columns {
    grid-template-columns: 1fr;
  }
}
</style>
