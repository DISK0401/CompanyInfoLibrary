<template>
  <n-config-provider :theme-overrides="themeOverrides" :locale="jaJP" :date-locale="dateJaJP">
    <n-message-provider>
      <n-dialog-provider>
        <div class="app-shell">
          <header class="app-header">
            <div class="header-inner">
              <div class="brand" @click="router.push({ name: 'CompanyList' })">
                <div class="brand-mark">
                  <n-icon :component="BusinessOutline" size="14" color="#fff" />
                </div>
                <div class="brand-text">
                  <span class="brand-name">企業情報ライブラリ</span>
                  <span class="brand-en">CompanyInfoLibrary</span>
                </div>
              </div>
              <nav class="header-nav">
                <RouterLink :to="{ name: 'CompanyList' }" class="nav-item" active-class="nav-item--active">
                  <n-icon :component="SearchOutline" size="14" />
                  企業検索
                </RouterLink>
                <a v-if="swaggerEnabled" href="/api-docs" target="_blank" rel="noopener" class="nav-item">
                  <n-icon :component="CodeSlashOutline" size="14" />
                  API仕様
                </a>
              </nav>
            </div>
          </header>
          <main class="app-main">
            <RouterView />
          </main>
        </div>
      </n-dialog-provider>
    </n-message-provider>
  </n-config-provider>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { RouterView, RouterLink, useRouter } from 'vue-router'
import { NConfigProvider, NMessageProvider, NDialogProvider, NIcon } from 'naive-ui'
import { jaJP, dateJaJP } from 'naive-ui'
import { BusinessOutline, SearchOutline, CodeSlashOutline } from '@vicons/ionicons5'
import axios from 'axios'

const router = useRouter()
const swaggerEnabled = ref(false)

onMounted(async () => {
  try {
    await axios.get('/v3/api-docs')
    swaggerEnabled.value = true
  } catch {
    swaggerEnabled.value = false
  }
})

const themeOverrides = {
  common: {
    primaryColor: '#4f46e5',
    primaryColorHover: '#4338ca',
    primaryColorPressed: '#3730a3',
    primaryColorSuppl: '#6366f1',
    fontFamily: '"Noto Sans JP", "Hiragino Sans", "Yu Gothic", sans-serif',
    fontFamilyMono: '"Fira Code", "SFMono-Regular", Consolas, monospace',
    borderRadius: '6px',
    borderRadiusSmall: '4px',
  },
  DataTable: {
    thColor: '#374151',
    thColorModal: '#374151',
    thColorPopover: '#374151',
    thTextColor: '#e5e7eb',
    thTextColorModal: '#e5e7eb',
    tdColorHover: '#f3f4f6',
    tdColorStriped: '#fafafa',
    borderColor: '#e5e7eb',
    thFontWeight: '600',
    fontSize: '13px',
  },
  Button: { fontWeight: '500' },
  Input: { borderRadius: '6px' },
  Select: { peers: { InternalSelection: { borderRadius: '6px' } } },
}
</script>

<style>
@import url('https://fonts.googleapis.com/css2?family=Noto+Sans+JP:wght@300;400;500;700&display=swap');

:root {
  --color-primary: #4f46e5;
  --color-primary-dark: #3730a3;
  --color-text: #0f172a;
  --color-text-secondary: #475569;
  --color-text-muted: #94a3b8;
  --color-border: #e2e8f0;
  --color-surface: #ffffff;
  --color-surface-alt: #f8fafc;
  --shadow-sm: 0 1px 3px rgba(15,23,42,0.07);
  --shadow-md: 0 4px 12px rgba(15,23,42,0.09);
}

*, *::before, *::after { box-sizing: border-box; }

html, body {
  margin: 0;
  padding: 0;
  font-family: 'Noto Sans JP', 'Hiragino Sans', sans-serif;
  font-feature-settings: "palt";
  -webkit-font-smoothing: antialiased;
  background: #f4f6f9;
  color: #0f172a;
}

#app {
  min-height: 100vh;
  /* 精密なドットグリッド背景 — データツールらしい奥行き感 */
  background-image: radial-gradient(circle, #c8cdd8 1px, transparent 1px);
  background-size: 24px 24px;
  background-attachment: fixed;
}

/* ===== カード全体にガラス効果 ===== */
.n-card {
  background: rgba(255, 255, 255, 0.92) !important;
  backdrop-filter: blur(10px) !important;
  -webkit-backdrop-filter: blur(10px) !important;
}

/* ===== 全DataTableに統一スタイル ===== */
.n-data-table .n-data-table-th {
  font-size: 0.72rem !important;
  letter-spacing: 0.07em !important;
  text-transform: uppercase !important;
  white-space: nowrap;
}

/* ヘッダーの列区切り */
.n-data-table .n-data-table-th:not(:last-child) {
  border-right: 1px solid #4b5563 !important;
}

/* 行ホバー時の左アクセント */
.n-data-table .n-data-table-tr:hover .n-data-table-td:first-child {
  box-shadow: inset 3px 0 0 #4f46e5;
}

/* 行クリック可能のカーソル（data-table-container に cursor: pointer を付けたいとき） */
.n-data-table.clickable-rows .n-data-table-tr {
  cursor: pointer;
}
</style>

<style scoped>
.app-shell {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.app-header {
  background: #1e293b;
  position: sticky;
  top: 0;
  z-index: 200;
  border-bottom: 1px solid #334155;
}

.header-inner {
  max-width: 1400px;
  margin: 0 auto;
  height: 52px;
  padding: 0 1.5rem;
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 2.5rem;
}

.brand {
  display: flex;
  flex-direction: row;
  gap: 0.7rem;
  cursor: pointer;
  align-items: center;
  text-decoration: none;
}

.brand-mark {
  width: 30px;
  height: 30px;
  border-radius: 7px;
  background: linear-gradient(135deg, #6366f1 0%, #4f46e5 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  box-shadow: 0 0 0 1px rgba(99,102,241,0.4);
}

.brand-text {
  display: flex;
  flex-direction: column;
  gap: 1px;
  line-height: 1;
}

.brand-name {
  font-size: 0.9rem;
  font-weight: 700;
  color: #f1f5f9;
  letter-spacing: -0.01em;
  white-space: nowrap;
}

.brand-en {
  font-size: 0.58rem;
  color: #94a3b8;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  font-weight: 400;
}

.header-nav {
  display: flex;
  flex-direction: row;
  gap: 0.2rem;
}

.nav-item {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 0.35rem;
  padding: 0.4rem 0.875rem;
  border-radius: 6px;
  font-size: 0.8rem;
  font-weight: 500;
  color: #94a3b8;
  text-decoration: none;
  transition: all 0.15s;
}

.nav-item:hover {
  background: rgba(255, 255, 255, 0.07);
  color: #e2e8f0;
}

.nav-item--active {
  background: rgba(99, 102, 241, 0.2);
  color: #a5b4fc;
}

.app-main {
  flex: 1;
}
</style>
