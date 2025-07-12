import React from 'react';
import { motion } from 'framer-motion';
import {
  UsersIcon,
  CalendarIcon,
  ClockIcon,
  ChartBarIcon,
  TrendingUpIcon,
  TrendingDownIcon,
  ExclamationTriangleIcon,
  CheckCircleIcon,
  XCircleIcon,
  PlusIcon,
} from '@heroicons/react/24/outline';
import { 
  BarChart, 
  Bar, 
  XAxis, 
  YAxis, 
  CartesianGrid, 
  Tooltip, 
  ResponsiveContainer,
  PieChart,
  Pie,
  Cell,
  LineChart,
  Line,
} from 'recharts';

// Sample data for charts
const attendanceData = [
  { month: 'Jan', present: 95, absent: 5 },
  { month: 'Feb', present: 92, absent: 8 },
  { month: 'Mar', present: 97, absent: 3 },
  { month: 'Apr', present: 94, absent: 6 },
  { month: 'May', present: 96, absent: 4 },
  { month: 'Jun', present: 93, absent: 7 },
];

const leaveData = [
  { name: 'Annual Leave', value: 45, color: '#10b981' },
  { name: 'Sick Leave', value: 23, color: '#f59e0b' },
  { name: 'Personal Leave', value: 12, color: '#3b82f6' },
  { name: 'Emergency Leave', value: 8, color: '#ef4444' },
];

const performanceData = [
  { month: 'Jan', score: 85 },
  { month: 'Feb', score: 88 },
  { month: 'Mar', score: 92 },
  { month: 'Apr', score: 89 },
  { month: 'May', score: 94 },
  { month: 'Jun', score: 91 },
];

const StatCard: React.FC<{
  title: string;
  value: string;
  change: string;
  trend: 'up' | 'down';
  icon: React.ElementType;
  color: string;
}> = ({ title, value, change, trend, icon: Icon, color }) => (
  <motion.div
    initial={{ opacity: 0, y: 20 }}
    animate={{ opacity: 1, y: 0 }}
    transition={{ duration: 0.5 }}
    className="bg-white rounded-2xl p-6 shadow-card hover:shadow-card-hover transition-all duration-300 border border-neutral-200"
  >
    <div className="flex items-center justify-between">
      <div>
        <p className="text-sm font-medium text-neutral-600">{title}</p>
        <p className="text-3xl font-bold text-primary-800 mt-2">{value}</p>
        <div className="flex items-center mt-2">
          {trend === 'up' ? (
            <TrendingUpIcon className="w-4 h-4 text-accent-500 mr-1" />
          ) : (
            <TrendingDownIcon className="w-4 h-4 text-danger-500 mr-1" />
          )}
          <span
            className={`text-sm font-medium ${
              trend === 'up' ? 'text-accent-600' : 'text-danger-600'
            }`}
          >
            {change}
          </span>
        </div>
      </div>
      <div className={`p-4 rounded-xl ${color}`}>
        <Icon className="w-8 h-8 text-white" />
      </div>
    </div>
  </motion.div>
);

const QuickActionCard: React.FC<{
  title: string;
  description: string;
  icon: React.ElementType;
  color: string;
  onClick: () => void;
}> = ({ title, description, icon: Icon, color, onClick }) => (
  <motion.div
    initial={{ opacity: 0, scale: 0.9 }}
    animate={{ opacity: 1, scale: 1 }}
    transition={{ duration: 0.5 }}
    whileHover={{ scale: 1.02 }}
    whileTap={{ scale: 0.98 }}
    className="bg-white rounded-2xl p-6 shadow-card hover:shadow-card-hover transition-all duration-300 border border-neutral-200 cursor-pointer"
    onClick={onClick}
  >
    <div className="flex items-center space-x-4">
      <div className={`p-3 rounded-xl ${color}`}>
        <Icon className="w-6 h-6 text-white" />
      </div>
      <div>
        <h3 className="text-lg font-semibold text-primary-800">{title}</h3>
        <p className="text-sm text-neutral-600">{description}</p>
      </div>
    </div>
  </motion.div>
);

const RecentActivity: React.FC = () => {
  const activities = [
    {
      id: 1,
      type: 'leave',
      message: 'John Doe submitted annual leave request',
      time: '2 hours ago',
      icon: CalendarIcon,
      color: 'bg-accent-500',
    },
    {
      id: 2,
      type: 'attendance',
      message: 'Sarah Wilson checked in late',
      time: '4 hours ago',
      icon: ClockIcon,
      color: 'bg-warning-500',
    },
    {
      id: 3,
      type: 'performance',
      message: 'Q2 performance reviews completed',
      time: '1 day ago',
      icon: ChartBarIcon,
      color: 'bg-success-500',
    },
    {
      id: 4,
      type: 'employee',
      message: 'New employee onboarding started',
      time: '2 days ago',
      icon: UsersIcon,
      color: 'bg-info-500',
    },
  ];

  return (
    <div className="bg-white rounded-2xl p-6 shadow-card border border-neutral-200">
      <h3 className="text-lg font-semibold text-primary-800 mb-4">Recent Activity</h3>
      <div className="space-y-4">
        {activities.map((activity) => (
          <div key={activity.id} className="flex items-center space-x-4">
            <div className={`p-2 rounded-lg ${activity.color}`}>
              <activity.icon className="w-4 h-4 text-white" />
            </div>
            <div className="flex-1">
              <p className="text-sm font-medium text-primary-800">{activity.message}</p>
              <p className="text-xs text-neutral-500">{activity.time}</p>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};

const Dashboard: React.FC = () => {
  const handleQuickAction = (action: string) => {
    console.log(`Quick action: ${action}`);
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <motion.div
        initial={{ opacity: 0, y: -20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.5 }}
        className="flex items-center justify-between"
      >
        <div>
          <h1 className="text-3xl font-bold text-primary-800">Dashboard</h1>
          <p className="text-neutral-600 mt-1">
            Welcome back! Here's what's happening with your HR system.
          </p>
        </div>
        <div className="flex items-center space-x-3">
          <motion.button
            whileHover={{ scale: 1.05 }}
            whileTap={{ scale: 0.95 }}
            className="flex items-center space-x-2 bg-gradient-to-r from-primary-600 to-primary-700 text-white px-4 py-2 rounded-lg hover:from-primary-700 hover:to-primary-800 transition-all duration-200 shadow-lg"
          >
            <PlusIcon className="w-5 h-5" />
            <span>Quick Add</span>
          </motion.button>
        </div>
      </motion.div>

      {/* Stats Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <StatCard
          title="Total Employees"
          value="1,234"
          change="+12%"
          trend="up"
          icon={UsersIcon}
          color="bg-gradient-to-br from-accent-500 to-accent-600"
        />
        <StatCard
          title="Leave Requests"
          value="28"
          change="+5%"
          trend="up"
          icon={CalendarIcon}
          color="bg-gradient-to-br from-warning-500 to-warning-600"
        />
        <StatCard
          title="Attendance Rate"
          value="95.2%"
          change="+2.1%"
          trend="up"
          icon={ClockIcon}
          color="bg-gradient-to-br from-success-500 to-success-600"
        />
        <StatCard
          title="Performance Score"
          value="89.4"
          change="-1.2%"
          trend="down"
          icon={ChartBarIcon}
          color="bg-gradient-to-br from-info-500 to-info-600"
        />
      </div>

      {/* Quick Actions */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        <QuickActionCard
          title="New Employee"
          description="Add a new employee to the system"
          icon={UsersIcon}
          color="bg-gradient-to-br from-accent-500 to-accent-600"
          onClick={() => handleQuickAction('new-employee')}
        />
        <QuickActionCard
          title="Leave Request"
          description="Submit a new leave request"
          icon={CalendarIcon}
          color="bg-gradient-to-br from-warning-500 to-warning-600"
          onClick={() => handleQuickAction('leave-request')}
        />
        <QuickActionCard
          title="Performance Review"
          description="Start a performance review"
          icon={ChartBarIcon}
          color="bg-gradient-to-br from-success-500 to-success-600"
          onClick={() => handleQuickAction('performance-review')}
        />
        <QuickActionCard
          title="Generate Report"
          description="Create HR analytics report"
          icon={ChartBarIcon}
          color="bg-gradient-to-br from-info-500 to-info-600"
          onClick={() => handleQuickAction('generate-report')}
        />
      </div>

      {/* Charts Section */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Attendance Chart */}
        <motion.div
          initial={{ opacity: 0, x: -20 }}
          animate={{ opacity: 1, x: 0 }}
          transition={{ duration: 0.5, delay: 0.2 }}
          className="bg-white rounded-2xl p-6 shadow-card border border-neutral-200"
        >
          <h3 className="text-lg font-semibold text-primary-800 mb-4">
            Attendance Trends
          </h3>
          <ResponsiveContainer width="100%" height={300}>
            <BarChart data={attendanceData}>
              <CartesianGrid strokeDasharray="3 3" stroke="#e5e7eb" />
              <XAxis dataKey="month" stroke="#6b7280" />
              <YAxis stroke="#6b7280" />
              <Tooltip
                contentStyle={{
                  backgroundColor: '#1e293b',
                  border: 'none',
                  borderRadius: '8px',
                  color: '#f8fafc',
                }}
              />
              <Bar dataKey="present" fill="#10b981" radius={[4, 4, 0, 0]} />
              <Bar dataKey="absent" fill="#ef4444" radius={[4, 4, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </motion.div>

        {/* Leave Distribution */}
        <motion.div
          initial={{ opacity: 0, x: 20 }}
          animate={{ opacity: 1, x: 0 }}
          transition={{ duration: 0.5, delay: 0.3 }}
          className="bg-white rounded-2xl p-6 shadow-card border border-neutral-200"
        >
          <h3 className="text-lg font-semibold text-primary-800 mb-4">
            Leave Distribution
          </h3>
          <ResponsiveContainer width="100%" height={300}>
            <PieChart>
              <Pie
                data={leaveData}
                cx="50%"
                cy="50%"
                innerRadius={60}
                outerRadius={120}
                paddingAngle={5}
                dataKey="value"
              >
                {leaveData.map((entry, index) => (
                  <Cell key={`cell-${index}`} fill={entry.color} />
                ))}
              </Pie>
              <Tooltip
                contentStyle={{
                  backgroundColor: '#1e293b',
                  border: 'none',
                  borderRadius: '8px',
                  color: '#f8fafc',
                }}
              />
            </PieChart>
          </ResponsiveContainer>
          <div className="flex flex-wrap gap-4 mt-4">
            {leaveData.map((item) => (
              <div key={item.name} className="flex items-center space-x-2">
                <div
                  className="w-3 h-3 rounded-full"
                  style={{ backgroundColor: item.color }}
                />
                <span className="text-sm text-neutral-600">{item.name}</span>
              </div>
            ))}
          </div>
        </motion.div>
      </div>

      {/* Performance and Recent Activity */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Performance Chart */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5, delay: 0.4 }}
          className="bg-white rounded-2xl p-6 shadow-card border border-neutral-200"
        >
          <h3 className="text-lg font-semibold text-primary-800 mb-4">
            Performance Trends
          </h3>
          <ResponsiveContainer width="100%" height={300}>
            <LineChart data={performanceData}>
              <CartesianGrid strokeDasharray="3 3" stroke="#e5e7eb" />
              <XAxis dataKey="month" stroke="#6b7280" />
              <YAxis stroke="#6b7280" />
              <Tooltip
                contentStyle={{
                  backgroundColor: '#1e293b',
                  border: 'none',
                  borderRadius: '8px',
                  color: '#f8fafc',
                }}
              />
              <Line
                type="monotone"
                dataKey="score"
                stroke="#10b981"
                strokeWidth={3}
                dot={{ fill: '#10b981', strokeWidth: 2, r: 6 }}
              />
            </LineChart>
          </ResponsiveContainer>
        </motion.div>

        {/* Recent Activity */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5, delay: 0.5 }}
        >
          <RecentActivity />
        </motion.div>
      </div>

      {/* Pending Actions */}
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.5, delay: 0.6 }}
        className="bg-white rounded-2xl p-6 shadow-card border border-neutral-200"
      >
        <h3 className="text-lg font-semibold text-primary-800 mb-4">
          Pending Actions
        </h3>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div className="flex items-center justify-between p-4 bg-warning-50 rounded-lg border border-warning-200">
            <div className="flex items-center space-x-3">
              <ExclamationTriangleIcon className="w-5 h-5 text-warning-600" />
              <div>
                <p className="font-medium text-warning-800">Leave Approvals</p>
                <p className="text-sm text-warning-600">12 pending</p>
              </div>
            </div>
            <button className="text-warning-700 hover:text-warning-800 font-medium text-sm">
              Review
            </button>
          </div>
          
          <div className="flex items-center justify-between p-4 bg-info-50 rounded-lg border border-info-200">
            <div className="flex items-center space-x-3">
              <CheckCircleIcon className="w-5 h-5 text-info-600" />
              <div>
                <p className="font-medium text-info-800">Performance Reviews</p>
                <p className="text-sm text-info-600">8 to complete</p>
              </div>
            </div>
            <button className="text-info-700 hover:text-info-800 font-medium text-sm">
              Start
            </button>
          </div>
          
          <div className="flex items-center justify-between p-4 bg-success-50 rounded-lg border border-success-200">
            <div className="flex items-center space-x-3">
              <UsersIcon className="w-5 h-5 text-success-600" />
              <div>
                <p className="font-medium text-success-800">New Hires</p>
                <p className="text-sm text-success-600">3 onboarding</p>
              </div>
            </div>
            <button className="text-success-700 hover:text-success-800 font-medium text-sm">
              View
            </button>
          </div>
        </div>
      </motion.div>
    </div>
  );
};

export default Dashboard;