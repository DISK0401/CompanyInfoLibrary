<template>
  <n-card v-if="hasData" class="section-card">
    <template #header>
      <div class="sec-hd">
        <n-icon :component="PeopleOutline" size="16" color="#4f46e5" />
        <span>職場情報</span>
      </div>
    </template>

    <n-descriptions
      label-placement="left"
      :column="2"
      bordered
      :label-style="labelStyle"
      :content-style="contentStyle"
    >
      <!-- 勤務基本情報 -->
      <n-descriptions-item v-if="w.averageAge != null" label="平均年齢">
        <span class="num-val">{{ w.averageAge }}</span> 歳
      </n-descriptions-item>
      <n-descriptions-item v-if="w.averageContinuousServiceYears != null" label="平均勤続年数">
        <span class="num-val">{{ w.averageContinuousServiceYears }}</span> 年
        <span v-if="w.averageContinuousServiceYearsType" class="sub-text">（{{ w.averageContinuousServiceYearsType }}）</span>
      </n-descriptions-item>
      <n-descriptions-item v-if="w.averageContinuousServiceYearsMale != null" label="平均勤続年数（男性）">
        <span class="num-val">{{ w.averageContinuousServiceYearsMale }}</span> 年
      </n-descriptions-item>
      <n-descriptions-item v-if="w.averageContinuousServiceYearsFemale != null" label="平均勤続年数（女性）">
        <span class="num-val">{{ w.averageContinuousServiceYearsFemale }}</span> 年
      </n-descriptions-item>
      <n-descriptions-item v-if="w.monthAverageOvertime != null" label="月平均残業時間">
        <span class="num-val">{{ w.monthAverageOvertime }}</span> 時間
      </n-descriptions-item>

      <!-- 女性活躍情報 -->
      <n-descriptions-item v-if="w.femaleWorkersProportion != null" label="女性労働者比率">
        <span class="num-val">{{ w.femaleWorkersProportion }}</span> %
        <span v-if="w.femaleWorkersProportionType" class="sub-text">（{{ w.femaleWorkersProportionType }}）</span>
      </n-descriptions-item>
      <n-descriptions-item
        v-if="w.femaleShareOfManager != null || w.genderTotalOfManager != null"
        label="女性管理職比率"
      >
        <template v-if="w.genderTotalOfManager">
          <span class="num-val">{{ w.femaleShareOfManager ?? 0 }}</span> 名 /
          <span class="num-val">{{ w.genderTotalOfManager }}</span> 名
          <span class="ratio-text">（{{ calcRatio(w.femaleShareOfManager, w.genderTotalOfManager) }}%）</span>
        </template>
        <template v-else>
          <span class="num-val">{{ w.femaleShareOfManager }}</span> 名
        </template>
      </n-descriptions-item>
      <n-descriptions-item
        v-if="w.femaleShareOfOfficers != null || w.genderTotalOfOfficers != null"
        label="女性役員比率"
      >
        <template v-if="w.genderTotalOfOfficers">
          <span class="num-val">{{ w.femaleShareOfOfficers ?? 0 }}</span> 名 /
          <span class="num-val">{{ w.genderTotalOfOfficers }}</span> 名
          <span class="ratio-text">（{{ calcRatio(w.femaleShareOfOfficers, w.genderTotalOfOfficers) }}%）</span>
        </template>
        <template v-else>
          <span class="num-val">{{ w.femaleShareOfOfficers }}</span> 名
        </template>
      </n-descriptions-item>

      <!-- 育児・仕事両立 -->
      <n-descriptions-item
        v-if="w.paternityLeaveAcquisitionNum != null || w.numberOfPaternityLeave != null"
        label="育休取得率（男性）"
      >
        <template v-if="w.numberOfPaternityLeave">
          <span class="num-val">{{ w.paternityLeaveAcquisitionNum ?? 0 }}</span> 名 /
          <span class="num-val">{{ w.numberOfPaternityLeave }}</span> 名
          <span class="ratio-text">（{{ calcRatio(w.paternityLeaveAcquisitionNum, w.numberOfPaternityLeave) }}%）</span>
        </template>
        <template v-else>
          <span class="num-val">{{ w.paternityLeaveAcquisitionNum }}</span> 名取得
        </template>
      </n-descriptions-item>
      <n-descriptions-item
        v-if="w.maternityLeaveAcquisitionNum != null || w.numberOfMaternityLeave != null"
        label="育休取得率（女性）"
      >
        <template v-if="w.numberOfMaternityLeave">
          <span class="num-val">{{ w.maternityLeaveAcquisitionNum ?? 0 }}</span> 名 /
          <span class="num-val">{{ w.numberOfMaternityLeave }}</span> 名
          <span class="ratio-text">（{{ calcRatio(w.maternityLeaveAcquisitionNum, w.numberOfMaternityLeave) }}%）</span>
        </template>
        <template v-else>
          <span class="num-val">{{ w.maternityLeaveAcquisitionNum }}</span> 名取得
        </template>
      </n-descriptions-item>
    </n-descriptions>
  </n-card>
</template>

<script setup>
import { computed } from 'vue'
import { NCard, NDescriptions, NDescriptionsItem, NIcon } from 'naive-ui'
import { PeopleOutline } from '@vicons/ionicons5'

const props = defineProps({
  workplace: { type: Object, default: null },
})

const w = computed(() => props.workplace ?? {})

const hasData = computed(() => {
  if (!props.workplace) return false
  return Object.values(props.workplace).some(v => v != null)
})

const labelStyle = {
  color: '#64748b',
  fontSize: '0.8rem',
  fontWeight: '600',
  whiteSpace: 'nowrap',
  minWidth: '140px',
}
const contentStyle = { fontSize: '0.875rem' }

function calcRatio(numerator, denominator) {
  if (!denominator || denominator === 0) return '—'
  return ((numerator ?? 0) / denominator * 100).toFixed(1)
}
</script>

<style scoped>
.section-card { border-radius: 10px; }
.sec-hd { display: flex; align-items: center; gap: 0.5rem; }
.sec-hd span { font-size: 0.9rem; font-weight: 600; color: #0f172a; }

.num-val {
  font-family: 'Fira Code', monospace;
  font-weight: 600;
  color: #1e3a8a;
}

.ratio-text {
  font-size: 0.8rem;
  color: #4f46e5;
  font-weight: 500;
  margin-left: 0.25rem;
}

.sub-text {
  font-size: 0.78rem;
  color: #94a3b8;
  margin-left: 0.25rem;
}
</style>
