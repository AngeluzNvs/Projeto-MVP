package barbearia;

public class Cliente {

    private String nome;
    private String telefone;

    public Cliente(String nome, String telefone) {
        this.nome = nome;
        this.telefone = telefone;
    }

    public String getNome() {
        return nome;
    }

    public String getTelefone() {
        return telefone;
    }

    public void exibirDados() {
        System.out.println("Cliente: " + nome);
        System.out.println("Telefone: " + telefone);
    }
}