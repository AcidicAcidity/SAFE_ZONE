import { Component, type ErrorInfo, type ReactNode } from "react";
import {
  Link,
  Navigate,
  Outlet,
  Route,
  Routes,
  useLocation,
} from "react-router-dom";
import { useAuth } from "./net/auth";
import { AuthPage } from "./ui/auth/AuthPage";
import { MainLayout } from "./ui/MainLayout";
import { AdminPage, HomePage } from "./ui/HomePage";
import { CellsPage, StatisticsPage, WarehousesPage } from "./ui/InventoryPages";
import { RentalsPage } from "./ui/RentalsPage";
import { RentPage } from "./ui/RentPage";
import { UsersPage } from "./ui/UsersPage";
import { Loading, Notice } from "./ui/components/common";

function RequireAuth() {
  const auth = useAuth();
  const location = useLocation();
  if (auth.loading)
    return (
      <div className="full-state">
        <Loading text="Открываем ваш кабинет…" />
      </div>
    );
  if (auth.error)
    return (
      <div className="full-state">
        <Notice tone="error">{auth.error.message}</Notice>
        <div className="dialog-actions">
          <button className="button" onClick={auth.retry}>
            Повторить
          </button>
          <button className="button" onClick={auth.logout}>
            Вернуться ко входу
          </button>
        </div>
      </div>
    );
  return auth.user ? (
    <Outlet />
  ) : (
    <Navigate to="/login" replace state={{ from: location.pathname }} />
  );
}
function RequireAdmin() {
  const { user } = useAuth();
  return user?.role === "ADMIN" ? (
    <Outlet />
  ) : (
    <>
      <h1>Недостаточно прав</h1>
      <p>Этот раздел доступен администратору.</p>
      <Link className="button" to="/">
        На главную
      </Link>
    </>
  );
}
class ErrorBoundary extends Component<
  { children: ReactNode },
  { failed: boolean }
> {
  state = { failed: false };
  static getDerivedStateFromError() {
    return { failed: true };
  }
  componentDidCatch(_error: Error, _info: ErrorInfo) {
    /* Do not log account data. */
  }
  render() {
    return this.state.failed ? (
      <div className="full-state">
        <h1>Не удалось открыть страницу</h1>
        <p>Обновите приложение и попробуйте снова.</p>
        <button
          className="button primary-button"
          onClick={() => window.location.reload()}
        >
          Обновить страницу
        </button>
      </div>
    ) : (
      this.props.children
    );
  }
}
export function App() {
  return (
    <ErrorBoundary>
      <Routes>
        <Route path="/login" element={<AuthPage key="login" />} />
        <Route
          path="/register"
          element={<AuthPage key="register" registration />}
        />
        <Route element={<RequireAuth />}>
          <Route element={<MainLayout />}>
            <Route index element={<HomePage />} />
            <Route path="warehouses" element={<WarehousesPage />} />
            <Route path="cells" element={<CellsPage />} />
            <Route path="rent" element={<RentPage />} />
            <Route path="rentals" element={<RentalsPage />} />
            <Route element={<RequireAdmin />}>
              <Route path="admin" element={<AdminPage />} />
              <Route path="admin/statistics" element={<StatisticsPage />} />
              <Route path="admin/users" element={<UsersPage />} />
              <Route
                path="admin/rentals"
                element={<RentalsPage administration />}
              />
            </Route>
            <Route
              path="*"
              element={
                <>
                  <h1>Страница не найдена</h1>
                  <p>Воспользуйтесь главным меню, чтобы продолжить работу.</p>
                  <Link to="/" className="button">
                    На главную
                  </Link>
                </>
              }
            />
          </Route>
        </Route>
      </Routes>
    </ErrorBoundary>
  );
}
