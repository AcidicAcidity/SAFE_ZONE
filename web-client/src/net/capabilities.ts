// Compose enables these operations for the current backend.
// Explicit flags also support connecting this client to older, read-only API versions.
export const capabilities = {
  createRental: import.meta.env.VITE_ENABLE_RENTAL_CREATION === "true",
  changeUserStatus: import.meta.env.VITE_ENABLE_USER_STATUS_UPDATES === "true",
  changeRentalStatus:
    import.meta.env.VITE_ENABLE_RENTAL_STATUS_UPDATES === "true",
};
