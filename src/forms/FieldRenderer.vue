<template>
  <component
    :is="componentName"
    :field="field"
    :model-value="modelValue"
    @update:model-value="(value: unknown) => emit('update:modelValue', value)"
  />
</template>

<script setup lang="ts">
import { computed } from 'vue';

import TextField from '@/fields/TextField.vue';
import NumberField from '@/fields/NumberField.vue';
import DateField from '@/fields/DateField.vue';
import BooleanField from '@/fields/BooleanField.vue';
import DropdownField from '@/fields/DropdownField.vue';
import MultiField from '@/fields/MultiField.vue';
import TableField from '@/fields/TableField.vue';

import type { FieldDefinition } from '@/models/template';

const props = defineProps<{
  field: FieldDefinition;
  modelValue: unknown;
}>();

const emit = defineEmits<{
  (e: 'update:modelValue', value: unknown): void;
}>();

const componentName = computed(() => {
  if (props.field.type === 'NUMBER' && props.field.isMulti) {
    return MultiField;
  }

  switch (props.field.type) {
    case 'TEXT':
      return TextField;
    case 'NUMBER':
      return NumberField;
    case 'DATE':
      return DateField;
    case 'BOOLEAN':
      return BooleanField;
    case 'DROPDOWN':
      return DropdownField;
    case 'TABLE':
      return TableField;
    default:
      return TextField;
  }
});
</script>
