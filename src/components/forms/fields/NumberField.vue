<script setup>
import InputNumber from 'primevue/inputnumber';
import Dropdown from 'primevue/dropdown';

const props = defineProps({
  field: { type: Object, required: true },
  modelValue: { type: Object, default: null },
});
const emit = defineEmits(['update:modelValue']);

const mv = () => props.modelValue ?? {};

function setNumber(v) {
  emit('update:modelValue', { type: 'NUMBER', value: v, unit: mv().unit ?? null });
}
function setUnit(u) {
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
