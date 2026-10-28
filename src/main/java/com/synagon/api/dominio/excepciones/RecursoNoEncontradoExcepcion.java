package com.synagon.api.dominio.excepciones;

/**
 * Excepción lanzada cuando no se encuentra una entidad o recurso solicitado en el dominio.
 */
public class RecursoNoEncontradoExcepcion extends RuntimeException {

    public RecursoNoEncontradoExcepcion(String mensaje) {
        super(mensaje);
    }

    public RecursoNoEncontradoExcepcion(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
