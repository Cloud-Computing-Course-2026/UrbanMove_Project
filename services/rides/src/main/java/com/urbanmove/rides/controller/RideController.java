package com.urbanmove.rides.controller;

import com.urbanmove.rides.dto.RideApi;
import com.urbanmove.rides.service.RideService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import java.net.URI;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rides")
@Validated
public class RideController {
    private final RideService service;

    public RideController(RideService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<RideApi.RideResponse> start(@Valid @RequestBody RideApi.StartRequest request,
            @RequestHeader("Idempotency-Key") @Pattern(regexp = "[A-Za-z0-9._:-]{1,128}") String key,
            @RequestAttribute("correlationId") String correlationId) {
        var ride = service.start(request, key, correlationId);
        return ResponseEntity.created(URI.create("/rides/" + ride.id())).body(ride);
    }

    @PostMapping("/{id}/finish")
    public RideApi.RideResponse finish(@PathVariable UUID id, @Valid @RequestBody RideApi.FinishRequest request,
            @RequestHeader("Idempotency-Key") @Pattern(regexp = "[A-Za-z0-9._:-]{1,128}") String key,
            @RequestAttribute("correlationId") String correlationId) {
        return service.finish(id, request, key, correlationId);
    }

    @GetMapping("/{id}")
    public RideApi.RideResponse get(@PathVariable UUID id) {
        return service.get(id);
    }
}
