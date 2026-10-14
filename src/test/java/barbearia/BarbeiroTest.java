package barbearia;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BarbeiroTest {

    @Test
    void deveRetornarNome() {
        Barbeiro barbeiro = new Barbeiro("Carlos", "Corte masculino");
        assertEquals("Carlos", barbeiro.getNome());
    }

    @Test
    void deveRetornarEspecialidade() {
        Barbeiro barbeiro = new Barbeiro("Carlos", "Corte masculino");
        assertEquals("Corte masculino", barbeiro.getEspecialidade());
    }

    @Test
    void deveExibirDadosDoBarbeiro() {
        PrintStream original = System.out;
        ByteArrayOutputStream saida = new ByteArrayOutputStream();
        System.setOut(new PrintStream(saida, true, StandardCharsets.UTF_8));
        try {
            new Barbeiro("Carlos", "Corte masculino").exibirDados();
        } finally {
            System.setOut(original);
        }
        String texto = saida.toString(StandardCharsets.UTF_8);
        assertTrue(texto.contains("Barbeiro: Carlos"));
        assertTrue(texto.contains("Especialidade: Corte masculino"));
    }
}
