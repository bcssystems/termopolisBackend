package com.bcsystems.siga.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record PersonalizacionRequest(
        @NotNull Integer idProducto,
        Integer idCliente,
        String colorNombre,
        @NotBlank String colorHex,
        @NotBlank String capasJson,
        @NotNull @DecimalMin(value = "0.01") Double precioUnitario,
        @NotNull @DecimalMin(value = "0.01") BigDecimal cantidad,
        @NotNull LocalDate fechaEntrega,
        String nota,
        @NotEmpty @Valid List<PersonalizacionVistaRequest> vistas
) {}
