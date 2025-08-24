<template>
  <div class="movie-list-container">
    <view class="header">
      <text class="title">电影列表</text>
      <button class="add-btn" @click="navigateToAdd">新增电影</button>
    </view>

    <!-- 搜索框 -->
    <view class="search-container">
      <input type="text" v-model="movieNameQuery" placeholder="输入电影名搜索..." class="search-input">
      <input type="text" v-model="tagsQuery" placeholder="输入标签搜索..." class="search-input">
      <button @click="fetchMovies" class="search-btn">搜索</button>
    </view>

    <!-- 加载状态 -->
    <view v-if="loading" class="loading">
      <view class="spinner"></view>
      <text>正在加载电影数据...</text>
    </view>

    <!-- 错误提示 -->
    <view v-else-if="error" class="error">
      <text>{{ error }}</text>
      <button @click="fetchMovies">重试</button>
    </view>

    <!-- 电影列表 -->
    <view v-else class="movies-grid">
      <view v-for="movie in movies" :key="movie.id" class="movie-card" @click="handleMovieCardClick(movie)">
        <view class="screenshots-container">
          <image v-if="movie.screenshots" :src="'data:image/jpeg;base64,' + movie.screenshots" mode="aspectFill" class="screenshot"></image>
          <view v-else class="no-screenshot">无截图</view>
        </view>
        <text class="movie-name" :title="movie.movieName">{{ truncate(movie.movieName, 20) }}</text>
        <text class="description">{{ truncate(movie.description, 100) }}</text>
        <text class="tags">{{ movie.tags }}</text>
        <view class="actions">
          <button @click.stop="navigateToEdit(movie.id)" class="edit-link">编辑</button>
          <button @click.stop="deleteMovie(movie.id)" class="delete-btn">删除</button>
        </view>
      </view>
      <view v-if="movies.length === 0" class="empty-state">
        <text>没有找到电影数据</text>
      </view>
    </view>

    <!-- 分页控件 -->
    <view v-if="!loading && !error && !movieNameQuery && !tagsQuery" class="pagination">
      <button @click="page > 1 && (page--, fetchMovies())" :disabled="page === 1">上一页</button>
      <text>第 {{ page }} 页 / 共 {{ totalPages }} 页</text>
      <text>共 {{ total }} 条记录</text>
      <picker mode="selector" :range="pageSizes" v-model="pageSizeIndex" @change="onPageSizeChange">
        <view class="picker">
          {{ pageSizes[pageSizeIndex] }}条/页
        </view>
      </picker>
      <button @click="page < totalPages && (page++, fetchMovies())" :disabled="page === totalPages">下一页</button>
    </view>


  </div>
</template>

<script>
export default {
  data() {
    return {
      movies: [],
      loading: false,
      error: '',
      movieNameQuery: '',
      tagsQuery: '',
      page: 1,
      pageSize: 10,
      pageSizes: ['5', '10', '12', '20', '50'],
      pageSizeIndex: 1, // 默认选中10条/页
      total: 0,
      totalPages: 0
    };
  },
  onShow() {
    // 每次页面显示时重新加载数据
    this.fetchMovies();
  },
  methods: {
    fetchMovies() {
      this.loading = true;
      this.error = '';

      // 构建请求参数
      const params = {
        page: this.page,
        pageSize: this.pageSize
      };

      if (this.movieNameQuery) {
        params.movieName = this.movieNameQuery;
      }

      if (this.tagsQuery) {
        params.tags = this.tagsQuery;
      }

      // 使用uni.request替代fetch
      uni.request({
        url: '/api/movies/movies/page',
        method: 'GET',
        data: params,
        header: {
          'Authorization': `Bearer ${uni.getStorageSync('token')}`
        },
        success: (res) => {
          console.log('Backend response:', res.data);
          // 后端分页接口返回的是包含movies、total等字段的对象
          if (res.data) {
            this.movies = res.data.movies || [];
            this.total = res.data.total || 0;
            this.totalPages = res.data.totalPages || 0;
          } else {
            this.error = '获取电影数据失败: 返回数据格式不正确';
          }
        },
        fail: (err) => {
          this.error = '网络错误: 请检查您的连接';
          console.error('Fetch movies error:', err);
        },
        complete: () => {
          this.loading = false;
        }
      });
    },
    truncate(str, length) {
      if (!str) return '';
      if (str.length <= length) return str;
      return str.substring(0, length) + '...';
    },
    navigateToAdd() {
      uni.navigateTo({ url: '/pages/movies/add/add' });
    },
    navigateToEdit(id) {
      uni.navigateTo({ url: `/pages/movies/edit/edit?id=${id}` });
    },
    deleteMovie(id) {
      uni.showModal({
        title: '确认删除',
        content: '确定要删除这部电影吗？',
        success: (res) => {
          if (res.confirm) {
            uni.request({
              url: `/api/movies/${id}`,
              method: 'DELETE',
              header: {
                'Authorization': `Bearer ${uni.getStorageSync('token')}`
              },
              success: (res) => {
                if (res.data.success) {
                  uni.showToast({ title: '删除成功', icon: 'success' });
                  this.fetchMovies(); // 重新加载列表
                } else {
                  uni.showToast({ title: '删除失败: ' + (res.data.message || '未知错误'), icon: 'none' });
                }
              },
              fail: (err) => {
                uni.showToast({ title: '网络错误', icon: 'none' });
                console.error('Delete movie error:', err);
              }
            });
          }
        }
      });
    },
    // 电影卡片点击处理方法
    handleMovieCardClick(movie) {
      console.log('电影卡片被点击:', movie);
      if (!movie) {
        console.error('电影数据为空');
        return;
      }
      
      console.log('电影URL:', movie.url);
      if (movie.url) {
        // 准备要加载的URL
        let validUrl = movie.url;
        
        // 确保URL是有效的绝对路径，不是API请求路径
        if (!validUrl.startsWith('http://') && !validUrl.startsWith('https://')) {
          // 检查是否以/api开头的路径
          if (validUrl.startsWith('/api')) {
            console.error('电影URL是API路径，不能打开');
            uni.showToast({ title: '该链接无法打开', icon: 'none' });
            return;
          }
          
          // 为非HTTP/HTTPS协议的URL添加http协议头
          validUrl = 'http://' + validUrl;
        }
        
        console.log('准备用默认浏览器打开的URL:', validUrl);
      
      // 尝试多种打开方式，并提供清晰的用户反馈
      try {
        // 1. 简化判断逻辑，首先尝试使用任何可用的打开方式
        let opened = false;
        
        // 尝试使用window.open（适用于H5平台）
        if (typeof window !== 'undefined' && typeof window.open === 'function') {
          console.log('尝试使用window.open打开链接');
          const newWindow = window.open(validUrl, '_blank');
          if (newWindow) {
            console.log('浏览器打开成功');
            opened = true;
          } else {
            console.warn('window.open调用但未返回窗口对象，可能被阻止');
          }
        } else {
          console.warn('window对象或window.open方法不可用');
        }
        
        // 如果window.open失败，尝试使用uni.openURL
        if (!opened && typeof uni !== 'undefined' && typeof uni.openURL === 'function') {
          console.log('尝试使用uni.openURL打开链接');
          // 使用Promise方式处理，确保能够等待结果
          return new Promise((resolve, reject) => {
            uni.openURL({
              url: validUrl,
              success: () => {
                console.log('链接打开成功');
                opened = true;
                resolve();
              },
              fail: (err) => {
                console.error('uni.openURL调用失败:', err);
                // 尝试使用location.href
                if (typeof window !== 'undefined') {
                  console.log('尝试使用location.href打开链接');
                  window.location.href = validUrl;
                  opened = true;
                  resolve();
                } else {
                  reject(err);
                }
              },
              complete: () => {
                if (!opened) {
                  handleOpenFailure(validUrl);
                  resolve();
                }
              }
            });
          });
        } else if (!opened) {
          // 如果上述方法都不可用，尝试使用location.href
          if (typeof window !== 'undefined') {
            console.log('尝试使用location.href打开链接');
            window.location.href = validUrl;
            opened = true;
          } else {
            // 所有方法都失败，显示错误信息并提供复制链接选项
            handleOpenFailure(validUrl);
          }
        }
      } catch (err) {
        console.error('打开链接时发生错误:', err);
        // 统一的错误处理
        handleOpenFailure(validUrl, err);
      }
      
      // 封装打开失败的处理逻辑为函数
      function handleOpenFailure(url, error = null) {
        console.error('无法打开外部链接：不支持任何可用的打开方式', error);
        
        // 显示错误提示并提供复制链接的选项
        uni.showModal({
          title: '无法打开链接',
          content: '当前环境不支持直接打开外部链接，是否需要复制链接地址？',
          confirmText: '复制链接',
          cancelText: '取消',
          success: (res) => {
            if (res.confirm) {
              // 尝试复制链接到剪贴板
              if (typeof window !== 'undefined' && navigator.clipboard) {
                navigator.clipboard.writeText(url).then(() => {
                  uni.showToast({ title: '链接已复制到剪贴板', icon: 'none' });
                }).catch(() => {
                  console.warn('复制链接到剪贴板失败');
                  // 降级方案：提示用户手动复制
                  showLinkPrompt(url);
                });
              } else {
                // 不支持剪贴板API时，显示链接供用户手动复制
                showLinkPrompt(url);
              }
            }
          }
        });
      }
      
      // 显示链接提示，供用户手动复制
      function showLinkPrompt(url) {
        uni.showModal({
          title: '链接地址',
          content: url,
          showCancel: false,
          confirmText: '我知道了'
        });
      }
      } else {
        console.log('电影没有URL属性或URL为空');
        uni.showToast({ title: '该电影没有相关链接', icon: 'none' });
      }
    },
    
    onPageSizeChange(e) {
      this.pageSizeIndex = e.detail.value;
      this.pageSize = parseInt(this.pageSizes[this.pageSizeIndex]);
      this.page = 1;
      this.fetchMovies();
    }
  }
};
</script>

<style scoped>
/* 保留原始样式并适配uni-app */
.movie-list-container {
  position: relative;
  z-index: 1;
  padding: 20rpx;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.title {
  font-size: 36rpx;
  font-weight: bold;
}

.add-btn {
  background-color: #007aff;
  color: white;
  padding: 10rpx 20rpx;
  border-radius: 5rpx;
}

.search-container {
  display: flex;
  margin-bottom: 20rpx;
  flex-wrap: wrap;
  gap: 10rpx;
}

.search-input {
  flex: 1;
  padding: 15rpx;
  border: 1rpx solid #ddd;
  border-radius: 5rpx;
  min-width: 200rpx;
}

.search-btn {
  background-color: #007aff;
  color: white;
  padding: 0 20rpx;
}

.loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 100rpx 0;
}

.spinner {
  width: 50rpx;
  height: 50rpx;
  border: 5rpx solid #f3f3f3;
  border-top: 5rpx solid #3498db;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 20rpx;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.error {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 100rpx 0;
  color: #ff0000;
}

.movies-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300rpx, 1fr));
  gap: 20rpx;
}

.movie-card {
  background-color: #fff;
  border-radius: 10rpx;
  overflow: hidden;
  box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.1);
  padding: 15rpx;
}

.screenshots-container {
  width: 100%;
  height: 200rpx;
  background-color: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 10rpx;
}

.screenshot {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.no-screenshot {
  color: #999;
}

.movie-name {
  font-size: 28rpx;
  font-weight: bold;
  margin-bottom: 5rpx;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.description {
  font-size: 24rpx;
  color: #666;
  margin-bottom: 5rpx;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.tags {
  font-size: 22rpx;
  color: #007aff;
  margin-bottom: 10rpx;
}

.actions {
  display: flex;
  justify-content: space-between;
}

.edit-link {
  color: #007aff;
  padding: 5rpx 10rpx;
  font-size: 24rpx;
  background: none;
  border: 1rpx solid #007aff;
  border-radius: 5rpx;
}

.delete-btn {
  color: #ff3b30;
  padding: 5rpx 10rpx;
  font-size: 24rpx;
  background: none;
  border: 1rpx solid #ff3b30;
  border-radius: 5rpx;
}

.empty-state {
  text-align: center;
  padding: 100rpx 0;
  color: #999;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 15rpx;
  margin-top: 30rpx;
  flex-wrap: wrap;
  padding: 10rpx;
}

.pagination button {
  padding: 5rpx 15rpx;
  background-color: #f5f5f5;
  border: 1rpx solid #ddd;
  border-radius: 5rpx;
  font-size: 24rpx;
}

.pagination button:disabled {
  color: #999;
  background-color: #f0f0f0;
}

.picker {
  padding: 5rpx 15rpx;
  border: 1rpx solid #ddd;
  border-radius: 5rpx;
  font-size: 24rpx;
}


</style>