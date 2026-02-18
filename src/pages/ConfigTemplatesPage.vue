<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import TabView from 'primevue/tabview';
import TabPanel from 'primevue/tabpanel';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import Button from 'primevue/button';
import Dropdown from 'primevue/dropdown';
import InputText from 'primevue/inputtext';
import Checkbox from 'primevue/checkbox';
import Divider from 'primevue/divider';
import Dialog from 'primevue/dialog';

import type { FieldDefinition, SheetTemplate, TemplateKind } from '@/models/form';
import { getTemplates, getTemplateFields, createTemplateVersion, saveFieldsAsNewVersion } from '@/api/templatesApi';
import TableSchemaEditor from '@/components/forms/config/TableSchemaEditor.vue';

const activeKind = ref<TemplateKind>('INPUT');
const activeIndex = computed({
  get: () => (activeKind.value === 'INPUT' ? 0 : 1),
  set: (i: number) => {
    activeKind.value = i === 0 ? 'INPUT' : 'OUTPUT';
  },
});

const templates = ref<SheetTemplate[]>([]);
const selectedTemplate = ref<SheetTemplate | null>(null);

const fields = ref<FieldDefinition[]>([]);
const selectedField = ref<FieldDefinition | null>(null);
const dirty = ref(false);

const showNewTemplate = ref(false);
const newTemplateName = ref('');
const newTemplateCode = ref('');

const fieldTypeOptions = ['TEXT', 'NUMBER', 'DATE', 'DROPDOWN', 'BOOLEAN', 'TABLE'];

const duplicateKeys = computed(() => {
  const map = new Map<string, number>();
  for (const f of fields.value) {
    const key = f.key?.trim();
    if (!key) continue;
    map.set(key, (map.get(key) ?? 0) + 1);
  }
  return new Set([...map.entries()].filter(([, count]) => count > 1).map(([key]) => key));
});

const hasDuplicateKeys = computed(() => duplicateKeys.value.size > 0);

async function loadTemplates() {
  templates.value = await getTemplates(activeKind.value);
  selectedTemplate.value = templates.value[0] ?? null;
}

async function loadFields() {
  dirty.value = false;
  if (!selectedTemplate.value) {
    fields.value = [];
    selectedField.value = null;
    return;
  }
  const data = await getTemplateFields(selectedTemplate.value.id);
  fields.value = data.sort((a, b) => (a.orderNo ?? 0) - (b.orderNo ?? 0));
  selectedField.value = fields.value[0] ?? null;
}

onMounted(async () => {
  await loadTemplates();
  await loadFields();
});

watch(activeKind, async () => {
  selectedTemplate.value = null;
  selectedField.value = null;
  await loadTemplates();
  await loadFields();
});

watch(selectedTemplate, async () => {
  selectedField.value = null;
  await loadFields();
});

function markDirty() {
  dirty.value = true;
}

function addField() {
  if (!selectedTemplate.value) return;
  const maxOrder = Math.max(0, ...fields.value.map((f) => f.orderNo ?? 0));
  fields.value.push({
    id: -(Date.now()),
    templateId: selectedTemplate.value.id,
    key: `new_field_${fields.value.length + 1}`,
    label: 'New Field',
    type: 'TEXT',
    required: false,
    orderNo: maxOrder + 10,
    groupKey: '',
    isMulti: false,
    multiKeys: [],
    unitMode: 'NONE',
    unitOptions: [],
    dropdownOptions: [],
  });
  selectedField.value = fields.value[fields.value.length - 1];
  markDirty();
}

function removeField(fd: FieldDefinition) {
  const idx = fields.value.indexOf(fd);
  fields.value = fields.value.filter((f) => f !== fd);
  if (selectedField.value?.id === fd.id) {
    selectedField.value = fields.value[Math.min(idx, fields.value.length - 1)] ?? null;
  }
  markDirty();
}

function move(fd: FieldDefinition, dir: -1 | 1) {
  const idx = fields.value.indexOf(fd);
  const j = idx + dir;
  if (j < 0 || j >= fields.value.length) return;
  const tmp = fields.value[idx];
  fields.value[idx] = fields.value[j];
  fields.value[j] = tmp;
  fields.value.forEach((f, i) => (f.orderNo = (i + 1) * 10));
  selectedField.value = fd;
  markDirty();
}

function ensureTableSchema(fd: FieldDefinition) {
  if (fd.type !== 'TABLE') return;
  if (!fd.tableSchema) {
    fd.tableSchema = {
      rowMode: 'CONFIGURABLE',
      defaultRows: ['0', '20', '40', '60', '80', '100', '80', '60', '40', '20', '0'],
      columns: [
        { key: 'percent', label: '%', type: 'NUMBER', unitMode: 'NONE' },
        { key: 'kn', label: 'kN', type: 'NUMBER', unitMode: 'OPTIONAL', unitOptions: ['kN'] },
        { key: 'mv_v', label: 'mV/V', type: 'NUMBER', unitMode: 'OPTIONAL', unitOptions: ['mV/V'] },
      ],
    };
  }
}

async function saveAsNewVersion() {
  if (!selectedTemplate.value || hasDuplicateKeys.value) return;
  fields.value.forEach((f, i) => (f.orderNo = (i + 1) * 10));
  const res = await saveFieldsAsNewVersion({ templateId: selectedTemplate.value.id, fields: fields.value });
  alert(`Saved as new version: ${res.name} (v${res.version})`);
  await loadTemplates();
  await loadFields();
}

async function createNewTemplate() {
  const name = newTemplateName.value.trim();
  const code = newTemplateCode.value.trim();
  if (!name || !code) return alert('Name & code required');
  await createTemplateVersion({ name, code, kind: activeKind.value });
  showNewTemplate.value = false;
  newTemplateName.value = '';
  newTemplateCode.value = '';
  await loadTemplates();
}
</script>

<template>
  <div class="p-4">
    <div class="flex align-items-center justify-content-between mb-3">
      <h2 class="m-0">Configuration</h2>
      <div class="flex gap-2">
        <Button label="New Template" icon="pi pi-plus" @click="showNewTemplate = true" />
        <Button
          label="Save as New Version"
          icon="pi pi-save"
          :disabled="!dirty || !selectedTemplate || hasDuplicateKeys"
          @click="saveAsNewVersion"
        />
      </div>
    </div>

    <TabView v-model:activeIndex="activeIndex">
      <TabPanel header="INPUT Templates" />
      <TabPanel header="OUTPUT Templates" />
    </TabView>

    <div class="grid mt-2">
      <div class="col-12 md:col-4">
        <div class="surface-card border-1 border-round p-3">
          <div class="font-semibold mb-2">Templates</div>
          <div v-if="templates.length === 0" class="p-text-secondary">No templates.</div>

          <div
            v-for="t in templates"
            :key="t.id"
            class="p-2 border-1 border-round mb-2 cursor-pointer"
            :class="{ 'surface-200': selectedTemplate?.id === t.id }"
            @click="selectedTemplate = t"
          >
            <div class="flex justify-content-between">
              <div class="font-medium">{{ t.name }}</div>
              <small class="p-text-secondary">v{{ t.version }}</small>
            </div>
            <small class="p-text-secondary">{{ t.code }}</small>
          </div>
        </div>
      </div>

      <div class="col-12 md:col-8">
        <div class="surface-card border-1 border-round p-3">
          <div class="flex align-items-center justify-content-between mb-2">
            <div class="font-semibold">Fields</div>
            <Button label="Add Field" icon="pi pi-plus" size="small" :disabled="!selectedTemplate" @click="addField" />
          </div>

          <div v-if="!selectedTemplate" class="p-text-secondary">Select a template to edit fields.</div>

          <DataTable
            v-else
            :value="fields"
            dataKey="id"
            size="small"
            class="p-datatable-sm"
            selectionMode="single"
            v-model:selection="selectedField"
          >
            <Column header="#" style="width: 4rem">
              <template #body="{ index }">
                <div class="flex gap-1">
                  <Button icon="pi pi-chevron-up" text @click="move(fields[index], -1)" />
                  <Button icon="pi pi-chevron-down" text @click="move(fields[index], 1)" />
                </div>
              </template>
            </Column>

            <Column header="Key" style="width: 14rem">
              <template #body="{ data }">
                <InputText
                  v-model="data.key"
                  class="w-full"
                  :class="{ 'p-invalid': duplicateKeys.has(data.key) }"
                  @update:modelValue="markDirty"
                />
              </template>
            </Column>

            <Column header="Label">
              <template #body="{ data }">
                <InputText v-model="data.label" class="w-full" @update:modelValue="markDirty" />
              </template>
            </Column>

            <Column header="Type" style="width: 10rem">
              <template #body="{ data }">
                <Dropdown
                  class="w-full"
                  :options="fieldTypeOptions"
                  v-model="data.type"
                  @update:modelValue="(v)=>{ data.type=v; ensureTableSchema(data); markDirty(); }"
                />
              </template>
            </Column>

            <Column header="Required" style="width: 7rem">
              <template #body="{ data }">
                <Checkbox v-model="data.required" binary @update:modelValue="markDirty" />
              </template>
            </Column>

            <Column header="Group" style="width: 10rem">
              <template #body="{ data }">
                <InputText v-model="data.groupKey" class="w-full" @update:modelValue="markDirty" />
              </template>
            </Column>

            <Column header="" style="width: 6rem">
              <template #body="{ data }">
                <Button icon="pi pi-trash" severity="danger" text @click="removeField(data)" />
              </template>
            </Column>
          </DataTable>

          <small v-if="hasDuplicateKeys" class="p-error block mt-2">Field keys must be unique within the same template.</small>

          <Divider />

          <div class="mt-2">
            <div class="font-semibold mb-2">Advanced</div>
            <div v-if="!selectedField" class="p-text-secondary">Select a field row to edit advanced settings.</div>

            <div v-else class="p-3 border-1 border-round">
              <div class="font-medium mb-3">
                {{ selectedField.label }} <span class="p-text-secondary">({{ selectedField.type }})</span>
              </div>

              <div v-if="selectedField.type === 'DROPDOWN'" class="mb-3">
                <label class="font-semibold mb-1 block">Dropdown Options (comma separated)</label>
                <InputText
                  class="w-full"
                  :modelValue="(selectedField.dropdownOptions ?? []).join(',')"
                  @update:modelValue="(v)=>{ selectedField!.dropdownOptions = (v ?? '').split(',').map(s=>s.trim()).filter(Boolean); markDirty(); }"
                />
              </div>

              <div v-if="selectedField.type === 'NUMBER' || selectedField.isMulti" class="mb-3">
                <label class="font-semibold mb-1 block">Unit Mode</label>
                <Dropdown
                  class="w-15rem"
                  :options="['NONE','OPTIONAL','REQUIRED']"
                  v-model="selectedField.unitMode"
                  @update:modelValue="markDirty"
                />

                <label class="font-semibold mt-2 mb-1 block">Unit Options (comma separated)</label>
                <InputText
                  class="w-full"
                  :modelValue="(selectedField.unitOptions ?? []).join(',')"
                  @update:modelValue="(v)=>{ selectedField!.unitOptions = (v ?? '').split(',').map(s=>s.trim()).filter(Boolean); markDirty(); }"
                />
              </div>

              <div class="mb-3">
                <label class="font-semibold mb-1 block">Is Multi (from/to etc.)</label>
                <Checkbox v-model="selectedField.isMulti" binary @update:modelValue="markDirty" />

                <div v-if="selectedField.isMulti" class="mt-2">
                  <label class="font-semibold mb-1 block">Multi Keys (comma separated)</label>
                  <InputText
                    class="w-full"
                    :modelValue="(selectedField.multiKeys ?? []).join(',')"
                    @update:modelValue="(v)=>{ selectedField!.multiKeys = (v ?? '').split(',').map(s=>s.trim()).filter(Boolean); markDirty(); }"
                  />
                </div>
              </div>

              <div v-if="selectedField.type === 'TABLE'">
                <div v-if="!selectedField.tableSchema" class="mb-2">
                  <Button label="Init Table Schema" icon="pi pi-wrench" size="small" @click="() => { ensureTableSchema(selectedField!); markDirty(); }" />
                </div>

                <TableSchemaEditor
                  v-if="selectedField.tableSchema"
                  :schema="selectedField.tableSchema"
                  @update:schema="(s)=>{ selectedField!.tableSchema = s; markDirty(); }"
                />
              </div>
            </div>
          </div>

          <small v-if="dirty" class="p-text-secondary block mt-3">You have unsaved changes. Use “Save as New Version”.</small>
        </div>
      </div>
    </div>

    <Dialog v-model:visible="showNewTemplate" modal header="New Template" :style="{ width: '36rem' }">
      <div class="grid">
        <div class="col-12">
          <label class="font-semibold mb-1 block">Code</label>
          <InputText v-model="newTemplateCode" class="w-full" placeholder="OUTPUT_FZ_POSITIVE" />
        </div>
        <div class="col-12">
          <label class="font-semibold mb-1 block">Name</label>
          <InputText v-model="newTemplateName" class="w-full" placeholder="Fz Output (Positive Loads)" />
        </div>
      </div>

      <template #footer>
        <Button label="Cancel" severity="secondary" @click="showNewTemplate = false" />
        <Button label="Create" @click="createNewTemplate" />
      </template>
    </Dialog>
  </div>
</template>

