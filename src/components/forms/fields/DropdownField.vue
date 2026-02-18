<script setup lang="ts">
import Dropdown from 'primevue/dropdown';
import type { FieldDefinition, FieldValuePrimitive } from '@/models/form';

const props = defineProps<{ field: FieldDefinition; modelValue: FieldValuePrimitive }>();
const emit = defineEmits<{ (e: 'update:modelValue', v: FieldValuePrimitive): void }>();

function setValue(v: string | null | undefined) {
  emit('update:modelValue', { type: 'DROPDOWN', value: v ?? null });
}
</script>

<template>
  <label class="font-semibold mb-1 block">{{ field.label }}</label>
  <Dropdown
    class="w-full"
    :options="props.field.dropdownOptions ?? []"
    :modelValue="(props.modelValue as any).value ?? null"
    @update:modelValue="setValue"
  />
</template>
