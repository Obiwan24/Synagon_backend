package com.synagon.api.infraestructura.adaptadores.salida.persistencia;

import com.synagon.api.dominio.modelos.DatosEquipoDivision;
import com.synagon.api.dominio.modelos.DatosMiembroEquipo;
import com.synagon.api.dominio.modelos.DisponibilidadJugador;
import com.synagon.api.dominio.modelos.Enfrentamiento;
import com.synagon.api.dominio.modelos.PartidoPareja;
import com.synagon.api.dominio.modelos.PropuestaFecha;
import com.synagon.api.dominio.puertos.salida.RepositorioActasYEstadisticasPuerto;
import com.synagon.api.dominio.puertos.salida.RepositorioEnfrentamientoPuerto;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Adaptador de salida PostgreSQL que implementa {@link RepositorioEnfrentamientoPuerto}
 * y {@link RepositorioActasYEstadisticasPuerto}.
 */
@Component
public class RepositorioEnfrentamientoPostgres
        implements RepositorioEnfrentamientoPuerto, RepositorioActasYEstadisticasPuerto {

    private final RepositorioJpaEnfrentamiento repositorioJpaEnfrentamiento;
    private final RepositorioJpaPropuestaFecha repositorioJpaPropuestaFecha;
    private final RepositorioJpaDisponibilidadJugador repositorioJpaDisponibilidadJugador;
    private final RepositorioJpaPartidoEnfrentamiento repositorioJpaPartidoEnfrentamiento;
    private final MapeadorPersistenciaEnfrentamiento mapeador;
    private final EntityManager gestorEntidades;

    public RepositorioEnfrentamientoPostgres(
            RepositorioJpaEnfrentamiento repositorioJpaEnfrentamiento,
            RepositorioJpaPropuestaFecha repositorioJpaPropuestaFecha,
            RepositorioJpaDisponibilidadJugador repositorioJpaDisponibilidadJugador,
            RepositorioJpaPartidoEnfrentamiento repositorioJpaPartidoEnfrentamiento,
            MapeadorPersistenciaEnfrentamiento mapeador,
            EntityManager gestorEntidades
    ) {
        this.repositorioJpaEnfrentamiento = repositorioJpaEnfrentamiento;
        this.repositorioJpaPropuestaFecha = repositorioJpaPropuestaFecha;
        this.repositorioJpaDisponibilidadJugador = repositorioJpaDisponibilidadJugador;
        this.repositorioJpaPartidoEnfrentamiento = repositorioJpaPartidoEnfrentamiento;
        this.mapeador = mapeador;
        this.gestorEntidades = gestorEntidades;
    }

    @Override
    public Optional<Enfrentamiento> obtenerPorId(UUID enfrentamientoId) {
        return repositorioJpaEnfrentamiento.buscarPorIdConDetalles(enfrentamientoId)
                .map(mapeador::aDominio);
    }

    @Override
    public Enfrentamiento guardar(Enfrentamiento enfrentamiento) {
        OffsetDateTime ahora = OffsetDateTime.now();

        EntidadEnfrentamiento entidad = repositorioJpaEnfrentamiento
                .buscarPorIdConDetalles(enfrentamiento.obtenerId())
                .orElseGet(() -> mapeador.crearNuevaEntidadEnfrentamiento(enfrentamiento));

        entidad.actualizarDatosPrincipales(
                enfrentamiento.obtenerEstado(),
                mapeador.aOffsetDateTime(enfrentamiento.obtenerFechaDefinitiva()),
                enfrentamiento.obtenerPistas(),
                enfrentamiento.obtenerSedeAlternativaJornada(),
                enfrentamiento.obtenerUrlFotoActa(),
                enfrentamiento.obtenerPuntosLocal(),
                enfrentamiento.obtenerPuntosVisitante(),
                enfrentamiento.esActaConfirmada(),
                ahora
        );

        sincronizarDisponibilidadesEliminadas(entidad, enfrentamiento);
        sincronizarPropuestasFecha(entidad, enfrentamiento, ahora);
        sincronizarDisponibilidadesNuevasOActualizadas(entidad, enfrentamiento, ahora);
        sincronizarPartidos(entidad, enfrentamiento, ahora);

        EntidadEnfrentamiento guardada = repositorioJpaEnfrentamiento.saveAndFlush(entidad);
        return mapeador.aDominio(guardada);
    }

    @Override
    public boolean existeUsuarioPorId(UUID usuarioId) {
        if (usuarioId == null) {
            return false;
        }
        return repositorioJpaEnfrentamiento.existeUsuarioEnTabla(usuarioId);
    }

    @Override
    public boolean perteneceJugadorAAlgunEquipo(UUID jugadorId, UUID equipoLocalId, UUID equipoVisitanteId) {
        if (jugadorId == null || equipoLocalId == null || equipoVisitanteId == null) {
            return false;
        }
        return repositorioJpaEnfrentamiento.perteneceJugadorAAlgunEquipoActivo(
                jugadorId, equipoLocalId, equipoVisitanteId
        );
    }

    @Override
    public boolean esCapitanDeAlgunEquipoDelEnfrentamiento(
            UUID capitanId,
            UUID equipoLocalId,
            UUID equipoVisitanteId
    ) {
        if (capitanId == null || equipoLocalId == null || equipoVisitanteId == null) {
            return false;
        }
        return repositorioJpaEnfrentamiento.esCapitanActivoDeAlgunEquipo(
                capitanId, equipoLocalId, equipoVisitanteId
        );
    }

    @Override
    public boolean existeDivisionPorId(UUID divisionId) {
        if (divisionId == null) {
            return false;
        }
        return repositorioJpaEnfrentamiento.existeDivisionEnTabla(divisionId);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<DatosEquipoDivision> obtenerEquiposPorDivisionId(UUID divisionId) {
        List<Object[]> filas = gestorEntidades.createNativeQuery(
                        "SELECT id, nombre FROM equipos WHERE division_id = :divisionId ORDER BY nombre ASC"
                )
                .setParameter("divisionId", divisionId)
                .getResultList();

        return filas.stream()
                .map(fila -> new DatosEquipoDivision(
                        convertirAUuid(fila[0]),
                        fila[1].toString()
                ))
                .toList();
    }

    @Override
    public List<Enfrentamiento> obtenerEnfrentamientosConActaPorDivisionId(UUID divisionId) {
        return repositorioJpaEnfrentamiento.buscarConActaPorDivisionId(divisionId).stream()
                .map(mapeador::aDominio)
                .toList();
    }

    @Override
    public boolean existeEquipoPorId(UUID equipoId) {
        if (equipoId == null) {
            return false;
        }
        return repositorioJpaEnfrentamiento.existeEquipoEnTabla(equipoId);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<DatosMiembroEquipo> obtenerMiembrosPorEquipoId(UUID equipoId) {
        List<Object[]> filas = gestorEntidades.createNativeQuery("""
                        SELECT u.id, CONCAT(u.nombre, ' ', u.apellidos), d.temporada_id
                        FROM miembros_equipo me
                        JOIN usuarios u ON u.id = me.usuario_id
                        JOIN equipos eq ON eq.id = me.equipo_id
                        JOIN divisiones d ON d.id = eq.division_id
                        WHERE me.equipo_id = :equipoId
                          AND me.activo = TRUE
                        ORDER BY me.es_capitan DESC, u.nombre ASC
                        """)
                .setParameter("equipoId", equipoId)
                .getResultList();

        return filas.stream()
                .map(fila -> new DatosMiembroEquipo(
                        convertirAUuid(fila[0]),
                        fila[1].toString(),
                        convertirAUuid(fila[2])
                ))
                .toList();
    }

    @Override
    public List<Enfrentamiento> obtenerEnfrentamientosConActaPorEquipoId(UUID equipoId) {
        return repositorioJpaEnfrentamiento.buscarConActaPorEquipoId(equipoId).stream()
                .map(mapeador::aDominio)
                .toList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<UUID, Integer> obtenerPartidosApuntadosPorMiembrosDeEquipo(UUID equipoId) {
        List<Object[]> filas = gestorEntidades.createNativeQuery("""
                        SELECT dj.jugador_id, COUNT(DISTINCT dj.enfrentamiento_id)
                        FROM disponibilidades_jugador dj
                        JOIN enfrentamientos e ON e.id = dj.enfrentamiento_id
                        JOIN miembros_equipo me ON me.usuario_id = dj.jugador_id AND me.equipo_id = :equipoId
                        WHERE (e.equipo_local_id = :equipoId OR e.equipo_visitante_id = :equipoId)
                          AND dj.estado_disponibilidad = 'DISPONIBLE'
                          AND me.activo = TRUE
                        GROUP BY dj.jugador_id
                        """)
                .setParameter("equipoId", equipoId)
                .getResultList();

        Map<UUID, Integer> resultado = new HashMap<>();
        for (Object[] fila : filas) {
            resultado.put(convertirAUuid(fila[0]), ((Number) fila[1]).intValue());
        }
        return resultado;
    }

    private UUID convertirAUuid(Object valor) {
        if (valor instanceof UUID uuid) {
            return uuid;
        }
        return UUID.fromString(valor.toString());
    }

    private void sincronizarDisponibilidadesEliminadas(
            EntidadEnfrentamiento entidad,
            Enfrentamiento dominio
    ) {
        Set<UUID> idsDominio = dominio.obtenerDisponibilidades().stream()
                .map(DisponibilidadJugador::obtenerId)
                .collect(Collectors.toSet());

        boolean eliminadas = entidad.obtenerDisponibilidades()
                .removeIf(disp -> !idsDominio.contains(disp.obtenerId()));

        if (eliminadas && gestorEntidades.contains(entidad)) {
            gestorEntidades.flush();
        }
    }

    private void sincronizarPropuestasFecha(
            EntidadEnfrentamiento entidad,
            Enfrentamiento dominio,
            OffsetDateTime ahora
    ) {
        Set<UUID> idsDominio = dominio.obtenerPropuestasFecha().stream()
                .map(PropuestaFecha::obtenerId)
                .collect(Collectors.toSet());

        boolean eliminadas = entidad.obtenerPropuestasFecha()
                .removeIf(prop -> !idsDominio.contains(prop.obtenerId()));

        if (eliminadas && gestorEntidades.contains(entidad)) {
            gestorEntidades.flush();
        }

        Map<UUID, EntidadPropuestaFecha> existentesPorId = entidad.obtenerPropuestasFecha().stream()
                .collect(Collectors.toMap(EntidadPropuestaFecha::obtenerId, Function.identity()));

        for (PropuestaFecha propuestaDominio : dominio.obtenerPropuestasFecha()) {
            EntidadPropuestaFecha existente = existentesPorId.get(propuestaDominio.obtenerId());
            if (existente != null) {
                existente.actualizar(
                        mapeador.aOffsetDateTime(propuestaDominio.obtenerFechaPropuesta()),
                        propuestaDominio.obtenerOrden(),
                        propuestaDominio.esSeleccionada(),
                        ahora
                );
            } else {
                entidad.obtenerPropuestasFecha().add(
                        mapeador.aEntidadPropuestaFecha(propuestaDominio, entidad)
                );
            }
        }

        if (gestorEntidades.contains(entidad)) {
            gestorEntidades.flush();
        }
    }

    private void sincronizarDisponibilidadesNuevasOActualizadas(
            EntidadEnfrentamiento entidad,
            Enfrentamiento dominio,
            OffsetDateTime ahora
    ) {
        Map<UUID, EntidadDisponibilidadJugador> existentesPorId = entidad.obtenerDisponibilidades().stream()
                .collect(Collectors.toMap(EntidadDisponibilidadJugador::obtenerId, Function.identity()));

        for (DisponibilidadJugador dispDominio : dominio.obtenerDisponibilidades()) {
            EntidadDisponibilidadJugador existente = existentesPorId.get(dispDominio.obtenerId());
            if (existente != null) {
                existente.actualizar(
                        dispDominio.obtenerPropuestaFechaId(),
                        dispDominio.obtenerEstado(),
                        dispDominio.esDisponibilidadFinal(),
                        ahora
                );
            } else {
                entidad.obtenerDisponibilidades().add(
                        mapeador.aEntidadDisponibilidad(dispDominio, entidad)
                );
            }
        }
    }

    private void sincronizarPartidos(
            EntidadEnfrentamiento entidad,
            Enfrentamiento dominio,
            OffsetDateTime ahora
    ) {
        Set<UUID> idsDominio = dominio.obtenerPartidos().stream()
                .map(PartidoPareja::obtenerId)
                .collect(Collectors.toSet());

        boolean eliminados = entidad.obtenerPartidos()
                .removeIf(partido -> !idsDominio.contains(partido.obtenerId()));

        if (eliminados && gestorEntidades.contains(entidad)) {
            gestorEntidades.flush();
        }

        Map<UUID, EntidadPartidoEnfrentamiento> existentesPorId = entidad.obtenerPartidos().stream()
                .collect(Collectors.toMap(EntidadPartidoEnfrentamiento::obtenerId, Function.identity()));

        for (PartidoPareja partidoDominio : dominio.obtenerPartidos()) {
            EntidadPartidoEnfrentamiento existente = existentesPorId.get(partidoDominio.obtenerId());
            if (existente != null) {
                existente.actualizar(
                        partidoDominio.obtenerNumeroPareja(),
                        partidoDominio.obtenerJugadorLocal1Id(),
                        partidoDominio.obtenerJugadorLocal2Id(),
                        partidoDominio.obtenerJugadorVisitante1Id(),
                        partidoDominio.obtenerJugadorVisitante2Id(),
                        partidoDominio.obtenerSet1Local(),
                        partidoDominio.obtenerSet1Visitante(),
                        partidoDominio.obtenerSet2Local(),
                        partidoDominio.obtenerSet2Visitante(),
                        partidoDominio.obtenerSet3Local(),
                        partidoDominio.obtenerSet3Visitante(),
                        partidoDominio.obtenerParejaGanadora(),
                        partidoDominio.estaCompletado(),
                        ahora
                );
            } else {
                entidad.obtenerPartidos().add(
                        mapeador.aEntidadPartido(partidoDominio, entidad)
                );
            }
        }
    }
}
