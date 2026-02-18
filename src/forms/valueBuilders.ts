import type { FieldDefinition } from '@/models/template';
import type { SheetValue, SheetValueMulti, SheetValueNumber, SheetValueTable } from '@/models/sheet';

export const buildInitialValue = (field: FieldDefinition): SheetValue => {
  if (field.type === 'BOOLEAN') {
    return false;
  }

  if (field.type === 'NUMBER') {
    if (field.isMulti) {
      const values: Record<string, string | number | null> = {};
      field.multiKeys.forEach((k) => {
        values[k] = null;
      });
      const multi: SheetValueMulti = { values };
      if (field.unitMode !== 'NONE') {
        multi.unit = field.unitMode === 'REQUIRED' ? field.unitOptions[0] ?? '' : undefined;
      }
      return multi;
    }

    const numberValue: SheetValueNumber = { value: null };
    if (field.unitMode !== 'NONE') {
      numberValue.unit = field.unitMode === 'REQUIRED' ? field.unitOptions[0] ?? '' : undefined;
    }
    return numberValue;
  }

  if (field.type === 'TABLE') {
    const rows = (field.tableSchema?.defaultRows ?? []).map(() => ({}));
    const table: SheetValueTable = { rows };
    return table;
  }

  if (field.type === 'DATE') {
    return null;
  }

  return '';
};
