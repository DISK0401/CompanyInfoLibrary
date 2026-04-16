<template>
  <div class="page">
    <!-- 戻るボタン -->
    <n-button text size="small" @click="router.back()" style="margin-bottom:1rem;">
      <template #icon><n-icon :component="ArrowBackOutline" /></template>
      企業一覧に戻る
    </n-button>

    <!-- ローディング -->
    <div v-if="loading" class="loading-wrap">
      <n-spin size="large" />
    </div>

    <!-- エラー -->
    <n-alert v-else-if="error" type="error" :closable="false">{{ error }}</n-alert>

    <!-- 本体 -->
    <template v-else-if="company">
      <!-- 会社ヘッダー -->
      <div class="company-hero">
        <div class="hero-main">
          <div class="hero-name-block">
            <h1 class="hero-name">{{ company.name }}</h1>
            <p v-if="company.kana" class="hero-kana">{{ company.kana }}</p>
            <p v-if="company.nameEn" class="hero-name-en">{{ company.nameEn }}</p>
          </div>
          <div class="hero-meta">
            <div class="meta-chip">
              <span class="meta-chip-label">法人番号</span>
              <code class="meta-chip-value">{{ company.corporateNumber }}</code>
            </div>
            <div v-if="company.status" class="meta-chip">
              <span class="meta-chip-label">ステータス</span>
              <n-tag :type="statusType(company.status)" size="small" round>{{ statusLabel(company.status) }}</n-tag>
            </div>
            <div v-if="company.location" class="meta-chip">
              <n-icon :component="LocationOutline" size="13" color="#64748b" />
              <span class="meta-loc">{{ company.location }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- セクション縦並び -->
      <div class="sections">
        <CompanyBasicCard :company="company" />
        <CompanyFinanceTable v-if="company.finances?.length" :finances="company.finances" />
        <CompanyWorkplaceSection :workplace="company.workplace" />

        <!-- 事業内容 -->
        <n-card v-if="company.businessItems?.length" class="section-card">
          <template #header>
            <div class="sec-hd">
              <n-icon :component="ListOutline" size="16" color="#4f46e5" />
              <span>事業内容</span>
              <n-text depth="3" style="font-size:0.75rem;">{{ company.businessItems.length }} 件</n-text>
            </div>
          </template>
          <div class="tag-wrap">
            <n-tag
              v-for="item in company.businessItems"
              :key="item"
              :bordered="false"
              size="small"
              style="background:#e0e7ff; color:#3730a3; font-size:0.82rem;"
            >
              {{ resolveBusinessItem(item) }}
            </n-tag>
          </div>
        </n-card>

        <CompanySubsidySection :corporate-number="corporateNumber" />
        <CompanyPatentSection :corporate-number="corporateNumber" />
        <CompanyProcurementSection :corporate-number="corporateNumber" />
        <CompanyCertificationSection :corporate-number="corporateNumber" />
        <CompanyCommendationSection :corporate-number="corporateNumber" />
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { NButton, NIcon, NSpin, NAlert, NCard, NTag, NText } from 'naive-ui'
import { ArrowBackOutline, LocationOutline, ListOutline } from '@vicons/ionicons5'
import { resolveBusinessItem } from '@/constants/businessItemMaster'
import { getCompany } from '@/api/companies'
import CompanyBasicCard from '@/components/detail/CompanyBasicCard.vue'
import CompanyFinanceTable from '@/components/detail/CompanyFinanceTable.vue'
import CompanyWorkplaceSection from '@/components/detail/CompanyWorkplaceSection.vue'
import CompanySubsidySection from '@/components/detail/CompanySubsidySection.vue'
import CompanyPatentSection from '@/components/detail/CompanyPatentSection.vue'
import CompanyProcurementSection from '@/components/detail/CompanyProcurementSection.vue'
import CompanyCertificationSection from '@/components/detail/CompanyCertificationSection.vue'
import CompanyCommendationSection from '@/components/detail/CompanyCommendationSection.vue'

const route = useRoute()
const router = useRouter()

const corporateNumber = route.params.corporateNumber
const company = ref(null)
const loading = ref(true)
const error = ref(null)

onMounted(async () => {
  try {
    company.value = await getCompany(corporateNumber)
  } catch (e) {
    if (e.response?.status === 404) {
      error.value = `法人番号 ${corporateNumber} の企業情報が見つかりませんでした。`
    } else {
      error.value = e.response?.data?.error ?? 'エラーが発生しました。'
    }
  } finally {
    loading.value = false
  }
})


function statusLabel(s) {
  return { '01': '存続', '11': '清算中', '21': '解散', '31': '取消', '41': '合併消滅', '51': '解散・清算中', '71': '廃業', '81': '廃業・清算中' }[s] ?? s
}

function statusType(s) {
  if (s === '01') return 'success'
  if (['11', '21', '51', '81'].includes(s)) return 'warning'
  return 'error'
}
</script>

<style scoped>
.page {
  max-width: 1100px;
  margin: 0 auto;
  padding: 1.25rem 1.25rem 2rem;
}

.loading-wrap {
  display: flex;
  justify-content: center;
  padding: 5rem;
}

/* 会社ヘッダー */
.company-hero {
  background: linear-gradient(135deg, #1e293b 0%, #312e81 100%);
  border-radius: 10px;
  padding: 1.1rem 1.5rem;
  margin-bottom: 1.25rem;
  border: 1px solid #3b5bdb;
  box-shadow: var(--shadow-md);
}

.hero-name {
  font-size: 1.4rem;
  font-weight: 700;
  color: #f1f5f9;
  margin: 0;
  letter-spacing: -0.01em;
  line-height: 1.3;
}

.hero-kana {
  font-size: 0.9rem;
  color: #94a3b8;
  margin: 0.25rem 0 0;
}

.hero-name-en {
  font-size: 0.82rem;
  color: #94a3b8;
  font-style: italic;
  margin: 0.15rem 0 0;
}

.hero-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 0.6rem;
  margin-top: 0.6rem;
}

.meta-chip {
  display: flex;
  align-items: center;
  gap: 0.4rem;
}

.meta-chip-label {
  font-size: 0.72rem;
  font-weight: 600;
  color: #94a3b8;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.meta-chip-value {
  font-family: 'Fira Code', monospace;
  font-size: 0.82rem;
  color: #a5b4fc;
  background: rgba(255, 255, 255, 0.06);
  padding: 0.15rem 0.5rem;
  border-radius: 4px;
}

.meta-loc {
  font-size: 0.82rem;
  color: #cbd5e1;
}

/* セクション */
.sections {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.section-card {
  border-radius: 10px;
  background: rgba(255,255,255,0.95) !important;
  backdrop-filter: blur(8px);
}

.sec-hd {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}
.sec-hd span {
  font-size: 0.9rem;
  font-weight: 600;
  color: #0f172a;
}

.tag-wrap {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}
</style>
