import { test, expect, type Page } from "@playwright/test";

const cells = [
  {
    id: 1,
    warehouseId: 1,
    row: 1,
    column: 1,
    number: "S-1-1",
    size: "SMALL",
    status: "AVAILABLE",
  },
  {
    id: 2,
    warehouseId: 1,
    row: 2,
    column: 1,
    number: "M-1-1",
    size: "MEDIUM",
    status: "RENTED",
  },
  {
    id: 3,
    warehouseId: 1,
    row: 4,
    column: 1,
    number: "L-1-1",
    size: "LARGE",
    status: "AVAILABLE",
  },
];
const warehouse = { id: 1, name: "Склад Владимир", addr: "ул. Мира, 12" };
const rental = {
  id: 10,
  cellId: 2,
  startRent: "2026-11-24T10:00:00",
  endRent: "2026-11-24T15:00:00",
  status: "ACTIVE",
};
async function backend(page: Page, role = "CLIENT") {
  const user = {
    id: 1,
    login: role === "ADMIN" ? "admin" : "client",
    Role: role,
    status: "ACTIVE",
  };
  let cancelled = false;
  await page.route("**/api/v1/**", async (route) => {
    const url = new URL(route.request().url());
    const path = url.pathname.replace("/api/v1", "");
    let data: unknown;
    if (path === "/auth/login")
      data = {
        accessToken: `test-${role}`,
        tokenType: "Bearer",
        expiresInSeconds: 1800,
      };
    else if (path === "/auth/register") data = user;
    else if (path === "/auth/me") data = user;
    else if (path === "/warehouses") data = [warehouse];
    else if (path === "/cells") data = cells;
    else if (path === "/cells/available") {
      expect(
        url.searchParams.has("start") && url.searchParams.has("end"),
      ).toBeTruthy();
      data = cells.filter((cell) => cell.status === "AVAILABLE");
    } else if (path === "/users")
      data = [user, { id: 2, login: "anna", Role: "CLIENT", status: "ACTIVE" }];
    else if (path === "/rentals")
      return route.fulfill({ status: 405, json: {} });
    else if (path === "/rentals/api/v1/rentals")
      data = [
        {
          ...rental,
          userId: 2,
          startDateTime: rental.startRent,
          endDateTime: rental.endRent,
        },
      ];
    else if (path === "/rentals/me")
      data = [{ ...rental, status: cancelled ? "CANCELLED" : "ACTIVE" }];
    else if (path === "/rentals/10/cancel") {
      cancelled = true;
      data = rental;
    } else
      return route.fulfill({ status: 404, json: { message: "Not found" } });
    return route.fulfill({ json: data });
  });
}
async function login(page: Page, role = "CLIENT") {
  await backend(page, role);
  await page.goto("/");
  await page
    .getByLabel("Логин", { exact: false })
    .fill(role === "ADMIN" ? "admin" : "client");
  await page
    .getByLabel("Пароль", { exact: false })
    .first()
    .fill("test-password");
  await page.getByRole("button", { name: "Войти", exact: true }).click();
  await expect(
    page.getByRole("heading", { name: "Главная", exact: true }),
  ).toBeVisible();
}

test("client: navigation, normalized API fields, search, persisted session and logout", async ({
  page,
}) => {
  const errors: string[] = [];
  page.on("pageerror", (error) => errors.push(error.message));
  await login(page);
  await expect(
    page.getByRole("link", { name: /Панель администратора/ }),
  ).toHaveCount(0);
  await page.getByRole("link", { name: /Склады Адреса/ }).click();
  await expect(page.getByRole("cell", { name: "ул. Мира, 12" })).toBeVisible();
  await page.reload();
  await expect(
    page.getByRole("heading", { name: "Склады", exact: true }),
  ).toBeVisible();
  await page.goto("/cells");
  await page.getByRole("searchbox").fill("средняя");
  await expect(
    page.getByRole("cell", { name: "M-1-1", exact: true }),
  ).toBeVisible();
  await expect(
    page.getByRole("cell", { name: "S-1-1", exact: true }),
  ).toHaveCount(0);
  await page.goto("/rent");
  await expect(
    page.getByText("Оформление новых аренд пока недоступно.", { exact: false }),
  ).toBeVisible();
  await page
    .getByRole("combobox", { name: "Ячейка", exact: false })
    .selectOption("1");
  await expect(
    page.getByRole("button", { name: "Арендовать", exact: true }),
  ).toBeDisabled();
  await page.getByRole("radio", { name: "По размеру" }).check();
  await page
    .getByRole("combobox", { name: "Размер", exact: false })
    .selectOption("MEDIUM");
  await page.goto("/admin/users");
  await expect(
    page.getByRole("heading", { name: "Недостаточно прав" }),
  ).toBeVisible();
  await page.getByRole("button", { name: /client/ }).click();
  await page.getByRole("button", { name: "Выйти из аккаунта" }).click();
  await expect(
    page.getByRole("heading", { name: "Вход в систему" }),
  ).toBeVisible();
  expect(
    await page.evaluate(() => sessionStorage.getItem("safezone.session.v1")),
  ).toBeNull();
  expect(errors).toEqual([]);
});
test("client can cancel an active rental after confirmation and refreshes stale API response", async ({
  page,
}) => {
  await login(page);
  await page.goto("/rentals");
  await expect(
    page.getByRole("cell", { name: "24.11.2026, 10:00" }),
  ).toBeVisible();
  await page.getByRole("radio", { name: "Выбрать аренду № 10" }).check();
  await page
    .getByRole("button", { name: "Отменить бронь", exact: true })
    .click();
  await expect(page.getByRole("dialog")).toBeVisible();
  await page
    .getByRole("dialog")
    .getByRole("button", { name: "Отменить бронь", exact: true })
    .click();
  await expect(page.getByText("Бронирование отменено.")).toBeVisible();
  await expect(
    page.getByRole("cell", { name: "Отменена", exact: true }),
  ).toBeVisible();
});
test("admin: legacy rental path, statistics including zero rentals, users and disabled mutations", async ({
  page,
}) => {
  await login(page, "ADMIN");
  await page.getByRole("link", { name: /Панель администратора/ }).click();
  await page.getByRole("link", { name: /Статистика по ячейкам/ }).click();
  await expect(
    page.getByRole("cell", { name: "M-1-1", exact: true }),
  ).toBeVisible();
  await expect(
    page.getByRole("row").filter({ hasText: "M-1-1" }).getByRole("cell").last(),
  ).toHaveText("1");
  await expect(
    page.getByRole("row").filter({ hasText: "L-1-1" }).getByRole("cell").last(),
  ).toHaveText("0");
  await page.goto("/admin/users");
  await page.getByRole("radio", { name: "Выбрать пользователя anna" }).check();
  await expect(
    page.getByRole("button", { name: "Заблокировать", exact: true }),
  ).toBeDisabled();
  await page.goto("/admin/rentals");
  await expect(
    page.getByRole("cell", { name: "#10", exact: true }),
  ).toBeVisible();
  await page.getByRole("radio", { name: "Выбрать аренду № 10" }).check();
  await expect(
    page.getByRole("button", { name: "Отменить бронь", exact: true }),
  ).toBeDisabled();
});
test("registration validates confirmation and returns to login without retaining passwords", async ({
  page,
}) => {
  await backend(page);
  await page.goto("/register");
  await page.getByLabel("Логин", { exact: false }).fill("new-client");
  await page.locator("#password").fill("password");
  await page.locator("#confirmation").fill("different");
  await page
    .getByRole("button", { name: "Зарегистрироваться", exact: true })
    .click();
  await expect(page.getByRole("alert")).toHaveText("Пароли не совпадают.");
  await page.locator("#confirmation").fill("password");
  await page
    .getByRole("button", { name: "Зарегистрироваться", exact: true })
    .click();
  await expect(
    page.getByText("Регистрация завершена. Войдите в аккаунт."),
  ).toBeVisible();
  await expect(page.locator("#login")).toHaveValue("new-client");
  await expect(page.locator("#password")).toHaveValue("");
});
test("protected 401 returns to login with a session message", async ({
  page,
}) => {
  await login(page);
  await page.route("**/api/v1/rentals/me", (route) =>
    route.fulfill({ status: 401, json: {} }),
  );
  await page.goto("/rentals");
  await expect(
    page.getByRole("heading", { name: "Вход в систему" }),
  ).toBeVisible();
  await expect(
    page.getByText("Сессия истекла. Войдите в аккаунт снова."),
  ).toBeVisible();
});
test("API outage shows a recoverable error, not an empty table", async ({
  page,
}) => {
  await login(page);
  await page.route("**/api/v1/warehouses", (route) =>
    route.fulfill({ status: 502, body: "<h1>Bad gateway</h1>" }),
  );
  await page.goto("/warehouses");
  await expect(page.getByRole("alert")).toBeVisible();
  await expect(
    page.getByRole("button", { name: "Повторить", exact: true }),
  ).toBeVisible();
  await page.unroute("**/api/v1/warehouses");
  await page.getByRole("button", { name: "Повторить", exact: true }).click();
  await expect(
    page.getByRole("cell", { name: warehouse.name, exact: true }),
  ).toBeVisible();
});
test("mobile layouts fit the viewport; wide tables scroll inside their own panel", async ({
  page,
}) => {
  await page.setViewportSize({ width: 390, height: 844 });
  await login(page);
  await expect(page.getByRole("link", { name: /Все ячейки/ })).toBeVisible();
  expect(
    await page.evaluate(
      () => document.documentElement.scrollWidth <= innerWidth,
    ),
  ).toBeTruthy();
  await page.goto("/cells");
  await expect(
    page.getByRole("cell", { name: "M-1-1", exact: true }),
  ).toBeVisible();
  expect(
    await page.evaluate(
      () => document.documentElement.scrollWidth <= innerWidth,
    ),
  ).toBeTruthy();
  await page.screenshot({
    path: "test-results/mobile-cells.png",
    fullPage: true,
  });
});
test("desktop screenshots", async ({ page }) => {
  await page.goto("/login");
  await expect(
    page.getByRole("heading", { name: "Вход в систему" }),
  ).toBeVisible();
  await page.screenshot({
    path: "test-results/desktop-login.png",
    fullPage: true,
  });
  await login(page, "ADMIN");
  await page.screenshot({
    path: "test-results/desktop-home.png",
    fullPage: true,
  });
  await page.goto("/admin/users");
  await expect(
    page.getByRole("cell", { name: "anna", exact: true }),
  ).toBeVisible();
  await page.screenshot({
    path: "test-results/desktop-users.png",
    fullPage: true,
  });
});
