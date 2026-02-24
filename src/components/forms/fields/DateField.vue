<script setup>
import Calendar from 'primevue/calendar';

const props = defineProps({
  field: { type: Object, required: true },
  modelValue: { type: Object, default: null },
});
const emit = defineEmits(['update:modelValue']);

function getDate() {
  const raw = props.modelValue?.value;
  if (!raw) return null;
  const d = new Date(raw);
  return Number.isNaN(d.getTime()) ? null : d;
}

function setValue(v) {
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
