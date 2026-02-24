<script setup>
import { computed, ref, watchEffect } from 'vue';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import InputNumber from 'primevue/inputnumber';
import InputText from 'primevue/inputtext';
import Dropdown from 'primevue/dropdown';

const props = defineProps({
  field: { type: Object, required: true },
  modelValue: { type: Object, default: null },
});
const emit = defineEmits(['update:modelValue']);

const mv = () => props.modelValue ?? {};

const rows = ref([]);

const schema = computed(() => props.field.tableSchema);
const columns = computed(() => schema.value?.columns ?? []);

watchEffect(() => {
  const currentRows = mv()?.value?.rows ?? [];
  if (currentRows.length) {
    rows.value = JSON.parse(JSON.stringify(currentRows));
    return;
  }

  const defaults = schema.value?.defaultRows ?? [];
  const base = defaults.map((r) => {
    const obj = {};
    if (columns.value[0]) obj[columns.value[0].key] = typeof r === 'string' ? Number(r) : r;
    for (let i = 1; i < columns.value.length; i++) obj[columns.value[i].key] = null;
    return obj;
  });

  rows.value = base;
  commit();
});

function commit() {
  emit('update:modelValue', { type: 'TABLE', value: { rows: rows.value } });
}

function setCell(rowIndex, colKey, val) {
  rows.value[rowIndex][colKey] = val;
  commit();
}
</script>

<template>
  <label class="font-semibold mb-2 block">{{ field.label }}</label>

  <DataTable :value="rows" size="small" class="p-datatable-sm" scrollable scrollHeight="260px">
    <Column v-for="c in columns" :key="c.key" :field="c.key" :header="c.label">
      <template #body="{ data, index }">
        <InputNumber
          v-if="c.type === 'NUMBER'"
          :modelValue="data[c.key] ?? null"
          @update:modelValue="(v) => setCell(index, c.key, v)"
          :useGrouping="false"
          class="w-full"
        />
        <Dropdown
          v-else-if="c.type === 'DROPDOWN'"
          :options="c.dropdownOptions ?? []"
          :modelValue="data[c.key] ?? null"
          @update:modelValue="(v) => setCell(index, c.key, v)"
          class="w-full"
        />
        <InputText
          v-else
          :modelValue="data[c.key] ?? ''"
          @update:modelValue="(v) => setCell(index, c.key, v ?? '')"
          class="w-full"
        />
      </template>
    </Column>
  </DataTable>

  <small class="p-text-secondary block mt-2">Rows & columns are defined by template configuration.</small>
</template>
