package com.synagon.api.infraestructura.adaptadores.entrada.rest;

import com.synagon.api.dominio.modelos.Liga;
import com.synagon.api.dominio.modelos.ResultadoAltaJugadorEquipo;
import com.synagon.api.dominio.modelos.ResumenMembresiaUsuario;
import com.synagon.api.dominio.modelos.Usuario;
import com.synagon.api.dominio.puertos.entrada.AgoraYLigasCasoUso;
import com.synagon.api.dominio.puertos.entrada.RegistroUsuarioCasoUso;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Controlador REST de entrada para el Ágora Principal (Home Multi-Rol), validación de códigos de invitación,
 * inscripción de equipos, alta de jugadores por capitán, primer acceso de contraseña y actualización de perfil.
 */
@RestController
public class ControladorAgora {

    private final AgoraYLigasCasoUso agoraYLigasCasoUso;
    private final RegistroUsuarioCasoUso registroUsuarioCasoUso;

    public ControladorAgora(
            AgoraYLigasCasoUso agoraYLigasCasoUso,
            RegistroUsuarioCasoUso registroUsuarioCasoUso
    ) {
        this.agoraYLigasCasoUso = agoraYLigasCasoUso;
        this.registroUsuarioCasoUso = registroUsuarioCasoUso;
    }

    @GetMapping("/api/v1/agora/usuarios/{usuarioId}/membresias")
    public ResponseEntity<List<RespuestaMembresiaUsuarioDto>> obtenerMembresiasUsuario(
            @PathVariable UUID usuarioId
    ) {
        List<RespuestaMembresiaUsuarioDto> respuesta = agoraYLigasCasoUso
                .obtenerMembresiasUsuario(usuarioId)
                .stream()
                .map(RespuestaMembresiaUsuarioDto::desdeDominio)
                .toList();
        return ResponseEntity.ok(respuesta);
    }

    @GetMapping("/api/v1/agora/ligas/validar-codigo/{codigo}")
    public ResponseEntity<RespuestaValidacionCodigoLigaDto> validarCodigoInvitacionLiga(
            @PathVariable("codigo") String codigo
    ) {
        Liga liga = agoraYLigasCasoUso.validarCodigoInvitacionLiga(codigo);
        return ResponseEntity.ok(RespuestaValidacionCodigoLigaDto.desdeDominio(liga));
    }

    @PostMapping("/api/v1/agora/equipos/inscribir")
    public ResponseEntity<RespuestaMembresiaUsuarioDto> inscribirEquipoConCodigo(
            @Valid @RequestBody PeticionInscribirEquipoDto peticion
    ) {
        ResumenMembresiaUsuario nuevaMembresia = agoraYLigasCasoUso.inscribirEquipoConCodigo(
                peticion.usuarioId(),
                peticion.codigoInvitacion(),
                peticion.nombreEquipo(),
                peticion.clubSedeOpcional()
        );
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(RespuestaMembresiaUsuarioDto.desdeDominio(nuevaMembresia));
    }

    @PostMapping("/api/v1/agora/equipos/{equipoId}/jugadores")
    public ResponseEntity<RespuestaAltaJugadorEquipoDto> darDeAltaJugadorPorCapitan(
            @PathVariable UUID equipoId,
            @Valid @RequestBody PeticionAltaJugadorCapitanDto peticion
    ) {
        ResultadoAltaJugadorEquipo resultado = registroUsuarioCasoUso.registrarJugadorPorCapitan(
                equipoId,
                peticion.capitanId(),
                peticion.email(),
                peticion.dni(),
                peticion.nombreCompleto(),
                peticion.telefono()
        );
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(RespuestaAltaJugadorEquipoDto.desdeDominio(resultado));
    }

    @PostMapping("/api/v1/usuarios/{usuarioId}/primer-acceso-password")
    public ResponseEntity<RespuestaPerfilUsuarioDto> gestionarPrimerAccesoPassword(
            @PathVariable UUID usuarioId,
            @Valid @RequestBody PeticionPrimerAccesoPasswordDto peticion
    ) {
        Usuario actualizado = registroUsuarioCasoUso.gestionarPrimerAccesoPassword(
                usuarioId,
                Boolean.TRUE.equals(peticion.deseaCambiar()),
                peticion.nuevaPasswordOpcional()
        );
        String mensaje = Boolean.TRUE.equals(peticion.deseaCambiar())
                ? "Contraseña actualizada correctamente en el primer acceso."
                : "Primer acceso completado conservando la contraseña actual.";
        return ResponseEntity.ok(RespuestaPerfilUsuarioDto.desdeDominio(actualizado, mensaje));
    }

    @PutMapping("/api/v1/usuarios/{usuarioId}/perfil")
    public ResponseEntity<RespuestaPerfilUsuarioDto> actualizarPerfilUsuario(
            @PathVariable UUID usuarioId,
            @Valid @RequestBody PeticionActualizarPerfilUsuarioDto peticion
    ) {
        Usuario actualizado = registroUsuarioCasoUso.actualizarPerfilUsuario(
                usuarioId,
                peticion.nombreCompleto(),
                peticion.telefono(),
                peticion.dni()
        );
        return ResponseEntity.ok(
                RespuestaPerfilUsuarioDto.desdeDominio(actualizado, "Perfil de usuario actualizado correctamente.")
        );
    }
}
