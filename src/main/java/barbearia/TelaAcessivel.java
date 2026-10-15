package barbearia;

import javax.swing.*;
import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;


public class TelaAcessivel extends JFrame {

    private static final Barbeiro[] BARBEIROS = {
            new Barbeiro("Carlos", "Corte masculino"), new Barbeiro("Pedro", "Barba")};
    private static final Servico[] SERVICOS = {
            new Servico("Corte de cabelo", 35.00), new Servico("Barba completa", 25.50),
            new Servico("Corte e barba", 55.00)};

    final JTextField nome = new JTextField(18), telefone = new JTextField(18), data = new JTextField(18);
    final JComboBox<String> barbeiro = new JComboBox<>(), servico = new JComboBox<>();
    final JComboBox<String> horario = new JComboBox<>(
            new String[]{"09:00", "10:00", "11:00", "13:00", "14:00", "15:00", "16:00", "17:00", "18:00"});
    final JTextArea resumo = new JTextArea(11, 30);
    final JLabel status = new JLabel(" ");
    final JPanel tela = new JPanel(new GridBagLayout());
    int tamanho = 16;
    boolean contraste = false;
    int linha = 0;

    public TelaAcessivel() {
        super("Barbearia - Agendamento acessível");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        for (Barbeiro b : BARBEIROS) barbeiro.addItem(b.getNome() + " - " + b.getEspecialidade());
        for (Servico s : SERVICOS) servico.addItem(String.format("%s - R$ %.2f", s.getNome(), s.getValor()));
        tela.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        JButton menos = botao("A-", KeyEvent.VK_UNDEFINED, "Diminuir texto");
        JButton mais = botao("A+", KeyEvent.VK_UNDEFINED, "Aumentar texto");
        JButton alto = botao("Alto contraste", KeyEvent.VK_C, "Alto contraste");
        JButton agendar = botao("Agendar", KeyEvent.VK_A, "Agendar");
        menos.addActionListener(e -> ajustar(-4));
        mais.addActionListener(e -> ajustar(4));
        alto.addActionListener(e -> alternarContraste());
        agendar.addActionListener(e -> agendar());

        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        barra.add(new JLabel("Tamanho do texto:"));
        barra.add(menos);
        barra.add(mais);
        barra.add(alto);
        adicionar(barra, 0, 2);
        campo("Nome do cliente", KeyEvent.VK_N, nome);
        campo("Telefone", KeyEvent.VK_T, telefone);
        campo("Barbeiro", KeyEvent.VK_B, barbeiro);
        campo("Serviço", KeyEvent.VK_S, servico);
        campo("Data (dd/mm/aaaa)", KeyEvent.VK_D, data);
        campo("Horário", KeyEvent.VK_H, horario);
        adicionar(agendar, 0, 2);
        resumo.setEditable(false);
        resumo.getAccessibleContext().setAccessibleName("Resumo do agendamento");
        adicionar(new JScrollPane(resumo), 0, 2);
        adicionar(status, 0, 2);

        atalho(KeyEvent.VK_EQUALS, () -> ajustar(4));
        atalho(KeyEvent.VK_MINUS, () -> ajustar(-4));
        atalho(KeyEvent.VK_H, this::alternarContraste);
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addPropertyChangeListener(
                "permanentFocusOwner", e -> { marcar(e.getOldValue()); marcar(e.getNewValue()); });

        setContentPane(new JScrollPane(tela, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED));
        getRootPane().setDefaultButton(agendar);
        aplicarTema();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    // ---------- montagem ----------

    private JButton botao(String texto, int tecla, String nomeAcessivel) {
        JButton b = new JButton(texto);
        if (tecla != KeyEvent.VK_UNDEFINED) b.setMnemonic(tecla);
        b.setFocusPainted(false);
        b.getAccessibleContext().setAccessibleName(nomeAcessivel);
        return b;
    }

    private void adicionar(JComponent c, int coluna, int largura) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = coluna; g.gridy = linha++; g.gridwidth = largura;
        g.insets = new Insets(6, 4, 6, 4);
        g.anchor = GridBagConstraints.WEST;
        g.fill = GridBagConstraints.HORIZONTAL;
        tela.add(c, g);
    }

    private void campo(String texto, int tecla, JComponent componente) {
        JLabel rotulo = new JLabel(texto);
        rotulo.setDisplayedMnemonic(tecla);
        rotulo.setLabelFor(componente);
        componente.getAccessibleContext().setAccessibleName(texto);
        int y = linha;
        adicionar(rotulo, 0, 1);
        linha = y;
        adicionar(componente, 1, 1);
    }

    private void atalho(int tecla, Runnable acao) {
        String id = "atalho" + tecla;
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(tecla, InputEvent.CTRL_DOWN_MASK), id);
        getRootPane().getActionMap().put(id, new AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) { acao.run(); }
        });
    }

    // ---------- acessibilidade ----------

    void ajustar(int passo) {
        int novo = Math.max(14, Math.min(40, tamanho + passo));
        status.setText(novo == tamanho ? "Tamanho limite atingido (" + tamanho + " pt)."
                : "Texto em " + novo + " pt.");
        tamanho = novo;
        aplicarTema();
    }

    void alternarContraste() {
        contraste = !contraste;
        aplicarTema();
        status.setText(contraste ? "Alto contraste ligado." : "Alto contraste desligado.");
    }

    private Color fundo() { return contraste ? Color.BLACK : new Color(0xF5F5F5); }
    private Color texto() { return contraste ? Color.WHITE : new Color(0x1A1A1A); }
    private Color destaque() { return contraste ? Color.YELLOW : new Color(0x0B3C8C); }

    private void aplicarTema() {
        aplicar(tela);
        getContentPane().setBackground(fundo());
        pack();
        Rectangle tamanhoTela = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        setSize(Math.min(getWidth() + 20, tamanhoTela.width), Math.min(getHeight() + 20, tamanhoTela.height));
    }

    private void aplicar(Component c) {
        c.setFont(c == resumo ? new Font(Font.MONOSPACED, Font.PLAIN, tamanho) : c.getFont().deriveFont((float) tamanho));
        if (c instanceof JTextComponent t) {
            t.setBackground(contraste ? Color.BLACK : Color.WHITE);
            t.setForeground(texto());
            t.setCaretColor(texto());
            t.setBorder(borda(false));
        } else if (c instanceof AbstractButton b) {
            b.setOpaque(true);
            b.setBackground(contraste ? Color.BLACK : new Color(0xDCE6F1));
            b.setForeground(contraste ? Color.YELLOW : texto());
            b.setBorder(borda(false));
        } else if (c instanceof JComboBox<?> cb) {
            setaCombo(cb);
            cb.setBackground(contraste ? Color.BLACK : Color.WHITE);
            cb.setForeground(texto());
            cb.setBorder(BorderFactory.createLineBorder(contraste ? Color.WHITE : Color.GRAY, 2));
        } else if (c instanceof JLabel || c instanceof JPanel) {
            c.setBackground(fundo());
            c.setForeground(texto());
        }
        if (c instanceof Container pai) for (Component filho : pai.getComponents()) aplicar(filho);
    }

    /** Seta da caixa de seleção na cor do tema (para aparecer no alto contraste). */
    private void setaCombo(JComboBox<?> cb) {
        Color seta = contraste ? Color.YELLOW : new Color(0x1A1A1A);
        Color base = contraste ? Color.BLACK : new Color(0xDCE6F1);
        cb.setUI(new BasicComboBoxUI() {
            protected JButton createArrowButton() {
                BasicArrowButton b = new BasicArrowButton(SwingConstants.SOUTH, base, seta, seta, seta);
                b.setBorder(BorderFactory.createLineBorder(contraste ? Color.WHITE : Color.GRAY, 1));
                return b;
            }
        });
    }

    private javax.swing.border.Border borda(boolean foco) {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(foco ? destaque() : texto(), foco ? 4 : 1),
                BorderFactory.createEmptyBorder(4, 8, 4, 8));
    }

    /** Borda grossa na cor de destaque no componente que está com o foco do teclado. */
    private void marcar(Object o) {
        if (o instanceof JComponent c && SwingUtilities.isDescendingFrom(c, tela)
                && (c instanceof JTextComponent || c instanceof AbstractButton)) {
            c.setBorder(borda(c.hasFocus()));
        } else if (o instanceof JComboBox<?> cb) {
            cb.setBorder(BorderFactory.createLineBorder(cb.hasFocus() ? destaque() : (contraste ? Color.WHITE : Color.GRAY),
                    cb.hasFocus() ? 4 : 2));
        }
    }

    // ---------- fluxo (usa as suas classes) ----------

    void agendar() {
        if (nome.getText().isBlank()) { erro("Atenção: informe o nome do cliente.", nome); return; }
        if (telefone.getText().isBlank()) { erro("Atenção: informe o telefone.", telefone); return; }
        if (data.getText().isBlank()) { erro("Atenção: informe a data (dd/mm/aaaa).", data); return; }

        Cliente cliente = new Cliente(nome.getText().trim(), telefone.getText().trim());
        Agendamento ag = new Agendamento(cliente, BARBEIROS[barbeiro.getSelectedIndex()],
                SERVICOS[servico.getSelectedIndex()], data.getText().trim(), (String) horario.getSelectedItem());

        PrintStream original = System.out;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        System.setOut(new PrintStream(buffer, true, StandardCharsets.UTF_8));
        try { ag.exibirAgendamento(); } finally { System.setOut(original); }

        resumo.setText(buffer.toString(StandardCharsets.UTF_8));
        resumo.setCaretPosition(0);
        status.setText("Agendamento confirmado para " + data.getText().trim() + " às " + horario.getSelectedItem() + ".");
        resumo.requestFocusInWindow();
    }

    private void erro(String mensagem, JComponent campo) {
        status.setText(mensagem);
        campo.requestFocusInWindow();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()); } catch (Exception e) { }
            new TelaAcessivel();
        });
    }
}