export type TemplateKind = 'INPUT' | 'OUTPUT';

export type FieldType = 'TEXT' | 'NUMBER' | 'DATE' | 'DROPDOWN' | 'BOOLEAN' | 'TABLE';

export type UnitMode = 'NONE' | 'OPTIONAL' | 'REQUIRED';

export interface TableColumnDefinition {
  key: string;
  label: string;
  type: 'NUMBER' | 'TEXT' | 'DROPDOWN' | 'BOOLEAN';
  unitMode: UnitMode;
  unitOptions: string[];
  dropdownOptions: string[];
}

export interface TableSchema {
  rowMode: 'CONFIGURABLE';
  defaultRows: string[];
  columns: TableColumnDefinition[];
}

export interface FieldDefinition {
  id: string;
  templateId: string;
  key: string;
  label: string;
  type: FieldType;
  required: boolean;
  orderNo: number;
  groupKey?: string;
  isMulti: boolean;
  multiKeys: string[];
  unitMode: UnitMode;
  unitOptions: string[];
  dropdownOptions: string[];
  tableSchema?: TableSchema;
}

export interface SheetTemplate {
  id: string;
  code: string;
  name: string;
  kind: TemplateKind;
  version: number;
  fields: FieldDefinition[];
}
