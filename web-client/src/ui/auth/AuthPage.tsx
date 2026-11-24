import { useState, type FormEvent } from "react";
import { Link, Navigate, useLocation, useNavigate } from "react-router-dom";
import { useAuth } from "../../net/auth";
import { api } from "../../net/client";
import { errorMessage } from "../../util/format";
import { Brand, Loading, Notice } from "../components/common";
import { Icon } from "../components/Icon";
import { Footer } from "../MainLayout";

function PasswordField({
  id,
  label,
  value,
  onChange,
  autoComplete,
  hint,
}: {
  id: string;
  label: string;
  value: string;
  onChange: (value: string) => void;
  autoComplete: string;
  hint?: string;
}) {
  const [visible, setVisible] = useState(false);
  return (
    <div className="field">
      <label htmlFor={id}>
        {label} <span className="required">*</span>
      </label>
      <div className="password-field">
        <input
          id={id}
          type={visible ? "text" : "password"}
          value={value}
          onChange={(event) => onChange(event.target.value)}
          autoComplete={autoComplete}
          required
          aria-describedby={hint ? `${id}-hint` : undefined}
        />
        <button
          type="button"
          aria-label={
            visible
              ? `Скрыть: ${label.toLowerCase()}`
              : `Показать: ${label.toLowerCase()}`
          }
          aria-pressed={visible}
          onClick={() => setVisible(!visible)}
        >
          <Icon name={visible ? "eye-off" : "eye"} />
        </button>
      </div>
      {hint && <small id={`${id}-hint`}>{hint}</small>}
    </div>
  );
}
export function AuthPage({ registration = false }: { registration?: boolean }) {
  const auth = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [login, setLogin] = useState(location.state?.login || "");
  const [password, setPassword] = useState("");
  const [confirmation, setConfirmation] = useState("");
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);
  const from = location.state?.from;
  if (auth.user)
    return (
      <Navigate
        to={
          typeof from === "string" &&
          from.startsWith("/") &&
          !from.startsWith("//") &&
          !["/login", "/register"].includes(from)
            ? from
            : "/"
        }
        replace
      />
    );
  async function submit(event: FormEvent) {
    event.preventDefault();
    if (busy) return;
    setError("");
    if (
      registration &&
      (login.length < 3 || password.length < 4 || password !== confirmation)
    ) {
      setError(
        login.length < 3
          ? "Логин должен содержать не менее 3 символов."
          : password.length < 4
            ? "Пароль должен содержать не менее 4 символов."
            : "Пароли не совпадают.",
      );
      return;
    }
    setBusy(true);
    try {
      if (registration) {
        await api.register(login, password);
        navigate("/login", {
          replace: true,
          state: { login, registered: true },
        });
      } else await auth.login(login, password);
    } catch (failure) {
      setError(errorMessage(failure));
    } finally {
      setPassword("");
      setConfirmation("");
      setBusy(false);
    }
  }
  return (
    <div className="app-shell auth-shell">
      <header className="app-header">
        <div className="header-inner">
          <Brand />
          <span className="auth-header-caption">
            <Icon name="shield" />
            Ваше пространство для хранения
          </span>
        </div>
      </header>
      <main className="auth-main">
        <section className="auth-card" aria-labelledby="auth-title">
          <div className="auth-tabs" aria-label="Авторизация">
            <Link
              to="/login"
              className={!registration ? "selected" : ""}
              aria-current={!registration ? "page" : undefined}
              tabIndex={busy ? -1 : undefined}
              aria-disabled={busy}
              onClick={(event) => {
                if (busy) event.preventDefault();
              }}
            >
              Вход
            </Link>
            <Link
              to="/register"
              className={registration ? "selected" : ""}
              aria-current={registration ? "page" : undefined}
              tabIndex={busy ? -1 : undefined}
              aria-disabled={busy}
              onClick={(event) => {
                if (busy) event.preventDefault();
              }}
            >
              Регистрация
            </Link>
          </div>
          <span className="auth-emblem">
            <Icon name={registration ? "users" : "key"} />
          </span>
          <h1 id="auth-title">
            {registration ? "Создать аккаунт" : "Вход в систему"}
          </h1>
          <p className="auth-subtitle">
            {registration
              ? "Зарегистрируйтесь, чтобы пользоваться складскими ячейками."
              : "Войдите, чтобы управлять ячейками и арендой."}
          </p>
          {auth.loading ? (
            <Loading text="Открываем ваш кабинет…" />
          ) : auth.error ? (
            <>
              <Notice tone="error">{auth.error.message}</Notice>
              <div className="dialog-actions">
                <button className="button" onClick={auth.retry}>
                  Повторить
                </button>
                <button className="text-button" onClick={auth.logout}>
                  Вернуться ко входу
                </button>
              </div>
            </>
          ) : (
            <>
              <form onSubmit={submit}>
                <fieldset disabled={busy}>
                  {error && <Notice tone="error">{error}</Notice>}
                  {!error && auth.notice && <Notice>{auth.notice}</Notice>}
                  {!registration && location.state?.registered && (
                    <Notice tone="success">
                      Регистрация завершена. Войдите в аккаунт.
                    </Notice>
                  )}
                  <div className="field">
                    <label htmlFor="login">
                      Логин <span className="required">*</span>
                    </label>
                    <input
                      id="login"
                      name="username"
                      value={login}
                      onChange={(event) => setLogin(event.target.value)}
                      autoComplete="username"
                      autoFocus
                      required
                      placeholder="Введите логин"
                      aria-describedby={registration ? "login-hint" : undefined}
                    />
                    {registration && (
                      <small id="login-hint">Не менее 3 символов</small>
                    )}
                  </div>
                  <PasswordField
                    id="password"
                    label="Пароль"
                    value={password}
                    onChange={setPassword}
                    autoComplete={
                      registration ? "new-password" : "current-password"
                    }
                    hint={registration ? "Не менее 4 символов" : undefined}
                  />
                  {registration && (
                    <PasswordField
                      id="confirmation"
                      label="Повторите пароль"
                      value={confirmation}
                      onChange={setConfirmation}
                      autoComplete="new-password"
                    />
                  )}
                  <button
                    className="button primary-button auth-submit"
                    type="submit"
                  >
                    {busy
                      ? "Пожалуйста, подождите…"
                      : registration
                        ? "Зарегистрироваться"
                        : "Войти"}
                  </button>
                </fieldset>
              </form>
              <p className="auth-switch">
                {registration ? "Уже есть аккаунт?" : "Нет аккаунта?"}{" "}
                <Link
                  to={registration ? "/login" : "/register"}
                  aria-disabled={busy}
                  onClick={(event) => {
                    if (busy) event.preventDefault();
                  }}
                >
                  {registration ? "Войти" : "Зарегистрироваться"}
                </Link>
              </p>
            </>
          )}
        </section>
        <div className="auth-footnote">
          <Icon name="box" />
          Всё для хранения — в одном месте
        </div>
      </main>
      <Footer />
    </div>
  );
}
