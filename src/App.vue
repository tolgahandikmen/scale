<script setup lang="ts">
import { ref } from 'vue';
import Button from 'primevue/button';
import ItemSheetsPage from '@/pages/ItemSheetsPage.vue';
import ConfigTemplatesPage from '@/pages/ConfigTemplatesPage.vue';
import PartTemplateConfigPage from '@/pages/PartTemplateConfigPage.vue';

const page = ref<'sheets' | 'config' | 'part-map'>('sheets');
</script>

<template>
  <div class="p-2 flex gap-2 border-bottom-1 surface-border">
    <Button label="Sheets" text :class="{ 'p-button-secondary': page === 'sheets' }" @click="page = 'sheets'" />
    <Button label="Config" text :class="{ 'p-button-secondary': page === 'config' }" @click="page = 'config'" />
    <Button label="Part-Template Map" text :class="{ 'p-button-secondary': page === 'part-map' }" @click="page = 'part-map'" />
  </div>

  <ItemSheetsPage v-if="page === 'sheets'" />
  <ConfigTemplatesPage v-else-if="page === 'config'" />
  <PartTemplateConfigPage v-else />
</template>
