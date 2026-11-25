package com.bcsystems.siga.dto;

public record ProductoColorResponse(
        Integer idColor,
        String nombre,
        String hex,
        Boolean activo,
        Integer orden
) {}
