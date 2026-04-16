import { ref } from 'vue'
import { DEFAULT_VISIBLE_COLUMNS } from '@/constants/companyFields'

const DB_NAME = 'company-info-library'
const DB_VERSION = 1
const STORE_NAME = 'column-settings'
const SETTINGS_KEY = 'companyList'

function openDatabase() {
  return new Promise((resolve, reject) => {
    const req = indexedDB.open(DB_NAME, DB_VERSION)
    req.onupgradeneeded = (e) => {
      e.target.result.createObjectStore(STORE_NAME, { keyPath: 'key' })
    }
    req.onsuccess = (e) => resolve(e.target.result)
    req.onerror = (e) => reject(e.target.error)
  })
}

export function useColumnSettings() {
  const visibleColumns = ref([...DEFAULT_VISIBLE_COLUMNS])
  const isLoaded = ref(false)

  async function loadSettings() {
    try {
      const db = await openDatabase()
      const tx = db.transaction(STORE_NAME, 'readonly')
      const store = tx.objectStore(STORE_NAME)
      const result = await new Promise((resolve) => {
        const req = store.get(SETTINGS_KEY)
        req.onsuccess = () => resolve(req.result)
        req.onerror = () => resolve(null)
      })
      if (result?.columns?.length) {
        visibleColumns.value = result.columns
      }
      db.close()
    } catch {
      // IndexedDB利用不可の場合はデフォルト値を使用
    } finally {
      isLoaded.value = true
    }
  }

  async function saveSettings() {
    try {
      const db = await openDatabase()
      const tx = db.transaction(STORE_NAME, 'readwrite')
      tx.objectStore(STORE_NAME).put({
        key: SETTINGS_KEY,
        columns: visibleColumns.value,
        updatedAt: new Date().toISOString(),
      })
      await new Promise((resolve, reject) => {
        tx.oncomplete = resolve
        tx.onerror = reject
      })
      db.close()
    } catch {
      // 保存失敗はサイレントに無視
    }
  }

  function toggleColumn(key) {
    const idx = visibleColumns.value.indexOf(key)
    if (idx >= 0) {
      if (visibleColumns.value.length > 1) {
        visibleColumns.value.splice(idx, 1)
      }
    } else {
      visibleColumns.value.push(key)
    }
    saveSettings()
  }

  function reorderColumns(newOrder) {
    visibleColumns.value = newOrder
    saveSettings()
  }

  return { visibleColumns, isLoaded, loadSettings, saveSettings, toggleColumn, reorderColumns }
}
