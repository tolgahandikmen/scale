export type TemplateKind = 'INPUT' | 'OUTPUT';

export type FieldType = 'TEXT' | 'NUMBER' | 'DATE' | 'DROPDOWN' | 'BOOLEAN' | 'TABLE';

export type UnitMode = 'NONE' | 'OPTIONAL' | 'REQUIRED';

export interface SheetTemplate {
  id: number;
  code: string;
  name: string;
  kind: TemplateKind;
  version: number;
}

export interface TableColumnSchema {
  key: string;
  label: string;
  type: 'NUMBER' | 'TEXT' | 'DROPDOWN' | 'BOOLEAN';
  unitMode?: UnitMode;
  unitOptions?: string[];
  dropdownOptions?: string[];
}

export interface TableSchema {
  rowMode: 'CONFIGURABLE';
  defaultRows: (string | number)[];
  columns: TableColumnSchema[];
}

export interface FieldDefinition {
  id: number;
  templateId: number;
  key: string;
  label: string;
  type: FieldType;
  required: boolean;
  orderNo: number;
  groupKey?: string;
  isMulti?: boolean;
  multiKeys?: string[];
  unitMode?: UnitMode;
  unitOptions?: string[];
  dropdownOptions?: string[];
  tableSchema?: TableSchema;
}

export interface SheetInstance {
  id: number;
  itemId: number;
  templateId: number;
  createdAt: string;
  sheetDate?: string;
  parentSheetId?: number;
  outputTemplateId?: number;
}

export interface ItemTreeNode {
  key: string;
  label: string;
  type: 'PART' | 'SERIAL' | 'BRIDGE';
  itemId?: number;
  children?: ItemTreeNode[];
}

export interface ItemMaster {
  id: number;
  partId: string;
  partSn: string;
  bridgeName: string;
}

export interface PartTemplateMapping {
  partId: string;
  inputTemplateIds: number[];
  outputTemplateIds: number[];
  updatedAt: string;
}

export type FieldValuePrimitive =
  | { type: 'TEXT'; value: string | null }
  | { type: 'NUMBER'; value: number | null; unit?: string | null }
  | { type: 'DATE'; value: string | null }
  | { type: 'BOOLEAN'; value: boolean | null }
  | { type: 'DROPDOWN'; value: string | null }
  | { type: 'MULTI'; value: Record<string, string | number | null>; unit?: string | null }
  | { type: 'TABLE'; value: { rows: Record<string, unknown>[] } };

export type ValuesMap = Record<number, FieldValuePrimitive>;
