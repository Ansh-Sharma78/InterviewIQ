import { Save } from 'lucide-react';
import { useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { updateProfile } from './authSlice.js';

export function ProfilePage() {
  const dispatch = useDispatch();
  const user = useSelector((state) => state.auth.user);
  const [saved, setSaved] = useState(false);
  const [form, setForm] = useState({
    fullName: user?.fullName ?? '',
    targetRole: user?.targetRole ?? '',
    experienceLevel: user?.experienceLevel ?? '',
    targetCompanies: user?.targetCompanies ?? ''
  });

  const onSubmit = async (event) => {
    event.preventDefault();
    const result = await dispatch(updateProfile(form));
    setSaved(updateProfile.fulfilled.match(result));
  };

  return (
    <section className="max-w-2xl">
      <h1 className="text-3xl font-semibold text-primary">Profile</h1>
      <form className="mt-6 space-y-4 rounded-xl border border-border bg-card p-6 shadow-sm" onSubmit={onSubmit}>
        <label className="block text-sm font-medium">
          Full name
          <input className="mt-1 w-full rounded-lg border border-border px-3 py-2 outline-none focus:border-accent" onChange={(event) => setForm({ ...form, fullName: event.target.value })} required value={form.fullName} />
        </label>
        <label className="block text-sm font-medium">
          Target role
          <input className="mt-1 w-full rounded-lg border border-border px-3 py-2 outline-none focus:border-accent" onChange={(event) => setForm({ ...form, targetRole: event.target.value })} value={form.targetRole} />
        </label>
        <label className="block text-sm font-medium">
          Experience level
          <input className="mt-1 w-full rounded-lg border border-border px-3 py-2 outline-none focus:border-accent" onChange={(event) => setForm({ ...form, experienceLevel: event.target.value })} value={form.experienceLevel} />
        </label>
        <label className="block text-sm font-medium">
          Target companies
          <textarea className="mt-1 min-h-24 w-full rounded-lg border border-border px-3 py-2 outline-none focus:border-accent" onChange={(event) => setForm({ ...form, targetCompanies: event.target.value })} value={form.targetCompanies} />
        </label>
        {saved && <p className="text-sm text-accent">Profile saved.</p>}
        <button className="inline-flex items-center gap-2 rounded-lg bg-accent px-4 py-2 font-medium text-white hover:bg-accentHover" type="submit">
          <Save size={18} />
          Save profile
        </button>
      </form>
    </section>
  );
}

