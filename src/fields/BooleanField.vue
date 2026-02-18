<template>
  <div class="field mb-3">
    <div class="flex align-items-center justify-content-between">
      <label class="font-medium block">{{ field.label }}</label>
      <InputSwitch :model-value="Boolean(modelValue)" @update:model-value="onUpdate" />
    </div>
  </div>
</template>

<script setup lang="ts">
import InputSwitch from 'primevue/inputswitch';
import type { FieldDefinition } from '@/models/template';

defineProps<{
  field: FieldDefinition;
  modelValue: unknown;
}>();

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void;
}>();

const onUpdate = (value: boolean) => emit('update:modelValue', value);
</script>
