<template>
	<view class="app">
		<router-view />
	</view>
</template>

<script>
import { onLaunch, onShow, onHide, onPageNotFound } from '@dcloudio/uni-app'
import router from './router'

export default {
	onLaunch: function() {
		console.log('App Launch')
		this.checkLoginStatus();
	},
	onShow: function() {
		console.log('App Show')
	},
	onHide: function() {
		console.log('App Hide')
	},
	onPageNotFound: function() {
		uni.redirectTo({ url: '/pages/login/login' });
	},
	methods: {
		// 检查登录状态
		checkLoginStatus() {
			const token = uni.getStorageSync('token');
			if (!token) {
				// 未登录，跳转到登录页
				uni.reLaunch({ url: '/pages/login/login' });
			} else {
				// 已登录，验证token有效性
				this.validateToken(token);
			}
		},
		// 验证token有效性
		validateToken(token) {
			// 简单验证：检查token是否存在且格式正确
			// 实际应用中可以添加更复杂的验证，如检查过期时间
			if (token && token.length > 0) {
				// 检查路由是否为登录页，如果是则跳转到电影列表
				const pages = getCurrentPages();
				if (pages.length > 0 && pages[0].route === 'pages/login/login') {
					uni.reLaunch({ url: '/pages/movies/movies' });
				}
			} else {
				uni.removeStorageSync('token');
				uni.reLaunch({ url: '/pages/login/login' });
			}
		}
	}
}

// 添加全局路由守卫
uni.addInterceptor('navigateTo', {
	onBefore: function(options) {
		const token = uni.getStorageSync('token');
		// 白名单页面，不需要登录
		const whiteList = ['/pages/login/login'];
		const path = options.url.split('?')[0];
		if (!whiteList.includes(path) && !token) {
			uni.reLaunch({ url: '/pages/login/login' });
			return false;
		}
		return true;
	}
});

uni.addInterceptor('redirectTo', {
	onBefore: function(options) {
		const token = uni.getStorageSync('token');
		const whiteList = ['/pages/login/login'];
		const path = options.url.split('?')[0];
		if (!whiteList.includes(path) && !token) {
			uni.reLaunch({ url: '/pages/login/login' });
			return false;
		}
		return true;
	}
});

uni.addInterceptor('reLaunch', {
	onBefore: function(options) {
		const token = uni.getStorageSync('token');
		const whiteList = ['/pages/login/login'];
		const path = options.url.split('?')[0];
		if (!whiteList.includes(path) && !token) {
			uni.reLaunch({ url: '/pages/login/login' });
			return false;
		}
		return true;
	}
});
</script>

<style>
/*每个页面公共css */
page {
	background-color: #f5f5f5;
}
</style>
