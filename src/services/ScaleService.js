import BaseService from './BaseService';

class ScaleService extends BaseService {
  unwrapResponse(payload) {
    if (payload && typeof payload === 'object' && 'status' in payload && 'data' in payload) {
      if (payload.status === false) {
        throw new Error(payload.detail || 'Service returned error');
      }
      return payload.data;
    }
    return payload;
  }

  createType(type) {
    return this.http.post('/scale/createType', type).then(({ data }) => this.unwrapResponse(data));
  }

  async getItemsTree() {
    const { data } = await this.http.get('/scale/items/tree');
    return this.unwrapResponse(data);
  }

  async getItemById(itemId) {
    const { data } = await this.http.get(`/scale/items/${itemId}`);
    return this.unwrapResponse(data);
  }

  async listPartIds() {
    const { data } = await this.http.get('/scale/items/partIds');
    return this.unwrapResponse(data);
  }

  async createSheet(payload) {
    const { data } = await this.http.post('/scale/sheets', payload);
    return this.unwrapResponse(data);
  }

  async getSheet(sheetId) {
    const { data } = await this.http.get(`/scale/sheets/${sheetId}`);
    return this.unwrapResponse(data);
  }

  async listItemSheets(itemId, kind) {
    const { data } = await this.http.get('/scale/sheets', {
      params: { itemId, kind },
    });
    return this.unwrapResponse(data);
  }

  async listInputOutputs(inputSheetId) {
    const { data } = await this.http.get(`/scale/sheets/${inputSheetId}/outputs`);
    return this.unwrapResponse(data);
  }

  async getTemplates(kind) {
    const { data } = await this.http.get('/scale/templates', {
      params: kind ? { kind } : undefined,
    });
    return this.unwrapResponse(data);
  }

  async getTemplateFields(templateId) {
    const { data } = await this.http.get(`/scale/templates/${templateId}/fields`);
    return this.unwrapResponse(data);
  }

  async createTemplateVersion(payload) {
    const { data } = await this.http.post('/scale/templates/version', payload);
    return this.unwrapResponse(data);
  }

  async saveFieldsAsNewVersion(payload) {
    const { data } = await this.http.post('/scale/templates/fields/version', payload);
    return this.unwrapResponse(data);
  }

  async listPartTemplateMappings() {
    const { data } = await this.http.get('/scale/part-template-mappings');
    return this.unwrapResponse(data);
  }

  async getTemplatesForPart(partId) {
    const { data } = await this.http.get(`/scale/part-template-mappings/${partId}`);
    return this.unwrapResponse(data);
  }

  async savePartTemplateMapping(payload) {
    const { data } = await this.http.post('/scale/part-template-mappings', payload);
    return this.unwrapResponse(data);
  }
}

const scaleService = new ScaleService();

export { ScaleService, scaleService };
export default scaleService;
