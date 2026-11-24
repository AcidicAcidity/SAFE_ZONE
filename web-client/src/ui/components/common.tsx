import { useEffect, useRef, useState, type ReactNode } from "react";
import { Link } from "react-router-dom";
import { Icon, type IconName } from "./Icon";
import { errorMessage } from "../../util/format";

export function Brand() {
  return (
    <Link className="brand" to="/" aria-label="SafeZone — главная">
      <span className="brand-mark">
        <Icon name="box" />
      </span>
      <span>
        <strong>Аренда ячеек</strong>
        <span className="wordmark">SAFEZONE</span>
      </span>
    </Link>
  );
}
export function Notice({
  children,
  tone = "info",
}: {
  children: ReactNode;
  tone?: "info" | "error" | "success";
}) {
  return (
    <div
      className={`notice notice-${tone}`}
      role={tone === "error" ? "alert" : "status"}
    >
      <Icon name={tone === "success" ? "check" : "info"} />
      <div>{children}</div>
    </div>
  );
}
export function Loading({ text = "Загружаем данные…" }: { text?: string }) {
  return (
    <div className="loading-state" role="status">
      <span className="spinner" />
      <span>{text}</span>
    </div>
  );
}
export function QueryState({
  pending,
  error,
  retry,
  children,
}: {
  pending: boolean;
  error: unknown;
  retry: () => void;
  children: ReactNode;
}) {
  if (pending) return <Loading />;
  if (error)
    return (
      <div className="error-state">
        <Notice tone="error">{errorMessage(error)}</Notice>
        <button className="button" onClick={retry}>
          <Icon name="refresh" />
          Повторить
        </button>
      </div>
    );
  return children;
}
export function PageHeader({
  title,
  description,
  back = "/",
  refresh,
  refreshing,
}: {
  title: string;
  description: string;
  back?: string;
  refresh?: () => void;
  refreshing?: boolean;
}) {
  return (
    <div className="page-heading">
      <div>
        <Link to={back} className="back-link">
          <Icon name="back" />
          {back === "/admin" ? "Панель администратора" : "На главную"}
        </Link>
        <h1>{title}</h1>
        <p>{description}</p>
      </div>
      {refresh && (
        <button
          className="button refresh-button"
          onClick={refresh}
          disabled={refreshing}
        >
          <Icon name="refresh" className={refreshing ? "spinning" : ""} />
          Обновить
        </button>
      )}
    </div>
  );
}
export function NavigationCard({
  title,
  description,
  to,
  icon,
  accent = false,
  disabled = false,
}: {
  title: string;
  description: string;
  to?: string;
  icon: IconName;
  accent?: boolean;
  disabled?: boolean;
}) {
  const content = (
    <>
      <span className="card-icon">
        <Icon name={icon} />
      </span>
      <span className="card-copy">
        <strong>{title}</strong>
        <span>{description}</span>
      </span>
      {disabled && <span className="tag">Позже</span>}
    </>
  );
  return to && !disabled ? (
    <Link
      className={`navigation-card ${accent ? "navigation-card-accent" : ""}`}
      to={to}
    >
      {content}
    </Link>
  ) : (
    <div
      className="navigation-card navigation-card-disabled"
      aria-disabled="true"
    >
      {content}
    </div>
  );
}
export function Badge({ status, label }: { status: string; label: string }) {
  const tone = ["AVAILABLE", "ACTIVE"].includes(status)
    ? "green"
    : ["BLOCKED", "UNAVAILABLE"].includes(status)
      ? "red"
      : "neutral";
  return <span className={`badge badge-${tone}`}>{label}</span>;
}

export interface Column<T> {
  key: string;
  title: string;
  render: (row: T) => ReactNode;
  value?: (row: T) => string | number;
}
export function DataTable<T extends { id: number }>({
  rows,
  columns,
  searchText,
  emptyText,
  label,
  selectedId,
}: {
  rows: T[];
  columns: Column<T>[];
  searchText: (row: T) => string;
  emptyText: string;
  label: string;
  selectedId?: number;
}) {
  const [search, setSearch] = useState("");
  const [sort, setSort] = useState<{ key: string; direction: 1 | -1 }>({
    key: "id",
    direction: 1,
  });
  const filtered = rows.filter((row) =>
    searchText(row)
      .toLocaleLowerCase("ru-RU")
      .includes(search.toLocaleLowerCase("ru-RU").trim()),
  );
  const column = columns.find((item) => item.key === sort.key);
  if (column?.value)
    filtered.sort((a, b) => {
      const left = column.value!(a),
        right = column.value!(b);
      return (
        sort.direction *
        (typeof left === "number" && typeof right === "number"
          ? left - right
          : String(left).localeCompare(String(right), "ru", { numeric: true }))
      );
    });
  return (
    <section className="table-panel" aria-label={label}>
      <div className="table-toolbar">
        <div className="table-count">
          Всего записей <strong>{rows.length}</strong>
        </div>
        <label className="search-field">
          <Icon name="search" />
          <input
            type="search"
            placeholder="Поиск по таблице"
            aria-label={`Поиск: ${label}`}
            value={search}
            onChange={(event) => setSearch(event.target.value)}
          />
        </label>
      </div>
      <div
        className="table-scroll"
        tabIndex={0}
        aria-label={`Таблица: ${label}`}
      >
        <table>
          <caption className="sr-only">{label}</caption>
          <thead>
            <tr>
              {columns.map((item) => (
                <th
                  key={item.key}
                  scope="col"
                  aria-sort={
                    !item.value
                      ? undefined
                      : sort.key === item.key
                        ? sort.direction === 1
                          ? "ascending"
                          : "descending"
                        : "none"
                  }
                >
                  {item.value ? (
                    <button
                      className="table-sort"
                      onClick={() =>
                        setSort({
                          key: item.key,
                          direction:
                            sort.key === item.key && sort.direction === 1
                              ? -1
                              : 1,
                        })
                      }
                    >
                      {item.title}
                      <span aria-hidden="true" className="sort-mark">
                        {sort.key === item.key
                          ? sort.direction === 1
                            ? "▴"
                            : "▾"
                          : "⋮"}
                      </span>
                    </button>
                  ) : (
                    item.title
                  )}
                </th>
              ))}
            </tr>
          </thead>
          <tbody>
            {filtered.map((row) => (
              <tr
                key={row.id}
                className={row.id === selectedId ? "selected-row" : ""}
              >
                {columns.map((item) => (
                  <td key={item.key}>{item.render(row)}</td>
                ))}
              </tr>
            ))}
          </tbody>
        </table>
        {!filtered.length && (
          <div className="empty-state">
            <span className="empty-icon">
              <Icon name="search" />
            </span>
            <strong>{search ? "Ничего не найдено" : emptyText}</strong>
            <span>
              {search
                ? "Попробуйте изменить поисковый запрос."
                : "Здесь появятся данные, когда они будут доступны."}
            </span>
            {search && (
              <button className="text-button" onClick={() => setSearch("")}>
                Сбросить поиск
              </button>
            )}
          </div>
        )}
      </div>
      <div className="table-bottom">
        Показано: {filtered.length} из {rows.length}
      </div>
    </section>
  );
}
export function ConfirmDialog({
  title,
  children,
  busy,
  error,
  confirmLabel,
  onConfirm,
  onClose,
}: {
  title: string;
  children: ReactNode;
  busy: boolean;
  error?: unknown;
  confirmLabel: string;
  onConfirm: () => void;
  onClose: () => void;
}) {
  const ref = useRef<HTMLDialogElement>(null);
  useEffect(() => {
    ref.current?.showModal();
  }, []);
  return (
    <dialog
      ref={ref}
      className="confirm-dialog"
      aria-labelledby="confirm-title"
      onCancel={(event) => {
        event.preventDefault();
        if (!busy) onClose();
      }}
    >
      <div className="dialog-body">
        <span className="card-icon">
          <Icon name="info" />
        </span>
        <h2 id="confirm-title">{title}</h2>
        <div className="dialog-description">{children}</div>
        {!!error && <Notice tone="error">{errorMessage(error)}</Notice>}
        <div className="dialog-actions">
          <button className="button" onClick={onClose} disabled={busy}>
            Назад
          </button>
          <button
            className="button danger-button"
            onClick={onConfirm}
            disabled={busy}
          >
            {busy ? "Сохраняем…" : confirmLabel}
          </button>
        </div>
      </div>
    </dialog>
  );
}
