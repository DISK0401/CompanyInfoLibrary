<template>
  <!-- テキスト型 -->
  <template v-if="fieldType === FIELD_TYPE.TEXT">
    <n-input
      :value="modelValue"
      size="small"
      clearable
      style="width: 100%;"
      @update:value="v => emit('update:modelValue', v)"
    />
  </template>

  <!-- 数値型：RANGE以外 -->
  <template v-else-if="fieldType === FIELD_TYPE.NUMBER && matchType !== 'RANGE'">
    <n-input-number
      :value="modelValue != null ? Number(modelValue) : null"
      size="small"
      :show-button="false"
      style="width: 100%;"
      @update:value="v => emit('update:modelValue', v != null ? String(v) : null)"
    />
  </template>

  <!-- 数値型：RANGE -->
  <template v-else-if="fieldType === FIELD_TYPE.NUMBER && matchType === 'RANGE'">
    <div class="range-inputs">
      <n-input-number
        :value="modelValue != null ? Number(modelValue) : null"
        size="small"
        :show-button="false"
        placeholder="以上"
        style="flex: 1;"
        @update:value="v => emit('update:modelValue', v != null ? String(v) : null)"
      />
      <span class="range-sep">〜</span>
      <n-input-number
        :value="modelValueTo != null ? Number(modelValueTo) : null"
        size="small"
        :show-button="false"
        placeholder="以下"
        style="flex: 1;"
        @update:value="v => emit('update:modelValueTo', v != null ? String(v) : null)"
      />
    </div>
  </template>

  <!-- 日付型：RANGE以外 -->
  <template v-else-if="fieldType === FIELD_TYPE.DATE && matchType !== 'RANGE'">
    <n-date-picker
      :value="modelValue ? new Date(modelValue).getTime() : null"
      type="date"
      size="small"
      clearable
      value-format="yyyy-MM-dd"
      style="width: 100%;"
      @update:value="v => emit('update:modelValue', v)"
    />
  </template>

  <!-- 日付型：RANGE -->
  <template v-else-if="fieldType === FIELD_TYPE.DATE && matchType === 'RANGE'">
    <n-date-picker
      :value="modelValue && modelValueTo ? [new Date(modelValue).getTime(), new Date(modelValueTo).getTime()] : null"
      type="daterange"
      size="small"
      clearable
      value-format="yyyy-MM-dd"
      style="width: 100%;"
      @update:value="onDateRangeUpdate"
    />
  </template>
</template>

<script setup>
import { NInput, NInputNumber, NDatePicker } from 'naive-ui'
import { FIELD_TYPE } from '@/constants/companyFields'

const props = defineProps({
  fieldType: { type: String, required: true },
  matchType: { type: String, required: true },
  modelValue: { type: [String, Number], default: null },
  modelValueTo: { type: [String, Number], default: null },
})

const emit = defineEmits(['update:modelValue', 'update:modelValueTo'])

function onDateRangeUpdate(v) {
  if (!v) {
    emit('update:modelValue', null)
    emit('update:modelValueTo', null)
  } else {
    emit('update:modelValue', v[0])
    emit('update:modelValueTo', v[1])
  }
}
</script>

<style scoped>
.range-inputs {
  display: flex;
  align-items: center;
  gap: 0.4rem;
  width: 100%;
}
.range-sep {
  color: #475569;
  white-space: nowrap;
  font-size: 0.85rem;
  flex-shrink: 0;
}
</style>
