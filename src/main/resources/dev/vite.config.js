import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vitejs.dev/config/
export default defineConfig({
  server: {
    proxy: {
      '/demo/api': {
        target: 'http://localhost:8081',
        changeOrigin: true,
        secure: false,
      }
    }
  },
  build: {
    outDir: '../static'
  },
  plugins: [react()],
})
