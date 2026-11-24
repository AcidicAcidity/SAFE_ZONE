export function dateTime(value: string) {
  if (!value) return "—";
  const date = new Date(value);
  return Number.isNaN(date.getTime())
    ? "—"
    : new Intl.DateTimeFormat("ru-RU", {
        day: "2-digit",
        month: "2-digit",
        year: "numeric",
        hour: "2-digit",
        minute: "2-digit",
      }).format(date);
}
export function errorMessage(error: unknown) {
  return error instanceof Error
    ? error.message
    : "Не удалось выполнить действие. Повторите попытку.";
}
