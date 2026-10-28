package com.synagon.api.infraestructura.adaptadores.salida.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio Spring Data JPA para la entidad {@link EntidadDisponibilidadJugador}.
 */
@Repository
public interface RepositorioJpaDisponibilidadJugador extends JpaRepository<EntidadDisponibilidadJugador, UUID> {

    @Query("SELECT d FROM EntidadDisponibilidadJugador d WHERE d.enfrentamiento.id = :enfrentamientoId ORDER BY d.creadoEn ASC")
    List<EntidadDisponibilidadJugador> buscarPorEnfrentamientoId(@Param("enfrentamientoId") UUID enfrentamientoId);

    @Query("""
            SELECT d FROM EntidadDisponibilidadJugador d
            WHERE d.enfrentamiento.id = :enfrentamientoId
              AND d.jugadorId = :jugadorId
              AND d.propuestaFechaId = :propuestaFechaId
            """)
    Optional<EntidadDisponibilidadJugador> buscarVotoJugadorPorPropuesta(
            @Param("enfrentamientoId") UUID enfrentamientoId,
            @Param("jugadorId") UUID jugadorId,
            @Param("propuestaFechaId") UUID propuestaFechaId
    );
}
