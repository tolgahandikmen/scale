<script setup lang="ts">
import Calendar from 'primevue/calendar';
import type { FieldDefinition, FieldValuePrimitive } from '@/models/form';

const props = defineProps<{ field: FieldDefinition; modelValue: FieldValuePrimitive }>();
const emit = defineEmits<{ (e: 'update:modelValue', v: FieldValuePrimitive): void }>();

function getDate() {
  const raw = (props.modelValue as any)?.value;
  if (!raw) return null;
  const d = new Date(raw);
  return Number.isNaN(d.getTime()) ? null : d;
}

function setValue(v: Date | Date[] | (Date | null)[] | null | undefined) {
  if (v instanceof Date) {
    emit('update:modelValue', { type: 'DATE', value: v.toISOString().slice(0, 10) });
    return;
  }
  emit('update:modelValue', { type: 'DATE', value: null });
}
</script>

<template>
  <label class="font-semibold mb-1 block">{{ field.label }}</label>
  <Calendar :modelValue="getDate()" @update:modelValue="setValue" dateFormat="yy-mm-dd" showIcon class="w-full" />
</template>
