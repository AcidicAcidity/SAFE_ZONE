package com.safezone.backend.api.controller;

import com.safezone.backend.api.dto.admin.AdminRentalResponse;
import com.safezone.backend.api.dto.request.RentalRequest;
import com.safezone.backend.api.dto.request.RentalStatusRequest;
import com.safezone.backend.api.dto.response.RentalResponse;
import com.safezone.backend.application.service.RentalService;
import com.safezone.backend.domain.model.Rental;
import com.safezone.backend.domain.enums.RentalStatus;
import com.safezone.backend.api.exception.NotFoundException;
import com.safezone.backend.api.exception.AccessDeniedException;
import com.safezone.backend.api.exception.ConflictException;
import com.safezone.backend.api.exception.DataBaseErrorException;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/api/v1/rentals")
public class RentalController {

    private final RentalService service;

    public RentalController(RentalService service) {
        this.service = service;
    }

    // ---------------------------------------------------------------------
    //  POST /api/v1/rentals
    // ---------------------------------------------------------------------
    @PostMapping
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<RentalResponse> create(
            @Valid @RequestBody RentalRequest req,          // @Valid запускает проверки @NotNull/@Min/@Max из DTO
            @AuthenticationPrincipal Integer userId) {      // id из JWT (его положил JwtAuthFilter)

        Rental rental;

        if (req.cellId() != null) {
            rental = service.rentSpecificCell(userId, req.cellId(), req.startDateTime(), req.hours());
        } else if (req.size() != null) {
            rental = service.rentCellBySize(userId, req.size(), req.startDateTime(), req.hours());
        } else {
            throw new IllegalArgumentException("Укажите cellId или size.");   // -> 400
        }

        return ResponseEntity.status(HttpStatus.CREATED)                      // 201, а не 200
                .body(RentalResponse.from(rental));
    }

    // ---------------------------------------------------------------------
    // GET /api/v1/rentals/me (CLIENT)
    // ---------------------------------------------------------------------
    @GetMapping("/me")
    @PreAuthorize("hasRole('CLIENT')")
    public List<RentalResponse> mine(@AuthenticationPrincipal Integer userId) {
        if (userId != null) {
            try {
                return service.getUserRentals(userId).stream()
                    .map(RentalResponse::from)
                    .toList();
            } catch (IllegalStateException e) {
                throw new NotFoundException("Не удалось найти ячейки");
            }
        } else {
            throw new IllegalArgumentException("No UserId");
        }
    }

    // ---------------------------------------------------------------------
    // GET /api/v1/rentals/{rentalId} (CLIENT: только свою, ADMIN: любую)
    // ---------------------------------------------------------------------
    @GetMapping("/{rentalId}")
    public RentalResponse getById(
            @PathVariable int rentalId,
            @AuthenticationPrincipal Integer userId,
            Authentication auth) {                          // из него узнаём роль

        Rental rental = service.findRentalById(rentalId);

        if (!isAdmin(auth) && rental.getUserId() != userId) {
            throw new AccessDeniedException("Вы не являетесь админом");
        }

        if (isAdmin(auth) == true) {
            return RentalResponse.from(rental);
        } else if (rental.getUserId() == userId) {
            return RentalResponse.from(rental);
        } else {
            throw new NotFoundException("Аренда не найдена" + rentalId);
        }

    }

    // ---------------------------------------------------------------------
    // PATCH /api/v1/rentals/{rentalId}/cancel (CLIENT, только свою)
    // ---------------------------------------------------------------------
    @PatchMapping("/{rentalId}/cancel")
    @PreAuthorize("hasRole('CLIENT')")
    public RentalResponse cancel(
            @PathVariable int rentalId,
            @AuthenticationPrincipal Integer userId) {

        Rental rental = service.findRentalById(rentalId);

        if (rental == null) {
            throw new NotFoundException("Аренда не найдена:" + rentalId);
        }

        if (rental.getUserId() != userId) {
            throw new ConflictException("Это не ваша ячейка" + rentalId);
        }

        try {
            service.cancelRental(rentalId);
        } catch(IllegalArgumentException e) {
            throw new DataBaseErrorException("Не удалось закрыть аренду ячейки");
        }

        return RentalResponse.from(rental);
    }

    // ---------------------------------------------------------------------
    // GET /api/v1/rentals (ADMIN)
    // ---------------------------------------------------------------------
    @GetMapping("/api/v1/rentals")
    @PreAuthorize("hasRole('ADMIN')")
    public List<AdminRentalResponse> getAll() {

        try {
            return service.getRentals().stream()
                .map(AdminRentalResponse::from)
                .toList();
        } catch (IllegalStateException e) {
            throw new DataBaseErrorException("Не удалось получить список аренд");
        }
        //TODO: Позже - пагинация (?page=0&size=20) и фильтры (?status=ACTIVE).
    }

    // ---------------------------------------------------------------------
    // PATCH /api/v1/rentals/{rentalId}/status (ADMIN)   [Endpoint.md: ЧАСТИЧНО]
    // ---------------------------------------------------------------------
    @PatchMapping("/{rentalId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public AdminRentalResponse changeStatus(
            @PathVariable int rentalId,
            @Valid @RequestBody RentalStatusRequest req) {

            RentalStatus status = req.status();

            if (status ==  null) {
                throw new IllegalStateException("Не обнаружен статус в запросе");
            }

            try {
                Rental changedStatus = service.changeRentalStatus(rentalId, status);
                return AdminRentalResponse.from(changedStatus);
            } catch (IllegalStateException e) {
                throw new DataBaseErrorException("Не удалось изменить статус аренды");
            }
    }

    private boolean isAdmin(Authentication auth) {
        return auth.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}
