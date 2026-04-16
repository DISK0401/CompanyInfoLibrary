<template>
  <n-popover trigger="click" placement="bottom-end" :width="260">
    <template #trigger>
      <n-button size="small" secondary>
        <template #icon><n-icon :component="GridOutline" /></template>
        列設定
      </n-button>
    </template>

    <div class="col-panel">
      <div class="panel-hd">
        <span>表示列の設定</span>
        <n-text depth="3" style="font-size:0.72rem;">{{ modelValue.length }} 列表示中</n-text>
      </div>
      <n-divider style="margin: 0.5rem 0;" />
      <div class="col-list">
        <div v-for="field in COMPANY_FIELDS" :key="field.key" class="col-item">
          <n-checkbox
            :checked="modelValue.includes(field.key)"
            :label="field.label"
            size="small"
            @update:checked="onToggle(field.key, $event)"
          />
        </div>
      </div>
    </div>
  </n-popover>
</template>

<script setup>
import { NPopover, NButton, NIcon, NCheckbox, NDivider, NText } from 'naive-ui'
import { GridOutline } from '@vicons/ionicons5'
import { COMPANY_FIELDS } from '@/constants/companyFields'

const props = defineProps({ modelValue: { type: Array, required: true } })
const emit = defineEmits(['update:modelValue'])

function onToggle(key, checked) {
  const current = [...props.modelValue]
  if (checked) {
    if (!current.includes(key)) current.push(key)
  } else {
    if (current.length <= 1) return
    current.splice(current.indexOf(key), 1)
  }
  emit('update:modelValue', current)
}
</script>

<style scoped>
.col-panel { padding: 0.25rem; min-width: 220px; }
.panel-hd { display: flex; justify-content: space-between; align-items: center; font-size: 0.82rem; font-weight: 600; color: #0f172a; }
.col-list { display: flex; flex-direction: column; gap: 0.25rem; max-height: 320px; overflow-y: auto; }
.col-item { padding: 0.2rem 0; }
</style>
