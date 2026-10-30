package com.synagon.api.dominio.modelos;

import com.synagon.api.dominio.excepciones.ConvocatoriaInvalidaExcepcion;
import com.synagon.api.dominio.excepciones.RecursoNoEncontradoExcepcion;
import com.synagon.api.dominio.excepciones.ReglaNegocioExcepcion;
import com.synagon.api.dominio.excepciones.ResultadoPadelIlegalExcepcion;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Modelo de dominio inmutable que representa un enfrentamiento oficial entre dos equipos en una jornada.
 * Gestiona la negociación de fechas, la votación de disponibilidades, la sede alternativa de jornada,
 * la validación de convocatorias de 6 jugadores únicos en 3 parejas y el registro/cierre del acta arbitral.
 */
public final class Enfrentamiento {

    private final UUID id;
    private final UUID divisionId;
    private final UUID equipoLocalId;
    private final UUID equipoVisitanteId;
    private final int numeroJornada;
    private final EstadoEnfrentamiento estado;
    private final LocalDateTime fechaDefinitiva;
    private final String pistas;
    private final String sedeAlternativaJornada;
    private final String urlFotoActa;
    private final int puntosLocal;
    private final int puntosVisitante;
    private final boolean actaConfirmada;
    private final List<PropuestaFecha> propuestasFecha;
    private final List<DisponibilidadJugador> disponibilidades;
    private final List<PartidoPareja> partidos;

    public Enfrentamiento(
            UUID id,
            UUID divisionId,
            UUID equipoLocalId,
            UUID equipoVisitanteId,
            int numeroJornada,
            EstadoEnfrentamiento estado,
            LocalDateTime fechaDefinitiva,
            String pistas,
            int puntosLocal,
            int puntosVisitante,
            boolean actaConfirmada,
            List<PropuestaFecha> propuestasFecha,
            List<DisponibilidadJugador> disponibilidades,
            List<PartidoPareja> partidos
    ) {
        this(
                id,
                divisionId,
                equipoLocalId,
                equipoVisitanteId,
                numeroJornada,
                estado,
                fechaDefinitiva,
                pistas,
                null,
                null,
                puntosLocal,
                puntosVisitante,
                actaConfirmada,
                propuestasFecha,
                disponibilidades,
                partidos
        );
    }

    public Enfrentamiento(
            UUID id,
            UUID divisionId,
            UUID equipoLocalId,
            UUID equipoVisitanteId,
            int numeroJornada,
            EstadoEnfrentamiento estado,
            LocalDateTime fechaDefinitiva,
            String pistas,
            String urlFotoActa,
            int puntosLocal,
            int puntosVisitante,
            boolean actaConfirmada,
            List<PropuestaFecha> propuestasFecha,
            List<DisponibilidadJugador> disponibilidades,
            List<PartidoPareja> partidos
    ) {
        this(
                id,
                divisionId,
                equipoLocalId,
                equipoVisitanteId,
                numeroJornada,
                estado,
                fechaDefinitiva,
                pistas,
                null,
                urlFotoActa,
                puntosLocal,
                puntosVisitante,
                actaConfirmada,
                propuestasFecha,
                disponibilidades,
                partidos
        );
    }

    public Enfrentamiento(
            UUID id,
            UUID divisionId,
            UUID equipoLocalId,
            UUID equipoVisitanteId,
            int numeroJornada,
            EstadoEnfrentamiento estado,
            LocalDateTime fechaDefinitiva,
            String pistas,
            String sedeAlternativaJornada,
            String urlFotoActa,
            int puntosLocal,
            int puntosVisitante,
            boolean actaConfirmada,
            List<PropuestaFecha> propuestasFecha,
            List<DisponibilidadJugador> disponibilidades,
            List<PartidoPareja> partidos
    ) {
        this.id = id != null ? id : UUID.randomUUID();
        this.divisionId = Objects.requireNonNull(divisionId, "El id de la división no puede ser nulo.");
        this.equipoLocalId = Objects.requireNonNull(equipoLocalId, "El equipo local es obligatorio.");
        this.equipoVisitanteId = Objects.requireNonNull(equipoVisitanteId, "El equipo visitante es obligatorio.");

        if (this.equipoLocalId.equals(this.equipoVisitanteId)) {
            throw new IllegalArgumentException("Un equipo no puede enfrentarse contra sí mismo.");
        }
        if (numeroJornada < 1) {
            throw new IllegalArgumentException("El número de jornada debe ser positivo.");
        }

        this.numeroJornada = numeroJornada;
        this.estado = estado != null ? estado : EstadoEnfrentamiento.PENDIENTE_FECHA;
        this.fechaDefinitiva = fechaDefinitiva;
        this.pistas = pistas;
        this.sedeAlternativaJornada = sedeAlternativaJornada != null && !sedeAlternativaJornada.isBlank()
                ? sedeAlternativaJornada.trim()
                : null;
        this.urlFotoActa = urlFotoActa;
        this.puntosLocal = Math.max(0, puntosLocal);
        this.puntosVisitante = Math.max(0, puntosVisitante);
        this.actaConfirmada = actaConfirmada;
        this.propuestasFecha = propuestasFecha != null ? List.copyOf(propuestasFecha) : List.of();
        this.disponibilidades = disponibilidades != null ? List.copyOf(disponibilidades) : List.of();
        this.partidos = partidos != null ? List.copyOf(partidos) : List.of();
    }

    public static Enfrentamiento crear(
            UUID divisionId,
            UUID equipoLocalId,
            UUID equipoVisitanteId,
            int numeroJornada
    ) {
        return new Enfrentamiento(
                UUID.randomUUID(),
                divisionId,
                equipoLocalId,
                equipoVisitanteId,
                numeroJornada,
                EstadoEnfrentamiento.PENDIENTE_FECHA,
                null,
                null,
                null,
                null,
                0,
                0,
                false,
                List.of(),
                List.of(),
                List.of()
        );
    }

    /**
     * Registra o limpia una sede alternativa para la jornada actual sin alterar la sede oficial del equipo local.
     */
    public Enfrentamiento actualizarSedeAlternativaJornada(String nuevaSedeAlternativa) {
        if (this.estado == EstadoEnfrentamiento.FINALIZADO
                || this.estado == EstadoEnfrentamiento.FINALIZADO_CONFIRMADO
                || this.estado == EstadoEnfrentamiento.CANCELADO) {
            throw new ReglaNegocioExcepcion(
                    String.format("No se puede actualizar la sede alternativa cuando el enfrentamiento está en estado %s.", this.estado)
            );
        }

        String sedeNormalizada = nuevaSedeAlternativa != null && !nuevaSedeAlternativa.isBlank()
                ? nuevaSedeAlternativa.trim()
                : null;

        if (sedeNormalizada != null && sedeNormalizada.length() > 150) {
            throw new IllegalArgumentException("La sede alternativa de la jornada no puede superar los 150 caracteres.");
        }

        return new Enfrentamiento(
                this.id,
                this.divisionId,
                this.equipoLocalId,
                this.equipoVisitanteId,
                this.numeroJornada,
                this.estado,
                this.fechaDefinitiva,
                this.pistas,
                sedeNormalizada,
                this.urlFotoActa,
                this.puntosLocal,
                this.puntosVisitante,
                this.actaConfirmada,
                this.propuestasFecha,
                this.disponibilidades,
                this.partidos
        );
    }

    /**
     * Propone exactamente 2 fechas futuras y abre el periodo de votación de disponibilidad.
     */
    public Enfrentamiento proponerFechas(List<LocalDateTime> listaDosFechas, LocalDateTime instanteActual) {
        if (this.estado != EstadoEnfrentamiento.PENDIENTE_FECHA
                && this.estado != EstadoEnfrentamiento.VOTACION_DISPONIBILIDAD_ABIERTA) {
            throw new ReglaNegocioExcepcion(
                    String.format("No se pueden proponer fechas cuando el enfrentamiento está en estado %s.", this.estado)
            );
        }

        if (listaDosFechas == null || listaDosFechas.size() != 2) {
            throw new ReglaNegocioExcepcion("Se deben proponer exactamente 2 fechas.");
        }

        LocalDateTime fecha1 = listaDosFechas.get(0);
        LocalDateTime fecha2 = listaDosFechas.get(1);
        LocalDateTime referencia = instanteActual != null ? instanteActual : LocalDateTime.now();

        if (fecha1 == null || fecha2 == null) {
            throw new ReglaNegocioExcepcion("Ninguna de las fechas propuestas puede ser nula.");
        }
        if (!fecha1.isAfter(referencia) || !fecha2.isAfter(referencia)) {
            throw new ReglaNegocioExcepcion("Ambas fechas propuestas deben ser futuras.");
        }
        if (fecha1.equals(fecha2)) {
            throw new ReglaNegocioExcepcion("Las dos fechas propuestas deben ser distintas entre sí.");
        }

        PropuestaFecha propuesta1 = PropuestaFecha.crearNueva(this.id, fecha1, 1);
        PropuestaFecha propuesta2 = PropuestaFecha.crearNueva(this.id, fecha2, 2);

        return new Enfrentamiento(
                this.id,
                this.divisionId,
                this.equipoLocalId,
                this.equipoVisitanteId,
                this.numeroJornada,
                EstadoEnfrentamiento.VOTACION_DISPONIBILIDAD_ABIERTA,
                null,
                this.pistas,
                this.sedeAlternativaJornada,
                this.urlFotoActa,
                this.puntosLocal,
                this.puntosVisitante,
                this.actaConfirmada,
                List.of(propuesta1, propuesta2),
                List.of(),
                this.partidos
        );
    }

    /**
     * Registra las dos fechas propuestas obligatorias por el capitán local (conservando el estado actual).
     */
    public Enfrentamiento ofrecerPropuestasFecha(PropuestaFecha propuesta1, PropuestaFecha propuesta2) {
        Objects.requireNonNull(propuesta1, "La propuesta 1 es obligatoria.");
        Objects.requireNonNull(propuesta2, "La propuesta 2 es obligatoria.");

        if (!this.id.equals(propuesta1.obtenerEnfrentamientoId()) || !this.id.equals(propuesta2.obtenerEnfrentamientoId())) {
            throw new IllegalArgumentException("Las propuestas no corresponden a este enfrentamiento.");
        }

        Set<Integer> ordenes = Set.of(propuesta1.obtenerOrden(), propuesta2.obtenerOrden());
        if (!ordenes.contains(1) || !ordenes.contains(2)) {
            throw new IllegalArgumentException("El capitán local debe proponer exactamente dos fechas: orden 1 y orden 2.");
        }

        return new Enfrentamiento(
                this.id,
                this.divisionId,
                this.equipoLocalId,
                this.equipoVisitanteId,
                this.numeroJornada,
                this.estado,
                this.fechaDefinitiva,
                this.pistas,
                this.sedeAlternativaJornada,
                this.urlFotoActa,
                this.puntosLocal,
                this.puntosVisitante,
                this.actaConfirmada,
                List.of(propuesta1, propuesta2),
                this.disponibilidades,
                this.partidos
        );
    }

    /**
     * Registra o actualiza el voto de disponibilidad de un jugador sobre una propuesta de fecha.
     */
    public Enfrentamiento registrarVotoDisponibilidad(
            UUID jugadorId,
            UUID propuestaFechaId,
            EstadoDisponibilidad tipoDisponibilidad
    ) {
        Objects.requireNonNull(jugadorId, "El identificador del jugador es obligatorio.");
        Objects.requireNonNull(propuestaFechaId, "El identificador de la propuesta de fecha es obligatorio.");
        Objects.requireNonNull(tipoDisponibilidad, "El tipo de disponibilidad es obligatorio.");

        if (this.estado != EstadoEnfrentamiento.VOTACION_DISPONIBILIDAD_ABIERTA
                && this.estado != EstadoEnfrentamiento.CONVOCATORIA_ABIERTA) {
            throw new ReglaNegocioExcepcion(
                    String.format("No se admite votación de disponibilidad en el estado actual (%s).", this.estado)
            );
        }

        PropuestaFecha propuesta = this.propuestasFecha.stream()
                .filter(p -> p.obtenerId().equals(propuestaFechaId))
                .findFirst()
                .orElseThrow(() -> new RecursoNoEncontradoExcepcion(
                        String.format("No existe la propuesta de fecha %s en el enfrentamiento %s.", propuestaFechaId, this.id)
                ));

        if (this.estado == EstadoEnfrentamiento.CONVOCATORIA_ABIERTA && !propuesta.esSeleccionada()) {
            throw new ReglaNegocioExcepcion(
                    "En fase de convocatoria abierta solo se permite votar disponibilidad sobre la propuesta de fecha elegida."
            );
        }

        List<DisponibilidadJugador> nuevasDisponibilidades = new ArrayList<>();
        boolean actualizado = false;

        for (DisponibilidadJugador disp : this.disponibilidades) {
            if (disp.obtenerJugadorId().equals(jugadorId)
                    && Objects.equals(disp.obtenerPropuestaFechaId(), propuestaFechaId)) {
                nuevasDisponibilidades.add(disp.cambiarEstado(tipoDisponibilidad));
                actualizado = true;
            } else {
                nuevasDisponibilidades.add(disp);
            }
        }

        if (!actualizado) {
            nuevasDisponibilidades.add(
                    DisponibilidadJugador.paraPropuesta(this.id, propuestaFechaId, jugadorId, tipoDisponibilidad)
            );
        }

        return new Enfrentamiento(
                this.id,
                this.divisionId,
                this.equipoLocalId,
                this.equipoVisitanteId,
                this.numeroJornada,
                this.estado,
                this.fechaDefinitiva,
                this.pistas,
                this.sedeAlternativaJornada,
                this.urlFotoActa,
                this.puntosLocal,
                this.puntosVisitante,
                this.actaConfirmada,
                this.propuestasFecha,
                nuevasDisponibilidades,
                this.partidos
        );
    }

    /**
     * Marca una de las propuestas de fecha como elegida/definitiva y avanza el estado a CONVOCATORIA_ABIERTA.
     */
    public Enfrentamiento seleccionarFechaDefinitiva(UUID propuestaFechaElegidaId) {
        Objects.requireNonNull(propuestaFechaElegidaId, "El identificador de la propuesta elegida es obligatorio.");

        if (this.estado != EstadoEnfrentamiento.VOTACION_DISPONIBILIDAD_ABIERTA) {
            throw new ReglaNegocioExcepcion(
                    String.format("Solo se puede fijar la fecha definitiva desde VOTACION_DISPONIBILIDAD_ABIERTA (estado actual: %s).",
                            this.estado)
            );
        }

        PropuestaFecha elegida = this.propuestasFecha.stream()
                .filter(p -> p.obtenerId().equals(propuestaFechaElegidaId))
                .findFirst()
                .orElseThrow(() -> new RecursoNoEncontradoExcepcion(
                        String.format("La propuesta de fecha %s no pertenece al enfrentamiento %s.", propuestaFechaElegidaId, this.id)
                ));

        List<PropuestaFecha> propuestasActualizadas = this.propuestasFecha.stream()
                .map(p -> p.obtenerId().equals(propuestaFechaElegidaId)
                        ? p.marcarComoSeleccionada()
                        : p.desmarcarSeleccion())
                .toList();

        return new Enfrentamiento(
                this.id,
                this.divisionId,
                this.equipoLocalId,
                this.equipoVisitanteId,
                this.numeroJornada,
                EstadoEnfrentamiento.CONVOCATORIA_ABIERTA,
                elegida.obtenerFechaPropuesta(),
                this.pistas,
                this.sedeAlternativaJornada,
                this.urlFotoActa,
                this.puntosLocal,
                this.puntosVisitante,
                this.actaConfirmada,
                propuestasActualizadas,
                this.disponibilidades,
                this.partidos
        );
    }

    /**
     * Fija directamente una fecha definitiva del enfrentamiento (método de compatibilidad).
     */
    public Enfrentamiento fijarFechaDefinitiva(LocalDateTime fechaDefinitiva, String pistas) {
        Objects.requireNonNull(fechaDefinitiva, "La fecha definitiva es obligatoria.");

        return new Enfrentamiento(
                this.id,
                this.divisionId,
                this.equipoLocalId,
                this.equipoVisitanteId,
                this.numeroJornada,
                EstadoEnfrentamiento.FECHA_FIJADA,
                fechaDefinitiva,
                pistas,
                this.sedeAlternativaJornada,
                this.urlFotoActa,
                this.puntosLocal,
                this.puntosVisitante,
                this.actaConfirmada,
                this.propuestasFecha,
                this.disponibilidades,
                this.partidos
        );
    }

    /**
     * Cierra la convocatoria y asigna las 3 parejas (Pareja 1, Pareja 2 y Pareja 3).
     */
    public Enfrentamiento cerrarConvocatoriaYAlineacion(List<PartidoPareja> listaTresParejas) {
        if (this.estado != EstadoEnfrentamiento.CONVOCATORIA_ABIERTA || this.fechaDefinitiva == null) {
            throw new ReglaNegocioExcepcion(
                    String.format("Solo se puede cerrar la convocatoria y alineación cuando el estado es CONVOCATORIA_ABIERTA y existe fecha acordada (estado actual: %s).",
                            this.estado)
            );
        }

        if (listaTresParejas == null || listaTresParejas.size() != 3) {
            throw new ConvocatoriaInvalidaExcepcion("Se deben asignar exactamente 3 parejas (Pareja 1, Pareja 2 y Pareja 3).");
        }

        Set<Integer> numerosPareja = new HashSet<>();
        Set<UUID> jugadoresConvocados = new HashSet<>();

        for (PartidoPareja pareja : listaTresParejas) {
            if (pareja == null) {
                throw new ConvocatoriaInvalidaExcepcion("Ninguna pareja de la convocatoria puede ser nula.");
            }
            if (!this.id.equals(pareja.obtenerEnfrentamientoId())) {
                throw new ConvocatoriaInvalidaExcepcion("Todas las parejas deben pertenecer al enfrentamiento actual.");
            }

            int numero = pareja.obtenerNumeroPareja();
            if (numero < 1 || numero > 3 || !numerosPareja.add(numero)) {
                throw new ConvocatoriaInvalidaExcepcion("Las parejas deben corresponder exactamente a los números 1, 2 y 3 sin duplicados.");
            }

            UUID jugador1 = pareja.obtenerJugadorLocal1Id();
            UUID jugador2 = pareja.obtenerJugadorLocal2Id();
            if (jugador1 == null || jugador2 == null) {
                throw new ConvocatoriaInvalidaExcepcion("Cada pareja debe contar con exactamente 2 jugadores.");
            }

            if (!jugadoresConvocados.add(jugador1)) {
                throw new ConvocatoriaInvalidaExcepcion(
                        String.format("El jugador %s está repetido en más de una pareja de la misma jornada.", jugador1)
                );
            }
            if (!jugadoresConvocados.add(jugador2)) {
                throw new ConvocatoriaInvalidaExcepcion(
                        String.format("El jugador %s está repetido en más de una pareja de la misma jornada.", jugador2)
                );
            }
        }

        if (jugadoresConvocados.size() != 6) {
            throw new ConvocatoriaInvalidaExcepcion(
                    String.format("La convocatoria debe incluir exactamente 6 jugadores únicos; recibidos: %d.", jugadoresConvocados.size())
            );
        }

        Optional<PropuestaFecha> propuestaAcordada = this.propuestasFecha.stream()
                .filter(PropuestaFecha::esSeleccionada)
                .findFirst();

        for (UUID jugadorId : jugadoresConvocados) {
            if (!tieneDisponibilidadConfirmadaParaFechaAcordada(jugadorId, propuestaAcordada.orElse(null))) {
                throw new ConvocatoriaInvalidaExcepcion(
                        String.format("El jugador %s no tiene disponibilidad DISPONIBLE registrada para la fecha acordada.", jugadorId)
                );
            }
        }

        List<PartidoPareja> parejasOrdenadas = listaTresParejas.stream()
                .sorted(Comparator.comparingInt(PartidoPareja::obtenerNumeroPareja))
                .toList();

        return new Enfrentamiento(
                this.id,
                this.divisionId,
                this.equipoLocalId,
                this.equipoVisitanteId,
                this.numeroJornada,
                EstadoEnfrentamiento.ALINEACION_CERRADA,
                this.fechaDefinitiva,
                this.pistas,
                this.sedeAlternativaJornada,
                this.urlFotoActa,
                this.puntosLocal,
                this.puntosVisitante,
                this.actaConfirmada,
                this.propuestasFecha,
                this.disponibilidades,
                parejasOrdenadas
        );
    }

    /**
     * Registra el acta arbitral del enfrentamiento:
     * - Verifica que el enfrentamiento tenga su alineación cerrada (ALINEACION_CERRADA o EN_DISPUTA) y 3 parejas asignadas.
     * - Valida la foto del acta y el tanteo reglamentario de cada uno de los 3 partidos.
     * - Determina el ganador de cada partido, computa los puntos y pasa el estado a FINALIZADO_CONFIRMADO.
     */
    public Enfrentamiento registrarActa(String nuevaUrlFotoActa, List<ResultadoPartido> resultadosTresPartidos) {
        if (this.estado != EstadoEnfrentamiento.ALINEACION_CERRADA && this.estado != EstadoEnfrentamiento.EN_DISPUTA) {
            throw new ReglaNegocioExcepcion(
                    String.format("Solo se puede registrar el acta cuando el enfrentamiento tiene la alineación cerrada (estado actual: %s).",
                            this.estado)
            );
        }

        if (nuevaUrlFotoActa == null || nuevaUrlFotoActa.isBlank()) {
            throw new ReglaNegocioExcepcion("La URL de la foto del acta es obligatoria para registrar el resultado.");
        }

        if (this.partidos == null || this.partidos.size() != 3) {
            throw new ReglaNegocioExcepcion("El enfrentamiento no dispone de los 3 partidos alineados para registrar el acta.");
        }

        if (resultadosTresPartidos == null || resultadosTresPartidos.size() != 3) {
            throw new ResultadoPadelIlegalExcepcion("Se deben enviar los resultados reglamentarios de los 3 partidos (Pareja 1, 2 y 3).");
        }

        Map<Integer, ResultadoPartido> resultadosPorPareja = new HashMap<>();
        for (ResultadoPartido resultado : resultadosTresPartidos) {
            if (resultado == null) {
                throw new ResultadoPadelIlegalExcepcion("Ninguno de los resultados de partido puede ser nulo.");
            }
            if (resultadosPorPareja.put(resultado.numeroPareja(), resultado) != null) {
                throw new ResultadoPadelIlegalExcepcion(
                        String.format("El resultado para la pareja %d está duplicado en el acta.", resultado.numeroPareja())
                );
            }
        }

        List<PartidoPareja> partidosActualizados = new ArrayList<>();
        int victoriasLocal = 0;
        int victoriasVisitante = 0;

        for (PartidoPareja partidoAlineado : this.partidos.stream()
                .sorted(Comparator.comparingInt(PartidoPareja::obtenerNumeroPareja))
                .toList()) {

            ResultadoPartido resultado = resultadosPorPareja.get(partidoAlineado.obtenerNumeroPareja());
            if (resultado == null) {
                throw new ResultadoPadelIlegalExcepcion(
                        String.format("Falta el resultado correspondiente a la pareja %d.", partidoAlineado.obtenerNumeroPareja())
                );
            }

            PartidoPareja partidoCompletado = partidoAlineado.registrarResultado(
                    resultado.set1Local(),
                    resultado.set1Visitante(),
                    resultado.set2Local(),
                    resultado.set2Visitante(),
                    resultado.set3Local(),
                    resultado.set3Visitante()
            );

            if (partidoCompletado.obtenerParejaGanadora() == GanadorPareja.LOCAL) {
                victoriasLocal++;
            } else {
                victoriasVisitante++;
            }

            partidosActualizados.add(partidoCompletado);
        }

        return new Enfrentamiento(
                this.id,
                this.divisionId,
                this.equipoLocalId,
                this.equipoVisitanteId,
                this.numeroJornada,
                EstadoEnfrentamiento.FINALIZADO_CONFIRMADO,
                this.fechaDefinitiva,
                this.pistas,
                this.sedeAlternativaJornada,
                nuevaUrlFotoActa.trim(),
                victoriasLocal,
                victoriasVisitante,
                true,
                this.propuestasFecha,
                this.disponibilidades,
                partidosActualizados
        );
    }

    private boolean tieneDisponibilidadConfirmadaParaFechaAcordada(UUID jugadorId, PropuestaFecha propuestaAcordada) {
        Optional<DisponibilidadJugador> disponibilidadFinal = this.disponibilidades.stream()
                .filter(d -> d.obtenerJugadorId().equals(jugadorId) && d.esDisponibilidadFinal())
                .findFirst();

        if (disponibilidadFinal.isPresent()) {
            return disponibilidadFinal.get().obtenerEstado() == EstadoDisponibilidad.DISPONIBLE;
        }

        if (propuestaAcordada != null) {
            return this.disponibilidades.stream()
                    .filter(d -> d.obtenerJugadorId().equals(jugadorId)
                            && propuestaAcordada.obtenerId().equals(d.obtenerPropuestaFechaId()))
                    .anyMatch(d -> d.obtenerEstado() == EstadoDisponibilidad.DISPONIBLE);
        }

        return false;
    }

    /**
     * Valida la alineación completa (ambos equipos) según las reglas de Synagon.
     */
    public void validarAlineacion(List<PartidoPareja> partidosAlineacion) {
        if (partidosAlineacion == null || partidosAlineacion.size() != 3) {
            throw new ConvocatoriaInvalidaExcepcion("Un enfrentamiento debe contener exactamente 3 partidos de parejas (Pareja 1, Pareja 2 y Pareja 3).");
        }

        Set<Integer> numerosPareja = new HashSet<>();
        Set<UUID> jugadoresLocales = new HashSet<>();
        Set<UUID> jugadoresVisitantes = new HashSet<>();

        for (PartidoPareja partido : partidosAlineacion) {
            if (!this.id.equals(partido.obtenerEnfrentamientoId())) {
                throw new ConvocatoriaInvalidaExcepcion("Uno de los partidos no pertenece a este enfrentamiento.");
            }

            int numero = partido.obtenerNumeroPareja();
            if (!numerosPareja.add(numero) || numero < 1 || numero > 3) {
                throw new ConvocatoriaInvalidaExcepcion("Los partidos deben cubrir exactamente las parejas 1, 2 y 3 sin duplicados.");
            }

            if (partido.obtenerJugadorLocal1Id() == null || partido.obtenerJugadorLocal2Id() == null
                    || partido.obtenerJugadorVisitante1Id() == null || partido.obtenerJugadorVisitante2Id() == null) {
                throw new ConvocatoriaInvalidaExcepcion("Ambos equipos deben tener sus parejas completas en los 3 partidos.");
            }

            if (!jugadoresLocales.add(partido.obtenerJugadorLocal1Id())) {
                throw new ConvocatoriaInvalidaExcepcion(
                        String.format("El jugador local %s está alineado en más de una pareja. Un jugador no puede repetirse en la jornada.",
                                partido.obtenerJugadorLocal1Id())
                );
            }
            if (!jugadoresLocales.add(partido.obtenerJugadorLocal2Id())) {
                throw new ConvocatoriaInvalidaExcepcion(
                        String.format("El jugador local %s está alineado en más de una pareja. Un jugador no puede repetirse en la jornada.",
                                partido.obtenerJugadorLocal2Id())
                );
            }

            if (!jugadoresVisitantes.add(partido.obtenerJugadorVisitante1Id())) {
                throw new ConvocatoriaInvalidaExcepcion(
                        String.format("El jugador visitante %s está alineado en más de una pareja. Un jugador no puede repetirse en la jornada.",
                                partido.obtenerJugadorVisitante1Id())
                );
            }
            if (!jugadoresVisitantes.add(partido.obtenerJugadorVisitante2Id())) {
                throw new ConvocatoriaInvalidaExcepcion(
                        String.format("El jugador visitante %s está alineado en más de una pareja. Un jugador no puede repetirse en la jornada.",
                                partido.obtenerJugadorVisitante2Id())
                );
            }
        }

        if (jugadoresLocales.size() != 6) {
            throw new ConvocatoriaInvalidaExcepcion(
                    String.format("El equipo local debe convocar exactamente a 6 jugadores únicos; convocados: %d.", jugadoresLocales.size())
            );
        }

        if (jugadoresVisitantes.size() != 6) {
            throw new ConvocatoriaInvalidaExcepcion(
                    String.format("El equipo visitante debe convocar exactamente a 6 jugadores únicos; convocados: %d.", jugadoresVisitantes.size())
            );
        }

        Set<UUID> interseccion = new HashSet<>(jugadoresLocales);
        interseccion.retainAll(jugadoresVisitantes);
        if (!interseccion.isEmpty()) {
            throw new ConvocatoriaInvalidaExcepcion("Un jugador no puede figurar en ambos equipos durante el mismo enfrentamiento.");
        }
    }

    /**
     * Asigna las alineaciones completas de ambos equipos al enfrentamiento y cambia su estado a EN_DISPUTA.
     */
    public Enfrentamiento asignarAlineaciones(List<PartidoPareja> partidosAlineacion) {
        validarAlineacion(partidosAlineacion);

        return new Enfrentamiento(
                this.id,
                this.divisionId,
                this.equipoLocalId,
                this.equipoVisitanteId,
                this.numeroJornada,
                EstadoEnfrentamiento.EN_DISPUTA,
                this.fechaDefinitiva,
                this.pistas,
                this.sedeAlternativaJornada,
                this.urlFotoActa,
                this.puntosLocal,
                this.puntosVisitante,
                this.actaConfirmada,
                this.propuestasFecha,
                this.disponibilidades,
                partidosAlineacion
        );
    }

    /**
     * Confirma el acta arbitral del enfrentamiento (con alineaciones completas de ambos equipos).
     */
    public Enfrentamiento confirmarActa(List<PartidoPareja> partidosFinalizados) {
        validarAlineacion(partidosFinalizados);

        int victoriasLocal = 0;
        int victoriasVisitante = 0;

        for (PartidoPareja partido : partidosFinalizados) {
            if (!partido.estaCompletado()) {
                throw new ResultadoPadelIlegalExcepcion("Todos los partidos deben estar completados para confirmar el acta.");
            }
            partido.validarResultadoReglamentario();
            GanadorPareja ganador = partido.obtenerParejaGanadora() != null
                    ? partido.obtenerParejaGanadora()
                    : partido.determinarGanador();

            if (ganador == GanadorPareja.LOCAL) {
                victoriasLocal++;
            } else {
                victoriasVisitante++;
            }
        }

        return new Enfrentamiento(
                this.id,
                this.divisionId,
                this.equipoLocalId,
                this.equipoVisitanteId,
                this.numeroJornada,
                EstadoEnfrentamiento.FINALIZADO,
                this.fechaDefinitiva,
                this.pistas,
                this.sedeAlternativaJornada,
                this.urlFotoActa,
                victoriasLocal,
                victoriasVisitante,
                true,
                this.propuestasFecha,
                this.disponibilidades,
                partidosFinalizados
        );
    }

    /**
     * Propiedad calculada que devuelve la sede alternativa de la jornada si no está vacía,
     * o la sede oficial (pistas) en caso contrario.
     */
    public String obtenerSedeEfectivaJuego() {
        if (this.sedeAlternativaJornada != null && !this.sedeAlternativaJornada.isBlank()) {
            return this.sedeAlternativaJornada;
        }
        return this.pistas != null ? this.pistas : "";
    }

    public UUID obtenerId() { return id; }
    public UUID obtenerDivisionId() { return divisionId; }
    public UUID obtenerEquipoLocalId() { return equipoLocalId; }
    public UUID obtenerEquipoVisitanteId() { return equipoVisitanteId; }
    public int obtenerNumeroJornada() { return numeroJornada; }
    public EstadoEnfrentamiento obtenerEstado() { return estado; }
    public LocalDateTime obtenerFechaDefinitiva() { return fechaDefinitiva; }
    public String obtenerPistas() { return pistas; }
    public String obtenerSedeAlternativaJornada() { return sedeAlternativaJornada; }
    public String obtenerUrlFotoActa() { return urlFotoActa; }
    public int obtenerPuntosLocal() { return puntosLocal; }
    public int obtenerPuntosVisitante() { return puntosVisitante; }
    public boolean esActaConfirmada() { return actaConfirmada; }
    public List<PropuestaFecha> obtenerPropuestasFecha() { return propuestasFecha; }
    public List<DisponibilidadJugador> obtenerDisponibilidades() { return disponibilidades; }
    public List<PartidoPareja> obtenerPartidos() { return partidos; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Enfrentamiento that = (Enfrentamiento) o;
        return numeroJornada == that.numeroJornada &&
                puntosLocal == that.puntosLocal &&
                puntosVisitante == that.puntosVisitante &&
                actaConfirmada == that.actaConfirmada &&
                Objects.equals(id, that.id) &&
                Objects.equals(divisionId, that.divisionId) &&
                Objects.equals(equipoLocalId, that.equipoLocalId) &&
                Objects.equals(equipoVisitanteId, that.equipoVisitanteId) &&
                estado == that.estado;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, divisionId, equipoLocalId, equipoVisitanteId, numeroJornada,
                estado, puntosLocal, puntosVisitante, actaConfirmada);
    }

    @Override
    public String toString() {
        return "Enfrentamiento{" +
                "id=" + id +
                ", divisionId=" + divisionId +
                ", local=" + equipoLocalId +
                ", visitante=" + equipoVisitanteId +
                ", jornada=" + numeroJornada +
                ", estado=" + estado +
                ", sedeEfectiva='" + obtenerSedeEfectivaJuego() + '\'' +
                ", puntos=" + puntosLocal + "-" + puntosVisitante +
                ", actaConfirmada=" + actaConfirmada +
                '}';
    }
}
