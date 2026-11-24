import { useQuery } from "@tanstack/react-query";
import { api } from "./client";

export function useInventory() {
  return useQuery({
    queryKey: ["inventory"],
    queryFn: async ({ signal }) => {
      const [warehouses, cells] = await Promise.all([
        api.warehouses(signal),
        api.cells(signal),
      ]);
      return { warehouses, cells };
    },
  });
}
export function useAllRentals(enabled = true) {
  return useQuery({
    queryKey: ["rentals", "all"],
    queryFn: ({ signal }) => api.rentals(signal),
    enabled,
  });
}
