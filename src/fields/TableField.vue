<template>
  <div class="field mb-3">
    <div class="flex justify-content-between align-items-center mb-2">
      <label class="font-medium block">{{ field.label }}</label>
      <Button size="small" icon="pi pi-plus" text label="Add Row" @click="addRow" />
    </div>

    <DataTable :value="rows" edit-mode="cell" data-key="_idx" responsive-layout="scroll" class="p-datatable-sm">
      <Column v-for="col in schemaColumns" :key="col.key" :field="col.key" :header="col.label">
        <template #body="{ data }">
          <span>{{ renderValue(data[col.key]) }}</span>
        </template>
        <template #editor="{ data }">
          <InputSwitch
            v-if="col.type === 'BOOLEAN'"
            :model-value="Boolean(data[col.key])"
            @update:model-value="(v) => updateCell(data._idx, col.key, v)"
          />
          <Dropdown
            v-else-if="col.type === 'DROPDOWN'"
            class="w-full"
            :options="col.dropdownOptions"
            :model-value="(data[col.key] as string) ?? ''"
            @update:model-value="(v) => updateCell(data._idx, col.key, v)"
          />
          <InputNumber
            v-else-if="col.type === 'NUMBER'"
            class="w-full"
            :model-value="(data[col.key] as number) ?? null"
            @update:model-value="(v) => updateCell(data._idx, col.key, v)"
          />
          <InputText
            v-else
            class="w-full"
            :model-value="(data[col.key] as string) ?? ''"
            @update:model-value="(v) => updateCell(data._idx, col.key, v)"
          />
        </template>
      </Column>
      <Column header="Actions" style="width: 6rem">
        <template #body="{ data }">
          <Button size="small" icon="pi pi-trash" text severity="danger" @click="removeRow(data._idx)" />
        </template>
      </Column>
    </DataTable>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import Button from 'primevue/button';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import InputText from 'primevue/inputtext';
import InputNumber from 'primevue/inputnumber';
import Dropdown from 'primevue/dropdown';
import InputSwitch from 'primevue/inputswitch';

import type { FieldDefinition } from '@/models/template';
import type { SheetValueTable } from '@/models/sheet';

type RowVM = Record<string, unknown> & { _idx: number };

const props = defineProps<{
  field: FieldDefinition;
  modelValue: unknown;
}>();

const emit = defineEmits<{
  (e: 'update:modelValue', value: SheetValueTable): void;
}>();

const schemaColumns = computed(() => props.field.tableSchema?.columns ?? []);

const tableValue = computed<SheetValueTable>(() => {
  if (typeof props.modelValue === 'object' && props.modelValue && 'rows' in props.modelValue) {
    return props.modelValue as SheetValueTable;
  }
  return { rows: [] };
});

const rows = computed<RowVM[]>(() => tableValue.value.rows.map((r, i) => ({ ...r, _idx: i })));

const emitRows = (newRows: Array<Record<string, unknown>>) => emit('update:modelValue', { rows: newRows });
const addRow = () => emitRows([...tableValue.value.rows, {}]);
const removeRow = (idx: number) => emitRows(tableValue.value.rows.filter((_, i) => i !== idx));

const updateCell = (idx: number, key: string, value: unknown) => {
  emitRows(
    tableValue.value.rows.map((row, i) => {
      if (i !== idx) return row;
      return { ...row, [key]: value };
    }),
  );
};

const renderValue = (value: unknown) => (typeof value === 'boolean' ? (value ? 'Yes' : 'No') : (value ?? ''));
</script>
