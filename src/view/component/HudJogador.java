package view.component;

import controller.GerenciadorJogo;
import view.util.AssetManager;
import view.util.Estilo;
import view.util.PixelFont;
import view.util.Sprites;

import javax.swing.JComponent;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

/** HUD no estilo Zelda: corações, mana/fúria, XP e nível com sombras. */
public class HudJogador extends JComponent {

    private final GerenciadorJogo jogo;

    public HudJogador(GerenciadorJogo jogo) {
        this.jogo = jogo;
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            AssetManager.preparar(g2);
            int w = getWidth();
            int h = getHeight();

            Estilo.painelZelda(g2, 0, 0, w, h, Estilo.FUNDO_HUD);
            g2.setColor(new Color(255, 240, 180, 30));
            g2.fillRect(10, 9, w - 20, 5);
            g2.setColor(Estilo.OURO_ESCURO);
            g2.fillRect(8, h - 4, w - 16, 2);

            PixelFont.desenharComSombra(g2, jogo.getNomeJogador(), 18, 12, 2,
                    Estilo.CREME, Estilo.PRETO);
            PixelFont.desenharComSombra(g2, jogo.getNomeClasse() + "  NIVEL "
                            + jogo.getNivel(), 18, 34, 1, Estilo.OURO, Estilo.PRETO);

            // painel interno dos corações
            Estilo.painel(g2, 14, 44, 368, 38, new Color(6, 14, 10),
                    Estilo.OURO_ESCURO, new Color(2, 6, 4));
            Estilo.coracoes(g2, 20, 47, jogo.getVida(), jogo.getVidaTotal(), 2);

            // vida numérica + barra
            PixelFont.desenharComSombra(g2, "VIDA " + jogo.getVida() + "/"
                            + jogo.getVidaTotal(), 410, 14, 1,
                    Estilo.TEXTO, Estilo.PRETO);
            Estilo.barra(g2, 410, 30, 290, 16,
                    jogo.getVidaTotal() == 0 ? 0
                            : jogo.getVida() / (double) jogo.getVidaTotal(),
                    Estilo.VERMELHO, Estilo.VERMELHO_ESCURO);

            // mana do mago ou fúria do guerreiro
            if (jogo.isMago()) {
                AssetManager.desenhar(g2, Sprites.MANA, 410, 50, 32, 32);
                PixelFont.desenharComSombra(g2, "MANA " + jogo.getMana() + "/"
                                + jogo.getManaTotal(), 450, 52, 1,
                        Estilo.TEXTO, Estilo.PRETO);
                Estilo.barra(g2, 450, 68, 250, 14,
                        jogo.getManaTotal() == 0 ? 0
                                : jogo.getMana() / (double) jogo.getManaTotal(),
                        Estilo.AZUL, Estilo.AZUL_ESCURO);
            } else {
                PixelFont.desenharComSombra(g2, "FURIA " + jogo.getFuria() + "/100",
                        410, 52, 1, Estilo.TEXTO, Estilo.PRETO);
                Estilo.barra(g2, 410, 68, 290, 14,
                        jogo.getFuria() / 100.0, Estilo.LARANJA,
                        new Color(0x5C2C10));
            }

            // bloco de XP no canto direito
            desenharRupee(g2, w - 190, 12);
            PixelFont.desenharComSombra(g2, "XP " + jogo.getXp(),
                    w - 164, 14, 2, Estilo.OURO_CLARO, Estilo.PRETO);
            int proximo = jogo.getXpProximoNivel();
            String nivel = proximo > 0 ? "PROXIMO: " + proximo : "NIVEL MAXIMO";
            PixelFont.desenharComSombra(g2, nivel,
                    w - PixelFont.largura(nivel, 1) - 18, 38, 1,
                    Estilo.TEXTO_APAGADO, Estilo.PRETO);
            String abates = "ABATES " + jogo.getInimigosDerrotados();
            PixelFont.desenharComSombra(g2, abates,
                    w - PixelFont.largura(abates, 1) - 18, 54, 1,
                    Estilo.TEXTO_APAGADO, Estilo.PRETO);
        } finally {
            g2.dispose();
        }
    }

    /** Pequena rupee dourada desenhada em pixels. */
    private void desenharRupee(Graphics2D g2, int x, int y) {
        g2.setColor(Estilo.OURO_ESCURO);
        g2.fillRect(x + 5, y, 4, 2);
        g2.fillRect(x + 3, y + 2, 8, 2);
        g2.fillRect(x + 1, y + 4, 12, 8);
        g2.fillRect(x + 3, y + 12, 8, 2);
        g2.fillRect(x + 5, y + 14, 4, 2);
        g2.setColor(Estilo.OURO);
        g2.fillRect(x + 5, y + 2, 4, 2);
        g2.fillRect(x + 3, y + 4, 8, 6);
        g2.fillRect(x + 5, y + 10, 4, 2);
        g2.setColor(Estilo.OURO_CLARO);
        g2.fillRect(x + 4, y + 4, 2, 2);
        g2.fillRect(x + 5, y + 3, 1, 1);
    }
}
