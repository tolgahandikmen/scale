<template>
  <div class="dashboard-container">
    <!-- ========== ROW 1: Summary KPI Cards ========== -->
    <div class="grid">
      <div class="col-12 md:col-6 lg:col-3" v-for="kpi in kpiCards" :key="kpi.label">
        <div class="kpi-card" :style="{ borderLeftColor: kpi.color }">
          <div class="kpi-card__icon" :style="{ backgroundColor: kpi.bgColor }">
            <i :class="kpi.icon" :style="{ color: kpi.color }"></i>
          </div>
          <div class="kpi-card__content">
            <span class="kpi-card__value" v-if="!kpiLoading">{{ kpi.value }}</span>
            <Skeleton v-else width="4rem" height="1.75rem" borderRadius="8px" />
            <span class="kpi-card__label">{{ kpi.label }}</span>
          </div>
          <div class="kpi-card__trend" :class="kpi.trendUp ? 'trend-up' : 'trend-down'" v-if="!kpiLoading">
            <i :class="kpi.trendUp ? 'pi pi-arrow-up' : 'pi pi-arrow-down'"></i>
            <span>{{ kpi.trendPercent }}%</span>
          </div>
          <Skeleton v-else width="3rem" height="1.5rem" borderRadius="12px" />
        </div>
      </div>
    </div>

    <!-- ========== ROW 2: Existing Charts ========== -->
    <div class="grid mt-4">
      <!-- Equipment Assignment Distribution (Pie) -->
      <div class="col-12 md:col-4">
        <Card class="dashboard-card">
          <template #title>
            <div class="card-title">
              <i class="pi pi-chart-pie"></i>
              <span>Equipment Assignment Distribution</span>
            </div>
          </template>
          <template #content>
            <Chart type="pie" :data="assignmentChartData" :options="pieOptions" class="chart-area" />
            <div class="chart-legend">
              <div class="legend-item" v-for="(label, i) in assignmentChartData.labels" :key="label">
                <span class="legend-dot" :style="{ backgroundColor: assignmentChartData.datasets[0].backgroundColor[i] }"></span>
                <span class="legend-label">{{ label }}</span>
                <span class="legend-value">{{ assignmentChartData.datasets[0].data[i] }}</span>
              </div>
            </div>
          </template>
        </Card>
      </div>

      <!-- Directory Distribution (Bar) -->
      <div class="col-12 md:col-4">
        <Card class="dashboard-card">
          <template #title>
            <div class="card-title">
              <i class="pi pi-chart-bar"></i>
              <span>Directory Distribution</span>
            </div>
          </template>
          <template #content>
            <Chart type="bar" :data="directoryChartData" :options="barOptions" class="chart-area" />
          </template>
        </Card>
      </div>

      <!-- Periodic Maintenance Distribution (Doughnut) -->
      <div class="col-12 md:col-4">
        <Card class="dashboard-card">
          <template #title>
            <div class="card-title">
              <i class="pi pi-cog"></i>
              <span>Periodic Maintenance Distribution</span>
            </div>
          </template>
          <template #content>
            <Chart type="doughnut" :data="maintenanceChartData" :options="doughnutOptions" class="chart-area" />
            <div class="chart-legend">
              <div class="legend-item" v-for="(label, i) in maintenanceChartData.labels" :key="label">
                <span class="legend-dot" :style="{ backgroundColor: maintenanceChartData.datasets[0].backgroundColor[i] }"></span>
                <span class="legend-label">{{ label }}</span>
                <span class="legend-value">{{ maintenanceChartData.datasets[0].data[i] }}</span>
              </div>
            </div>
          </template>
        </Card>
      </div>
    </div>

    <!-- ========== ROW 3: New Timeline Charts ========== -->
    <div class="grid mt-4">
      <!-- Service Life Expiring -->
      <div class="col-12 md:col-4">
        <Card class="dashboard-card warning-card">
          <template #title>
            <div class="card-title">
              <i class="pi pi-clock" style="color: var(--orange-500)"></i>
              <span>Service Life Expiring</span>
              <Badge :value="serviceLifeExpiring.length" severity="warning" class="ml-auto" />
            </div>
          </template>
          <template #content>
            <div class="timeline-list">
              <div
                v-for="item in serviceLifeExpiring"
                :key="item.id"
                class="timeline-item"
                :class="getUrgencyClass(item.remainingDays)"
              >
                <div class="timeline-item__indicator">
                  <Knob
                    :modelValue="item.usagePercent"
                    :size="48"
                    :strokeWidth="6"
                    readonly
                    :valueColor="getKnobColor(item.usagePercent)"
                    rangeColor="#e9ecef"
                    valueTemplate="{value}%"
                  />
                </div>
                <div class="timeline-item__content">
                  <span class="timeline-item__name">{{ item.name }}</span>
                  <span class="timeline-item__meta">
                    <i class="pi pi-tag"></i> {{ item.partNumber }}
                  </span>
                  <span class="timeline-item__meta">
                    <i class="pi pi-map-marker"></i> {{ item.directory }}
                  </span>
                </div>
                <div class="timeline-item__badge">
                  <Tag
                    :severity="getDaySeverity(item.remainingDays)"
                    :value="item.remainingDays + ' days'"
                    rounded
                  />
                </div>
              </div>
              <div v-if="serviceLifeExpiring.length === 0" class="empty-state">
                <i class="pi pi-check-circle"></i>
                <span>No equipment with expiring service life</span>
              </div>
            </div>
          </template>
        </Card>
      </div>

      <!-- Distribution Date Approaching -->
      <div class="col-12 md:col-4">
        <Card class="dashboard-card info-card">
          <template #title>
            <div class="card-title">
              <i class="pi pi-send" style="color: var(--blue-500)"></i>
              <span>Upcoming Distribution Dates</span>
              <Badge :value="distributionApproaching.length" severity="info" class="ml-auto" />
            </div>
          </template>
          <template #content>
            <div class="timeline-list">
              <div
                v-for="item in distributionApproaching"
                :key="item.id"
                class="timeline-item"
              >
                <div class="timeline-item__indicator">
                  <div class="date-badge">
                    <span class="date-badge__day">{{ getDayFromDate(item.distributionDate) }}</span>
                    <span class="date-badge__month">{{ getMonthFromDate(item.distributionDate) }}</span>
                  </div>
                </div>
                <div class="timeline-item__content">
                  <span class="timeline-item__name">{{ item.name }}</span>
                  <span class="timeline-item__meta">
                    <i class="pi pi-tag"></i> {{ item.partNumber }}
                  </span>
                  <span class="timeline-item__meta">
                    <i class="pi pi-user"></i> {{ item.assignedTo }}
                  </span>
                </div>
                <div class="timeline-item__badge">
                  <Tag
                    :severity="getDaySeverity(item.remainingDays)"
                    :value="item.remainingDays + ' days'"
                    rounded
                  />
                </div>
              </div>
              <div v-if="distributionApproaching.length === 0" class="empty-state">
                <i class="pi pi-check-circle"></i>
                <span>No upcoming distribution dates</span>
              </div>
            </div>
          </template>
        </Card>
      </div>

      <!-- Expiration Date Approaching -->
      <div class="col-12 md:col-4">
        <Card class="dashboard-card danger-card">
          <template #title>
            <div class="card-title">
              <i class="pi pi-exclamation-triangle" style="color: var(--red-500)"></i>
              <span>Expiration Date Approaching</span>
              <Badge :value="expirationApproaching.length" severity="danger" class="ml-auto" />
            </div>
          </template>
          <template #content>
            <div class="timeline-list">
              <div
                v-for="item in expirationApproaching"
                :key="item.id"
                class="timeline-item"
                :class="getUrgencyClass(item.remainingDays)"
              >
                <div class="timeline-item__indicator">
                  <div class="date-badge date-badge--danger">
                    <span class="date-badge__day">{{ getDayFromDate(item.expirationDate) }}</span>
                    <span class="date-badge__month">{{ getMonthFromDate(item.expirationDate) }}</span>
                  </div>
                </div>
                <div class="timeline-item__content">
                  <span class="timeline-item__name">{{ item.name }}</span>
                  <span class="timeline-item__meta">
                    <i class="pi pi-tag"></i> {{ item.partNumber }}
                  </span>
                  <span class="timeline-item__meta">
                    <i class="pi pi-calendar"></i> Exp: {{ formatDate(item.expirationDate) }}
                  </span>
                </div>
                <div class="timeline-item__badge">
                  <Tag
                    :severity="getDaySeverity(item.remainingDays)"
                    :value="item.remainingDays + ' days'"
                    rounded
                  />
                </div>
              </div>
              <div v-if="expirationApproaching.length === 0" class="empty-state">
                <i class="pi pi-check-circle"></i>
                <span>No equipment with approaching expiration date</span>
              </div>
            </div>
          </template>
        </Card>
      </div>
    </div>

    <!-- ========== ROW 4: Expiration Deactivation Table ========== -->
    <div class="grid mt-4">
      <div class="col-12">
        <Card class="dashboard-card deactivation-card">
          <template #title>
            <div class="card-title">
              <i class="pi pi-ban" style="color: var(--red-600)"></i>
              <span>Expired Equipment — Deactivation Approval List</span>
              <Badge :value="expiredEquipments.length" severity="danger" class="ml-2" />
              <div class="card-title__actions">
                <Button
                  label="Deactivate Selected"
                  icon="pi pi-power-off"
                  severity="danger"
                  :disabled="selectedExpired.length === 0"
                  :badge="selectedExpired.length > 0 ? String(selectedExpired.length) : undefined"
                  @click="confirmBulkDeactivate"
                  size="small"
                />
              </div>
            </div>
          </template>
          <template #content>
            <DataTable
              v-model:selection="selectedExpired"
              :value="expiredEquipments"
              dataKey="id"
              :paginator="true"
              :rows="10"
              :rowsPerPageOptions="[5, 10, 20]"
              responsiveLayout="scroll"
              stripedRows
              :globalFilterFields="['name', 'partNumber', 'serialNumber', 'directory']"
              v-model:filters="expiredFilters"
              filterDisplay="menu"
              class="deactivation-table"
              :rowClass="getExpiredRowClass"
              paginatorTemplate="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink CurrentPageReport RowsPerPageDropdown"
              currentPageReportTemplate="{first} - {last} / {totalRecords} equipment"
            >
              <template #header>
                <div class="table-header">
                  <span class="p-input-icon-left">
                    <i class="pi pi-search" />
                    <InputText
                      v-model="expiredFilters['global'].value"
                      placeholder="Search equipment..."
                      size="small"
                    />
                  </span>
                  <div class="table-header__info">
                    <Tag severity="danger" :value="overdueCount + ' Overdue'" rounded />
                    <Tag severity="warning" :value="todayExpiredCount + ' Expired Today'" rounded />
                  </div>
                </div>
              </template>

              <Column selectionMode="multiple" headerStyle="width: 3rem" />

              <Column field="name" header="Equipment Name" sortable style="min-width: 12rem">
                <template #body="{ data }">
                  <div class="equipment-cell">
                    <span class="equipment-cell__name">{{ data.name }}</span>
                    <span class="equipment-cell__serial">S/N: {{ data.serialNumber }}</span>
                  </div>
                </template>
              </Column>

              <Column field="partNumber" header="Part Number" sortable style="min-width: 8rem" />

              <Column field="directory" header="Directory" sortable style="min-width: 8rem">
                <template #body="{ data }">
                  <Tag :value="data.directory" severity="info" />
                </template>
              </Column>

              <Column field="expirationDate" header="Expiration Date" sortable style="min-width: 8rem">
                <template #body="{ data }">
                  <span class="expiration-date">
                    <i class="pi pi-calendar-times"></i>
                    {{ formatDate(data.expirationDate) }}
                  </span>
                </template>
              </Column>

              <Column field="expiredDaysAgo" header="Overdue" sortable style="min-width: 6rem">
                <template #body="{ data }">
                  <Tag
                    :severity="data.expiredDaysAgo > 30 ? 'danger' : 'warning'"
                    :value="data.expiredDaysAgo + ' days'"
                    rounded
                  />
                </template>
              </Column>

              <Column field="assignedTo" header="Assigned To" sortable style="min-width: 8rem">
                <template #body="{ data }">
                  <div class="assigned-cell" v-if="data.assignedTo">
                    <Avatar :label="data.assignedTo.charAt(0)" shape="circle" size="small" />
                    <span>{{ data.assignedTo }}</span>
                  </div>
                  <span v-else class="text-muted">Unassigned</span>
                </template>
              </Column>

              <Column header="Actions" style="min-width: 8rem" frozen alignFrozen="right">
                <template #body="{ data }">
                  <div class="action-cell">
                    <Button
                      icon="pi pi-power-off"
                      severity="danger"
                      text
                      rounded
                      size="small"
                      v-tooltip.top="'Deactivate'"
                      @click="confirmSingleDeactivate(data)"
                    />
                    <Button
                      icon="pi pi-eye"
                      severity="info"
                      text
                      rounded
                      size="small"
                      v-tooltip.top="'Details'"
                      @click="showEquipmentDetail(data)"
                    />
                    <Button
                      icon="pi pi-history"
                      severity="secondary"
                      text
                      rounded
                      size="small"
                      v-tooltip.top="'View History'"
                      @click="showEquipmentHistory(data)"
                    />
                  </div>
                </template>
              </Column>

              <template #empty>
                <div class="empty-table-state">
                  <i class="pi pi-verified"></i>
                  <span>No expired equipment found.</span>
                </div>
              </template>
            </DataTable>
          </template>
        </Card>
      </div>
    </div>

    <!-- ========== Confirm Dialogs ========== -->
    <Dialog
      v-model:visible="showDeactivateDialog"
      :modal="true"
      :closable="true"
      header="Equipment Deactivation Confirmation"
      :style="{ width: '480px' }"
      class="deactivate-dialog"
    >
      <div class="deactivate-dialog__content">
        <i class="pi pi-exclamation-triangle deactivate-dialog__icon"></i>
        <p v-if="deactivateTarget === 'single'">
          Are you sure you want to deactivate
          <strong>{{ singleDeactivateItem?.name }}</strong> (S/N: {{ singleDeactivateItem?.serialNumber }})?
        </p>
        <p v-else>
          Are you sure you want to deactivate the selected <strong>{{ selectedExpired.length }}</strong> equipment?
        </p>
        <Message severity="warn" :closable="false" class="mt-3">
          This action will set the equipment to inactive status. They will no longer be assignable or usable.
        </Message>

        <!-- Bulk list preview -->
        <div v-if="deactivateTarget === 'bulk' && selectedExpired.length > 0" class="bulk-preview mt-3">
          <DataTable :value="selectedExpired" :rows="5" :paginator="selectedExpired.length > 5" size="small">
            <Column field="name" header="Equipment" />
            <Column field="partNumber" header="Part Number" />
            <Column field="expirationDate" header="Expiration Date">
              <template #body="{ data }">
                {{ formatDate(data.expirationDate) }}
              </template>
            </Column>
          </DataTable>
        </div>
      </div>

      <template #footer>
        <Button label="Cancel" icon="pi pi-times" severity="secondary" text @click="showDeactivateDialog = false" />
        <Button label="Deactivate" icon="pi pi-power-off" severity="danger" @click="executeDeactivation" :loading="deactivating" />
      </template>
    </Dialog>

    <!-- Toast -->
    <Toast position="top-right" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useToast } from 'primevue/usetoast';
import { FilterMatchMode } from 'primevue/api';

import Card from 'primevue/card';
import Chart from 'primevue/chart';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import Button from 'primevue/button';
import Badge from 'primevue/badge';
import Tag from 'primevue/tag';
import Knob from 'primevue/knob';
import Avatar from 'primevue/avatar';
import InputText from 'primevue/inputtext';
import Dialog from 'primevue/dialog';
import Message from 'primevue/message';
import Toast from 'primevue/toast';
import Skeleton from 'primevue/skeleton';

const toast = useToast();

// ========================
// KPI Cards
// ========================
const kpiLoading = ref(true);

const kpiCards = ref([
  {
    label: 'Total Equipment',
    value: null,
    icon: 'pi pi-box',
    color: '#3B82F6',
    bgColor: '#EFF6FF',
    trendUp: true,
    trendPercent: null,
  },
  {
    label: 'Active Equipment',
    value: null,
    icon: 'pi pi-check-circle',
    color: '#22C55E',
    bgColor: '#F0FDF4',
    trendUp: true,
    trendPercent: null,
  },
  {
    label: 'Pending Maintenance',
    value: null,
    icon: 'pi pi-wrench',
    color: '#F59E0B',
    bgColor: '#FFFBEB',
    trendUp: false,
    trendPercent: null,
  },
  {
    label: 'Expired',
    value: null,
    icon: 'pi pi-exclamation-circle',
    color: '#EF4444',
    bgColor: '#FEF2F2',
    trendUp: false,
    trendPercent: null,
  },
]);

async function fetchKpiData() {
  kpiLoading.value = true;
  try {
    // TODO: Replace with your actual API call
    // const response = await api.get('/dashboard/kpi');
    // const data = response.data;

    // Simulated API response
    await new Promise((resolve) => setTimeout(resolve, 1500));
    const data = {
      totalEquipment: { value: 2847, trendUp: true, trendPercent: 4.2 },
      activeEquipment: { value: 2631, trendUp: true, trendPercent: 2.1 },
      pendingMaintenance: { value: 156, trendUp: false, trendPercent: 8.3 },
      expired: { value: 60, trendUp: false, trendPercent: 12.5 },
    };

    const entries = Object.values(data);
    entries.forEach((entry, i) => {
      kpiCards.value[i].value = entry.value;
      kpiCards.value[i].trendUp = entry.trendUp;
      kpiCards.value[i].trendPercent = entry.trendPercent;
    });
  } catch (error) {
    console.error('Failed to fetch KPI data:', error);
    toast.add({
      severity: 'error',
      summary: 'Error',
      detail: 'Failed to load dashboard summary data.',
      life: 4000,
    });
  } finally {
    kpiLoading.value = false;
  }
}

onMounted(() => {
  fetchKpiData();
});

// ========================
// Chart Data — Assignment Distribution
// ========================
const assignmentChartData = ref({
  labels: ['Assigned Equipment', 'Unassigned Equipment'],
  datasets: [
    {
      data: [1876, 971],
      backgroundColor: ['#F97316', '#FBBF24'],
      hoverBackgroundColor: ['#EA580C', '#F59E0B'],
      borderWidth: 0,
    },
  ],
});

// ========================
// Chart Data — Directory Distribution
// ========================
const directoryChartData = ref({
  labels: ['Fixed Wing', 'Rotating Wing', 'Flight School', 'Guest', 'Common Use', 'Undefined'],
  datasets: [
    {
      label: 'Assigned',
      backgroundColor: '#22C55E',
      data: [180, 45, 12, 5, 1650, 0],
      borderRadius: 4,
    },
    {
      label: 'Unassigned',
      backgroundColor: '#3B82F6',
      data: [85, 30, 8, 3, 1280, 0],
      borderRadius: 4,
    },
  ],
});

// ========================
// Chart Data — Maintenance Distribution
// ========================
const maintenanceChartData = ref({
  labels: ['Maintained Equipment', 'Upcoming Maintenance', 'Overdue Maintenance', 'Maintenance-Free Equipment'],
  datasets: [
    {
      data: [1820, 320, 85, 622],
      backgroundColor: ['#22C55E', '#F59E0B', '#EF4444', '#06B6D4'],
      hoverBackgroundColor: ['#16A34A', '#D97706', '#DC2626', '#0891B2'],
      borderWidth: 0,
    },
  ],
});

// ========================
// Chart Options
// ========================
const pieOptions = ref({
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: { display: false },
  },
});

const doughnutOptions = ref({
  responsive: true,
  maintainAspectRatio: false,
  cutout: '60%',
  plugins: {
    legend: { display: false },
  },
});

const barOptions = ref({
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: {
      position: 'top',
      labels: {
        usePointStyle: true,
        padding: 16,
        font: { size: 11 },
      },
    },
  },
  scales: {
    x: {
      grid: { display: false },
      ticks: { font: { size: 10 } },
    },
    y: {
      grid: { color: '#f1f5f9' },
      ticks: { font: { size: 10 } },
    },
  },
});

// ========================
// Service Life Expiring
// ========================
const serviceLifeExpiring = ref([
  { id: 1, name: 'Oxygen Mask', partNumber: 'OXM-3421', directory: 'Fixed Wing', remainingDays: 5, usagePercent: 95 },
  { id: 2, name: 'Fire Extinguisher A12', partNumber: 'FEX-1102', directory: 'Common Use', remainingDays: 12, usagePercent: 88 },
  { id: 3, name: 'Life Vest Type III', partNumber: 'LVT-0087', directory: 'Rotating Wing', remainingDays: 22, usagePercent: 78 },
  { id: 4, name: 'First Aid Kit', partNumber: 'FAK-5540', directory: 'Flight School', remainingDays: 30, usagePercent: 72 },
  { id: 5, name: 'Emergency Locator', partNumber: 'ELT-9901', directory: 'Fixed Wing', remainingDays: 45, usagePercent: 65 },
]);

// ========================
// Distribution Date Approaching
// ========================
const distributionApproaching = ref([
  { id: 10, name: 'Headset Aviation', partNumber: 'HSA-2210', assignedTo: 'Ahmet Yılmaz', distributionDate: '2026-03-28', remainingDays: 5 },
  { id: 11, name: 'Flashlight LED-X', partNumber: 'FLX-0044', assignedTo: 'Mehmet Demir', distributionDate: '2026-04-02', remainingDays: 10 },
  { id: 12, name: 'Tool Kit B-Series', partNumber: 'TKB-7712', assignedTo: 'Ayşe Kara', distributionDate: '2026-04-08', remainingDays: 16 },
  { id: 13, name: 'Safety Harness', partNumber: 'SHR-3305', assignedTo: 'Fatma Çelik', distributionDate: '2026-04-15', remainingDays: 23 },
]);

// ========================
// Expiration Date Approaching
// ========================
const expirationApproaching = ref([
  { id: 20, name: 'Epoxy Adhesive Lot-44', partNumber: 'EPA-4400', expirationDate: '2026-03-25', remainingDays: 2 },
  { id: 21, name: 'Hydraulic Fluid MIL-5606', partNumber: 'HFL-5606', expirationDate: '2026-03-30', remainingDays: 7 },
  { id: 22, name: 'Sealant PR-1422', partNumber: 'SLT-1422', expirationDate: '2026-04-05', remainingDays: 13 },
  { id: 23, name: 'Battery Pack Li-Ion 24V', partNumber: 'BPL-2400', expirationDate: '2026-04-12', remainingDays: 20 },
  { id: 24, name: 'Paint Primer MIL-P', partNumber: 'PPM-0082', expirationDate: '2026-04-20', remainingDays: 28 },
]);

// ========================
// Expired Equipments — Deactivation Table
// ========================
const expiredEquipments = ref([
  { id: 100, name: 'Lubricant Grease MIL-21164', partNumber: 'LGM-2116', serialNumber: 'SN-00211640A', directory: 'Common Use', expirationDate: '2026-02-15', expiredDaysAgo: 36, assignedTo: 'Hasan Koç', active: true },
  { id: 101, name: 'Epoxy Compound EA-9394', partNumber: 'EPC-9394', serialNumber: 'SN-00939401B', directory: 'Fixed Wing', expirationDate: '2026-02-28', expiredDaysAgo: 23, assignedTo: 'Ali Vural', active: true },
  { id: 102, name: 'O-Ring Kit Viton AS568', partNumber: 'ORK-5680', serialNumber: 'SN-00568001C', directory: 'Rotating Wing', expirationDate: '2026-03-01', expiredDaysAgo: 22, assignedTo: null, active: true },
  { id: 103, name: 'Sealant CS3204', partNumber: 'SLT-3204', serialNumber: 'SN-00320401D', directory: 'Common Use', expirationDate: '2026-03-05', expiredDaysAgo: 18, assignedTo: 'Zeynep Arslan', active: true },
  { id: 104, name: 'Adhesive Film AF-163', partNumber: 'ADF-1630', serialNumber: 'SN-00163001E', directory: 'Fixed Wing', expirationDate: '2026-03-08', expiredDaysAgo: 15, assignedTo: 'Murat Şen', active: true },
  { id: 105, name: 'Threadlocker Blue 242', partNumber: 'TLB-0242', serialNumber: 'SN-00024201F', directory: 'Flight School', expirationDate: '2026-03-10', expiredDaysAgo: 13, assignedTo: null, active: true },
  { id: 106, name: 'Corrosion Inhibitor CPC', partNumber: 'CIC-0050', serialNumber: 'SN-00005001G', directory: 'Common Use', expirationDate: '2026-03-12', expiredDaysAgo: 11, assignedTo: 'Emre Dağ', active: true },
  { id: 107, name: 'Primer EC-3960', partNumber: 'PEC-3960', serialNumber: 'SN-00396001H', directory: 'Rotating Wing', expirationDate: '2026-03-15', expiredDaysAgo: 8, assignedTo: 'Deniz Yıldız', active: true },
  { id: 108, name: 'Fuel Tank Sealant PR-1776', partNumber: 'FTS-1776', serialNumber: 'SN-00177601I', directory: 'Fixed Wing', expirationDate: '2026-03-18', expiredDaysAgo: 5, assignedTo: 'Burak Tan', active: true },
  { id: 109, name: 'Cleaning Solvent MEK', partNumber: 'CSM-0010', serialNumber: 'SN-00001001J', directory: 'Common Use', expirationDate: '2026-03-20', expiredDaysAgo: 3, assignedTo: null, active: true },
  { id: 110, name: 'RTV Silicone 738', partNumber: 'RTV-0738', serialNumber: 'SN-00073801K', directory: 'Common Use', expirationDate: '2026-03-22', expiredDaysAgo: 1, assignedTo: 'Selin Aydın', active: true },
  { id: 111, name: 'Anti-Seize Compound', partNumber: 'ASC-0025', serialNumber: 'SN-00002501L', directory: 'Rotating Wing', expirationDate: '2026-03-23', expiredDaysAgo: 0, assignedTo: 'Can Polat', active: true },
]);

const selectedExpired = ref([]);

const expiredFilters = ref({
  global: { value: null, matchMode: FilterMatchMode.CONTAINS },
});

const overdueCount = computed(() => expiredEquipments.value.filter((e) => e.expiredDaysAgo > 7).length);
const todayExpiredCount = computed(() => expiredEquipments.value.filter((e) => e.expiredDaysAgo === 0).length);

// ========================
// Deactivation Dialog
// ========================
const showDeactivateDialog = ref(false);
const deactivateTarget = ref('single'); // 'single' | 'bulk'
const singleDeactivateItem = ref(null);
const deactivating = ref(false);

function confirmSingleDeactivate(item) {
  singleDeactivateItem.value = item;
  deactivateTarget.value = 'single';
  showDeactivateDialog.value = true;
}

function confirmBulkDeactivate() {
  deactivateTarget.value = 'bulk';
  showDeactivateDialog.value = true;
}

async function executeDeactivation() {
  deactivating.value = true;

  // Simulate API call
  await new Promise((resolve) => setTimeout(resolve, 1200));

  if (deactivateTarget.value === 'single' && singleDeactivateItem.value) {
    const idx = expiredEquipments.value.findIndex((e) => e.id === singleDeactivateItem.value.id);
    if (idx > -1) {
      expiredEquipments.value[idx].active = false;
      expiredEquipments.value.splice(idx, 1);
    }
    toast.add({
      severity: 'success',
      summary: 'Deactivated',
      detail: `${singleDeactivateItem.value.name} has been successfully deactivated.`,
      life: 3000,
    });
  } else {
    const ids = selectedExpired.value.map((e) => e.id);
    expiredEquipments.value = expiredEquipments.value.filter((e) => !ids.includes(e.id));
    toast.add({
      severity: 'success',
      summary: 'Bulk Deactivation',
      detail: `${ids.length} equipment have been successfully deactivated.`,
      life: 3000,
    });
    selectedExpired.value = [];
  }

  deactivating.value = false;
  showDeactivateDialog.value = false;

  // Update KPI
  kpiCards.value[3].value = expiredEquipments.value.length;
}

// ========================
// Helpers
// ========================
function showEquipmentDetail(data) {
  toast.add({ severity: 'info', summary: 'Details', detail: `Redirecting to ${data.name} detail page.`, life: 2000 });
  // router.push({ name: 'equipment-detail', params: { id: data.id } });
}

function showEquipmentHistory(data) {
  toast.add({ severity: 'info', summary: 'History', detail: `Loading history records for ${data.name}.`, life: 2000 });
}

function formatDate(dateStr) {
  const d = new Date(dateStr);
  return d.toLocaleDateString('en-US', { day: '2-digit', month: '2-digit', year: 'numeric' });
}

function getDayFromDate(dateStr) {
  return new Date(dateStr).getDate();
}

function getMonthFromDate(dateStr) {
  return new Date(dateStr).toLocaleDateString('en-US', { month: 'short' }).toUpperCase();
}

function getDaySeverity(days) {
  if (days <= 7) return 'danger';
  if (days <= 14) return 'warning';
  return 'info';
}

function getUrgencyClass(days) {
  if (days <= 7) return 'urgency-critical';
  if (days <= 14) return 'urgency-warning';
  return '';
}

function getKnobColor(percent) {
  if (percent >= 90) return '#EF4444';
  if (percent >= 75) return '#F59E0B';
  return '#22C55E';
}

function getExpiredRowClass(data) {
  if (data.expiredDaysAgo > 30) return 'row-critical';
  if (data.expiredDaysAgo > 14) return 'row-warning';
  return '';
}
</script>

<style scoped>
/* ===========================
   Dashboard Container
   =========================== */
.dashboard-container {
  padding: 1.5rem;
  background: #f8fafc;
  min-height: 100vh;
}

/* ===========================
   KPI Cards
   =========================== */
.kpi-card {
  display: flex;
  align-items: center;
  gap: 1rem;
  background: #fff;
  border-radius: 12px;
  padding: 1.25rem 1.5rem;
  border-left: 4px solid;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06), 0 1px 2px rgba(0, 0, 0, 0.04);
  transition: transform 0.2s, box-shadow 0.2s;
}

.kpi-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.kpi-card__icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.kpi-card__icon i {
  font-size: 1.25rem;
}

.kpi-card__content {
  display: flex;
  flex-direction: column;
  flex: 1;
}

.kpi-card__value {
  font-size: 1.75rem;
  font-weight: 700;
  color: #1e293b;
  line-height: 1.2;
}

.kpi-card__label {
  font-size: 0.8rem;
  color: #64748b;
  margin-top: 2px;
}

.kpi-card__trend {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  font-size: 0.75rem;
  font-weight: 600;
  padding: 0.25rem 0.5rem;
  border-radius: 20px;
}

.trend-up {
  color: #16a34a;
  background: #f0fdf4;
}

.trend-down {
  color: #dc2626;
  background: #fef2f2;
}

/* ===========================
   Dashboard Cards
   =========================== */
.dashboard-card {
  border-radius: 12px;
  height: 100%;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
  border: 1px solid #e2e8f0;
}

.dashboard-card :deep(.p-card-title) {
  font-size: 0.9rem;
  padding-bottom: 0.75rem;
  border-bottom: 1px solid #f1f5f9;
}

.dashboard-card :deep(.p-card-content) {
  padding-top: 0.75rem;
}

.card-title {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-weight: 600;
  color: #334155;
}

.card-title i {
  font-size: 1rem;
}

.card-title__actions {
  margin-left: auto;
}

/* ===========================
   Chart Area
   =========================== */
.chart-area {
  height: 220px;
}

/* ===========================
   Chart Legend
   =========================== */
.chart-legend {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  margin-top: 1rem;
  padding-top: 0.75rem;
  border-top: 1px solid #f1f5f9;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.8rem;
}

.legend-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  flex-shrink: 0;
}

.legend-label {
  flex: 1;
  color: #64748b;
}

.legend-value {
  font-weight: 600;
  color: #334155;
}

/* ===========================
   Card Accent Borders
   =========================== */
.warning-card {
  border-top: 3px solid #F59E0B;
}

.info-card {
  border-top: 3px solid #3B82F6;
}

.danger-card {
  border-top: 3px solid #EF4444;
}

.deactivation-card {
  border-top: 3px solid #DC2626;
}

/* ===========================
   Timeline List
   =========================== */
.timeline-list {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  max-height: 420px;
  overflow-y: auto;
  padding-right: 0.25rem;
}

.timeline-list::-webkit-scrollbar {
  width: 4px;
}

.timeline-list::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 4px;
}

.timeline-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.75rem;
  border-radius: 8px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  transition: all 0.2s;
}

.timeline-item:hover {
  background: #fff;
  border-color: #cbd5e1;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.04);
}

.timeline-item.urgency-critical {
  border-left: 3px solid #EF4444;
  background: #fef2f2;
}

.timeline-item.urgency-warning {
  border-left: 3px solid #F59E0B;
  background: #fffbeb;
}

.timeline-item__indicator {
  flex-shrink: 0;
}

.timeline-item__content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.timeline-item__name {
  font-weight: 600;
  font-size: 0.85rem;
  color: #1e293b;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.timeline-item__meta {
  font-size: 0.75rem;
  color: #94a3b8;
  display: flex;
  align-items: center;
  gap: 0.3rem;
}

.timeline-item__meta i {
  font-size: 0.65rem;
}

.timeline-item__badge {
  flex-shrink: 0;
}

/* ===========================
   Date Badge
   =========================== */
.date-badge {
  width: 48px;
  height: 48px;
  border-radius: 10px;
  background: linear-gradient(135deg, #3B82F6, #2563EB);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.date-badge--danger {
  background: linear-gradient(135deg, #EF4444, #DC2626);
}

.date-badge__day {
  font-size: 1.1rem;
  font-weight: 700;
  line-height: 1;
}

.date-badge__month {
  font-size: 0.6rem;
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

/* ===========================
   Table Styles
   =========================== */
.deactivation-table :deep(.p-datatable-header) {
  background: transparent;
  border: none;
  padding: 0 0 0.75rem 0;
}

.table-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 0.75rem;
}

.table-header__info {
  display: flex;
  gap: 0.5rem;
}

.deactivation-table :deep(.p-datatable-tbody > tr.row-critical) {
  background: #fef2f2 !important;
}

.deactivation-table :deep(.p-datatable-tbody > tr.row-warning) {
  background: #fffbeb !important;
}

.equipment-cell {
  display: flex;
  flex-direction: column;
}

.equipment-cell__name {
  font-weight: 600;
  color: #1e293b;
}

.equipment-cell__serial {
  font-size: 0.75rem;
  color: #94a3b8;
}

.expiration-date {
  display: flex;
  align-items: center;
  gap: 0.35rem;
  color: #dc2626;
  font-weight: 500;
  font-size: 0.85rem;
}

.assigned-cell {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.text-muted {
  color: #94a3b8;
  font-style: italic;
  font-size: 0.85rem;
}

.action-cell {
  display: flex;
  gap: 0.25rem;
}

/* ===========================
   Empty States
   =========================== */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 2rem;
  color: #94a3b8;
  gap: 0.5rem;
}

.empty-state i {
  font-size: 2rem;
  color: #22C55E;
}

.empty-table-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 3rem;
  color: #94a3b8;
  gap: 0.75rem;
}

.empty-table-state i {
  font-size: 2.5rem;
  color: #22C55E;
}

/* ===========================
   Deactivate Dialog
   =========================== */
.deactivate-dialog__content {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}

.deactivate-dialog__icon {
  font-size: 3rem;
  color: #F59E0B;
  margin-bottom: 1rem;
}

.deactivate-dialog__content p {
  color: #334155;
  font-size: 0.95rem;
  line-height: 1.6;
}

.bulk-preview {
  width: 100%;
  max-height: 200px;
  overflow-y: auto;
}

/* ===========================
   Responsive
   =========================== */
@media (max-width: 768px) {
  .dashboard-container {
    padding: 0.75rem;
  }

  .kpi-card {
    padding: 1rem;
  }

  .kpi-card__value {
    font-size: 1.35rem;
  }

  .table-header {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
