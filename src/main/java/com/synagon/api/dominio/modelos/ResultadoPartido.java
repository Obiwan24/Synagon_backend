package com.synagon.api.dominio.modelos;

import com.synagon.api.dominio.excepciones.ResultadoPadelIlegalExcepcion;

/**
 * Objeto de valor inmutable del dominio que encapsula el resultado en sets reportado
 * en el acta para uno de los 3 partidos del enfrentamiento.
 */
public record ResultadoPartido(
        int numeroPareja,
        int set1Local,
        int set1Visitante,
        int set2Local,
        int set2Visitante,
        Integer set3Local,
        Integer set3Visitante
) {
    public ResultadoPartido {
        if (numeroPareja < 1 || numeroPareja > 3) {
            throw new ResultadoPadelIlegalExcepcion("El número de pareja del partido debe ser 1, 2 o 3.");
        }
        ValidadorTanteoPadel.validarPartido(
                set1Local,
                set1Visitante,
                set2Local,
                set2Visitante,
                set3Local,
                set3Visitante
        );
    }

    public GanadorPareja determinarGanador() {
        return ValidadorTanteoPadel.determinarGanador(
                set1Local,
                set1Visitante,
                set2Local,
                set2Visitante,
                set3Local,
                set3Visitante
        );
    }
}
