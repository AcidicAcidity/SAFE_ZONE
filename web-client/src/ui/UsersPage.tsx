import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
  roleLabels,
  userStatusLabels,
  type User,
  type UserStatus,
} from "../model/models";
import { useAuth } from "../net/auth";
import { api } from "../net/client";
import { capabilities } from "../net/capabilities";
import {
  Badge,
  ConfirmDialog,
  DataTable,
  Notice,
  PageHeader,
  QueryState,
  type Column,
} from "./components/common";

export function UsersPage() {
  const { user } = useAuth();
  const client = useQueryClient();
  const users = useQuery({
    queryKey: ["users"],
    queryFn: ({ signal }) => api.users(signal),
  });
  const [selectedId, setSelectedId] = useState<number>();
  const [action, setAction] = useState<{ user: User; status: UserStatus }>();
  const [success, setSuccess] = useState(false);
  const selected = users.data?.find((row) => row.id === selectedId);
  const blocked =
    !capabilities.changeUserStatus || !selected || selected.id === user?.id;
  const mutation = useMutation({
    mutationFn: (value: { user: User; status: UserStatus }) =>
      api.changeUserStatus(value.user.id, value.status),
    onSuccess: async () => {
      setAction(undefined);
      setSuccess(true);
      await client.invalidateQueries({ queryKey: ["users"] });
    },
  });
  const verbs: Record<UserStatus, string> = {
    BLOCKED: "Заблокировать",
    ACTIVE: "Разблокировать",
    DELETED: "Удалить аккаунт",
  };
  const columns: Column<User>[] = [
    {
      key: "selection",
      title: "Выбор",
      render: (row) => (
        <input
          type="radio"
          name="user-selection"
          aria-label={`Выбрать пользователя ${row.login}`}
          checked={selectedId === row.id}
          onChange={() => {
            setSelectedId(row.id);
            setSuccess(false);
          }}
        />
      ),
    },
    {
      key: "id",
      title: "ID",
      value: (row) => row.id,
      render: (row) => <span className="muted">{row.id}</span>,
    },
    {
      key: "login",
      title: "Логин",
      value: (row) => row.login,
      render: (row) => (
        <strong className="table-emphasis">
          {row.login}
          {row.id === user?.id && <span className="you-label">Вы</span>}
        </strong>
      ),
    },
    {
      key: "role",
      title: "Роль",
      value: (row) => roleLabels[row.role],
      render: (row) => roleLabels[row.role],
    },
    {
      key: "status",
      title: "Статус",
      value: (row) => userStatusLabels[row.status],
      render: (row) => (
        <Badge status={row.status} label={userStatusLabels[row.status]} />
      ),
    },
  ];
  return (
    <>
      <PageHeader
        title="Управление аккаунтами"
        description="Пользователи, роли и статусы аккаунтов."
        back="/admin"
        refresh={() => {
          void users.refetch();
        }}
        refreshing={users.isFetching}
      />
      <QueryState
        pending={users.isPending}
        error={users.error}
        retry={() => {
          void users.refetch();
        }}
      >
        {success && <Notice tone="success">Статус аккаунта обновлён.</Notice>}
        <DataTable
          rows={users.data || []}
          columns={columns}
          selectedId={selectedId}
          searchText={(row) =>
            `${row.id} ${row.login} ${roleLabels[row.role]} ${userStatusLabels[row.status]}`
          }
          label="Пользователи"
          emptyText="Пользователи не найдены"
        />
        <div className="action-panel user-actions">
          <div>
            <strong>
              {selected
                ? `Пользователь № ${selected.id} · ${selected.login}`
                : "Пользователь не выбран"}
            </strong>
            <p>
              {!capabilities.changeUserStatus
                ? "Изменение статусов аккаунтов пока недоступно."
                : selected?.id === user?.id
                  ? "Изменять статус собственного аккаунта нельзя."
                  : "Выберите пользователя и нужное действие."}
            </p>
          </div>
          <div className="button-group">
            {(["BLOCKED", "ACTIVE", "DELETED"] as const).map((status) => (
              <button
                key={status}
                className={`button ${status === "DELETED" ? "danger-button" : ""}`}
                disabled={blocked || selected?.status === status}
                onClick={() => {
                  if (selected) {
                    mutation.reset();
                    setAction({ user: selected, status });
                  }
                }}
              >
                {verbs[status]}
              </button>
            ))}
          </div>
        </div>
      </QueryState>
      {action && (
        <ConfirmDialog
          title={`${verbs[action.status]}?`}
          confirmLabel={verbs[action.status]}
          busy={mutation.isPending}
          error={mutation.error}
          onConfirm={() => mutation.mutate(action)}
          onClose={() => setAction(undefined)}
        >
          <p>
            Аккаунт: <strong>{action.user.login}</strong>. Новый статус:{" "}
            {userStatusLabels[action.status].toLocaleLowerCase("ru-RU")}.
          </p>
        </ConfirmDialog>
      )}
    </>
  );
}
