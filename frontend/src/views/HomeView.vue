<template>
  <div class="home">
    <div class="hero">
      <h1 class="hero-title">CompanyInfoLibrary</h1>
      <p class="hero-subtitle">
        gBizINFO をベースにした企業情報の検索・閲覧サービスです。<br />
        法人名・所在地・資本金・従業員数で絞り込めます。
      </p>
      <RouterLink to="/companies" class="btn-primary">企業を検索する</RouterLink>
    </div>
  </div>
</template>

<script setup>
import { RouterLink } from 'vue-router'
</script>

<style scoped>
.home {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 70vh;
}
.hero {
  text-align: center;
  padding: 2rem;
}
.hero-title {
  font-size: 2.4rem;
  font-weight: 700;
  color: #1a202c;
  margin-bottom: 1rem;
}
.hero-subtitle {
  font-size: 1.05rem;
  color: #4a5568;
  line-height: 1.8;
  margin-bottom: 2rem;
}
.btn-primary {
  display: inline-block;
  padding: 0.75rem 2rem;
  background: #3b82f6;
  color: #fff;
  border-radius: 6px;
  text-decoration: none;
  font-size: 1rem;
  font-weight: 600;
  transition: background 0.2s;
}
.btn-primary:hover {
  background: #2563eb;
}
</style>
