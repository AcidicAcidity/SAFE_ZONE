import { useState, type FormEvent } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { sizeLabels, type CellSize } from "../model/models";
import { useAuth } from "../net/auth";
import { api, localDateTime } from "../net/client";
import { capabilities } from "../net/capabilities";
import { errorMessage } from "../util/format";
import { Icon } from "./components/Icon";
import { Notice, PageHeader, QueryState } from "./components/common";

export function RentPage() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const client = useQueryClient();
  const available = useQuery({
    queryKey: ["available-cells"],
    queryFn: ({ signal }) => api.availableCells(signal),
  });
  const [mode, setMode] = useState<"cell" | "size">("cell");
  const [cellId, setCellId] = useState("");
  const [size, setSize] = useState<CellSize | "">("");
  const [hours, setHours] = useState(1);
  const [error, setError] = useState("");
  const selected = available.data?.find((cell) => cell.id === Number(cellId));
  const canCreate = capabilities.createRental && user?.role === "CLIENT";
  const mutation = useMutation({
    mutationFn: () =>
      api.createRental({
        ...(mode === "cell"
          ? { cellId: Number(cellId) }
          : { size: size as CellSize }),
        startDateTime: localDateTime(),
        hours,
      }),
    onSuccess: async () => {
      await Promise.all([
        client.invalidateQueries({ queryKey: ["rentals"] }),
        client.invalidateQueries({ queryKey: ["inventory"] }),
        client.invalidateQueries({ queryKey: ["available-cells"] }),
      ]);
      navigate("/rentals");
    },
  });
  function submit(event: FormEvent) {
    event.preventDefault();
    if (!canCreate || mutation.isPending) return;
    if (mode === "cell" && !selected) {
      setError("Выберите доступную ячейку.");
      return;
    }
    if (mode === "size" && !size) {
      setError("Выберите размер ячейки.");
      return;
    }
    if (!Number.isInteger(hours) || hours < 1 || hours > 24) {
      setError("Продолжительность аренды — от 1 до 24 часов.");
      return;
    }
    setError("");
    mutation.mutate();
  }
  return (
    <>
      <PageHeader
        title="Арендовать ячейку"
        description="Выберите подходящую ячейку и срок хранения."
        refresh={() => {
          void available.refetch();
        }}
        refreshing={available.isFetching}
      />
      <div className="rent-layout">
        <section className="surface rent-form">
          <div className="surface-heading">
            <span className="card-icon">
              <Icon name="key" />
            </span>
            <div>
              <h2>Параметры аренды</h2>
              <p>Хранение на срок от 1 до 24 часов</p>
            </div>
          </div>
          {!canCreate && (
            <Notice>
              {user?.role === "ADMIN"
                ? "Оформление аренды доступно для клиентских аккаунтов."
                : "Оформление новых аренд пока недоступно. Вы можете просматривать ячейки и свои бронирования."}
            </Notice>
          )}
          <QueryState
            pending={available.isPending}
            error={available.error}
            retry={() => {
              void available.refetch();
            }}
          >
            <form onSubmit={submit}>
              <fieldset disabled={mutation.isPending}>
                <legend className="field-legend">Способ выбора</legend>
                <div className="rental-mode">
                  <label>
                    <input
                      type="radio"
                      name="mode"
                      checked={mode === "cell"}
                      onChange={() => {
                        setMode("cell");
                        setError("");
                      }}
                    />
                    Конкретная ячейка
                  </label>
                  <label>
                    <input
                      type="radio"
                      name="mode"
                      checked={mode === "size"}
                      onChange={() => {
                        setMode("size");
                        setError("");
                      }}
                    />
                    По размеру
                  </label>
                </div>
                {mode === "cell" ? (
                  <div className="field">
                    <label htmlFor="rent-cell">
                      Ячейка <span className="required">*</span>
                    </label>
                    <select
                      id="rent-cell"
                      value={cellId}
                      onChange={(event) => setCellId(event.target.value)}
                      required
                    >
                      <option value="">
                        {available.data?.length
                          ? "Выберите ячейку"
                          : "Свободных ячеек нет"}
                      </option>
                      {available.data?.map((cell) => (
                        <option key={cell.id} value={cell.id}>
                          {cell.number} · {sizeLabels[cell.size]} · Склад №{" "}
                          {cell.warehouseId}
                        </option>
                      ))}
                    </select>
                  </div>
                ) : (
                  <div className="field">
                    <label htmlFor="rent-size">
                      Размер <span className="required">*</span>
                    </label>
                    <select
                      id="rent-size"
                      value={size}
                      onChange={(event) =>
                        setSize(event.target.value as CellSize)
                      }
                      required
                    >
                      <option value="">Выберите размер</option>
                      {Object.entries(sizeLabels).map(([value, label]) => (
                        <option key={value} value={value}>
                          {label}
                        </option>
                      ))}
                    </select>
                  </div>
                )}
                <div className="field hours-field">
                  <label htmlFor="rent-hours">
                    Продолжительность, часы <span className="required">*</span>
                  </label>
                  <input
                    id="rent-hours"
                    type="number"
                    min={1}
                    max={24}
                    step={1}
                    value={Number.isNaN(hours) ? "" : hours}
                    onChange={(event) => setHours(event.target.valueAsNumber)}
                    required
                  />
                  <small>От 1 до 24 часов</small>
                </div>
                <p className="rent-start">
                  <Icon name="calendar" />
                  Начало аренды — при оформлении
                </p>
                {(error || mutation.error) && (
                  <Notice tone="error">
                    {error || errorMessage(mutation.error)}
                  </Notice>
                )}
                <button
                  className="button primary-button"
                  disabled={
                    !canCreate ||
                    mutation.isPending ||
                    (mode === "cell" && !selected) ||
                    (mode === "size" && !size)
                  }
                >
                  {mutation.isPending ? "Оформляем…" : "Арендовать"}
                </button>
              </fieldset>
            </form>
          </QueryState>
        </section>
        <aside className="surface rental-summary">
          <h2>Ваш выбор</h2>
          <div className="summary-icon">
            <Icon name="box" />
          </div>
          <strong>
            {mode === "cell"
              ? selected?.number || "Ячейка не выбрана"
              : size
                ? `${sizeLabels[size]} ячейка`
                : "Размер не выбран"}
          </strong>
          <p>
            {mode === "cell" && selected
              ? `Склад № ${selected.warehouseId} · ${sizeLabels[selected.size]}`
              : "Выберите параметры слева"}
          </p>
          <dl>
            <div>
              <dt>Срок аренды</dt>
              <dd>{Number.isFinite(hours) ? hours : "—"} ч.</dd>
            </div>
            <div>
              <dt>Начало</dt>
              <dd>При оформлении</dd>
            </div>
          </dl>
          <Link className="text-link" to="/rentals">
            Открыть мои аренды
          </Link>
        </aside>
      </div>
    </>
  );
}
