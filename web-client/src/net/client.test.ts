import { afterEach, describe, expect, it, vi } from "vitest";
import { api, ApiError, request } from "./client";
import { session } from "./session";
import { countCellRentals, type Cell, type Rental } from "../model/models";

afterEach(() => {
  session.clear();
  vi.unstubAllGlobals();
});
const json = (value: unknown, status = 200) =>
  new Response(JSON.stringify(value), { status });

describe("API compatibility with the unchanged backend", () => {
  it("normalizes the backend role, warehouse address and rental date fields", async () => {
    const fetch = vi
      .fn()
      .mockResolvedValueOnce(
        json({ id: 2, login: "client", Role: "CLIENT", status: "ACTIVE" }),
      )
      .mockResolvedValueOnce(json([{ id: 3, name: "Склад", addr: "Адрес" }]))
      .mockResolvedValueOnce(
        json([
          {
            id: 4,
            cellId: 7,
            startRent: "2026-11-24T10:00:00",
            endRent: "2026-11-24T11:00:00",
            status: "ACTIVE",
          },
        ]),
      );
    vi.stubGlobal("fetch", fetch);
    expect((await api.me()).role).toBe("CLIENT");
    expect((await api.warehouses())[0].address).toBe("Адрес");
    expect((await api.myRentals())[0].startDateTime).toBe(
      "2026-11-24T10:00:00",
    );
  });
  it.each([404, 405])(
    "retries the duplicated administrator route on HTTP %i",
    async (status) => {
      const fetch = vi
        .fn()
        .mockResolvedValueOnce(json({}, status))
        .mockResolvedValueOnce(json([]));
      vi.stubGlobal("fetch", fetch);
      expect(await api.rentals()).toEqual([]);
      expect(fetch.mock.calls.map((args) => args[0])).toEqual([
        "/api/v1/rentals",
        "/api/v1/rentals/api/v1/rentals",
      ]);
    },
  );
  it("does not retry another endpoint after access denial", async () => {
    const fetch = vi.fn().mockResolvedValue(json({}, 403));
    vi.stubGlobal("fetch", fetch);
    await expect(api.rentals()).rejects.toMatchObject({ status: 403 });
    expect(fetch).toHaveBeenCalledTimes(1);
  });
  it("passes both dates to the currently supported availability branch", async () => {
    const fetch = vi.fn().mockResolvedValue(json([]));
    vi.stubGlobal("fetch", fetch);
    await api.availableCells();
    const url = new URL(fetch.mock.calls[0][0], "http://localhost");
    const start = url.searchParams.get("start"),
      end = url.searchParams.get("end");
    expect(start).toMatch(/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}$/);
    expect(new Date(end!).getTime() - new Date(start!).getTime()).toBe(
      3_600_000,
    );
  });
  it("counts every rental status and includes cells with no rentals", () => {
    const cells = [{ id: 1 }, { id: 2 }] as Cell[];
    const rentals = [
      { cellId: 1, status: "CANCELLED" },
      { cellId: 1, status: "ACTIVE" },
    ] as Rental[];
    expect(
      countCellRentals(cells, rentals).map((row) => row.rentalCount),
    ).toEqual([2, 0]);
  });
});
describe("session and error handling", () => {
  it("sends bearer credentials only on authenticated requests", async () => {
    session.save("private-token", 60);
    const fetch = vi.fn().mockImplementation(() => Promise.resolve(json({})));
    vi.stubGlobal("fetch", fetch);
    await request("/users/me");
    await api.login("client", "secret");
    expect(fetch.mock.calls[0][1].headers.get("Authorization")).toBe(
      "Bearer private-token",
    );
    expect(fetch.mock.calls[1][1].headers.has("Authorization")).toBe(false);
  });
  it("clears an expired session without making a request", async () => {
    session.save("expired", -1);
    const fetch = vi.fn();
    vi.stubGlobal("fetch", fetch);
    await expect(request("/users/me")).rejects.toMatchObject({ status: 401 });
    expect(session.get()).toBeNull();
    expect(fetch).not.toHaveBeenCalled();
  });
  it("clears the session on a protected 401", async () => {
    session.save("expired-on-server", 60);
    vi.stubGlobal("fetch", vi.fn().mockResolvedValue(json({}, 401)));
    await expect(request("/users/me")).rejects.toBeInstanceOf(ApiError);
    expect(session.get()).toBeNull();
  });
  it("does not clear a new session when a request from the previous session returns 401", async () => {
    session.save("old", 60);
    vi.stubGlobal(
      "fetch",
      vi.fn().mockImplementation(async () => {
        session.save("new", 60);
        return json({}, 401);
      }),
    );
    await expect(request("/users/me")).rejects.toMatchObject({ status: 401 });
    expect(session.get()?.token).toBe("new");
  });
  it("hides Java errors and handles an HTML gateway failure", async () => {
    vi.stubGlobal(
      "fetch",
      vi
        .fn()
        .mockResolvedValueOnce(
          json({ message: "java.sql.SQLException: secret" }, 500),
        )
        .mockResolvedValueOnce(
          new Response("<h1>Bad gateway</h1>", { status: 502 }),
        ),
    );
    await expect(request("/cells")).rejects.toThrow(
      "Не удалось выполнить запрос",
    );
    await expect(request("/cells")).rejects.toThrow(
      "Не удалось выполнить запрос",
    );
  });
});
