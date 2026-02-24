<script setup>
import InputSwitch from 'primevue/inputswitch';

defineProps({
  field: { type: Object, required: true },
  modelValue: { type: Object, default: null },
});
const emit = defineEmits(['update:modelValue']);

function setValue(v) {
  emit('update:modelValue', { type: 'BOOLEAN', value: v });
}
</script>

<template>
  <label class="font-semibold mb-1 block">{{ field.label }}</label>
  <InputSwitch :modelValue="Boolean(modelValue?.value)" @update:modelValue="setValue" />
</template>
