import { FileUp, Save } from 'lucide-react';
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { createJobDescriptionPdf, createJobDescriptionText } from './jobDescriptionApi.js';

export function JobDescriptionCreatePage() {
  const navigate = useNavigate();
  const [mode, setMode] = useState('text');
  const [error, setError] = useState(null);
  const [form, setForm] = useState({ companyName: '', roleTitle: '', rawText: '' });
  const [file, setFile] = useState(null);

  const onSubmit = async (event) => {
    event.preventDefault();
    try {
      if (mode === 'text') {
        await createJobDescriptionText(form);
      } else {
        await createJobDescriptionPdf({ file, companyName: form.companyName, roleTitle: form.roleTitle });
      }
      navigate('/job-descriptions');
    } catch (err) {
      setError(err.response?.data?.message ?? 'Could not save job description');
    }
  };

  return (
    <section className="max-w-3xl">
      <h1 className="text-3xl font-semibold text-primary">Add Job Description</h1>
      <form className="mt-6 space-y-4 rounded-xl border border-border bg-card p-6 shadow-sm" onSubmit={onSubmit}>
        <div className="inline-flex rounded-lg border border-border bg-paper p-1">
          <button className={`rounded px-3 py-2 text-sm ${mode === 'text' ? 'bg-card shadow-sm' : 'text-secondary'}`} onClick={() => setMode('text')} type="button">
            Text
          </button>
          <button className={`rounded px-3 py-2 text-sm ${mode === 'pdf' ? 'bg-card shadow-sm' : 'text-secondary'}`} onClick={() => setMode('pdf')} type="button">
            PDF
          </button>
        </div>
        <div className="grid gap-4 md:grid-cols-2">
          <label className="block text-sm font-medium">
            Company
            <input className="mt-1 w-full rounded-lg border border-border px-3 py-2 outline-none focus:border-accent" onChange={(event) => setForm({ ...form, companyName: event.target.value })} value={form.companyName} />
          </label>
          <label className="block text-sm font-medium">
            Role title
            <input className="mt-1 w-full rounded-lg border border-border px-3 py-2 outline-none focus:border-accent" onChange={(event) => setForm({ ...form, roleTitle: event.target.value })} value={form.roleTitle} />
          </label>
        </div>
        {mode === 'text' ? (
          <label className="block text-sm font-medium">
            Job description text
            <textarea className="mt-1 min-h-72 w-full rounded-lg border border-border px-3 py-2 outline-none focus:border-accent" onChange={(event) => setForm({ ...form, rawText: event.target.value })} required value={form.rawText} />
          </label>
        ) : (
          <label className="flex cursor-pointer flex-col items-center justify-center rounded-xl border border-dashed border-border p-8 text-center hover:bg-sidebar">
            <FileUp className="text-accent" />
            <span className="mt-3 font-medium">{file ? file.name : 'Choose a PDF job description'}</span>
            <input accept="application/pdf" className="sr-only" onChange={(event) => setFile(event.target.files?.[0] ?? null)} required={mode === 'pdf'} type="file" />
          </label>
        )}
        {error && <p className="rounded-lg bg-accent/10 px-3 py-2 text-sm text-accent">{error}</p>}
        <button className="inline-flex items-center gap-2 rounded-lg bg-accent px-4 py-2 font-medium text-white hover:bg-accentHover" type="submit">
          <Save size={18} />
          Save job description
        </button>
      </form>
    </section>
  );
}

