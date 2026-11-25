package com.bcsystems.siga.service;

import com.bcsystems.siga.domain.Personalizacion;
import com.bcsystems.siga.dto.PersonalizacionRequest;
import com.bcsystems.siga.dto.PersonalizacionResponse;

import java.util.List;

public interface PersonalizacionService {
    PersonalizacionResponse crear(PersonalizacionRequest request);
    PersonalizacionResponse obtener(Integer id);
    List<PersonalizacionResponse> listar(String estado);
    List<PersonalizacionResponse> listarDisponibles();
    PersonalizacionResponse enviarAPos(Integer id);
    Personalizacion obtenerParaVenta(Integer id);
    void marcarVendida(Personalizacion personalizacion);
    void marcarDisponible(Personalizacion personalizacion);
}
