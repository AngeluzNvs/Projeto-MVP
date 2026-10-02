package barbearia;

public class Servico {

    private String nome;
    private double valor;

    public Servico(String nome, double valor) {
        this.nome = nome;
        this.valor = valor;
    }

    public String getNome() {
        return nome;
    }

    public double getValor() {
        return valor;
    }

    public void exibirDados() {
        System.out.println("Serviço: " + nome);
        System.out.printf("Valor: R$ %.2f%n", valor);
    }
}