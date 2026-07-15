import { apiClient } from '../../lib/apiClient.js';

export async function createChatSession(reportId) {
  const response = await apiClient.post('/chat/sessions', { reportId });
  return response.data;
}

export async function listChatSessions() {
  const response = await apiClient.get('/chat/sessions');
  return response.data;
}

export async function getChatSession(sessionId) {
  const response = await apiClient.get(`/chat/sessions/${sessionId}`);
  return response.data;
}

export async function renameChatSession(sessionId, title) {
  const response = await apiClient.patch(`/chat/sessions/${sessionId}`, { title });
  return response.data;
}

export async function deleteChatSession(sessionId) {
  await apiClient.delete(`/chat/sessions/${sessionId}`);
}

export async function listMessages(sessionId) {
  const response = await apiClient.get(`/chat/sessions/${sessionId}/messages`);
  return response.data;
}

export async function sendMessage(sessionId, content) {
  const response = await apiClient.post(`/chat/sessions/${sessionId}/messages`, { content });
  return response.data;
}
