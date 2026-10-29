package com.synagon.api.dominio.excepciones;

/**
 * Excepción de dominio lanzada cuando se intenta registrar un usuario con un correo electrónico
 * que ya existe en el sistema.
 */
public class EmailYaRegistradoExcepcion extends ReglaNegocioExcepcion {

    public EmailYaRegistradoExcepcion(String email) {
        super(String.format("El correo electrónico '%s' ya se encuentra registrado en Synagon.", email));
    }
}
