<template>
  <div class="field mb-3">
    <label class="font-medium block mb-2">
      {{ field.label }}
      <span v-if="field.required" class="text-red-500">*</span>
    </label>
    <div class="flex gap-2 align-items-center">
      <InputNumber :model-value="numberValue" input-class="w-full" class="flex-1" @update:model-value="onNumberUpdate" />
      <Dropdown
        v-if="field.unitMode !== 'NONE'"
        class="w-8rem"
        :model-value="unitValue"
        :options="field.unitOptions"
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
import type { SheetValueNumber } from '@/models/sheet';

const props = defineProps<{
  field: FieldDefinition;
  modelValue: unknown;
}>();

const emit = defineEmits<{
  (e: 'update:modelValue', value: SheetValueNumber): void;
}>();

const parsed = computed<SheetValueNumber>(() => {
  if (typeof props.modelValue === 'object' && props.modelValue && 'value' in props.modelValue) {
    return props.modelValue as SheetValueNumber;
  }
  return { value: null };
});

const numberValue = computed(() => parsed.value.value ?? null);
const unitValue = computed(() => parsed.value.unit);

const onNumberUpdate = (value: number | null) => emit('update:modelValue', { ...parsed.value, value });
const onUnitUpdate = (unit: string) => emit('update:modelValue', { ...parsed.value, unit });
</script>
