import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/HomeView.vue')
  },
  {
    path: '/companies',
    name: 'CompanyList',
    component: () => import('@/views/CompanyListView.vue')
  },
  {
    path: '/companies/:corporateNumber',
    name: 'CompanyDetail',
    component: () => import('@/views/CompanyDetailView.vue')
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

export default router
