package barbearia;

public class Agendamento {

    private Cliente cliente;
    private Barbeiro barbeiro;
    private Servico servico;
    private String data;
    private String horario;
    private String status;

    public Agendamento(
            Cliente cliente,
            Barbeiro barbeiro,
            Servico servico,
            String data,
            String horario) {

        this.cliente = cliente;
        this.barbeiro = barbeiro;
        this.servico = servico;
        this.data = data;
        this.horario = horario;
        this.status = "Agendado";
    }

    public void exibirAgendamento() {

        System.out.println("================================");
        System.out.println("          AGENDAMENTO");
        System.out.println("================================");

        System.out.println("Cliente: " + cliente.getNome());
        System.out.println("Barbeiro: " + barbeiro.getNome());
        System.out.println("Serviço: " + servico.getNome());
        System.out.println("Data: " + data);
        System.out.println("Horário: " + horario);
        System.out.println("Status: " + status);
        System.out.printf("Valor: R$ %.2f%n", servico.getValor());
    }
}