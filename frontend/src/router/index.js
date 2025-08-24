import { createRouter } from 'vue-router'
import Login from '../components/Login.vue'
import NoteList from '../components/NoteList.vue'
import NoteAdd from '../components/NoteAdd.vue'
import NoteEdit from '../components/NoteEdit.vue'

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: Login
  },
  {
    path: '/notes',
    name: 'NoteList',
    component: NoteList,
    meta: { requiresAuth: true }
  },
  {
    path: '/notes/add',
    name: 'NoteAdd',
    component: NoteAdd,
    meta: { requiresAuth: true }
  },
  {
    path: '/notes/edit/:id',
    name: 'NoteEdit',
    component: NoteEdit,
    meta: { requiresAuth: true }
  }
]

import { createWebHashHistory } from 'vue-router'

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  // 确保在每次路由跳转时都检查localStorage
  
  
  
  // 检查路由是否需要认证
  const requiresAuth = to.matched.some(record => record.meta.requiresAuth);
  
  if (requiresAuth) {
    // 检查用户是否已登录
    const token = localStorage.getItem('token');
    const isLoggedIn = !!token;
    
    
    
    if (!isLoggedIn) {
      
      // 未登录，重定向到登录页面
      next({
        path: '/login',
        query: { redirect: to.fullPath }
      });
    } else {
      
      // 已登录，继续访问
      next();
    }
  } else {
    
    // 不需要认证的路由，直接访问
    next();
  }
  
})

export default router