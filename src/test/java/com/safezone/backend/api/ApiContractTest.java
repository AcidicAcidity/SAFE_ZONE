package com.safezone.backend.api;

import com.safezone.backend.api.config.SecurityConfig;
import com.safezone.backend.api.controller.*;
import com.safezone.backend.api.security.JwtService;
import com.safezone.backend.application.exception.AuthenticationException;
import com.safezone.backend.application.exception.UserAlreadyExistsException;
import com.safezone.backend.application.service.*;
import com.safezone.backend.domain.enums.*;
import com.safezone.backend.domain.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {AuthController.class, UserController.class, RentalController.class, CellController.class})
@Import({SecurityConfig.class, JwtService.class})
class ApiContractTest {
    @Autowired MockMvc mvc;
    @Autowired JwtService jwt;
    @MockBean AuthService auth;
    @MockBean UserService users;
    @MockBean RentalService rentals;
    @MockBean CellService cells;
    @MockBean WarehouseService warehouses;

    private final User client = new User(2, "client", "hash", UserStatus.ACTIVE, Role.CLIENT);
    private final User admin = new User(1, "admin", "hash", UserStatus.ACTIVE, Role.ADMIN);
    private final LocalDateTime start = LocalDateTime.of(2026, 11, 24, 12, 0);

    @BeforeEach void setup() {
        when(users.findUserById(1)).thenReturn(admin);
        when(users.findUserById(2)).thenReturn(client);
    }

    @Test void registrationAndLoginReturnUsableBearerToken() throws Exception {
        when(auth.register("client", "secret")).thenReturn(client);
        mvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content("{\"login\":\"client\",\"password\":\"secret\"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.id").value(2));
        when(auth.login("client", "secret")).thenReturn(client);
        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"login\":\"client\",\"password\":\"secret\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.accessToken").isNotEmpty());
        mvc.perform(get("/api/v1/auth/me").header("Authorization", bearer(client)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.login").value("client"));
    }

    @Test void badPasswordAndDuplicateLoginHaveUsefulStatusCodes() throws Exception {
        when(auth.login(anyString(), anyString())).thenThrow(new AuthenticationException("Неверный логин или пароль."));
        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"login\":\"client\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.message").value("Неверный логин или пароль."));
        when(auth.register(anyString(), anyString())).thenThrow(new UserAlreadyExistsException("Логин занят."));
        mvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content("{\"login\":\"client\",\"password\":\"secret\"}"))
                .andExpect(status().isConflict());
    }

    @Test void rentalDateAndEnumValidationAcceptValidRequests() throws Exception {
        Rental rental = new Rental(10, 2, 3, start, start.plusHours(2), RentalStatus.ACTIVE);
        when(rentals.rentSpecificCell(eq(2), eq(3), any(), eq(2))).thenReturn(rental);
        mvc.perform(post("/api/v1/rentals").header("Authorization", bearer(client))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"cellId\":3,\"startDateTime\":\"2026-11-24T12:00:00\",\"hours\":2}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.id").value(10));
        mvc.perform(patch("/api/v1/users/2/status").header("Authorization", bearer(admin))
                .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"BLOCKED\"}"))
                .andExpect(status().isOk());
        verify(users).blockUser(2);
        when(rentals.findRentalById(10)).thenReturn(rental);
        when(rentals.changeRentalStatus(10, RentalStatus.CANCELLED)).thenReturn(
                new Rental(10, 2, 3, start, start.plusHours(2), RentalStatus.CANCELLED));
        mvc.perform(patch("/api/v1/rentals/10/status").header("Authorization", bearer(admin))
                .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"CANCELLED\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test void invalidRentalAndNullStatusReturn400RatherThan500() throws Exception {
        mvc.perform(post("/api/v1/rentals").header("Authorization", bearer(client))
                .contentType(MediaType.APPLICATION_JSON).content("{\"cellId\":3,\"hours\":0}"))
                .andExpect(status().isBadRequest());
        mvc.perform(patch("/api/v1/users/2/status").header("Authorization", bearer(admin))
                .contentType(MediaType.APPLICATION_JSON).content("{\"status\":null}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(rentals);
    }

    @Test void administratorListSupportsStandardAndLegacyPathsAndRejectsClient() throws Exception {
        when(rentals.getRentals()).thenReturn(List.of());
        for (String path : List.of("/api/v1/rentals", "/api/v1/rentals/api/v1/rentals")) {
            mvc.perform(get(path).header("Authorization", bearer(admin))).andExpect(status().isOk());
        }
        mvc.perform(get("/api/v1/rentals").header("Authorization", bearer(client))).andExpect(status().isForbidden());
    }

    @Test void blockedAccountCannotKeepUsingAnExistingJwt() throws Exception {
        String token = bearer(client);
        client.setStatus(UserStatus.BLOCKED);
        mvc.perform(get("/api/v1/auth/me").header("Authorization", token)).andExpect(status().isUnauthorized());
    }

    @Test void availabilityHandlesMissingPartialAndValidIntervals() throws Exception {
        when(cells.getAvailableCells()).thenReturn(List.of());
        when(cells.getAvailableCells(any(), any())).thenReturn(List.of());
        mvc.perform(get("/api/v1/cells/available")).andExpect(status().isOk());
        verify(cells).getAvailableCells();
        mvc.perform(get("/api/v1/cells/available").param("start", "2026-11-24T12:00:00"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/v1/cells/available").param("start", "2026-11-24T12:00:00")
                .param("end", "2026-11-24T14:00:00")).andExpect(status().isOk());
        verify(cells).getAvailableCells(start, start.plusHours(2));
        mvc.perform(get("/api/v1/cells/available").param("start", "2026-11-24T12:00:00")
                .param("end", "2026-11-24T11:00:00")).andExpect(status().isBadRequest());
    }

    @Test void missingRentalReturns404InsteadOfNullPointerFailure() throws Exception {
        mvc.perform(get("/api/v1/rentals/999").header("Authorization", bearer(client)))
                .andExpect(status().isNotFound());
    }

    @Test void cancellationReturnsTheUpdatedStatus() throws Exception {
        Rental active = new Rental(10, 2, 3, start, start.plusHours(2), RentalStatus.ACTIVE);
        Rental cancelled = new Rental(10, 2, 3, start, start.plusHours(2), RentalStatus.CANCELLED);
        when(rentals.findRentalById(10)).thenReturn(active, cancelled);
        mvc.perform(patch("/api/v1/rentals/10/cancel").header("Authorization", bearer(client)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    private String bearer(User user) { return "Bearer " + jwt.generate(user.getUserId(), user.getRole().name()); }
}
