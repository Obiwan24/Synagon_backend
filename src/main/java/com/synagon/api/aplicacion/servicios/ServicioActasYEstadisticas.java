package com.synagon.api.aplicacion.servicios;

import com.synagon.api.dominio.excepciones.RecursoNoEncontradoExcepcion;
import com.synagon.api.dominio.modelos.DatosEquipoDivision;
import com.synagon.api.dominio.modelos.DatosMiembroEquipo;
import com.synagon.api.dominio.modelos.Enfrentamiento;
import com.synagon.api.dominio.modelos.EstadisticaJugador;
import com.synagon.api.dominio.modelos.GanadorPareja;
import com.synagon.api.dominio.modelos.PartidoPareja;
import com.synagon.api.dominio.modelos.PosicionClasificacionEquipo;
import com.synagon.api.dominio.modelos.ResultadoPartido;
import com.synagon.api.dominio.puertos.entrada.ActasYEstadisticasCasoUso;
import com.synagon.api.dominio.puertos.salida.RepositorioActasYEstadisticasPuerto;
import com.synagon.api.dominio.puertos.salida.RepositorioEnfrentamientoPuerto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Servicio transaccional de aplicación encargado del registro de actas arbitrales,
 * cálculo en tiempo real de la clasificación de una división y estadísticas individuales por equipo.
 */
@Service
@Transactional
public class ServicioActasYEstadisticas implements ActasYEstadisticasCasoUso {

    private final RepositorioEnfrentamientoPuerto repositorioEnfrentamiento;
    private final RepositorioActasYEstadisticasPuerto repositorioEstadisticas;

    public ServicioActasYEstadisticas(
            RepositorioEnfrentamientoPuerto repositorioEnfrentamiento,
            RepositorioActasYEstadisticasPuerto repositorioEstadisticas
    ) {
        this.repositorioEnfrentamiento = repositorioEnfrentamiento;
        this.repositorioEstadisticas = repositorioEstadisticas;
    }

    @Override
    public Enfrentamiento registrarActaEnfrentamiento(
            UUID enfrentamientoId,
            String urlFotoActa,
            List<ResultadoPartido> resultadosTresPartidos
    ) {
        if (enfrentamientoId == null) {
            throw new IllegalArgumentException("El identificador del enfrentamiento es obligatorio.");
        }

        Enfrentamiento enfrentamiento = repositorioEnfrentamiento.obtenerPorId(enfrentamientoId)
                .orElseThrow(() -> new RecursoNoEncontradoExcepcion(
                        String.format("No se encontró el enfrentamiento con id %s.", enfrentamientoId)
                ));

        Enfrentamiento actaConfirmada = enfrentamiento.registrarActa(urlFotoActa, resultadosTresPartidos);
        return repositorioEnfrentamiento.guardar(actaConfirmada);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PosicionClasificacionEquipo> obtenerClasificacionDivision(UUID divisionId) {
        if (divisionId == null) {
            throw new IllegalArgumentException("El identificador de la división es obligatorio.");
        }
        if (!repositorioEstadisticas.existeDivisionPorId(divisionId)) {
            throw new RecursoNoEncontradoExcepcion(
                    String.format("No se encontró la división con id %s.", divisionId)
            );
        }

        List<DatosEquipoDivision> equiposDivision = repositorioEstadisticas.obtenerEquiposPorDivisionId(divisionId);
        List<Enfrentamiento> enfrentamientosConActa = repositorioEstadisticas.obtenerEnfrentamientosConActaPorDivisionId(divisionId);

        Map<UUID, AcumuladorEquipo> acumuladores = new LinkedHashMap<>();
        for (DatosEquipoDivision equipo : equiposDivision) {
            acumuladores.put(equipo.equipoId(), new AcumuladorEquipo(equipo.equipoId(), equipo.nombreEquipo()));
        }

        for (Enfrentamiento enfrentamiento : enfrentamientosConActa) {
            AcumuladorEquipo local = acumuladores.get(enfrentamiento.obtenerEquipoLocalId());
            AcumuladorEquipo visitante = acumuladores.get(enfrentamiento.obtenerEquipoVisitanteId());

            int partidosLocal = 0;
            int partidosVisitante = 0;
            int setsLocal = 0;
            int setsVisitante = 0;
            int juegosLocal = 0;
            int juegosVisitante = 0;

            for (PartidoPareja partido : enfrentamiento.obtenerPartidos()) {
                if (!partido.estaCompletado()) {
                    continue;
                }
                GanadorPareja ganador = partido.obtenerParejaGanadora() != null
                        ? partido.obtenerParejaGanadora()
                        : partido.determinarGanador();

                if (ganador == GanadorPareja.LOCAL) {
                    partidosLocal++;
                } else {
                    partidosVisitante++;
                }

                setsLocal += partido.calcularSetsGanadosLocal();
                setsVisitante += partido.calcularSetsGanadosVisitante();
                juegosLocal += partido.calcularJuegosGanadosLocal();
                juegosVisitante += partido.calcularJuegosGanadosVisitante();
            }

            if (partidosLocal == 0 && partidosVisitante == 0) {
                continue;
            }

            boolean victoriaLocal = partidosLocal > partidosVisitante;

            if (local != null) {
                local.registrarEnfrentamiento(
                        victoriaLocal,
                        partidosLocal,
                        partidosVisitante,
                        setsLocal,
                        setsVisitante,
                        juegosLocal,
                        juegosVisitante
                );
            }

            if (visitante != null) {
                visitante.registrarEnfrentamiento(
                        !victoriaLocal,
                        partidosVisitante,
                        partidosLocal,
                        setsVisitante,
                        setsLocal,
                        juegosVisitante,
                        juegosLocal
                );
            }
        }

        List<PosicionClasificacionEquipo> ordenados = acumuladores.values().stream()
                .map(AcumuladorEquipo::aModeloDominioSinPosicion)
                .sorted(PosicionClasificacionEquipo.comparadorClasificacion())
                .toList();

        List<PosicionClasificacionEquipo> clasificacionFinal = new ArrayList<>(ordenados.size());
        for (int i = 0; i < ordenados.size(); i++) {
            clasificacionFinal.add(ordenados.get(i).conPosicion(i + 1));
        }

        return clasificacionFinal;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EstadisticaJugador> obtenerEstadisticasJugadoresEquipo(UUID equipoId) {
        if (equipoId == null) {
            throw new IllegalArgumentException("El identificador del equipo es obligatorio.");
        }
        if (!repositorioEstadisticas.existeEquipoPorId(equipoId)) {
            throw new RecursoNoEncontradoExcepcion(
                    String.format("No se encontró el equipo con id %s.", equipoId)
            );
        }

        List<DatosMiembroEquipo> miembros = repositorioEstadisticas.obtenerMiembrosPorEquipoId(equipoId);
        Map<UUID, Integer> apuntadosPorJugador = repositorioEstadisticas.obtenerPartidosApuntadosPorMiembrosDeEquipo(equipoId);
        List<Enfrentamiento> enfrentamientosConActa = repositorioEstadisticas.obtenerEnfrentamientosConActaPorEquipoId(equipoId);

        Map<UUID, EstadisticaJugador> estadisticasPorJugador = new LinkedHashMap<>();
        for (DatosMiembroEquipo miembro : miembros) {
            int apuntados = apuntadosPorJugador.getOrDefault(miembro.jugadorId(), 0);
            estadisticasPorJugador.put(
                    miembro.jugadorId(),
                    EstadisticaJugador.inicialConDisponibilidad(
                            miembro.jugadorId(),
                            miembro.nombreCompleto(),
                            miembro.temporadaId(),
                            apuntados
                    )
            );
        }

        for (Enfrentamiento enfrentamiento : enfrentamientosConActa) {
            boolean esEquipoLocal = equipoId.equals(enfrentamiento.obtenerEquipoLocalId());
            boolean esEquipoVisitante = equipoId.equals(enfrentamiento.obtenerEquipoVisitanteId());

            for (PartidoPareja partido : enfrentamiento.obtenerPartidos()) {
                if (!partido.estaCompletado()) {
                    continue;
                }

                GanadorPareja ganador = partido.obtenerParejaGanadora() != null
                        ? partido.obtenerParejaGanadora()
                        : partido.determinarGanador();

                int setsLocal = partido.calcularSetsGanadosLocal();
                int setsVisitante = partido.calcularSetsGanadosVisitante();
                int juegosLocal = partido.calcularJuegosGanadosLocal();
                int juegosVisitante = partido.calcularJuegosGanadosVisitante();

                if (esEquipoLocal) {
                    boolean victoria = ganador == GanadorPareja.LOCAL;
                    acumularParaJugadorSiPertenece(
                            estadisticasPorJugador,
                            partido.obtenerJugadorLocal1Id(),
                            victoria, setsLocal, setsVisitante, juegosLocal, juegosVisitante
                    );
                    acumularParaJugadorSiPertenece(
                            estadisticasPorJugador,
                            partido.obtenerJugadorLocal2Id(),
                            victoria, setsLocal, setsVisitante, juegosLocal, juegosVisitante
                    );
                }

                if (esEquipoVisitante) {
                    boolean victoria = ganador == GanadorPareja.VISITANTE;
                    acumularParaJugadorSiPertenece(
                            estadisticasPorJugador,
                            partido.obtenerJugadorVisitante1Id(),
                            victoria, setsVisitante, setsLocal, juegosVisitante, juegosLocal
                    );
                    acumularParaJugadorSiPertenece(
                            estadisticasPorJugador,
                            partido.obtenerJugadorVisitante2Id(),
                            victoria, setsVisitante, setsLocal, juegosVisitante, juegosLocal
                    );
                }
            }
        }

        Collator coladorEspanol = Collator.getInstance(Locale.forLanguageTag("es-ES"));
        coladorEspanol.setStrength(Collator.PRIMARY);

        return estadisticasPorJugador.values().stream()
                .sorted(Comparator.comparingInt(EstadisticaJugador::obtenerPartidosGanados).reversed()
                        .thenComparing(Comparator.comparingInt(EstadisticaJugador::calcularDiferenciaSets).reversed())
                        .thenComparing(Comparator.comparingInt(EstadisticaJugador::calcularDiferenciaJuegos).reversed())
                        .thenComparing(Comparator.comparingInt(EstadisticaJugador::obtenerPartidosJugados).reversed())
                        .thenComparing(Comparator.comparingInt(EstadisticaJugador::obtenerPartidosApuntados).reversed())
                        .thenComparing(
                                e -> e.obtenerNombreJugador() != null ? e.obtenerNombreJugador() : "",
                                coladorEspanol
                        ))
                .toList();
    }

    private void acumularParaJugadorSiPertenece(
            Map<UUID, EstadisticaJugador> mapa,
            UUID jugadorId,
            boolean victoria,
            int setsGanados,
            int setsPerdidos,
            int juegosGanados,
            int juegosPerdidos
    ) {
        if (jugadorId == null) {
            return;
        }
        EstadisticaJugador actual = mapa.get(jugadorId);
        if (actual != null) {
            mapa.put(
                    jugadorId,
                    actual.acumularPartido(
                            victoria,
                            setsGanados,
                            setsPerdidos,
                            juegosGanados,
                            juegosPerdidos,
                            victoria ? 3 : 0
                    )
            );
        }
    }

    private static final class AcumuladorEquipo {
        private final UUID equipoId;
        private final String nombreEquipo;
        private int puntos;
        private int enfrentamientosJugados;
        private int enfrentamientosGanados;
        private int enfrentamientosPerdidos;
        private int partidosGanados;
        private int partidosPerdidos;
        private int setsAFavor;
        private int setsEnContra;
        private int juegosAFavor;
        private int juegosEnContra;

        private AcumuladorEquipo(UUID equipoId, String nombreEquipo) {
            this.equipoId = equipoId;
            this.nombreEquipo = nombreEquipo;
        }

        private void registrarEnfrentamiento(
                boolean victoriaEnfrentamiento,
                int partidosFavor,
                int partidosContra,
                int setsFavor,
                int setsContra,
                int juegosFavor,
                int juegosContra
        ) {
            this.enfrentamientosJugados++;
            if (victoriaEnfrentamiento) {
                this.enfrentamientosGanados++;
                this.puntos += PosicionClasificacionEquipo.PUNTOS_POR_VICTORIA_ENFRENTAMIENTO;
            } else {
                this.enfrentamientosPerdidos++;
            }
            this.partidosGanados += partidosFavor;
            this.partidosPerdidos += partidosContra;
            this.setsAFavor += setsFavor;
            this.setsEnContra += setsContra;
            this.juegosAFavor += juegosFavor;
            this.juegosEnContra += juegosContra;
        }

        private PosicionClasificacionEquipo aModeloDominioSinPosicion() {
            return new PosicionClasificacionEquipo(
                    0,
                    equipoId,
                    nombreEquipo,
                    puntos,
                    enfrentamientosJugados,
                    enfrentamientosGanados,
                    enfrentamientosPerdidos,
                    partidosGanados,
                    partidosPerdidos,
                    setsAFavor,
                    setsEnContra,
                    juegosAFavor,
                    juegosEnContra
            );
        }
    }
}
