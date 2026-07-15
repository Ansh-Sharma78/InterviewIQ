export function AuthFormShell({ title, subtitle, children }) {
  return (
    <section className="mx-auto max-w-md">
      <div className="mb-6">
        <h1 className="text-3xl font-semibold text-primary">{title}</h1>
        <p className="mt-2 text-sm text-secondary">{subtitle}</p>
      </div>
      <div className="rounded-xl border border-border bg-card p-6 shadow-sm">{children}</div>
    </section>
  );
}

