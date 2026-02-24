<script setup>
import { computed, reactive, watchEffect } from 'vue';
import FieldRenderer from './FieldRenderer.vue';

const props = defineProps({
  fields: { type: Array, default: () => [] },
  modelValue: { type: Object, default: undefined },
});

const emit = defineEmits(['update:modelValue']);

const sortedFields = computed(() => [...props.fields].sort((a, b) => (a.orderNo ?? 0) - (b.orderNo ?? 0)));

const values = reactive({});

function ensureDefaults() {
  for (const f of sortedFields.value) {
    if (values[f.id]) continue;

    if (f.type === 'TEXT') values[f.id] = { type: 'TEXT', value: null };
    else if (f.type === 'NUMBER') values[f.id] = { type: 'NUMBER', value: null, unit: null };
    else if (f.type === 'DATE') values[f.id] = { type: 'DATE', value: null };
    else if (f.type === 'BOOLEAN') values[f.id] = { type: 'BOOLEAN', value: null };
    else if (f.type === 'DROPDOWN') values[f.id] = { type: 'DROPDOWN', value: null };
    else if (f.type === 'TABLE') values[f.id] = { type: 'TABLE', value: { rows: [] } };

    if (f.isMulti && f.multiKeys?.length) {
      const obj = {};
      for (const k of f.multiKeys) obj[k] = null;
      values[f.id] = { type: 'MULTI', value: obj, unit: null };
    }
  }
}

watchEffect(() => {
  if (props.modelValue) {
    for (const [k, v] of Object.entries(props.modelValue)) {
      values[Number(k)] = v;
    }
  }
  ensureDefaults();
});

watchEffect(() => {
  emit('update:modelValue', { ...values });
});
</script>

<template>
  <div class="p-fluid">
    <div v-for="f in sortedFields" :key="f.id" class="mb-3">
      <FieldRenderer :field="f" :modelValue="values[f.id]" @update:modelValue="(v) => (values[f.id] = v)" />
    </div>
  </div>
</template>
