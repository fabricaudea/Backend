import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import path from 'path'

export default defineConfig({
  plugins: [react()],
  base: '/',
  build: { outDir: 'dist' },
  server: { proxy: { '/api': 'http://localhost:8080' } },
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),              // para "@/..."
      '@components': path.resolve(__dirname, './src/components'), // para "@components/..."
      '@lib': path.resolve(__dirname, './src/lib')        // (opcional) si lo usas en imports
    }
  }
})