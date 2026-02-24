<script setup>
import { computed, ref, watch } from 'vue';
import Button from 'primevue/button';

import ItemTree from '@/components/tree/ItemTree.vue';
import SheetList from '@/components/forms/SheetList.vue';
import NewSheetDialog from '@/components/forms/NewSheetDialog.vue';
import SheetDocumentPreview from '@/components/forms/SheetDocumentPreview.vue';

import scaleService from '@/services/ScaleService';

const selectedItemId = ref(null);
const selectedPartId = ref(null);

const inputSheets = ref([]);
const historicalRecords = ref([]);
const selectedRecordInputId = ref(null);
const selectedOutputId = ref(null);

const selectedInputFields = ref([]);
const selectedInputValues = ref({});
const selectedOutputFields = ref([]);
const selectedOutputValues = ref({});

const templateLookup = ref({});
const allowedInputTemplateIds = ref([]);
const allowedOutputTemplateIds = ref([]);

const showNewInput = ref(false);
const showNewOutput = ref(false);

const templateNames = computed(() => {
  const map = {};
  Object.values(templateLookup.value).forEach((t) => {
    map[t.id] = `${t.name} (v${t.version})`;
  });
  return map;
});

const selectedRecord = computed(() => {
  if (!selectedRecordInputId.value) return null;
  return historicalRecords.value.find((r) => r.input.id === selectedRecordInputId.value) ?? null;
});

const selectedInput = computed(() => selectedRecord.value?.input ?? null);

const selectedInputTemplateName = computed(() => {
  if (!selectedInput.value) return 'Input Sheet';
  return templateNames.value[selectedInput.value.templateId] ?? `Template ${selectedInput.value.templateId}`;
});

const selectedOutput = computed(() => {
  if (!selectedOutputId.value || !selectedRecord.value) return null;
  return selectedRecord.value.outputs.find((o) => o.id === selectedOutputId.value) ?? null;
});

const selectedOutputTemplateName = computed(() => {
  if (!selectedOutput.value) return 'Output Sheet';
  return templateNames.value[selectedOutput.value.templateId] ?? `Template ${selectedOutput.value.templateId}`;
});

const canCreate = computed(() => selectedItemId.value != null);
const canCreateOutput = computed(() => selectedItemId.value != null && selectedInput.value != null);

function getSheetTimestamp(sheet) {
  const raw = sheet.sheetDate ?? sheet.createdAt;
  const ts = Date.parse(raw);
  return Number.isNaN(ts) ? 0 : ts;
}

function sortByLatest(sheets) {
  return [...sheets].sort((a, b) => getSheetTimestamp(b) - getSheetTimestamp(a));
}

async function loadTemplateLookup() {
  const [inputs, outputs] = await Promise.all([
    scaleService.getTemplates('INPUT'),
    scaleService.getTemplates('OUTPUT'),
  ]);

  templateLookup.value = [...inputs, ...outputs].reduce((acc, t) => {
    acc[t.id] = t;
    return acc;
  }, {});
}

async function loadPartTemplateRules() {
  if (!selectedItemId.value) {
    selectedPartId.value = null;
    allowedInputTemplateIds.value = [];
    allowedOutputTemplateIds.value = [];
    return;
  }

  const item = await scaleService.getItemById(selectedItemId.value);
  selectedPartId.value = item?.partId ?? null;

  if (!item?.partId) {
    allowedInputTemplateIds.value = [];
    allowedOutputTemplateIds.value = [];
    return;
  }

  const rules = await scaleService.getTemplatesForPart(item.partId);
  allowedInputTemplateIds.value = rules.inputTemplateIds;
  allowedOutputTemplateIds.value = rules.outputTemplateIds;
}

function openNewRecordDialog() {
  if (!canCreate.value) return;
  showNewInput.value = true;
}

async function loadHistory(preferLatest = false) {
  if (!selectedItemId.value) {
    inputSheets.value = [];
    historicalRecords.value = [];
    selectedRecordInputId.value = null;
    selectedOutputId.value = null;
    return;
  }

  const inputs = await scaleService.listItemSheets(selectedItemId.value, 'INPUT');
  const orderedInputs = sortByLatest(inputs);
  inputSheets.value = orderedInputs;

  const bundles = await Promise.all(
    orderedInputs.map(async (input) => ({
      input,
      outputs: sortByLatest(await scaleService.listInputOutputs(input.id)),
    })),
  );

  historicalRecords.value = bundles;

  if (preferLatest) {
    selectedRecordInputId.value = bundles[0]?.input.id ?? null;
    return;
  }

  const selectedStillExists = bundles.some((b) => b.input.id === selectedRecordInputId.value);
  if (!selectedStillExists) {
    selectedRecordInputId.value = bundles[0]?.input.id ?? null;
  }
}

async function loadSelectedInputDocument() {
  if (!selectedInput.value) {
    selectedInputFields.value = [];
    selectedInputValues.value = {};
    return;
  }

  const [detail, fields] = await Promise.all([
    scaleService.getSheet(selectedInput.value.id),
    scaleService.getTemplateFields(selectedInput.value.templateId),
  ]);

  selectedInputValues.value = detail.values;
  selectedInputFields.value = fields;
}

async function loadSelectedOutputDocument() {
  if (!selectedOutput.value) {
    selectedOutputFields.value = [];
    selectedOutputValues.value = {};
    return;
  }

  const [detail, fields] = await Promise.all([
    scaleService.getSheet(selectedOutput.value.id),
    scaleService.getTemplateFields(selectedOutput.value.templateId),
  ]);

  selectedOutputValues.value = detail.values;
  selectedOutputFields.value = fields;
}

watch(selectedItemId, async () => {
  selectedRecordInputId.value = null;
  selectedOutputId.value = null;
  await loadPartTemplateRules();
  await loadHistory();
});

watch(selectedRecord, async (record) => {
  if (record) {
    selectedOutputId.value = record.outputs[0]?.id ?? null;
  } else {
    selectedOutputId.value = null;
  }

  await loadSelectedInputDocument();
});

watch(selectedOutput, async () => {
  await loadSelectedOutputDocument();
});

async function afterSavedInput() {
  await loadHistory(true);
}

async function afterSavedOutput() {
  await loadHistory();
}

loadTemplateLookup();
</script>

<template>
  <div class="elite-shell p-3">
    <section class="hero-ribbon mb-3">
      <div class="hero-line" />
      <div>
        <h2 class="hero-title m-0">SCALE</h2>
        <small class="p-text-secondary" v-if="selectedPartId">Part {{ selectedPartId }} template rules active</small>
      </div>
      <div class="hero-stats">
        <div class="stat-pill">ITEM {{ selectedItemId ?? '-' }}</div>
        <div class="stat-pill">RECORD {{ historicalRecords.length }}</div>
        <div class="stat-pill">OUT {{ selectedRecord?.outputs.length ?? 0 }}</div>
      </div>
    </section>

    <div class="grid elite-grid">
      <div class="col-12 xl:col-3">
        <section class="elite-panel panel-nav h-full">
          <div class="panel-head">Item Tree</div>
          <ItemTree @selectItem="(id) => (selectedItemId = id)" />
        </section>
      </div>

      <div class="col-12 xl:col-4">
        <section class="elite-panel h-full">
          <div class="panel-head-row mb-2">
            <div class="panel-head">All Records</div>
            <Button
              label="New Record"
              icon="pi pi-plus"
              size="small"
              :disabled="!canCreate"
              @click="openNewRecordDialog"
            />
          </div>
          <div v-if="historicalRecords.length === 0" class="p-text-secondary">Select an item to list records.</div>

          <div
            v-for="entry in historicalRecords"
            :key="entry.input.id"
            class="history-row"
            :class="{ active: selectedRecordInputId === entry.input.id }"
            @click="selectedRecordInputId = entry.input.id"
          >
            <div class="history-top">
              <div>
                <strong>#{{ entry.input.id }}</strong>
                <span class="history-kind">{{ templateNames[entry.input.templateId] ?? `Template ${entry.input.templateId}` }}</span>
              </div>
              <small class="p-text-secondary">{{ entry.outputs.length }} output</small>
            </div>
            <small class="p-text-secondary">{{ entry.input.sheetDate ?? entry.input.createdAt }}</small>
          </div>
        </section>
      </div>

      <div class="col-12 xl:col-5">
        <section class="elite-panel h-full">
          <div class="sheet-columns">
            <div>
              <SheetList
                title="Input Sheets"
                :sheets="inputSheets"
                :selectedId="selectedInput?.id ?? null"
                :templateNames="templateNames"
                @select="(s) => (selectedRecordInputId = s.id)"
              />
            </div>

            <div>
              <SheetList
                title="Output Sheets"
                :sheets="selectedRecord?.outputs ?? []"
                :selectedId="selectedOutput?.id ?? null"
                :templateNames="templateNames"
                @select="(s) => (selectedOutputId = s.id)"
              >
                <template #actions>
                  <Button
                    label="New Output"
                    icon="pi pi-plus"
                    size="small"
                    :disabled="!canCreateOutput"
                    @click="showNewOutput = true"
                  />
                </template>
              </SheetList>
            </div>
          </div>
        </section>
      </div>
    </div>

    <div class="grid mt-2">
      <div class="col-12 xl:col-6">
        <section class="elite-panel h-full">
          <div class="panel-head mb-2">Input Sheet Document View</div>
          <SheetDocumentPreview
            v-if="selectedInput"
            :title="selectedInputTemplateName"
            :subtitle="`Sheet #${selectedInput.id} | ${selectedInput.sheetDate ?? selectedInput.createdAt}`"
            :fields="selectedInputFields"
            :values="selectedInputValues"
          />
          <div v-else class="p-text-secondary">Select an input record.</div>
        </section>
      </div>

      <div class="col-12 xl:col-6">
        <section class="elite-panel h-full">
          <div class="panel-head mb-2">Output Sheet Document View</div>
          <SheetDocumentPreview
            v-if="selectedOutput"
            :title="selectedOutputTemplateName"
            :subtitle="`Sheet #${selectedOutput.id} | ${selectedOutput.sheetDate ?? selectedOutput.createdAt}`"
            :fields="selectedOutputFields"
            :values="selectedOutputValues"
          />
          <div v-else class="p-text-secondary">Select an output sheet.</div>
        </section>
      </div>
    </div>

    <NewSheetDialog
      v-if="selectedItemId"
      v-model:visible="showNewInput"
      kind="INPUT"
      :itemId="selectedItemId"
      :allowedTemplateIds="allowedInputTemplateIds"
      @saved="afterSavedInput"
    />

    <NewSheetDialog
      v-if="selectedItemId && selectedInput"
      v-model:visible="showNewOutput"
      kind="OUTPUT"
      :itemId="selectedItemId"
      :parentSheetId="selectedInput.id"
      :allowedTemplateIds="allowedOutputTemplateIds"
      @saved="afterSavedOutput"
    />
  </div>
</template>

<style scoped>
.elite-shell {
  min-height: calc(100vh - 4rem);
  background:
    radial-gradient(1200px 350px at 20% -20%, rgba(36, 72, 132, 0.18), transparent 70%),
    radial-gradient(900px 350px at 90% -20%, rgba(168, 24, 43, 0.12), transparent 70%),
    #eef2f7;
}

.hero-ribbon {
  display: grid;
  grid-template-columns: 8px 1fr auto;
  align-items: center;
  gap: 1rem;
  padding: 1rem 1.1rem;
  background: #ffffffde;
  border: 1px solid #d8deea;
  border-radius: 1rem;
  backdrop-filter: blur(3px);
}

.hero-line {
  height: 100%;
  border-radius: 999px;
  background: linear-gradient(180deg, #103e78 0%, #ca1e2a 100%);
}

.hero-title {
  font-family: Bahnschrift, 'Trebuchet MS', sans-serif;
  font-size: clamp(1.3rem, 2.4vw, 2.1rem);
  letter-spacing: 0.03em;
}

.hero-stats {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.stat-pill {
  background: #ecf2fe;
  color: #184080;
  border: 1px solid #c6d4f5;
  border-radius: 999px;
  padding: 0.3rem 0.65rem;
  font-size: 0.76rem;
  font-weight: 700;
  letter-spacing: 0.04em;
}

.elite-panel {
  background: #ffffffdf;
  border: 1px solid #d7deea;
  border-radius: 1rem;
  padding: 0.8rem;
  box-shadow: 0 10px 20px rgba(15, 35, 70, 0.06);
}

.panel-nav {
  background: linear-gradient(180deg, #f8fbff 0%, #fdfdff 100%);
}

.panel-head {
  font-weight: 800;
  letter-spacing: 0.04em;
  color: #1f3255;
  padding: 0.4rem 0.5rem 0;
}

.panel-head-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.history-row {
  border: 1px solid #d6dbe8;
  border-radius: 0.75rem;
  padding: 0.55rem;
  margin-bottom: 0.6rem;
  background: #fafcff;
  cursor: pointer;
}

.history-row.active {
  border-color: #2d4f91;
  box-shadow: 0 0 0 1px #2d4f91 inset;
}

.history-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.history-kind {
  margin-left: 0.35rem;
  color: #34507f;
  font-weight: 600;
  font-size: 0.82rem;
}

.sheet-columns {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0.8rem;
}

@media (max-width: 1280px) {
  .hero-ribbon {
    grid-template-columns: 8px 1fr;
  }

  .hero-stats {
    grid-column: 1 / -1;
    justify-content: flex-start;
  }

  .sheet-columns {
    grid-template-columns: 1fr;
  }
}
</style>
