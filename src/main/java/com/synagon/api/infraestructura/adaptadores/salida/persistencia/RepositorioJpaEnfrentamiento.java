package com.synagon.api.infraestructura.adaptadores.salida.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio Spring Data JPA para la entidad {@link EntidadEnfrentamiento}.
 */
@Repository
public interface RepositorioJpaEnfrentamiento extends JpaRepository<EntidadEnfrentamiento, UUID> {

    @Query("""
            SELECT DISTINCT e FROM EntidadEnfrentamiento e
            LEFT JOIN FETCH e.propuestasFecha
            LEFT JOIN FETCH e.disponibilidades
            LEFT JOIN FETCH e.partidos
            WHERE e.id = :enfrentamientoId
            """)
    Optional<EntidadEnfrentamiento> buscarPorIdConDetalles(@Param("enfrentamientoId") UUID enfrentamientoId);

    @Query("""
            SELECT DISTINCT e FROM EntidadEnfrentamiento e
            LEFT JOIN FETCH e.partidos
            WHERE e.divisionId = :divisionId
              AND (e.actaConfirmada = TRUE
                   OR e.estado IN (
                       com.synagon.api.dominio.modelos.EstadoEnfrentamiento.ACTA_SUBIDA,
                       com.synagon.api.dominio.modelos.EstadoEnfrentamiento.FINALIZADO_CONFIRMADO,
                       com.synagon.api.dominio.modelos.EstadoEnfrentamiento.FINALIZADO
                   ))
            """)
    List<EntidadEnfrentamiento> buscarConActaPorDivisionId(@Param("divisionId") UUID divisionId);

    @Query("""
            SELECT DISTINCT e FROM EntidadEnfrentamiento e
            LEFT JOIN FETCH e.partidos
            WHERE (e.equipoLocalId = :equipoId OR e.equipoVisitanteId = :equipoId)
              AND (e.actaConfirmada = TRUE
                   OR e.estado IN (
                       com.synagon.api.dominio.modelos.EstadoEnfrentamiento.ACTA_SUBIDA,
                       com.synagon.api.dominio.modelos.EstadoEnfrentamiento.FINALIZADO_CONFIRMADO,
                       com.synagon.api.dominio.modelos.EstadoEnfrentamiento.FINALIZADO
                   ))
            """)
    List<EntidadEnfrentamiento> buscarConActaPorEquipoId(@Param("equipoId") UUID equipoId);

    @Query(value = "SELECT EXISTS(SELECT 1 FROM usuarios WHERE id = :usuarioId)", nativeQuery = true)
    boolean existeUsuarioEnTabla(@Param("usuarioId") UUID usuarioId);

    @Query(value = "SELECT EXISTS(SELECT 1 FROM divisiones WHERE id = :divisionId)", nativeQuery = true)
    boolean existeDivisionEnTabla(@Param("divisionId") UUID divisionId);

    @Query(value = "SELECT EXISTS(SELECT 1 FROM equipos WHERE id = :equipoId)", nativeQuery = true)
    boolean existeEquipoEnTabla(@Param("equipoId") UUID equipoId);

    @Query(value = """
            SELECT EXISTS(
                SELECT 1 FROM miembros_equipo
                WHERE usuario_id = :jugadorId
                  AND equipo_id IN (:equipoLocalId, :equipoVisitanteId)
                  AND activo = TRUE
            )
            """, nativeQuery = true)
    boolean perteneceJugadorAAlgunEquipoActivo(
            @Param("jugadorId") UUID jugadorId,
            @Param("equipoLocalId") UUID equipoLocalId,
            @Param("equipoVisitanteId") UUID equipoVisitanteId
    );

    @Query(value = """
            SELECT EXISTS(
                SELECT 1 FROM equipos eq
                WHERE eq.id IN (:equipoLocalId, :equipoVisitanteId)
                  AND eq.capitan_id = :capitanId
                UNION ALL
                SELECT 1 FROM miembros_equipo me
                WHERE me.equipo_id IN (:equipoLocalId, :equipoVisitanteId)
                  AND me.usuario_id = :capitanId
                  AND me.es_capitan = TRUE
                  AND me.activo = TRUE
            )
            """, nativeQuery = true)
    boolean esCapitanActivoDeAlgunEquipo(
            @Param("capitanId") UUID capitanId,
            @Param("equipoLocalId") UUID equipoLocalId,
            @Param("equipoVisitanteId") UUID equipoVisitanteId
    );
}
