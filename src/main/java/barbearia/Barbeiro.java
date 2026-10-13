package barbearia;

public class Barbeiro {

    private String nome;
    private String especialidade;

    public Barbeiro(String nome, String especialidade) {
        this.nome = nome;
        this.especialidade = especialidade;
    }

    public String getNome() {
        return nome;
    }

    public String getEspecialidade() {
        return especialidade;
    }

    public void exibirDados() {
        System.out.println("Barbeiro: " + nome);
        System.out.println("Especialidade: " + especialidade);
    }
}