import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { rentalStatusLabels, sizeLabels, type Rental } from "../model/models";
import { useAuth } from "../net/auth";
import { api } from "../net/client";
import { capabilities } from "../net/capabilities";
import { useInventory } from "../net/queries";
import { dateTime } from "../util/format";
import {
  Badge,
  ConfirmDialog,
  DataTable,
  Notice,
  PageHeader,
  QueryState,
  type Column,
} from "./components/common";

export function RentalsPage({
  administration = false,
}: {
  administration?: boolean;
}) {
  const { user } = useAuth();
  const isAdmin = user?.role === "ADMIN";
  const client = useQueryClient();
  const inventory = useInventory();
  const rentals = useQuery({
    queryKey: ["rentals", isAdmin ? "all" : "mine"],
    queryFn: ({ signal }) =>
      isAdmin ? api.rentals(signal) : api.myRentals(signal),
  });
  const rows = (rentals.data || []).filter(
    (row) => administration || !isAdmin || row.userId === user?.id,
  );
  const [selectedId, setSelectedId] = useState<number>();
  const [confirm, setConfirm] = useState<Rental>();
  const [success, setSuccess] = useState(false);
  const selected = rows.find((row) => row.id === selectedId);
  const canCancel = !isAdmin || capabilities.changeRentalStatus;
  const mutation = useMutation({
    mutationFn: (id: number) =>
      isAdmin ? api.cancelRentalAsAdmin(id) : api.cancelRental(id),
    onSuccess: async () => {
      setConfirm(undefined);
      setSelectedId(undefined);
      setSuccess(true);
      await Promise.all([
        client.invalidateQueries({ queryKey: ["rentals"] }),
        client.invalidateQueries({ queryKey: ["inventory"] }),
        client.invalidateQueries({ queryKey: ["available-cells"] }),
      ]);
    },
  });
  const cell = (row: Rental) =>
    inventory.data?.cells.find((value) => value.id === row.cellId);
  const warehouse = (row: Rental) =>
    inventory.data?.warehouses.find(
      (value) => value.id === cell(row)?.warehouseId,
    )?.name || "—";
  const columns: Column<Rental>[] = [
    {
      key: "selection",
      title: "Выбор",
      render: (row) => (
        <input
          type="radio"
          name="rental-selection"
          aria-label={`Выбрать аренду № ${row.id}`}
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
      title: "№ аренды",
      value: (row) => row.id,
      render: (row) => <strong className="table-emphasis">#{row.id}</strong>,
    },
    ...(administration
      ? [
          {
            key: "userId",
            title: "ID пользователя",
            value: (row: Rental) => row.userId || 0,
            render: (row: Rental) => row.userId,
          },
        ]
      : []),
    { key: "warehouse", title: "Склад", value: warehouse, render: warehouse },
    {
      key: "cell",
      title: "Ячейка",
      value: (row) => cell(row)?.number || "",
      render: (row) => (
        <span>
          {cell(row)?.number || `ID ${row.cellId}`}
          <small className="cell-subtitle">
            {cell(row) ? sizeLabels[cell(row)!.size] : ""}
          </small>
        </span>
      ),
    },
    {
      key: "start",
      title: "Начало",
      value: (row) => row.startDateTime,
      render: (row) => dateTime(row.startDateTime),
    },
    {
      key: "end",
      title: "Окончание",
      value: (row) => row.endDateTime,
      render: (row) => dateTime(row.endDateTime),
    },
    {
      key: "status",
      title: "Статус",
      value: (row) => rentalStatusLabels[row.status],
      render: (row) => (
        <Badge status={row.status} label={rentalStatusLabels[row.status]} />
      ),
    },
  ];
  const refresh = () => {
    setSuccess(false);
    void inventory.refetch();
    void rentals.refetch();
  };
  return (
    <>
      <PageHeader
        title={administration ? "Управление бронями" : "Мои аренды"}
        description={
          administration
            ? "Все бронирования и сроки хранения."
            : "Ваши бронирования и сроки хранения."
        }
        back={administration ? "/admin" : "/"}
        refresh={refresh}
        refreshing={inventory.isFetching || rentals.isFetching}
      />
      <QueryState
        pending={inventory.isPending || rentals.isPending}
        error={inventory.error || rentals.error}
        retry={refresh}
      >
        {success && <Notice tone="success">Бронирование отменено.</Notice>}
        <DataTable
          rows={rows}
          columns={columns}
          selectedId={selectedId}
          searchText={(row) =>
            `${row.id} ${row.userId || ""} ${warehouse(row)} ${cell(row)?.number || ""} ${rentalStatusLabels[row.status]}`
          }
          label="Аренды"
          emptyText={
            administration ? "Бронирований пока нет" : "У вас пока нет аренд"
          }
        />
        <div className="action-panel">
          <div>
            <strong>
              {selected
                ? `Аренда № ${selected.id}`
                : "Выберите аренду в таблице"}
            </strong>
            <p>
              {canCancel
                ? "Можно отменить только действующее бронирование."
                : "Отмена бронирований администратором пока недоступна."}
            </p>
          </div>
          <button
            className="button danger-button"
            disabled={!canCancel || !selected || selected.status !== "ACTIVE"}
            onClick={() => {
              mutation.reset();
              setConfirm(selected);
            }}
          >
            Отменить бронь
          </button>
        </div>
      </QueryState>
      {confirm && (
        <ConfirmDialog
          title={`Отменить бронь № ${confirm.id}?`}
          busy={mutation.isPending}
          error={mutation.error}
          confirmLabel="Отменить бронь"
          onConfirm={() => mutation.mutate(confirm.id)}
          onClose={() => setConfirm(undefined)}
        >
          <p>
            Бронирование ячейки {cell(confirm)?.number || confirm.cellId} будет
            отменено. Это действие нельзя отменить.
          </p>
        </ConfirmDialog>
      )}
    </>
  );
}
