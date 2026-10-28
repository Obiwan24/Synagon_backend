package com.synagon.api.dominio.excepciones;

/**
 * Excepción lanzada cuando un tanteo o resultado de pádel no cumple con el reglamento oficial.
 */
public class ResultadoPadelIlegalExcepcion extends RuntimeException {

    public ResultadoPadelIlegalExcepcion(String mensaje) {
        super(mensaje);
    }

    public ResultadoPadelIlegalExcepcion(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
