import { MessageSquare, Pencil, Trash2 } from 'lucide-react';
import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { deleteChatSession, listChatSessions, renameChatSession } from './chatApi.js';

export function ChatSessionListPage() {
  const [sessions, setSessions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [editingId, setEditingId] = useState(null);
  const [title, setTitle] = useState('');

  const load = async () => {
    setLoading(true);
    const data = await listChatSessions();
    setSessions(data.content);
    setLoading(false);
  };

  useEffect(() => {
    load();
  }, []);

  const startRename = (session) => {
    setEditingId(session.id);
    setTitle(session.title);
  };

  const saveRename = async (event) => {
    event.preventDefault();
    if (!editingId || !title.trim()) return;
    await renameChatSession(editingId, title.trim());
    setEditingId(null);
    setTitle('');
    await load();
  };

  const onDelete = async (id) => {
    await deleteChatSession(id);
    await load();
  };

  return (
    <section>
      <div className="mb-6">
        <h1 className="text-3xl font-semibold text-primary">Chat Sessions</h1>
        <p className="mt-2 text-secondary">Reopen report-scoped coaching conversations.</p>
      </div>
      {loading ? (
        <p className="text-secondary">Loading chats...</p>
      ) : sessions.length === 0 ? (
        <div className="rounded-xl border border-dashed border-border bg-card p-8 text-center">
          <MessageSquare className="mx-auto text-accent" />
          <p className="mt-3 font-medium">No chat sessions yet</p>
          <p className="mt-1 text-sm text-secondary">Open a completed report and start a chat from there.</p>
        </div>
      ) : (
        <div className="space-y-3">
          {sessions.map((session) => (
            <article className="rounded-xl border border-border bg-card p-4 shadow-sm" key={session.id}>
              {editingId === session.id ? (
                <form className="flex gap-3" onSubmit={saveRename}>
                  <input className="min-w-0 flex-1 rounded-lg border border-border px-3 py-2 outline-none focus:border-accent" onChange={(event) => setTitle(event.target.value)} value={title} />
                  <button className="rounded-lg bg-accent px-4 py-2 text-sm font-medium text-white hover:bg-accentHover" type="submit">
                    Save
                  </button>
                  <button className="rounded-lg px-4 py-2 text-sm text-secondary hover:bg-sidebar" onClick={() => setEditingId(null)} type="button">
                    Cancel
                  </button>
                </form>
              ) : (
                <div className="flex items-start justify-between gap-4">
                  <Link className="min-w-0 flex-1" to={`/chat/sessions/${session.id}`}>
                    <h2 className="font-semibold">{session.title}</h2>
                    <p className="mt-1 text-sm text-secondary">Report #{session.reportId}</p>
                  </Link>
                  <div className="flex gap-1">
                    <button className="rounded-lg p-2 text-secondary hover:bg-sidebar" onClick={() => startRename(session)} title="Rename chat" type="button">
                      <Pencil size={18} />
                    </button>
                    <button className="rounded-lg p-2 text-secondary hover:bg-sidebar hover:text-accent" onClick={() => onDelete(session.id)} title="Delete chat" type="button">
                      <Trash2 size={18} />
                    </button>
                  </div>
                </div>
              )}
            </article>
          ))}
        </div>
      )}
    </section>
  );
}

