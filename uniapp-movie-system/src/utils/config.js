// API配置文件
// 用于处理不同环境下的API基础URL

// 判断当前运行环境
const isH5 = process.env.UNI_PLATFORM === 'h5';
const isDev = process.env.NODE_ENV === 'development';

// 基础URL配置
let baseUrl = '';

// 在H5开发环境下，使用相对路径（依赖于devServer代理）
// 在移动设备或生产环境下，使用完整的后端API地址
if (isH5 && isDev) {
  // 开发环境H5，使用相对路径，由devServer代理处理
  baseUrl = '';
} else {
  // 移动设备或生产环境，使用完整的后端API地址
  // 根据用户提供的信息，后端服务实际运行在192.168.31.84:8086
  // 与vue.config.js中的代理配置保持一致
  baseUrl = 'http://192.168.31.84:8086';
}

export default {
  // API基础URL
  baseUrl,
  
  // 接口地址
  api: {
    login: '/api/auth/login',
    movies: '/api/movies/movies/page',
    movieDetail: '/api/movies/'
  }
};