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

        System.out.println("================================");
        System.out.println("       SISTEMA BARBEARIA");
        System.out.println("================================");

        cliente.exibirDados();

        System.out.println();

        barbeiro.exibirDados();
    }
}