import { Send } from 'lucide-react';
import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { getChatSession, listMessages, sendMessage } from './chatApi.js';

export function ChatSessionPage() {
  const { sessionId } = useParams();
  const [session, setSession] = useState(null);
  const [messages, setMessages] = useState([]);
  const [content, setContent] = useState('');
  const [error, setError] = useState(null);

  useEffect(() => {
    async function load() {
      const [sessionData, messageData] = await Promise.all([getChatSession(sessionId), listMessages(sessionId)]);
      setSession(sessionData);
      setMessages(messageData.content);
    }
    load().catch((err) => setError(err.response?.data?.message ?? 'Could not load chat'));
  }, [sessionId]);

  const onSubmit = async (event) => {
    event.preventDefault();
    const text = content.trim();
    if (!text) return;
    const optimistic = { id: `local-${Date.now()}`, role: 'USER', content: text };
    setMessages((current) => [...current, optimistic]);
    setContent('');
    try {
      const assistant = await sendMessage(sessionId, text);
      setMessages((current) => [...current, assistant]);
    } catch (err) {
      setError(err.response?.data?.message ?? 'Message failed');
    }
  };

  return (
    <section className="mx-auto max-w-3xl">
      <div className="mb-6">
        <Link className="text-sm font-medium text-accent" to="/chat">
          Back to chats
        </Link>
        <h1 className="mt-2 text-3xl font-semibold text-primary">{session?.title ?? 'Chat'}</h1>
        {session && <p className="mt-2 text-secondary">Report #{session.reportId}</p>}
      </div>
      {error && <p className="mb-4 rounded-lg bg-accent/10 px-3 py-2 text-sm text-accent">{error}</p>}
      <div className="min-h-96 space-y-3 rounded-xl border border-border bg-card p-4 shadow-sm">
        {messages.length === 0 ? (
          <p className="text-sm text-secondary">Ask for harder questions, resume rewrites, or a focused prep plan.</p>
        ) : (
          messages.map((message) => (
            <div className={`flex ${message.role === 'USER' ? 'justify-end' : 'justify-start'}`} key={message.id}>
              <div className={`max-w-[80%] rounded-xl px-4 py-3 text-sm leading-6 ${message.role === 'USER' ? 'bg-accent text-white' : 'bg-paper text-primary'}`}>
                {message.content}
              </div>
            </div>
          ))
        )}
      </div>
      <form className="mt-4 flex gap-3" onSubmit={onSubmit}>
        <input className="min-w-0 flex-1 rounded-lg border border-border px-3 py-2 outline-none focus:border-accent" onChange={(event) => setContent(event.target.value)} placeholder="Ask a follow-up..." value={content} />
        <button className="inline-flex items-center gap-2 rounded-lg bg-accent px-4 py-2 font-medium text-white hover:bg-accentHover" type="submit">
          <Send size={18} />
          Send
        </button>
      </form>
    </section>
  );
}

