
import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/store/auth'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/Home.vue')
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue')
  },
  {
    path: '/student-info',
    name: 'StudentInfo',
    component: () => import('@/views/StudentInfoInput.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/colleges',
    name: 'Colleges',
    component: () => import('@/views/Colleges.vue')
  },
  {
    path: '/colleges/:id',
    name: 'CollegeDetail',
    component: () => import('@/views/CollegeDetail.vue')
  },
  {
    path: '/majors',
    name: 'Majors',
    component: () => import('@/views/Majors.vue')
  },
  {
    path: '/admission-plans',
    name: 'AdmissionPlans',
    component: () => import('@/views/AdmissionPlan.vue')
  },
  {
    path: '/recommend',
    name: 'Recommend',
    component: () => import('@/views/Recommend.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/volunteer-filter',
    name: 'VolunteerFilter',
    component: () => import('@/views/VolunteerFilter.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/volunteer',
    name: 'Volunteer',
    component: () => import('@/views/Volunteer.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/risk-assessment',
    name: 'RiskAssessment',
    component: () => import('@/views/RiskAssessment.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/submit',
    name: 'MockSubmission',
    component: () => import('@/views/MockSubmission.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/plans',
    name: 'Plans',
    component: () => import('@/views/Plans.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/policies',
    name: 'PolicyNews',
    component: () => import('@/views/PolicyNews.vue')
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('@/views/Profile.vue'),
    meta: { requiresAuth: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()
  
  if (to.meta.requiresAuth && !authStore.isLoggedIn) {
    next('/login')
  } else {
    next()
  }
})

export default router
