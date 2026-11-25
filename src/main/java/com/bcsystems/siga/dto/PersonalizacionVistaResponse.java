package com.bcsystems.siga.dto;

public record PersonalizacionVistaResponse(
        Integer idVista,
        String tipo,
        String url,
        Integer orden
) {}
