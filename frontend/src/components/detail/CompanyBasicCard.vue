<template>
  <n-card class="section-card">
    <template #header>
      <div class="sec-hd">
        <n-icon :component="IdCardOutline" size="16" color="#4f46e5" />
        <span>基本情報</span>
      </div>
    </template>
    <n-descriptions
      label-placement="left"
      :column="2"
      bordered
      :label-style="labelStyle"
      :content-style="contentStyle"
    >
      <n-descriptions-item v-if="kindLabel" label="法人種別">{{ kindLabel }}</n-descriptions-item>
      <n-descriptions-item v-if="company.location" label="所在地" :span="2">{{ company.location }}</n-descriptions-item>
      <n-descriptions-item v-if="company.postalCode" label="郵便番号">〒{{ company.postalCode }}</n-descriptions-item>
      <n-descriptions-item v-if="company.capitalStock != null" label="資本金">
        <strong class="num-strong">{{ formatCapital(company.capitalStock) }}</strong>
      </n-descriptions-item>
      <n-descriptions-item v-if="company.employeeNumber != null" label="従業員数">
        <strong class="num-strong">{{ company.employeeNumber.toLocaleString() }}</strong> 名
      </n-descriptions-item>
      <n-descriptions-item
        v-if="company.companySizeMale != null || company.companySizeFemale != null"
        label="従業員（男女別）"
      >
        <span v-if="company.companySizeMale != null">男性 <strong class="num-strong">{{ company.companySizeMale.toLocaleString() }}</strong> 名</span>
        <span v-if="company.companySizeMale != null && company.companySizeFemale != null" class="divider"> / </span>
        <span v-if="company.companySizeFemale != null">女性 <strong class="num-strong">{{ company.companySizeFemale.toLocaleString() }}</strong> 名</span>
      </n-descriptions-item>
      <n-descriptions-item v-if="company.representativeName" label="代表者">
        {{ company.representativeName }}
      </n-descriptions-item>
      <n-descriptions-item v-if="company.dateOfEstablishment" label="設立年月日">
        {{ formatJpDate(company.dateOfEstablishment) }}
      </n-descriptions-item>
      <n-descriptions-item v-if="company.foundingYear" label="創業年">{{ company.foundingYear }} 年</n-descriptions-item>
      <n-descriptions-item v-if="company.nameEn" label="英語名" :span="2">{{ company.nameEn }}</n-descriptions-item>
      <!-- 閉鎖情報（閉鎖している場合のみ） -->
      <n-descriptions-item v-if="company.closeDate" label="閉鎖日">
        {{ formatJpDate(company.closeDate) }}
      </n-descriptions-item>
      <n-descriptions-item v-if="company.closeCause" label="閉鎖事由">{{ closeCauseLabel }}</n-descriptions-item>
      <n-descriptions-item v-if="company.gbizinfoUpdateDate" label="gBizINFO更新日">
        {{ formatJpDate(company.gbizinfoUpdateDate) }}
      </n-descriptions-item>
      <!-- 全省庁統一資格等級: 物品の製造、物品の販売、役務の提供等、物品の買受け の順 -->
      <n-descriptions-item v-if="qualGrades.length" label="全省庁統一資格" :span="2">
        <div class="qual-grades">
          <div v-for="g in qualGrades" :key="g.label" class="qual-item">
            <span class="qual-label">{{ g.label }}</span>
            <span :class="['qual-grade', g.grade ? 'qual-grade--' + g.grade.toLowerCase() : 'qual-grade--none']">
              {{ g.grade || '—' }}
            </span>
          </div>
        </div>
      </n-descriptions-item>

      <!-- 業種: industryMaster で変換して表示 -->
      <n-descriptions-item v-if="industryItems.length" label="業種" :span="2">
        <div class="industry-list">
          <div v-for="(item, i) in industryItems" :key="i" class="industry-entry">
            <!-- 大分類のみの場合 -->
            <template v-if="item.large && !item.medium">
              <span class="cls-badge cls-large">大分類</span>
              <span class="cls-name">{{ item.large }}</span>
            </template>
            <!-- 中分類あり（大分類も表示） -->
            <template v-else-if="item.medium">
              <span v-if="item.large" class="cls-large-prefix">{{ item.large }}</span>
              <span v-if="item.large" class="cls-arrow"> › </span>
              <span class="cls-badge cls-medium">中分類</span>
              <span class="cls-name">{{ item.medium }}</span>
              <span v-if="item.smallCode" class="cls-code">（{{ item.smallCode }}）</span>
              <span v-if="item.detailCode" class="cls-code">（{{ item.detailCode }}）</span>
            </template>
            <!-- マッチなし -->
            <template v-else>
              <span class="cls-name cls-unknown">{{ item.raw }}</span>
            </template>
          </div>
        </div>
      </n-descriptions-item>

      <n-descriptions-item v-if="company.companyUrl" label="企業HP" :span="2">
        <a :href="company.companyUrl" target="_blank" rel="noopener noreferrer" class="link">
          {{ company.companyUrl }}
        </a>
      </n-descriptions-item>
      <n-descriptions-item v-if="company.businessSummary" label="事業概要" :span="2">
        <p class="summary-text">{{ company.businessSummary }}</p>
      </n-descriptions-item>
    </n-descriptions>
  </n-card>
</template>

<script setup>
import { computed } from 'vue'
import { NCard, NDescriptions, NDescriptionsItem, NIcon } from 'naive-ui'
import { IdCardOutline } from '@vicons/ionicons5'
import { formatCapital, formatJpDate } from '@/utils/format'
import { parseIndustryField, INDUSTRY_LARGE } from '@/constants/industryMaster'

const props = defineProps({
  company: { type: Object, required: true },
})

const labelStyle = {
  color: '#64748b',
  fontSize: '0.8rem',
  fontWeight: '600',
  whiteSpace: 'nowrap',
  minWidth: '100px',
}
const contentStyle = { fontSize: '0.875rem' }

// ── 法人種別コード（Kihonjoho.md の組織種別コード準拠） ──────────────
const KIND_LABELS = {
  '101': '国の機関',
  '201': '地方公共団体',
  '301': '株式会社',
  '302': '有限会社',
  '303': '合名会社',
  '304': '合資会社',
  '305': '合同会社',
  '399': 'その他の設立登記法人',
  '401': '外国会社等',
  '499': 'その他',
}

const kindLabel = computed(() => {
  const k = props.company.kind
  if (!k) return null
  return KIND_LABELS[k] ?? k
})

// ── 閉鎖事由コード ────────────────────────────────────────────────────
const CLOSE_CAUSE_LABELS = {
  '01': '清算の結了等',
  '11': '合併による解散等',
  '21': '登記官による閉鎖',
  '31': 'その他の清算の結了等',
}
const closeCauseLabel = computed(() => {
  const c = props.company.closeCause
  if (!c) return null
  return CLOSE_CAUSE_LABELS[c] ?? c
})

// ── 全省庁統一資格等級のパース ─────────────────────────────────────────
// 格納形式: "、、B、" や "、A、A、A" のように全角コンマ区切り4フィールド
// 順序: 物品の製造、物品の販売、役務の提供等、物品の買受け
const QUAL_LABELS = ['物品の製造', '物品の販売', '役務の提供等', '物品の買受け']

const qualGrades = computed(() => {
  const raw = props.company.qualificationGrade
  if (!raw) return []
  const parts = raw.split('、')
  const result = QUAL_LABELS.map((label, i) => ({
    label,
    grade: (parts[i] ?? '').trim() || '',
  }))
  // 全て空なら表示しない
  if (result.every(g => !g.grade)) return []
  return result
})

// ── 業種: industryMaster.js で変換 ─────────────────────────────────
// gBizINFO 格納形式: '["H"]' / '["H01"]' / '["42"]' など
const industryItems = computed(() => parseIndustryField(props.company.industry))
</script>

<style scoped>
.section-card { border-radius: 10px; }

.sec-hd { display: flex; align-items: center; gap: 0.5rem; }
.sec-hd span { font-size: 0.9rem; font-weight: 600; color: #0f172a; }

.num-strong {
  font-family: 'Fira Code', monospace;
  color: #1e3a8a;
}

.sub-text { color: #94a3b8; font-size: 0.85em; }
.divider { color: #cbd5e1; margin: 0 0.2rem; }

/* 業種表示 */
.industry-list {
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
}

.industry-entry {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 0.3rem;
}

.cls-badge {
  font-size: 0.65rem;
  font-weight: 700;
  color: #fff;
  padding: 0.1rem 0.4rem;
  border-radius: 3px;
  white-space: nowrap;
  letter-spacing: 0.02em;
  flex-shrink: 0;
}

.cls-large { background: #4338ca; }
.cls-medium { background: #0284c7; }

.cls-large-prefix {
  font-size: 0.78rem;
  color: #64748b;
  white-space: nowrap;
}

.cls-arrow {
  color: #94a3b8;
  font-size: 0.85rem;
}

.cls-name {
  font-size: 0.875rem;
  color: #1e293b;
  font-weight: 500;
}

.cls-unknown {
  color: #64748b;
  font-weight: 400;
}

.cls-code {
  font-size: 0.75rem;
  color: #94a3b8;
}

.link {
  color: #4f46e5;
  font-size: 0.85rem;
  word-break: break-all;
}
.link:hover { text-decoration: underline; }

/* 全省庁統一資格等級 */
.qual-grades {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.qual-item {
  display: flex;
  align-items: center;
  gap: 0.35rem;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  padding: 0.3rem 0.6rem;
}

.qual-label {
  font-size: 0.75rem;
  color: #64748b;
  font-weight: 500;
}

.qual-grade {
  font-size: 0.82rem;
  font-weight: 700;
  font-family: 'Fira Code', monospace;
  min-width: 1.4rem;
  text-align: center;
}

.qual-grade--a { color: #15803d; }   /* A: 緑 */
.qual-grade--b { color: #2563eb; }   /* B: 青 */
.qual-grade--c { color: #d97706; }   /* C: オレンジ */
.qual-grade--d { color: #dc2626; }   /* D: 赤 */
.qual-grade--none { color: #cbd5e1; }

.summary-text {
  margin: 0;
  line-height: 1.75;
  font-size: 0.875rem;
  color: #334155;
  white-space: pre-wrap;
}
</style>
