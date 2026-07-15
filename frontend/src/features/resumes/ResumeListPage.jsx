import { Plus, Trash2, Upload } from 'lucide-react';
import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { deleteResume, listResumes } from './resumeApi.js';

export function ResumeListPage() {
  const [resumes, setResumes] = useState([]);
  const [loading, setLoading] = useState(true);

  const load = async () => {
    setLoading(true);
    const data = await listResumes();
    setResumes(data.content);
    setLoading(false);
  };

  useEffect(() => {
    load();
  }, []);

  const onDelete = async (id) => {
    await deleteResume(id);
    await load();
  };

  return (
    <section>
      <div className="mb-6 flex flex-wrap items-center justify-between gap-3">
        <div>
          <h1 className="text-3xl font-semibold text-primary">Resumes</h1>
          <p className="mt-2 text-secondary">Upload resume PDFs and reuse them across job descriptions.</p>
        </div>
        <Link className="inline-flex items-center gap-2 rounded-lg bg-accent px-4 py-2 font-medium text-white hover:bg-accentHover" to="/resumes/new">
          <Plus size={18} />
          Upload
        </Link>
      </div>
      {loading ? (
        <p className="text-secondary">Loading resumes...</p>
      ) : resumes.length === 0 ? (
        <div className="rounded-xl border border-dashed border-border bg-card p-8 text-center">
          <Upload className="mx-auto text-accent" />
          <p className="mt-3 font-medium">No resumes yet</p>
          <p className="mt-1 text-sm text-secondary">Upload a PDF resume to start building reports.</p>
        </div>
      ) : (
        <div className="space-y-3">
          {resumes.map((resume) => (
            <article className="rounded-xl border border-border bg-card p-4 shadow-sm" key={resume.id}>
              <div className="flex items-start justify-between gap-4">
                <div>
                  <h2 className="font-semibold">{resume.originalFilename}</h2>
                  <p className="mt-1 text-sm text-secondary">{resume.parseStatus} | {(resume.fileSizeBytes / 1024).toFixed(1)} KB</p>
                  {resume.parsedTextPreview && <p className="mt-3 line-clamp-2 text-sm text-secondary">{resume.parsedTextPreview}</p>}
                </div>
                <button className="rounded-lg p-2 text-secondary hover:bg-sidebar hover:text-accent" onClick={() => onDelete(resume.id)} title="Delete resume" type="button">
                  <Trash2 size={18} />
                </button>
              </div>
            </article>
          ))}
        </div>
      )}
    </section>
  );
}

