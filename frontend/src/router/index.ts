import { createRouter, createWebHistory } from 'vue-router'
import ListingsView from '../views/ListingsView.vue'
import ListingDetailsView from '../views/ListingDetailsView.vue'
import LoginView from '../views/LoginView.vue'
import SignupView from '../views/SignupView.vue'
import ProfileView from '../views/ProfileView.vue'
import OwnerProfileView from '../views/OwnerProfileView.vue'
import AdminModerationView from '@/views/AdminModerationView.vue'
import AdminClientsView from '@/views/AdminClientsView.vue'
import AdminListingsView from '@/views/AdminListingsView.vue'
import ClinicDashboardView from '@/views/ClinicDashboardView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'listings',
      component: ListingsView,
    },
    {
      path: '/login',
      name: 'login',
      component: LoginView,
    },
    {
      path: '/signup',
      name: 'signup',
      component: SignupView,
    },
    {
      path: '/profile',
      name: 'profile',
      component: ProfileView,
    },
    {
      path: '/clinics',
      name: 'clinic-dashboard',
      component: ClinicDashboardView,
    },
    {
      path: '/profile/:userId',
      name: 'user-profile',
      component: ProfileView,
    },
    {
      path: '/owner/:ownerId',
      name: 'owner-profile',
      component: OwnerProfileView,
    },
    {
      path: '/listing/:id',
      name: 'listing-details',
      component: ListingDetailsView,
    },
    {
      path: '/admin/clinics',
      name: 'admin-clinics',
      component: AdminModerationView,
    },
    {
      path: '/admin/clients',
      name: 'admin-clients',
      component: AdminClientsView,
    },
    {
      path: '/admin/listings',
      name: 'admin-listings',
      component: AdminListingsView,
    },
  ],
})

export default router
