<script setup>
import TextField from './fields/TextField.vue';
import NumberField from './fields/NumberField.vue';
import DateField from './fields/DateField.vue';
import BooleanField from './fields/BooleanField.vue';
import DropdownField from './fields/DropdownField.vue';
import MultiField from './fields/MultiField.vue';
import TableField from './fields/TableField.vue';

defineProps({
  field: { type: Object, required: true },
  modelValue: { type: Object, default: null },
});

const emit = defineEmits(['update:modelValue']);
</script>

<template>
  <MultiField
    v-if="field.isMulti"
    :field="field"
    :modelValue="modelValue"
    @update:modelValue="emit('update:modelValue', $event)"
  />

  <TextField
    v-else-if="field.type === 'TEXT'"
    :field="field"
    :modelValue="modelValue"
    @update:modelValue="emit('update:modelValue', $event)"
  />

  <NumberField
    v-else-if="field.type === 'NUMBER'"
    :field="field"
    :modelValue="modelValue"
    @update:modelValue="emit('update:modelValue', $event)"
  />

  <DateField
    v-else-if="field.type === 'DATE'"
    :field="field"
    :modelValue="modelValue"
    @update:modelValue="emit('update:modelValue', $event)"
  />

  <BooleanField
    v-else-if="field.type === 'BOOLEAN'"
    :field="field"
    :modelValue="modelValue"
    @update:modelValue="emit('update:modelValue', $event)"
  />

  <DropdownField
    v-else-if="field.type === 'DROPDOWN'"
    :field="field"
    :modelValue="modelValue"
    @update:modelValue="emit('update:modelValue', $event)"
  />

  <TableField
    v-else-if="field.type === 'TABLE'"
    :field="field"
    :modelValue="modelValue"
    @update:modelValue="emit('update:modelValue', $event)"
  />

  <div v-else class="p-text-secondary">Unsupported field type: {{ field.type }}</div>
</template>
