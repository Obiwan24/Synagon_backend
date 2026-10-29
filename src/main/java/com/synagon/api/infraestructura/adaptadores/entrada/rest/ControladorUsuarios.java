package com.synagon.api.infraestructura.adaptadores.entrada.rest;

import com.synagon.api.dominio.modelos.DatosRegistroUsuario;
import com.synagon.api.dominio.modelos.Usuario;
import com.synagon.api.dominio.puertos.entrada.RegistroUsuarioCasoUso;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST de entrada para el registro público y gestión de acceso de usuarios.
 */
@RestController
@RequestMapping("/api/v1/usuarios")
public class ControladorUsuarios {

    private final RegistroUsuarioCasoUso registroUsuarioCasoUso;

    public ControladorUsuarios(RegistroUsuarioCasoUso registroUsuarioCasoUso) {
        this.registroUsuarioCasoUso = registroUsuarioCasoUso;
    }

    @PostMapping("/registro")
    public ResponseEntity<RegistroRespuestaDto> registrarUsuario(
            @Valid @RequestBody RegistroPeticionDto peticion
    ) {
        DatosRegistroUsuario datosRegistro = new DatosRegistroUsuario(
                peticion.nombreCompleto(),
                peticion.email(),
                peticion.telefono(),
                peticion.password(),
                peticion.aceptoPrivacidad()
        );

        Usuario usuarioRegistrado = registroUsuarioCasoUso.registrarUsuario(datosRegistro);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(RegistroRespuestaDto.desdeDominio(usuarioRegistrado));
    }
}
