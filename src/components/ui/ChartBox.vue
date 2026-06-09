<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref, shallowRef, watch } from 'vue'
import * as echarts from 'echarts'
import { useThemeStore } from '@/stores/theme'

const props = withDefaults(defineProps<{
  option: echarts.EChartsCoreOption
  height?: string
}>(), {
  height: '280px'
})

const theme = useThemeStore()
const host = ref<HTMLElement | null>(null)
const chart = shallowRef<echarts.ECharts | null>(null)
let observer: ResizeObserver | null = null
let resizeRaf = 0

// canvas 不认识 CSS 变量，渲染前把 var(--xxx) 解析成真实颜色值
const resolveVars = (input: unknown): unknown => {
  if (typeof input === 'string') {
    if (!input.includes('var(')) return input
    const styles = host.value ? getComputedStyle(host.value) : null
    return input.replace(/var\(\s*(--[\w-]+)\s*(?:,\s*([^)]+))?\)/g, (_, name, fallback) => {
      const value = styles?.getPropertyValue(name).trim()
      return value || (fallback ? fallback.trim() : '')
    })
  }
  if (Array.isArray(input)) return input.map(resolveVars)
  if (input && typeof input === 'object') {
    const out: Record<string, unknown> = {}
    for (const [key, value] of Object.entries(input)) out[key] = resolveVars(value)
    return out
  }
  return input
}

const render = () => {
  if (!chart.value) return
  chart.value.setOption(resolveVars(props.option) as echarts.EChartsCoreOption, true)
}

const init = () => {
  if (!host.value) return
  chart.value?.dispose()
  chart.value = echarts.init(host.value, theme.isDark ? 'dark' : undefined, { renderer: 'canvas' })
  // 让背景透明，跟随卡片底色
  chart.value.setOption({ backgroundColor: 'transparent' })
  render()
}

onMounted(() => {
  init()
  // rAF 节流：多次 resize 通知合并为每帧一次，避免容器宽度连续变化时反复重排
  observer = new ResizeObserver(() => {
    if (resizeRaf) return
    resizeRaf = requestAnimationFrame(() => {
      resizeRaf = 0
      chart.value?.resize()
    })
  })
  if (host.value) observer.observe(host.value)
})

watch(() => props.option, render, { deep: true })
watch(() => theme.mode, () => init())

onBeforeUnmount(() => {
  if (resizeRaf) cancelAnimationFrame(resizeRaf)
  observer?.disconnect()
  chart.value?.dispose()
})
</script>

<template>
  <div ref="host" class="chart-box" :style="{ height }"></div>
</template>

<style scoped>
.chart-box {
  width: 100%;
}
</style>
