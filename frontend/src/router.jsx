import { createBrowserRouter, Navigate } from 'react-router-dom';
import { AppLayout } from './ui/AppLayout.jsx';
import { LoginPage } from './features/auth/LoginPage.jsx';
import { RegisterPage } from './features/auth/RegisterPage.jsx';
import { ProfilePage } from './features/auth/ProfilePage.jsx';
import { DashboardPage } from './features/dashboard/DashboardPage.jsx';
import { ProtectedRoute } from './features/auth/ProtectedRoute.jsx';
import { ResumeListPage } from './features/resumes/ResumeListPage.jsx';
import { ResumeUploadPage } from './features/resumes/ResumeUploadPage.jsx';
import { JobDescriptionListPage } from './features/jobDescriptions/JobDescriptionListPage.jsx';
import { JobDescriptionCreatePage } from './features/jobDescriptions/JobDescriptionCreatePage.jsx';
import { GenerateReportPage } from './features/reports/GenerateReportPage.jsx';
import { ReportDetailPage } from './features/reports/ReportDetailPage.jsx';
import { ReportHistoryPage } from './features/reports/ReportHistoryPage.jsx';
import { ReportChatPage } from './features/chat/ReportChatPage.jsx';
import { ChatSessionListPage } from './features/chat/ChatSessionListPage.jsx';
import { ChatSessionPage } from './features/chat/ChatSessionPage.jsx';

export const router = createBrowserRouter([
  {
    path: '/',
    element: <AppLayout />,
    children: [
      { index: true, element: <Navigate to="/dashboard" replace /> },
      { path: 'login', element: <LoginPage /> },
      { path: 'register', element: <RegisterPage /> },
      {
        element: <ProtectedRoute />,
        children: [
          { path: 'dashboard', element: <DashboardPage /> },
          { path: 'resumes', element: <ResumeListPage /> },
          { path: 'resumes/new', element: <ResumeUploadPage /> },
          { path: 'job-descriptions', element: <JobDescriptionListPage /> },
          { path: 'job-descriptions/new', element: <JobDescriptionCreatePage /> },
          { path: 'reports', element: <ReportHistoryPage /> },
          { path: 'reports/new', element: <GenerateReportPage /> },
          { path: 'reports/:id', element: <ReportDetailPage /> },
          { path: 'reports/:id/chat', element: <ReportChatPage /> },
          { path: 'chat', element: <ChatSessionListPage /> },
          { path: 'chat/sessions/:sessionId', element: <ChatSessionPage /> },
          { path: 'profile', element: <ProfilePage /> }
        ]
      }
    ]
  }
]);
