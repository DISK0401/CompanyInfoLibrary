import { ref } from 'vue'
import { defineStore } from 'pinia'
import { advancedSearch } from '@/api/companies'

export const useCompanySearchStore = defineStore('companySearch', () => {
  const result = ref(null)
  const currentPage = ref(0)
  const loading = ref(false)
  const error = ref(null)
  const lastSearchBody = ref(null)

  async function search(body) {
    loading.value = true
    error.value = null
    lastSearchBody.value = body
    try {
      result.value = await advancedSearch({ ...body, page: currentPage.value })
    } catch (e) {
      error.value = e.response?.data?.error ?? 'エラーが発生しました。'
    } finally {
      loading.value = false
    }
  }

  async function changePage(page, rows) {
    if (!lastSearchBody.value) return
    currentPage.value = page
    const body = rows
      ? { ...lastSearchBody.value, size: rows }
      : lastSearchBody.value
    await search(body)
  }

  function reset() {
    result.value = null
    currentPage.value = 0
    error.value = null
    lastSearchBody.value = null
  }

  return { result, currentPage, loading, error, lastSearchBody, search, changePage, reset }
})
