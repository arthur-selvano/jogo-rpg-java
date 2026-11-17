package view.util;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

/** Cores, fontes e primitivas visuais no estilo 16-bits (Zelda SNES/GBA). */
public final class Estilo {

    public static final Color PRETO = new Color(0x0B0A14);
    public static final Color FUNDO_CAIXA = new Color(18, 30, 54);
    public static final Color FUNDO_PAINEL = new Color(16, 26, 42);
    public static final Color FUNDO_HUD = new Color(12, 24, 18);
    public static final Color BORDA_ESCURA = new Color(40, 42, 66);
    public static final Color BORDA_CLARA = new Color(0x9A9AB8);
    public static final Color OURO = new Color(0xF0C64B);
    public static final Color OURO_CLARO = new Color(0xFFE07A);
    public static final Color OURO_ESCURO = new Color(0x9C6E1C);
    public static final Color CREME = new Color(0xF8EED2);
    public static final Color TEXTO = new Color(0xEEF0F8);
    public static final Color TEXTO_APAGADO = new Color(0xA8A4BC);
    public static final Color VERMELHO = new Color(0xD2323C);
    public static final Color VERMELHO_ESCURO = new Color(0x701C24);
    public static final Color VERDE = new Color(0x54C868);
    public static final Color VERDE_ESCURO = new Color(0x1E5C2A);
    public static final Color AZUL = new Color(0x3C78EB);
    public static final Color AZUL_ESCURO = new Color(0x1C2C6C);
    public static final Color LARANJA = new Color(0xE8822C);
    public static final Color ROXO = new Color(0x6C3CBC);
    public static final Color ROXO_ESCURO = new Color(0x241A44);
    public static final Color PEDRA = new Color(0x2C2A3E);
    public static final Color PEDRA_CLARA = new Color(0x45435C);
    public static final Color SOMBRA_TEXTO = new Color(0, 0, 0, 235);

    public static final Font FONTE_CORPO = new Font("Monospaced", Font.BOLD, 16);
    public static final Font FONTE_PEQUENA = new Font("Monospaced", Font.BOLD, 13);

    private static final Color[] CEU = {
            new Color(0x0B0E26), new Color(0x101636), new Color(0x161D46),
            new Color(0x1D2556), new Color(0x252D66), new Color(0x2D3574),
            new Color(0x363D82), new Color(0x404690)
    };

    private Estilo() {
    }

    public static Color misturar(Color a, Color b, double t) {
        double f = Math.max(0, Math.min(1, t));
        return new Color(
                (int) Math.round(a.getRed() + (b.getRed() - a.getRed()) * f),
                (int) Math.round(a.getGreen() + (b.getGreen() - a.getGreen()) * f),
                (int) Math.round(a.getBlue() + (b.getBlue() - a.getBlue()) * f),
                a.getAlpha());
    }

    /** Fundo em faixas horizontais (usado em telas noturnas). */
    public static void fundoCeu(Graphics2D g2, int largura, int altura) {
        int faixa = Math.max(1, altura / CEU.length);
        for (int i = 0; i < CEU.length; i++) {
            g2.setColor(CEU[i]);
            int y = i * faixa;
            int h = (i == CEU.length - 1) ? altura - y : faixa;
            g2.fillRect(0, y, largura, h);
        }
    }

    /** Fundo de grama de Hyrule em mosaico, com leve véu para contraste. */
    public static void fundoGrama(Graphics2D g2, int largura, int altura) {
        AssetManager.desenharMosaico(g2, Sprites.BG_TILE, largura, altura);
        g2.setColor(new Color(6, 20, 12, 46));
        g2.fillRect(0, 0, largura, altura);
    }

    /** Escurece toda a cena com uma cor translúcida. */
    public static void escurecer(Graphics2D g2, int largura, int altura, Color cor) {
        g2.setColor(cor);
        g2.fillRect(0, 0, largura, altura);
    }

    /** Vinheta em faixas nos cantos superior e inferior. */
    public static void vinheta(Graphics2D g2, int largura, int altura) {
        for (int i = 0; i < 7; i++) {
            g2.setColor(new Color(0, 0, 0, 8 + i * 3));
            g2.fillRect(0, i * 18, largura, 18);
            g2.fillRect(0, altura - (i + 1) * 18, largura, 18);
        }
        for (int i = 0; i < 5; i++) {
            g2.setColor(new Color(0, 0, 0, 6 + i * 3));
            g2.fillRect(i * 20, 0, 20, altura);
            g2.fillRect(largura - (i + 1) * 20, 0, 20, altura);
        }
    }

    /** Sombra projetada pixelada sob personagens. */
    public static void sombraProjetada(Graphics2D g2, int centroX, int baseY, int largura) {
        int w = Math.max(8, largura);
        g2.setColor(new Color(0, 0, 0, 115));
        g2.fillRect(centroX - w / 2, baseY, w, 5);
        g2.setColor(new Color(0, 0, 0, 80));
        g2.fillRect(centroX - w / 2 + 5, baseY + 5, w - 10, 4);
        g2.setColor(new Color(0, 0, 0, 50));
        g2.fillRect(centroX - w / 2 + 12, baseY + 9, w - 24, 2);
    }

    /** Painel simples com sombra e bisel. */
    public static void painel(Graphics2D g2, int x, int y, int largura, int altura,
                              Color fundo, Color bordaClara, Color bordaEscura) {
        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRect(x + 5, y + 5, largura, altura);
        g2.setColor(fundo);
        g2.fillRect(x, y, largura, altura);
        g2.setColor(bordaClara);
        g2.fillRect(x, y, largura, 3);
        g2.fillRect(x, y, 3, altura);
        g2.setColor(bordaEscura);
        g2.fillRect(x, y + altura - 3, largura, 3);
        g2.fillRect(x + largura - 3, y, 3, altura);
    }

    /** Painel ornamentado com moldura dupla dourada e rebites. */
    public static void painelZelda(Graphics2D g2, int x, int y, int largura, int altura) {
        painelZelda(g2, x, y, largura, altura, FUNDO_PAINEL);
    }

    public static void painelZelda(Graphics2D g2, int x, int y, int largura, int altura,
                                   Color fundo) {
        g2.setColor(new Color(0, 0, 0, 125));
        g2.fillRect(x + 6, y + 6, largura, altura);

        g2.setColor(new Color(10, 8, 18));
        g2.fillRect(x, y, largura, altura);
        g2.setColor(OURO_ESCURO);
        g2.fillRect(x + 2, y + 2, largura - 4, altura - 4);
        g2.setColor(OURO);
        g2.fillRect(x + 4, y + 4, largura - 8, altura - 8);
        g2.setColor(new Color(10, 8, 18));
        g2.fillRect(x + 6, y + 6, largura - 12, altura - 12);
        g2.setColor(fundo);
        g2.fillRect(x + 8, y + 8, largura - 16, altura - 16);

        // bisel interno
        g2.setColor(new Color(255, 240, 180, 34));
        g2.fillRect(x + 8, y + 8, largura - 16, 4);
        g2.setColor(new Color(0, 0, 0, 60));
        g2.fillRect(x + 8, y + altura - 12, largura - 16, 4);

        // rebites dos cantos
        int[][] cantos = {
                {x + 5, y + 5}, {x + largura - 9, y + 5},
                {x + 5, y + altura - 9}, {x + largura - 9, y + altura - 9}
        };
        for (int[] p : cantos) {
            g2.setColor(OURO);
            g2.fillRect(p[0], p[1], 4, 4);
            g2.setColor(OURO_ESCURO);
            g2.fillRect(p[0] + 1, p[1] + 1, 2, 2);
        }
    }

    /** Caixa de diálogo ornamentada (arte de ui/dialog_box.png). */
    public static void caixaDialogo(Graphics2D g2, int x, int y, int largura, int altura) {
        AssetManager.caixaDialogo(g2, x, y, largura, altura);
    }

    /** Barra de HP/Mana com moldura dourada, bisel e brilho. */
    public static void barra(Graphics2D g2, int x, int y, int largura, int altura,
                             double fracao, Color cor, Color corFundo) {
        double f = Math.max(0, Math.min(1, fracao));
        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRect(x - 3, y - 3, largura + 6, altura + 6);
        g2.setColor(OURO_ESCURO);
        g2.fillRect(x - 2, y - 2, largura + 4, altura + 4);
        g2.setColor(OURO);
        g2.fillRect(x - 2, y - 2, largura + 4, 1);
        g2.setColor(new Color(10, 8, 18));
        g2.fillRect(x - 1, y - 1, largura + 2, altura + 2);

        g2.setColor(corFundo);
        g2.fillRect(x, y, largura, altura);

        int preenchido = (int) Math.round(largura * f);
        if (preenchido > 0) {
            Color claro = misturar(cor, Color.WHITE, 0.32);
            Color escuro = misturar(cor, Color.BLACK, 0.38);
            g2.setColor(cor);
            g2.fillRect(x, y, preenchido, altura);
            g2.setColor(claro);
            g2.fillRect(x, y, preenchido, Math.max(1, altura / 3));
            g2.setColor(escuro);
            g2.fillRect(x, y + altura - Math.max(1, altura / 4),
                    preenchido, Math.max(1, altura / 4));
            g2.setColor(new Color(255, 255, 255, 100));
            g2.fillRect(x + 1, y + 1, Math.max(0, preenchido - 2), 1);
        }

        g2.setColor(new Color(0, 0, 0, 55));
        for (int sx = x + 10; sx < x + largura; sx += 10) {
            g2.fillRect(sx, y + 2, 1, altura - 4);
        }
        g2.setColor(OURO_ESCURO);
        g2.drawRect(x - 1, y - 1, largura + 1, altura + 1);
    }

    /**
     * Desenha no máximo 10 corações, cada um valendo 1/10 da vida máxima.
     * Os vazios permanecem visíveis para indicar o dano sofrido.
     */
    public static void coracoes(Graphics2D g2, int x, int y, int vida, int vidaTotal,
                                int escala) {
        final int total = 10;
        double porCoracao = Math.max(1.0, vidaTotal / (double) total);
        int cheios = (int) Math.ceil(Math.max(0, vida) / porCoracao);
        cheios = Math.max(0, Math.min(total, cheios));
        int lado = 16 * escala;
        int espaco = Math.max(2, escala);
        for (int i = 0; i < total; i++) {
            String sprite = i < cheios ? Sprites.CORACAO_CHEIO : Sprites.CORACAO_VAZIO;
            AssetManager.desenhar(g2, sprite, x + i * (lado + espaco), y, lado, lado);
        }
    }

    /** Texto com sombra preta de 2px (para HUD, diálogos e títulos). */
    public static void textoComSombra(Graphics2D g2, String texto, int x, int y,
                                      Color cor, Font fonte) {
        g2.setFont(fonte);
        g2.setColor(SOMBRA_TEXTO);
        g2.drawString(texto, x + 2, y + 2);
        g2.setColor(cor);
        g2.drawString(texto, x, y);
    }

    /** Desenha texto quebrado em linhas, com sombra de 2px. */
    public static int textoQuebrado(Graphics2D g2, String texto, int x, int y,
                                    int larguraMax, Color cor, Font fonte, int alturaLinha) {
        g2.setFont(fonte);
        FontMetrics fm = g2.getFontMetrics();
        List<String> linhas = quebrar(texto, fm, larguraMax);
        int cursor = y;
        for (String linha : linhas) {
            g2.setColor(SOMBRA_TEXTO);
            g2.drawString(linha, x + 2, cursor + 2);
            g2.setColor(cor);
            g2.drawString(linha, x, cursor);
            cursor += alturaLinha;
        }
        return cursor - y;
    }

    public static List<String> quebrar(String texto, FontMetrics fm, int larguraMax) {
        List<String> linhas = new ArrayList<>();
        if (texto == null || texto.isEmpty()) {
            return linhas;
        }
        for (String paragrafo : texto.split("\\n")) {
            if (paragrafo.isEmpty()) {
                linhas.add("");
                continue;
            }
            StringBuilder atual = new StringBuilder();
            for (String palavra : paragrafo.split(" ")) {
                String tentativa = atual.length() == 0 ? palavra : atual + " " + palavra;
                if (fm.stringWidth(tentativa) > larguraMax && atual.length() > 0) {
                    linhas.add(atual.toString());
                    atual = new StringBuilder(palavra);
                } else {
                    atual.setLength(0);
                    atual.append(tentativa);
                }
            }
            linhas.add(atual.toString());
        }
        return linhas;
    }

    /** Pequenas estrelas decorativas determinísticas. */
    public static void estrelas(Graphics2D g2, int largura, int altura, int quantidade) {
        java.util.Random r = new java.util.Random(42);
        for (int i = 0; i < quantidade; i++) {
            int x = r.nextInt(Math.max(1, largura));
            int y = r.nextInt(Math.max(1, altura));
            int tamanho = r.nextInt(4) == 0 ? 3 : 2;
            g2.setColor(r.nextInt(3) == 0 ? CREME : TEXTO_APAGADO);
            g2.fillRect(x, y, tamanho, tamanho);
        }
    }

    /** Fundo de pedra com tijolos (mantido para telas de masmorra). */
    public static void fundoPedra(Graphics2D g2, int largura, int altura) {
        g2.setColor(PEDRA);
        g2.fillRect(0, 0, largura, altura);
        g2.setColor(new Color(0x232136));
        for (int y = 0; y < altura; y += 32) {
            g2.fillRect(0, y, largura, 2);
            int deslocamento = ((y / 32) % 2) * 32;
            for (int x = deslocamento; x < largura; x += 64) {
                g2.fillRect(x, y, 2, 32);
            }
        }
        g2.setColor(new Color(0x35334C));
        for (int y = 2; y < altura; y += 32) {
            for (int x = ((y / 32) % 2) * 32; x < largura; x += 64) {
                g2.fillRect(x + 2, y, 60, 1);
            }
        }
    }
}
