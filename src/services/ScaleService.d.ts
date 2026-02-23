import type {
  FieldDefinition,
  ItemMaster,
  ItemTreeNode,
  PartTemplateMapping,
  SheetInstance,
  SheetTemplate,
  TemplateKind,
  ValuesMap,
} from '@/models/form';

export type CreateSheetPayload = {
  itemId: number;
  templateId: number;
  sheetDate?: string;
  parentSheetId?: number;
  outputTemplateId?: number;
  values: ValuesMap;
};

export type CreateTemplateVersionPayload = {
  baseTemplateId?: number;
  code: string;
  name: string;
  kind: TemplateKind;
};

export type SaveFieldsAsNewVersionPayload = {
  templateId: number;
  fields: FieldDefinition[];
};

export type SavePartTemplateMappingPayload = {
  partId: string;
  inputTemplateIds: number[];
  outputTemplateIds: number[];
};

export declare class ScaleService {
  createType(type: unknown): Promise<unknown>;
  getItemsTree(): Promise<ItemTreeNode[]>;
  getItemById(itemId: number): Promise<ItemMaster | null>;
  listPartIds(): Promise<string[]>;
  createSheet(payload: CreateSheetPayload): Promise<SheetInstance>;
  getSheet(sheetId: number): Promise<{ sheet: SheetInstance; values: ValuesMap }>;
  listItemSheets(itemId: number, kind: 'INPUT' | 'OUTPUT'): Promise<SheetInstance[]>;
  listInputOutputs(inputSheetId: number): Promise<SheetInstance[]>;
  getTemplates(kind?: TemplateKind): Promise<SheetTemplate[]>;
  getTemplateFields(templateId: number): Promise<FieldDefinition[]>;
  createTemplateVersion(payload: CreateTemplateVersionPayload): Promise<SheetTemplate>;
  saveFieldsAsNewVersion(payload: SaveFieldsAsNewVersionPayload): Promise<SheetTemplate>;
  listPartTemplateMappings(): Promise<PartTemplateMapping[]>;
  getTemplatesForPart(partId: string): Promise<{ inputTemplateIds: number[]; outputTemplateIds: number[] }>;
  savePartTemplateMapping(payload: SavePartTemplateMappingPayload): Promise<PartTemplateMapping>;
}

declare const scaleService: ScaleService;

export { scaleService };
export default scaleService;
