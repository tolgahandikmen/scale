<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import Tree from 'primevue/tree';
import InputText from 'primevue/inputtext';
import type { TreeNode } from 'primevue/treenode';
import scaleService from '@/services/ScaleService';
import type { ItemTreeNode } from '@/models/form';

const emit = defineEmits<{
  (e: 'selectItem', itemId: number): void;
}>();

const raw = ref<ItemTreeNode[]>([]);
const selectedKey = ref<Record<string, boolean>>({});
const expandedKeys = ref<Record<string, boolean>>({});
const searchTerm = ref('');

function filterNodes(nodes: ItemTreeNode[], term: string): ItemTreeNode[] {
  return nodes.reduce<ItemTreeNode[]>((acc, node) => {
    const children = node.children ? filterNodes(node.children, term) : [];
    const haystack = `${node.label} ${node.type} ${node.itemId ?? ''}`.toLowerCase();
    const selfMatch = haystack.includes(term);

    if (selfMatch) {
      acc.push({ ...node, children: node.children ? structuredClone(node.children) : [] });
      return acc;
    }

    if (children.length) {
      acc.push({ ...node, children });
    }

    return acc;
  }, []);
}

const filteredRaw = computed<ItemTreeNode[]>(() => {
  const q = searchTerm.value.trim().toLowerCase();
  if (!q) return raw.value;
  return filterNodes(raw.value, q);
});

function toTreeNodes(nodes: ItemTreeNode[]): TreeNode[] {
  return nodes.map((n) => ({
    key: n.key,
    label: n.label,
    data: n,
    leaf: !n.children?.length,
    children: n.children ? toTreeNodes(n.children) : [],
  }));
}

function collectExpandKeys(nodes: TreeNode[]): Record<string, boolean> {
  const keys: Record<string, boolean> = {};
  const walk = (list: TreeNode[]) => {
    list.forEach((node) => {
      if (typeof node.key === 'string') {
        keys[node.key] = true;
      }
      if (node.children?.length) {
        walk(node.children);
      }
    });
  };
  walk(nodes);
  return keys;
}

const treeNodes = computed<TreeNode[]>(() => toTreeNodes(filteredRaw.value));

onMounted(async () => {
  raw.value = await scaleService.getItemsTree();
});

watch([searchTerm, treeNodes], () => {
  if (searchTerm.value.trim()) {
    expandedKeys.value = collectExpandKeys(treeNodes.value);
  }
});

function onNodeSelect(node: TreeNode) {
  const data = node.data as ItemTreeNode | undefined;
  if (data?.type === 'BRIDGE' && data.itemId) {
    emit('selectItem', data.itemId);
  }
}
</script>

<template>
  <div class="tree-shell">
    <div class="tree-head">
      <div class="tree-title">Item Explorer</div>
      <small class="tree-note">Search works across part, serial, and bridge nodes.</small>
    </div>

    <span class="p-input-icon-left w-full mb-3">
      <i class="pi pi-search" />
      <InputText v-model="searchTerm" class="w-full" placeholder="Search part / serial / bridge" />
    </span>

    <Tree
      :value="treeNodes"
      selectionMode="single"
      v-model:selectionKeys="selectedKey"
      v-model:expandedKeys="expandedKeys"
      @nodeSelect="onNodeSelect"
      class="w-full item-tree"
    />

    <small v-if="searchTerm.trim() && treeNodes.length === 0" class="p-text-secondary block mt-2">No matching node found.</small>
  </div>
</template>

<style scoped>
.tree-shell {
  padding: 0.75rem;
}

.tree-head {
  margin-bottom: 0.75rem;
}

.tree-title {
  font-weight: 700;
  letter-spacing: 0.03em;
}

.tree-note {
  color: #5f6471;
}

.item-tree :deep(.p-treenode-label) {
  font-weight: 500;
}
</style>

