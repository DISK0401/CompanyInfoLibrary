<template>
  <div class="page">
    <button class="btn-back" @click="router.back()">← 検索結果に戻る</button>

    <!-- ローディング -->
    <div v-if="loading" class="loading">読み込み中...</div>

    <!-- エラー -->
    <div v-else-if="error" class="alert-error">{{ error }}</div>

    <!-- 詳細 -->
    <template v-else-if="company">
      <!-- ヘッダー -->
      <div class="company-header">
        <h1 class="company-name">{{ company.name }}</h1>
        <span class="company-kana">{{ company.kana }}</span>
        <span class="corp-num">法人番号 {{ company.corporateNumber }}</span>
      </div>

      <div class="detail-grid">
        <!-- 基本情報 -->
        <section class="card">
          <h2 class="card-title">基本情報</h2>
          <dl class="info-list">
            <template v-if="company.location">
              <dt>所在地</dt><dd>{{ company.location }}</dd>
            </template>
            <template v-if="company.postalCode">
              <dt>郵便番号</dt><dd>〒{{ company.postalCode }}</dd>
            </template>
            <template v-if="company.capitalStock != null">
              <dt>資本金</dt><dd>{{ formatCapital(company.capitalStock) }}</dd>
            </template>
            <template v-if="company.employeeNumber != null">
              <dt>従業員数</dt><dd>{{ company.employeeNumber.toLocaleString() }} 名</dd>
            </template>
            <template v-if="company.representativeName">
              <dt>代表者</dt>
              <dd>
                {{ company.representativeName }}
                <span v-if="company.representativePosition" class="sub-text">（{{ company.representativePosition }}）</span>
              </dd>
            </template>
            <template v-if="company.dateOfEstablishment">
              <dt>設立年月日</dt><dd>{{ company.dateOfEstablishment }}</dd>
            </template>
            <template v-if="company.foundingYear">
              <dt>創業年</dt><dd>{{ company.foundingYear }} 年</dd>
            </template>
            <template v-if="company.companyUrl">
              <dt>企業HP</dt>
              <dd><a :href="company.companyUrl" target="_blank" rel="noopener noreferrer">{{ company.companyUrl }}</a></dd>
            </template>
            <template v-if="company.businessSummary">
              <dt>事業概要</dt><dd class="summary-text">{{ company.businessSummary }}</dd>
            </template>
          </dl>
        </section>

        <!-- 事業内容 -->
        <section v-if="company.businessItems?.length" class="card">
          <h2 class="card-title">事業内容</h2>
          <ul class="tag-list">
            <li v-for="item in company.businessItems" :key="item" class="tag">{{ item }}</li>
          </ul>
        </section>
      </div>

      <!-- 財務情報 -->
      <section v-if="company.finances?.length" class="card mt">
        <h2 class="card-title">財務情報</h2>
        <table class="finance-table">
          <thead>
            <tr>
              <th>期</th>
              <th class="num-col">売上高</th>
              <th class="num-col">当期純利益</th>
              <th class="num-col">総資産額</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="f in company.finances" :key="f.period">
              <td>{{ f.period ?? '—' }}</td>
              <td class="num-col">{{ formatAmount(f.netSales) }}</td>
              <td class="num-col" :class="{ negative: f.netIncomeLoss != null && f.netIncomeLoss < 0 }">
                {{ formatAmount(f.netIncomeLoss) }}
              </td>
              <td class="num-col">{{ formatAmount(f.totalAssets) }}</td>
            </tr>
          </tbody>
        </table>
      </section>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getCompany } from '@/api/companies'

const route = useRoute()
const router = useRouter()

const company = ref(null)
const loading = ref(true)
const error = ref(null)

onMounted(async () => {
  try {
    company.value = await getCompany(route.params.corporateNumber)
  } catch (e) {
    if (e.response?.status === 404) {
      error.value = `法人番号 ${route.params.corporateNumber} の企業情報が見つかりませんでした。`
    } else {
      error.value = e.response?.data?.error ?? 'エラーが発生しました。しばらく時間をおいてから再試行してください。'
    }
  } finally {
    loading.value = false
  }
})

function formatCapital(value) {
  if (value == null) return '—'
  if (value >= 100_000_000) {
    const oku = Math.floor(value / 100_000_000)
    const man = Math.floor((value % 100_000_000) / 10_000)
    return man > 0 ? `${oku}億${man.toLocaleString()}万円` : `${oku}億円`
  }
  if (value >= 10_000) return `${Math.floor(value / 10_000).toLocaleString()}万円`
  return `${value.toLocaleString()}円`
}

function formatAmount(value) {
  if (value == null) return '—'
  const abs = Math.abs(value)
  const sign = value < 0 ? '▲' : ''
  if (abs >= 100_000_000) return `${sign}${Math.floor(abs / 100_000_000).toLocaleString()}億円`
  if (abs >= 10_000) return `${sign}${Math.floor(abs / 10_000).toLocaleString()}万円`
  return `${sign}${abs.toLocaleString()}円`
}
</script>

<style scoped>
.page { max-width: 900px; margin: 0 auto; padding: 2rem 1rem; }
.btn-back { background: none; border: none; color: #3b82f6; font-size: 0.9rem; cursor: pointer; padding: 0; margin-bottom: 1.5rem; }
.btn-back:hover { text-decoration: underline; }

.loading { text-align: center; color: #718096; padding: 4rem; }
.alert-error { background: #fff5f5; border: 1px solid #feb2b2; color: #c53030; border-radius: 6px; padding: 0.75rem 1rem; font-size: 0.9rem; }

/* ヘッダー */
.company-header { margin-bottom: 1.5rem; }
.company-name { font-size: 1.8rem; font-weight: 700; color: #1a202c; margin: 0 0 0.25rem; }
.company-kana { display: block; font-size: 0.9rem; color: #718096; margin-bottom: 0.5rem; }
.corp-num { font-size: 0.8rem; color: #a0aec0; font-family: monospace; }

/* グリッド */
.detail-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(300px, 1fr)); gap: 1rem; margin-bottom: 1rem; }

/* カード */
.card { background: #fff; border: 1px solid #e2e8f0; border-radius: 8px; padding: 1.25rem; }
.card.mt { margin-top: 1rem; }
.card-title { font-size: 1rem; font-weight: 700; color: #2d3748; margin: 0 0 1rem; padding-bottom: 0.5rem; border-bottom: 2px solid #e2e8f0; }

/* 基本情報リスト */
.info-list { display: grid; grid-template-columns: 7rem 1fr; gap: 0.4rem 0.75rem; margin: 0; font-size: 0.875rem; }
.info-list dt { color: #718096; font-weight: 600; align-self: start; padding-top: 0.1rem; }
.info-list dd { margin: 0; color: #2d3748; word-break: break-all; }
.info-list a { color: #3b82f6; }
.info-list a:hover { text-decoration: underline; }
.sub-text { color: #718096; }
.summary-text { line-height: 1.6; }

/* タグリスト */
.tag-list { list-style: none; padding: 0; margin: 0; display: flex; flex-wrap: wrap; gap: 0.5rem; }
.tag { background: #ebf8ff; color: #2b6cb0; padding: 0.25rem 0.75rem; border-radius: 999px; font-size: 0.82rem; font-weight: 500; }

/* 財務テーブル */
.finance-table { width: 100%; border-collapse: collapse; font-size: 0.9rem; }
.finance-table th { background: #edf2f7; padding: 0.6rem 0.75rem; font-weight: 600; color: #4a5568; border-bottom: 2px solid #cbd5e0; white-space: nowrap; }
.finance-table td { padding: 0.6rem 0.75rem; border-bottom: 1px solid #e2e8f0; }
.num-col { text-align: right; white-space: nowrap; }
.negative { color: #e53e3e; }
</style>
