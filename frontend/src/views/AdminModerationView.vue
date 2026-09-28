<template>
  <main class="admin-moderation">
    <section class="header-section">
      <div class="container">
        <h1 class="page-title">Clinics</h1>
        <p class="page-subtitle">Review clinic applications and inspect clinic reviews.</p>
      </div>
    </section>

    <section class="container moderation-body">
      <div v-if="errorMessage" class="alert alert-danger">{{ errorMessage }}</div>

      <div class="moderation-grid">
        <section class="panel">
          <div class="panel-header">
            <h2>Clinic Applications</h2>
            <button class="btn btn-sm btn-outline-secondary" type="button" @click="loadClinicApplications">Refresh</button>
          </div>

          <div v-if="clinicApplications.length === 0" class="empty-state">No clinic applications found.</div>
          <article v-for="application in clinicApplications" :key="application.applicationId" class="application-card">
            <div class="item-header">
              <div>
                <h3>{{ application.name }}</h3>
                <p>{{ application.city }} - {{ application.address }}</p>
                <p>{{ application.email || 'No email' }} {{ application.phone ? `| ${application.phone}` : '' }}</p>
              </div>
              <span class="badge" :class="statusClass(application.status)">{{ application.status }}</span>
            </div>
            <p class="meta">Submitted {{ formatDate(application.submittedAt) }}</p>
            <p v-if="application.denialReason" class="denial">Denied: {{ application.denialReason }}</p>
            <div class="actions">
              <button
                class="btn btn-sm btn-success"
                type="button"
                :disabled="application.status === 'APPROVED'"
                @click="approveApplication(application)"
              >
                Approve
              </button>
              <button
                class="btn btn-sm btn-outline-danger"
                type="button"
                :disabled="application.status === 'DENIED'"
                @click="denyApplication(application)"
              >
                Deny
              </button>
            </div>
          </article>
        </section>

        <section class="panel">
          <div class="panel-header">
            <h2>Clinic Reviews</h2>
            <button class="btn btn-sm btn-outline-secondary" type="button" @click="loadClinics">Refresh</button>
          </div>

          <div class="client-layout">
            <div class="client-list">
              <button
                v-for="clinic in clinics"
                :key="clinic.clinicId"
                type="button"
                class="client-row"
                :class="{ active: selectedClinic?.clinicId === clinic.clinicId }"
                @click="selectClinic(clinic)"
              >
                <span>{{ clinic.name }}</span>
                <small>{{ clinic.city }} | {{ clinic.address }}</small>
                <small class="clinic-review-stats">
                  {{ getClinicReviewCount(clinic.clinicId) }} reviews | {{ getClinicAverageRating(clinic.clinicId).toFixed(1) }} stars
                </small>
              </button>
            </div>

            <div class="review-panel">
              <div v-if="!selectedClinic" class="empty-state">Select a clinic to inspect its reviews.</div>
              <template v-else>
                <div class="item-header">
                  <div>
                    <h3>{{ selectedClinic.name }}</h3>
                    <p>{{ selectedClinic.city }} | {{ selectedClinic.address }}</p>
                  </div>
                </div>

                <div v-if="clinicReviews.length === 0" class="empty-state compact">No reviews for this clinic.</div>
                <article v-for="review in clinicReviews" :key="review.reviewId" class="review-card">
                  <div class="item-header">
                    <div>
                      <strong>{{ review.reviewerName }}</strong>
                      <small>@{{ review.reviewerUsername }}</small>
                    </div>
                    <div class="rating">{{ '★'.repeat(Number(review.rating || 0)) }}</div>
                  </div>
                  <p>{{ review.comment || 'No comment' }}</p>
                  <small>{{ formatDate(review.createdAt) }}</small>
                </article>
              </template>
            </div>
          </div>
        </section>

      </div>
    </section>
  </main>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  approveClinicApplication,
  denyClinicApplication,
  getClinicApplications,
  getClinicsAdmin,
} from '../api/admin'
import { getReviewsByClinic, type Review } from '../api/reviews'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const auth = useAuthStore()

const clinicApplications = ref<any[]>([])
const clinics = ref<any[]>([])
const selectedClinic = ref<any | null>(null)
const clinicReviews = ref<Review[]>([])
const clinicReviewStats = ref<Record<number, { count: number; average: number }>>({})
const errorMessage = ref('')


async function loadClinicApplications() {
  if (!auth.user?.userId) return
  clinicApplications.value = await getClinicApplications(auth.user.userId)
}


async function loadClinics() {
  if (!auth.user?.userId) return
  clinics.value = await getClinicsAdmin(auth.user.userId)
  await loadClinicReviewStats()
}

async function selectClinic(clinic: any) {
  selectedClinic.value = clinic
  errorMessage.value = ''
  try {
    clinicReviews.value = await getReviewsByClinic(clinic.clinicId)
  } catch (error) {
    clinicReviews.value = []
    errorMessage.value = error instanceof Error ? error.message : 'Failed to load clinic reviews'
  }
}

async function loadClinicReviewStats() {
  const entries = await Promise.all(clinics.value.map(async (clinic) => {
    try {
      const reviews = await getReviewsByClinic(clinic.clinicId)
      const average = reviews.length > 0
        ? reviews.reduce((sum, review) => sum + Number(review.rating || 0), 0) / reviews.length
        : 0
      return [clinic.clinicId, { count: reviews.length, average }] as const
    } catch {
      return [clinic.clinicId, { count: 0, average: 0 }] as const
    }
  }))

  clinicReviewStats.value = Object.fromEntries(entries)
}

function getClinicReviewCount(clinicId: number) {
  return clinicReviewStats.value[clinicId]?.count || 0
}

function getClinicAverageRating(clinicId: number) {
  return clinicReviewStats.value[clinicId]?.average || 0
}


async function approveApplication(application: any) {
  if (!auth.user?.userId) return
  try {
    const updated = await approveClinicApplication(auth.user.userId, application.applicationId)
    replaceApplication(updated)
    await loadClinics()
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : 'Failed to approve application'
  }
}

async function denyApplication(application: any) {
  if (!auth.user?.userId) return
  const reason = window.prompt('Reason for denial?', application.denialReason || '')
  if (reason === null) return

  try {
    const updated = await denyClinicApplication(auth.user.userId, application.applicationId, reason)
    replaceApplication(updated)
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : 'Failed to deny application'
  }
}


function replaceApplication(updated: any) {
  clinicApplications.value = clinicApplications.value.map((application) =>
    application.applicationId === updated.applicationId ? updated : application
  )
}

function statusClass(status: string) {
  if (status === 'APPROVED') return 'bg-success'
  if (status === 'DENIED') return 'bg-danger'
  return 'bg-warning'
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
    await Promise.all([loadClinicApplications(), loadClinics()])
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : 'Failed to load admin moderation data'
  }
})
</script>

<style scoped>
.admin-moderation {
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

.moderation-grid {
  display: grid;
  gap: 24px;
}

.panel,
.application-card,
.review-panel {
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
}

.panel {
  padding: 24px;
}

.panel-header,
.item-header,
.actions {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.panel-header {
  align-items: center;
  margin-bottom: 18px;
}

.panel h2,
.application-card h3,
.review-panel h3 {
  color: #1a202c;
  margin: 0;
}

.application-card {
  padding: 16px;
  margin-bottom: 12px;
}

.application-card p,
.review-panel p,
.meta {
  color: #718096;
  margin: 4px 0;
}

.denial {
  color: #b91c1c;
  font-weight: 600;
}

.badge {
  border-radius: 4px;
  color: white;
  font-size: 0.78rem;
  font-weight: 700;
  padding: 4px 8px;
}

.bg-success { background: #16a34a; }
.bg-danger { background: #dc2626; }
.bg-warning { background: #f59e0b; }

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

.client-row small {
  color: #718096;
}

.client-row .blocked {
  color: #b91c1c;
  font-weight: 700;
}

.clinic-review-stats {
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
  color: #718096;
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
