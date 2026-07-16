import { defineConfig } from "vite"
import vue from "@vitejs/plugin-vue"
export default defineConfig({
  plugins: [vue()],
  server: { port: 3000, proxy: { "/api": { target: "http://localhost:8080", changeOrigin: true } } },
  build: {
    rollupOptions: {
      output: {
        manualChunks: {
          vendor: ["vue", "vue-router", "pinia"],
          ui: ["element-plus", "@element-plus/icons-vue"],
          chart: ["echarts", "vue-echarts"],
        },
      },
    },
    chunkSizeWarningLimit: 500,
  },
})