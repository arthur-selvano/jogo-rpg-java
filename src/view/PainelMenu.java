package view;

import controller.GerenciadorJogo;
import view.component.DialogoPixel;
import view.util.AssetManager;
import view.util.Estilo;
import view.util.PixelFont;
import view.util.Sprites;

import javax.swing.AbstractAction;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Random;

/**
 * Menu inicial 16-bits: grama de Hyrule em mosaico, estrada de terra,
 * placa medieval e botões em placas douradas.
 */
public class PainelMenu extends JPanel {

    public static final int LARGURA = 960;
    public static final int ALTURA = 600;

    private static final int LARGURA_BOTAO = 288;
    private static final int ALTURA_BOTAO = 96;

    private final TelaPrincipal tela;
    private final GerenciadorJogo jogo;
    private final Rectangle[] areas = new Rectangle[2];
    private final Timer piscaSelecao;

    private int selecionado;
    private int pressionado = -1;
    private boolean piscar = true;

    public PainelMenu(TelaPrincipal tela, GerenciadorJogo jogo) {
        this.tela = tela;
        this.jogo = jogo;

        setPreferredSize(new Dimension(LARGURA, ALTURA));
        setFocusable(true);
        setLayout(null);
        setBackground(Estilo.PRETO);

        int x = (LARGURA - LARGURA_BOTAO) / 2;
        areas[0] = new Rectangle(x, 330, LARGURA_BOTAO, ALTURA_BOTAO);
        areas[1] = new Rectangle(x, 438, LARGURA_BOTAO, ALTURA_BOTAO);

        MouseAdapter mouse = new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int indice = indiceNaPosicao(e.getPoint());
                setCursor(Cursor.getPredefinedCursor(
                        indice >= 0 ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
                if (indice >= 0 && indice != selecionado) {
                    selecionado = indice;
                    repaint();
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    pressionado = indiceNaPosicao(e.getPoint());
                    repaint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                pressionado = -1;
                repaint();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                int indice = indiceNaPosicao(e.getPoint());
                if (indice >= 0 && e.getButton() == MouseEvent.BUTTON1) {
                    selecionado = indice;
                    ativar(indice);
                }
            }
        };
        addMouseListener(mouse);
        addMouseMotionListener(mouse);

        piscaSelecao = new Timer(420, e -> {
            piscar = !piscar;
            if (isShowing()) {
                repaint();
            }
        });
        piscaSelecao.start();

        instalarTeclado();
    }

    private void instalarTeclado() {
        getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke("UP"), "cima");
        getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke("W"), "cima");
        getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke("DOWN"), "baixo");
        getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke("S"), "baixo");
        getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke("ENTER"), "confirmar");
        getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke("SPACE"), "confirmar");
        getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke("ESCAPE"), "sair");

        getActionMap().put("cima", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selecionado = (selecionado + areas.length - 1) % areas.length;
                repaint();
            }
        });
        getActionMap().put("baixo", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selecionado = (selecionado + 1) % areas.length;
                repaint();
            }
        });
        getActionMap().put("confirmar", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ativar(selecionado);
            }
        });
        getActionMap().put("sair", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selecionado = 1;
                ativar(1);
            }
        });
    }

    /** Chamado pela TelaPrincipal sempre que o menu é exibido. */
    public void aoExibir() {
        selecionado = 0;
        requestFocusInWindow();
        repaint();
    }

    private int indiceNaPosicao(Point ponto) {
        ponto = Estilo.posicaoNaTela(this, ponto, LARGURA, ALTURA);
        for (int i = 0; i < areas.length; i++) {
            if (areas[i].contains(ponto)) {
                return i;
            }
        }
        return -1;
    }

    private void ativar(int indice) {
        if (indice == 0) {
            jogo.reiniciar();
            tela.mostrarCriacao();
        } else if (DialogoPixel.confirmar(this, "SAIR DO JOGO",
                "Deseja mesmo abandonar a aventura?")) {
            tela.dispose();
            System.exit(0);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            Estilo.escalarTela(g2, this, LARGURA, ALTURA);
            AssetManager.preparar(g2);
            int w = LARGURA;
            int h = ALTURA;

            Estilo.fundoGrama(g2, w, h);
            desenharTrilha(g2, w, h);
            Estilo.vinheta(g2, w, h);

            desenharTitulo(g2, w);
            desenharPersonagens(g2);
            desenharBotoes(g2);
            desenharRodape(g2, w, h);
        } finally {
            g2.dispose();
        }
    }

    /** Estrada de terra no centro, ladeada por grama e pedrinhas. */
    private void desenharTrilha(Graphics2D g2, int w, int h) {
        int px = 294;
        int pw = 372;

        g2.setColor(new Color(0, 0, 0, 55));
        g2.fillRect(px + 8, 0, pw, h);
        g2.setColor(new Color(82, 56, 30));
        g2.fillRect(px, 0, pw, h);
        g2.setColor(new Color(116, 82, 44));
        g2.fillRect(px + 7, 0, pw - 14, h);
        g2.setColor(new Color(138, 100, 54));
        g2.fillRect(px + 17, 0, pw - 34, h);

        Random r = new Random(11);
        for (int y = 0; y < h; y += 16) {
            int e = 3 + r.nextInt(7);
            g2.setColor(new Color(70, 48, 26));
            g2.fillRect(px + e, y, 9, 1);
            g2.fillRect(px + pw - e - 9, y + 8, 9, 1);
        }
        for (int i = 0; i < 30; i++) {
            int x = px + 22 + r.nextInt(pw - 44);
            int y = r.nextInt(h);
            g2.setColor(new Color(108, 98, 84));
            g2.fillRect(x, y, 3, 2);
            g2.setColor(new Color(150, 140, 122));
            g2.fillRect(x, y, 2, 1);
        }
        for (int y = 10; y < h; y += 24) {
            g2.setColor(new Color(42, 118, 52));
            g2.fillRect(px + 1, y, 3, 7);
            g2.fillRect(px + pw - 5, y + 9, 3, 7);
            g2.setColor(new Color(94, 184, 94));
            g2.fillRect(px + 2, y, 1, 4);
            g2.fillRect(px + pw - 4, y + 9, 1, 4);
        }

        for (int i = 0; i < 8; i++) {
            g2.setColor(new Color(8, 20, 12, 26 - i * 3));
            g2.fillRect(0, i * 30, w, 30);
        }
        g2.setColor(new Color(8, 20, 12, 130));
        g2.fillRect(0, 548, w, h - 548);
        g2.setColor(Estilo.OURO_ESCURO);
        g2.fillRect(0, 548, w, 2);
    }

    private void desenharTitulo(Graphics2D g2, int w) {
        int largura = 576;
        int altura = 144;
        int x = (w - largura) / 2;
        g2.setColor(new Color(0, 0, 0, 100));
        g2.fillRect(x + 8, 38, largura, altura);
        AssetManager.desenhar(g2, Sprites.TITULO, x, 30, largura, altura);

        PixelFont.desenharCentralizado(g2, "A LENDA DO REINO PERDIDO",
                w / 2, 194, 2, Estilo.CREME, Estilo.PRETO);

        g2.setColor(Estilo.OURO_ESCURO);
        g2.fillRect(w / 2 - 240, 220, 480, 3);
        g2.setColor(Estilo.OURO);
        g2.fillRect(w / 2 - 8, 217, 16, 9);
        g2.setColor(Estilo.OURO_CLARO);
        g2.fillRect(w / 2 - 4, 219, 8, 2);
    }

    private void desenharPersonagens(Graphics2D g2) {
        Estilo.sombraProjetada(g2, 128, 532, 88);
        AssetManager.desenhar(g2, Sprites.PLAYER, 80, 436, 96, 96);

        Estilo.sombraProjetada(g2, 844, 532, 88);
        AssetManager.desenhar(g2, Sprites.DRAGAO, 796, 436, 96, 96);
    }

    private void desenharBotoes(Graphics2D g2) {
        for (int i = 0; i < areas.length; i++) {
            Rectangle area = areas[i];
            boolean ativo = i == selecionado;
            String sprite = i == 0
                    ? (ativo ? Sprites.BTN_JOGAR_HOVER : Sprites.BTN_JOGAR)
                    : (ativo ? Sprites.BTN_SAIR_HOVER : Sprites.BTN_SAIR);

            g2.setColor(new Color(0, 0, 0, 120));
            g2.fillRect(area.x + 7, area.y + 7, area.width, area.height);

            if (ativo) {
                int alpha = piscar ? 150 : 70;
                g2.setColor(new Color(255, 220, 100, alpha));
                g2.drawRect(area.x - 4, area.y - 4, area.width + 7, area.height + 7);
                g2.drawRect(area.x - 5, area.y - 5, area.width + 9, area.height + 9);
            }

            int deslocamento = pressionado == i ? 4 : 0;
            AssetManager.desenhar(g2, sprite, area.x, area.y + deslocamento,
                    area.width, area.height);
            if (pressionado == i) {
                g2.setColor(new Color(0, 0, 0, 90));
                g2.fillRect(area.x, area.y + deslocamento, area.width, area.height);
            }

            if (ativo && piscar) {
                int sx = area.x + 34;
                int sy = area.y + area.height / 2 - 22;
                g2.setColor(Estilo.OURO_CLARO);
                g2.fillRect(sx + 2, sy, 3, 9);
                g2.fillRect(sx, sy + 3, 9, 3);
                g2.setColor(Color.WHITE);
                g2.fillRect(sx + 3, sy + 4, 2, 2);
            }
        }
    }

    private void desenharRodape(Graphics2D g2, int w, int h) {
        g2.setColor(Estilo.OURO_ESCURO);
        g2.fillRect(40, 548, w - 80, 2);
        PixelFont.desenharCentralizado(g2,
                "ENTER CONFIRMA   -   SETAS NAVEGAM   -   ESC SAI",
                w / 2, 560, 1, Estilo.CREME, Estilo.PRETO);
        PixelFont.desenharCentralizado(g2,
                "CRIADO COM JAVA SWING  -  ESTILO 16-BITS",
                w / 2, 578, 1, new Color(0xC0C8BC), Estilo.PRETO);
    }
}
