import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { Provider } from 'react-redux';
import { QueryClient, QueryClientProvider } from 'react-query';
import { Toaster } from 'react-hot-toast';
import { store } from './store';
import Layout from './components/Layout';
import Dashboard from './pages/Dashboard';
import Employees from './pages/Employees';
import LeaveManagement from './pages/LeaveManagement';
import Attendance from './pages/Attendance';
import Performance from './pages/Performance';
import Recruitment from './pages/Recruitment';
import Analytics from './pages/Analytics';
import Settings from './pages/Settings';
import Login from './pages/Login';
import { AuthProvider } from './context/AuthContext';
import PrivateRoute from './components/PrivateRoute';
import './index.css';

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: 3,
      retryDelay: 1000,
      staleTime: 5 * 60 * 1000, // 5 minutes
      cacheTime: 10 * 60 * 1000, // 10 minutes
    },
  },
});

function App() {
  return (
    <Provider store={store}>
      <QueryClientProvider client={queryClient}>
        <AuthProvider>
          <Router>
            <div className="App">
              <Toaster
                position="top-right"
                toastOptions={{
                  duration: 4000,
                  style: {
                    background: '#1e293b',
                    color: '#f8fafc',
                    border: '1px solid #334155',
                  },
                  success: {
                    style: {
                      background: '#047857',
                      color: '#f0fdf4',
                    },
                  },
                  error: {
                    style: {
                      background: '#b91c1c',
                      color: '#fef2f2',
                    },
                  },
                }}
              />
              <Routes>
                <Route path="/login" element={<Login />} />
                <Route
                  path="/*"
                  element={
                    <PrivateRoute>
                      <Layout>
                        <Routes>
                          <Route index element={<Dashboard />} />
                          <Route path="employees/*" element={<Employees />} />
                          <Route path="leave/*" element={<LeaveManagement />} />
                          <Route path="attendance/*" element={<Attendance />} />
                          <Route path="performance/*" element={<Performance />} />
                          <Route path="recruitment/*" element={<Recruitment />} />
                          <Route path="analytics/*" element={<Analytics />} />
                          <Route path="settings/*" element={<Settings />} />
                        </Routes>
                      </Layout>
                    </PrivateRoute>
                  }
                />
              </Routes>
            </div>
          </Router>
        </AuthProvider>
      </QueryClientProvider>
    </Provider>
  );
}

export default App;