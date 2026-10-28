package com.synagon.api.dominio.excepciones;

/**
 * Excepción lanzada cuando se intenta ejecutar una operación que infringe una regla de negocio o estado del dominio.
 */
public class ReglaNegocioExcepcion extends RuntimeException {

    public ReglaNegocioExcepcion(String mensaje) {
        super(mensaje);
    }

    public ReglaNegocioExcepcion(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
