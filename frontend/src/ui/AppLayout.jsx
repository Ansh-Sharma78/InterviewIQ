import { LogOut, UserRound } from 'lucide-react';
import { useEffect, useRef } from 'react';
import { Link, Outlet, useNavigate } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux';
import { attachStore } from '../lib/apiClient.js';
import { store } from '../store/store.js';
import { bootstrapSession, logout } from '../features/auth/authSlice.js';

const navLinkClass = 'rounded-lg px-3 py-2 text-sm text-secondary hover:bg-card hover:text-primary';

export function AppLayout() {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const { user, bootstrapped } = useSelector((state) => state.auth);
  const didBootstrap = useRef(false);

  useEffect(() => {
    if (didBootstrap.current) {
      return;
    }
    didBootstrap.current = true;
    attachStore(store);
    dispatch(bootstrapSession());
  }, [dispatch]);

  const onLogout = async () => {
    await dispatch(logout());
    navigate('/login');
  };

  if (!bootstrapped) {
    return <div className="flex min-h-screen items-center justify-center bg-background text-secondary">Loading InterviewIQ...</div>;
  }

  return (
    <div className="min-h-screen bg-background text-primary">
      <header className="sticky top-0 z-10 border-b border-border bg-sidebar">
        <div className="mx-auto flex max-w-6xl items-center justify-between px-4 py-3">
          <Link to="/dashboard" className="text-xl font-semibold text-primary">
            InterviewIQ
          </Link>
          <nav className="flex items-center gap-3">
            {user ? (
              <>
                <NavItem to="/resumes">Resumes</NavItem>
                <NavItem to="/job-descriptions">JDs</NavItem>
                <NavItem to="/reports/new">Reports</NavItem>
                <NavItem to="/chat">Chat</NavItem>
                <Link className={`${navLinkClass} inline-flex items-center gap-2`} to="/profile">
                  <UserRound size={16} />
                  {user.fullName}
                </Link>
                <button
                  className="inline-flex items-center gap-2 rounded-lg bg-accent px-3 py-2 text-sm font-medium text-white hover:bg-accentHover"
                  onClick={onLogout}
                  type="button"
                >
                  <LogOut size={16} />
                  Logout
                </button>
              </>
            ) : (
              <>
                <NavItem to="/login">Login</NavItem>
                <Link className="rounded-lg bg-accent px-3 py-2 text-sm font-medium text-white hover:bg-accentHover" to="/register">
                  Register
                </Link>
              </>
            )}
          </nav>
        </div>
      </header>
      <main className="mx-auto max-w-6xl px-4 py-8 md:py-10">
        <Outlet />
      </main>
    </div>
  );
}

function NavItem({ to, children }) {
  return (
    <Link className={navLinkClass} to={to}>
      {children}
    </Link>
  );
}
