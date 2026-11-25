package com.bcsystems.siga.repository;

import com.bcsystems.siga.domain.PersonalizacionVista;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PersonalizacionVistaRepository extends JpaRepository<PersonalizacionVista, Integer> {
}
