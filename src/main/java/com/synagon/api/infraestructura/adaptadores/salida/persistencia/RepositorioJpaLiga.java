package com.synagon.api.infraestructura.adaptadores.salida.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio Spring Data JPA para la entidad {@link EntidadLiga}.
 */
public interface RepositorioJpaLiga extends JpaRepository<EntidadLiga, UUID> {

    @Query("""
            SELECT l FROM EntidadLiga l
            WHERE UPPER(l.codigoInvitacion) = UPPER(:codigoInvitacion)
            """)
    Optional<EntidadLiga> buscarPorCodigoInvitacionIgnorandoMayusculas(
            @Param("codigoInvitacion") String codigoInvitacion
    );

    List<EntidadLiga> findByPresidenteId(UUID presidenteId);
}
