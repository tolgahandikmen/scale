<template>
  <form class="grid" @submit.prevent="submit">
    <div v-for="field in orderedFields" :key="field.id" class="col-12">
      <FieldRenderer
        :field="field"
        :model-value="localValues[field.key]"
        @update:model-value="(value) => updateValue(field.key, value as SheetValue)"
      />
    </div>

    <div class="col-12 flex justify-content-end">
      <Button type="submit" label="Save" />
    </div>
  </form>
</template>

<script setup lang="ts">
import { computed, reactive, watch } from 'vue';
import Button from 'primevue/button';

import FieldRenderer from './FieldRenderer.vue';
import { buildInitialValue } from './valueBuilders';

import type { FieldDefinition } from '@/models/template';
import type { SheetValue } from '@/models/sheet';

const props = defineProps<{
  fields: FieldDefinition[];
  modelValue?: Record<string, SheetValue>;
}>();

const emit = defineEmits<{
  (e: 'update:modelValue', value: Record<string, SheetValue>): void;
  (e: 'submit', value: Record<string, SheetValue>): void;
}>();

const orderedFields = computed(() => [...props.fields].sort((a, b) => a.orderNo - b.orderNo));

const localValues = reactive<Record<string, SheetValue>>({});

const hydrate = () => {
  orderedFields.value.forEach((field) => {
    localValues[field.key] = props.modelValue?.[field.key] ?? buildInitialValue(field);
  });
};

watch(() => props.fields, hydrate, { immediate: true, deep: true });
watch(
  () => props.modelValue,
  (val) => {
    if (val) {
      Object.keys(val).forEach((k) => {
        localValues[k] = val[k];
      });
    }
  },
  { deep: true },
);

const updateValue = (key: string, value: SheetValue) => {
  localValues[key] = value;
  emit('update:modelValue', { ...localValues });
};

const submit = () => {
  emit('submit', { ...localValues });
};
</script>
