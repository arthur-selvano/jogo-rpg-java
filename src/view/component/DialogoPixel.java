package view.component;

import view.util.AssetManager;
import view.util.Estilo;
import view.util.PixelFont;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Alerta/confirmação modal desenhado com a caixa de diálogo 8-bits.
 * Nenhuma exceção ou stacktrace é mostrada ao jogador: apenas a mensagem
 * amigável produzida pelo TratadorDeErros.
 */
public class DialogoPixel extends JDialog {

    private static final int LARGURA = 700;
    private static final int MARGEM = 10;
    private static final int LARGURA_TEXTO = LARGURA - 2 * 54;

    private boolean confirmado;

    public DialogoPixel(Window dono, String titulo, String mensagem) {
        this(dono, titulo, mensagem, false);
    }

    private DialogoPixel(Window dono, String titulo, String mensagem, boolean confirmacao) {
        super(dono, ModalityType.APPLICATION_MODAL);

        BufferedImage tmp = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = tmp.createGraphics();
        AssetManager.preparar(g2);
        g2.setFont(Estilo.FONTE_CORPO);
        FontMetrics fm = g2.getFontMetrics();
        List<String> linhas = Estilo.quebrar(mensagem == null ? "" : mensagem, fm, LARGURA_TEXTO);
        g2.dispose();

        int alturaTexto = Math.max(24, linhas.size() * 24);
        int altura = Math.max(292, 170 + alturaTexto + 70);

        setUndecorated(true);
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel painel = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                try {
                    AssetManager.preparar(g2);
                    g2.setColor(new Color(8, 10, 16));
                    g2.fillRect(0, 0, getWidth(), getHeight());
                    g2.setColor(new Color(0, 0, 0, 70));
                    g2.fillRect(0, 0, getWidth(), 16);
                    g2.fillRect(0, getHeight() - 16, getWidth(), 16);

                    Estilo.caixaDialogo(g2, MARGEM, MARGEM,
                            getWidth() - MARGEM * 2, getHeight() - MARGEM * 2);

                    // título e filete dourado
                    PixelFont.centralizadoComSombra(g2, titulo, getWidth() / 2,
                            40, 3, Estilo.OURO_CLARO, Estilo.PRETO);
                    g2.setColor(Estilo.OURO_ESCURO);
                    g2.fillRect(44, 70, getWidth() - 88, 3);
                    g2.setColor(Estilo.OURO);
                    g2.fillRect(getWidth() / 2 - 10, 68, 20, 7);

                    g2.setFont(Estilo.FONTE_CORPO);
                    int y = 98;
                    for (String linha : linhas) {
                        g2.setColor(Estilo.SOMBRA_TEXTO);
                        g2.drawString(linha, 56, y + 2);
                        g2.setColor(Estilo.TEXTO);
                        g2.drawString(linha, 54, y);
                        y += 24;
                    }
                } finally {
                    g2.dispose();
                }
            }
        };
        painel.setOpaque(true);
        painel.setBackground(Estilo.PRETO);
        painel.setPreferredSize(new Dimension(LARGURA, altura));

        int larguraBotao = confirmacao ? 170 : 160;
        int yBotao = altura - 86;
        if (confirmacao) {
            BotaoPixel sim = new BotaoPixel("SIM", () -> {
                confirmado = true;
                dispose();
            }, larguraBotao, 44);
            BotaoPixel nao = new BotaoPixel("NAO", this::dispose, larguraBotao, 44);
            sim.setBounds(LARGURA / 2 - larguraBotao - 14, yBotao, larguraBotao, 44);
            nao.setBounds(LARGURA / 2 + 14, yBotao, larguraBotao, 44);
            painel.add(sim);
            painel.add(nao);
            nao.setAtalho("ESC");
        } else {
            BotaoPixel ok = new BotaoPixel("OK", this::dispose, larguraBotao, 44);
            ok.setBounds(LARGURA / 2 - larguraBotao / 2, yBotao, larguraBotao, 44);
            painel.add(ok);
            SwingUtilities.invokeLater(ok::requestFocusInWindow);
        }

        setContentPane(painel);
        pack();
        setLocationRelativeTo(dono);
        getRootPane().registerKeyboardAction(this::fecharComEsc,
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    private void fecharComEsc(ActionEvent evento) {
        confirmado = false;
        dispose();
    }

    public boolean isConfirmado() {
        return confirmado;
    }

    /** Descobre a janela dona, aceitando tanto um painel quanto o próprio frame. */
    private static Window janelaDona(java.awt.Component pai) {
        if (pai == null) {
            return null;
        }
        if (pai instanceof Window) {
            return (Window) pai;
        }
        return SwingUtilities.getWindowAncestor(pai);
    }

    /** Mostra um alerta amigável centralizado na janela do componente. */
    public static void mostrar(java.awt.Component pai, String titulo, String mensagem) {
        new DialogoPixel(janelaDona(pai), titulo, mensagem).setVisible(true);
    }

    /** Confirmação modal; retorna true apenas se o jogador escolher SIM. */
    public static boolean confirmar(java.awt.Component pai, String titulo, String mensagem) {
        DialogoPixel dialogo = new DialogoPixel(janelaDona(pai), titulo, mensagem, true);
        dialogo.setVisible(true);
        return dialogo.isConfirmado();
    }
}