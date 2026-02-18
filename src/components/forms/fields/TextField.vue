<script setup lang="ts">
import InputText from 'primevue/inputtext';
import type { FieldDefinition, FieldValuePrimitive } from '@/models/form';

const props = defineProps<{ field: FieldDefinition; modelValue: FieldValuePrimitive }>();
const emit = defineEmits<{ (e: 'update:modelValue', v: FieldValuePrimitive): void }>();

function setValue(v: string | undefined) {
  emit('update:modelValue', { type: 'TEXT', value: v ?? '' });
}
</script>

<template>
  <label class="font-semibold mb-1 block">{{ field.label }}</label>
  <InputText
    :modelValue="(props.modelValue as any).value ?? ''"
    @update:modelValue="setValue"
    :class="{ 'p-invalid': field.required && !((props.modelValue as any).value) }"
  />
</template>
