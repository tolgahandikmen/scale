<script setup>
import { computed } from 'vue';

const props = defineProps({
  title: { type: String, required: true },
  subtitle: { type: String, default: '' },
  fields: { type: Array, default: () => [] },
  values: { type: Object, default: () => ({}) },
});

function stringify(value) {
  if (!value) return { unit: '', value: '-' };

  if (value.type === 'TEXT' || value.type === 'DATE' || value.type === 'DROPDOWN') {
    return { unit: '', value: value.value ? String(value.value) : '-' };
  }

  if (value.type === 'BOOLEAN') {
    return { unit: '', value: value.value == null ? '-' : value.value ? 'Yes' : 'No' };
  }

  if (value.type === 'NUMBER') {
    return {
      unit: value.unit ?? '',
      value: value.value == null ? '-' : String(value.value),
    };
  }

  if (value.type === 'MULTI') {
    const pairs = Object.entries(value.value ?? {}).map(([k, v]) => `${k}: ${v ?? '-'}`);
    return {
      unit: value.unit ?? '',
      value: pairs.join(' | ') || '-',
    };
  }

  return {
    unit: '',
    value: `${value.value.rows.length} row(s)`,
  };
}

const tableRows = computed(() => {
  return [...props.fields]
    .sort((a, b) => (a.orderNo ?? 0) - (b.orderNo ?? 0))
    .map((f) => {
      const result = stringify(props.values[f.id]);
      return {
        key: f.key,
        label: f.label,
        unit: result.unit || (f.unitOptions?.[0] ?? ''),
        value: result.value,
      };
    });
});
</script>

<template>
  <section class="doc-preview">
    <header class="doc-head">
      <div class="doc-line" />
      <div>
        <h3 class="doc-title m-0">{{ title }}</h3>
        <p class="doc-sub mt-1 mb-0">{{ subtitle || 'Dynamic input sheet preview' }}</p>
      </div>
    </header>

    <table class="doc-table mt-3">
      <thead>
        <tr>
          <th>Calibration Metadata</th>
          <th>Unit</th>
          <th>Value</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="row in tableRows" :key="row.key">
          <td>{{ row.label }}</td>
          <td>{{ row.unit || '-' }}</td>
          <td>{{ row.value }}</td>
        </tr>
      </tbody>
    </table>
  </section>
</template>

<style scoped>
.doc-preview {
  background: #fff;
  border: 1px solid #1a1d25;
  border-radius: 0.75rem;
  padding: 0.9rem;
}

.doc-head {
  display: grid;
  grid-template-columns: 6px 1fr;
  gap: 0.7rem;
  align-items: center;
}

.doc-line {
  height: 100%;
  border-radius: 999px;
  background: linear-gradient(180deg, #0f356a 0%, #b31f2a 100%);
}

.doc-title {
  font-size: 1.35rem;
  letter-spacing: 0.02em;
}

.doc-sub {
  color: #61697a;
  font-weight: 600;
}

.doc-table {
  width: 100%;
  border-collapse: collapse;
}

.doc-table th,
.doc-table td {
  border: 1px solid #1f222b;
  padding: 0.42rem 0.5rem;
  font-size: 0.85rem;
}

.doc-table th {
  background: #eef1f7;
  text-align: left;
}

.doc-table td:nth-child(2),
.doc-table td:nth-child(3) {
  font-weight: 600;
}
</style>
