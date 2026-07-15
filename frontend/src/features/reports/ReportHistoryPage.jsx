import { Plus, Sparkles } from 'lucide-react';
import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { listReports } from './reportApi.js';

export function ReportHistoryPage() {
  const [reports, setReports] = useState([]);

  useEffect(() => {
    listReports().then((data) => setReports(data.content));
  }, []);

  return (
    <section>
      <div className="mb-6 flex flex-wrap items-center justify-between gap-3">
        <div>
          <h1 className="text-3xl font-semibold text-primary">Reports</h1>
          <p className="mt-2 text-secondary">Track every resume and job-description analysis.</p>
        </div>
        <Link className="inline-flex items-center gap-2 rounded-lg bg-accent px-4 py-2 font-medium text-white hover:bg-accentHover" to="/reports/new">
          <Plus size={18} />
          New report
        </Link>
      </div>
      {reports.length === 0 ? (
        <div className="rounded-xl border border-dashed border-border bg-card p-8 text-center">
          <Sparkles className="mx-auto text-accent" />
          <p className="mt-3 font-medium">No reports yet</p>
          <p className="mt-1 text-sm text-secondary">Generate your first report from a parsed resume and JD.</p>
        </div>
      ) : (
        <div className="space-y-3">
          {reports.map((report) => (
            <Link className="block rounded-xl border border-border bg-card p-4 shadow-sm hover:border-accent" key={report.id} to={`/reports/${report.id}`}>
              <div className="flex flex-wrap items-center justify-between gap-3">
                <div>
                  <h2 className="font-semibold">Report #{report.id}</h2>
                  <p className="mt-1 text-sm text-secondary">{report.status}</p>
                </div>
                <div className="flex gap-2 text-sm">
                  <span className="rounded-lg bg-accent/10 px-3 py-1 text-accent">ATS {report.atsMatchScore ?? '-'}</span>
                  <span className="rounded-lg bg-accent/10 px-3 py-1 text-accent">Ready {report.interviewReadinessScore ?? '-'}</span>
                </div>
              </div>
            </Link>
          ))}
        </div>
      )}
    </section>
  );
}

