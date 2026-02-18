import type { FieldDefinition, SheetTemplate, TemplateKind } from '@/models/form';

const templates: SheetTemplate[] = [
  { id: 1, code: 'CAL_IN_STD', name: 'Calibration Input Standard', kind: 'INPUT', version: 1 },
  { id: 2, code: 'FZ_OUTPUT_STD', name: 'Fz Output Standard', kind: 'OUTPUT', version: 1 },
];

const fieldsByTemplate: Record<number, FieldDefinition[]> = {
  1: [
    { id: 101, templateId: 1, key: 'operator', label: 'Operator', type: 'TEXT', required: true, orderNo: 10 },
    { id: 102, templateId: 1, key: 'ambient_temp', label: 'Ambient Temp', type: 'NUMBER', required: true, orderNo: 20, unitMode: 'REQUIRED', unitOptions: ['C', 'K'] },
    { id: 103, templateId: 1, key: 'load_range', label: 'Load Range', type: 'NUMBER', required: false, orderNo: 30, isMulti: true, multiKeys: ['from', 'to'], unitMode: 'OPTIONAL', unitOptions: ['kN'] },
    { id: 104, templateId: 1, key: 'sheet_date', label: 'Sheet Date', type: 'DATE', required: true, orderNo: 40 },
    { id: 105, templateId: 1, key: 'approved_precheck', label: 'Approved Precheck', type: 'BOOLEAN', required: false, orderNo: 50 },
    { id: 106, templateId: 1, key: 'method', label: 'Method', type: 'DROPDOWN', required: true, orderNo: 60, dropdownOptions: ['Method A', 'Method B'] },
  ],
  2: [
    {
      id: 201,
      templateId: 2,
      key: 'fz_grid',
      label: 'Fz Table',
      type: 'TABLE',
      required: true,
      orderNo: 10,
      tableSchema: {
        rowMode: 'CONFIGURABLE',
        defaultRows: ['0', '20', '40', '60', '80', '100', '80', '60', '40', '20', '0'],
        columns: [
          { key: 'percent', label: '%', type: 'NUMBER', unitMode: 'NONE' },
          { key: 'kn', label: 'kN', type: 'NUMBER', unitMode: 'OPTIONAL', unitOptions: ['kN'] },
          { key: 'mv_v', label: 'mV/V', type: 'NUMBER', unitMode: 'OPTIONAL', unitOptions: ['mV/V'] },
        ],
      },
    },
    { id: 202, templateId: 2, key: 'result', label: 'Result', type: 'DROPDOWN', required: true, orderNo: 20, dropdownOptions: ['PASS', 'FAIL'] },
  ],
};

let nextTemplateId = 10;

export async function getTemplates(kind?: TemplateKind): Promise<SheetTemplate[]> {
  return Promise.resolve(kind ? templates.filter((t) => t.kind === kind) : templates);
}

export async function getTemplateFields(templateId: number): Promise<FieldDefinition[]> {
  return Promise.resolve(structuredClone(fieldsByTemplate[templateId] ?? []));
}

export async function createTemplateVersion(payload: {
  baseTemplateId?: number;
  code: string;
  name: string;
  kind: TemplateKind;
}): Promise<SheetTemplate> {
  const versions = templates.filter((t) => t.code === payload.code).map((t) => t.version);
  const created: SheetTemplate = {
    id: nextTemplateId++,
    code: payload.code,
    name: payload.name,
    kind: payload.kind,
    version: versions.length ? Math.max(...versions) + 1 : 1,
  };
  templates.unshift(created);

  const baseFields = payload.baseTemplateId ? (fieldsByTemplate[payload.baseTemplateId] ?? []) : [];
  fieldsByTemplate[created.id] = baseFields.map((f, i) => ({ ...structuredClone(f), id: Date.now() + i, templateId: created.id }));
  return Promise.resolve(structuredClone(created));
}

export async function saveFieldsAsNewVersion(payload: {
  templateId: number;
  fields: FieldDefinition[];
}): Promise<SheetTemplate> {
  const current = templates.find((t) => t.id === payload.templateId);
  if (!current) {
    throw new Error('Template not found');
  }

  const created = await createTemplateVersion({
    baseTemplateId: payload.templateId,
    code: current.code,
    name: current.name,
    kind: current.kind,
  });

  fieldsByTemplate[created.id] = payload.fields.map((f, i) => ({
    ...structuredClone(f),
    id: Date.now() + i,
    templateId: created.id,
    orderNo: (i + 1) * 10,
  }));

  return Promise.resolve(created);
}
