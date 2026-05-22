import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'
import path from 'path'

export default defineConfig({
  plugins: [react(), tailwindcss()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
  },
  server: {
    proxy: {
      // Portfolio service
      '/api/portfolios': {
        target: 'http://localhost:8081',
        changeOrigin: true,
        secure: false,
      },
      // Market service
      '/api/market-prices': {
        target: 'http://localhost:8082',
        changeOrigin: true,
        secure: false,
      },
      // Advisor service
      '/api/recommendations': {
        target: 'http://localhost:8083',
        changeOrigin: true,
        secure: false,
      },
      // Fallback for other /api calls to portfolio manager
      '/api': {
        target: 'http://localhost:8081',
        changeOrigin: true,
        secure: false,
      },
      // Keycloak auth proxy
      '/realms': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
      },
    },
  },
})
