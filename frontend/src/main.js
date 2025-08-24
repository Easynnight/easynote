import { createApp } from 'vue'
import App from './App.vue'
import router from './router'

// 添加响应拦截器，处理403错误
window.fetchWithAuth = async (url, options = {}) => {
  // 添加认证头
  const token = localStorage.getItem('token');
  if (token) {
    options.headers = {
      ...options.headers,
      'Authorization': `Bearer ${token}`
    };
  }

  // 发送请求
  const response = await fetch(url, options);

  // 处理403错误
  if (response.status === 403) {
    // 清除无效token
    localStorage.removeItem('token');
    localStorage.removeItem('username');
    // 重定向到登录页
    router.push('/login');
    throw new Error('认证失败，请重新登录');
  }

  return response;
};

// 添加路由守卫
router.beforeEach((to, from, next) => {
  // 检查路由是否需要认证
  if (to.matched.some(record => record.meta.requiresAuth)) {
    // 检查是否已登录
    const isLoggedIn = !!localStorage.getItem('token');
    if (!isLoggedIn) {
      // 未登录，重定向到登录页面
      next({ name: 'Login' });
    } else {
      // 已登录，继续访问
      next();
    }
  } else {
    // 不需要认证的路由，直接访问
    next();
  }
})

createApp(App).use(router).mount('#app')