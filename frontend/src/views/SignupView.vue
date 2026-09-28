<template>
  <main class="auth-wrap">
    <!-- animated blobs -->
    <div class="blob blob-1" aria-hidden="true"></div>
    <div class="blob blob-2" aria-hidden="true"></div>

    <div class="auth-card card border-0 shadow-lg">
      <div class="card-body p-4 p-md-5">
        <!-- Header -->
        <div class="d-flex align-items-center gap-3 mb-4">
          <div class="brand-badge" aria-hidden="true">
            <!-- paw -->
            <svg width="22" height="22" viewBox="0 0 24 24" fill="none">
              <path
                d="M7.6 10.2c-1.1 0-2-.98-2-2.2s.9-2.2 2-2.2 2 .98 2 2.2-.9 2.2-2 2.2Zm8.8 0c-1.1 0-2-.98-2-2.2s.9-2.2 2-2.2 2 .98 2 2.2-.9 2.2-2 2.2ZM10 8.2c-1 0-1.8-.9-1.8-2s.8-2 1.8-2 1.8.9 1.8 2-.8 2-1.8 2Zm4 0c-1 0-1.8-.9-1.8-2s.8-2 1.8-2 1.8.9 1.8 2-.8 2-1.8 2ZM12 21c-2.6 0-5.6-1.6-5.6-4.5 0-2.2 1.9-3.7 3.9-3.7.8 0 1.4.2 1.7.5.3-.3.9-.5 1.7-.5 2 0 3.9 1.5 3.9 3.7C17.6 19.4 14.6 21 12 21Z"
                fill="currentColor"
                opacity=".92"
              />
            </svg>
          </div>

          <div class="flex-grow-1">
            <h1 class="h4 mb-1 title">Create your account</h1>
            <p class="text-muted mb-0">Create an account to start using Petify.</p>
          </div>
        </div>

        <!-- Alerts -->
        <div v-if="success" class="alert alert-success auth-alert" role="alert">
          <strong class="me-1">Done.</strong>{{ success }}
        </div>
        <div v-if="error" class="alert alert-danger auth-alert" role="alert">
          <strong class="me-1">Oops.</strong>{{ error }}
        </div>

        <form @submit.prevent="submit" class="mt-3">
          <!-- Username -->
          <div class="mb-3">
            <label class="form-label" for="username">Username</label>
            <div class="input-group auth-input">
              <span class="input-group-text">
                <!-- user -->
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none">
                  <path
                    d="M12 12a4.5 4.5 0 1 0-4.5-4.5A4.5 4.5 0 0 0 12 12Zm0 2.25c-4.42 0-8 2.24-8 5v.75h16v-.75c0-2.76-3.58-5-8-5Z"
                    fill="currentColor"
                    opacity=".85"
                  />
                </svg>
              </span>
              <input
                id="username"
                v-model.trim="username"
                class="form-control"
                type="text"
                autocomplete="username"
                required
                placeholder="e.g. user_123"
              />
            </div>
          </div>

          <!-- Email -->
          <div class="mb-3">
            <label class="form-label" for="email">Email</label>
            <div class="input-group auth-input">
              <span class="input-group-text">
                <!-- mail -->
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none">
                  <path
                    d="M20 6H4a2 2 0 0 0-2 2v10a2 2 0 0 0 2 2h16a2 2 0 0 0 2-2V8a2 2 0 0 0-2-2Zm-.6 3.2-6.9 4.5a1 1 0 0 1-1 0L4.6 9.2A1 1 0 1 1 5.7 7.6L12 11.7l6.3-4.1a1 1 0 1 1 1.1 1.6Z"
                    fill="currentColor"
                    opacity=".85"
                  />
                </svg>
              </span>
              <input
                id="email"
                v-model.trim="email"
                class="form-control"
                type="email"
                autocomplete="email"
                required
                placeholder="you@petify.com"
              />
            </div>
          </div>

          <!-- First/Last name -->
          <div class="row g-2">
            <div class="col-12 col-md-6">
              <div class="mb-3">
                <label class="form-label" for="firstName">First name</label>
                <input
                  id="firstName"
                  v-model.trim="firstName"
                  class="form-control soft-input"
                  type="text"
                  autocomplete="given-name"
                  required
                  placeholder="Name"
                />
              </div>
            </div>
            <div class="col-12 col-md-6">
              <div class="mb-3">
                <label class="form-label" for="lastName">Last name</label>
                <input
                  id="lastName"
                  v-model.trim="lastName"
                  class="form-control soft-input"
                  type="text"
                  autocomplete="family-name"
                  required
                  placeholder="Surname"
                />
              </div>
            </div>
          </div>

          <!-- Password + show/hide -->
          <div class="mb-3">
            <label class="form-label" for="password">Password</label>
            <div class="input-group auth-input">
              <span class="input-group-text">
                <!-- lock -->
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none">
                  <path
                    d="M17 10h-1V8a4 4 0 0 0-8 0v2H7a2 2 0 0 0-2 2v7a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2v-7a2 2 0 0 0-2-2Zm-7-2a2 2 0 0 1 4 0v2h-4V8Z"
                    fill="currentColor"
                    opacity=".85"
                  />
                </svg>
              </span>
              <input
                id="password"
                v-model="password"
                class="form-control"
                :type="showPassword ? 'text' : 'password'"
                autocomplete="new-password"
                required
                minlength="6"
                placeholder="At least 6 characters"
              />
              <button
                class="btn btn-eye"
                type="button"
                :aria-label="showPassword ? 'Hide password' : 'Show password'"
                :title="showPassword ? 'Hide password' : 'Show password'"
                @click="togglePassword"
              >
                <svg v-if="!showPassword" width="18" height="18" viewBox="0 0 24 24" fill="none">
                  <path
                    d="M12 5c5.5 0 9.5 5.5 9.5 7s-4 7-9.5 7S2.5 14.5 2.5 12 6.5 5 12 5Zm0 2C8 7 4.9 10.8 4.9 12S8 17 12 17s7.1-3.8 7.1-5S16 7 12 7Zm0 2.5A2.5 2.5 0 1 1 9.5 12 2.5 2.5 0 0 1 12 9.5Z"
                    fill="currentColor"
                    opacity=".85"
                  />
                </svg>
                <svg v-else width="18" height="18" viewBox="0 0 24 24" fill="none">
                  <path
                    d="M3.3 4.7 4.7 3.3l16 16-1.4 1.4-2.3-2.3c-1.4.8-3 1.3-4.9 1.3-5.5 0-9.5-5.5-9.5-7 0-1.2 2.2-4.3 5.7-6.1L3.3 4.7ZM12 7c-.9 0-1.8.2-2.6.5l1.7 1.7c.3-.1.6-.2.9-.2A2.5 2.5 0 0 1 14.5 12c0 .3-.1.6-.2.9l1.7 1.7c.3-.8.5-1.7.5-2.6 0-3-2.4-5-4.5-5Zm0 10c.9 0 1.8-.2 2.6-.5l-1.7-1.7c-.3.1-.6.2-.9.2A2.5 2.5 0 0 1 9.5 12c0-.3.1-.6.2-.9L8 9.4c-.3.8-.5 1.7-.5 2.6 0 3 2.4 5 4.5 5Zm0-12c5.5 0 9.5 5.5 9.5 7 0 .8-1.3 2.8-3.4 4.5l-1.4-1.4c1.7-1.4 2.4-2.8 2.4-3.1 0-1.2-3.1-5-7.1-5-1.2 0-2.3.3-3.3.7L7.2 6.3C8.6 5.5 10.2 5 12 5Z"
                    fill="currentColor"
                    opacity=".85"
                  />
                </svg>
              </button>
            </div>
            <div class="form-text soft-text">At least 6 characters.</div>
          </div>

          <!-- Confirm + show/hide (same toggle) -->
          <div class="mb-2">
            <label class="form-label" for="confirm">Confirm password</label>
            <div class="input-group auth-input">
              <span class="input-group-text">
                <!-- lock -->
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none">
                  <path
                    d="M17 10h-1V8a4 4 0 0 0-8 0v2H7a2 2 0 0 0-2 2v7a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2v-7a2 2 0 0 0-2-2Zm-7-2a2 2 0 0 1 4 0v2h-4V8Z"
                    fill="currentColor"
                    opacity=".85"
                  />
                </svg>
              </span>
              <input
                id="confirm"
                v-model="confirm"
                class="form-control"
                :type="showPassword ? 'text' : 'password'"
                autocomplete="new-password"
                required
                minlength="6"
                placeholder="Repeat your password"
              />
              <button
                class="btn btn-eye"
                type="button"
                :aria-label="showPassword ? 'Hide password' : 'Show password'"
                :title="showPassword ? 'Hide password' : 'Show password'"
                @click="togglePassword"
              >
                <svg v-if="!showPassword" width="18" height="18" viewBox="0 0 24 24" fill="none">
                  <path
                    d="M12 5c5.5 0 9.5 5.5 9.5 7s-4 7-9.5 7S2.5 14.5 2.5 12 6.5 5 12 5Zm0 2C8 7 4.9 10.8 4.9 12S8 17 12 17s7.1-3.8 7.1-5S16 7 12 7Zm0 2.5A2.5 2.5 0 1 1 9.5 12 2.5 2.5 0 0 1 12 9.5Z"
                    fill="currentColor"
                    opacity=".85"
                  />
                </svg>
                <svg v-else width="18" height="18" viewBox="0 0 24 24" fill="none">
                  <path
                    d="M3.3 4.7 4.7 3.3l16 16-1.4 1.4-2.3-2.3c-1.4.8-3 1.3-4.9 1.3-5.5 0-9.5-5.5-9.5-7 0-1.2 2.2-4.3 5.7-6.1L3.3 4.7ZM12 7c-.9 0-1.8.2-2.6.5l1.7 1.7c.3-.1.6-.2.9-.2A2.5 2.5 0 0 1 14.5 12c0 .3-.1.6-.2.9l1.7 1.7c.3-.8.5-1.7.5-2.6 0-3-2.4-5-4.5-5Zm0 10c.9 0 1.8-.2 2.6-.5l-1.7-1.7c-.3.1-.6.2-.9.2A2.5 2.5 0 0 1 9.5 12c0-.3.1-.6.2-.9L8 9.4c-.3.8-.5 1.7-.5 2.6 0 3 2.4 5 4.5 5Zm0-12c5.5 0 9.5 5.5 9.5 7 0 .8-1.3 2.8-3.4 4.5l-1.4-1.4c1.7-1.4 2.4-2.8 2.4-3.1 0-1.2-3.1-5-7.1-5-1.2 0-2.3.3-3.3.7L7.2 6.3C8.6 5.5 10.2 5 12 5Z"
                    fill="currentColor"
                    opacity=".85"
                  />
                </svg>
              </button>
            </div>
          </div>

          <!-- Remember me -->
          <div class="d-flex justify-content-between align-items-center mt-2 mb-3">
            <span class="small text-muted">You can change details later.</span>
          </div>

          <!-- CTA -->
          <button class="btn btn-primary w-100 btn-orange" type="submit" :disabled="loading">
            <span v-if="loading" class="spinner-border spinner-border-sm me-2" aria-hidden="true"></span>
            {{ loading ? 'Creating…' : 'Create account' }}
          </button>
        </form>

        <div class="d-flex justify-content-between mt-4">
          <RouterLink class="link subtle" to="/">Back to listings</RouterLink>
          <RouterLink class="link accent" to="/login">Already have an account?</RouterLink>
        </div>
      </div>
    </div>
  </main>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const router = useRouter()

const username = ref('')
const email = ref('')
const firstName = ref('')
const lastName = ref('')
const password = ref('')
const confirm = ref('')
const loading = ref(false)
const error = ref<string | null>(null)
const success = ref<string | null>(null)

const showPassword = ref(false)

function togglePassword() {
  showPassword.value = !showPassword.value
}

async function submit() {
  error.value = null
  success.value = null

  if (password.value !== confirm.value) {
    error.value = 'Passwords do not match'
    return
  }

  loading.value = true
  try {
    const result = await auth.signup({
      username: username.value,
      email: email.value,
      password: password.value,
      firstName: firstName.value,
      lastName: lastName.value,
    })

    // Optionally use rememberMe in your store to choose localStorage vs sessionStorage.
    if (result.tokenReturned) {
      await router.replace('/')
      return
    }

    success.value = 'Account created. Redirecting to login…'
    setTimeout(() => {
      router.push('/login')
    }, 600)
  } catch (e) {
    error.value = e instanceof Error ? e.message : String(e)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
/* Theme tokens (match login) */
.auth-wrap {
  --petify-orange: #ff7a18;
  --petify-orange-2: #ff9a3d;
  --petify-ink: #1f2937;

  position: relative;
  overflow: hidden;
  min-height: 100vh;
  display: grid;
  place-items: center;
  padding: 3rem 1rem;

  background:
    radial-gradient(1200px 600px at 15% 0%, rgba(255, 122, 24, 0.18), transparent 60%),
    radial-gradient(900px 500px at 95% 15%, rgba(255, 154, 61, 0.14), transparent 55%),
    linear-gradient(180deg, #fff7f0 0%, #ffffff 45%, #ffffff 100%);
}

/* Animated blobs */
.blob {
  position: absolute;
  width: 520px;
  height: 520px;
  border-radius: 999px;
  filter: blur(42px);
  opacity: 0.55;
  pointer-events: none;
  mix-blend-mode: multiply;
  animation: floaty 10s ease-in-out infinite;
  background: radial-gradient(circle at 30% 30%, rgba(255, 122, 24, 0.55), rgba(255, 154, 61, 0.12));
}

.blob-1 {
  top: -220px;
  left: -220px;
}

.blob-2 {
  bottom: -240px;
  right: -240px;
  animation-delay: -3.5s;
}

@keyframes floaty {
  0% {
    transform: translate3d(0, 0, 0) scale(1);
  }
  50% {
    transform: translate3d(28px, 18px, 0) scale(1.06);
  }
  100% {
    transform: translate3d(0, 0, 0) scale(1);
  }
}

/* Card */
.auth-card {
  width: 100%;
  max-width: 520px;
  border-radius: 18px;
  overflow: hidden;
  position: relative;
  z-index: 1;
  backdrop-filter: blur(6px);
}

/* top accent strip */
.auth-card::before {
  content: "";
  position: absolute;
  inset: 0 0 auto 0;
  height: 6px;
  background: linear-gradient(90deg, var(--petify-orange), var(--petify-orange-2));
}

.title {
  color: var(--petify-ink);
  letter-spacing: -0.2px;
}

.brand-badge {
  width: 46px;
  height: 46px;
  border-radius: 14px;
  display: grid;
  place-items: center;
  color: #fff;
  background: linear-gradient(135deg, var(--petify-orange), var(--petify-orange-2));
  box-shadow: 0 10px 25px rgba(255, 122, 24, 0.28);
}

.auth-alert {
  border-radius: 12px;
}

/* Input styling */
.auth-input .input-group-text {
  border-radius: 12px 0 0 12px;
  background: #fff;
  border-color: rgba(31, 41, 55, 0.12);
  color: rgba(31, 41, 55, 0.7);
  border-right: 0;
}

.auth-input .form-control {
  border-radius: 0;
  border-color: rgba(31, 41, 55, 0.12);
  padding: 0.75rem 0.9rem;
  border-left: 0;
}

.auth-input .form-control:focus {
  box-shadow: 0 0 0 0.25rem rgba(255, 122, 24, 0.18);
  border-color: rgba(255, 122, 24, 0.55);
}

/* Soft inputs (for non-group fields) */
.soft-input {
  border-radius: 12px;
  border-color: rgba(31, 41, 55, 0.12);
  padding: 0.75rem 0.9rem;
}

.soft-input:focus {
  box-shadow: 0 0 0 0.25rem rgba(255, 122, 24, 0.18);
  border-color: rgba(255, 122, 24, 0.55);
}

.soft-text {
  color: rgba(31, 41, 55, 0.65);
}

/* Eye button */
.btn-eye {
  border-radius: 0 12px 12px 0;
  border: 1px solid rgba(31, 41, 55, 0.12);
  border-left: 0;
  background: #fff;
  color: rgba(31, 41, 55, 0.7);
  padding: 0 0.85rem;
}

.btn-eye:hover {
  color: rgba(31, 41, 55, 0.95);
}

.btn-eye:focus {
  box-shadow: 0 0 0 0.25rem rgba(255, 122, 24, 0.18);
}

/* Orange CTA button */
.btn-orange {
  border: none;
  border-radius: 12px;
  padding: 0.8rem 1rem;
  font-weight: 600;
  background: linear-gradient(135deg, var(--petify-orange), var(--petify-orange-2));
  box-shadow: 0 12px 24px rgba(255, 122, 24, 0.25);
}

.btn-orange:disabled {
  opacity: 0.75;
  box-shadow: none;
}

.btn-orange:hover {
  filter: brightness(0.98);
}

/* Links */
.link {
  text-decoration: none;
}

.link.subtle {
  color: rgba(31, 41, 55, 0.75);
}

.link.subtle:hover {
  color: rgba(31, 41, 55, 0.95);
}

.link.accent {
  color: #ff7a18;
  font-weight: 600;
}

.link.accent:hover {
  color: #e76610;
}

/* Respect reduced motion */
@media (prefers-reduced-motion: reduce) {
  .blob {
    animation: none;
  }
}
</style>
