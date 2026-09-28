<script setup lang="ts">
import TopNav from '@/components/TopNav.vue'
</script>

<template>
  <div class="app">
    <TopNav />
    <RouterView />
  </div>
</template>

<style>
html,
body,
#app {
  height: 100%;
}

body {
  background:
    radial-gradient(900px 600px at 15% 0%, rgba(249, 115, 22, 0.14), transparent 60%),
    radial-gradient(900px 600px at 85% 10%, rgba(249, 115, 22, 0.1), transparent 55%),
    #f7f7fb;
}
</style>
