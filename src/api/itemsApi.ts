import type { ItemMaster, ItemTreeNode } from '@/models/form';

const itemMaster: ItemMaster[] = [
  { id: 1, partId: 'P100', partSn: 'SN001', bridgeName: 'Bridge-A' },
  { id: 2, partId: 'P100', partSn: 'SN001', bridgeName: 'Bridge-B' },
  { id: 3, partId: 'P200', partSn: 'SN888', bridgeName: 'Bridge-Z' },
];

const tree: ItemTreeNode[] = [
  {
    key: 'part-P100',
    label: 'Part P100',
    type: 'PART',
    children: [
      {
        key: 'sn-SN001',
        label: 'SN001',
        type: 'SERIAL',
        children: [
          { key: 'bridge-A', label: 'Bridge-A', type: 'BRIDGE', itemId: 1 },
          { key: 'bridge-B', label: 'Bridge-B', type: 'BRIDGE', itemId: 2 },
        ],
      },
    ],
  },
  {
    key: 'part-P200',
    label: 'Part P200',
    type: 'PART',
    children: [
      {
        key: 'sn-SN888',
        label: 'SN888',
        type: 'SERIAL',
        children: [{ key: 'bridge-Z', label: 'Bridge-Z', type: 'BRIDGE', itemId: 3 }],
      },
    ],
  },
];

export async function getItemsTree(): Promise<ItemTreeNode[]> {
  return Promise.resolve(structuredClone(tree));
}

export async function getItemById(itemId: number): Promise<ItemMaster | null> {
  return Promise.resolve(itemMaster.find((x) => x.id === itemId) ?? null);
}

export async function listPartIds(): Promise<string[]> {
  return Promise.resolve(Array.from(new Set(itemMaster.map((x) => x.partId))).sort());
}
