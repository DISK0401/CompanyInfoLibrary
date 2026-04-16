<template>
  <n-card class="query-card" :content-style="{ padding: '0' }">
    <template #header>
      <div style="display:flex; align-items:center; gap:0.5rem;">
        <n-icon :component="FunnelOutline" size="16" :color="conditions.length ? '#4f46e5' : '#94a3b8'" />
        <span style="font-size:0.875rem; font-weight:600;">検索条件</span>
        <n-badge v-if="conditions.length" :value="conditions.length" type="info" :max="99" processing />
      </div>
    </template>
    <template #header-extra>
      <n-button text size="tiny" @click="collapsed = !collapsed">
        <template #icon>
          <n-icon :component="collapsed ? ChevronDownOutline : ChevronUpOutline" />
        </template>
      </n-button>
    </template>

    <n-collapse-transition :show="!collapsed">
      <div class="card-body">
        <!-- AND/OR -->
        <div class="logic-row">
          <span class="logic-label">条件の組み合わせ</span>
          <n-radio-group v-model:value="logic" size="small">
            <n-radio-button value="AND">AND（すべて一致）</n-radio-button>
            <n-radio-button value="OR">OR（いずれか一致）</n-radio-button>
          </n-radio-group>
        </div>

        <!-- 条件なし -->
        <n-empty
          v-if="conditions.length === 0"
          description="条件なしで検索すると全件が対象になります"
          size="small"
          style="padding: 1.25rem 0;"
        />

        <!-- 条件リスト -->
        <div v-else class="conditions-wrap">
          <template v-for="(condition, i) in conditions" :key="condition.id">
            <div v-if="i > 0" class="logic-connector">
              <n-tag size="tiny" :bordered="false" style="background:#f1f5f9; color:#64748b; font-size:0.65rem; font-weight:700; letter-spacing:0.05em;">
                {{ logic }}
              </n-tag>
            </div>
            <QueryConditionRow
              :condition="condition"
              :match-type-options="matchTypeOptions"
              @update-field="updateConditionField"
              @update-condition="updateCondition"
              @remove="removeCondition"
            />
          </template>
        </div>

        <!-- アクション -->
        <div class="actions">
          <n-button size="small" dashed @click="addCondition">
            <template #icon><n-icon :component="AddOutline" /></template>
            条件を追加
          </n-button>
          <div style="display:flex; gap:0.4rem;">
            <n-button size="small" quaternary @click="onReset">リセット</n-button>
            <n-button size="small" type="primary" @click="onSearch">
              <template #icon><n-icon :component="SearchOutline" /></template>
              検索
            </n-button>
          </div>
        </div>
      </div>
    </n-collapse-transition>
  </n-card>
</template>

<script setup>
import { ref } from 'vue'
import { NCard, NIcon, NBadge, NButton, NRadioGroup, NRadioButton, NEmpty, NCollapseTransition, NTag } from 'naive-ui'
import { useMessage } from 'naive-ui'
import { FunnelOutline, ChevronDownOutline, ChevronUpOutline, AddOutline, SearchOutline } from '@vicons/ionicons5'
import QueryConditionRow from './QueryConditionRow.vue'
import { useQueryBuilder } from '@/composables/useQueryBuilder'

const emit = defineEmits(['search', 'reset'])
const collapsed = ref(false)
const message = useMessage()
const { logic, conditions, addCondition, removeCondition, updateConditionField, updateCondition, resetConditions, buildSearchBody, matchTypeOptions } = useQueryBuilder()

function onSearch() {
  try {
    emit('search', buildSearchBody(null, null, 0, 20))
  } catch (e) {
    message.warning(e.message)
  }
}
function onReset() { resetConditions(); emit('reset') }
</script>

<style scoped>
.query-card {
  border-radius: 10px;
  border-left: 3px solid #4f46e5 !important;
  background: rgba(255,255,255,0.95) !important;
}

.card-body { padding: 1rem 1.25rem; display: flex; flex-direction: column; gap: 0.875rem; border-top: 1px solid #f1f5f9; }
.logic-row { display: flex; align-items: center; gap: 0.75rem; flex-wrap: wrap; }
.logic-label { font-size: 0.78rem; font-weight: 500; color: #94a3b8; white-space: nowrap; }
.conditions-wrap { display: flex; flex-direction: column; gap: 0; }
.logic-connector { padding: 0.25rem 0.5rem; }
.actions { display: flex; justify-content: space-between; align-items: center; padding-top: 0.5rem; border-top: 1px solid #f8fafc; flex-wrap: wrap; gap: 0.5rem; }
</style>
