import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Layout from './components/Layout';
import Dashboard from './pages/Dashboard';
import Devices from './pages/Devices';
import Employees from './pages/Employees';
import Attendance from './pages/Attendance';
import Reports from './pages/Reports';

// Placeholder pages for later implementation
const PlaceholderPage = ({ title }) => (
  <div className="panel p-8 rounded-2xl flex items-center justify-center min-h-[400px]">
    <h1 className="heading-page text-text-dim">{title} Page (Coming Soon)</h1>
  </div>
);

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<Layout />}>
          <Route index element={<Dashboard />} />
          <Route path="devices" element={<Devices />} />
          <Route path="employees" element={<Employees />} />
          <Route path="attendance" element={<Attendance />} />
          <Route path="reports" element={<Reports />} />
          <Route path="settings" element={<PlaceholderPage title="System Settings" />} />
        </Route>
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </Router>
  );
}

export default App;
