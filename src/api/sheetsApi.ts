import type { SheetInstance, ValuesMap } from '@/models/form';

type CreatePayload = {
  itemId: number;
  templateId: number;
  sheetDate?: string;
  parentSheetId?: number;
  outputTemplateId?: number;
  values: ValuesMap;
};

let nextSheetId = 1000;

const sheets: Array<SheetInstance & { kind: 'INPUT' | 'OUTPUT'; values: ValuesMap }> = [
  {
    id: 501,
    itemId: 1,
    templateId: 1,
    createdAt: '2026-02-10T09:00:00.000Z',
    sheetDate: '2026-02-10',
    kind: 'INPUT',
    values: {
      101: { type: 'TEXT', value: 'Operator A' },
    },
  },
  {
    id: 601,
    itemId: 1,
    templateId: 2,
    createdAt: '2026-02-11T09:00:00.000Z',
    sheetDate: '2026-02-11',
    parentSheetId: 501,
    outputTemplateId: 2,
    kind: 'OUTPUT',
    values: {
      202: { type: 'DROPDOWN', value: 'PASS' },
    },
  },
];

export async function createSheet(payload: CreatePayload): Promise<SheetInstance> {
  if (payload.parentSheetId && payload.outputTemplateId) {
    const exists = sheets.find(
      (s) =>
        s.kind === 'OUTPUT' &&
        s.parentSheetId === payload.parentSheetId &&
        s.outputTemplateId === payload.outputTemplateId,
    );

    if (exists) {
      const error = { response: { status: 409 } };
      return Promise.reject(error);
    }
  }

  const created: SheetInstance & { kind: 'INPUT' | 'OUTPUT'; values: ValuesMap } = {
    id: nextSheetId++,
    itemId: payload.itemId,
    templateId: payload.templateId,
    createdAt: new Date().toISOString(),
    sheetDate: payload.sheetDate,
    parentSheetId: payload.parentSheetId,
    outputTemplateId: payload.outputTemplateId,
    kind: payload.parentSheetId ? 'OUTPUT' : 'INPUT',
    values: structuredClone(payload.values),
  };

  sheets.unshift(created);
  return Promise.resolve(structuredClone(created));
}

export async function getSheet(sheetId: number): Promise<{ sheet: SheetInstance; values: ValuesMap }> {
  const found = sheets.find((s) => s.id === sheetId);
  if (!found) {
    throw new Error('Sheet not found');
  }
  return Promise.resolve({ sheet: structuredClone(found), values: structuredClone(found.values) });
}

export async function listItemSheets(itemId: number, kind: 'INPUT' | 'OUTPUT'): Promise<SheetInstance[]> {
  return Promise.resolve(
    sheets
      .filter((s) => s.itemId === itemId && s.kind === kind)
      .map((s) => ({
        id: s.id,
        itemId: s.itemId,
        templateId: s.templateId,
        createdAt: s.createdAt,
        sheetDate: s.sheetDate,
        parentSheetId: s.parentSheetId,
        outputTemplateId: s.outputTemplateId,
      })),
  );
}

export async function listInputOutputs(inputSheetId: number): Promise<SheetInstance[]> {
  return Promise.resolve(
    sheets
      .filter((s) => s.kind === 'OUTPUT' && s.parentSheetId === inputSheetId)
      .map((s) => ({
        id: s.id,
        itemId: s.itemId,
        templateId: s.templateId,
        createdAt: s.createdAt,
        sheetDate: s.sheetDate,
        parentSheetId: s.parentSheetId,
        outputTemplateId: s.outputTemplateId,
      })),
  );
}
