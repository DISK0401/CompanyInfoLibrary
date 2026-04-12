import axios from 'axios'

const api = axios.create({ baseURL: '/api' })

/**
 * 企業検索
 * @param {{ name?, location?, minCapital?, minEmployees?, page?, size? }} params
 */
export const searchCompanies = (params) =>
  api.get('/companies', { params }).then((r) => r.data)

/**
 * 企業詳細取得
 * @param {string} corporateNumber
 */
export const getCompany = (corporateNumber) =>
  api.get(`/companies/${corporateNumber}`).then((r) => r.data)
