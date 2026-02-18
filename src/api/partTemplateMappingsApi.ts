import type { PartTemplateMapping } from '@/models/form';

let mappings: PartTemplateMapping[] = [
  {
    partId: 'P100',
    inputTemplateIds: [1],
    outputTemplateIds: [2],
    updatedAt: new Date().toISOString(),
  },
  {
    partId: 'P200',
    inputTemplateIds: [1],
    outputTemplateIds: [2],
    updatedAt: new Date().toISOString(),
  },
];

export async function listPartTemplateMappings(): Promise<PartTemplateMapping[]> {
  return Promise.resolve(structuredClone(mappings));
}

export async function getTemplatesForPart(partId: string): Promise<{ inputTemplateIds: number[]; outputTemplateIds: number[] }> {
  const found = mappings.find((x) => x.partId === partId);
  return Promise.resolve({
    inputTemplateIds: found?.inputTemplateIds ?? [],
    outputTemplateIds: found?.outputTemplateIds ?? [],
  });
}

export async function savePartTemplateMapping(payload: {
  partId: string;
  inputTemplateIds: number[];
  outputTemplateIds: number[];
}): Promise<PartTemplateMapping> {
  const existing = mappings.find((x) => x.partId === payload.partId);
  const updated: PartTemplateMapping = {
    partId: payload.partId,
    inputTemplateIds: [...payload.inputTemplateIds],
    outputTemplateIds: [...payload.outputTemplateIds],
    updatedAt: new Date().toISOString(),
  };

  if (existing) {
    mappings = mappings.map((x) => (x.partId === payload.partId ? updated : x));
  } else {
    mappings = [...mappings, updated];
  }

  return Promise.resolve(structuredClone(updated));
}
