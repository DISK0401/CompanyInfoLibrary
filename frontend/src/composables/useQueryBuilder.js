import { ref, computed } from 'vue'
import { COMPANY_FIELDS, FIELD_TYPE, TEXT_MATCH_TYPES, NUMBER_DATE_MATCH_TYPES } from '@/constants/companyFields'

export function useQueryBuilder() {
  const logic = ref('AND')
  const conditions = ref([])

  function getDefaultMatchType(fieldType) {
    return fieldType === FIELD_TYPE.TEXT ? 'CONTAINS' : 'EQ'
  }

  function addCondition() {
    const firstField = COMPANY_FIELDS[1] // 'name'をデフォルト
    conditions.value.push({
      id: crypto.randomUUID(),
      field: firstField.key,
      fieldType: firstField.type,
      matchType: getDefaultMatchType(firstField.type),
      value: null,
      valueTo: null,
    })
  }

  function removeCondition(id) {
    conditions.value = conditions.value.filter(c => c.id !== id)
  }

  function updateConditionField(id, fieldKey) {
    const c = conditions.value.find(c => c.id === id)
    if (!c) return
    const field = COMPANY_FIELDS.find(f => f.key === fieldKey)
    c.field = fieldKey
    c.fieldType = field.type
    c.matchType = getDefaultMatchType(field.type)
    c.value = null
    c.valueTo = null
  }

  function updateCondition(id, updates) {
    const c = conditions.value.find(c => c.id === id)
    if (c) Object.assign(c, updates)
  }

  function resetConditions() {
    conditions.value = []
    logic.value = 'AND'
  }

  // conditions → APIリクエストボディに変換
  function buildSearchBody(sortField, sortOrder, page, size) {
    const validConditions = conditions.value
      .filter(c => {
        if (c.value === null || c.value === '') return false
        if (c.matchType === 'RANGE' && (c.valueTo === null || c.valueTo === '')) return false
        return true
      })
      .map(c => ({
        field: c.field,
        matchType: c.matchType,
        value: String(c.value),
        valueTo: c.valueTo != null ? String(c.valueTo) : null,
      }))

    // バックエンドは FUZZY + OR の組み合わせを拒否するため、フロントで事前チェック
    const hasFuzzy = validConditions.some(c => c.matchType === 'FUZZY')
    if (hasFuzzy && logic.value === 'OR') {
      throw new Error('あいまい一致（FUZZY）はOR条件と組み合わせて使用できません。AND条件に変更してください。')
    }

    const sort = sortField ? [{ field: sortField, direction: sortOrder === 1 ? 'ASC' : 'DESC' }] : []

    return {
      conditions: validConditions,
      logic: logic.value,
      sort,
      page: page ?? 0,
      size: size ?? 20,
    }
  }

  const matchTypeOptions = computed(() => (fieldType) =>
    fieldType === FIELD_TYPE.TEXT ? TEXT_MATCH_TYPES : NUMBER_DATE_MATCH_TYPES
  )

  return {
    logic, conditions,
    addCondition, removeCondition, updateConditionField, updateCondition, resetConditions,
    buildSearchBody, matchTypeOptions,
  }
}
