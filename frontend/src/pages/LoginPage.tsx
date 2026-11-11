import { useState, type FormEvent } from 'react';
import { Navigate, useNavigate } from 'react-router-dom';
import { useAuth } from '@/hooks/useAuth';

export function LoginPage() {
  const { user, login, isAuthenticating, error } = useAuth();
  const navigate = useNavigate();

  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);

  if (user) {
    if ((user.role === 'PARENT' || user.role === 'TRUSTED_PERSON')) {
      return <Navigate to="/dashboard" replace />;
    }

    if (user.role === 'SECURITY') {
      return <Navigate to="/security" replace />;
    }

    if (user.role === 'ADMIN') {
      return <Navigate to="/admin" replace />;
    }
  }

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setShowPassword(false);

    try {
      const loggedInUser = await login(username, password);

      if ((loggedInUser.role === 'PARENT' || loggedInUser.role === 'TRUSTED_PERSON')) {
        navigate('/dashboard', { replace: true });
      } else if (loggedInUser.role === 'SECURITY') {
        navigate('/security', { replace: true });
      } else if (loggedInUser.role === 'ADMIN') {
        navigate('/admin', { replace: true });
      }
    } catch {
      // error is surfaced via auth context state
    }
  }

  return (
    <div className="login-screen">
      <div className="login-card">
        <h1 className="login-card__title">Вход в систему</h1>

        <p className="login-card__subtitle">
          Войдите, чтобы получить доступ к функциям системы в соответствии с вашей ролью.
        </p>

        {error && <div className="form-error">{error}</div>}

        <form onSubmit={handleSubmit}>
          <div className="field">
            <label htmlFor="username">Имя пользователя</label>

            <input
              id="username"
              type="text"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              autoComplete="username"
              required
            />
          </div>

          <div className="field">
            <label htmlFor="password">Пароль</label>
            <div className="login-password">
              <input
                id="password"
                type={showPassword ? 'text' : 'password'}
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                autoComplete="current-password"
                required
              />
              <button
                type="button"
                className="login-password__toggle"
                aria-controls="password"
                aria-label={showPassword ? 'Скрыть пароль' : 'Показать пароль'}
                onClick={() => setShowPassword((shown) => !shown)}
              >
                <svg
                  width="18"
                  height="18"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  strokeWidth="1.75"
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  aria-hidden="true"
                >
                  {showPassword ? (
                    <>
                      <path d="M3 3l18 18M10.6 10.6a2 2 0 0 0 2.8 2.8" />
                      <path d="M10.7 5.1A10 10 0 0 1 12 5c5 0 8.5 4 10 7a15 15 0 0 1-2.7 3.6M6.4 6.4C4.4 8 2.9 10 2 12c1.5 3 5 7 10 7 2 0 3.9-.7 5.6-1.7" />
                    </>
                  ) : (
                    <>
                      <path d="M2 12c1.5-3 5-7 10-7s8.5 4 10 7c-1.5 3-5 7-10 7s-8.5-4-10-7Z" />
                      <circle cx="12" cy="12" r="3" />
                    </>
                  )}
                </svg>
              </button>
            </div>
          </div>

          <button
            type="submit"
            className="btn-primary"
            disabled={isAuthenticating}
          >
            {isAuthenticating ? 'Входим…' : 'Войти'}
          </button>
        </form>

        <div className="login-card__hint"></div>
      </div>
    </div>
  );
}
