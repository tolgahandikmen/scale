<script setup lang="ts">
import InputNumber from 'primevue/inputnumber';
import InputText from 'primevue/inputtext';
import Dropdown from 'primevue/dropdown';
import type { FieldDefinition, FieldValuePrimitive } from '@/models/form';

const props = defineProps<{ field: FieldDefinition; modelValue: FieldValuePrimitive }>();
const emit = defineEmits<{ (e: 'update:modelValue', v: FieldValuePrimitive): void }>();

const mv = () => props.modelValue as { value?: Record<string, string | number | null>; unit?: string | null };

function setPart(key: string, val: string | number | null) {
  emit('update:modelValue', {
    type: 'MULTI',
    value: { ...(mv().value ?? {}), [key]: val },
    unit: mv().unit ?? null,
  });
}
function setUnit(u: string | null | undefined) {
  emit('update:modelValue', { type: 'MULTI', value: mv().value ?? {}, unit: u ?? null });
}

function isNumericMulti() {
  return true;
}
</script>

<template>
  <label class="font-semibold mb-1 block">{{ field.label }}</label>

  <div class="flex gap-2 align-items-center flex-wrap">
    <div v-for="k in field.multiKeys ?? []" :key="k" class="flex flex-column">
      <small class="p-text-secondary">{{ k }}</small>

      <InputNumber
        v-if="isNumericMulti()"
        :modelValue="(mv().value ?? {})[k] as number | null"
        @update:modelValue="(v) => setPart(k, v)"
        :useGrouping="false"
      />
      <InputText
        v-else
        :modelValue="((mv().value ?? {})[k] as string) ?? ''"
        @update:modelValue="(v) => setPart(k, v ?? '')"
      />
    </div>

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
