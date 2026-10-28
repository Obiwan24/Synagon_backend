package com.synagon.api.infraestructura.adaptadores.entrada.rest;

import com.synagon.api.dominio.modelos.Enfrentamiento;
import com.synagon.api.dominio.modelos.ResultadoPartido;
import com.synagon.api.dominio.puertos.entrada.ActasYEstadisticasCasoUso;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Controlador REST para el registro de actas arbitrales, consulta en tiempo real
 * de la tabla de clasificación de cada división y estadísticas individuales de jugadores por equipo.
 */
@RestController
public class ControladorActasYEstadisticas {

    private final ActasYEstadisticasCasoUso actasYEstadisticasCasoUso;

    public ControladorActasYEstadisticas(ActasYEstadisticasCasoUso actasYEstadisticasCasoUso) {
        this.actasYEstadisticasCasoUso = actasYEstadisticasCasoUso;
    }

    @RequestMapping(
            value = "/api/enfrentamientos/{id}/acta",
            method = {RequestMethod.POST, RequestMethod.PUT}
    )
    public ResponseEntity<RespuestaEnfrentamiento> registrarActaEnfrentamiento(
            @PathVariable("id") UUID enfrentamientoId,
            @Valid @RequestBody PeticionRegistrarActa peticion
    ) {
        List<ResultadoPartido> resultadosDominio = peticion.resultadosTresPartidos().stream()
                .map(r -> new ResultadoPartido(
                        r.numeroPareja(),
                        r.set1Local(),
                        r.set1Visitante(),
                        r.set2Local(),
                        r.set2Visitante(),
                        r.set3Local(),
                        r.set3Visitante()
                ))
                .toList();

        Enfrentamiento actualizado = actasYEstadisticasCasoUso.registrarActaEnfrentamiento(
                enfrentamientoId,
                peticion.urlFotoActa(),
                resultadosDominio
        );

        return ResponseEntity.ok(RespuestaEnfrentamiento.desdeDominio(actualizado));
    }

    @GetMapping("/api/divisiones/{id}/clasificacion")
    public ResponseEntity<List<RespuestaPosicionClasificacion>> obtenerClasificacionDivision(
            @PathVariable("id") UUID divisionId
    ) {
        List<RespuestaPosicionClasificacion> respuesta = actasYEstadisticasCasoUso
                .obtenerClasificacionDivision(divisionId)
                .stream()
                .map(RespuestaPosicionClasificacion::desdeDominio)
                .toList();

        return ResponseEntity.ok(respuesta);
    }

    @GetMapping("/api/equipos/{id}/estadisticas")
    public ResponseEntity<List<RespuestaEstadisticaJugador>> obtenerEstadisticasJugadoresEquipo(
            @PathVariable("id") UUID equipoId
    ) {
        List<RespuestaEstadisticaJugador> respuesta = actasYEstadisticasCasoUso
                .obtenerEstadisticasJugadoresEquipo(equipoId)
                .stream()
                .map(RespuestaEstadisticaJugador::desdeDominio)
                .toList();

        return ResponseEntity.ok(respuesta);
    }
}
