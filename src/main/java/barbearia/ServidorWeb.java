package barbearia;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Versão web do agendamento da barbearia (usada no deploy em nuvem).
 * Reaproveita as classes Cliente, Barbeiro, Servico e Agendamento.
 * Rotas: GET / (formulário), POST /agendar (resultado) e GET /health (monitoramento).
 */
public class ServidorWeb {

    static final Barbeiro[] BARBEIROS = {
            new Barbeiro("Carlos", "Corte masculino"), new Barbeiro("Pedro", "Barba")};
    static final Servico[] SERVICOS = {
            new Servico("Corte de cabelo", 35.00), new Servico("Barba completa", 25.50),
            new Servico("Corte e barba", 55.00)};
    static final String[] HORARIOS = {"09:00", "10:00", "11:00", "13:00", "14:00", "15:00", "16:00", "17:00", "18:00"};

    public static void main(String[] args) throws IOException {
        int porta = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
        HttpServer servidor = criar(porta);
        servidor.start();
        System.out.println("Barbearia no ar na porta " + servidor.getAddress().getPort());
    }

    /** Cria o servidor (porta 0 = qualquer porta livre, usada nos testes). */
    static HttpServer criar(int porta) throws IOException {
        // Servidores em nuvem costumam usar locale em inglês: força o formato brasileiro (R$ 55,00).
        java.util.Locale.setDefault(new java.util.Locale("pt", "BR"));
        HttpServer s = HttpServer.create(new InetSocketAddress(porta), 0);
        s.createContext("/health", ex -> responder(ex, 200, "text/plain; charset=utf-8", "OK"));
        s.createContext("/agendar", ex -> {
            if (!"POST".equals(ex.getRequestMethod())) { redirecionar(ex); return; }
            String corpo = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            responder(ex, 200, "text/html; charset=utf-8", processar(lerCampos(corpo)));
        });
        s.createContext("/", ex -> {
            if (!"/".equals(ex.getRequestURI().getPath())) {
                responder(ex, 404, "text/html; charset=utf-8", pagina("Página não encontrada",
                        "<p>Página não encontrada. <a href=\"/\">Voltar ao agendamento</a>.</p>"));
                return;
            }
            responder(ex, 200, "text/html; charset=utf-8", formulario("", "", "", "", "", ""));
        });
        return s;
    }

    // ---------- lógica ----------

    static String processar(Map<String, String> c) {
        String nome = c.getOrDefault("nome", "").trim();
        String telefone = c.getOrDefault("telefone", "").trim();
        String dataIso = c.getOrDefault("data", "").trim();
        String horario = c.getOrDefault("horario", "");
        String erro = null;
        LocalDate data = null;
        int b = indice(c.get("barbeiro"), BARBEIROS.length);
        int s = indice(c.get("servico"), SERVICOS.length);

        if (nome.isEmpty()) erro = "Atenção: informe o nome do cliente.";
        else if (telefone.isEmpty()) erro = "Atenção: informe o telefone.";
        else if (dataIso.isEmpty()) erro = "Atenção: informe a data.";
        else {
            try { data = LocalDate.parse(dataIso); } catch (Exception e) { erro = "Atenção: data inválida."; }
        }
        if (erro == null && (b < 0 || s < 0 || !contem(HORARIOS, horario))) erro = "Atenção: escolha barbeiro, serviço e horário.";
        if (erro != null) return formulario(nome, telefone, dataIso, c.get("barbeiro"), c.get("servico"), erro);

        Agendamento ag = new Agendamento(new Cliente(nome, telefone), BARBEIROS[b], SERVICOS[s],
                data.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), horario);
        String resumo = capturar(ag);
        return pagina("Agendamento confirmado",
                "<h2 id=\"conteudo\" tabindex=\"-1\">Agendamento confirmado</h2>"
                        + "<pre aria-label=\"Resumo do agendamento\">" + esc(resumo) + "</pre>"
                        + "<p><a class=\"botao\" href=\"/\">Fazer novo agendamento</a></p>");
    }

    /** Captura o que exibirAgendamento() escreve no console (sincronizado: System.out é global). */
    static synchronized String capturar(Agendamento ag) {
        PrintStream original = System.out;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        System.setOut(new PrintStream(buffer, true, StandardCharsets.UTF_8));
        try { ag.exibirAgendamento(); } finally { System.setOut(original); }
        return buffer.toString(StandardCharsets.UTF_8);
    }

    private static int indice(String v, int tamanho) {
        try { int i = Integer.parseInt(v); return i >= 0 && i < tamanho ? i : -1; } catch (Exception e) { return -1; }
    }

    private static boolean contem(String[] lista, String v) {
        for (String x : lista) if (x.equals(v)) return true;
        return false;
    }

    static Map<String, String> lerCampos(String corpo) {
        Map<String, String> m = new HashMap<>();
        for (String par : corpo.split("&")) {
            int i = par.indexOf('=');
            if (i > 0) m.put(URLDecoder.decode(par.substring(0, i), StandardCharsets.UTF_8),
                    URLDecoder.decode(par.substring(i + 1), StandardCharsets.UTF_8));
        }
        return m;
    }

    // ---------- HTML ----------

    static String esc(String t) {
        return t == null ? "" : t.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    static String formulario(String nome, String tel, String data, String barb, String serv, String erro) {
        StringBuilder h = new StringBuilder();
        h.append("<h2 id=\"conteudo\" tabindex=\"-1\">Novo agendamento</h2>");
        h.append("<p id=\"status\" role=\"alert\" class=\"status\">").append(esc(erro)).append("</p>");
        h.append("<form method=\"post\" action=\"/agendar\">");
        h.append(campo("nome", "Nome do cliente", "text", nome, "name"));
        h.append(campo("telefone", "Telefone", "tel", tel, "tel"));
        h.append("<label for=\"barbeiro\">Barbeiro</label><select id=\"barbeiro\" name=\"barbeiro\">");
        for (int i = 0; i < BARBEIROS.length; i++)
            h.append(opcao(String.valueOf(i), BARBEIROS[i].getNome() + " - " + BARBEIROS[i].getEspecialidade(), String.valueOf(i).equals(barb)));
        h.append("</select><label for=\"servico\">Serviço</label><select id=\"servico\" name=\"servico\">");
        for (int i = 0; i < SERVICOS.length; i++)
            h.append(opcao(String.valueOf(i), String.format("%s - R$ %.2f", SERVICOS[i].getNome(), SERVICOS[i].getValor()), String.valueOf(i).equals(serv)));
        h.append("</select>");
        h.append(campo("data", "Data", "date", data, "off"));
        h.append("<label for=\"horario\">Horário</label><select id=\"horario\" name=\"horario\">");
        for (String hr : HORARIOS) h.append(opcao(hr, hr, false));
        h.append("</select><button type=\"submit\" class=\"botao\">Agendar</button></form>");
        if (!erro.isEmpty()) h.append("<script>document.addEventListener('DOMContentLoaded',function(){var e=document.querySelectorAll('input');"
                + "for(var i=0;i<e.length;i++){if(!e[i].value){e[i].focus();break;}}});</script>");
        return pagina("Agendamento", h.toString());
    }

    private static String campo(String id, String rotulo, String tipo, String valor, String auto) {
        return "<label for=\"" + id + "\">" + rotulo + "</label><input id=\"" + id + "\" name=\"" + id + "\" type=\"" + tipo
                + "\" value=\"" + esc(valor) + "\" autocomplete=\"" + auto + "\" required>";
    }

    private static String opcao(String valor, String texto, boolean sel) {
        return "<option value=\"" + valor + "\"" + (sel ? " selected" : "") + ">" + esc(texto) + "</option>";
    }

    static String pagina(String titulo, String corpo) {
        return "<!doctype html><html lang=\"pt-BR\"><head><meta charset=\"utf-8\">"
                + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">"
                + "<title>Barbearia - " + esc(titulo) + "</title><style>"
                + ":root{--fundo:#f5f5f5;--texto:#1a1a1a;--campo:#fff;--destaque:#0b3c8c;--botao:#dce6f1;font-size:18px}"
                + "body.contraste{--fundo:#000;--texto:#fff;--campo:#000;--destaque:#ff0;--botao:#000}"
                + "body{margin:0;background:var(--fundo);color:var(--texto);font-family:Arial,Helvetica,sans-serif;line-height:1.5}"
                + "main{max-width:640px;margin:0 auto;padding:16px 20px}"
                + "label{display:block;margin-top:14px;font-weight:bold}"
                + "input,select,.botao,button{font-size:1rem;padding:8px 10px;border:2px solid var(--texto);border-radius:4px;"
                + "background:var(--campo);color:var(--texto);width:100%;box-sizing:border-box}"
                + ".botao,button{background:var(--botao);cursor:pointer;width:auto;display:inline-block;text-decoration:none;margin-top:14px}"
                + "body.contraste .botao,body.contraste button{color:#ff0;border-color:#ff0}"
                + "a{color:var(--destaque)}"
                + ":focus{outline:4px solid var(--destaque);outline-offset:2px}"
                + ".barra button{margin:0 6px 0 0}.status{min-height:1.5em;font-weight:bold}"
                + "pre{background:var(--campo);border:2px solid var(--texto);padding:12px;overflow-x:auto;font-size:1rem}"
                + ".pular{position:absolute;left:-999px}.pular:focus{left:8px;top:8px;background:var(--campo);padding:6px}"
                + "</style></head><body><a class=\"pular\" href=\"#conteudo\">Ir para o conteúdo</a><main>"
                + "<h1>Barbearia</h1><div class=\"barra\" role=\"group\" aria-label=\"Acessibilidade\">"
                + "<button type=\"button\" id=\"menos\" aria-label=\"Diminuir texto\">A-</button>"
                + "<button type=\"button\" id=\"mais\" aria-label=\"Aumentar texto\">A+</button>"
                + "<button type=\"button\" id=\"contraste\" aria-pressed=\"false\">Alto contraste</button></div>"
                + corpo + "</main><script>(function(){var r=document.documentElement,b=document.body,t=18;"
                + "try{t=+localStorage.getItem('t')||18;if(localStorage.getItem('c')==='1')b.classList.add('contraste');}catch(e){}"
                + "function ap(){r.style.fontSize=t+'px';try{localStorage.setItem('t',t);}catch(e){}}"
                + "function ct(){var on=b.classList.contains('contraste');document.getElementById('contraste').setAttribute('aria-pressed',on);"
                + "try{localStorage.setItem('c',on?'1':'0');}catch(e){}}"
                + "ap();ct();"
                + "document.getElementById('mais').onclick=function(){t=Math.min(40,t+4);ap();};"
                + "document.getElementById('menos').onclick=function(){t=Math.max(14,t-4);ap();};"
                + "document.getElementById('contraste').onclick=function(){b.classList.toggle('contraste');ct();};"
                + "document.addEventListener('keydown',function(e){if(e.ctrlKey&&e.key==='h'){e.preventDefault();"
                + "b.classList.toggle('contraste');ct();}});})();</script></body></html>";
    }

    // ---------- HTTP ----------

    private static void redirecionar(HttpExchange ex) throws IOException {
        ex.getResponseHeaders().add("Location", "/");
        ex.sendResponseHeaders(303, -1);
        ex.close();
    }

    private static void responder(HttpExchange ex, int codigo, String tipo, String corpo) throws IOException {
        byte[] bytes = corpo.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", tipo);
        ex.sendResponseHeaders(codigo, bytes.length);
        ex.getResponseBody().write(bytes);
        ex.close();
    }
}
