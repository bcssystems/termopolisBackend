package com.bcsystems.siga.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record PersonalizacionResponse(
        Integer idPersonalizacion,
        String folio,
        Integer idProducto,
        String productoSku,
        String productoNombre,
        Integer idCliente,
        String clienteNombre,
        String estado,
        String modelo3dUrl,
        String colorNombre,
        String colorHex,
        String capasJson,
        Double precioUnitario,
        BigDecimal cantidad,
        LocalDate fechaEntrega,
        String nota,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaActualizacion,
        List<PersonalizacionVistaResponse> vistas
) {}
