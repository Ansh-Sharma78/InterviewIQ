import { ClipboardList, FileText, Sparkles, UploadCloud } from 'lucide-react';
import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { createJobDescriptionPdf, createJobDescriptionText } from '../jobDescriptions/jobDescriptionApi.js';
import { uploadResume } from '../resumes/resumeApi.js';
import { createReport } from './reportApi.js';

export function GenerateReportPage() {
  const navigate = useNavigate();
  const [jdMode, setJdMode] = useState('text');
  const [resumeFile, setResumeFile] = useState(null);
  const [jdFile, setJdFile] = useState(null);
  const [form, setForm] = useState({ companyName: '', roleTitle: '', rawText: '' });
  const [error, setError] = useState(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const onSubmit = async (event) => {
    event.preventDefault();
    setError(null);

    if (!resumeFile) {
      setError('Choose a PDF resume first.');
      return;
    }

    if (jdMode === 'pdf' && !jdFile) {
      setError('Choose a PDF job description first.');
      return;
    }

    try {
      setIsSubmitting(true);
      const resume = await uploadResume(resumeFile);
      if (resume.parseStatus !== 'SUCCESS') {
        throw new Error('Resume upload finished, but text extraction did not succeed.');
      }

      const jobDescription = jdMode === 'text'
        ? await createJobDescriptionText(form)
        : await createJobDescriptionPdf({ file: jdFile, companyName: form.companyName, roleTitle: form.roleTitle });

      if (jobDescription.parseStatus !== 'SUCCESS') {
        throw new Error('Job description was saved, but text extraction did not succeed.');
      }

      const report = await createReport({
        resumeId: resume.id,
        jobDescriptionId: jobDescription.id
      });
      navigate(`/reports/${report.id}`);
    } catch (err) {
      setError(err.response?.data?.message ?? err.message ?? 'Could not generate report');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <section className="max-w-5xl">
      <div className="flex flex-wrap items-start justify-between gap-4">
        <div>
          <h1 className="text-3xl font-semibold text-primary">Generate Report</h1>
          <p className="mt-2 text-secondary">Upload a resume, add the target job description, and generate the analysis in one flow.</p>
        </div>
        <Link className="rounded-lg border border-border px-4 py-2 text-sm font-medium text-secondary hover:bg-card" to="/reports">
          Report history
        </Link>
      </div>

      <form className="mt-6 space-y-5" onSubmit={onSubmit}>
        <div className="grid gap-5 lg:grid-cols-[minmax(0,0.9fr)_minmax(0,1.1fr)]">
          <section className="rounded-xl border border-border bg-card p-5 shadow-sm">
            <div className="flex items-center gap-2">
              <UploadCloud className="text-accent" size={22} />
              <h2 className="font-semibold">Resume</h2>
            </div>
            <label className="mt-4 flex min-h-52 cursor-pointer flex-col items-center justify-center rounded-xl border border-dashed border-border p-8 text-center hover:bg-sidebar">
              <UploadCloud className="text-accent" size={34} />
              <span className="mt-3 max-w-full break-words font-medium">{resumeFile ? resumeFile.name : 'Choose a PDF resume'}</span>
              <span className="mt-1 text-sm text-secondary">Maximum size: 5MB</span>
              <input accept="application/pdf" className="sr-only" onChange={(event) => setResumeFile(event.target.files?.[0] ?? null)} type="file" />
            </label>
          </section>

          <section className="rounded-xl border border-border bg-card p-5 shadow-sm">
            <div className="flex flex-wrap items-center justify-between gap-3">
              <div className="flex items-center gap-2">
                <ClipboardList className="text-accent" size={22} />
                <h2 className="font-semibold">Job Description</h2>
              </div>
              <div className="inline-flex rounded-lg border border-border bg-paper p-1">
                <button className={`rounded px-3 py-2 text-sm ${jdMode === 'text' ? 'bg-card shadow-sm' : 'text-secondary'}`} onClick={() => setJdMode('text')} type="button">
                  Text
                </button>
                <button className={`rounded px-3 py-2 text-sm ${jdMode === 'pdf' ? 'bg-card shadow-sm' : 'text-secondary'}`} onClick={() => setJdMode('pdf')} type="button">
                  PDF
                </button>
              </div>
            </div>

            <div className="mt-4 grid gap-4 md:grid-cols-2">
              <label className="block text-sm font-medium">
                Company
                <input className="mt-1 w-full rounded-lg border border-border px-3 py-2 outline-none focus:border-accent" onChange={(event) => setForm({ ...form, companyName: event.target.value })} value={form.companyName} />
              </label>
              <label className="block text-sm font-medium">
                Role title
                <input className="mt-1 w-full rounded-lg border border-border px-3 py-2 outline-none focus:border-accent" onChange={(event) => setForm({ ...form, roleTitle: event.target.value })} value={form.roleTitle} />
              </label>
            </div>

            {jdMode === 'text' ? (
              <label className="mt-4 block text-sm font-medium">
                Job description text
                <textarea className="mt-1 min-h-64 w-full rounded-lg border border-border px-3 py-2 leading-6 outline-none focus:border-accent" onChange={(event) => setForm({ ...form, rawText: event.target.value })} required value={form.rawText} />
              </label>
            ) : (
              <label className="mt-4 flex min-h-64 cursor-pointer flex-col items-center justify-center rounded-xl border border-dashed border-border p-8 text-center hover:bg-sidebar">
                <FileText className="text-accent" size={34} />
                <span className="mt-3 max-w-full break-words font-medium">{jdFile ? jdFile.name : 'Choose a PDF job description'}</span>
                <span className="mt-1 text-sm text-secondary">Maximum size: 5MB</span>
                <input accept="application/pdf" className="sr-only" onChange={(event) => setJdFile(event.target.files?.[0] ?? null)} required={jdMode === 'pdf'} type="file" />
              </label>
            )}
          </section>
        </div>

        {error && <p className="rounded-lg bg-accent/10 px-3 py-2 text-sm text-accent">{error}</p>}

        <button className="inline-flex items-center gap-2 rounded-lg bg-accent px-5 py-3 font-medium text-white hover:bg-accentHover disabled:cursor-not-allowed disabled:opacity-60" disabled={isSubmitting} type="submit">
          <Sparkles size={18} />
          {isSubmitting ? 'Creating report...' : 'Generate report'}
        </button>
      </form>
    </section>
  );
}
