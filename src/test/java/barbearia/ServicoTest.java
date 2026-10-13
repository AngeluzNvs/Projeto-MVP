package barbearia;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServicoTest {

    @Test
    void deveRetornarNome() {
        Servico servico = new Servico("Corte de cabelo", 35.00);
        assertEquals("Corte de cabelo", servico.getNome());
    }

    @Test
    void deveRetornarValor() {
        Servico servico = new Servico("Corte de cabelo", 35.00);
        assertEquals(35.00, servico.getValor(), 0.001);
    }

    @Test
    void deveExibirDadosDoServico() {
        PrintStream original = System.out;
        ByteArrayOutputStream saida = new ByteArrayOutputStream();
        System.setOut(new PrintStream(saida, true, StandardCharsets.UTF_8));
        try {
            new Servico("Corte de cabelo", 35.00).exibirDados();
        } finally {
            System.setOut(original);
        }
        String texto = saida.toString(StandardCharsets.UTF_8);
        assertTrue(texto.contains("Serviço: Corte de cabelo"));
        assertTrue(texto.contains("Valor: R$ 35"));
    }
}
