<template>
  <main class="clinic-dashboard">
    <section class="dashboard-header">
      <div class="container">
        <div>
          <p class="eyebrow">Clinic workspace</p>
          <h1 class="page-title">Appointments & Availability</h1>
          <p v-if="clinic" class="clinic-subtitle">{{ clinic.name }} - {{ clinic.city }}, {{ clinic.address }}</p>
        </div>
        <div class="toolbar">
          <button class="btn btn-outline-secondary btn-sm" type="button" @click="goToPreviousDay">
            Previous day
          </button>
          <input v-model="selectedDate" type="date" class="form-control date-input" />
          <button class="btn btn-outline-secondary btn-sm" type="button" @click="goToToday">
            Today
          </button>
          <button class="btn btn-outline-secondary btn-sm" type="button" @click="goToNextDay">
            Next day
          </button>
        </div>
      </div>
    </section>

    <section class="container dashboard-body">
      <div v-if="accessError" class="alert alert-danger">{{ accessError }}</div>
      <div v-if="scheduleError" class="alert alert-danger">{{ scheduleError }}</div>
      <div v-if="notificationsError" class="alert alert-danger">{{ notificationsError }}</div>

      <div v-if="!canUseDashboard" class="empty-state">
        <h2>Clinic login required</h2>
        <p>This dashboard is only available for logged-in clinic accounts.</p>
      </div>

      <template v-else>
        <div class="summary-strip">
          <div class="summary-item">
            <span class="summary-value">{{ appointments.length }}</span>
            <span class="summary-label">Appointments</span>
          </div>
          <div class="summary-item">
            <span class="summary-value">{{ availableSlots.length }}</span>
            <span class="summary-label">Available</span>
          </div>
          <div class="summary-item">
            <span class="summary-value">{{ unavailableSlots.length }}</span>
            <span class="summary-label">Not working</span>
          </div>
        </div>

        <div v-if="isLoading" class="alert alert-info">Loading clinic schedule...</div>

        <div v-else class="schedule-layout">
          <section class="schedule-section">
            <div class="section-heading">
              <h2>Slots for {{ selectedDate }}</h2>
              <button class="btn btn-outline-secondary btn-sm" type="button" @click="loadSchedule">Refresh</button>
            </div>

            <div class="slot-grid">
              <div
                v-for="slot in daySlots"
                :key="slot.dateTime"
                class="slot-card"
                :class="slot.kind"
              >
                <div class="slot-time">{{ slot.label }}</div>
                <div class="slot-main">
                  <span class="slot-status">{{ slot.statusText }}</span>
                  <span v-if="slot.detail" class="slot-detail">{{ slot.detail }}</span>
                </div>
                <button
                  v-if="slot.kind === 'available'"
                  type="button"
                  class="btn btn-sm btn-outline-danger"
                  @click="blockSlot(slot.dateTime)"
                >
                  Mark not working
                </button>
                <button
                  v-else-if="slot.kind === 'unavailable' && slot.unavailableSlotId"
                  type="button"
                  class="btn btn-sm btn-outline-secondary"
                  @click="unblockSlot(slot.unavailableSlotId)"
                >
                  Make available
                </button>
              </div>
            </div>
          </section>

          <aside class="appointments-panel">
            <section class="notifications-panel">
              <div class="section-heading compact">
                <h2>Notifications</h2>
                <button class="btn btn-outline-secondary btn-sm" type="button" @click="loadNotifications">Refresh</button>
              </div>
              <div v-if="notifications.length === 0" class="panel-empty">No notifications yet.</div>
              <div v-else class="notification-list">
                <article v-for="notification in notifications.slice(0, 5)" :key="notification.notificationId" class="notification-row">
                  <div class="notification-message">{{ notification.message }}</div>
                  <div class="notification-date">{{ formatDateTime(notification.createdAt) }}</div>
                </article>
              </div>
            </section>

            <h2>Appointments</h2>
            <div v-if="appointments.length === 0" class="panel-empty">No appointments on this date.</div>
            <div v-else class="appointment-list">
              <article v-for="appointment in appointments" :key="appointment.appointmentId" class="appointment-row">
                <div class="appointment-time">{{ appointment.label }}</div>
                <div>
                  <div class="appointment-title">{{ appointment.petName || 'Pet' }}</div>
                  <div class="appointment-meta">
                    {{ appointment.petSpecies || 'Species unknown' }} with {{ appointment.ownerName || 'owner' }}
                  </div>
                  <div v-if="appointment.notes" class="appointment-notes">{{ appointment.notes }}</div>
                  <button
                    v-if="canMarkNoShow(appointment)"
                    type="button"
                    class="btn btn-sm btn-outline-danger appointment-action"
                    :disabled="updatingAppointmentId === appointment.appointmentId"
                    @click="markNoShow(appointment)"
                  >
                    {{ updatingAppointmentId === appointment.appointmentId ? 'Updating...' : 'Mark no-show' }}
                  </button>
                </div>
                <span class="badge" :class="getStatusClass(appointment.status)">{{ appointment.status }}</span>
              </article>
            </div>
          </aside>
        </div>
      </template>
    </section>
  </main>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import {
  createMyClinicUnavailableSlot,
  deleteMyClinicUnavailableSlot,
  getMyClinic,
  getMyClinicAppointments,
  getMyClinicUnavailableSlots,
  getMyNotifications,
  markMyClinicAppointmentNoShow,
  type AppNotification,
  type AppointmentSlot,
  type ClinicAppointment,
  type ClinicUnavailableSlot,
  type VetClinic,
} from '../api/profile'
import { useAuthStore } from '../stores/auth'

type ScheduleSlot = {
  dateTime: string
  label: string
  kind: 'available' | 'booked' | 'unavailable' | 'past'
  statusText: string
  detail?: string
  unavailableSlotId?: number
}

const router = useRouter()
const auth = useAuthStore()

const clinic = ref<VetClinic | null>(null)
const selectedDate = ref(toDateKey(new Date()))
const availableSlots = ref<AppointmentSlot[]>([])
const unavailableSlots = ref<ClinicUnavailableSlot[]>([])
const appointments = ref<ClinicAppointment[]>([])
const notifications = ref<AppNotification[]>([])
const isLoading = ref(false)
const updatingAppointmentId = ref<number | null>(null)
const accessError = ref('')
const scheduleError = ref('')
const notificationsError = ref('')
const NON_BLOCKING_STATUSES = new Set(['CANCELLED', 'CANCELED', 'NO_SHOW'])
let latestScheduleRequest = 0

const canUseDashboard = computed(() => auth.isAuthenticated && auth.user?.userType === 'CLINIC')

const appointmentsByDateTime = computed(() => {
  const map = new Map<string, ClinicAppointment>()
  appointments.value
    .filter((appointment) => !NON_BLOCKING_STATUSES.has(String(appointment.status || '').toUpperCase()))
    .forEach((appointment) => map.set(normalizeDateTime(appointment.dateTime), appointment))
  return map
})

const availableDateTimes = computed(() => {
  return new Set(availableSlots.value.map((slot) => normalizeDateTime(slot.dateTime)))
})

const unavailableByDateTime = computed(() => {
  const map = new Map<string, ClinicUnavailableSlot>()
  unavailableSlots.value.forEach((slot) => map.set(normalizeDateTime(slot.dateTime), slot))
  return map
})

const daySlots = computed<ScheduleSlot[]>(() => {
  const slots: ScheduleSlot[] = []
  const now = new Date()

  for (let hour = 9; hour < 17; hour += 1) {
    for (const minute of [0, 30]) {
      const dateTime = `${selectedDate.value}T${String(hour).padStart(2, '0')}:${String(minute).padStart(2, '0')}`
      const label = `${String(hour).padStart(2, '0')}:${String(minute).padStart(2, '0')}`
      const key = normalizeDateTime(dateTime)
      const appointment = appointmentsByDateTime.value.get(key)
      const unavailable = unavailableByDateTime.value.get(key)

      if (appointment) {
        slots.push({
          dateTime,
          label,
          kind: 'booked',
          statusText: 'Booked',
          detail: appointment.petName || undefined,
        })
      } else if (unavailable) {
        slots.push({
          dateTime,
          label,
          kind: 'unavailable',
          statusText: 'Not working',
          detail: unavailable.reason || undefined,
          unavailableSlotId: unavailable.slotId,
        })
      } else if (availableDateTimes.value.has(key)) {
        slots.push({
          dateTime,
          label,
          kind: 'available',
          statusText: 'Available',
        })
      } else if (new Date(dateTime).getTime() < now.getTime()) {
        slots.push({
          dateTime,
          label,
          kind: 'past',
          statusText: 'Past',
        })
      } else {
        slots.push({
          dateTime,
          label,
          kind: 'unavailable',
          statusText: 'Unavailable',
        })
      }
    }
  }

  return slots
})

function buildAvailableSlots(
  date: string,
  clinicAppointments: ClinicAppointment[],
  clinicUnavailableSlots: ClinicUnavailableSlot[]
): AppointmentSlot[] {
  const now = new Date()
  const booked = new Set(
    clinicAppointments
      .filter((appointment) => !NON_BLOCKING_STATUSES.has(String(appointment.status || '').toUpperCase()))
      .map((appointment) => normalizeDateTime(appointment.dateTime))
  )
  const unavailable = new Set(
    clinicUnavailableSlots.map((slot) => normalizeDateTime(slot.dateTime))
  )
  const slots: AppointmentSlot[] = []

  for (let hour = 9; hour < 17; hour += 1) {
    for (const minute of [0, 30]) {
      const dateTime = `${date}T${String(hour).padStart(2, '0')}:${String(minute).padStart(2, '0')}`
      const key = normalizeDateTime(dateTime)
      if (new Date(dateTime).getTime() < now.getTime()) continue
      if (booked.has(key) || unavailable.has(key)) continue
      slots.push({
        dateTime,
        label: `${String(hour).padStart(2, '0')}:${String(minute).padStart(2, '0')}`,
      })
    }
  }

  return slots
}

async function loadSchedule() {
  if (!auth.user?.userId || !canUseDashboard.value || !selectedDate.value) return

  const requestId = ++latestScheduleRequest
  try {
    isLoading.value = true
    scheduleError.value = ''
    const [unavailable, clinicAppointments] = await Promise.all([
      getMyClinicUnavailableSlots(auth.user.userId, selectedDate.value),
      getMyClinicAppointments(auth.user.userId, selectedDate.value),
    ])

    if (requestId !== latestScheduleRequest) return
    unavailableSlots.value = unavailable
    appointments.value = clinicAppointments
    availableSlots.value = buildAvailableSlots(selectedDate.value, clinicAppointments, unavailable)
  } catch (error) {
    if (requestId !== latestScheduleRequest) return
    availableSlots.value = []
    unavailableSlots.value = []
    appointments.value = []
    scheduleError.value = error instanceof Error ? error.message : 'Failed to load clinic schedule'
  } finally {
    if (requestId === latestScheduleRequest) {
      isLoading.value = false
    }
  }
}

async function loadNotifications() {
  if (!auth.user?.userId || !canUseDashboard.value) return

  try {
    notificationsError.value = ''
    notifications.value = await getMyNotifications(auth.user.userId)
  } catch (error) {
    notifications.value = []
    notificationsError.value = error instanceof Error ? error.message : 'Failed to load notifications'
  }
}

async function blockSlot(dateTime: string) {
  if (!auth.user?.userId) return
  const reason = window.prompt('Reason for blocking this slot?', 'Not working')
  if (reason === null) return

  try {
    scheduleError.value = ''
    await createMyClinicUnavailableSlot(auth.user.userId, {
      dateTime,
      reason: reason.trim() || 'Not working',
    })
    await loadSchedule()
  } catch (error) {
    scheduleError.value = error instanceof Error ? error.message : 'Failed to block slot'
  }
}

async function unblockSlot(slotId: number) {
  if (!auth.user?.userId) return

  try {
    scheduleError.value = ''
    await deleteMyClinicUnavailableSlot(auth.user.userId, slotId)
    await loadSchedule()
  } catch (error) {
    scheduleError.value = error instanceof Error ? error.message : 'Failed to unblock slot'
  }
}

function canMarkNoShow(appointment: ClinicAppointment): boolean {
  return ['CONFIRMED', 'DONE'].includes(appointment.status) && new Date(appointment.dateTime).getTime() <= Date.now()
}

async function markNoShow(appointment: ClinicAppointment) {
  if (!auth.user?.userId) return
  if (!window.confirm('Mark this appointment as no-show?')) return

  try {
    updatingAppointmentId.value = appointment.appointmentId
    scheduleError.value = ''
    const updated = await markMyClinicAppointmentNoShow(auth.user.userId, appointment.appointmentId)
    appointments.value = appointments.value.map((item) =>
      item.appointmentId === updated.appointmentId ? updated : item
    )
  } catch (error) {
    scheduleError.value = error instanceof Error ? error.message : 'Failed to mark appointment as no-show'
  } finally {
    updatingAppointmentId.value = null
  }
}

function getStatusClass(status: string): string {
  if (status === 'CONFIRMED' || status === 'DONE') return 'bg-success'
  if (status === 'CANCELLED' || status === 'CANCELED' || status === 'NO_SHOW') return 'bg-secondary'
  return 'bg-warning'
}

function normalizeDateTime(value: string): string {
  return value.length >= 16 ? value.slice(0, 16) : value
}

function toDateKey(date: Date): string {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

function shiftSelectedDate(days: number) {
  const date = new Date(`${selectedDate.value}T00:00:00`)
  date.setDate(date.getDate() + days)
  selectedDate.value = toDateKey(date)
}

function goToPreviousDay() {
  shiftSelectedDate(-1)
}

function goToNextDay() {
  shiftSelectedDate(1)
}

function goToToday() {
  selectedDate.value = toDateKey(new Date())
}

function formatDateTime(value: string): string {
  return new Date(value).toLocaleString('en-US', {
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
}

onMounted(async () => {
  if (!auth.isAuthenticated) {
    router.push('/login')
    return
  }

  if (auth.user?.userType !== 'CLINIC') {
    accessError.value = 'This dashboard is only available for clinic accounts.'
    return
  }

  try {
    clinic.value = await getMyClinic(auth.user.userId)
    accessError.value = ''
  } catch (error) {
    accessError.value = error instanceof Error ? error.message : 'Unable to load your clinic profile'
    return
  }

  await Promise.all([loadSchedule(), loadNotifications()])
})

watch(selectedDate, () => {
  loadSchedule()
})
</script>

<style scoped>
.clinic-dashboard {
  min-height: 100vh;
  background: #f7fafc;
  padding-bottom: 56px;
}

.dashboard-header {
  background: white;
  border-bottom: 1px solid #e2e8f0;
  padding: 32px 0;
}

.dashboard-header .container {
  display: flex;
  align-items: end;
  justify-content: space-between;
  gap: 24px;
}

.eyebrow {
  color: #f97316;
  font-weight: 700;
  margin: 0 0 8px;
  text-transform: uppercase;
  font-size: 0.78rem;
}

.page-title {
  color: #1a202c;
  font-size: 2rem;
  margin: 0;
}

.clinic-subtitle {
  color: #718096;
  margin: 8px 0 0;
  font-weight: 600;
}

.toolbar {
  display: flex;
  gap: 12px;
  align-items: center;
}

.date-input {
  width: 180px;
}

.dashboard-body {
  padding-top: 32px;
}

.empty-state,
.summary-strip,
.schedule-section,
.appointments-panel {
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
}

.empty-state {
  padding: 48px;
  text-align: center;
}

.empty-state h2 {
  font-size: 1.35rem;
  margin: 0 0 8px;
}

.empty-state p {
  color: #718096;
  margin: 0;
}

.summary-strip {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  margin-bottom: 20px;
}

.summary-item {
  padding: 20px 24px;
  border-right: 1px solid #e2e8f0;
}

.summary-item:last-child {
  border-right: none;
}

.summary-value {
  display: block;
  font-size: 1.8rem;
  color: #1a202c;
  font-weight: 800;
}

.summary-label {
  color: #718096;
  font-weight: 600;
}

.schedule-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 380px;
  gap: 20px;
  align-items: start;
}

.schedule-section,
.appointments-panel {
  padding: 24px;
}

.section-heading {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 18px;
  gap: 16px;
}

.section-heading.compact {
  margin-bottom: 12px;
}

.notifications-panel {
  border-bottom: 1px solid #e2e8f0;
  margin-bottom: 20px;
  padding-bottom: 20px;
}

.notification-list {
  display: grid;
  gap: 10px;
}

.notification-row {
  background: #fff7ed;
  border: 1px solid #fed7aa;
  border-radius: 8px;
  padding: 10px 12px;
}

.notification-message {
  color: #2d3748;
  font-weight: 600;
  line-height: 1.35;
}

.notification-date {
  color: #718096;
  font-size: 0.82rem;
  margin-top: 4px;
}

.section-heading h2,
.appointments-panel h2 {
  font-size: 1.2rem;
  margin: 0;
  color: #1a202c;
}

.slot-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(190px, 1fr));
  gap: 12px;
}

.slot-card {
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 14px;
  display: grid;
  gap: 10px;
  min-height: 136px;
  align-content: start;
}

.slot-card.available {
  border-color: #9ae6b4;
  background: #f0fff4;
}

.slot-card.booked {
  border-color: #90cdf4;
  background: #ebf8ff;
}

.slot-card.unavailable {
  border-color: #fed7d7;
  background: #fff5f5;
}

.slot-card.past {
  color: #718096;
  background: #edf2f7;
}

.slot-time {
  font-size: 1.25rem;
  font-weight: 800;
  color: #1a202c;
}

.slot-main {
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.slot-status {
  font-weight: 700;
}

.slot-detail {
  color: #4a5568;
  font-size: 0.9rem;
}

.appointment-list {
  display: grid;
  gap: 12px;
  margin-top: 16px;
}

.appointment-row {
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 14px;
  display: grid;
  grid-template-columns: 58px minmax(0, 1fr) auto;
  gap: 12px;
  align-items: start;
}

.appointment-time {
  font-weight: 800;
  color: #f97316;
}

.appointment-title {
  font-weight: 800;
  color: #1a202c;
}

.appointment-meta,
.appointment-notes,
.panel-empty {
  color: #718096;
  font-size: 0.9rem;
}

.appointment-notes {
  margin-top: 6px;
  background: #f7fafc;
  padding: 8px;
  border-radius: 6px;
}

.appointment-action {
  margin-top: 10px;
}

.badge {
  border-radius: 6px;
  padding: 4px 8px;
  font-size: 0.75rem;
}

.bg-success {
  background: #c6f6d5;
  color: #22543d;
}

.bg-secondary {
  background: #e2e8f0;
  color: #2d3748;
}

.bg-warning {
  background: #fefcbf;
  color: #744210;
}

@media (max-width: 980px) {
  .dashboard-header .container,
  .toolbar {
    align-items: stretch;
    flex-direction: column;
  }

  .date-input {
    min-width: 0;
    width: 100%;
  }

  .schedule-layout {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .summary-strip {
    grid-template-columns: 1fr;
  }

  .summary-item {
    border-right: none;
    border-bottom: 1px solid #e2e8f0;
  }

  .summary-item:last-child {
    border-bottom: none;
  }
}
</style>
