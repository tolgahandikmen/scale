<template>
  <div class="field mb-3">
    <label class="font-medium block mb-2">
      {{ field.label }}
      <span v-if="field.required" class="text-red-500">*</span>
    </label>
    <Dropdown
      class="w-full"
      :options="field.dropdownOptions"
      :model-value="(modelValue as string) ?? ''"
      @update:model-value="onUpdate"
    />
  </div>
</template>

<script setup lang="ts">
import Dropdown from 'primevue/dropdown';
import type { FieldDefinition } from '@/models/template';

defineProps<{
  field: FieldDefinition;
  modelValue: unknown;
}>();

const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void;
}>();

const onUpdate = (value: string) => emit('update:modelValue', value);
</script>
