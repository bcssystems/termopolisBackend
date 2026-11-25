package com.bcsystems.siga.dto;

public record ProductoColorRequest(
        String nombre,
        String hex,
        Boolean activo,
        Integer orden
) {}
