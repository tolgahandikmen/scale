<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import ItemTree from '@/components/tree/ItemTree.vue';
import SheetList from '@/components/forms/SheetList.vue';
import NewSheetDialog from '@/components/forms/NewSheetDialog.vue';
import SheetDocumentPreview from '@/components/forms/SheetDocumentPreview.vue';
import type { FieldDefinition, SheetInstance, SheetTemplate, ValuesMap } from '@/models/form';
import { listItemSheets, listInputOutputs, getSheet } from '@/api/sheetsApi';
import { getTemplateFields, getTemplates } from '@/api/templatesApi';
import { getItemById } from '@/api/itemsApi';
import { getTemplatesForPart } from '@/api/partTemplateMappingsApi';
import Button from 'primevue/button';

const selectedItemId = ref<number | null>(null);

const inputSheets = ref<SheetInstance[]>([]);
const selectedInput = ref<SheetInstance | null>(null);

const outputSheets = ref<SheetInstance[]>([]);

const showNewInput = ref(false);
const showNewOutput = ref(false);
const selectedInputValues = ref<ValuesMap>({});
const selectedInputFields = ref<FieldDefinition[]>([]);
const templateLookup = ref<Record<number, SheetTemplate>>({});
const historicalRecords = ref<Array<{ input: SheetInstance; outputs: SheetInstance[] }>>([]);
const selectedPartId = ref<string | null>(null);
const allowedInputTemplateIds = ref<number[]>([]);
const allowedOutputTemplateIds = ref<number[]>([]);

const canCreate = computed(() => selectedItemId.value != null);
const canCreateOutput = computed(() => selectedItemId.value != null && selectedInput.value != null);
const allowedInputTemplateNames = computed(() =>
  allowedInputTemplateIds.value.map((id) => templateNames.value[id] ?? `Template ${id}`),
);
const allowedOutputTemplateNames = computed(() =>
  allowedOutputTemplateIds.value.map((id) => templateNames.value[id] ?? `Template ${id}`),
);

const templateNames = computed<Record<number, string>>(() => {
  const map: Record<number, string> = {};
  Object.values(templateLookup.value).forEach((t) => {
    map[t.id] = `${t.name} (v${t.version})`;
  });
  return map;
});

const selectedInputTemplateName = computed(() => {
  if (!selectedInput.value) return 'Calibration Input';
  return templateNames.value[selectedInput.value.templateId] ?? `Template ${selectedInput.value.templateId}`;
});

const loadProfile = [0, 20, 40, 60, 80, 100, 80, 60, 40, 20, 0];

const positiveProfile = computed(() =>
  loadProfile.map((p) => ({
    percent: `${p}%`,
    kn: (p / 100 * 2.2).toFixed(1),
    mvv: (p / 100 * 0.184).toFixed(3),
  })),
);

const negativeProfile = computed(() =>
  loadProfile.map((p) => ({
    percent: `${p}%`,
    kn: (-(p / 100) * 1.0).toFixed(1),
    mvv: (-(p / 100) * 0.084).toFixed(3),
  })),
);

async function loadInputs() {
  if (!selectedItemId.value) return;
  inputSheets.value = await listItemSheets(selectedItemId.value, 'INPUT');
  if (inputSheets.value.length && !selectedInput.value) selectedInput.value = inputSheets.value[0];
  await loadHistory();
}

async function loadOutputs() {
  if (!selectedInput.value) {
    outputSheets.value = [];
    selectedInputFields.value = [];
    selectedInputValues.value = {};
    return;
  }
  outputSheets.value = await listInputOutputs(selectedInput.value.id);
  const detail = await getSheet(selectedInput.value.id);
  selectedInputValues.value = detail.values;
  selectedInputFields.value = await getTemplateFields(selectedInput.value.templateId);
}

async function loadHistory() {
  if (!selectedItemId.value) {
    historicalRecords.value = [];
    return;
  }

  const orderedInputs = [...inputSheets.value].sort((a, b) => b.id - a.id);
  const bundles = await Promise.all(
    orderedInputs.map(async (input) => ({
      input,
      outputs: await listInputOutputs(input.id),
    })),
  );
  historicalRecords.value = bundles;
}

async function loadTemplateLookup() {
  const [inputs, outputs] = await Promise.all([getTemplates('INPUT'), getTemplates('OUTPUT')]);
  const all = [...inputs, ...outputs];
  templateLookup.value = all.reduce<Record<number, SheetTemplate>>((acc, t) => {
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

  const item = await getItemById(selectedItemId.value);
  selectedPartId.value = item?.partId ?? null;

  if (!item?.partId) {
    allowedInputTemplateIds.value = [];
    allowedOutputTemplateIds.value = [];
    return;
  }

  const rules = await getTemplatesForPart(item.partId);
  allowedInputTemplateIds.value = rules.inputTemplateIds;
  allowedOutputTemplateIds.value = rules.outputTemplateIds;
}

watch(selectedItemId, async () => {
  selectedInput.value = null;
  await loadPartTemplateRules();
  await loadInputs();
  await loadOutputs();
});

watch(selectedInput, async () => {
  await loadOutputs();
});

async function afterSavedInput() {
  await loadInputs();
  await loadOutputs();
}

async function afterSavedOutput() {
  await loadOutputs();
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
        <div class="stat-pill">INPUT {{ inputSheets.length }}</div>
        <div class="stat-pill">OUTPUT {{ outputSheets.length }}</div>
      </div>
    </section>

    <section v-if="selectedPartId" class="elite-panel mb-3">
      <div class="panel-head mb-2">Part-Template Rules</div>
      <div class="rule-grid">
        <div>
          <small class="p-text-secondary block mb-1">Allowed INPUT templates</small>
          <div class="rule-tags">
            <span v-for="name in allowedInputTemplateNames" :key="name">{{ name }}</span>
            <small v-if="allowedInputTemplateNames.length === 0" class="p-text-secondary">No rule configured</small>
          </div>
        </div>
        <div>
          <small class="p-text-secondary block mb-1">Allowed OUTPUT templates</small>
          <div class="rule-tags">
            <span v-for="name in allowedOutputTemplateNames" :key="name">{{ name }}</span>
            <small v-if="allowedOutputTemplateNames.length === 0" class="p-text-secondary">No rule configured</small>
          </div>
        </div>
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
          <SheetList
            title="Input Sheets"
            :sheets="inputSheets"
            :selectedId="selectedInput?.id ?? null"
            :templateNames="templateNames"
            @select="(s) => (selectedInput = s)"
          >
            <template #actions>
              <Button label="New Input" icon="pi pi-plus" size="small" :disabled="!canCreate" @click="showNewInput = true" />
            </template>
          </SheetList>
        </section>
      </div>

      <div class="col-12 xl:col-5">
        <section class="elite-panel h-full">
          <SheetList title="Output Sheets" :sheets="outputSheets" :selectedId="null" :templateNames="templateNames" @select="() => {}">
            <template #actions>
              <Button label="New Output" icon="pi pi-plus" size="small" :disabled="!canCreateOutput" @click="showNewOutput = true" />
            </template>
          </SheetList>

          <div v-if="selectedInput" class="output-visual mt-3">
            <div class="visual-title">Output Profile Preview</div>
            <div class="grid mt-2">
              <div class="col-12 md:col-6">
                <table class="load-table blue">
                  <thead>
                    <tr><th>%</th><th>kN</th><th>mV/V</th></tr>
                  </thead>
                  <tbody>
                    <tr v-for="r in positiveProfile" :key="`p-${r.percent}-${r.kn}`">
                      <td>{{ r.percent }}</td><td>{{ r.kn }}</td><td>{{ r.mvv }}</td>
                    </tr>
                  </tbody>
                </table>
              </div>
              <div class="col-12 md:col-6">
                <table class="load-table red">
                  <thead>
                    <tr><th>%</th><th>kN</th><th>mV/V</th></tr>
                  </thead>
                  <tbody>
                    <tr v-for="r in negativeProfile" :key="`n-${r.percent}-${r.kn}`">
                      <td>{{ r.percent }}</td><td>{{ r.kn }}</td><td>{{ r.mvv }}</td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>
          </div>

          <div class="mt-3 p-text-secondary" v-else>Select an input sheet to view and create outputs.</div>
        </section>
      </div>
    </div>

    <div class="grid mt-1">
      <div class="col-12 xl:col-8">
        <section class="elite-panel">
          <div class="panel-head mb-2">Input Sheet Document View</div>
          <SheetDocumentPreview
            v-if="selectedInput"
            :title="selectedInputTemplateName"
            :subtitle="`Sheet #${selectedInput.id} | ${selectedInput.sheetDate ?? ''}`"
            :fields="selectedInputFields"
            :values="selectedInputValues"
          />
          <div v-else class="p-text-secondary">Select an input to see document-style preview.</div>
        </section>
      </div>

      <div class="col-12 xl:col-4">
        <section class="elite-panel h-full">
          <div class="panel-head mb-2">Historical Records</div>
          <div v-if="historicalRecords.length === 0" class="p-text-secondary">No historical input/output records for this item.</div>
          <div v-for="entry in historicalRecords" :key="entry.input.id" class="history-row">
            <div class="history-top">
              <div>
                <strong>#{{ entry.input.id }}</strong>
                <span class="history-kind">{{ templateNames[entry.input.templateId] ?? `Template ${entry.input.templateId}` }}</span>
              </div>
              <Button text size="small" label="Open" @click="selectedInput = entry.input" />
            </div>
            <small class="p-text-secondary">{{ entry.input.sheetDate ?? entry.input.createdAt }}</small>
            <div class="history-output-tags">
              <span v-for="out in entry.outputs" :key="out.id">
                {{ templateNames[out.templateId] ?? `Output ${out.id}` }}
              </span>
              <small v-if="entry.outputs.length === 0" class="p-text-secondary">No outputs</small>
            </div>
          </div>
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

.hero-sub {
  color: #5d6473;
  font-weight: 500;
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

.output-visual {
  border-top: 1px dashed #cfd6e4;
  padding-top: 0.8rem;
}

.visual-title {
  font-weight: 700;
  letter-spacing: 0.02em;
}

.load-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 0.79rem;
  border: 1px solid #121315;
}

.load-table th,
.load-table td {
  border: 1px solid #121315;
  text-align: center;
  padding: 0.24rem;
  font-weight: 700;
}

.load-table.blue tbody td {
  background: #17a6da;
  color: #031d28;
}

.load-table.red tbody td {
  background: #d91f1f;
  color: #2b0a0a;
}

.history-row {
  border: 1px solid #d6dbe8;
  border-radius: 0.75rem;
  padding: 0.55rem;
  margin-bottom: 0.6rem;
  background: #fafcff;
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

.history-output-tags {
  margin-top: 0.4rem;
  display: flex;
  gap: 0.35rem;
  flex-wrap: wrap;
}

.history-output-tags span {
  font-size: 0.73rem;
  font-weight: 700;
  background: #edf2ff;
  color: #253f72;
  border-radius: 999px;
  padding: 0.16rem 0.42rem;
}

.rule-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0.8rem;
}

.rule-tags {
  display: flex;
  gap: 0.35rem;
  flex-wrap: wrap;
}

.rule-tags span {
  font-size: 0.73rem;
  font-weight: 700;
  background: #edf2ff;
  color: #253f72;
  border-radius: 999px;
  padding: 0.16rem 0.42rem;
}

@media (max-width: 1280px) {
  .hero-ribbon {
    grid-template-columns: 8px 1fr;
  }

  .hero-stats {
    grid-column: 1 / -1;
    justify-content: flex-start;
  }

  .rule-grid {
    grid-template-columns: 1fr;
  }
}
</style>
