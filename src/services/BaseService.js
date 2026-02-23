import { http } from '@/api/http';

export default class BaseService {
  constructor(client = http) {
    this.http = client;
  }
}
