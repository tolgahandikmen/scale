<script setup lang="ts">
import InputSwitch from 'primevue/inputswitch';
import type { FieldDefinition, FieldValuePrimitive } from '@/models/form';

defineProps<{ field: FieldDefinition; modelValue: FieldValuePrimitive }>();
const emit = defineEmits<{ (e: 'update:modelValue', v: FieldValuePrimitive): void }>();

function setValue(v: boolean) {
  emit('update:modelValue', { type: 'BOOLEAN', value: v });
}
</script>

<template>
  <label class="font-semibold mb-1 block">{{ field.label }}</label>
  <InputSwitch :modelValue="Boolean((modelValue as any).value)" @update:modelValue="setValue" />
</template>
