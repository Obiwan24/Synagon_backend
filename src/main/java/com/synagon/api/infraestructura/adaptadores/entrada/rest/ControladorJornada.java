package com.synagon.api.infraestructura.adaptadores.entrada.rest;

import com.synagon.api.dominio.modelos.Enfrentamiento;
import com.synagon.api.dominio.modelos.PartidoPareja;
import com.synagon.api.dominio.puertos.entrada.GestionJornadaCasoUso;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Controlador REST de entrada para la gestión de enfrentamientos, fechas propuestas,
 * votaciones de disponibilidad y convocatorias/alineaciones de cada jornada.
 */
@RestController
@RequestMapping("/api/v1/jornadas/enfrentamientos")
public class ControladorJornada {

    private final GestionJornadaCasoUso gestionJornadaCasoUso;

    public ControladorJornada(GestionJornadaCasoUso gestionJornadaCasoUso) {
        this.gestionJornadaCasoUso = gestionJornadaCasoUso;
    }

    @GetMapping("/{enfrentamientoId}")
    public ResponseEntity<RespuestaEnfrentamiento> obtenerEnfrentamiento(
            @PathVariable UUID enfrentamientoId
    ) {
        Enfrentamiento enfrentamiento = gestionJornadaCasoUso.obtenerEnfrentamientoPorId(enfrentamientoId);
        return ResponseEntity.ok(RespuestaEnfrentamiento.desdeDominio(enfrentamiento));
    }

    @PostMapping("/{enfrentamientoId}/propuestas-fecha")
    public ResponseEntity<RespuestaEnfrentamiento> proponerFechas(
            @PathVariable UUID enfrentamientoId,
            @Valid @RequestBody PeticionProponerFechas peticion
    ) {
        Enfrentamiento actualizado = gestionJornadaCasoUso.proponerFechas(
                enfrentamientoId,
                peticion.fechas()
        );
        return ResponseEntity.ok(RespuestaEnfrentamiento.desdeDominio(actualizado));
    }

    @PostMapping("/{enfrentamientoId}/disponibilidades")
    public ResponseEntity<RespuestaEnfrentamiento> votarDisponibilidad(
            @PathVariable UUID enfrentamientoId,
            @Valid @RequestBody PeticionVotarDisponibilidad peticion
    ) {
        Enfrentamiento actualizado = gestionJornadaCasoUso.votarDisponibilidad(
                enfrentamientoId,
                peticion.jugadorId(),
                peticion.propuestaFechaId(),
                peticion.tipoDisponibilidad()
        );
        return ResponseEntity.ok(RespuestaEnfrentamiento.desdeDominio(actualizado));
    }

    @PutMapping("/{enfrentamientoId}/fecha-definitiva")
    public ResponseEntity<RespuestaEnfrentamiento> fijarFechaDefinitiva(
            @PathVariable UUID enfrentamientoId,
            @Valid @RequestBody PeticionFijarFechaDefinitiva peticion
    ) {
        Enfrentamiento actualizado = gestionJornadaCasoUso.fijarFechaDefinitiva(
                enfrentamientoId,
                peticion.propuestaFechaElegidaId()
        );
        return ResponseEntity.ok(RespuestaEnfrentamiento.desdeDominio(actualizado));
    }

    @PostMapping("/{enfrentamientoId}/alineacion")
    public ResponseEntity<RespuestaEnfrentamiento> cerrarConvocatoriaYAlineacion(
            @PathVariable UUID enfrentamientoId,
            @Valid @RequestBody PeticionCerrarConvocatoria peticion
    ) {
        List<PartidoPareja> listaTresParejas = peticion.parejas().stream()
                .map(p -> PartidoPareja.crearParejaConvocada(
                        enfrentamientoId,
                        p.numeroPareja(),
                        p.jugador1Id(),
                        p.jugador2Id()
                ))
                .toList();

        Enfrentamiento actualizado = gestionJornadaCasoUso.cerrarConvocatoriaYAlineacion(
                enfrentamientoId,
                listaTresParejas
        );
        return ResponseEntity.ok(RespuestaEnfrentamiento.desdeDominio(actualizado));
    }
}
