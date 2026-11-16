package view.util;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Carrega e escala as imagens de {@code resources/assets} mantendo a
 * pixel art nítida (nearest neighbor, sem antialiasing).
 */
public final class AssetManager {

    private static final Map<String, BufferedImage> CACHE = new HashMap<>();
    private static final Map<String, BufferedImage> CACHE_ESCALADO = new HashMap<>();

    private AssetManager() {
    }

    /** Carrega (com cache) um asset relativo à pasta {@code assets}. */
    public static BufferedImage get(String caminho) {
        BufferedImage imagem = CACHE.get(caminho);
        if (imagem != null) {
            return imagem;
        }
        try (InputStream entrada = AssetManager.class.getResourceAsStream("/assets/" + caminho)) {
            if (entrada == null) {
                throw new IllegalStateException("Asset não encontrado: /assets/" + caminho);
            }
            imagem = ImageIO.read(entrada);
            if (imagem == null) {
                throw new IllegalStateException("Arquivo de imagem inválido: " + caminho);
            }
            CACHE.put(caminho, imagem);
            return imagem;
        } catch (IOException e) {
            throw new IllegalStateException("Falha ao carregar o asset: " + caminho, e);
        }
    }

    /** Versão escalada com cache, para não reescalar a cada repaint. */
    public static BufferedImage escalado(String caminho, int largura, int altura) {
        String chave = caminho + "@" + largura + "x" + altura;
        BufferedImage pronta = CACHE_ESCALADO.get(chave);
        if (pronta != null) {
            return pronta;
        }
        pronta = escalar(get(caminho), largura, altura);
        CACHE_ESCALADO.put(chave, pronta);
        return pronta;
    }

    /** Monta um fundo em mosaico (tiling) com cache por tamanho. */
    public static BufferedImage mosaico(String caminho, int largura, int altura) {
        String chave = "mosaico:" + caminho + "@" + largura + "x" + altura;
        BufferedImage pronta = CACHE_ESCALADO.get(chave);
        if (pronta != null) {
            return pronta;
        }
        BufferedImage tile = get(caminho);
        BufferedImage out = new BufferedImage(Math.max(1, largura),
                Math.max(1, altura), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = out.createGraphics();
        try {
            preparar(g2);
            for (int y = 0; y < out.getHeight(); y += tile.getHeight()) {
                for (int x = 0; x < out.getWidth(); x += tile.getWidth()) {
                    g2.drawImage(tile, x, y, null);
                }
            }
        } finally {
            g2.dispose();
        }
        CACHE_ESCALADO.put(chave, out);
        return out;
    }

    /** Desenha um fundo em mosaico cobrindo toda a área informada. */
    public static void desenharMosaico(Graphics2D g2, String caminho,
                                       int largura, int altura) {
        g2.drawImage(mosaico(caminho, largura, altura), 0, 0, null);
    }

    /** Escala uma imagem usando interpolação nearest neighbor. */
    public static BufferedImage escalar(BufferedImage origem, int largura, int altura) {
        int w = Math.max(1, largura);
        int h = Math.max(1, altura);
        BufferedImage destino = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = destino.createGraphics();
        try {
            preparar(g2);
            g2.drawImage(origem, 0, 0, w, h, null);
        } finally {
            g2.dispose();
        }
        return destino;
    }

    /** Configura o Graphics2D para pixel art: sem suavização. */
    public static void preparar(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_OFF);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
    }

    /** Desenha um asset já escalado na posição indicada. */
    public static void desenhar(Graphics2D g2, String caminho, int x, int y,
                                int largura, int altura) {
        g2.drawImage(escalado(caminho, largura, altura), x, y, null);
    }

    /**
     * Desenha a caixa de diálogo em nove fatias: cantos nítidos e bordas
     * esticadas, permitindo qualquer tamanho sem deformar o traço.
     */
    public static void caixaDialogo(Graphics2D g2, int x, int y, int largura, int altura) {
        BufferedImage caixa = get(Sprites.DIALOGO);
        int sw = caixa.getWidth();
        int sh = caixa.getHeight();
        int margem = 8;           // margem da arte original (cantos ornamentados)
        int borda = 24;           // espessura final da borda

        // sombra dura atrás da caixa
        g2.setColor(new Color(0, 0, 0, 110));
        g2.fillRect(x + 7, y + 7, largura, altura);

        // interior
        g2.setColor(Estilo.FUNDO_CAIXA);
        g2.fillRect(x + borda, y + borda,
                Math.max(0, largura - borda * 2), Math.max(0, altura - borda * 2));

        // cantos
        desenharFatia(g2, caixa, x, y, borda, borda,
                0, 0, margem, margem);
        desenharFatia(g2, caixa, x + largura - borda, y, borda, borda,
                sw - margem, 0, sw, margem);
        desenharFatia(g2, caixa, x, y + altura - borda, borda, borda,
                0, sh - margem, margem, sh);
        desenharFatia(g2, caixa, x + largura - borda, y + altura - borda, borda, borda,
                sw - margem, sh - margem, sw, sh);

        // bordas esticadas
        int largCentro = Math.max(0, largura - borda * 2);
        int altCentro = Math.max(0, altura - borda * 2);
        desenharFatia(g2, caixa, x + borda, y, largCentro, borda,
                margem, 0, sw - margem, margem);
        desenharFatia(g2, caixa, x + borda, y + altura - borda, largCentro, borda,
                margem, sh - margem, sw - margem, sh);
        desenharFatia(g2, caixa, x, y + borda, borda, altCentro,
                0, margem, margem, sh - margem);
        desenharFatia(g2, caixa, x + largura - borda, y + borda, borda, altCentro,
                sw - margem, margem, sw, sh - margem);

        // brilho interno
        g2.setColor(Estilo.BORDA_ESCURA);
        g2.drawRect(x + borda, y + borda, largura - borda * 2 - 1, altura - borda * 2 - 1);
    }

    private static void desenharFatia(Graphics2D g2, BufferedImage img,
                                      int dx, int dy, int dw, int dh,
                                      int sx1, int sy1, int sx2, int sy2) {
        if (dw <= 0 || dh <= 0) {
            return;
        }
        g2.drawImage(img, dx, dy, dx + dw, dy + dh,
                sx1, sy1, sx2, sy2, null);
    }
}
