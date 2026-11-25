package com.bcsystems.siga.dto;

import jakarta.validation.constraints.NotBlank;

public record PersonalizacionVistaRequest(
        @NotBlank String tipo,
        @NotBlank String imagen
) {}
