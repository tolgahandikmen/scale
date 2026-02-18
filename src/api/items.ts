import type { Item } from '@/models/sheet';

const items: Item[] = [
  { id: 'I-100', part_id: 'P-100', part_sn: 'SN-001', bridge_name: 'Bridge-A', label: 'P-100 / SN-001 / Bridge-A' },
  { id: 'I-101', part_id: 'P-101', part_sn: 'SN-145', bridge_name: 'Bridge-B', label: 'P-101 / SN-145 / Bridge-B' },
  { id: 'I-102', part_id: 'P-100', part_sn: 'SN-877', bridge_name: 'Bridge-C', label: 'P-100 / SN-877 / Bridge-C' },
];

export const itemApi = {
  async listItems(): Promise<Item[]> {
    return Promise.resolve([...items]);
  },
};
