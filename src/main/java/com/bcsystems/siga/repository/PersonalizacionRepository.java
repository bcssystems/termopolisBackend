package com.bcsystems.siga.repository;

import com.bcsystems.siga.domain.Personalizacion;
import com.bcsystems.siga.domain.en.EstadoPersonalizacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface PersonalizacionRepository extends JpaRepository<Personalizacion, Integer> {
    boolean existsByFolio(String folio);
    List<Personalizacion> findAllByOrderByFechaCreacionDesc();
    List<Personalizacion> findByEstadoInOrderByFechaCreacionDesc(Collection<EstadoPersonalizacion> estados);
}
