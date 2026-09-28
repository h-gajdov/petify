<template>
  <div class="appointments-calendar">
    <div class="calendar-header">
      <button class="btn btn-outline-secondary btn-sm" type="button" @click="goPrevMonth">
        Prev
      </button>

      <div
        v-if="!isEditingMonth"
        class="calendar-title"
        title="Double-click to change month and year"
        @dblclick="startEditingMonth"
      >
        {{ monthLabel }}
      </div>

      <div v-else class="month-year-editor">
        <select v-model.number="editMonth" class="form-control form-control-sm month-select">
          <option v-for="m in months" :key="m.value" :value="m.value">
            {{ m.label }}
          </option>
        </select>

        <input
          ref="yearInputRef"
          v-model.number="editYear"
          type="number"
          class="form-control form-control-sm year-input"
          min="1900"
          max="2100"
          @keyup.enter="applyMonthYearInput"
          @keyup.esc="cancelEditingMonth"
        />

        <button class="btn btn-sm btn-primary" type="button" @click="applyMonthYearInput">
          Go
        </button>

        <button class="btn btn-sm btn-outline-secondary" type="button" @click="cancelEditingMonth">
          Cancel
        </button>
      </div>

      <button class="btn btn-outline-secondary btn-sm" type="button" @click="goNextMonth">
        Next
      </button>
    </div>

    <div class="calendar-grid">
      <div v-for="dayName in dayNames" :key="dayName" class="calendar-cell calendar-day-name">
        {{ dayName }}
      </div>

      <button
        v-for="day in calendarDays"
        :key="day.key"
        class="calendar-cell calendar-day"
        :class="{
          'is-empty': !day.date,
          'is-selected': day.dateKey === selectedDate,
          'has-appointments': day.hasAppointments,
        }"
        type="button"
        :disabled="!day.date"
        @click="selectDay(day.dateKey)"
      >
        <span v-if="day.date" class="day-number">{{ day.dayNumber }}</span>
        <span v-if="day.hasAppointments" class="day-dot"></span>
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, ref } from 'vue'

type AppointmentDay = {
  appointmentId: number
  dateTime: string
}

const props = defineProps<{
  appointments: AppointmentDay[]
  selectedDate: string
}>()

const emit = defineEmits<{
  (e: 'select', dateKey: string): void
}>()

const dayNames = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat']
const monthCursor = ref(new Date())

const isEditingMonth = ref(false)
const editMonth = ref(new Date().getMonth())
const editYear = ref(new Date().getFullYear())
const yearInputRef = ref<HTMLInputElement | null>(null)

const months = [
  { value: 0, label: 'January' },
  { value: 1, label: 'February' },
  { value: 2, label: 'March' },
  { value: 3, label: 'April' },
  { value: 4, label: 'May' },
  { value: 5, label: 'June' },
  { value: 6, label: 'July' },
  { value: 7, label: 'August' },
  { value: 8, label: 'September' },
  { value: 9, label: 'October' },
  { value: 10, label: 'November' },
  { value: 11, label: 'December' },
]

const appointmentDateKeys = computed(() => {
  const keys = new Set<string>()

  props.appointments.forEach((appt) => {
    keys.add(toDateKey(new Date(appt.dateTime)))
  })

  return keys
})

const monthLabel = computed(() => {
  return monthCursor.value.toLocaleDateString('en-US', {
    month: 'long',
    year: 'numeric',
  })
})

const calendarDays = computed(() => {
  const year = monthCursor.value.getFullYear()
  const month = monthCursor.value.getMonth()

  const firstDay = new Date(year, month, 1)
  const lastDay = new Date(year, month + 1, 0)

  const startOffset = firstDay.getDay()
  const totalDays = lastDay.getDate()

  const cells: Array<{
    key: string
    date?: Date
    dateKey?: string
    dayNumber?: number
    hasAppointments: boolean
  }> = []

  for (let i = 0; i < startOffset; i++) {
    cells.push({
      key: `empty-${i}`,
      hasAppointments: false,
    })
  }

  for (let day = 1; day <= totalDays; day++) {
    const date = new Date(year, month, day)
    const dateKey = toDateKey(date)

    cells.push({
      key: dateKey,
      date,
      dateKey,
      dayNumber: day,
      hasAppointments: appointmentDateKeys.value.has(dateKey),
    })
  }

  const remainder = cells.length % 7
  if (remainder !== 0) {
    for (let i = 0; i < 7 - remainder; i++) {
      cells.push({
        key: `tail-${i}`,
        hasAppointments: false,
      })
    }
  }

  return cells
})

function startEditingMonth() {
  editMonth.value = monthCursor.value.getMonth()
  editYear.value = monthCursor.value.getFullYear()
  isEditingMonth.value = true

  nextTick(() => {
    yearInputRef.value?.focus()
  })
}

function applyMonthYearInput() {
  if (!editYear.value || editYear.value < 1900 || editYear.value > 2100) {
    return
  }

  monthCursor.value = new Date(editYear.value, editMonth.value, 1)
  isEditingMonth.value = false
}

function cancelEditingMonth() {
  isEditingMonth.value = false
}

function toDateKey(date: Date): string {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')

  return `${year}-${month}-${day}`
}

function selectDay(dateKey?: string) {
  if (!dateKey) return
  emit('select', dateKey)
}

function goPrevMonth() {
  const date = new Date(monthCursor.value)
  date.setMonth(date.getMonth() - 1)
  monthCursor.value = date
}

function goNextMonth() {
  const date = new Date(monthCursor.value)
  date.setMonth(date.getMonth() + 1)
  monthCursor.value = date
}
</script>

<style scoped>
.appointments-calendar {
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 16px;
  background: #fff;
}

.calendar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.calendar-title {
  font-weight: 700;
  color: #1a202c;
  cursor: pointer;
  min-width: 180px;
  text-align: center;
}

.month-year-editor {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
  justify-content: center;
}

.month-select {
  max-width: 150px;
}

.year-input {
  max-width: 100px;
}

.calendar-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 6px;
}

.calendar-cell {
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  min-height: 44px;
  background: #f8fafc;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

.calendar-day-name {
  background: transparent;
  border: none;
  font-weight: 600;
  color: #4a5568;
  min-height: 24px;
}

.calendar-day {
  cursor: pointer;
  transition: all 0.2s ease;
}

.calendar-day:disabled {
  cursor: default;
  background: transparent;
  border: none;
}

.calendar-day.has-appointments {
  border-color: #f97316;
}

.calendar-day.is-selected {
  background: #f97316;
  color: #fff;
  border-color: #f97316;
}

.day-number {
  font-weight: 600;
}

.day-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #f97316;
  position: absolute;
  bottom: 6px;
}

.calendar-day.is-selected .day-dot {
  background: #fff;
}
</style>
