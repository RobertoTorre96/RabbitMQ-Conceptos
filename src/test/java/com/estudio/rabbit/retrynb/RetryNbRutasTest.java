package com.estudio.rabbit.retrynb;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/** Test unitario: lógica pura, sin broker. */
class RetryNbRutasTest {
    @Test void primerFalloVaALaEspera1() { assertEquals("wait.1", RetryNbConsumidor.rutaDe(0)); }
    @Test void ultimoReintentoVaALaEspera3() { assertEquals("wait.3", RetryNbConsumidor.rutaDe(2)); }
    @Test void agotadosVanAlParking() { assertEquals("parking", RetryNbConsumidor.rutaDe(3)); }
}
