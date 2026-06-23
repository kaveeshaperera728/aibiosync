import axios from 'axios';

// Use a relative path so it works in production via Nginx proxy, 
// and locally via Vite proxy (if configured). 
const API_URL = import.meta.env.PROD ? '/api' : 'http://localhost:8081/api';

const api = axios.create({
  baseURL: API_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Device APIs
export const getDevices = () => api.get('/devices');
export const addDevice = (device) => api.post('/devices', device);
export const deleteDevice = (id) => api.delete(`/devices/${id}`);
export const getMissingUsers = (id) => api.get(`/devices/${id}/missing-users`);
export const provisionUsers = (id, userIds) => api.post(`/devices/${id}/provision-users`, userIds);

// Employee APIs
export const getEmployees = () => api.get('/employees');

// Attendance APIs
export const getAttendanceLogs = () => api.get('/attendance');
export const getDailyAttendance = (date) => api.get(`/attendance/daily${date ? `?date=${date}` : ''}`);
export const getMonthlyReport = (year, month) => api.get(`/reports/monthly?year=${year}&month=${month}`);

// Command APIs
export const syncUsersFromDevice = () => api.post('/commands/sync-users');

// Dashboard APIs
export const getDashboardStats = () => api.get('/dashboard/stats');

export default api;
