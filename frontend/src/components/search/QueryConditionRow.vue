<template>
  <div class="cond-row">
    <n-select
      :value="condition.field"
      :options="fieldOptions"
      size="small"
      style="width: 155px; flex-shrink: 0;"
      @update:value="onFieldChange"
    />
    <n-select
      :value="condition.matchType"
      :options="matchTypeOptions(condition.fieldType)"
      size="small"
      style="width: 145px; flex-shrink: 0;"
      @update:value="onMatchTypeChange"
    />
    <div style="flex:1; min-width:140px;">
      <ConditionValueInput
        :field-type="condition.fieldType"
        :match-type="condition.matchType"
        :model-value="condition.value"
        :model-value-to="condition.valueTo"
        @update:model-value="v => emit('update-condition', condition.id, { value: v })"
        @update:model-value-to="v => emit('update-condition', condition.id, { valueTo: v })"
      />
    </div>
    <n-button circle quaternary size="small" @click="emit('remove', condition.id)">
      <template #icon><n-icon :component="CloseOutline" color="#ef4444" /></template>
    </n-button>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { NSelect, NButton, NIcon } from 'naive-ui'
import { CloseOutline } from '@vicons/ionicons5'
import ConditionValueInput from './ConditionValueInput.vue'
import { COMPANY_FIELDS } from '@/constants/companyFields'

const props = defineProps({ condition: Object, matchTypeOptions: Function })
const emit = defineEmits(['update-field', 'update-condition', 'remove'])

const fieldOptions = COMPANY_FIELDS.map(f => ({ label: f.label, value: f.key }))

function onFieldChange(v) { emit('update-field', props.condition.id, v) }
function onMatchTypeChange(v) { emit('update-condition', props.condition.id, { matchType: v, value: null, valueTo: null }) }
</script>

<style scoped>
.cond-row { display: flex; align-items: center; gap: 0.4rem; padding: 0.5rem 0.6rem; background: #fafafa; border: 1px solid #e8ecf0; border-radius: 7px; margin-bottom: 0.35rem; transition: border-color 0.15s, box-shadow 0.15s; }
.cond-row:hover { border-color: #a5b4fc; box-shadow: 0 0 0 3px rgba(79,70,229,0.06); }
@media (max-width: 768px) { .cond-row { flex-wrap: wrap; } }
</style>
