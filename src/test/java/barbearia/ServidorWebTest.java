package barbearia;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServidorWebTest {

    private static HttpServer servidor;
    private static String base;
    private static final HttpClient cliente = HttpClient.newHttpClient();

    @BeforeAll
    static void iniciar() throws Exception {
        servidor = ServidorWeb.criar(0);
        servidor.start();
        base = "http://localhost:" + servidor.getAddress().getPort();
    }

    @AfterAll
    static void parar() {
        servidor.stop(0);
    }

    private HttpResponse<String> get(String caminho) throws Exception {
        return cliente.send(HttpRequest.newBuilder(URI.create(base + caminho)).build(),
                HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> post(String corpo) throws Exception {
        return cliente.send(HttpRequest.newBuilder(URI.create(base + "/agendar"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(corpo)).build(), HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void healthRespondeOk() throws Exception {
        HttpResponse<String> r = get("/health");
        assertEquals(200, r.statusCode());
        assertEquals("OK", r.body());
    }

    @Test
    void paginaInicialTemFormularioAcessivel() throws Exception {
        HttpResponse<String> r = get("/");
        assertEquals(200, r.statusCode());
        assertTrue(r.body().contains("lang=\"pt-BR\""));
        assertTrue(r.body().contains("<label for=\"nome\">"));
        assertTrue(r.body().contains("Alto contraste"));
    }

    @Test
    void rotaInexistenteRetorna404() throws Exception {
        assertEquals(404, get("/nao-existe").statusCode());
    }

    @Test
    void agendamentoValidoMostraResumoComValor() throws Exception {
        HttpResponse<String> r = post("nome=Jo%C3%A3o&telefone=43999999999&barbeiro=0&servico=2&data=2026-11-05&horario=10%3A00");
        assertEquals(200, r.statusCode());
        assertTrue(r.body().contains("Agendamento confirmado"));
        assertTrue(r.body().contains("Cliente: João"));
        assertTrue(r.body().contains("Data: 05/11/2026"));
        assertTrue(r.body().contains("Valor: R$ 55,00"));
    }

    @Test
    void nomeVazioMostraErroEmTexto() throws Exception {
        HttpResponse<String> r = post("nome=&telefone=1&barbeiro=0&servico=0&data=2026-11-05&horario=10%3A00");
        assertTrue(r.body().contains("Atenção: informe o nome do cliente."));
    }

    @Test
    void dataInvalidaEhRejeitada() throws Exception {
        HttpResponse<String> r = post("nome=Ana&telefone=1&barbeiro=0&servico=0&data=31%2F02%2F2026&horario=10%3A00");
        assertTrue(r.body().contains("Atenção: data inválida."));
    }

    @Test
    void getEmAgendarVoltaParaInicio() throws Exception {
        HttpClient semRedirecionar = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NEVER).build();
        HttpResponse<String> r = semRedirecionar.send(
                HttpRequest.newBuilder(URI.create(base + "/agendar")).build(), HttpResponse.BodyHandlers.ofString());
        assertEquals(303, r.statusCode());
    }

    @Test
    void escapaHtmlNosCampos() throws Exception {
        HttpResponse<String> r = post("nome=%3Cscript%3E&telefone=1&barbeiro=0&servico=0&data=2026-11-05&horario=10%3A00");
        assertTrue(r.body().contains("&lt;script&gt;"));
    }

    @Test
    void opcaoDeBarbeiroInvalidaEhRejeitada() throws Exception {
        HttpResponse<String> r = post("nome=Ana&telefone=1&barbeiro=9&servico=0&data=2026-11-05&horario=10%3A00");
        assertTrue(r.body().contains("escolha barbeiro, serviço e horário"));
    }
}
