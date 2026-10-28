package com.synagon.api.dominio.excepciones;

/**
 * Excepción lanzada cuando una convocatoria o alineación de parejas no cumple las reglas de la liga.
 */
public class ConvocatoriaInvalidaExcepcion extends RuntimeException {

    public ConvocatoriaInvalidaExcepcion(String mensaje) {
        super(mensaje);
    }

    public ConvocatoriaInvalidaExcepcion(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
