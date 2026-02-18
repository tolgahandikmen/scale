<template>
  <div class="field mb-3">
    <label class="font-medium block mb-2">
      {{ field.label }}
      <span v-if="field.required" class="text-red-500">*</span>
    </label>
    <InputText class="w-full" :model-value="(modelValue as string) ?? ''" @update:model-value="update" />
  </div>
</template>

<script setup lang="ts">
import InputText from 'primevue/inputtext';
import type { FieldDefinition } from '@/models/template';

defineProps<{
  field: FieldDefinition;
  modelValue: unknown;
}>();

const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void;
}>();

const update = (value: string | undefined) => emit('update:modelValue', value ?? '');
</script>
