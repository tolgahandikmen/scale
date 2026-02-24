<script setup>
defineProps({
  title: { type: String, required: true },
  sheets: { type: Array, default: () => [] },
  selectedId: { type: Number, default: null },
  templateNames: { type: Object, default: () => ({}) },
});

const emit = defineEmits(['select']);
</script>

<template>
  <div class="sheet-list">
    <div class="flex align-items-center justify-content-between mb-2">
      <div class="sheet-list-title">{{ title }}</div>
      <slot name="actions" />
    </div>

    <div v-if="sheets.length === 0" class="sheet-empty">No records.</div>

    <div
      v-for="s in sheets"
      :key="s.id"
      class="sheet-row"
      :class="{ active: s.id === selectedId }"
      @click="emit('select', s)"
    >
      <div class="flex align-items-center justify-content-between">
        <div class="sheet-id">#{{ s.id }}</div>
        <small class="sheet-date">{{ s.sheetDate ?? '-' }}</small>
      </div>
      <div class="sheet-meta">
        <span>{{ templateNames?.[s.templateId] ?? `TEMPLATE ${s.templateId}` }}</span>
        <span v-if="s.outputTemplateId">{{ templateNames?.[s.outputTemplateId] ?? `FORMAT ${s.outputTemplateId}` }}</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.sheet-list-title {
  font-weight: 700;
  letter-spacing: 0.03em;
}

.sheet-empty {
  border: 1px dashed #d2d8e3;
  border-radius: 0.75rem;
  padding: 0.75rem;
  color: #5f6471;
}

.sheet-row {
  cursor: pointer;
  border: 1px solid #d9dce6;
  border-radius: 0.8rem;
  padding: 0.7rem;
  margin-bottom: 0.65rem;
  background: linear-gradient(180deg, #ffffff 0%, #f8f9fc 100%);
  transition: 140ms ease;
}

.sheet-row:hover {
  transform: translateY(-1px);
  border-color: #a9b7d7;
}

.sheet-row.active {
  border-color: #2d4f91;
  box-shadow: 0 0 0 1px #2d4f91 inset;
}

.sheet-id {
  font-weight: 700;
  color: #202635;
}

.sheet-date {
  color: #6a7181;
}

.sheet-meta {
  margin-top: 0.35rem;
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.sheet-meta span {
  font-size: 0.74rem;
  font-weight: 700;
  letter-spacing: 0.02em;
  color: #35548d;
  background: #ecf1fb;
  padding: 0.2rem 0.45rem;
  border-radius: 999px;
}
</style>
