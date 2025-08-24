module.exports = {
  devServer: {
    port: 8080,
    https: false,
    proxy: {
      '/api': {
        target: 'http://192.168.31.84:8086',
        changeOrigin: true,
        ws: true,
        pathRewrite: {
          '^/api': '/api'
        },
        logLevel: 'debug',
        secure: false,
        cookiePathRewrite: {'/': '/'}
      }
    }
  }
}