export interface Item {
  id: string;
  part_id: string;
  part_sn: string;
  bridge_name: string;
  label: string;
}

export interface SheetValueNumber {
  value: number | null;
  unit?: string;
}

export interface SheetValueMulti {
  values: Record<string, string | number | null>;
  unit?: string;
}

export interface SheetValueTable {
  rows: Array<Record<string, unknown>>;
}

export type SheetValue = string | boolean | null | SheetValueNumber | SheetValueMulti | SheetValueTable;

export interface Sheet {
  id: string;
  itemId: string;
  templateId: string;
  templateCode: string;
  templateVersion: number;
  createdAt: string;
  values: Record<string, SheetValue>;
}

export interface NewSheetPayload {
  itemId: string;
  templateId: string;
  inputSheetId?: string;
  values: Record<string, SheetValue>;
}
