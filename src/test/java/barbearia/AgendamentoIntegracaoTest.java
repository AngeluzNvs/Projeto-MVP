package barbearia;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Testes de integração: verificam a comunicação entre Agendamento,
 * Cliente, Barbeiro e Servico, e o fluxo completo da classe Main.
 */
class AgendamentoIntegracaoTest {

    private String capturar(Runnable acao) {
        PrintStream original = System.out;
        ByteArrayOutputStream saida = new ByteArrayOutputStream();
        System.setOut(new PrintStream(saida, true, StandardCharsets.UTF_8));
        try {
            acao.run();
        } finally {
            System.setOut(original);
        }
        return saida.toString(StandardCharsets.UTF_8);
    }

    @Test
    void agendamentoDeveReunirDadosDeClienteBarbeiroEServico() {
        Cliente cliente = new Cliente("Maria", "(43) 98888-7777");
        Barbeiro barbeiro = new Barbeiro("Pedro", "Barba");
        Servico servico = new Servico("Barba completa", 25.50);
        Agendamento agendamento = new Agendamento(cliente, barbeiro, servico, "10/10/2026", "09:30");

        String texto = capturar(agendamento::exibirAgendamento);

        assertTrue(texto.contains("Cliente: Maria"));
        assertTrue(texto.contains("Barbeiro: Pedro"));
        assertTrue(texto.contains("Serviço: Barba completa"));
        assertTrue(texto.contains("Data: 10/10/2026"));
        assertTrue(texto.contains("Horário: 09:30"));
        assertTrue(texto.contains("Status: Agendado"));
    }

    @Test
    void fluxoCompletoDaMainDeveExibirAgendamento() {
        String texto = capturar(() -> Main.main(new String[0]));

        assertTrue(texto.contains("Cliente: João"));
        assertTrue(texto.contains("Barbeiro: Carlos"));
        assertTrue(texto.contains("Serviço: Corte de cabelo"));
        assertTrue(texto.contains("Status: Agendado"));
    }
}
