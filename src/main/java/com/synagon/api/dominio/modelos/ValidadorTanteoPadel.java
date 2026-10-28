package com.synagon.api.dominio.modelos;

import com.synagon.api.dominio.excepciones.ResultadoPadelIlegalExcepcion;

/**
 * Validador puro de dominio para el reglamento oficial de tanteo en pádel:
 * - Cada set se gana con 6 juegos y diferencia mínima de 2 (6-0 a 6-4), o bien 7-5 o 7-6 (tie-break).
 * - Un partido se juega al mejor de 3 sets (gana la primera pareja en alcanzar 2 sets).
 * - El 3er set solo debe existir si hay empate 1-1 tras los dos primeros sets.
 */
public final class ValidadorTanteoPadel {

    private ValidadorTanteoPadel() {
        // Clase de utilidad de dominio
    }

    /**
     * Comprueba sin lanzar excepción si la puntuación de un set individual es reglamentaria.
     */
    public static boolean esSetReglamentario(int juegosLocal, int juegosVisitante) {
        if (juegosLocal < 0 || juegosVisitante < 0) {
            return false;
        }
        int ganador = Math.max(juegosLocal, juegosVisitante);
        int perdedor = Math.min(juegosLocal, juegosVisitante);

        if (ganador == 6) {
            return perdedor <= 4;
        }
        if (ganador == 7) {
            return perdedor == 5 || perdedor == 6;
        }
        return false;
    }

    /**
     * Valida un set individual lanzando {@link ResultadoPadelIlegalExcepcion} si incumple el reglamento.
     */
    public static void validarSet(int numeroSet, Integer juegosLocal, Integer juegosVisitante) {
        if (juegosLocal == null || juegosVisitante == null) {
            throw new ResultadoPadelIlegalExcepcion(
                    String.format("El set %d es obligatorio y no contiene puntuación completa.", numeroSet)
            );
        }
        if (!esSetReglamentario(juegosLocal, juegosVisitante)) {
            throw new ResultadoPadelIlegalExcepcion(
                    String.format("El resultado del set %d (%d-%d) no es reglamentario. Debe ganarse con 6 juegos y diferencia mínima de 2 (6-0 a 6-4), o bien 7-5 o 7-6.",
                            numeroSet, juegosLocal, juegosVisitante)
            );
        }
    }

    /**
     * Valida el resultado completo de un partido al mejor de 3 sets.
     * Lanza {@link ResultadoPadelIlegalExcepcion} ante cualquier combinación ilegal.
     */
    public static void validarPartido(
            Integer set1Local,
            Integer set1Visitante,
            Integer set2Local,
            Integer set2Visitante,
            Integer set3Local,
            Integer set3Visitante
    ) {
        validarSet(1, set1Local, set1Visitante);
        validarSet(2, set2Local, set2Visitante);

        boolean localGanaSet1 = set1Local > set1Visitante;
        boolean localGanaSet2 = set2Local > set2Visitante;

        if (localGanaSet1 == localGanaSet2) {
            if (set3Local != null || set3Visitante != null) {
                throw new ResultadoPadelIlegalExcepcion(
                        "El partido ya fue decidido 2-0 en los dos primeros sets; el 3er set no debe existir."
                );
            }
        } else {
            if (set3Local == null || set3Visitante == null) {
                throw new ResultadoPadelIlegalExcepcion(
                        "Existe empate 1-1 tras los dos primeros sets; es obligatorio registrar un 3er set reglamentario."
                );
            }
            validarSet(3, set3Local, set3Visitante);
        }
    }

    /**
     * Comprueba sin lanzar excepción si el resultado global en sets es reglamentario.
     */
    public static boolean esPartidoReglamentario(
            Integer set1Local,
            Integer set1Visitante,
            Integer set2Local,
            Integer set2Visitante,
            Integer set3Local,
            Integer set3Visitante
    ) {
        try {
            validarPartido(set1Local, set1Visitante, set2Local, set2Visitante, set3Local, set3Visitante);
            return true;
        } catch (ResultadoPadelIlegalExcepcion e) {
            return false;
        }
    }

    /**
     * Determina la pareja ganadora tras validar reglamentariamente el tanteo.
     */
    public static GanadorPareja determinarGanador(
            Integer set1Local,
            Integer set1Visitante,
            Integer set2Local,
            Integer set2Visitante,
            Integer set3Local,
            Integer set3Visitante
    ) {
        validarPartido(set1Local, set1Visitante, set2Local, set2Visitante, set3Local, set3Visitante);
        int setsLocal = 0;
        int setsVisitante = 0;

        if (set1Local > set1Visitante) setsLocal++; else setsVisitante++;
        if (set2Local > set2Visitante) setsLocal++; else setsVisitante++;
        if (set3Local != null && set3Visitante != null) {
            if (set3Local > set3Visitante) setsLocal++; else setsVisitante++;
        }

        return setsLocal > setsVisitante ? GanadorPareja.LOCAL : GanadorPareja.VISITANTE;
    }
}
