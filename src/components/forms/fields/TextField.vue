<script setup>
import InputText from 'primevue/inputtext';

const props = defineProps({
  field: { type: Object, required: true },
  modelValue: { type: Object, default: null },
});
const emit = defineEmits(['update:modelValue']);

function setValue(v) {
  emit('update:modelValue', { type: 'TEXT', value: v ?? '' });
}
</script>

<template>
  <label class="font-semibold mb-1 block">{{ field.label }}</label>
  <InputText
    :modelValue="props.modelValue?.value ?? ''"
    @update:modelValue="setValue"
    :class="{ 'p-invalid': field.required && !(props.modelValue?.value) }"
  />
</template>
