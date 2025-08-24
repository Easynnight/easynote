<template>
  <div class="terminal-container">
    <canvas class="digital-rain"></canvas>
    <div class="terminal-header">
      <div class="terminal-buttons" style="display: none;">
        <div class="terminal-button"></div>
        <div class="terminal-button"></div>
        <div class="terminal-button"></div>
      </div>
      <div class="terminal-title" style="display: none;">SYSTEM ACCESS TERMINAL</div>
    </div>
    <div class="login-container">
      <h2 style="display: none;">SYSTEM ACCESS</h2>
      <form @submit.prevent="login">
        <div class="form-group">
          <label for="username" style="display: none;">USER ID</label>
          <input type="text" id="username" v-model="username" placeholder="" required autocomplete="off">
        </div>
        <div class="form-group">
          <label for="password" style="display: none;">AUTH CODE</label>
          <input type="password" id="password" v-model="password" placeholder="" required autocomplete="new-password">
        </div>
        <button type="button" class="login-btn" :class="{ 'loading-active': isLoading }" @click="login">{{ isLoading ? 'AUTHENTICATING...' : 'ACCESS' }}</button>
      </form>
      <div class="command-lines">
        <!-- 隐藏命令行文本 -->
        <div class="command-lines" style="display: none;">
          <div class="command-line">system@hackstation:~$ sudo access-system</div>
          <div class="command-line">[sudo] password for system: ********</div>
          <div class="command-line" v-if="!isLoading">system@hackstation:~$ waiting for authentication...</div>
        </div>
        <div class="error-message" v-if="errorMsg">{{ errorMsg }}</div>
      </div>
    </div>
  </div>
</template>

<script>
// 导入配置文件
import config from '../../utils/config';

export default {
  data() {
    return {
      username: '',
      password: '',
      isLoading: false,
      errorMsg: ''
    }
  },
  mounted() {
    this.initDigitalRain();
  },
  beforeDestroy() {
    this.cleanupDigitalRain();
  },
  methods: {
    initDigitalRain() {
      const canvas = uni.createSelectorQuery().select('.digital-rain').nodesRef;
      if (!canvas) return;
      canvas.fields({ node: true, size: true }, res => {
        const ctx = res.node.getContext('2d');
        const dpr = uni.getSystemInfoSync().pixelRatio;
        res.node.width = res.width * dpr;
        res.node.height = res.height * dpr;
        ctx.scale(dpr, dpr);

        // 数字雨配置
        const fontSize = 14;
        const columns = res.width / fontSize;
        const drops = [];

        // 初始化每列的起始位置
        for (let i = 0; i < columns; i++) {
          drops[i] = Math.random() * -100;
        }

        // 字符集
        const chars = '0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz!@#$%^&*()';

        const draw = () => {
          // 半透明黑色背景，产生拖尾效果
          ctx.fillStyle = 'rgba(0, 0, 0, 0.05)';
          ctx.fillRect(0, 0, res.width, res.height);

          ctx.fillStyle = '#00ff41';
          ctx.font = `${fontSize}px Courier New`;

          for (let i = 0; i < drops.length; i++) {
            // 随机字符
            const text = chars[Math.floor(Math.random() * chars.length)];

            // 绘制字符
            ctx.fillText(text, i * fontSize, drops[i] * fontSize);

            // 如果字符到达底部或随机重置
            if (drops[i] * fontSize > res.height && Math.random() > 0.975) {
              drops[i] = 0;
            }

            // 下降
            drops[i]++;
          }

          this.rainAnimation = requestAnimationFrame(draw);
        };

        draw();
      }).exec();
    },
    cleanupDigitalRain() {
      if (this.rainAnimation) {
        cancelAnimationFrame(this.rainAnimation);
      }
    },
    login() {
      console.log('Login method called');
      console.log('Username:', this.username);
      console.log('Password length:', this.password.length);
      this.isLoading = true;
      this.errorMsg = '';

      // 构建完整的API URL
      const apiUrl = config.baseUrl + config.api.login;
      console.log('API URL:', apiUrl);

      // 使用uni.request替代fetch
      uni.request({
        url: apiUrl,
        method: 'POST',
        data: {
          username: this.username,
          password: this.password
        },
        success: (res) => {
          console.log('Login response:', res);
          if (res.statusCode === 200) {
            if (res.data.token) {
              // 使用uni.setStorageSync替代localStorage
              uni.setStorageSync('token', res.data.token);
              uni.setStorageSync('username', this.username);
              // 跳转到电影列表页面
              uni.navigateTo({ url: '/pages/movies/movies' });
            } else {
              this.errorMsg = '登录失败: ' + (res.data.message || '无效的用户名或密码');
              console.log('Login failed - no token:', res.data);
            }
          } else {
            this.errorMsg = `登录失败: HTTP状态码 ${res.statusCode}`;
            console.log('Login failed - status code:', res.statusCode, res.data);
          }
        },
        fail: (err) => {
          this.errorMsg = '网络错误: 请检查您的连接';
          console.error('Login error:', err);
        },
        complete: () => {
          this.isLoading = false;
        }
      });
    }
  }
}
</script>

<style scoped>
/* 保留原始样式 */
.terminal-container {
  position: relative;
  width: 100%;
  height: 100vh;
  overflow: hidden;
  background-color: #000;
}

.digital-rain {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 0;
}

.login-container {
  position: relative;
  z-index: 1;
  max-width: 400px;
  margin: 100px auto;
  padding: 20px;
  background-color: rgba(0, 0, 0, 0.7);
  border-radius: 5px;
  box-shadow: 0 0 10px rgba(0, 255, 65, 0.3);
}

.form-group {
  margin-bottom: 15px;
}

input {
  width: 100%;
  padding: 10px;
  background-color: #111;
  border: 1px solid #00ff41;
  color: #00ff41;
  border-radius: 3px;
  font-family: 'Courier New', monospace;
}

.login-btn {
  width: 100%;
  padding: 10px;
  background-color: #00ff41;
  color: #000;
  border: none;
  border-radius: 3px;
  cursor: pointer;
  font-weight: bold;
  font-family: 'Courier New', monospace;
}

.loading-active {
  animation: blink 1s infinite;
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.error-message {
  color: #ff0000;
  margin-top: 15px;
  font-family: 'Courier New', monospace;
}
</style>