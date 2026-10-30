package com.synagon.api.infraestructura.adaptadores.salida.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio Spring Data JPA para la entidad {@link EntidadUsuario}.
 */
public interface RepositorioJpaUsuario extends JpaRepository<EntidadUsuario, UUID> {

    @Query("""
            SELECT CASE WHEN COUNT(u) > 0 THEN TRUE ELSE FALSE END
            FROM EntidadUsuario u
            WHERE LOWER(u.email) = LOWER(:email)
            """)
    boolean existePorEmailIgnorandoMayusculas(@Param("email") String email);

    @Query("""
            SELECT u
            FROM EntidadUsuario u
            WHERE LOWER(u.email) = LOWER(:email)
            """)
    Optional<EntidadUsuario> buscarPorEmailIgnorandoMayusculas(@Param("email") String email);

    @Query("""
            SELECT CASE WHEN COUNT(u) > 0 THEN TRUE ELSE FALSE END
            FROM EntidadUsuario u
            WHERE u.dni IS NOT NULL
              AND UPPER(u.dni) = UPPER(:dni)
              AND (:usuarioExcluidoId IS NULL OR u.id <> :usuarioExcluidoId)
            """)
    boolean existePorDniExcluyendoUsuario(
            @Param("dni") String dni,
            @Param("usuarioExcluidoId") UUID usuarioExcluidoId
    );
}
