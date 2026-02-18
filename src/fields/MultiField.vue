<template>
  <div class="field mb-3">
    <label class="font-medium block mb-2">
      {{ field.label }}
      <span v-if="field.required" class="text-red-500">*</span>
    </label>
    <div class="grid formgrid">
      <div v-for="key in field.multiKeys" :key="key" class="col-6">
        <small class="block mb-1 text-color-secondary">{{ key }}</small>
        <InputNumber class="w-full" :model-value="asMulti.values[key] as number | null" @update:model-value="(v) => onValueUpdate(key, v)" />
      </div>
    </div>
    <div v-if="field.unitMode !== 'NONE'" class="mt-2">
      <Dropdown
        class="w-12rem"
        :options="field.unitOptions"
        :model-value="asMulti.unit"
        :placeholder="field.unitMode === 'REQUIRED' ? 'Unit*' : 'Unit'"
        @update:model-value="onUnitUpdate"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import InputNumber from 'primevue/inputnumber';
import Dropdown from 'primevue/dropdown';
import type { FieldDefinition } from '@/models/template';
import type { SheetValueMulti } from '@/models/sheet';

const props = defineProps<{
  field: FieldDefinition;
  modelValue: unknown;
}>();

const emit = defineEmits<{
  (e: 'update:modelValue', value: SheetValueMulti): void;
}>();

const asMulti = computed<SheetValueMulti>(() => {
  if (typeof props.modelValue === 'object' && props.modelValue && 'values' in props.modelValue) {
    return props.modelValue as SheetValueMulti;
  }

  const values: Record<string, string | number | null> = {};
  props.field.multiKeys.forEach((k) => {
    values[k] = null;
  });
  return { values };
});

const onValueUpdate = (key: string, value: number | null) => {
  emit('update:modelValue', {
    ...asMulti.value,
    values: {
      ...asMulti.value.values,
      [key]: value,
    },
  });
};

const onUnitUpdate = (unit: string) => emit('update:modelValue', { ...asMulti.value, unit });
</script>
