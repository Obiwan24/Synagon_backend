package com.synagon.api.dominio.modelos;

import com.synagon.api.dominio.excepciones.ResultadoPadelIlegalExcepcion;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas de ValidadorTanteoPadel")
class ValidadorTanteoPadelTest {

    @ParameterizedTest(name = "Set reglamentario: {0}-{1}")
    @CsvSource({
            "6, 0", "6, 1", "6, 2", "6, 3", "6, 4",
            "0, 6", "1, 6", "2, 6", "3, 6", "4, 6",
            "7, 5", "5, 7",
            "7, 6", "6, 7"
    })
    void debeValidarSetsReglamentarios(int local, int visitante) {
        assertDoesNotThrow(() -> ValidadorTanteoPadel.validarSet(1, local, visitante));
    }

    @ParameterizedTest(name = "Set ilegal lanza ResultadoPadelIlegalExcepcion: {0}-{1}")
    @CsvSource({
            "6, 5", "5, 6",
            "6, 6",
            "7, 4", "4, 7",
            "7, 0", "0, 7",
            "8, 6", "6, 8",
            "5, 3", "3, 5",
            "-1, 6"
    })
    void debeLanzarExcepcionEnSetsIlegales(int local, int visitante) {
        assertThrows(ResultadoPadelIlegalExcepcion.class, () ->
                ValidadorTanteoPadel.validarSet(1, local, visitante)
        );
    }

    @Test
    @DisplayName("Partido ganado 2-0 determina ganador correctamente y rechaza 3er set")
    void debeDeterminarGanadorEnDosSetsYRechazarTercerSet() {
        assertEquals(
                GanadorPareja.LOCAL,
                ValidadorTanteoPadel.determinarGanador(6, 4, 7, 5, null, null)
        );
        assertEquals(
                GanadorPareja.VISITANTE,
                ValidadorTanteoPadel.determinarGanador(6, 7, 3, 6, null, null)
        );

        assertThrows(ResultadoPadelIlegalExcepcion.class, () ->
                ValidadorTanteoPadel.validarPartido(6, 4, 6, 2, 6, 3)
        );
    }

    @Test
    @DisplayName("Partido empatado 1-1 exige 3er set reglamentario")
    void debeExigirTercerSetCuandoHayEmpateUnoAUno() {
        assertThrows(ResultadoPadelIlegalExcepcion.class, () ->
                ValidadorTanteoPadel.validarPartido(6, 4, 4, 6, null, null)
        );

        assertEquals(
                GanadorPareja.VISITANTE,
                ValidadorTanteoPadel.determinarGanador(6, 4, 4, 6, 6, 7)
        );
    }
}
