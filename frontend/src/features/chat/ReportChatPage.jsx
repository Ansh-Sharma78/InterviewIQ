import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { createChatSession } from './chatApi.js';

export function ReportChatPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [error, setError] = useState(null);

  useEffect(() => {
    createChatSession(Number(id))
      .then((created) => navigate(`/chat/sessions/${created.id}`, { replace: true }))
      .catch((err) => setError(err.response?.data?.message ?? 'Could not start chat'));
  }, [id, navigate]);

  return (
    <section className="mx-auto max-w-3xl">
      <div className="mb-6">
        <h1 className="text-3xl font-semibold text-primary">Starting Chat</h1>
        <p className="mt-2 text-secondary">Creating a report-scoped coaching session...</p>
      </div>
      {error && <p className="mb-4 rounded-lg bg-accent/10 px-3 py-2 text-sm text-accent">{error}</p>}
    </section>
  );
}
