import axios from 'axios';
import { clearSession, setCredentials } from '../features/auth/authSlice.js';

const baseURL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8081/api/v1';

export const apiClient = axios.create({
  baseURL,
  withCredentials: true
});

let storeRef;
let refreshPromise = null;

export function attachStore(store) {
  storeRef = store;
}

apiClient.interceptors.request.use((config) => {
  const publicAuthPath = ['/auth/login', '/auth/register', '/auth/refresh'].some((path) => config.url?.includes(path));
  if (publicAuthPath) {
    delete config.headers.Authorization;
    return config;
  }

  const token = storeRef?.getState().auth.accessToken;
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

apiClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    const original = error.config;
    if (error.response?.status !== 401 || original?._retry || original?.url?.includes('/auth/refresh')) {
      return Promise.reject(error);
    }

    original._retry = true;
    try {
      refreshPromise ??= apiClient.post('/auth/refresh');
      const response = await refreshPromise;
      refreshPromise = null;
      storeRef.dispatch(setCredentials(response.data));
      return apiClient(original);
    } catch (refreshError) {
      refreshPromise = null;
      storeRef?.dispatch(clearSession());
      return Promise.reject(refreshError);
    }
  }
);
