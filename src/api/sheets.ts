import type { ApiError } from '@/models/api';
import type { NewSheetPayload, Sheet } from '@/models/sheet';

let inputSheets: Sheet[] = [
  {
    id: 'S-IN-1',
    itemId: 'I-100',
    templateId: 'T-IN-1',
    templateCode: 'CAL-IN-A',
    templateVersion: 1,
    createdAt: '2026-01-10T09:00:00.000Z',
    values: {
      operator: 'Alice',
      ambientTemp: { value: 25, unit: 'C' },
      calDate: '2026-01-10',
      passedPrecheck: true,
    },
  },
];

let outputSheets: Array<Sheet & { inputSheetId: string }> = [
  {
    id: 'S-OUT-1',
    itemId: 'I-100',
    templateId: 'T-OUT-1',
    templateCode: 'CAL-OUT-A',
    templateVersion: 1,
    createdAt: '2026-01-11T09:00:00.000Z',
    inputSheetId: 'S-IN-1',
    values: {
      reviewer: 'Bob',
      approved: true,
      resultBand: 'A',
    },
  },
];

export const sheetApi = {
  async listInputSheets(itemId: string): Promise<Sheet[]> {
    return Promise.resolve(inputSheets.filter((s) => s.itemId === itemId).map((s) => structuredClone(s)));
  },

  async listOutputSheets(itemId: string, inputSheetId?: string): Promise<Array<Sheet & { inputSheetId: string }>> {
    const data = outputSheets.filter((s) => s.itemId === itemId && (!inputSheetId || s.inputSheetId === inputSheetId));
    return Promise.resolve(data.map((s) => structuredClone(s)));
  },

  async createInputSheet(payload: NewSheetPayload): Promise<Sheet> {
    const created: Sheet = {
      id: `S-IN-${crypto.randomUUID().slice(0, 8)}`,
      itemId: payload.itemId,
      templateId: payload.templateId,
      templateCode: 'INPUT-TEMPLATE',
      templateVersion: 1,
      createdAt: new Date().toISOString(),
      values: structuredClone(payload.values),
    };
    inputSheets = [created, ...inputSheets];
    return Promise.resolve(structuredClone(created));
  },

  async createOutputSheet(payload: NewSheetPayload): Promise<Sheet & { inputSheetId: string }> {
    if (!payload.inputSheetId) {
      const error: ApiError = { status: 400, message: 'inputSheetId is required for output sheet' };
      return Promise.reject(error);
    }

    const duplicate = outputSheets.find(
      (s) => s.inputSheetId === payload.inputSheetId && s.templateId === payload.templateId,
    );

    if (duplicate) {
      const error: ApiError = {
        status: 409,
        message: 'Output already exists for this input sheet and output template',
      };
      return Promise.reject(error);
    }

    const created: Sheet & { inputSheetId: string } = {
      id: `S-OUT-${crypto.randomUUID().slice(0, 8)}`,
      itemId: payload.itemId,
      inputSheetId: payload.inputSheetId,
      templateId: payload.templateId,
      templateCode: 'OUTPUT-TEMPLATE',
      templateVersion: 1,
      createdAt: new Date().toISOString(),
      values: structuredClone(payload.values),
    };

    outputSheets = [created, ...outputSheets];
    return Promise.resolve(structuredClone(created));
  },
};
