import { useQuery } from "@tanstack/react-query";
import type { Cell, Warehouse } from "../model/models";
import {
  cellStatusLabels,
  countCellRentals,
  sizeLabels,
} from "../model/models";
import { api } from "../net/client";
import { useAllRentals, useInventory } from "../net/queries";
import {
  Badge,
  DataTable,
  PageHeader,
  QueryState,
  type Column,
} from "./components/common";

export function WarehousesPage() {
  const query = useQuery({
    queryKey: ["warehouses"],
    queryFn: ({ signal }) => api.warehouses(signal),
  });
  const columns: Column<Warehouse>[] = [
    {
      key: "id",
      title: "ID",
      value: (row) => row.id,
      render: (row) => <span className="muted">{row.id}</span>,
    },
    {
      key: "name",
      title: "Название",
      value: (row) => row.name,
      render: (row) => <strong className="table-emphasis">{row.name}</strong>,
    },
    {
      key: "address",
      title: "Адрес",
      value: (row) => row.address,
      render: (row) => row.address,
    },
  ];
  return (
    <>
      <PageHeader
        title="Склады"
        description="Список складов и их адреса."
        refresh={() => {
          void query.refetch();
        }}
        refreshing={query.isFetching}
      />
      <QueryState
        pending={query.isPending}
        error={query.error}
        retry={() => {
          void query.refetch();
        }}
      >
        <DataTable
          rows={query.data || []}
          columns={columns}
          searchText={(row) => `${row.id} ${row.name} ${row.address}`}
          label="Склады"
          emptyText="Склады пока не добавлены"
        />
      </QueryState>
    </>
  );
}
export function CellsPage() {
  const query = useInventory();
  const warehouseName = (row: Cell) =>
    query.data?.warehouses.find((warehouse) => warehouse.id === row.warehouseId)
      ?.name || `Склад № ${row.warehouseId}`;
  const columns: Column<Cell>[] = [
    {
      key: "id",
      title: "ID",
      value: (row) => row.id,
      render: (row) => <span className="muted">{row.id}</span>,
    },
    {
      key: "warehouse",
      title: "Склад",
      value: warehouseName,
      render: warehouseName,
    },
    {
      key: "number",
      title: "Номер",
      value: (row) => row.number,
      render: (row) => <strong className="table-emphasis">{row.number}</strong>,
    },
    {
      key: "size",
      title: "Размер",
      value: (row) => sizeLabels[row.size],
      render: (row) => sizeLabels[row.size],
    },
    {
      key: "status",
      title: "Статус",
      value: (row) => cellStatusLabels[row.status],
      render: (row) => (
        <Badge status={row.status} label={cellStatusLabels[row.status]} />
      ),
    },
    {
      key: "row",
      title: "Ряд",
      value: (row) => row.row,
      render: (row) => row.row,
    },
    {
      key: "column",
      title: "Колонка",
      value: (row) => row.column,
      render: (row) => row.column,
    },
  ];
  return (
    <>
      <PageHeader
        title="Все ячейки"
        description="Ячейки складов, их размеры и текущий статус."
        refresh={() => {
          void query.refetch();
        }}
        refreshing={query.isFetching}
      />
      <QueryState
        pending={query.isPending}
        error={query.error}
        retry={() => {
          void query.refetch();
        }}
      >
        <DataTable
          rows={query.data?.cells || []}
          columns={columns}
          searchText={(row) =>
            `${row.id} ${row.number} ${warehouseName(row)} ${sizeLabels[row.size]} ${cellStatusLabels[row.status]}`
          }
          label="Ячейки"
          emptyText="Ячейки пока не добавлены"
        />
      </QueryState>
    </>
  );
}
export function StatisticsPage() {
  const inventory = useInventory();
  const rentals = useAllRentals();
  const rows = countCellRentals(
    inventory.data?.cells || [],
    rentals.data || [],
  );
  const refresh = () => {
    void inventory.refetch();
    void rentals.refetch();
  };
  const columns: Column<(typeof rows)[number]>[] = [
    {
      key: "number",
      title: "Ячейка",
      value: (row) => row.number,
      render: (row) => <strong className="table-emphasis">{row.number}</strong>,
    },
    {
      key: "size",
      title: "Размер",
      value: (row) => sizeLabels[row.size],
      render: (row) => sizeLabels[row.size],
    },
    {
      key: "rentalCount",
      title: "Количество аренд",
      value: (row) => row.rentalCount,
      render: (row) => (
        <strong className="stat-count">{row.rentalCount}</strong>
      ),
    },
  ];
  return (
    <>
      <PageHeader
        title="Статистика по ячейкам"
        description="Количество аренд за всё время, включая завершённые и отменённые."
        back="/admin"
        refresh={refresh}
        refreshing={inventory.isFetching || rentals.isFetching}
      />
      <QueryState
        pending={inventory.isPending || rentals.isPending}
        error={inventory.error || rentals.error}
        retry={refresh}
      >
        <div className="stats-summary">
          <div>
            <span>Всего ячеек</span>
            <strong>{rows.length}</strong>
          </div>
          <div>
            <span>Всего аренд</span>
            <strong>{rentals.data?.length || 0}</strong>
          </div>
          <div>
            <span>Ячеек с арендой</span>
            <strong>{rows.filter((row) => row.rentalCount > 0).length}</strong>
          </div>
        </div>
        <DataTable
          rows={rows}
          columns={columns}
          searchText={(row) => `${row.number} ${sizeLabels[row.size]}`}
          label="Статистика по ячейкам"
          emptyText="Нет данных по ячейкам"
        />
      </QueryState>
    </>
  );
}
