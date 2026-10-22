package com.safezone.api.controller;

import com.safezone.api.dto.CellResponse;
import com.safezone.api.dto.CellRequest;
import com.safezone.application.service.CellService;
import com.safezone.application.service.WarehouseService;
import com.safezone.domain.model.Cell;
import com.safezone.domain.model.Warehouse;
import com.safezone.api.exception.NotFoundException;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class CellController {

    private final CellService cellService;
    private final WarehouseService warehouseService;

    public CellController(CellService cellService, WarehouseService warehouseService) {
        this.cellService = cellService;
        this.warehouseService = warehouseService;
    }

    // GET /api/v1/cells
    @GetMapping("/cells")
    public List<CellResponse> getAll() throws SQLException {
        return cellService.getCells().stream()
            .map(CellResponse::from)
            .toList();
    }

    @GetMapping("/cells/available")
    public List<CellResponse> getAvailable(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {


        if (start != null && end != null ) {
            return cellService.getAvailableCells().stream()
                .map(CellResponse::from)
                .toList();
        } else if (start == null && end == null) {
            return cellService.getAvailableCells(start, end).stream()
                .map(CellResponse::from)
                .toList();
        } else if ( end.isBefore(start)) {
            throw new IllegalArgumentException();
        } else {
            throw new IllegalArgumentException("Укажите start и end вместе.");
        }
    }

    // GET /api/v1/cells/12
    @GetMapping("/cells/{cellId}")
    public CellResponse getById(@PathVariable int cellId) {
        Cell cell = cellService.findCellById(cellId);
        if (cell == null) {
            throw new NotFoundException("Cell" + cellId + "not found");
        }
        return CellResponse.from(cell);
    }

    // GET /api/v1/warehouses/5/cells
    @GetMapping("/warehouses/{warehouseId}/cells")
    public List<CellResponse> getByWarehouse(@PathVariable int warehouseId) {
        Warehouse w = warehouseService.getWarehouseById(warehouseId);
        if (w == null) {
            throw new NotFoundException("Warehouse" + warehouseId + "not found.");
        } else {
            return cellService.findByWarehouseId(warehouseId).stream()
                .map(CellResponse::from)
                .toList();
        }
    }



    @PostMapping("/warehouses/{warehouseId}/cells")
    @PreAuthorize("hasRole('ADMIN')")
    public boolean createCell(@PathVariable int warehouseId,
        @Valid @RequestBody CellRequest req) {
        Warehouse w = warehouseService.getWarehouseById(warehouseId);
        if (w == null) {
            throw new NotFoundException("Warehouse " + warehouseId + "not found");
        } else {
            int c = cellService.createCell(warehouseId, req.row(), req.column(), req.size());
            if (c == -1) {
                return true;
            } else {
                return false;
            }
        }
    }

    // =====================================================================
    // ЗАДАНИЯ СО ЗВЁЗДОЧКОЙ (Endpoint.md: НЕ РЕАЛИЗОВАНО, все ADMIN).
    // Цепочка та же: репозиторий -> сервис -> контроллер.
    //
    // POST   /warehouses/{warehouseId}/cells   создать ячейку (201). Проверить, что склад существует.
    // PATCH  /cells/{cellId}                   изменить параметры (частичное обновление, поля nullable)
    // PATCH  /cells/{cellId}/status            тело {"status":"UNAVAILABLE"} -> enum CellStatus.
    //                                          Подумай: можно ли поставить UNAVAILABLE, пока идёт активная аренда?
    // DELETE /cells/{cellId}                   у ячейки есть аренды -> 409 (есть rentalRepository.countByCellId)
    //
    // Над каждым: @PreAuthorize("hasRole('ADMIN')")
    // =====================================================================
}
