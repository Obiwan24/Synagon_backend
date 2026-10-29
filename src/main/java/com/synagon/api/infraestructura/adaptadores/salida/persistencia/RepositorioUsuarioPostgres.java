package com.synagon.api.infraestructura.adaptadores.salida.persistencia;

import com.synagon.api.dominio.modelos.Usuario;
import com.synagon.api.dominio.puertos.salida.RepositorioUsuarioPuerto;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador de salida PostgreSQL que implementa {@link RepositorioUsuarioPuerto}
 * para persistir usuarios y su alta en {@code miembros_equipo}.
 */
@Component
public class RepositorioUsuarioPostgres implements RepositorioUsuarioPuerto {

    /**
     * Identificador del equipo semilla principal ("Real Club Pádel Triana") utilizado por defecto en pruebas.
     */
    public static final UUID EQUIPO_PRUEBAS_POR_DEFECTO_ID =
            UUID.fromString("50000000-0000-0000-0000-000000000001");

    private final RepositorioJpaUsuario repositorioJpaUsuario;
    private final RepositorioJpaMiembroEquipo repositorioJpaMiembroEquipo;
    private final MapeadorPersistenciaUsuario mapeador;
    private final EntityManager gestorEntidades;

    public RepositorioUsuarioPostgres(
            RepositorioJpaUsuario repositorioJpaUsuario,
            RepositorioJpaMiembroEquipo repositorioJpaMiembroEquipo,
            MapeadorPersistenciaUsuario mapeador,
            EntityManager gestorEntidades
    ) {
        this.repositorioJpaUsuario = repositorioJpaUsuario;
        this.repositorioJpaMiembroEquipo = repositorioJpaMiembroEquipo;
        this.mapeador = mapeador;
        this.gestorEntidades = gestorEntidades;
    }

    @Override
    public boolean existePorEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        return repositorioJpaUsuario.existePorEmailIgnorandoMayusculas(email.trim());
    }

    @Override
    @SuppressWarnings("unchecked")
    public Optional<UUID> obtenerEquipoPorDefectoId() {
        List<Object> equipoSemilla = gestorEntidades.createNativeQuery(
                        "SELECT id FROM equipos WHERE id = :idSemilla"
                )
                .setParameter("idSemilla", EQUIPO_PRUEBAS_POR_DEFECTO_ID)
                .getResultList();

        if (!equipoSemilla.isEmpty()) {
            return Optional.of(convertirAUuid(equipoSemilla.get(0)));
        }

        List<Object> primerEquipo = gestorEntidades.createNativeQuery(
                        "SELECT id FROM equipos ORDER BY creado_en ASC, id ASC LIMIT 1"
                )
                .getResultList();

        if (primerEquipo.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(convertirAUuid(primerEquipo.get(0)));
    }

    @Override
    public Usuario guardarConMembresiaEquipo(Usuario usuario, UUID equipoId) {
        OffsetDateTime ahora = OffsetDateTime.now();

        EntidadUsuario entidadUsuario = mapeador.aEntidad(usuario, ahora);
        EntidadUsuario usuarioGuardado = repositorioJpaUsuario.saveAndFlush(entidadUsuario);

        EntidadMiembroEquipo entidadMiembro = new EntidadMiembroEquipo(
                UUID.randomUUID(),
                equipoId,
                usuarioGuardado.obtenerId(),
                false,
                true,
                ahora,
                ahora
        );
        repositorioJpaMiembroEquipo.saveAndFlush(entidadMiembro);

        return mapeador.aDominio(usuarioGuardado, equipoId);
    }

    private UUID convertirAUuid(Object valor) {
        if (valor instanceof UUID uuid) {
            return uuid;
        }
        return UUID.fromString(valor.toString());
    }
}
