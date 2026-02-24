<script setup>
import InputNumber from 'primevue/inputnumber';
import InputText from 'primevue/inputtext';
import Dropdown from 'primevue/dropdown';

const props = defineProps({
  field: { type: Object, required: true },
  modelValue: { type: Object, default: null },
});
const emit = defineEmits(['update:modelValue']);

const mv = () => props.modelValue ?? {};

function setPart(key, val) {
  emit('update:modelValue', {
    type: 'MULTI',
    value: { ...(mv().value ?? {}), [key]: val },
    unit: mv().unit ?? null,
  });
}
function setUnit(u) {
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
        :modelValue="(mv().value ?? {})[k] ?? null"
        @update:modelValue="(v) => setPart(k, v)"
        :useGrouping="false"
      />
      <InputText
        v-else
        :modelValue="(mv().value ?? {})[k] ?? ''"
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
