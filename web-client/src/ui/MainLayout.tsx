import { useEffect, useRef, useState } from "react";
import { Link, Outlet, useLocation } from "react-router-dom";
import { useAuth } from "../net/auth";
import { roleLabels, userStatusLabels } from "../model/models";
import { Brand } from "./components/common";
import { Icon } from "./components/Icon";

export function MainLayout() {
  const { user, logout } = useAuth();
  const location = useLocation();
  const menu = useRef<HTMLDivElement>(null);
  const trigger = useRef<HTMLButtonElement>(null);
  const [open, setOpen] = useState(false);
  useEffect(() => {
    setOpen(false);
    window.scrollTo(0, 0);
    document.getElementById("main-content")?.focus({ preventScroll: true });
  }, [location.pathname]);
  useEffect(() => {
    const close = (event: MouseEvent) => {
      if (!menu.current?.contains(event.target as Node)) setOpen(false);
    };
    const escape = (event: KeyboardEvent) => {
      if (event.key === "Escape" && open) {
        setOpen(false);
        trigger.current?.focus();
      }
    };
    document.addEventListener("click", close);
    document.addEventListener("keydown", escape);
    return () => {
      document.removeEventListener("click", close);
      document.removeEventListener("keydown", escape);
    };
  }, [open]);
  if (!user) return null;
  return (
    <div className="app-shell">
      <a className="skip-link" href="#main-content">
        Перейти к содержимому
      </a>
      <header className="app-header">
        <div className="header-inner">
          <Brand />
          <nav aria-label="Основная навигация">
            <Link
              className={`nav-link ${location.pathname === "/" ? "active" : ""}`}
              to="/"
            >
              Главная
            </Link>
          </nav>
          <div className="account" ref={menu}>
            <button
              className={`account-trigger ${open ? "is-open" : ""}`}
              ref={trigger}
              aria-label={`${user.login} · ${roleLabels[user.role]}`}
              aria-expanded={open}
              aria-controls="account-panel"
              onClick={() => setOpen(!open)}
            >
              <span className="avatar">
                {user.login.slice(0, 1).toLocaleUpperCase("ru-RU")}
              </span>
              <span className="account-copy">
                <strong>{user.login}</strong>
                <span>{roleLabels[user.role]}</span>
              </span>
              <Icon name="chevron" />
            </button>
            {open && (
              <div id="account-panel" className="account-panel">
                <strong>{user.login}</strong>
                <span>
                  ID: {user.id} · {userStatusLabels[user.status]}
                </span>
                <div className="account-divider" />
                <button className="account-signout" onClick={logout}>
                  <Icon name="logout" />
                  Выйти из аккаунта
                </button>
              </div>
            )}
          </div>
        </div>
      </header>
      <main id="main-content" tabIndex={-1} className="main-content">
        <Outlet />
      </main>
      <Footer />
    </div>
  );
}
export function Footer() {
  return (
    <footer className="app-footer">
      <div>
        <span className="footer-wordmark">SAFEZONE</span>
        <span>Система аренды складских ячеек</span>
      </div>
    </footer>
  );
}
