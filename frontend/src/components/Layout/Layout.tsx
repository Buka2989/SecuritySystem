import { Link } from 'react-router-dom';
import { ReviewNotifications } from '@/components/ReviewNotifications';
import type { ReactNode } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '@/hooks/useAuth';

export function Layout({ children }: { children: ReactNode }) {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  function handleLogout() {
    logout();
    navigate('/login', { replace: true });
  }

  return (
    <div className="app-shell">
      <header className="topbar">
        <span className="topbar__brand">Security System · Школа</span>
        {user && (
          <div className="topbar__user">
            <span>{user.fullName}</span>
            <button className="topbar__logout" onClick={handleLogout}>
              Выйти
            </button>
          </div>
        )}
      </header>
      <nav className="page__header" style={{padding:'12px 24px'}}>
        {user?.role==='ADMIN' && <><Link to="/admin">Заявки</Link><Link to="/manage-students">Ученики</Link></>}
        {user?.role==='SECURITY' && <Link to="/security">Сканирование</Link>}
        {(user?.role==='ADMIN'||user?.role==='SECURITY') && <Link to="/reports">Отчёт о доступе</Link>}
      </nav><ReviewNotifications />{children}
    </div>
  );
}
