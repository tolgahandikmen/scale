<script setup lang="ts">
import InputNumber from 'primevue/inputnumber';
import Dropdown from 'primevue/dropdown';
import type { FieldDefinition, FieldValuePrimitive } from '@/models/form';

const props = defineProps<{ field: FieldDefinition; modelValue: FieldValuePrimitive }>();
const emit = defineEmits<{ (e: 'update:modelValue', v: FieldValuePrimitive): void }>();

const mv = () => props.modelValue as { value?: number | null; unit?: string | null };

function setNumber(v: number | null) {
  emit('update:modelValue', { type: 'NUMBER', value: v, unit: mv().unit ?? null });
}
function setUnit(u: string | null | undefined) {
  emit('update:modelValue', { type: 'NUMBER', value: mv().value ?? null, unit: u ?? null });
}
</script>

<template>
  <label class="font-semibold mb-1 block">{{ field.label }}</label>

  <div class="flex gap-2 align-items-center">
    <InputNumber class="flex-1" :modelValue="mv().value ?? null" @update:modelValue="setNumber" :useGrouping="false" />

    <Dropdown
      v-if="field.unitMode && field.unitMode !== 'NONE'"
      class="w-10rem"
      :options="field.unitOptions ?? []"
      :modelValue="mv().unit ?? null"
      @update:modelValue="setUnit"
      placeholder="Unit"
    />
  </div>
</template>
