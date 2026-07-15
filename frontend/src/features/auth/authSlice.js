import { createAsyncThunk, createSlice } from '@reduxjs/toolkit';
import { apiClient } from '../../lib/apiClient.js';

export const bootstrapSession = createAsyncThunk('auth/bootstrapSession', async (_, { rejectWithValue }) => {
  try {
    const response = await apiClient.post('/auth/refresh');
    return response.data;
  } catch {
    return rejectWithValue(null);
  }
});

export const login = createAsyncThunk('auth/login', async (payload, { rejectWithValue }) => {
  try {
    const response = await apiClient.post('/auth/login', payload);
    return response.data;
  } catch (error) {
    return rejectWithValue(error.response?.data?.message ?? 'Unable to log in');
  }
});

export const register = createAsyncThunk('auth/register', async (payload, { rejectWithValue }) => {
  try {
    const response = await apiClient.post('/auth/register', payload);
    return response.data;
  } catch (error) {
    return rejectWithValue(error.response?.data?.message ?? 'Unable to register');
  }
});

export const logout = createAsyncThunk('auth/logout', async () => {
  await apiClient.post('/auth/logout');
});

export const updateProfile = createAsyncThunk('auth/updateProfile', async (payload, { rejectWithValue }) => {
  try {
    const response = await apiClient.patch('/auth/me', payload);
    return response.data;
  } catch (error) {
    return rejectWithValue(error.response?.data?.message ?? 'Unable to update profile');
  }
});

const authSlice = createSlice({
  name: 'auth',
  initialState: {
    accessToken: null,
    user: null,
    bootstrapped: false,
    status: 'idle',
    error: null
  },
  reducers: {
    setCredentials(state, action) {
      state.accessToken = action.payload.accessToken;
      state.user = action.payload.user;
      state.bootstrapped = true;
      state.error = null;
    },
    clearSession(state) {
      state.accessToken = null;
      state.user = null;
      state.bootstrapped = true;
    }
  },
  extraReducers: (builder) => {
    builder
      .addCase(bootstrapSession.fulfilled, (state, action) => {
        state.accessToken = action.payload.accessToken;
        state.user = action.payload.user;
        state.bootstrapped = true;
      })
      .addCase(bootstrapSession.rejected, (state) => {
        state.bootstrapped = true;
      })
      .addCase(login.pending, (state) => {
        state.status = 'loading';
        state.error = null;
      })
      .addCase(login.fulfilled, (state, action) => {
        state.status = 'idle';
        state.accessToken = action.payload.accessToken;
        state.user = action.payload.user;
        state.bootstrapped = true;
      })
      .addCase(login.rejected, (state, action) => {
        state.status = 'idle';
        state.error = action.payload;
      })
      .addCase(register.fulfilled, (state, action) => {
        state.accessToken = action.payload.accessToken;
        state.user = action.payload.user;
        state.bootstrapped = true;
      })
      .addCase(logout.fulfilled, (state) => {
        state.accessToken = null;
        state.user = null;
        state.bootstrapped = true;
      })
      .addCase(updateProfile.fulfilled, (state, action) => {
        state.user = action.payload;
      });
  }
});

export const { setCredentials, clearSession } = authSlice.actions;
export default authSlice.reducer;

