import { UserPlus } from 'lucide-react';
import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useDispatch } from 'react-redux';
import { AuthFormShell } from './AuthFormShell.jsx';
import { register } from './authSlice.js';

export function RegisterPage() {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const [error, setError] = useState(null);
  const [form, setForm] = useState({ fullName: '', email: '', password: '' });

  const onSubmit = async (event) => {
    event.preventDefault();
    const result = await dispatch(register(form));
    if (register.fulfilled.match(result)) {
      navigate('/dashboard');
    } else {
      setError(result.payload);
    }
  };

  return (
    <AuthFormShell title="Create your account" subtitle="Start building a preparation history around your target roles.">
      <form className="space-y-4" onSubmit={onSubmit}>
        <label className="block text-sm font-medium">
          Full name
          <input className="mt-1 w-full rounded-lg border border-border px-3 py-2 outline-none focus:border-accent" onChange={(event) => setForm({ ...form, fullName: event.target.value })} required value={form.fullName} />
        </label>
        <label className="block text-sm font-medium">
          Email
          <input className="mt-1 w-full rounded-lg border border-border px-3 py-2 outline-none focus:border-accent" onChange={(event) => setForm({ ...form, email: event.target.value })} required type="email" value={form.email} />
        </label>
        <label className="block text-sm font-medium">
          Password
          <input className="mt-1 w-full rounded-lg border border-border px-3 py-2 outline-none focus:border-accent" minLength={8} onChange={(event) => setForm({ ...form, password: event.target.value })} required type="password" value={form.password} />
        </label>
        {error && <p className="rounded-lg bg-accent/10 px-3 py-2 text-sm text-accent">{error}</p>}
        <button className="inline-flex w-full items-center justify-center gap-2 rounded-lg bg-accent px-4 py-2 font-medium text-white hover:bg-accentHover" type="submit">
          <UserPlus size={18} />
          Create account
        </button>
      </form>
      <p className="mt-4 text-sm text-secondary">
        Already have an account?{' '}
        <Link className="font-medium text-accent" to="/login">
          Sign in
        </Link>
      </p>
    </AuthFormShell>
  );
}

