package com.synagon.api.infraestructura.adaptadores.salida.persistencia;

import com.synagon.api.dominio.modelos.Liga;
import com.synagon.api.dominio.modelos.ModoSedeLiga;
import com.synagon.api.dominio.modelos.ResumenMembresiaUsuario;
import com.synagon.api.dominio.modelos.RolMembresia;
import com.synagon.api.dominio.puertos.salida.RepositorioAgoraYLigasPuerto;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador de salida PostgreSQL para las consultas del Ágora Principal,
 * parametrización de ligas e inscripción de equipos mediante código de invitación.
 */
@Component
public class RepositorioAgoraYLigasPostgres implements RepositorioAgoraYLigasPuerto {

    private final RepositorioJpaLiga repositorioJpaLiga;
    private final RepositorioJpaEquipo repositorioJpaEquipo;
    private final RepositorioJpaMiembroEquipo repositorioJpaMiembroEquipo;
    private final RepositorioJpaUsuario repositorioJpaUsuario;
    private final EntityManager gestorEntidades;

    public RepositorioAgoraYLigasPostgres(
            RepositorioJpaLiga repositorioJpaLiga,
            RepositorioJpaEquipo repositorioJpaEquipo,
            RepositorioJpaMiembroEquipo repositorioJpaMiembroEquipo,
            RepositorioJpaUsuario repositorioJpaUsuario,
            EntityManager gestorEntidades
    ) {
        this.repositorioJpaLiga = repositorioJpaLiga;
        this.repositorioJpaEquipo = repositorioJpaEquipo;
        this.repositorioJpaMiembroEquipo = repositorioJpaMiembroEquipo;
        this.repositorioJpaUsuario = repositorioJpaUsuario;
        this.gestorEntidades = gestorEntidades;
    }

    @Override
    public boolean existeUsuarioPorId(UUID usuarioId) {
        if (usuarioId == null) {
            return false;
        }
        return repositorioJpaUsuario.existsById(usuarioId);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<ResumenMembresiaUsuario> obtenerMembresiasActivasDeUsuario(UUID usuarioId) {
        List<ResumenMembresiaUsuario> membresias = new ArrayList<>();

        // 1. Membresías como PRESIDENTE de liga
        List<EntidadLiga> ligasPresididas = repositorioJpaLiga.findByPresidenteId(usuarioId);
        for (EntidadLiga entidadLiga : ligasPresididas) {
            Liga liga = aDominioLiga(entidadLiga);
            membresias.add(new ResumenMembresiaUsuario(
                    liga.obtenerId(),
                    liga.obtenerNombre(),
                    null,
                    "Presidencia y dirección de liga",
                    liga.obtenerTextoSedeDetallePresidencia(),
                    liga.obtenerPartidosPorJornada(),
                    RolMembresia.PRESIDENTE
            ));
        }

        // 2. Membresías activas como CAPITAN o JUGADOR en equipos de temporadas activas
        List<Object[]> filasEquipos = gestorEntidades.createNativeQuery("""
                        SELECT l.id,
                               l.nombre,
                               l.partidos_por_jornada,
                               l.modo_sede,
                               l.sede_unica_liga,
                               eq.id,
                               eq.nombre,
                               eq.club,
                               (me.es_capitan OR eq.capitan_id = :usuarioId) AS es_capitan_efectivo
                        FROM miembros_equipo me
                        JOIN equipos eq ON eq.id = me.equipo_id
                        JOIN divisiones d ON d.id = eq.division_id
                        JOIN temporadas t ON t.id = d.temporada_id
                        JOIN ligas l ON l.id = t.liga_id
                        WHERE me.usuario_id = :usuarioId
                          AND me.activo = TRUE
                          AND t.activa = TRUE
                        """)
                .setParameter("usuarioId", usuarioId)
                .getResultList();

        for (Object[] fila : filasEquipos) {
            UUID ligaId = convertirAUuid(fila[0]);
            String nombreLiga = fila[1].toString();
            int partidosPorJornada = ((Number) fila[2]).intValue();
            ModoSedeLiga modoSede = ModoSedeLiga.valueOf(fila[3].toString());
            String sedeUnicaLiga = fila[4] != null ? fila[4].toString() : null;
            UUID equipoId = convertirAUuid(fila[5]);
            String nombreEquipo = fila[6].toString();
            String clubEquipo = fila[7] != null ? fila[7].toString() : null;
            boolean esCapitan = Boolean.TRUE.equals(fila[8]);

            String textoSede = construirTextoSedeEquipo(modoSede, sedeUnicaLiga, clubEquipo);
            RolMembresia rol = esCapitan ? RolMembresia.CAPITAN : RolMembresia.JUGADOR;

            membresias.add(new ResumenMembresiaUsuario(
                    ligaId,
                    nombreLiga,
                    equipoId,
                    nombreEquipo,
                    textoSede,
                    partidosPorJornada,
                    rol
            ));
        }

        return membresias;
    }

    @Override
    public Optional<Liga> buscarLigaPorCodigoInvitacion(String codigoInvitacion) {
        if (codigoInvitacion == null || codigoInvitacion.isBlank()) {
            return Optional.empty();
        }
        return repositorioJpaLiga
                .buscarPorCodigoInvitacionIgnorandoMayusculas(codigoInvitacion.trim())
                .map(this::aDominioLiga);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Optional<Liga> obtenerLigaPorEquipoId(UUID equipoId) {
        if (equipoId == null) {
            return Optional.empty();
        }
        List<Object> idsLiga = gestorEntidades.createNativeQuery("""
                        SELECT t.liga_id
                        FROM equipos eq
                        JOIN divisiones d ON d.id = eq.division_id
                        JOIN temporadas t ON t.id = d.temporada_id
                        WHERE eq.id = :equipoId
                        LIMIT 1
                        """)
                .setParameter("equipoId", equipoId)
                .getResultList();

        if (idsLiga.isEmpty()) {
            return Optional.empty();
        }
        return repositorioJpaLiga.findById(convertirAUuid(idsLiga.get(0)))
                .map(this::aDominioLiga);
    }

    @Override
    public boolean perteneceUsuarioALiga(UUID usuarioId, UUID ligaId) {
        if (usuarioId == null || ligaId == null) {
            return false;
        }
        Object existe = gestorEntidades.createNativeQuery("""
                        SELECT EXISTS(
                            SELECT 1
                            FROM miembros_equipo me
                            JOIN equipos eq ON eq.id = me.equipo_id
                            JOIN divisiones d ON d.id = eq.division_id
                            JOIN temporadas t ON t.id = d.temporada_id
                            WHERE me.usuario_id = :usuarioId
                              AND me.activo = TRUE
                              AND t.liga_id = :ligaId
                              AND t.activa = TRUE
                        )
                        """)
                .setParameter("usuarioId", usuarioId)
                .setParameter("ligaId", ligaId)
                .getSingleResult();
        return Boolean.TRUE.equals(existe);
    }

    @Override
    @SuppressWarnings("unchecked")
    public UUID obtenerOCrearDivisionInscripcionLiga(UUID ligaId, String nombreLiga) {
        List<Object> divisionesExistentes = gestorEntidades.createNativeQuery("""
                        SELECT d.id
                        FROM divisiones d
                        JOIN temporadas t ON t.id = d.temporada_id
                        WHERE t.liga_id = :ligaId
                          AND t.activa = TRUE
                        ORDER BY d.nivel ASC, d.creado_en ASC
                        LIMIT 1
                        """)
                .setParameter("ligaId", ligaId)
                .getResultList();

        if (!divisionesExistentes.isEmpty()) {
            return convertirAUuid(divisionesExistentes.get(0));
        }

        List<Object> temporadasActivas = gestorEntidades.createNativeQuery("""
                        SELECT id
                        FROM temporadas
                        WHERE liga_id = :ligaId
                          AND activa = TRUE
                        ORDER BY creado_en ASC
                        LIMIT 1
                        """)
                .setParameter("ligaId", ligaId)
                .getResultList();

        UUID temporadaId;
        if (!temporadasActivas.isEmpty()) {
            temporadaId = convertirAUuid(temporadasActivas.get(0));
        } else {
            temporadaId = UUID.randomUUID();
            LocalDate hoy = LocalDate.now();
            gestorEntidades.createNativeQuery("""
                            INSERT INTO temporadas (id, liga_id, nombre, fecha_inicio, activa)
                            VALUES (:id, :ligaId, :nombre, :fechaInicio, TRUE)
                            """)
                    .setParameter("id", temporadaId)
                    .setParameter("ligaId", ligaId)
                    .setParameter("nombre", "Temporada Inicial " + hoy.getYear())
                    .setParameter("fechaInicio", hoy)
                    .executeUpdate();
        }

        UUID nuevaDivisionId = UUID.randomUUID();
        gestorEntidades.createNativeQuery("""
                        INSERT INTO divisiones (id, temporada_id, nombre, nivel)
                        VALUES (:id, :temporadaId, :nombre, 1)
                        """)
                .setParameter("id", nuevaDivisionId)
                .setParameter("temporadaId", temporadaId)
                .setParameter("nombre", "Primera División - " + nombreLiga)
                .executeUpdate();

        return nuevaDivisionId;
    }

    @Override
    public boolean existeEquipoConNombreEnDivision(UUID divisionId, String nombreEquipo) {
        return repositorioJpaEquipo.existePorDivisionIdYNombreIgnorandoMayusculas(divisionId, nombreEquipo);
    }

    @Override
    public UUID crearEquipoConCapitan(
            UUID divisionId,
            String nombreEquipo,
            String clubSede,
            UUID capitanUsuarioId
    ) {
        OffsetDateTime ahora = OffsetDateTime.now();
        UUID equipoId = UUID.randomUUID();

        EntidadEquipo nuevoEquipo = new EntidadEquipo(
                equipoId,
                divisionId,
                nombreEquipo,
                clubSede,
                capitanUsuarioId,
                ahora,
                ahora
        );
        repositorioJpaEquipo.saveAndFlush(nuevoEquipo);

        EntidadMiembroEquipo membresiaCapitan = new EntidadMiembroEquipo(
                UUID.randomUUID(),
                equipoId,
                capitanUsuarioId,
                true,
                true,
                ahora,
                ahora
        );
        repositorioJpaMiembroEquipo.saveAndFlush(membresiaCapitan);

        return equipoId;
    }

    private Liga aDominioLiga(EntidadLiga entidad) {
        return new Liga(
                entidad.obtenerId(),
                entidad.obtenerNombre(),
                entidad.obtenerDescripcion(),
                entidad.obtenerCodigoInvitacion(),
                entidad.obtenerPresidenteId(),
                entidad.obtenerPartidosPorJornada(),
                entidad.obtenerModoSede(),
                entidad.obtenerSedeUnicaLiga(),
                entidad.obtenerPuntosVictoria(),
                entidad.obtenerPuntosEmpate(),
                entidad.obtenerPuntosDerrota(),
                entidad.exigeDniJugador(),
                entidad.exigeTelefonoJugador(),
                entidad.obtenerNormativaTexto()
        );
    }

    private String construirTextoSedeEquipo(ModoSedeLiga modoSede, String sedeUnicaLiga, String clubEquipo) {
        if (modoSede == ModoSedeLiga.SEDE_UNICA_CLUB) {
            return sedeUnicaLiga != null && !sedeUnicaLiga.isBlank()
                    ? "Sede única: " + sedeUnicaLiga.trim()
                    : "Sede única de liga";
        }
        if (clubEquipo != null && !clubEquipo.isBlank()) {
            return "Sede local: " + clubEquipo.trim();
        }
        return "Sede local por equipo";
    }

    private UUID convertirAUuid(Object valor) {
        if (valor instanceof UUID uuid) {
            return uuid;
        }
        return UUID.fromString(valor.toString());
    }
}
