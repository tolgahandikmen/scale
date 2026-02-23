<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import MultiSelect from 'primevue/multiselect';
import Button from 'primevue/button';
import Tag from 'primevue/tag';

import scaleService from '@/services/ScaleService';


import type { PartTemplateMapping, SheetTemplate } from '@/models/form';

type RowVm = {
  partId: string;
  inputTemplateIds: number[];
  outputTemplateIds: number[];
  updatedAt: string;
};

const inputTemplates = ref<SheetTemplate[]>([]);
const outputTemplates = ref<SheetTemplate[]>([]);
const rows = ref<RowVm[]>([]);

const templateNameMap = computed<Record<number, string>>(() => {
  const map: Record<number, string> = {};
  [...inputTemplates.value, ...outputTemplates.value].forEach((t) => {
    map[t.id] = `${t.name} (v${t.version})`;
  });
  return map;
});

async function load() {
  const [ins, outs, parts, currentMappings] = await Promise.all([
    scaleService.getTemplates('INPUT'),
    scaleService.getTemplates('OUTPUT'),
    scaleService.listPartIds(),
    scaleService.listPartTemplateMappings(),
  ]);

  inputTemplates.value = ins;
  outputTemplates.value = outs;

  const mappingByPart = currentMappings.reduce<Record<string, PartTemplateMapping>>((acc, cur) => {
    acc[cur.partId] = cur;
    return acc;
  }, {});

  rows.value = parts.map((partId) => ({
    partId,
    inputTemplateIds: mappingByPart[partId]?.inputTemplateIds ?? [],
    outputTemplateIds: mappingByPart[partId]?.outputTemplateIds ?? [],
    updatedAt: mappingByPart[partId]?.updatedAt ?? '-',
  }));
}

async function saveRow(row: RowVm) {
  const saved = await scaleService.savePartTemplateMapping({
    partId: row.partId,
    inputTemplateIds: row.inputTemplateIds,
    outputTemplateIds: row.outputTemplateIds,
  });
  row.updatedAt = saved.updatedAt;
  alert(`Saved mapping for ${row.partId}`);
}

onMounted(load);
</script>

<template>
  <div class="p-4">
    <div class="flex align-items-center justify-content-between mb-3">
      <div>
        <h2 class="m-0">Part-Template Mapping</h2>
        <small class="p-text-secondary">Define which INPUT/OUTPUT templates are allowed per Part ID.</small>
      </div>
    </div>

    <div class="surface-card border-1 border-round p-3">
      <DataTable :value="rows" dataKey="partId" responsiveLayout="scroll" class="p-datatable-sm">
        <Column field="partId" header="Part ID" style="width: 12rem">
          <template #body="{ data }">
            <Tag :value="data.partId" severity="info" />
          </template>
        </Column>

        <Column header="Allowed INPUT Templates">
          <template #body="{ data }">
            <MultiSelect
              class="w-full"
              :options="inputTemplates"
              optionLabel="name"
              optionValue="id"
              v-model="data.inputTemplateIds"
              display="chip"
              :maxSelectedLabels="2"
              placeholder="Select input templates"
            />
          </template>
        </Column>

        <Column header="Allowed OUTPUT Templates">
          <template #body="{ data }">
            <MultiSelect
              class="w-full"
              :options="outputTemplates"
              optionLabel="name"
              optionValue="id"
              v-model="data.outputTemplateIds"
              display="chip"
              :maxSelectedLabels="2"
              placeholder="Select output templates"
            />
          </template>
        </Column>

        <Column header="Preview" style="width: 26rem">
          <template #body="{ data }">
            <div class="text-sm">
              <div>
                <strong>IN:</strong>
                {{ data.inputTemplateIds.map((id: number) => templateNameMap[id] ?? id).join(', ') || '-' }}
              </div>
              <div>
                <strong>OUT:</strong>
                {{ data.outputTemplateIds.map((id: number) => templateNameMap[id] ?? id).join(', ') || '-' }}
              </div>
            </div>
          </template>
        </Column>

        <Column header="Updated" style="width: 14rem">
          <template #body="{ data }">
            <small>{{ data.updatedAt === '-' ? '-' : new Date(data.updatedAt).toLocaleString() }}</small>
          </template>
        </Column>

        <Column header="" style="width: 8rem">
          <template #body="{ data }">
            <Button size="small" label="Save" icon="pi pi-save" @click="saveRow(data)" />
          </template>
        </Column>
      </DataTable>
    </div>
  </div>
</template>

