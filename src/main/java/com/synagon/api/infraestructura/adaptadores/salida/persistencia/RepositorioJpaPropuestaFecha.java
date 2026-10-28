package com.synagon.api.infraestructura.adaptadores.salida.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repositorio Spring Data JPA para la entidad {@link EntidadPropuestaFecha}.
 */
@Repository
public interface RepositorioJpaPropuestaFecha extends JpaRepository<EntidadPropuestaFecha, UUID> {

    @Query("SELECT p FROM EntidadPropuestaFecha p WHERE p.enfrentamiento.id = :enfrentamientoId ORDER BY p.orden ASC")
    List<EntidadPropuestaFecha> buscarPorEnfrentamientoId(@Param("enfrentamientoId") UUID enfrentamientoId);
}
