import { UploadCloud } from 'lucide-react';
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { uploadResume } from './resumeApi.js';

export function ResumeUploadPage() {
  const navigate = useNavigate();
  const [file, setFile] = useState(null);
  const [error, setError] = useState(null);

  const onSubmit = async (event) => {
    event.preventDefault();
    if (!file) {
      setError('Choose a PDF resume first.');
      return;
    }
    try {
      await uploadResume(file);
      navigate('/resumes');
    } catch (err) {
      setError(err.response?.data?.message ?? 'Upload failed');
    }
  };

  return (
    <section className="max-w-2xl">
      <h1 className="text-3xl font-semibold text-primary">Upload Resume</h1>
      <form className="mt-6 rounded-xl border border-border bg-card p-6 shadow-sm" onSubmit={onSubmit}>
        <label className="flex cursor-pointer flex-col items-center justify-center rounded-xl border border-dashed border-border p-10 text-center hover:bg-sidebar">
          <UploadCloud className="text-accent" size={34} />
          <span className="mt-3 font-medium">{file ? file.name : 'Choose a PDF file'}</span>
          <span className="mt-1 text-sm text-secondary">Maximum size: 5MB</span>
          <input accept="application/pdf" className="sr-only" onChange={(event) => setFile(event.target.files?.[0] ?? null)} type="file" />
        </label>
        {error && <p className="mt-4 rounded-lg bg-accent/10 px-3 py-2 text-sm text-accent">{error}</p>}
        <button className="mt-5 rounded-lg bg-accent px-4 py-2 font-medium text-white hover:bg-accentHover" type="submit">
          Upload resume
        </button>
      </form>
    </section>
  );
}

