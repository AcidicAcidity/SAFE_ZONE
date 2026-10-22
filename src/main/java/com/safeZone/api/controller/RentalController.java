package com.safezone.api.controller;

import com.safezone.api.dto.AdminRentalResponse;
import com.safezone.api.dto.RentalRequest;
import com.safezone.api.dto.RentalResponse;
import com.safezone.api.dto.RentalStatusRequest;
import com.safezone.application.service.SafeZoneService;
import com.safezone.domain.model.Rental;

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

/**
 * ОБРАЗЕЦ №2 + СКЕЛЕТ: аренды (Endpoint.md, раздел 6). Здесь появляются три новые идеи:
 *
 *   A. userId берётся ТОЛЬКО из токена: @AuthenticationPrincipal Integer userId.
 *      Никогда не из тела запроса и не из URL, иначе можно арендовать "за другого".
 *   B. Роль на уровне метода: @PreAuthorize("hasRole('CLIENT')").
 *   C. "Свой или админ": это проверка владельца внутри метода (см. getById и cancel).
 *
 * Перед началом напиши DTO:
 *   RentalResponse(id, cellId, startDateTime, endDateTime, status)            для клиента
 *   AdminRentalResponse(id, userId, cellId, startDateTime, endDateTime, status) для админа
 *   RentalStatusRequest(@NotNull RentalStatus status)
 *   RentalRequest(Integer cellId, CellSize size, @NotNull LocalDateTime startDateTime,
 *                 @Min(1) @Max(24) int hours)    (описан в прошлом ответе)
 */
@RestController
@RequestMapping("/api/v1/rentals")
public class RentalController {

    private final SafeZoneService service;

    public RentalController(SafeZoneService service) {
        this.service = service;
    }

    // ---------------------------------------------------------------------
    // ОБРАЗЕЦ (готово, разбери построчно): POST /api/v1/rentals
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
        // TODO: service.getUserRentals(userId) -> map(RentalResponse::from) -> toList()
        throw new UnsupportedOperationException("TODO");
    }

    // ---------------------------------------------------------------------
    // GET /api/v1/rentals/{rentalId} (CLIENT: только свою, ADMIN: любую)
    // ---------------------------------------------------------------------
    @GetMapping("/{rentalId}")
    public RentalResponse getById(
            @PathVariable int rentalId,
            @AuthenticationPrincipal Integer userId,
            Authentication auth) {                          // из него узнаём роль

        // TODO:
        //   1. Rental rental = service.findRentalById(rentalId); null -> NotFoundException
        //   2. Если НЕ админ И rental.getUserId() != userId -> throw new AccessDeniedException("...")
        //      (org.springframework.security.access.AccessDeniedException, даст 403).
        //      Альтернатива: отвечать 404, тогда клиент не узнает, что чужая аренда с таким id существует.
        //   3. return RentalResponse.from(rental)
        //
        // Ловушка: int != Integer безопасно (Java распаковывает). Но если getUserId() вернёт Integer
        // (объект), то != сравнит ссылки, а не значения. Тогда используй Objects.equals(a, b).
        throw new UnsupportedOperationException("TODO");
    }

    // ---------------------------------------------------------------------
    // PATCH /api/v1/rentals/{rentalId}/cancel (CLIENT, только свою)
    // ---------------------------------------------------------------------
    @PatchMapping("/{rentalId}/cancel")
    @PreAuthorize("hasRole('CLIENT')")
    public RentalResponse cancel(
            @PathVariable int rentalId,
            @AuthenticationPrincipal Integer userId) {

        // TODO:
        //   1. найти аренду (404, если нет)
        //   2. проверить владельца (403)         <- без этого любой клиент отменит чужую аренду!
        //   3. service.cancelRental(rentalId)    (бросит IllegalStateException, если не ACTIVE.
        //                                         Сделай там ConflictException, чтобы получилось 409)
        //   4. вернуть обновлённую: RentalResponse.from(service.findRentalById(rentalId))
        throw new UnsupportedOperationException("TODO");
    }

    // ---------------------------------------------------------------------
    // GET /api/v1/rentals (ADMIN)
    // ---------------------------------------------------------------------
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<AdminRentalResponse> getAll() {
        // TODO: service.getRentals() -> AdminRentalResponse::from.
        //       Позже: пагинация (?page=0&size=20) и фильтры (?status=ACTIVE).
        throw new UnsupportedOperationException("TODO");
    }

    // ---------------------------------------------------------------------
    // PATCH /api/v1/rentals/{rentalId}/status (ADMIN)   [Endpoint.md: ЧАСТИЧНО]
    // ---------------------------------------------------------------------
    @PatchMapping("/{rentalId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public AdminRentalResponse changeStatus(
            @PathVariable int rentalId,
            @Valid @RequestBody RentalStatusRequest req) {

        // TODO (сначала сервис):
        //   В SafeZoneService добавь changeRentalStatus(int rentalId, RentalStatus newStatus).
        //   Реши, какие переходы разрешены. Например: ACTIVE -> CANCELLED / EXPIRED можно,
        //   а из CANCELLED и EXPIRED выходить нельзя (ConflictException -> 409).
        //   Репозиторий уже умеет: rentalRepository.updateStatus(id, status).
        // Затем здесь: вызвать метод сервиса и вернуть AdminRentalResponse.from(...).
        throw new UnsupportedOperationException("TODO");
    }
}
