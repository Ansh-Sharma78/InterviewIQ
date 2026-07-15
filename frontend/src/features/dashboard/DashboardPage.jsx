import { FileText, MessageSquare, Sparkles } from 'lucide-react';
import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { getDashboardSummary } from './dashboardApi.js';

export function DashboardPage() {
  const [summary, setSummary] = useState(null);

  useEffect(() => {
    getDashboardSummary().then(setSummary).catch(() => setSummary(null));
  }, []);

  return (
    <section>
      <div className="mb-8">
        <h1 className="text-3xl font-semibold text-primary">Dashboard</h1>
        <p className="mt-2 max-w-2xl text-secondary">Your preparation workspace, report history, and next actions.</p>
      </div>
      <div className="mb-6 grid gap-4 md:grid-cols-3">
        <Stat label="Reports" value={summary?.totalReports ?? 0} />
        <Stat label="Average ATS" value={summary?.averageAtsScore ?? '-'} />
        <Stat label="Average Readiness" value={summary?.averageReadinessScore ?? '-'} />
      </div>
      <div className="grid gap-4 md:grid-cols-3">
        <Link className="rounded-xl border border-border bg-card p-5 shadow-sm hover:border-accent" to="/resumes">
          <FileText className="text-accent" />
          <h2 className="mt-4 font-semibold">Resume library</h2>
          <p className="mt-2 text-sm text-secondary">{summary?.recentResumes?.length ?? 0} recent resumes available.</p>
        </Link>
        <Link className="rounded-xl border border-border bg-card p-5 shadow-sm hover:border-accent" to="/job-descriptions">
          <Sparkles className="text-accent" />
          <h2 className="mt-4 font-semibold">Job descriptions</h2>
          <p className="mt-2 text-sm text-secondary">{summary?.recentJobDescriptions?.length ?? 0} recent job descriptions saved.</p>
        </Link>
        <Link className="rounded-xl border border-border bg-card p-5 shadow-sm hover:border-accent" to="/reports/new">
          <MessageSquare className="text-accent" />
          <h2 className="mt-4 font-semibold">AI reports</h2>
          <p className="mt-2 text-sm text-secondary">Generate an async structured interview-prep report.</p>
        </Link>
      </div>
      {(summary?.recentReports?.length > 0 || summary?.recentChatSessions?.length > 0) && (
        <div className="mt-8 grid gap-6 lg:grid-cols-2">
          {summary?.recentReports?.length > 0 && (
            <section>
              <h2 className="mb-3 text-xl font-semibold">Recent Reports</h2>
              <div className="space-y-3">
                {summary.recentReports.map((report) => (
                  <Link className="block rounded-xl border border-border bg-card p-4 shadow-sm hover:border-accent" key={report.id} to={`/reports/${report.id}`}>
                    <div className="flex items-center justify-between gap-3">
                      <span className="font-medium">Report #{report.id}</span>
                      <span className="text-sm text-secondary">{report.status}</span>
                    </div>
                  </Link>
                ))}
              </div>
            </section>
          )}
          {summary?.recentChatSessions?.length > 0 && (
            <section>
              <h2 className="mb-3 text-xl font-semibold">Recent Chats</h2>
              <div className="space-y-3">
                {summary.recentChatSessions.map((session) => (
                  <Link className="block rounded-xl border border-border bg-card p-4 shadow-sm hover:border-accent" key={session.id} to={`/chat/sessions/${session.id}`}>
                    <div className="flex items-center justify-between gap-3">
                      <span className="font-medium">{session.title}</span>
                      <span className="text-sm text-secondary">Report #{session.reportId}</span>
                    </div>
                  </Link>
                ))}
              </div>
            </section>
          )}
        </div>
      )}
    </section>
  );
}

function Stat({ label, value }) {
  return (
    <div className="rounded-xl border border-border bg-card p-5 shadow-sm">
      <p className="text-sm text-secondary">{label}</p>
      <p className="mt-2 text-3xl font-semibold text-primary">{value}</p>
    </div>
  );
}
