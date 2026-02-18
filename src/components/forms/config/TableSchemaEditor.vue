<script setup lang="ts">
import { computed, reactive, watchEffect } from 'vue';
import Button from 'primevue/button';
import InputText from 'primevue/inputtext';
import Dropdown from 'primevue/dropdown';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import Divider from 'primevue/divider';
import type { TableSchema, TableColumnSchema } from '@/models/form';

const props = defineProps<{ schema: TableSchema }>();
const emit = defineEmits<{ (e: 'update:schema', v: TableSchema): void }>();

const state = reactive<TableSchema>({ rowMode: 'CONFIGURABLE', defaultRows: [], columns: [] });

watchEffect(() => {
  state.rowMode = props.schema.rowMode;
  state.defaultRows = [...(props.schema.defaultRows ?? [])];
  state.columns = (props.schema.columns ?? []).map((c) => ({ ...c }));
});

const colTypeOptions: Array<TableColumnSchema['type']> = ['NUMBER', 'TEXT', 'DROPDOWN', 'BOOLEAN'];
const unitModeOptions = ['NONE', 'OPTIONAL', 'REQUIRED'];

function commit() {
  emit('update:schema', {
    rowMode: state.rowMode,
    defaultRows: [...state.defaultRows],
    columns: state.columns.map((c) => ({ ...c })),
  });
}

function setDefaultRowsCsv(v: string | undefined) {
  state.defaultRows = (v ?? '')
    .split(',')
    .map((s) => s.trim())
    .filter(Boolean);
  commit();
}

const defaultRowsCsv = computed(() => (state.defaultRows ?? []).join(','));

function addColumn() {
  const idx = state.columns.length + 1;
  state.columns.push({
    key: `col_${idx}`,
    label: `Column ${idx}`,
    type: 'NUMBER',
    unitMode: 'NONE',
    unitOptions: [],
    dropdownOptions: [],
  });
  commit();
}

function removeColumn(c: TableColumnSchema) {
  state.columns = state.columns.filter((x) => x !== c);
  commit();
}

const previewRows = computed(() => {
  const cols = state.columns ?? [];
  const defaults = state.defaultRows ?? [];

  return defaults.map((r) => {
    const obj: Record<string, unknown> = {};
    cols.forEach((c, idx) => {
      obj[c.key] = idx === 0 ? r : '';
    });
    return obj;
  });
});
</script>

<template>
  <div class="mt-3">
    <div class="font-semibold mb-2">Table Schema</div>

    <div class="mb-3">
      <label class="font-semibold mb-1 block">Default Rows (comma separated)</label>
      <InputText class="w-full" :modelValue="defaultRowsCsv" @update:modelValue="setDefaultRowsCsv" />
      <small class="p-text-secondary">Example: 0,20,40,60,80,100,80,60,40,20,0</small>
    </div>

    <div class="flex align-items-center justify-content-between mb-2">
      <div class="font-semibold">Columns</div>
      <Button label="Add Column" icon="pi pi-plus" size="small" @click="addColumn" />
    </div>

    <DataTable :value="state.columns" size="small" class="p-datatable-sm">
      <Column header="Key" style="width: 12rem">
        <template #body="{ data }">
          <InputText class="w-full" v-model="data.key" @update:modelValue="commit" />
        </template>
      </Column>

      <Column header="Label">
        <template #body="{ data }">
          <InputText class="w-full" v-model="data.label" @update:modelValue="commit" />
        </template>
      </Column>

      <Column header="Type" style="width: 10rem">
        <template #body="{ data }">
          <Dropdown class="w-full" :options="colTypeOptions" v-model="data.type" @update:modelValue="commit" />
        </template>
      </Column>

      <Column header="Unit Mode" style="width: 11rem">
        <template #body="{ data }">
          <Dropdown class="w-full" :options="unitModeOptions" v-model="data.unitMode" @update:modelValue="commit" />
        </template>
      </Column>

      <Column header="Unit Options" style="width: 16rem">
        <template #body="{ data }">
          <InputText
            class="w-full"
            :modelValue="(data.unitOptions ?? []).join(',')"
            @update:modelValue="(v)=>{ data.unitOptions = (v ?? '').split(',').map(s=>s.trim()).filter(Boolean); commit(); }"
          />
        </template>
      </Column>

      <Column header="Dropdown Options" style="width: 16rem">
        <template #body="{ data }">
          <InputText
            class="w-full"
            :disabled="data.type !== 'DROPDOWN'"
            :modelValue="(data.dropdownOptions ?? []).join(',')"
            @update:modelValue="(v)=>{ data.dropdownOptions = (v ?? '').split(',').map(s=>s.trim()).filter(Boolean); commit(); }"
          />
        </template>
      </Column>

      <Column header="" style="width: 4rem">
        <template #body="{ data }">
          <Button icon="pi pi-trash" severity="danger" text @click="removeColumn(data)" />
        </template>
      </Column>
    </DataTable>

    <div v-if="state.columns.length && state.defaultRows.length" class="mt-4">
      <Divider />
      <div class="font-semibold mb-2">Preview</div>
      <DataTable :value="previewRows" size="small" class="p-datatable-sm">
        <Column v-for="c in state.columns" :key="c.key" :field="c.key" :header="c.label" />
      </DataTable>
      <small class="p-text-secondary">This is a preview of how the table will render in the sheet.</small>
    </div>
  </div>
</template>
