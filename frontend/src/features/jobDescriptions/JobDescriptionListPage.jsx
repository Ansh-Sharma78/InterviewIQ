import { BriefcaseBusiness, Plus, Trash2 } from 'lucide-react';
import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { deleteJobDescription, listJobDescriptions } from './jobDescriptionApi.js';

export function JobDescriptionListPage() {
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);

  const load = async () => {
    setLoading(true);
    const data = await listJobDescriptions();
    setItems(data.content);
    setLoading(false);
  };

  useEffect(() => {
    load();
  }, []);

  const onDelete = async (id) => {
    await deleteJobDescription(id);
    await load();
  };

  return (
    <section>
      <div className="mb-6 flex flex-wrap items-center justify-between gap-3">
        <div>
          <h1 className="text-3xl font-semibold text-primary">Job Descriptions</h1>
          <p className="mt-2 text-secondary">Save target roles as text or PDF inputs for analysis.</p>
        </div>
        <Link className="inline-flex items-center gap-2 rounded-lg bg-accent px-4 py-2 font-medium text-white hover:bg-accentHover" to="/job-descriptions/new">
          <Plus size={18} />
          Add JD
        </Link>
      </div>
      {loading ? (
        <p className="text-secondary">Loading job descriptions...</p>
      ) : items.length === 0 ? (
        <div className="rounded-xl border border-dashed border-border bg-card p-8 text-center">
          <BriefcaseBusiness className="mx-auto text-accent" />
          <p className="mt-3 font-medium">No job descriptions yet</p>
          <p className="mt-1 text-sm text-secondary">Paste a JD or upload a PDF to prepare analysis inputs.</p>
        </div>
      ) : (
        <div className="space-y-3">
          {items.map((item) => (
            <article className="rounded-xl border border-border bg-card p-4 shadow-sm" key={item.id}>
              <div className="flex items-start justify-between gap-4">
                <div>
                  <h2 className="font-semibold">{item.roleTitle || 'Untitled role'}</h2>
                  <p className="mt-1 text-sm text-secondary">{item.companyName || 'Company not set'} | {item.sourceType} | {item.parseStatus}</p>
                  {item.textPreview && <p className="mt-3 line-clamp-2 text-sm text-secondary">{item.textPreview}</p>}
                </div>
                <button className="rounded-lg p-2 text-secondary hover:bg-sidebar hover:text-accent" onClick={() => onDelete(item.id)} title="Delete job description" type="button">
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

