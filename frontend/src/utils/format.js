/**
 * 資本金を「〇億〇万円」形式にフォーマットする
 * @param {number|null} value
 * @returns {string}
 */
export function formatCapital(value) {
  if (value == null) return '—'
  if (value >= 100_000_000) {
    const oku = Math.floor(value / 100_000_000)
    const man = Math.floor((value % 100_000_000) / 10_000)
    return man > 0 ? `${oku}億${man.toLocaleString()}万円` : `${oku}億円`
  }
  if (value >= 10_000) {
    return `${Math.floor(value / 10_000).toLocaleString()}万円`
  }
  return `${value.toLocaleString()}円`
}

/**
 * 財務金額を「▲〇億円」形式にフォーマットする
 * @param {number|null} value
 * @returns {string}
 */
export function formatAmount(value) {
  if (value == null) return '—'
  const abs = Math.abs(value)
  const sign = value < 0 ? '▲' : ''
  if (abs >= 100_000_000) return `${sign}${Math.floor(abs / 100_000_000).toLocaleString()}億円`
  if (abs >= 10_000) return `${sign}${Math.floor(abs / 10_000).toLocaleString()}万円`
  return `${sign}${abs.toLocaleString()}円`
}
