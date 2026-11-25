package view.component;

import view.util.AssetManager;
import view.util.Estilo;
import view.util.PixelFont;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/** Botão desenhado à mão no estilo 8-bits, com hover, foco e teclado. */
public class BotaoPixel extends JComponent {

    private final String texto;
    private final Runnable acao;
    private String atalho;
    private boolean hover;
    private boolean pressionado;

    public BotaoPixel(String texto, Runnable acao) {
        this(texto, acao, 180, 44);
    }

    public BotaoPixel(String texto, Runnable acao, int largura, int altura) {
        this.texto = texto;
        this.acao = acao;
        setPreferredSize(new Dimension(largura, altura));
        setMinimumSize(new Dimension(largura, altura));
        setOpaque(false);
        setFocusable(true);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!isEnabled()) return;
                hover = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hover = false;
                pressionado = false;
                repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (!isEnabled() || e.getButton() != MouseEvent.BUTTON1) return;
                pressionado = true;
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (!isEnabled() || e.getButton() != MouseEvent.BUTTON1) return;
                boolean dentro = contains(e.getPoint());
                pressionado = false;
                repaint();
                if (dentro) {
                    clicar();
                }
            }
        });

        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                repaint();
            }
        });

        getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke("ENTER"), "acionar");
        getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke("SPACE"), "acionar");
        getActionMap().put("acionar", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clicar();
            }
        });
    }

    public void setAtalho(String atalho) {
        this.atalho = atalho;
        repaint();
    }

    public String getTexto() {
        return texto;
    }

    public void clicar() {
        if (isEnabled() && acao != null) {
            acao.run();
        }
    }

    @Override
    public void setEnabled(boolean habilitado) {
        super.setEnabled(habilitado);
        setCursor(Cursor.getPredefinedCursor(
                habilitado ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            Dimension tamanho = Estilo.escalarComponente(g2, this);
            AssetManager.preparar(g2);
            int w = tamanho.width;
            int h = tamanho.height;
            boolean ativo = isEnabled();
            boolean destaque = ativo && (hover || hasFocus());

            Color base = ativo
                    ? (destaque ? new Color(154, 100, 52) : new Color(112, 70, 34))
                    : new Color(68, 62, 68);
            Color baseClara = ativo
                    ? (destaque ? new Color(198, 138, 76) : new Color(154, 102, 54))
                    : new Color(94, 88, 96);
            Color baseEscura = ativo
                    ? (destaque ? new Color(108, 64, 30) : new Color(76, 46, 20))
                    : new Color(44, 40, 48);

            // sombra projetada
            g2.setColor(new Color(0, 0, 0, 125));
            g2.fillRect(5, 5, w - 1, h - 1);

            // moldura dupla dourada
            g2.setColor(new Color(14, 10, 20));
            g2.fillRect(0, 0, w, h);
            g2.setColor(destaque ? Estilo.OURO_CLARO : Estilo.OURO_ESCURO);
            g2.fillRect(1, 1, w - 2, h - 2);
            g2.setColor(destaque ? Estilo.OURO : new Color(0xB89030));
            g2.fillRect(2, 2, w - 4, h - 4);
            g2.setColor(new Color(10, 8, 18));
            g2.fillRect(4, 4, w - 8, h - 8);

            // placa de madeira
            g2.setColor(base);
            g2.fillRect(5, 5, w - 10, h - 10);
            g2.setColor(baseClara);
            g2.fillRect(5, 5, w - 10, 2);
            g2.fillRect(5, 5, 2, h - 10);
            g2.setColor(baseEscura);
            g2.fillRect(5, h - 7, w - 10, 2);
            g2.fillRect(w - 7, 5, 2, h - 10);

            // veios da madeira
            g2.setColor(new Color(0, 0, 0, 40));
            for (int gx = 9; gx < w - 9; gx += 13) {
                g2.fillRect(gx, 8, 1, h - 16);
            }
            if (destaque) {
                g2.setColor(new Color(255, 246, 190, 120));
                g2.fillRect(7, 6, w - 14, 2);
            }

            // rebites
            int[][] rebites = {{6, 6}, {w - 9, 6}, {6, h - 9}, {w - 9, h - 9}};
            for (int[] p : rebites) {
                g2.setColor(Estilo.OURO);
                g2.fillRect(p[0], p[1], 3, 3);
                g2.setColor(Estilo.OURO_ESCURO);
                g2.fillRect(p[0] + 2, p[1] + 2, 1, 1);
            }

            if (pressionado) {
                g2.setColor(new Color(0, 0, 0, 90));
                g2.fillRect(3, 3, w - 6, h - 6);
            }

            // seta seletora dourada
            if (destaque) {
                int ax = 10;
                int ay = h / 2;
                Polygon borda = new Polygon(
                        new int[]{ax, ax + 9, ax}, new int[]{ay - 6, ay, ay + 6}, 3);
                g2.setColor(new Color(24, 14, 8));
                g2.fillPolygon(borda);
                Polygon seta = new Polygon(
                        new int[]{ax + 1, ax + 7, ax + 1},
                        new int[]{ay - 4, ay, ay + 4}, 3);
                g2.setColor(Estilo.OURO);
                g2.fillPolygon(seta);
                g2.setColor(Estilo.OURO_CLARO);
                g2.fillRect(ax + 2, ay - 2, 3, 1);
            }

            int escala = PixelFont.largura(texto, 2) + 30 <= w ? 2 : 1;
            Color corTexto = ativo
                    ? (destaque ? Estilo.OURO_CLARO : Estilo.CREME)
                    : Estilo.TEXTO_APAGADO;
            PixelFont.centralizadoComSombra(g2, texto, w / 2,
                    (h - PixelFont.altura(escala)) / 2,
                    escala, corTexto, Estilo.PRETO);

            if (atalho != null) {
                PixelFont.desenharComSombra(g2, atalho,
                        w - PixelFont.largura(atalho, 1) - 6,
                        h - PixelFont.altura(1) - 4, 1,
                        ativo ? Estilo.CREME : Estilo.TEXTO_APAGADO, Estilo.PRETO);
            }
        } finally {
            g2.dispose();
        }
    }
}
