import BaseService from './BaseService';

class ScaleService extends BaseService {
  createType(type) {
    return this.http.post('/scale/createType', type);
  }

  async getItemsTree() {
    const { data } = await this.http.get('/scale/items/tree');
    return data;
  }

  async getItemById(itemId) {
    const { data } = await this.http.get(`/scale/items/${itemId}`);
    return data;
  }

  async listPartIds() {
    const { data } = await this.http.get('/scale/items/partIds');
    return data;
  }

  async createSheet(payload) {
    const { data } = await this.http.post('/scale/sheets', payload);
    return data;
  }

  async getSheet(sheetId) {
    const { data } = await this.http.get(`/scale/sheets/${sheetId}`);
    return data;
  }

  async listItemSheets(itemId, kind) {
    const { data } = await this.http.get('/scale/sheets', {
      params: { itemId, kind },
    });
    return data;
  }

  async listInputOutputs(inputSheetId) {
    const { data } = await this.http.get(`/scale/sheets/${inputSheetId}/outputs`);
    return data;
  }

  async getTemplates(kind) {
    const { data } = await this.http.get('/scale/templates', {
      params: kind ? { kind } : undefined,
    });
    return data;
  }

  async getTemplateFields(templateId) {
    const { data } = await this.http.get(`/scale/templates/${templateId}/fields`);
    return data;
  }

  async createTemplateVersion(payload) {
    const { data } = await this.http.post('/scale/templates/version', payload);
    return data;
  }

  async saveFieldsAsNewVersion(payload) {
    const { data } = await this.http.post('/scale/templates/fields/version', payload);
    return data;
  }

  async listPartTemplateMappings() {
    const { data } = await this.http.get('/scale/part-template-mappings');
    return data;
  }

  async getTemplatesForPart(partId) {
    const { data } = await this.http.get(`/scale/part-template-mappings/${partId}`);
    return data;
  }

  async savePartTemplateMapping(payload) {
    const { data } = await this.http.post('/scale/part-template-mappings', payload);
    return data;
  }
}

const scaleService = new ScaleService();

export { ScaleService, scaleService };
export default scaleService;
