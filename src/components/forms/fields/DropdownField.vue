<script setup>
import Dropdown from 'primevue/dropdown';

const props = defineProps({
  field: { type: Object, required: true },
  modelValue: { type: Object, default: null },
});
const emit = defineEmits(['update:modelValue']);

function setValue(v) {
  emit('update:modelValue', { type: 'DROPDOWN', value: v ?? null });
}
</script>

<template>
  <label class="font-semibold mb-1 block">{{ field.label }}</label>
  <Dropdown
    class="w-full"
    :options="props.field.dropdownOptions ?? []"
    :modelValue="props.modelValue?.value ?? null"
    @update:modelValue="setValue"
  />
</template>
