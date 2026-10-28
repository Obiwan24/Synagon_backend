package com.synagon.api.infraestructura.adaptadores.salida.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repositorio Spring Data JPA para la entidad {@link EntidadPartidoEnfrentamiento}.
 */
@Repository
public interface RepositorioJpaPartidoEnfrentamiento extends JpaRepository<EntidadPartidoEnfrentamiento, UUID> {

    @Query("SELECT p FROM EntidadPartidoEnfrentamiento p WHERE p.enfrentamiento.id = :enfrentamientoId ORDER BY p.numeroPareja ASC")
    List<EntidadPartidoEnfrentamiento> buscarPorEnfrentamientoId(@Param("enfrentamientoId") UUID enfrentamientoId);
}
