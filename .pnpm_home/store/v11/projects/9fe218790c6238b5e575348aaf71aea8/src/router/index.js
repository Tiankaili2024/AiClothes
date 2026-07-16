import { createRouter, createWebHistory } from 'vue-router'
import { isLoggedIn, getUser } from '../utils/auth'

const routes = [
  { path: '/login', name: 'Login', component: () => import('../views/login/LoginView.vue'), meta: { title: '登录' } },
  { path: '/forgot-password', name: 'ForgotPassword', component: () => import('../views/login/ForgotPasswordView.vue'), meta: { title: '找回密码' } },
  { path: '/register', name: 'Register', component: () => import('../views/register/RegisterView.vue'), meta: { title: '注册' } },
  { path: '/', component: () => import('../views/layout/LayoutView.vue'), redirect: '/outfit', meta: { requiresAuth: true },
    children: [
      { path: 'outfit', name: 'Outfit', component: () => import('../views/outfit/OutfitChatView.vue'), meta: { title: 'AI穿搭' } },
      { path: 'profile', name: 'Profile', component: () => import('../views/profile/ProfileView.vue'), meta: { title: '个人档案' } },
      { path: 'portrait', name: 'Portrait', component: () => import('../views/portrait/PortraitView.vue'), meta: { title: '穿搭画像' } },
      { path: 'history', name: 'History', component: () => import('../views/history/HistoryView.vue'), meta: { title: '穿搭记录' } },
      { path: 'favorite', name: 'Favorite', component: () => import('../views/favorite/FavoriteView.vue'), meta: { title: '我的收藏' } },
      { path: 'admin', component: () => import('../views/admin/AdminLayout.vue'), meta: { title: '管理后台', requiresAdmin: true },
        children: [
          { path: 'dashboard', component: () => import('../views/admin/DashboardView.vue'), meta: { title: '数据概览' } },
          { path: 'users', component: () => import('../views/admin/UserManageView.vue'), meta: { title: '用户管理' } },
          { path: 'config', component: () => import('../views/admin/ConfigView.vue'), meta: { title: '系统配置' } },
          { path: 'logs', component: () => import('../views/admin/LogsView.vue'), meta: { title: '操作日志' } },
          { path: 'gportrait', component: () => import('../views/admin/GlobalPortraitView.vue'), meta: { title: '全局画像' } },
        ]
      },
    ]
  },
  { path: '/:pathMatch(.*)*', redirect: '/' },
]

const router = createRouter({ history: createWebHistory(), routes })
router.beforeEach((to, from, next) => {
  document.title = to.meta.title ? to.meta.title + ' - 简搭AI' : '简搭AI'
  if (to.meta.requiresAuth && !isLoggedIn()) { next('/login'); return }
  if (to.meta.requiresAdmin) {
    const user = getUser()
    if (!user || user.role !== 1) { next('/outfit'); return }
  }
  next()
})

export default router