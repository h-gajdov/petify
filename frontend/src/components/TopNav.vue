<template>
  <nav class="navbar navbar-expand-lg navbar-light bg-white border-bottom sticky-top nav-shadow">
    <div class="container py-2">
      <!-- Petify logo links to Listings and stays orange -->
      <RouterLink class="navbar-brand fw-bold brand" to="/">
        Petify
      </RouterLink>

      <button
        class="navbar-toggler"
        type="button"
        aria-controls="petifyNavbar"
        :aria-expanded="isOpen ? 'true' : 'false'"
        aria-label="Toggle navigation"
        @click="isOpen = !isOpen"
      >
        <span class="navbar-toggler-icon"></span>
      </button>

      <div
        id="petifyNavbar"
        class="navbar-collapse"
        :class="isOpen ? 'collapse show' : 'collapse'"
      >
        <div class="d-flex align-items-center gap-2 ms-auto">
          <div class="d-flex gap-2">
            <template v-if="auth.isAuthenticated">
              <RouterLink v-if="auth.user?.userType === 'CLINIC'" class="btn btn-outline-secondary" to="/clinics">
                Clinic Dashboard
              </RouterLink>
              <template v-if="auth.user?.userType === 'ADMIN'">
                <RouterLink class="btn btn-admin" to="/admin/clinics">
                  <i class="bi bi-hospital"></i> Clinics
                </RouterLink>
                <RouterLink class="btn btn-admin" to="/admin/clients">
                  <i class="bi bi-people"></i> Clients
                </RouterLink>
                <RouterLink class="btn btn-admin" to="/admin/listings">
                  <i class="bi bi-card-list"></i> Listings
                </RouterLink>
              </template>
              <RouterLink class="btn btn-outline-secondary" to="/profile">
                {{ auth.user?.firstName }}'s Profile
              </RouterLink>
              <button
                class="btn btn-outline-secondary"
                type="button"
                @click="logout"
              >
                Log out
              </button>
            </template>
            <template v-else>
              <RouterLink class="btn btn-login" to="/login">
                Log in
              </RouterLink>
              <RouterLink class="btn btn-signup" to="/signup">
                Sign up
              </RouterLink>
            </template>
          </div>
        </div>
      </div>
    </div>
  </nav>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth.ts'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const isOpen = ref(false)

watch(
  () => route.fullPath,
  () => {
    isOpen.value = false
  },
)

function logout() {
  auth.logout()
  router.push('/')
}
</script>

<style scoped>
.nav-shadow {
  box-shadow: 0 6px 20px rgba(17, 24, 39, 0.06);
}

/* Petify logo – orange and stable */
.brand,
.brand:hover,
.brand:focus,
.brand:active,
.brand:visited {
  color: #f97316;
  text-decoration: none;
  letter-spacing: 0.3px;
}

/* Login button */
.btn-login {
  border-radius: 12px;
  padding: 0.45rem 1.1rem;
  border: 1px solid #e5e7eb;
  background-color: #ffffff;
  color: #374151;
  font-weight: 500;
  transition: all 0.15s ease;
}

.btn-login:hover {
  background-color: #f9fafb;
  border-color: #d1d5db;
  color: #111827;
}

/* Sign up button */
.btn-signup {
  border-radius: 12px;
  padding: 0.45rem 1.2rem;
  background: linear-gradient(135deg, #fb923c, #f97316);
  border: none;
  color: #ffffff;
  font-weight: 600;
  box-shadow: 0 6px 16px rgba(249, 115, 22, 0.35);
  transition: all 0.15s ease;
}

.btn-signup:hover {
  transform: translateY(-1px);
  box-shadow: 0 10px 22px rgba(249, 115, 22, 0.45);
}

/* Admin button */
.btn-admin {
  border-radius: 12px;
  padding: 0.45rem 1rem;
  background: linear-gradient(135deg, #dc2626 0%, #b91c1c 100%);
  border: none;
  color: #ffffff;
  font-weight: 600;
  box-shadow: 0 6px 16px rgba(220, 38, 38, 0.35);
  transition: all 0.15s ease;
  display: flex;
  align-items: center;
  gap: 6px;
}

.btn-admin:hover {
  transform: translateY(-1px);
  box-shadow: 0 10px 22px rgba(220, 38, 38, 0.45);
  color: #ffffff;
  text-decoration: none;
}

.btn-admin i {
  font-size: 0.9rem;
}
</style>
