<template>
  <div class="field mb-3">
    <label class="font-medium block mb-2">
      {{ field.label }}
      <span v-if="field.required" class="text-red-500">*</span>
    </label>
    <Calendar
      class="w-full"
      date-format="yy-mm-dd"
      :manual-input="false"
      :model-value="dateValue"
      @update:model-value="onUpdate"
    />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import Calendar from 'primevue/calendar';
import type { FieldDefinition } from '@/models/template';

const props = defineProps<{
  field: FieldDefinition;
  modelValue: unknown;
}>();

const emit = defineEmits<{
  (e: 'update:modelValue', value: string | null): void;
}>();

const dateValue = computed<Date | null>(() => {
  if (typeof props.modelValue !== 'string' || !props.modelValue) return null;
  const parsed = new Date(props.modelValue);
  return Number.isNaN(parsed.getTime()) ? null : parsed;
});

const onUpdate = (value: Date | Date[] | (Date | null)[] | null | undefined) => {
  if (value instanceof Date) {
    const yyyy = value.getFullYear();
    const mm = String(value.getMonth() + 1).padStart(2, '0');
    const dd = String(value.getDate()).padStart(2, '0');
    emit('update:modelValue', `${yyyy}-${mm}-${dd}`);
    return;
  }
  emit('update:modelValue', null);
};
</script>
