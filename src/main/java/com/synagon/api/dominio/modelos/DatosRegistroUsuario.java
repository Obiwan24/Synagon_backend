package com.synagon.api.dominio.modelos;

/**
 * Datos de entrada del dominio para registrar un nuevo usuario jugador en el sistema.
 */
public record DatosRegistroUsuario(
        String nombreCompleto,
        String email,
        String telefono,
        String contrasenaEnClaro,
        Boolean aceptoPrivacidad
) {
}
