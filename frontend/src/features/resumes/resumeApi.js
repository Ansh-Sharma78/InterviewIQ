import { apiClient } from '../../lib/apiClient.js';

export async function listResumes() {
  const response = await apiClient.get('/resumes');
  return response.data;
}

export async function getResume(id) {
  const response = await apiClient.get(`/resumes/${id}`);
  return response.data;
}

export async function uploadResume(file) {
  const data = new FormData();
  data.append('file', file);
  const response = await apiClient.post('/resumes', data);
  return response.data;
}

export async function deleteResume(id) {
  await apiClient.delete(`/resumes/${id}`);
}
