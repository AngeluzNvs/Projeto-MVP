package barbearia;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertTrue;

class AgendamentoTest {

    private Agendamento agendamento;

    @BeforeEach
    void preparar() {
        Cliente cliente = new Cliente("João", "(43) 99999-9999");
        Barbeiro barbeiro = new Barbeiro("Carlos", "Corte masculino");
        Servico servico = new Servico("Corte de cabelo", 35.00);
        agendamento = new Agendamento(cliente, barbeiro, servico, "05/10/2026", "14:00");
    }

    private String capturarSaida() {
        PrintStream original = System.out;
        ByteArrayOutputStream saida = new ByteArrayOutputStream();
        System.setOut(new PrintStream(saida, true, StandardCharsets.UTF_8));
        try {
            agendamento.exibirAgendamento();
        } finally {
            System.setOut(original);
        }
        return saida.toString(StandardCharsets.UTF_8);
    }

    @Test
    void deveIniciarComStatusAgendado() {
        assertTrue(capturarSaida().contains("Status: Agendado"));
    }

    @Test
    void deveExibirCabecalho() {
        assertTrue(capturarSaida().contains("AGENDAMENTO"));
    }

    @Test
    void deveExibirDataEHorario() {
        String texto = capturarSaida();
        assertTrue(texto.contains("Data: 05/10/2026"));
        assertTrue(texto.contains("Horário: 14:00"));
    }

    @Test
    void deveExibirValorDoServico() {
        assertTrue(capturarSaida().contains("Valor: R$ 35"));
    }
}
