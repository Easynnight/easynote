import Vue from 'vue'
import App from './App'
import './uni.promisify.adaptor'
// 导入配置文件
import config from './utils/config'

Vue.config.productionTip = false

// 添加请求拦截器
uni.addInterceptor('request', {
  invoke(args) {
    // 请求前的处理逻辑
    console.log('Original Request:', args);
    
    // 处理URL，确保使用正确的协议和基础URL
    if (args.url && !args.url.startsWith('http')) {
      // 如果URL不是以/api开头，则添加/api前缀
      let urlPath = args.url;
      if (!urlPath.startsWith('/api')) {
        urlPath = '/api' + (urlPath.startsWith('/') ? urlPath : '/' + urlPath);
      }
      
      // 使用配置的基础URL
      args.url = config.baseUrl + urlPath;
      console.log('Processed Request URL:', args.url);
    }
    
    // 添加认证头
    const token = uni.getStorageSync('token');
    if (token) {
      args.header = args.header || {};
      args.header['Authorization'] = `Bearer ${token}`;
    }
  },
  fail(err) {
    console.error('Request failed:', err);
  }
});

// 使用全局变量来管理web-view请求标记
let isWebviewRequest = false;

// 提供全局访问方法
Vue.prototype.$setWebviewRequest = function(value) {
  isWebviewRequest = value;
};

// 添加响应拦截器
uni.addInterceptor('request', {
  success(res) {
    // 检查是否是web-view的请求，如果是则跳过401/403处理
    if (isWebviewRequest) {
      // 重置标记
      isWebviewRequest = false;
      return;
    }
    
    // 处理HTTP错误状态码
    if (res.statusCode >= 400) {
      // 处理401和403错误（未授权或权限不足）
      if (res.statusCode === 401 || res.statusCode === 403) {
        // 清除无效token
        uni.removeStorageSync('token');
        uni.removeStorageSync('username');
        
        // 获取当前页面路由
        const pages = getCurrentPages();
        const currentPage = pages[pages.length - 1].route;
        
        // 避免无限重定向
        if (currentPage !== 'pages/login/login') {
          uni.reLaunch({ url: '/pages/login/login' });
        }
      }
    }
  },
  fail(err) {
    console.error('Response failed:', err);
    // 重置标记
    isWebviewRequest = false;
  }
});

App.mpType = 'app'

const app = new Vue({
  ...App
})
app.$mount()
