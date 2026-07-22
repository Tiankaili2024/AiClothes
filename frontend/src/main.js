import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import App from './App.vue'
import router from './router'

const app = createApp(App)
app.use(createPinia())
app.use(router)
app.use(ElementPlus)

// 注册所有图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 全局错误处理，防止白屏
app.config.errorHandler = (err, instance, info) => {
  console.error('[Global Error]', err, info)
}
app.config.warnHandler = (msg, instance, trace) => {
  console.warn('[Global Warn]', msg)
}

window.addEventListener('error', (e) => {
  console.error('[Uncaught Error]', e.error || e.message)
  e.preventDefault()
})

window.addEventListener('unhandledrejection', (e) => {
  console.error('[Unhandled Promise]', e.reason)
  e.preventDefault()
})

app.mount('#app')
