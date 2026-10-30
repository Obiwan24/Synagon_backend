package com.synagon.api.infraestructura.adaptadores.salida.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio Spring Data JPA para la entidad {@link EntidadMiembroEquipo}.
 */
public interface RepositorioJpaMiembroEquipo extends JpaRepository<EntidadMiembroEquipo, UUID> {

    List<EntidadMiembroEquipo> findByUsuarioId(UUID usuarioId);

    Optional<EntidadMiembroEquipo> findByEquipoIdAndUsuarioId(UUID equipoId, UUID usuarioId);
}
