package view;

import controller.GerenciadorJogo;
import view.component.BotaoPixel;
import view.util.AssetManager;
import view.util.Estilo;
import view.util.PixelFont;
import view.util.Sprites;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/** Criação do herói: nome, classe e início da jornada. */
public class PainelCriacao extends JPanel {

    private final TelaPrincipal tela;
    private final GerenciadorJogo jogo;
    private final JTextField campoNome;
    private final CartaoClasse cartaoGuerreiro;
    private final CartaoClasse cartaoMago;

    private GerenciadorJogo.Classe selecionada = GerenciadorJogo.Classe.GUERREIRO;

    public PainelCriacao(TelaPrincipal tela, GerenciadorJogo jogo) {
        this.tela = tela;
        this.jogo = jogo;

        setLayout(null);
        setFocusable(true);

        campoNome = new JTextField("Heroi");
        campoNome.setBounds(340, 174, 280, 38);
        campoNome.setBackground(Estilo.FUNDO_CAIXA);
        campoNome.setForeground(Estilo.CREME);
        campoNome.setCaretColor(Estilo.OURO);
        campoNome.setFont(new Font("Monospaced", Font.BOLD, 20));
        campoNome.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Estilo.OURO, 3),
                BorderFactory.createEmptyBorder(3, 10, 3, 10)));
        campoNome.addActionListener(e -> iniciar());
        add(campoNome);

        cartaoGuerreiro = new CartaoClasse(GerenciadorJogo.Classe.GUERREIRO,
                () -> selecionar(GerenciadorJogo.Classe.GUERREIRO),
                () -> selecionar(GerenciadorJogo.Classe.MAGO));
        cartaoGuerreiro.setBounds(100, 226, 340, 274);
        add(cartaoGuerreiro);

        cartaoMago = new CartaoClasse(GerenciadorJogo.Classe.MAGO,
                () -> selecionar(GerenciadorJogo.Classe.MAGO),
                () -> selecionar(GerenciadorJogo.Classe.GUERREIRO));
        cartaoMago.setBounds(520, 226, 340, 274);
        add(cartaoMago);

        BotaoPixel iniciar = new BotaoPixel("INICIAR JORNADA", this::iniciar, 240, 48);
        iniciar.setBounds(270, 524, 240, 48);
        add(iniciar);

        BotaoPixel voltar = new BotaoPixel("VOLTAR", this::voltarMenu, 160, 48);
        voltar.setBounds(530, 524, 160, 48);
        add(voltar);

        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke("ESCAPE"), "voltar");
        getActionMap().put("voltar", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isShowing()) {
                    voltarMenu();
                }
            }
        });

        selecionar(GerenciadorJogo.Classe.GUERREIRO);
    }

    /** Chamado pela TelaPrincipal ao exibir a tela. */
    public void aoExibir() {
        campoNome.requestFocusInWindow();
        campoNome.selectAll();
        repaint();
    }

    private void selecionar(GerenciadorJogo.Classe classe) {
        selecionada = classe;
        cartaoGuerreiro.setSelecionado(classe == GerenciadorJogo.Classe.GUERREIRO);
        cartaoMago.setSelecionado(classe == GerenciadorJogo.Classe.MAGO);
        repaint();
    }

    private void iniciar() {
        jogo.iniciarJornada(selecionada, campoNome.getText());
        tela.mostrarMapa();
    }

    private void voltarMenu() {
        jogo.reiniciar();
        tela.mostrarMenu();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            AssetManager.preparar(g2);
            int w = getWidth();
            int h = getHeight();
            Estilo.fundoGrama(g2, w, h);
            Estilo.escurecer(g2, w, h, new Color(8, 18, 12, 118));
            Estilo.vinheta(g2, w, h);

            g2.setColor(new Color(0, 0, 0, 90));
            g2.fillRect(0, 0, w, 118);
            g2.setColor(Estilo.OURO_ESCURO);
            g2.fillRect(0, 118, w, 3);

            g2.setColor(new Color(0, 0, 0, 100));
            g2.fillRect((w - 384) / 2 + 7, 21, 384, 96);
            AssetManager.desenhar(g2, Sprites.TITULO,
                    (w - 384) / 2, 14, 384, 96);

            g2.setColor(new Color(0, 0, 0, 100));
            g2.fillRect(345, 179, 280, 38);

            PixelFont.desenharCentralizado(g2,
                    "ESCOLHA SUA CLASSE E UM NOME PARA O HEROI",
                    w / 2, 128, 1, Estilo.CREME, Estilo.PRETO);
            PixelFont.desenharCentralizado(g2, "NOME DO HEROI",
                    w / 2, 156, 1, Estilo.OURO_CLARO, Estilo.PRETO);

            g2.setColor(Estilo.OURO_ESCURO);
            g2.fillRect(40, 540, w - 80, 2);

            PixelFont.desenharCentralizado(g2,
                    "ENTER NO CAMPO DE NOME INICIA A JORNADA",
                    w / 2, 584, 1, Estilo.CREME, Estilo.PRETO);
        } finally {
            g2.dispose();
        }
    }

    /** Cartão clicável com sprite, descrição e atributos de uma classe. */
    private final class CartaoClasse extends JComponent {

        private final GerenciadorJogo.Classe classe;
        private final Runnable aoSelecionar;
        private final Runnable aoAlternar;
        private boolean selecionado;

        CartaoClasse(GerenciadorJogo.Classe classe, Runnable aoSelecionar,
                     Runnable aoAlternar) {
            this.classe = classe;
            this.aoSelecionar = aoSelecionar;
            this.aoAlternar = aoAlternar;
            setOpaque(false);
            setFocusable(true);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(340, 274));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    requestFocusInWindow();
                    selecionar();
                }
            });
            addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    selecionar();
                }
            });

            getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke("LEFT"), "alternar");
            getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke("RIGHT"), "alternar");
            getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke("ENTER"), "selecionar");
            getActionMap().put("alternar", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    aoAlternar.run();
                }
            });
            getActionMap().put("selecionar", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    selecionar();
                }
            });
        }

        private void selecionar() {
            aoSelecionar.run();
        }

        void setSelecionado(boolean selecionado) {
            this.selecionado = selecionado;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                AssetManager.preparar(g2);
                int w = getWidth();
                int h = getHeight();
                Color fundo = selecionado ? new Color(28, 40, 34) : new Color(18, 26, 40);
                Estilo.painelZelda(g2, 0, 0, w, h, fundo);

                if (selecionado) {
                    g2.setColor(new Color(240, 198, 75, 22));
                    g2.fillRect(9, 9, w - 18, h - 18);
                    g2.setColor(Estilo.OURO_CLARO);
                    g2.drawRect(2, 2, w - 5, h - 5);
                }

                Estilo.sombraProjetada(g2, w / 2, 140, 92);
                AssetManager.desenhar(g2, Sprites.daClasse(classe),
                        w / 2 - 64, 16, 128, 128);

                PixelFont.centralizadoComSombra(g2, classe.getNome(), w / 2, 154,
                        2, selecionado ? Estilo.OURO_CLARO : Estilo.CREME, Estilo.PRETO);

                Estilo.textoQuebrado(g2, classe.getDescricao(), 24, 182, w - 48,
                        Estilo.TEXTO, Estilo.FONTE_PEQUENA, 17);

                String atributos = "VIDA " + classe.getVidaBase()
                        + "   ATQ " + classe.getAtaqueBase()
                        + "   DEF " + classe.getDefesaBase();
                PixelFont.desenharCentralizado(g2, atributos, w / 2, h - 54, 1,
                        Estilo.OURO_CLARO, Estilo.PRETO);
                if (classe.getManaBase() > 0) {
                    PixelFont.desenharCentralizado(g2, "MANA " + classe.getManaBase(),
                            w / 2, h - 34, 1, Estilo.AZUL, Estilo.PRETO);
                } else {
                    PixelFont.desenharCentralizado(g2, "FURIA AO ATACAR",
                            w / 2, h - 34, 1, Estilo.LARANJA, Estilo.PRETO);
                }
            } finally {
                g2.dispose();
            }
        }
    }
}
