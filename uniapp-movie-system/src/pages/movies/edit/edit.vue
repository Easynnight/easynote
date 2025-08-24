<template>
  <div class="movie-edit-container">
    <view class="header">
      <text class="title">编辑电影</text>
    </view>

    <form @submit.prevent="submitForm">
      <view class="form-group">
        <label>电影名称</label>
        <input type="text" v-model="movie.movieName" required placeholder="请输入电影名称">
      </view>

      <view class="form-group">
        <label>电影描述</label>
        <textarea v-model="movie.description" placeholder="请输入电影描述"></textarea>
      </view>

      <view class="form-group">
        <label>电影标签</label>
        <input type="text" v-model="movie.tags" placeholder="请输入电影标签，用逗号分隔">
      </view>

      <view class="form-group">
        <label>电影链接</label>
        <input type="url" v-model="movie.url" placeholder="请输入电影链接">
      </view>

      <view class="form-group">
        <label>电影截图</label>
        <button type="button" @click="chooseImage" class="upload-btn">更换图片</button>
        <view class="image-preview" v-if="imagePath">
          <image :src="imagePath" mode="aspectFill"></image>
          <button type="button" @click="removeImage" class="remove-btn">移除</button>
        </view>
      </view>

      <view class="form-actions">
        <button type="button" @click="navigateBack" class="cancel-btn">取消</button>
        <button type="submit" :loading="isSubmitting" class="submit-btn">保存</button>
      </view>
    </form>
  </div>
</template>

<script>
export default {
  data() {
    return {
      movie: {
        id: '',
        movieName: '',
        description: '',
        tags: '',
        url: '',
        screenshots: ''
      },
      imagePath: '',
      imageBase64: '',
      isSubmitting: false,
      loading: true,
      error: ''
    };
  },
  onLoad(options) {
    if (options.id) {
      this.movie.id = options.id;
      this.fetchMovieDetail();
    } else {
      this.error = '电影ID不存在';
      this.loading = false;
    }
  },
  methods: {
    fetchMovieDetail() {
      this.loading = true;

      uni.request({
        url: `/api/movies/${this.movie.id}`,
        method: 'GET',
        header: {
          'Authorization': `Bearer ${uni.getStorageSync('token')}`
        },
        success: (res) => {
          if (res.data.success) {
            this.movie = res.data.data;
            // 如果有截图，显示出来
            if (this.movie.screenshots) {
              this.imagePath = `data:image/jpeg;base64,${this.movie.screenshots}`;
              this.imageBase64 = this.movie.screenshots;
            }
          } else {
            this.error = '获取电影详情失败: ' + (res.data.message || '未知错误');
          }
        },
        fail: (err) => {
          this.error = '网络错误: 请检查您的连接';
          console.error('Fetch movie detail error:', err);
        },
        complete: () => {
          this.loading = false;
        }
      });
    },
    chooseImage() {
      uni.chooseImage({
        count: 1,
        sizeType: ['compressed'],
        sourceType: ['album', 'camera'],
        success: (res) => {
          this.imagePath = res.tempFilePaths[0];
          // 转换为base64
          uni.getFileSystemManager().readFile({
            filePath: this.imagePath,
            encoding: 'base64',
            success: (res) => {
              this.imageBase64 = res.data;
              this.movie.screenshots = this.imageBase64;
            },
            fail: (err) => {
              console.error('Read file error:', err);
              uni.showToast({ title: '图片读取失败', icon: 'none' });
            }
          });
        },
        fail: (err) => {
          console.error('Choose image error:', err);
        }
      });
    },
    removeImage() {
      this.imagePath = '';
      this.imageBase64 = '';
      this.movie.screenshots = '';
    },
    submitForm() {
      this.isSubmitting = true;

      uni.request({
        url: `/api/movies/${this.movie.id}`,
        method: 'PUT',
        data: this.movie,
        header: {
          'Authorization': `Bearer ${uni.getStorageSync('token')}`
        },
        success: (res) => {
          if (res.data.success) {
            uni.showToast({ title: '保存成功', icon: 'success' });
            setTimeout(() => {
              uni.navigateBack();
            }, 1500);
          } else {
            uni.showToast({ title: '保存失败: ' + (res.data.message || '未知错误'), icon: 'none' });
          }
        },
        fail: (err) => {
          uni.showToast({ title: '网络错误', icon: 'none' });
          console.error('Update movie error:', err);
        },
        complete: () => {
          this.isSubmitting = false;
        }
      });
    },
    navigateBack() {
      uni.navigateBack();
    }
  }
};
</script>

<style scoped>
.movie-edit-container {
  padding: 20rpx;
}

.header {
  margin-bottom: 30rpx;
}

.title {
  font-size: 36rpx;
  font-weight: bold;
}

.form-group {
  margin-bottom: 30rpx;
}

label {
  display: block;
  margin-bottom: 10rpx;
  font-size: 28rpx;
  color: #333;
}

input, textarea {
  width: 100%;
  padding: 15rpx;
  border: 1rpx solid #ddd;
  border-radius: 5rpx;
  font-size: 28rpx;
}

textarea {
  height: 200rpx;
  resize: vertical;
}

.upload-btn {
  background-color: #f5f5f5;
  color: #333;
  padding: 15rpx;
  border: 1rpx solid #ddd;
  border-radius: 5rpx;
  width: 100%;
}

.image-preview {
  margin-top: 20rpx;
  position: relative;
  width: 100%;
  height: 300rpx;
  background-color: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
}

image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.remove-btn {
  position: absolute;
  top: 10rpx;
  right: 10rpx;
  background-color: rgba(255, 0, 0, 0.7);
  color: white;
  border: none;
  border-radius: 50%;
  width: 50rpx;
  height: 50rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.form-actions {
  display: flex;
  gap: 20rpx;
  margin-top: 40rpx;
}

.cancel-btn {
  flex: 1;
  background-color: #f5f5f5;
  color: #333;
  border: 1rpx solid #ddd;
  border-radius: 5rpx;
  padding: 15rpx;
}

.submit-btn {
  flex: 1;
  background-color: #007aff;
  color: white;
  border: none;
  border-radius: 5rpx;
  padding: 15rpx;
}
</style>