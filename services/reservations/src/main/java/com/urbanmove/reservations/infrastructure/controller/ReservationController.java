package com.urbanmove.reservations.infrastructure.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping({"/api/v1/reservations", "/reservations"})
@Tag(name = "Reservas (8106)", description = "Microservicio de Reservas - Esqueleto base de endpoints")
public class ReservationController {

    @PostMapping
    @Operation(summary = "Crear nueva reserva (Esqueleto)")
    public ResponseEntity<Map<String, String>> createReservation(@RequestBody(required = false) Map<String, Object> request) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                .body(Map.of("message", "Endpoint no implementado aún en el esqueleto base"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar reserva por ID (Esqueleto)")
    public ResponseEntity<Map<String, String>> getReservationById(@PathVariable("id") Long id) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                .body(Map.of("message", "Endpoint no implementado aún en el esqueleto base"));
    }

    @PostMapping("/{id}/confirm")
    @Operation(summary = "Confirmar reserva al iniciar viaje (Esqueleto)")
    public ResponseEntity<Map<String, String>> confirmReservation(@PathVariable("id") Long id) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                .body(Map.of("message", "Endpoint no implementado aún en el esqueleto base"));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancelar reserva (Esqueleto)")
    public ResponseEntity<Map<String, String>> cancelReservation(@PathVariable("id") Long id) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                .body(Map.of("message", "Endpoint no implementado aún en el esqueleto base"));
    }

    @PostMapping("/{id}/expire")
    @Operation(summary = "Expirar reserva por timeout (Esqueleto)")
    public ResponseEntity<Map<String, String>> expireReservation(@PathVariable("id") Long id) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                .body(Map.of("message", "Endpoint no implementado aún en el esqueleto base"));
    }
}
