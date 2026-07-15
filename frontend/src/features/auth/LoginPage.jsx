import { LogIn } from 'lucide-react';
import { useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux';
import { AuthFormShell } from './AuthFormShell.jsx';
import { login } from './authSlice.js';

export function LoginPage() {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const location = useLocation();
  const { status, error } = useSelector((state) => state.auth);
  const [form, setForm] = useState({ email: '', password: '' });

  const onSubmit = async (event) => {
    event.preventDefault();
    const result = await dispatch(login(form));
    if (login.fulfilled.match(result)) {
      navigate(location.state?.from?.pathname ?? '/dashboard');
    }
  };

  return (
    <AuthFormShell title="Welcome back" subtitle="Sign in to continue your interview preparation.">
      <form className="space-y-4" onSubmit={onSubmit}>
        <label className="block text-sm font-medium">
          Email
          <input
            className="mt-1 w-full rounded-lg border border-border px-3 py-2 outline-none focus:border-accent"
            onChange={(event) => setForm({ ...form, email: event.target.value })}
            required
            type="email"
            value={form.email}
          />
        </label>
        <label className="block text-sm font-medium">
          Password
          <input
            className="mt-1 w-full rounded-lg border border-border px-3 py-2 outline-none focus:border-accent"
            onChange={(event) => setForm({ ...form, password: event.target.value })}
            required
            type="password"
            value={form.password}
          />
        </label>
        {error && <p className="rounded-lg bg-accent/10 px-3 py-2 text-sm text-accent">{error}</p>}
        <button className="inline-flex w-full items-center justify-center gap-2 rounded-lg bg-accent px-4 py-2 font-medium text-white hover:bg-accentHover" disabled={status === 'loading'} type="submit">
          <LogIn size={18} />
          {status === 'loading' ? 'Signing in...' : 'Sign in'}
        </button>
      </form>
      <p className="mt-4 text-sm text-secondary">
        New here?{' '}
        <Link className="font-medium text-accent" to="/register">
          Create an account
        </Link>
      </p>
    </AuthFormShell>
  );
}

