import type { FieldDefinition, SheetTemplate, TemplateKind } from '@/models/template';

const makeField = (overrides: Partial<FieldDefinition>): FieldDefinition => ({
  id: crypto.randomUUID(),
  templateId: '',
  key: '',
  label: '',
  type: 'TEXT',
  required: false,
  orderNo: 1,
  groupKey: '',
  isMulti: false,
  multiKeys: [],
  unitMode: 'NONE',
  unitOptions: [],
  dropdownOptions: [],
  ...overrides,
});

let templates: SheetTemplate[] = [
  {
    id: 'T-IN-1',
    code: 'CAL-IN-A',
    name: 'Calibration Input A',
    kind: 'INPUT',
    version: 1,
    fields: [
      makeField({ id: 'F-IN-1', templateId: 'T-IN-1', key: 'operator', label: 'Operator', type: 'TEXT', required: true, orderNo: 1 }),
      makeField({ id: 'F-IN-2', templateId: 'T-IN-1', key: 'ambientTemp', label: 'Ambient Temp', type: 'NUMBER', required: true, orderNo: 2, unitMode: 'REQUIRED', unitOptions: ['C', 'F'] }),
      makeField({ id: 'F-IN-3', templateId: 'T-IN-1', key: 'range', label: 'Range', type: 'NUMBER', required: false, orderNo: 3, isMulti: true, multiKeys: ['from', 'to'], unitMode: 'OPTIONAL', unitOptions: ['V', 'mV'] }),
      makeField({ id: 'F-IN-4', templateId: 'T-IN-1', key: 'calDate', label: 'Calibration Date', type: 'DATE', required: true, orderNo: 4 }),
      makeField({ id: 'F-IN-5', templateId: 'T-IN-1', key: 'passedPrecheck', label: 'Passed Precheck', type: 'BOOLEAN', required: true, orderNo: 5 }),
      makeField({ id: 'F-IN-6', templateId: 'T-IN-1', key: 'notesType', label: 'Notes Type', type: 'DROPDOWN', required: false, orderNo: 6, dropdownOptions: ['General', 'Critical', 'N/A'] }),
      makeField({
        id: 'F-IN-7',
        templateId: 'T-IN-1',
        key: 'readings',
        label: 'Readings',
        type: 'TABLE',
        required: false,
        orderNo: 7,
        tableSchema: {
          rowMode: 'CONFIGURABLE',
          defaultRows: ['R1', 'R2'],
          columns: [
            { key: 'point', label: 'Point', type: 'TEXT', unitMode: 'NONE', unitOptions: [], dropdownOptions: [] },
            { key: 'value', label: 'Value', type: 'NUMBER', unitMode: 'OPTIONAL', unitOptions: ['V', 'mV'], dropdownOptions: [] },
            { key: 'status', label: 'Status', type: 'DROPDOWN', unitMode: 'NONE', unitOptions: [], dropdownOptions: ['OK', 'WARN', 'FAIL'] },
          ],
        },
      }),
    ],
  },
  {
    id: 'T-OUT-1',
    code: 'CAL-OUT-A',
    name: 'Calibration Output A',
    kind: 'OUTPUT',
    version: 1,
    fields: [
      makeField({ id: 'F-OUT-1', templateId: 'T-OUT-1', key: 'reviewer', label: 'Reviewer', type: 'TEXT', required: true, orderNo: 1 }),
      makeField({ id: 'F-OUT-2', templateId: 'T-OUT-1', key: 'approved', label: 'Approved', type: 'BOOLEAN', required: true, orderNo: 2 }),
      makeField({ id: 'F-OUT-3', templateId: 'T-OUT-1', key: 'resultBand', label: 'Result Band', type: 'DROPDOWN', required: true, orderNo: 3, dropdownOptions: ['A', 'B', 'C'] }),
    ],
  },
];

const nextVersion = (code: string) => {
  const versions = templates.filter((t) => t.code === code).map((t) => t.version);
  return versions.length ? Math.max(...versions) + 1 : 1;
};

export const templateApi = {
  async listTemplates(kind?: TemplateKind): Promise<SheetTemplate[]> {
    const data = kind ? templates.filter((t) => t.kind === kind) : templates;
    return Promise.resolve(structuredClone(data));
  },

  async saveAsNewVersion(template: Omit<SheetTemplate, 'id' | 'version'>): Promise<SheetTemplate> {
    const created: SheetTemplate = {
      ...structuredClone(template),
      id: `T-${template.kind}-${crypto.randomUUID().slice(0, 8)}`,
      version: nextVersion(template.code),
      fields: template.fields.map((f, idx) => ({
        ...structuredClone(f),
        id: `F-${crypto.randomUUID().slice(0, 8)}`,
        templateId: '',
        orderNo: idx + 1,
      })),
    };

    created.fields = created.fields.map((f) => ({ ...f, templateId: created.id }));
    templates = [...templates, created];
    return Promise.resolve(structuredClone(created));
  },
};
