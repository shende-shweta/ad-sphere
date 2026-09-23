import { api } from './client.js';

export const authApi = {
  login: (username, password) => api.post('/api/auth/login', { username, password }),
  me: () => api.get('/api/auth/me'),
};

export const lookupApi = {
  all: () => api.get('/api/lookups'),
};

export const campaignApi = {
  list: (params, signal) => api.get('/api/campaigns', params, signal),
  get: (id) => api.get(`/api/campaigns/${id}`),
  create: (body) => api.post('/api/campaigns', body),
  update: (id, body) => api.put(`/api/campaigns/${id}`, body),
  remove: (id) => api.del(`/api/campaigns/${id}`),
};

export const placementApi = {
  list: (params, signal) => api.get('/api/placements', params, signal),
  create: (body) => api.post('/api/placements', body),
};

export const audienceApi = {
  list: (params, signal) => api.get('/api/audiences', params, signal),
  get: (id) => api.get(`/api/audiences/${id}`),
  create: (body) => api.post('/api/audiences', body),
  update: (id, body) => api.put(`/api/audiences/${id}`, body),
  setStatus: (id, status) => api.patch(`/api/audiences/${id}/status`, { status }),
};

export const dealApi = {
  list: (params, signal) => api.get('/api/deals', params, signal),
  bid: (id, body) => api.post(`/api/deals/${id}/bids`, body),
};

export const settingsApi = {
  get: () => api.get('/api/settings'),
  update: (body) => api.put('/api/settings', body),
};

export const profileApi = {
  get: () => api.get('/api/profile'),
  update: (body) => api.put('/api/profile', body),
  changePassword: (body) => api.put('/api/profile/password', body),
};

export const helpApi = {
  faqs: () => api.get('/api/help/faqs'),
  createTicket: (body) => api.post('/api/help/tickets', body),
};
