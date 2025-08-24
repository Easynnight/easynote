<template>
  <div class="movie-add-container">
    <view class="header">
      <text class="title">新增电影</text>
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
        <label>上传截图</label>
        <button type="button" @click="chooseImage" class="upload-btn">选择图片</button>
        <view class="image-preview" v-if="imagePath">
          <image :src="imagePath" mode="aspectFill"></image>
          <button type="button" @click="removeImage" class="remove-btn">移除</button>
        </view>
      </view>

      <view class="form-actions">
        <button type="button" @click="navigateBack" class="cancel-btn">取消</button>
        <button type="submit" :loading="isSubmitting" class="submit-btn">提交</button>
      </view>
    </form>
  </div>
</template>

<script>
export default {
  data() {
    return {
      movie: {
        movieName: '',
        description: '',
        tags: '',
        url: ''
      },
      imagePath: '',
      imageBase64: '',
      isSubmitting: false
    };
  },
  methods: {
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
    },
    submitForm() {
      this.isSubmitting = true;

      // 添加图片数据
      if (this.imageBase64) {
        this.movie.screenshots = this.imageBase64;
      }

      uni.request({
        url: '/api/movies',
        method: 'POST',
        data: this.movie,
        header: {
          'Authorization': `Bearer ${uni.getStorageSync('token')}`
        },
        success: (res) => {
          if (res.data.success) {
            uni.showToast({ title: '添加成功', icon: 'success' });
            setTimeout(() => {
              uni.navigateBack();
            }, 1500);
          } else {
            uni.showToast({ title: '添加失败: ' + (res.data.message || '未知错误'), icon: 'none' });
          }
        },
        fail: (err) => {
          uni.showToast({ title: '网络错误', icon: 'none' });
          console.error('Submit movie error:', err);
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
.movie-add-container {
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