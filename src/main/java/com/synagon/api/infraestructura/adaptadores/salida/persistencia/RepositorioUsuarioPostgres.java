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
 * para persistir usuarios y gestionar su vinculación en {@code miembros_equipo}.
 */
@Component
public class RepositorioUsuarioPostgres implements RepositorioUsuarioPuerto {

    /**
     * Identificador del equipo semilla principal utilizado por defecto en pruebas de registro público.
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
    public Optional<Usuario> obtenerPorId(UUID usuarioId) {
        if (usuarioId == null) {
            return Optional.empty();
        }
        return repositorioJpaUsuario.findById(usuarioId)
                .map(entidad -> mapeador.aDominio(entidad, obtenerPrimerEquipoActivoDeUsuario(usuarioId)));
    }

    @Override
    public Optional<Usuario> buscarPorEmail(String email) {
        if (email == null || email.isBlank()) {
            return Optional.empty();
        }
        return repositorioJpaUsuario.buscarPorEmailIgnorandoMayusculas(email.trim())
                .map(entidad -> mapeador.aDominio(entidad, obtenerPrimerEquipoActivoDeUsuario(entidad.obtenerId())));
    }

    @Override
    public boolean existePorDniExcluyendoUsuario(String dni, UUID usuarioExcluidoId) {
        if (dni == null || dni.isBlank()) {
            return false;
        }
        return repositorioJpaUsuario.existePorDniExcluyendoUsuario(dni.trim(), usuarioExcluidoId);
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
    public Usuario guardar(Usuario usuario) {
        OffsetDateTime ahora = OffsetDateTime.now();

        EntidadUsuario entidad = repositorioJpaUsuario.findById(usuario.obtenerId())
                .map(existente -> {
                    mapeador.actualizarEntidadExistente(existente, usuario, ahora);
                    return existente;
                })
                .orElseGet(() -> mapeador.aEntidad(usuario, ahora));

        EntidadUsuario guardado = repositorioJpaUsuario.saveAndFlush(entidad);
        UUID equipoAsociado = usuario.obtenerEquipoId() != null
                ? usuario.obtenerEquipoId()
                : obtenerPrimerEquipoActivoDeUsuario(guardado.obtenerId());
        return mapeador.aDominio(guardado, equipoAsociado);
    }

    @Override
    public Usuario guardarConMembresiaEquipo(Usuario usuario, UUID equipoId) {
        OffsetDateTime ahora = OffsetDateTime.now();

        EntidadUsuario entidadUsuario = mapeador.aEntidad(usuario, ahora);
        EntidadUsuario usuarioGuardado = repositorioJpaUsuario.saveAndFlush(entidadUsuario);

        vincularEnMiembrosEquipo(usuarioGuardado.obtenerId(), equipoId, ahora);

        return mapeador.aDominio(usuarioGuardado, equipoId);
    }

    @Override
    public boolean existeEquipoPorId(UUID equipoId) {
        if (equipoId == null) {
            return false;
        }
        Object resultado = gestorEntidades.createNativeQuery(
                        "SELECT EXISTS(SELECT 1 FROM equipos WHERE id = :equipoId)"
                )
                .setParameter("equipoId", equipoId)
                .getSingleResult();
        return Boolean.TRUE.equals(resultado);
    }

    @Override
    public boolean esCapitanActivoDeEquipo(UUID usuarioId, UUID equipoId) {
        if (usuarioId == null || equipoId == null) {
            return false;
        }
        Object resultado = gestorEntidades.createNativeQuery("""
                        SELECT EXISTS(
                            SELECT 1 FROM equipos eq
                            WHERE eq.id = :equipoId AND eq.capitan_id = :usuarioId
                            UNION ALL
                            SELECT 1 FROM miembros_equipo me
                            WHERE me.equipo_id = :equipoId
                              AND me.usuario_id = :usuarioId
                              AND me.es_capitan = TRUE
                              AND me.activo = TRUE
                        )
                        """)
                .setParameter("equipoId", equipoId)
                .setParameter("usuarioId", usuarioId)
                .getSingleResult();
        return Boolean.TRUE.equals(resultado);
    }

    @Override
    public boolean esMiembroActivoDeEquipo(UUID usuarioId, UUID equipoId) {
        if (usuarioId == null || equipoId == null) {
            return false;
        }
        return repositorioJpaMiembroEquipo.findByEquipoIdAndUsuarioId(equipoId, usuarioId)
                .map(EntidadMiembroEquipo::estaActivo)
                .orElse(false);
    }

    @Override
    public void vincularJugadorAEquipo(UUID usuarioId, UUID equipoId) {
        vincularEnMiembrosEquipo(usuarioId, equipoId, OffsetDateTime.now());
    }

    private void vincularEnMiembrosEquipo(UUID usuarioId, UUID equipoId, OffsetDateTime ahora) {
        Optional<EntidadMiembroEquipo> existente = repositorioJpaMiembroEquipo
                .findByEquipoIdAndUsuarioId(equipoId, usuarioId);

        if (existente.isPresent()) {
            EntidadMiembroEquipo miembro = existente.get();
            miembro.reactivarMembresia(false, ahora);
            repositorioJpaMiembroEquipo.saveAndFlush(miembro);
            return;
        }

        EntidadMiembroEquipo entidadMiembro = new EntidadMiembroEquipo(
                UUID.randomUUID(),
                equipoId,
                usuarioId,
                false,
                true,
                ahora,
                ahora
        );
        repositorioJpaMiembroEquipo.saveAndFlush(entidadMiembro);
    }

    private UUID obtenerPrimerEquipoActivoDeUsuario(UUID usuarioId) {
        return repositorioJpaMiembroEquipo.findByUsuarioId(usuarioId).stream()
                .filter(EntidadMiembroEquipo::estaActivo)
                .map(EntidadMiembroEquipo::obtenerEquipoId)
                .findFirst()
                .orElse(null);
    }

    private UUID convertirAUuid(Object valor) {
        if (valor instanceof UUID uuid) {
            return uuid;
        }
        return UUID.fromString(valor.toString());
    }
}
