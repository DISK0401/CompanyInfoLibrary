<template>
  <n-card class="section-card">
    <template #header>
      <div class="sec-hd">
        <n-icon :component="TrendingUpOutline" size="16" color="#4f46e5" />
        <span>財務情報</span>
        <n-text depth="3" style="font-size:0.75rem;">{{ finances.length }} 期分</n-text>
      </div>
    </template>

    <!-- 財務テーブル（行=指標, 列=期） -->
    <div class="finance-table-wrap" style="max-height:380px; overflow:auto;">
      <table class="finance-table">
        <thead>
          <tr>
            <th class="metric-col">指標</th>
            <th
              v-for="(label, i) in periodLabels"
              :key="i"
              class="period-col"
              :class="{ 'current-period': i === 0 }"
            >
              <div class="period-label">{{ label }}</div>
            </th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="metric in activeMetrics" :key="metric.key" class="metric-row">
            <td class="metric-name">{{ metric.label }}</td>
            <td
              v-for="(f, i) in sortedFinances"
              :key="i"
              class="metric-value"
              :class="{ 'current-period': i === 0, 'negative-val': metric.negative && f[metric.key] < 0 }"
            >
              <template v-if="f[metric.key] != null">
                <span class="num">{{ metric.format(f[metric.key]) }}</span>
              </template>
              <span v-else class="empty-dash">—</span>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- fiscalYearCoverPage は全期共通のため補足として1度だけ表示 -->
    <p v-if="fiscalYearNote" class="fiscal-note">
      <span class="fiscal-note-label">決算期</span>{{ fiscalYearNote }}
    </p>
  </n-card>

  <!-- 大株主情報（直近期より） -->
  <n-card v-if="latestShareholders.length" class="section-card">
    <template #header>
      <div class="sec-hd">
        <n-icon :component="PeopleOutline" size="16" color="#4f46e5" />
        <span>大株主情報</span>
        <n-text depth="3" style="font-size:0.75rem;">直近期（{{ sortedFinances[0]?.period ?? '当期' }}）</n-text>
      </div>
    </template>
    <n-data-table
      :columns="shareholderColumns"
      :data="latestShareholders"
      :bordered="true"
      size="small"
      striped
    />
  </n-card>
</template>

<script setup>
import { computed, h } from 'vue'
import { NCard, NDataTable, NIcon, NText } from 'naive-ui'
import { TrendingUpOutline, PeopleOutline } from '@vicons/ionicons5'
import { formatAmount } from '@/utils/format'

const props = defineProps({
  finances: { type: Array, default: () => [] },
})

// finances はバックエンドから文字列ソート降順で来るが、
// 多桁の期番号（「第50期」等）で逆転することがあるため、
// 「数値の大きい方が当期」というユーザー要件に合わせてフロントで逆順に並び替える
const sortedFinances = computed(() => [...props.finances].reverse())

// 期ラベル: sortedFinances[0] = 当期（最新）
const periodLabels = computed(() =>
  sortedFinances.value.map((_, i) => i === 0 ? '当期' : `${i}期前`)
)

// 表示する指標定義
const metricDefs = [
  { label: '売上高',        key: 'netSales',             negative: false, format: formatAmount },
  { label: '営業総収入',    key: 'grossOperatingRevenue', negative: false, format: formatAmount },
  { label: '営業収益',      key: 'operatingRevenue1',     negative: false, format: formatAmount },
  { label: '経常収益',      key: 'ordinaryIncome',        negative: false, format: formatAmount },
  { label: '経常利益',      key: 'ordinaryIncomeLoss',    negative: true,  format: formatAmount },
  { label: '当期純利益',    key: 'netIncomeLoss',         negative: true,  format: formatAmount },
  { label: '正味保険料',    key: 'netPremiumsWritten',    negative: false, format: formatAmount },
  { label: '総資産',        key: 'totalAssets',           negative: false, format: formatAmount },
  { label: '純資産',        key: 'netAssets',             negative: false, format: formatAmount },
  { label: '資本金（財務）',key: 'capitalStock',          negative: false, format: formatAmount },
  { label: '従業員数（財務）',key: 'numberOfEmployees',   negative: false, format: v => `${v.toLocaleString()} 名` },
]

// いずれかの期に値がある指標だけ表示
const activeMetrics = computed(() =>
  metricDefs.filter(m => sortedFinances.value.some(f => f[m.key] != null))
)

// 決算期補足表示（全期共通なので1件だけ抽出）
const fiscalYearNote = computed(() =>
  sortedFinances.value.find(f => f.fiscalYearCoverPage)?.fiscalYearCoverPage ?? null
)

// 直近期（sortedFinances[0]）の大株主JSONをパース
const latestShareholders = computed(() => {
  if (!sortedFinances.value.length) return []
  const raw = sortedFinances.value[0].majorShareholders
  if (!raw) return []
  try {
    const parsed = JSON.parse(raw)
    if (!Array.isArray(parsed)) return []
    return parsed
  } catch {
    return []
  }
})

// 大株主テーブルの列定義
// gBizINFO の majorShareholders JSON の代表的なキー名パターンを網羅する
const shareholderColumns = computed(() => {
  if (!latestShareholders.value.length) return []
  const sample = latestShareholders.value[0]

  // 株主名キーの候補（gBizINFO では 'name' が一般的）
  const NAME_KEYS = ['name', 'shareholderName', 'shareholder_name', '株主名', '名称', 'holder_name']
  const nameKey = NAME_KEYS.find(k => k in sample)

  // 持株比率キーの候補（小数 0.xx で格納 → ×100で%表示）
  const RATIO_KEYS = ['shareholdingRatio', 'shareholding_ratio', 'ratio', 'share_ratio',
                      'holding_ratio', 'ownershipRatio', 'ownership_ratio', '比率', 'share']
  const ratioKey = RATIO_KEYS.find(k => k in sample)

  // 順位キーの候補
  const RANK_KEYS = ['rank', '順位', 'order']
  const rankKey = RANK_KEYS.find(k => k in sample)

  const cols = []

  // 既知キーの日本語ラベルマップ（全列に適用）
  const LABEL_MAP = {
    name: '株主名', shareholderName: '株主名', shareholder_name: '株主名',
    shareholdingRatio: '持株比率', shareholding_ratio: '持株比率',
    ratio: '持株比率', share_ratio: '持株比率', ownershipRatio: '持株比率',
    rank: '順位', order: '順位',
    address: '住所', prefecture: '都道府県',
    shares: '株数', stock_count: '株数',
    relationship: '関係',
  }

  // 処理済みキーを追跡
  const handled = new Set()

  // 1. 順位
  if (rankKey) {
    cols.push({ title: '順位', key: rankKey, width: 60, align: 'center' })
    handled.add(rankKey)
  }

  // 2. 株主名
  if (nameKey) {
    cols.push({ title: '株主名', key: nameKey, ellipsis: { tooltip: true }, minWidth: 180 })
    handled.add(nameKey)
  }

  // 3. 持株比率（×100して% 表示）
  if (ratioKey) {
    cols.push({
      title: '持株比率',
      key: ratioKey,
      width: 110,
      align: 'right',
      render: r => {
        const v = r[ratioKey]
        if (v == null) return h('span', { style: { color: '#cbd5e1' } }, '—')
        const pct = v <= 1 ? v * 100 : v
        return h('span', { style: { fontFamily: 'monospace', fontSize: '0.82rem' } },
          `${pct.toFixed(2)}%`)
      },
    })
    handled.add(ratioKey)
  }

  // 4. 上記で未処理の残りキーを全て追加（順序保持）
  Object.keys(sample).forEach(k => {
    if (handled.has(k)) return
    cols.push({
      title: LABEL_MAP[k] ?? k,
      key: k,
      ellipsis: { tooltip: true },
      minWidth: 100,
    })
  })

  return cols
})
</script>

<style scoped>
.section-card { border-radius: 10px; }

.sec-hd { display: flex; align-items: center; gap: 0.5rem; }
.sec-hd span { font-size: 0.9rem; font-weight: 600; color: #0f172a; }

/* 決算期補足 */
.fiscal-note {
  margin: 0.5rem 0 0;
  padding: 0.45rem 0.875rem;
  font-size: 0.78rem;
  color: #64748b;
  background: #f8fafc;
  border-top: 1px solid #f1f5f9;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.fiscal-note-label {
  font-size: 0.7rem;
  font-weight: 600;
  color: #94a3b8;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  white-space: nowrap;
}

/* 財務テーブル */
.finance-table-wrap {
  overflow-x: auto;
}

.finance-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 0.855rem;
  min-width: 420px;
}

.finance-table thead tr {
  background: #374151;
}

.finance-table th {
  padding: 0.55rem 0.875rem;
  text-align: left;
  font-size: 0.72rem;
  font-weight: 600;
  letter-spacing: 0.07em;
  text-transform: uppercase;
  color: #e5e7eb;
  border-right: 1px solid #4b5563;
  white-space: nowrap;
}

.finance-table th:last-child {
  border-right: none;
}

.finance-table th.current-period {
  background: #4b5563;
  color: #f9fafb;
}

.period-label {
  font-size: 0.78rem;
  font-weight: 700;
  color: #f3f4f6;
}

.period-year {
  font-size: 0.65rem;
  color: #d1d5db;
  margin-top: 2px;
  font-family: 'Fira Code', monospace;
  letter-spacing: 0;
}

.period-cover {
  font-size: 0.58rem;
  color: #9ca3af;
  margin-top: 1px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 110px;
  font-weight: 400;
  text-transform: none;
  letter-spacing: 0;
}

.metric-col {
  width: 130px;
  min-width: 110px;
  text-align: left;
}

.period-col {
  min-width: 105px;
  text-align: right;
}

/* 行 */
.metric-row {
  border-bottom: 1px solid #f1f5f9;
  transition: background 0.1s;
}

.metric-row:hover {
  background: #eff3ff;
}

.metric-row:hover .metric-value:first-child {
  box-shadow: inset 3px 0 0 #4f46e5;
}

.metric-name {
  padding: 0.4rem 0.75rem;
  color: #475569;
  font-size: 0.8rem;
  font-weight: 500;
  white-space: nowrap;
  background: #f8fafc;
  border-right: 1px solid #e8ecf0;
}

.metric-value {
  padding: 0.4rem 0.75rem;
  text-align: right;
  font-family: 'Fira Code', monospace;
  font-size: 0.8rem;
  font-feature-settings: "tnum";
  color: #1e293b;
  border-right: 1px solid #f1f5f9;
}

.metric-value:last-child {
  border-right: none;
}

.metric-value.current-period {
  background: #fafbff;
  font-weight: 500;
}

.metric-value.negative-val {
  color: #dc2626;
}

.empty-dash {
  color: #cbd5e1;
  font-family: sans-serif;
}
</style>
