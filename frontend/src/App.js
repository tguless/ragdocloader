import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import MainLayout from './components/layout/MainLayout';
import Dashboard from './pages/Dashboard';
import Login from './pages/auth/Login';
import Register from './pages/auth/Register';
import TenantRegistration from './pages/auth/TenantRegistration';
import JobList from './pages/jobs/JobList';
import JobDetail from './pages/jobs/JobDetail';
import CreateJob from './pages/jobs/CreateJob';
import TenantList from './pages/tenants/TenantList';
import TenantDetail from './pages/tenants/TenantDetail';
import Profile from './pages/user/Profile';
import Settings from './pages/user/Settings';
import NotFound from './pages/NotFound';
import { useAuth } from './context/AuthContext';

function App() {
  const { isAdmin, isSystemAdmin } = useAuth();

  return (
    <Routes>
      {/* Public routes */}
      <Route path="/login" element={
        <MainLayout requireAuth={false}>
          <Login />
        </MainLayout>
      } />
      <Route path="/register" element={
        <MainLayout requireAuth={false}>
          <Register />
        </MainLayout>
      } />
      <Route path="/register-tenant" element={
        <MainLayout requireAuth={false}>
          <TenantRegistration />
        </MainLayout>
      } />

      {/* Protected routes */}
      <Route path="/" element={<Navigate to="/dashboard" replace />} />
      
      <Route path="/dashboard" element={
        <MainLayout>
          <Dashboard />
        </MainLayout>
      } />
      
      <Route path="/jobs" element={
        <MainLayout>
          <JobList />
        </MainLayout>
      } />
      
      <Route path="/jobs/create" element={
        <MainLayout>
          <CreateJob />
        </MainLayout>
      } />
      
      <Route path="/jobs/:id" element={
        <MainLayout>
          <JobDetail />
        </MainLayout>
      } />
      
      {/* System Admin-only routes */}
      <Route path="/tenants" element={
        <MainLayout>
          {isSystemAdmin ? <TenantList /> : <Navigate to="/dashboard" replace />}
        </MainLayout>
      } />
      
      <Route path="/tenants/:id" element={
        <MainLayout>
          {isSystemAdmin ? <TenantDetail /> : <Navigate to="/dashboard" replace />}
        </MainLayout>
      } />
      
      {/* User profile routes */}
      <Route path="/profile" element={
        <MainLayout>
          <Profile />
        </MainLayout>
      } />
      
      <Route path="/settings" element={
        <MainLayout>
          <Settings />
        </MainLayout>
      } />
      
      {/* Catch-all for 404 */}
      <Route path="*" element={
        <MainLayout requireAuth={false}>
          <NotFound />
        </MainLayout>
      } />
    </Routes>
  );
}

export default App; 