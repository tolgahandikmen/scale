import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router';

import ItemSheetsPage from '@/pages/ItemSheetsPage.vue';
import ConfigTemplatesPage from '@/pages/ConfigTemplatesPage.vue';

const routes: RouteRecordRaw[] = [
  { path: '/', redirect: { name: 'item-sheets' } },
  { path: '/items', name: 'item-sheets', component: ItemSheetsPage },
  { path: '/templates', name: 'config-templates', component: ConfigTemplatesPage },
];

export default createRouter({
  history: createWebHistory(),
  routes,
});
