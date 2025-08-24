<template>
  <div class="login-container">
    <h2>用户登录</h2>
    <form @submit.prevent="login">
      <div class="form-group">
        <label for="username">用户名</label>
        <input type="text" id="username" v-model="username" placeholder="请输入用户名" required autocomplete="off">
      </div>
      <div class="form-group">
        <label for="password">密码</label>
        <input type="password" id="password" v-model="password" placeholder="请输入密码" required autocomplete="off">
      </div>
      <button type="submit" class="login-btn" :class="{ 'loading-active': isLoading }" :disabled="isLoading">
        {{ isLoading ? '登录中...' : '登录' }}
      </button>
    </form>
    <div class="error-message" v-if="errorMsg">{{ errorMsg }}</div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      username: '',
      password: '',
      isLoading: false,
      errorMsg: ''
    }
  },
  methods: {
    async login() {
      if (!this.username || !this.password) {
        this.errorMsg = '用户名和密码不能为空';
        return;
      }

      try {
        this.isLoading = true;
        this.errorMsg = '';
        
        const response = await fetch('/api/auth/login', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify({ username: this.username, password: this.password })
        });

        if (!response.ok) {
          throw new Error('登录失败: ' + response.statusText);
        }

        const data = await response.json();
        
        // 保存登录状态
        localStorage.setItem('isLoggedIn', 'true');
        localStorage.setItem('username', this.username);
        localStorage.setItem('token', data.token);
        
        // 跳转到笔记列表页面
        this.$router.push('/notes');
        
      } catch (error) {
        console.error('登录错误:', error);
        this.errorMsg = '登录失败: ' + error.message;
      } finally {
        this.isLoading = false;
      }
    }
  }
}
</script>

<style scoped>
/* 全局样式重置 */
* {
  box-sizing: border-box;
  margin: 0;
  padding: 0;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  line-height: 1.5;
  background-color: #f5f5f5;
  color: #333;
  min-height: 100vh;
}

#app {
  min-height: 100vh;
  padding: 0;
  margin: 0;
}

.login-container {
  max-width: 400px;
  margin: 100px auto;
  padding: 40px;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.login-container h2 {
  text-align: center;
  margin-bottom: 30px;
  font-size: 24px;
  color: #333;
  font-weight: 600;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-size: 14px;
  font-weight: 500;
  color: #555;
}

.form-group input {
  width: 100%;
  padding: 12px 16px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 16px;
  transition: border-color 0.3s ease;
}

.form-group input:focus {
  outline: none;
  border-color: #4a90e2;
  box-shadow: 0 0 0 2px rgba(74, 144, 226, 0.2);
}

.login-btn {
  width: 100%;
  padding: 12px;
  background-color: #4a90e2;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.3s ease;
}

.login-btn:hover:not(:disabled) {
  background-color: #357abd;
}

.login-btn:active:not(:disabled) {
  transform: translateY(1px);
}

.login-btn:disabled {
  background-color: #ccc;
  cursor: not-allowed;
  opacity: 0.7;
}

/* 加载中按钮样式 */
.login-btn.loading-active {
  position: relative;
}

.login-btn.loading-active::after {
  content: '';
  position: absolute;
  top: 50%;
  right: 16px;
  width: 16px;
  height: 16px;
  margin-top: -8px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top: 2px solid white;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 错误信息样式 */
.error-message {
  color: #e74c3c;
  font-size: 14px;
  margin-top: 12px;
  padding: 8px 12px;
  background-color: #fadbd8;
  border-radius: 4px;
  border-left: 4px solid #e74c3c;
}

/* 移动端适配 */
@media screen and (max-width: 480px) {
  .login-container {
    margin: 50px 20px;
    padding: 24px;
  }

  .login-container h2 {
    font-size: 20px;
    margin-bottom: 20px;
  }

  .form-group {
    margin-bottom: 16px;
  }

  .form-group input,
  .login-btn {
    padding: 10px 14px;
    font-size: 14px;
  }
}
</style>