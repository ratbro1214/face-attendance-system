import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    component: () => import('@/views/layout/MainLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/Dashboard.vue'),
        meta: { title: '仪表盘', roles: ['STUDENT', 'TEACHER'] }
      },
      {
        path: 'users',
        name: 'UserManagement',
        component: () => import('@/views/user/UserManagement.vue'),
        meta: { title: '人员信息管理', roles: ['ADMIN'] }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/user/Profile.vue'),
        meta: { title: '个人信息' }
      },
      {
        path: 'password',
        name: 'ChangePassword',
        component: () => import('@/views/user/ChangePassword.vue'),
        meta: { title: '修改密码' }
      },
      {
        path: 'courses',
        name: 'CourseList',
        component: () => import('@/views/course/CourseList.vue'),
        meta: { title: '课程列表', roles: ['STUDENT', 'TEACHER', 'ADMIN'] }
      },
      {
        path: 'courses/:id',
        name: 'CourseDetail',
        component: () => import('@/views/course/CourseDetail.vue'),
        meta: { title: '课程详情', roles: ['STUDENT', 'TEACHER', 'ADMIN'] }
      },
      {
        path: 'attendance',
        name: 'AttendanceRecord',
        component: () => import('@/views/attendance/AttendanceRecord.vue'),
        meta: { title: '考勤记录', roles: ['STUDENT', 'TEACHER'] }
      },
      {
        path: 'attendance/statistics',
        name: 'AttendanceStatistics',
        component: () => import('@/views/attendance/AttendanceStatistics.vue'),
        meta: { title: '考勤统计', roles: ['TEACHER'] }
      },
      {
        path: 'attendance/leaves', name: 'LeaveManagement',
        component: () => import('@/views/attendance/LeaveManagement.vue'), meta: { title: '请假管理', roles: ['STUDENT', 'TEACHER'] }
      },
      {
        path: 'face/register',
        name: 'FaceRegister',
        component: () => import('@/views/face/FaceRegister.vue'),
        meta: { title: '人脸注册', roles: ['STUDENT'] }
      },
      {
        path: 'face/attendance',
        name: 'FaceAttendance',
        component: () => import('@/views/face/FaceRecognition.vue'),
        meta: { title: '人脸签到', roles: ['STUDENT'] }
      },
      {
        path: 'face/re-enroll-review', name: 'FaceReEnrollReview',
        component: () => import('@/views/face/FaceReEnrollReview.vue'), meta: { title: '人脸重录审批', roles: ['TEACHER'] }
      },
      {
        path: 'groups', name: 'StudentGroup',
        component: () => import('@/views/group/StudentGroup.vue'), meta: { title: '我的小组', roles: ['STUDENT'] }
      },
      {
        path: 'group-management', name: 'TeacherGroupManagement',
        component: () => import('@/views/group/TeacherGroupManagement.vue'), meta: { title: '小组管理', roles: ['TEACHER'] }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/NotFound.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫：不能只相信本地残留的 token，进入系统前必须确认用户身份有效。
router.beforeEach(async to => {
  const userStore = useUserStore()
  const token = localStorage.getItem('token')
  const isPublicPage = to.path === '/login'

  if (to.meta.requiresAuth && !token) {
    userStore.clearSession()
    return { path: '/login', query: { redirect: to.fullPath } }
  }

  if (!token) {
    if (userStore.isLoggedIn || userStore.userInfo) userStore.clearSession()
    return true
  }

  if (!userStore.userInfo) {
    try {
      await userStore.fetchUserInfo({ silent: true, skipAuthRedirect: true })
    } catch (error) {
      userStore.clearSession()
      return isPublicPage
        ? true
        : { path: '/login', query: { redirect: to.fullPath } }
    }
  }

  const home = userStore.isAdmin ? '/users' : '/'
  if (isPublicPage) return home
  if (userStore.isAdmin && to.path === '/') return '/users'
  if (to.meta.roles && !to.meta.roles.includes(userStore.userInfo?.role)) return home
  return true
})

export default router
