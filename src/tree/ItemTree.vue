<template>
  <div class="page-card p-3 h-full">
    <h3 class="mt-0">Items</h3>
    <Tree
      :value="nodes"
      selection-mode="single"
      :selection-keys="selectionKeys"
      @node-select="onNodeSelect"
      @update:selection-keys="(v) => (selectionKeys = v)"
      class="w-full"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import Tree from 'primevue/tree';
import type { TreeNode } from 'primevue/treenode';

import type { Item } from '@/models/sheet';

const props = defineProps<{
  items: Item[];
}>();

const emit = defineEmits<{
  (e: 'select', itemId: string): void;
}>();

const selectionKeys = ref<Record<string, { checked?: boolean; partialChecked?: boolean }>>({});

const nodes = computed(() =>
  props.items.map((item) => ({
    key: item.id,
    label: `${item.part_id} | ${item.part_sn} | ${item.bridge_name}`,
    data: item,
    leaf: true,
  })),
);

const onNodeSelect = (node: TreeNode) => {
  const key = typeof node.key === 'string' ? node.key : undefined;
  if (key) emit('select', key);
};
</script>
