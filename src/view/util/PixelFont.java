package view.util;

import java.awt.Color;
import java.awt.Graphics2D;
import java.text.Normalizer;
import java.util.HashMap;
import java.util.Map;

/**
 * Fonte pixel 5x7 desenhada em blocos, para títulos e HUD.
 * Somente caixa alta, números e pouca pontuação; acentos são removidos.
 */
public final class PixelFont {

    private static final int LARGURA_GLIFO = 5;
    private static final int ALTURA_GLIFO = 7;
    private static final int ESPACO = 1;
    private static final Map<Character, String[]> GLIFOS = new HashMap<>();

    private PixelFont() {
    }

    static {
        g('A', "01110", "10001", "10001", "11111", "10001", "10001", "10001");
        g('B', "11110", "10001", "10001", "11110", "10001", "10001", "11110");
        g('C', "01111", "10000", "10000", "10000", "10000", "10000", "01111");
        g('D', "11110", "10001", "10001", "10001", "10001", "10001", "11110");
        g('E', "11111", "10000", "10000", "11110", "10000", "10000", "11111");
        g('F', "11111", "10000", "10000", "11110", "10000", "10000", "10000");
        g('G', "01111", "10000", "10000", "10111", "10001", "10001", "01111");
        g('H', "10001", "10001", "10001", "11111", "10001", "10001", "10001");
        g('I', "11111", "00100", "00100", "00100", "00100", "00100", "11111");
        g('J', "00111", "00010", "00010", "00010", "00010", "10010", "01100");
        g('K', "10001", "10010", "10100", "11000", "10100", "10010", "10001");
        g('L', "10000", "10000", "10000", "10000", "10000", "10000", "11111");
        g('M', "10001", "11011", "10101", "10101", "10001", "10001", "10001");
        g('N', "10001", "11001", "10101", "10011", "10001", "10001", "10001");
        g('O', "01110", "10001", "10001", "10001", "10001", "10001", "01110");
        g('P', "11110", "10001", "10001", "11110", "10000", "10000", "10000");
        g('Q', "01110", "10001", "10001", "10001", "10101", "10010", "01101");
        g('R', "11110", "10001", "10001", "11110", "10100", "10010", "10001");
        g('S', "01111", "10000", "10000", "01110", "00001", "00001", "11110");
        g('T', "11111", "00100", "00100", "00100", "00100", "00100", "00100");
        g('U', "10001", "10001", "10001", "10001", "10001", "10001", "01110");
        g('V', "10001", "10001", "10001", "10001", "10001", "01010", "00100");
        g('W', "10001", "10001", "10001", "10101", "10101", "10101", "01010");
        g('X', "10001", "10001", "01010", "00100", "01010", "10001", "10001");
        g('Y', "10001", "10001", "01010", "00100", "00100", "00100", "00100");
        g('Z', "11111", "00001", "00010", "00100", "01000", "10000", "11111");

        g('0', "01110", "10001", "10011", "10101", "11001", "10001", "01110");
        g('1', "00100", "01100", "00100", "00100", "00100", "00100", "01110");
        g('2', "01110", "10001", "00001", "00010", "00100", "01000", "11111");
        g('3', "11111", "00010", "00100", "00010", "00001", "10001", "01110");
        g('4', "00010", "00110", "01010", "10010", "11111", "00010", "00010");
        g('5', "11111", "10000", "11110", "00001", "00001", "10001", "01110");
        g('6', "00110", "01000", "10000", "11110", "10001", "10001", "01110");
        g('7', "11111", "00001", "00010", "00100", "01000", "01000", "01000");
        g('8', "01110", "10001", "10001", "01110", "10001", "10001", "01110");
        g('9', "01110", "10001", "10001", "01111", "00001", "00010", "01100");

        g(' ', "00000", "00000", "00000", "00000", "00000", "00000", "00000");
        g('.', "00000", "00000", "00000", "00000", "00000", "00100", "00100");
        g(',', "00000", "00000", "00000", "00000", "00100", "00100", "01000");
        g(':', "00000", "00100", "00100", "00000", "00100", "00100", "00000");
        g('!', "00100", "00100", "00100", "00100", "00100", "00000", "00100");
        g('?', "01110", "10001", "00001", "00010", "00100", "00000", "00100");
        g('-', "00000", "00000", "00000", "11111", "00000", "00000", "00000");
        g('+', "00000", "00100", "00100", "11111", "00100", "00100", "00000");
        g('=', "00000", "00000", "11111", "00000", "11111", "00000", "00000");
        g('/', "00001", "00010", "00010", "00100", "01000", "01000", "10000");
        g('>', "10000", "01000", "00100", "00010", "00100", "01000", "10000");
        g('<', "00001", "00010", "00100", "01000", "00100", "00010", "00001");
        g('%', "11001", "11010", "00010", "00100", "01000", "01011", "10011");
        g('*', "00000", "10101", "01110", "11111", "01110", "10101", "00000");
        g('\'', "00100", "00100", "00000", "00000", "00000", "00000", "00000");
    }

    private static void g(char c, String... linhas) {
        GLIFOS.put(c, linhas);
    }

    /** Remove acentos e converte para caixa alta. */
    public static String normalizar(String texto) {
        if (texto == null) {
            return "";
        }
        String semAcento = Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return semAcento.toUpperCase();
    }

    /** Largura em pixels de um texto na escala indicada. */
    public static int largura(String texto, int escala) {
        if (texto == null || texto.isEmpty()) {
            return 0;
        }
        return (texto.length() * (LARGURA_GLIFO + ESPACO) - ESPACO) * escala;
    }

    public static int altura(int escala) {
        return ALTURA_GLIFO * escala;
    }

    /** Desenha o texto com canto superior esquerdo em (x, y). */
    public static void desenhar(Graphics2D g2, String texto, int x, int y,
                                int escala, Color cor) {
        String normalizado = normalizar(texto);
        g2.setColor(cor);
        int cursorX = x;
        for (int i = 0; i < normalizado.length(); i++) {
            String[] glifo = GLIFOS.get(normalizado.charAt(i));
            if (glifo != null) {
                for (int linha = 0; linha < ALTURA_GLIFO; linha++) {
                    String pixels = glifo[linha];
                    for (int coluna = 0; coluna < LARGURA_GLIFO; coluna++) {
                        if (pixels.charAt(coluna) == '1') {
                            g2.fillRect(cursorX + coluna * escala,
                                    y + linha * escala, escala, escala);
                        }
                    }
                }
            }
            cursorX += (LARGURA_GLIFO + ESPACO) * escala;
        }
    }

    /** Desenha centralizado horizontalmente em relação a centroX. */
    public static void desenharCentralizado(Graphics2D g2, String texto, int centroX,
                                            int y, int escala, Color cor) {
        desenhar(g2, texto, centroX - largura(normalizar(texto), escala) / 2,
                y, escala, cor);
    }

    /** Desenha centralizado com sombra preta projetada de 2px. */
    public static void desenharCentralizado(Graphics2D g2, String texto, int centroX,
                                            int y, int escala, Color cor, Color sombra) {
        int x = centroX - largura(normalizar(texto), escala) / 2;
        desenharComSombra(g2, texto, x, y, escala, cor, sombra);
    }

    /** Desenha com sombra preta projetada de 2px, melhorando o contraste. */
    public static void desenharComSombra(Graphics2D g2, String texto, int x, int y,
                                         int escala, Color cor, Color sombra) {
        desenhar(g2, texto, x + 2, y + 2, escala, sombra);
        desenhar(g2, texto, x, y, escala, cor);
    }

    public static void centralizadoComSombra(Graphics2D g2, String texto, int centroX,
                                             int y, int escala, Color cor, Color sombra) {
        int x = centroX - largura(normalizar(texto), escala) / 2;
        desenharComSombra(g2, texto, x, y, escala, cor, sombra);
    }
}
