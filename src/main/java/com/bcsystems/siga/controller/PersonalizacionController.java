package com.bcsystems.siga.controller;

import com.bcsystems.siga.dto.PersonalizacionRequest;
import com.bcsystems.siga.dto.PersonalizacionResponse;
import com.bcsystems.siga.service.PersonalizacionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/personalizaciones")
public class PersonalizacionController {

    private final PersonalizacionService personalizacionService;

    public PersonalizacionController(PersonalizacionService personalizacionService) {
        this.personalizacionService = personalizacionService;
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('DISENOS_CREAR','VENTAS_CREAR')")
    public ResponseEntity<PersonalizacionResponse> crear(@Valid @RequestBody PersonalizacionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(personalizacionService.crear(request));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('DISENOS_VER')")
    public ResponseEntity<List<PersonalizacionResponse>> listar(
            @RequestParam(required = false) String estado) {
        return ResponseEntity.ok(personalizacionService.listar(estado));
    }

    @GetMapping("/disponibles")
    @PreAuthorize("hasAnyAuthority('DISENOS_VER','VENTAS_CREAR')")
    public ResponseEntity<List<PersonalizacionResponse>> listarDisponibles() {
        return ResponseEntity.ok(personalizacionService.listarDisponibles());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('DISENOS_VER','VENTAS_CREAR')")
    public ResponseEntity<PersonalizacionResponse> obtener(@PathVariable Integer id) {
        return ResponseEntity.ok(personalizacionService.obtener(id));
    }

    @PostMapping("/{id}/enviar-pos")
    @PreAuthorize("hasAnyAuthority('DISENOS_EDITAR','VENTAS_CREAR')")
    public ResponseEntity<PersonalizacionResponse> enviarAPos(@PathVariable Integer id) {
        return ResponseEntity.ok(personalizacionService.enviarAPos(id));
    }
}
