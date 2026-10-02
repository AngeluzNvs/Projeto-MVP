package barbearia;

public class Main {

    public static void main(String[] args) {

        Cliente cliente = new Cliente(
                "João",
                "(43) 99999-9999"
        );

        Barbeiro barbeiro = new Barbeiro(
                "Carlos",
                "Corte masculino"
        );

        Servico servico = new Servico(
                "Corte de cabelo",
                35.00
        );

        Agendamento agendamento = new Agendamento(
                cliente,
                barbeiro,
                servico,
                "05/10/2026",
                "14:00"
        );

        agendamento.exibirAgendamento();
    }
}