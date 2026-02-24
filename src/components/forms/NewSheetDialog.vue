<script setup>
import { watch, ref, computed } from 'vue';
import Dialog from 'primevue/dialog';
import Dropdown from 'primevue/dropdown';
import Button from 'primevue/button';
import Calendar from 'primevue/calendar';
import DynamicForm from './DynamicForm.vue';
import scaleService from '@/services/ScaleService';

const props = defineProps({
  visible: { type: Boolean, default: false },
  kind: { type: String, required: true },
  itemId: { type: Number, required: true },
  parentSheetId: { type: Number, default: undefined },
  allowedTemplateIds: { type: Array, default: () => [] },
});

const emit = defineEmits(['update:visible', 'saved']);

const templates = ref([]);
const selectedTemplate = ref(null);
const fields = ref([]);
const values = ref({});
const sheetDate = ref(new Date());

const header = computed(() => (props.kind === 'INPUT' ? 'New Input Sheet' : 'New Output Sheet'));
const filteredTemplates = computed(() => {
  if (!props.allowedTemplateIds?.length) return templates.value;
  const allowSet = new Set(props.allowedTemplateIds);
  return templates.value.filter((t) => allowSet.has(t.id));
});

watch(
  () => props.visible,
  async (v) => {
    if (!v) return;
    templates.value = await scaleService.getTemplates(props.kind);
    selectedTemplate.value = filteredTemplates.value[0] ?? null;
  },
);

watch(
  () => props.allowedTemplateIds,
  () => {
    if (!selectedTemplate.value) return;
    if (!filteredTemplates.value.find((x) => x.id === selectedTemplate.value?.id)) {
      selectedTemplate.value = filteredTemplates.value[0] ?? null;
    }
  },
  { deep: true },
);

watch(
  () => selectedTemplate.value?.id,
  async (id) => {
    if (!id) return;
    fields.value = await scaleService.getTemplateFields(id);
    values.value = {};
  },
);

async function save() {
  if (!selectedTemplate.value) return;

  const payload = {
    itemId: props.itemId,
    templateId: selectedTemplate.value.id,
    sheetDate: sheetDate.value ? sheetDate.value.toISOString().slice(0, 10) : undefined,
    values: values.value,
  };

  if (props.kind === 'OUTPUT') {
    if (!props.parentSheetId) throw new Error('parentSheetId required for OUTPUT');
    payload.parentSheetId = props.parentSheetId;
    payload.outputTemplateId = selectedTemplate.value.id;
  }

  try {
    await scaleService.createSheet(payload);
    emit('saved');
    emit('update:visible', false);
  } catch (e) {
    const status = e?.response?.status;
    if (status === 409) alert('This output format already exists for this input sheet.');
    else alert('Save failed.');
  }
}

function close() {
  emit('update:visible', false);
}
</script>

<template>
  <Dialog :visible="visible" modal :header="header" :style="{ width: '60rem' }" @update:visible="emit('update:visible', $event)">
    <div class="grid">
      <div class="col-12 md:col-6">
        <label class="font-semibold mb-1 block">Template / Format</label>
        <Dropdown class="w-full" :options="filteredTemplates" optionLabel="name" v-model="selectedTemplate" />
      </div>
      <div class="col-12 md:col-6">
        <label class="font-semibold mb-1 block">Sheet Date</label>
        <Calendar class="w-full" v-model="sheetDate" dateFormat="yy-mm-dd" showIcon />
      </div>

      <div v-if="!filteredTemplates.length" class="col-12 p-text-secondary">
        No allowed {{ kind === 'INPUT' ? 'input' : 'output' }} templates configured for this part.
      </div>

      <div class="col-12" v-else>
        <DynamicForm v-if="fields.length" :fields="fields" v-model="values" />
      </div>
    </div>

    <template #footer>
      <Button label="Cancel" severity="secondary" @click="close" />
      <Button label="Save" @click="save" />
    </template>
  </Dialog>
</template>
