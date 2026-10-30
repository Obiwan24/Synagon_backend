package com.synagon.api.infraestructura.adaptadores.salida.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

/**
 * Repositorio Spring Data JPA para la entidad {@link EntidadEquipo}.
 */
public interface RepositorioJpaEquipo extends JpaRepository<EntidadEquipo, UUID> {

    @Query("""
            SELECT CASE WHEN COUNT(eq) > 0 THEN TRUE ELSE FALSE END
            FROM EntidadEquipo eq
            WHERE eq.divisionId = :divisionId
              AND LOWER(eq.nombre) = LOWER(:nombreEquipo)
            """)
    boolean existePorDivisionIdYNombreIgnorandoMayusculas(
            @Param("divisionId") UUID divisionId,
            @Param("nombreEquipo") String nombreEquipo
    );
}
