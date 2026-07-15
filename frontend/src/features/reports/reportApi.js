import { apiClient } from '../../lib/apiClient.js';

export async function listReports() {
  const response = await apiClient.get('/reports');
  return response.data;
}

export async function createReport(payload) {
  const response = await apiClient.post('/reports', payload);
  return response.data;
}

export async function getReport(id) {
  const response = await apiClient.get(`/reports/${id}`);
  return response.data;
}

export async function retryReport(id) {
  const response = await apiClient.post(`/reports/${id}/retry`);
  return response.data;
}

