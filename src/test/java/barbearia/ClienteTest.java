package barbearia;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClienteTest {

    @Test
    void deveRetornarNome() {
        Cliente cliente = new Cliente("João", "(43) 99999-9999");
        assertEquals("João", cliente.getNome());
    }

    @Test
    void deveRetornarTelefone() {
        Cliente cliente = new Cliente("João", "(43) 99999-9999");
        assertEquals("(43) 99999-9999", cliente.getTelefone());
    }

    @Test
    void deveExibirDadosDoCliente() {
        PrintStream original = System.out;
        ByteArrayOutputStream saida = new ByteArrayOutputStream();
        System.setOut(new PrintStream(saida, true, StandardCharsets.UTF_8));
        try {
            new Cliente("João", "(43) 99999-9999").exibirDados();
        } finally {
            System.setOut(original);
        }
        String texto = saida.toString(StandardCharsets.UTF_8);
        assertTrue(texto.contains("Cliente: João"));
        assertTrue(texto.contains("Telefone: (43) 99999-9999"));
    }
}
