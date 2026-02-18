<template>
  <div class="page-card p-3">
    <div class="flex justify-content-between align-items-center mb-3">
      <h3 class="m-0">Table Schema Editor</h3>
      <Button size="small" label="Add Column" icon="pi pi-plus" @click="addColumn" />
    </div>

    <DataTable :value="localColumns" data-key="key" edit-mode="cell" responsive-layout="scroll" class="p-datatable-sm">
      <Column field="key" header="Key">
        <template #body="{ data }">
          <InputText class="w-full" v-model="data.key" @input="emitChange" />
        </template>
      </Column>
      <Column field="label" header="Label">
        <template #body="{ data }">
          <InputText class="w-full" v-model="data.label" @input="emitChange" />
        </template>
      </Column>
      <Column field="type" header="Type">
        <template #body="{ data }">
          <Dropdown class="w-full" :options="colTypes" v-model="data.type" @change="emitChange" />
        </template>
      </Column>
      <Column field="unitMode" header="Unit Mode">
        <template #body="{ data }">
          <Dropdown class="w-full" :options="unitModes" v-model="data.unitMode" @change="emitChange" />
        </template>
      </Column>
      <Column header="Units CSV">
        <template #body="{ data }">
          <InputText class="w-full" :model-value="data.unitOptions.join(',')" @update:model-value="(v) => setCsv(data, 'unitOptions', v ?? '')" />
        </template>
      </Column>
      <Column header="Dropdown CSV">
        <template #body="{ data }">
          <InputText class="w-full" :model-value="data.dropdownOptions.join(',')" @update:model-value="(v) => setCsv(data, 'dropdownOptions', v ?? '')" />
        </template>
      </Column>
      <Column header="Actions" style="width: 4rem">
        <template #body="{ index }">
          <Button icon="pi pi-trash" text severity="danger" @click="removeColumn(index)" />
        </template>
      </Column>
    </DataTable>

    <div class="mt-3">
      <h4 class="mb-2">Default Rows (CSV)</h4>
      <InputText class="w-full" :model-value="localDefaultRows.join(',')" @update:model-value="(v) => setRows(v ?? '')" />
    </div>

    <div class="mt-4">
      <h4 class="mb-2">Live Preview</h4>
      <DataTable :value="previewRows" responsive-layout="scroll" class="p-datatable-sm">
        <Column v-for="col in localColumns" :key="col.key" :field="col.key" :header="col.label || col.key" />
      </DataTable>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import Button from 'primevue/button';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import InputText from 'primevue/inputtext';
import Dropdown from 'primevue/dropdown';

import type { TableColumnDefinition, TableSchema, UnitMode } from '@/models/template';

const props = defineProps<{
  modelValue?: TableSchema;
}>();

const emit = defineEmits<{
  (e: 'update:modelValue', value: TableSchema): void;
}>();

const colTypes: Array<TableColumnDefinition['type']> = ['NUMBER', 'TEXT', 'DROPDOWN', 'BOOLEAN'];
const unitModes: UnitMode[] = ['NONE', 'OPTIONAL', 'REQUIRED'];

const localColumns = ref<TableColumnDefinition[]>([]);
const localDefaultRows = ref<string[]>([]);

const hydrate = () => {
  localColumns.value = structuredClone(props.modelValue?.columns ?? []);
  localDefaultRows.value = structuredClone(props.modelValue?.defaultRows ?? []);
};

watch(() => props.modelValue, hydrate, { immediate: true, deep: true });

const emitChange = () => {
  emit('update:modelValue', {
    rowMode: 'CONFIGURABLE',
    defaultRows: localDefaultRows.value,
    columns: localColumns.value,
  });
};

const addColumn = () => {
  localColumns.value.push({
    key: `col_${localColumns.value.length + 1}`,
    label: `Column ${localColumns.value.length + 1}`,
    type: 'TEXT',
    unitMode: 'NONE',
    unitOptions: [],
    dropdownOptions: [],
  });
  emitChange();
};

const removeColumn = (idx: number) => {
  localColumns.value = localColumns.value.filter((_, i) => i !== idx);
  emitChange();
};

const setCsv = (column: TableColumnDefinition, key: 'unitOptions' | 'dropdownOptions', raw: string) => {
  column[key] = raw.split(',').map((x) => x.trim()).filter(Boolean);
  emitChange();
};

const setRows = (raw: string) => {
  localDefaultRows.value = raw.split(',').map((x) => x.trim()).filter(Boolean);
  emitChange();
};

const previewRows = computed(() => {
  const rowCount = Math.max(localDefaultRows.value.length, 1);
  return Array.from({ length: rowCount }, (_, idx) => {
    const row: Record<string, unknown> = {};
    localColumns.value.forEach((col) => {
      if (col.type === 'BOOLEAN') row[col.key] = false;
      else if (col.type === 'NUMBER') row[col.key] = 0;
      else if (col.type === 'DROPDOWN') row[col.key] = col.dropdownOptions[0] ?? '';
      else row[col.key] = localDefaultRows.value[idx] ?? '';
    });
    return row;
  });
});
</script>
