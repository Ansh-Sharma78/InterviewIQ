import { apiClient } from '../../lib/apiClient.js';

export async function listJobDescriptions() {
  const response = await apiClient.get('/job-descriptions');
  return response.data;
}

export async function getJobDescription(id) {
  const response = await apiClient.get(`/job-descriptions/${id}`);
  return response.data;
}

export async function createJobDescriptionText(payload) {
  const response = await apiClient.post('/job-descriptions/text', payload);
  return response.data;
}

export async function createJobDescriptionPdf({ file, companyName, roleTitle }) {
  const data = new FormData();
  data.append('file', file);
  if (companyName) data.append('companyName', companyName);
  if (roleTitle) data.append('roleTitle', roleTitle);
  const response = await apiClient.post('/job-descriptions/pdf', data);
  return response.data;
}

export async function deleteJobDescription(id) {
  await apiClient.delete(`/job-descriptions/${id}`);
}
