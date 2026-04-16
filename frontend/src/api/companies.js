import axios from 'axios'

const api = axios.create({ baseURL: '/api' })

export const searchCompanies = (params) =>
  api.get('/companies', { params }).then((r) => r.data)

// 新しい高度検索API
export const advancedSearch = (body) =>
  api.post('/companies/search', body).then((r) => r.data)

export const getCompany = (corporateNumber) =>
  api.get(`/companies/${corporateNumber}`).then((r) => r.data)

export const getSubsidies = (corporateNumber) =>
  api.get(`/companies/${corporateNumber}/subsidies`).then((r) => r.data)

export const getPatents = (corporateNumber) =>
  api.get(`/companies/${corporateNumber}/patents`).then((r) => r.data)

export const getProcurements = (corporateNumber) =>
  api.get(`/companies/${corporateNumber}/procurements`).then((r) => r.data)

export const getCertifications = (corporateNumber) =>
  api.get(`/companies/${corporateNumber}/certifications`).then((r) => r.data)

export const getCommendations = (corporateNumber) =>
  api.get(`/companies/${corporateNumber}/commendations`).then((r) => r.data)
