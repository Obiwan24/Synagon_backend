package com.synagon.api.dominio.modelos;

import com.synagon.api.dominio.excepciones.ResultadoPadelIlegalExcepcion;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas del Modelo PartidoPareja y Tanteo de Pádel")
class PartidoParejaTest {

    private final UUID enfrentamientoId = UUID.randomUUID();
    private final UUID local1 = UUID.randomUUID();
    private final UUID local2 = UUID.randomUUID();
    private final UUID visitante1 = UUID.randomUUID();
    private final UUID visitante2 = UUID.randomUUID();

    @Nested
    @DisplayName("Validación de Sets Individuales")
    class ValidacionSetsIndividuales {

        @ParameterizedTest(name = "Tanteo legal: {0}-{1}")
        @CsvSource({
                "6, 0", "6, 1", "6, 2", "6, 3", "6, 4",
                "0, 6", "1, 6", "2, 6", "3, 6", "4, 6",
                "7, 5", "5, 7",
                "7, 6", "6, 7"
        })
        void debeAceptarTanteosReglamentarios(int local, int visitante) {
            assertTrue(PartidoPareja.esSetReglamentario(local, visitante));
        }

        @ParameterizedTest(name = "Tanteo ilegal: {0}-{1}")
        @CsvSource({
                "6, 5", "5, 6",
                "6, 6",
                "5, 4", "4, 5",
                "7, 4", "4, 7",
                "7, 3", "3, 7",
                "8, 6", "6, 8",
                "9, 7",
                "-1, 6", "6, -1",
                "0, 0"
        })
        void debeRechazarTanteosNoReglamentarios(int local, int visitante) {
            assertFalse(PartidoPareja.esSetReglamentario(local, visitante));
        }
    }

    @Nested
    @DisplayName("Validación de Resultado Global del Partido (Mejor de 3 sets)")
    class ValidacionResultadoPartido {

        @Test
        @DisplayName("Victoria legal 2-0 para local sin tercer set")
        void debePermitirVictoria2CeroLocal() {
            PartidoPareja partido = PartidoPareja.crear(enfrentamientoId, 1, local1, local2, visitante1, visitante2);
            PartidoPareja completado = partido.registrarResultado(6, 4, 6, 2, null, null);

            assertTrue(completado.esResultadoReglamentario());
            assertEquals(GanadorPareja.LOCAL, completado.obtenerParejaGanadora());
            assertEquals(2, completado.calcularSetsGanadosLocal());
            assertEquals(0, completado.calcularSetsGanadosVisitante());
            assertEquals(12, completado.calcularJuegosGanadosLocal());
            assertEquals(6, completado.calcularJuegosGanadosVisitante());
        }

        @Test
        @DisplayName("Victoria legal 2-0 para visitante sin tercer set")
        void debePermitirVictoria2CeroVisitante() {
            PartidoPareja partido = PartidoPareja.crear(enfrentamientoId, 1, local1, local2, visitante1, visitante2);
            PartidoPareja completado = partido.registrarResultado(3, 6, 5, 7, null, null);

            assertTrue(completado.esResultadoReglamentario());
            assertEquals(GanadorPareja.VISITANTE, completado.obtenerParejaGanadora());
            assertEquals(0, completado.calcularSetsGanadosLocal());
            assertEquals(2, completado.calcularSetsGanadosVisitante());
        }

        @Test
        @DisplayName("Victoria legal 2-1 tras empate 1-1 y disputa del 3er set")
        void debePermitirVictoria2UnoConTercerSet() {
            PartidoPareja partido = PartidoPareja.crear(enfrentamientoId, 1, local1, local2, visitante1, visitante2);
            PartidoPareja completado = partido.registrarResultado(6, 4, 4, 6, 7, 6);

            assertTrue(completado.esResultadoReglamentario());
            assertEquals(GanadorPareja.LOCAL, completado.obtenerParejaGanadora());
            assertEquals(2, completado.calcularSetsGanadosLocal());
            assertEquals(1, completado.calcularSetsGanadosVisitante());
        }

        @Test
        @DisplayName("Rechazar partido 2-0 donde se haya jugado un 3er set indebido")
        void debeLanzarExcepcionSiSeJuegaTercerSetGanando2Cero() {
            PartidoPareja partido = PartidoPareja.crear(enfrentamientoId, 1, local1, local2, visitante1, visitante2);

            assertThrows(ResultadoPadelIlegalExcepcion.class, () ->
                    partido.registrarResultado(6, 3, 6, 2, 6, 4)
            );
        }

        @Test
        @DisplayName("Rechazar partido empatado 1-1 sin tercer set disputado")
        void debeLanzarExcepcionSiEmpatanUnoAUnoSinTercerSet() {
            PartidoPareja partido = PartidoPareja.crear(enfrentamientoId, 1, local1, local2, visitante1, visitante2);

            assertThrows(ResultadoPadelIlegalExcepcion.class, () ->
                    partido.registrarResultado(6, 4, 3, 6, null, null)
            );
        }

        @Test
        @DisplayName("Rechazar partido con tanteo de set ilegal")
        void debeLanzarExcepcionSiUnSetEsIlegal() {
            PartidoPareja partido = PartidoPareja.crear(enfrentamientoId, 1, local1, local2, visitante1, visitante2);

            assertThrows(ResultadoPadelIlegalExcepcion.class, () ->
                    partido.registrarResultado(6, 5, 6, 2, null, null)
            );
        }
    }
}
