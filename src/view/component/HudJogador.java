package view.component;

import controller.GerenciadorJogo;
import view.util.AssetManager;
import view.util.Estilo;
import view.util.PixelFont;
import view.util.Sprites;

import javax.swing.JComponent;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.Random;

/** HUD animado no estilo Zelda: corações reativos, barras com rastro, XP rolante e level up. */
public class HudJogador extends JComponent {

    private final GerenciadorJogo jogo;
    private final Timer loop;
    private final Random random = new Random();

    private boolean inicializado = false;
    private double vidaVisual = 0;
    private double vidaRastro = 0;
    private double recursoVisual = 0;
    private double xpVisual = 0;

    private int ultimaVida = -1;
    private int ultimoNivel = -1;

    private double fase = 0.0;
    private int framesDano = 0;
    private int framesCura = 0;
    private int framesLevelUp = 0;
    private int framesBrilhoRecurso = 0;

    public HudJogador(GerenciadorJogo jogo) {
        this.jogo = jogo;
        setOpaque(false);
        this.loop = new Timer(25, e -> atualizarAnimacao());
    }

    @Override
    public void addNotify() {
        super.addNotify();
        sincronizarEstado(true);
        if (!loop.isRunning()) {
            loop.start();
        }
    }

    @Override
    public void removeNotify() {
        loop.stop();
        super.removeNotify();
    }

    private void sincronizarEstado(boolean resetarSeNovo) {
        int vidaAtual = jogo.getVida();
        int recursoAtual = jogo.isMago() ? jogo.getMana() : jogo.getFuria();
        int xpAtual = jogo.getXp();
        int nivelAtual = jogo.getNivel();

        if (!inicializado || (resetarSeNovo && ultimaVida < 0)) {
            vidaVisual = vidaAtual;
            vidaRastro = vidaAtual;
            recursoVisual = recursoAtual;
            xpVisual = xpAtual;
            ultimaVida = vidaAtual;
            ultimoNivel = nivelAtual;
            inicializado = true;
            return;
        }

        if (vidaAtual < ultimaVida) {
            framesDano = 14;
        } else if (vidaAtual > ultimaVida) {
            framesCura = 22;
        }
        ultimaVida = vidaAtual;

        if (nivelAtual > ultimoNivel) {
            framesLevelUp = 36;
        } else if (nivelAtual < ultimoNivel) {
            vidaVisual = vidaAtual;
            vidaRastro = vidaAtual;
            recursoVisual = recursoAtual;
            xpVisual = xpAtual;
        }
        ultimoNivel = nivelAtual;

        if (recursoAtual > recursoVisual + 0.5) {
            framesBrilhoRecurso = 12;
        }
    }

    private void atualizarAnimacao() {
        if (!isShowing()) {
            return;
        }

        sincronizarEstado(false);
        fase += 0.14;

        int vidaAlvo = jogo.getVida();
        int recursoAlvo = jogo.isMago() ? jogo.getMana() : jogo.getFuria();
        int xpAlvo = jogo.getXp();

        vidaVisual += (vidaAlvo - vidaVisual) * 0.24;
        if (Math.abs(vidaAlvo - vidaVisual) < 0.25) {
            vidaVisual = vidaAlvo;
        }

        if (vidaRastro > vidaVisual) {
            vidaRastro += (vidaVisual - vidaRastro) * 0.08;
            if (vidaRastro - vidaVisual < 0.25) {
                vidaRastro = vidaVisual;
            }
        } else {
            vidaRastro = vidaVisual;
        }

        recursoVisual += (recursoAlvo - recursoVisual) * 0.22;
        if (Math.abs(recursoAlvo - recursoVisual) < 0.25) {
            recursoVisual = recursoAlvo;
        }

        if (xpVisual < xpAlvo) {
            double passo = Math.max(0.6, (xpAlvo - xpVisual) * 0.2);
            xpVisual = Math.min(xpAlvo, xpVisual + passo);
        } else if (xpVisual > xpAlvo) {
            xpVisual = xpAlvo;
        }

        if (framesDano > 0) framesDano--;
        if (framesCura > 0) framesCura--;
        if (framesLevelUp > 0) framesLevelUp--;
        if (framesBrilhoRecurso > 0) framesBrilhoRecurso--;

        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        sincronizarEstado(false);
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            Dimension tamanho = Estilo.escalarComponente(g2, this);
            AssetManager.preparar(g2);
            int w = tamanho.width;
            int h = tamanho.height;

            Estilo.painelZelda(g2, 0, 0, w, h, Estilo.FUNDO_HUD);
            g2.setColor(new Color(255, 240, 180, 30));
            g2.fillRect(10, 9, w - 20, 5);
            g2.setColor(Estilo.OURO_ESCURO);
            g2.fillRect(8, h - 4, w - 16, 2);

            if (framesLevelUp > 0) {
                int alpha = Math.min(180, framesLevelUp * 5);
                g2.setColor(new Color(255, 224, 122, alpha));
                g2.drawRect(3, 3, w - 7, h - 7);
                g2.drawRect(5, 5, w - 11, h - 11);
            }

            PixelFont.desenharComSombra(g2, jogo.getNomeJogador(), 18, 12, 2,
                    Estilo.CREME, Estilo.PRETO);
            Color corNivel = (framesLevelUp > 0 && (framesLevelUp / 3) % 2 == 0)
                    ? Estilo.OURO_CLARO : Estilo.OURO;
            String textoNivel = jogo.getNomeClasse() + "  NIVEL " + jogo.getNivel()
                    + (framesLevelUp > 0 ? "  UP!" : "");
            PixelFont.desenharComSombra(g2, textoNivel, 18, 34, 1, corNivel, Estilo.PRETO);

            int shakeX = framesDano > 0 ? random.nextInt(5) - 2 : 0;
            int shakeY = framesDano > 0 ? random.nextInt(3) - 1 : 0;

            double fracaoVida = jogo.getVidaTotal() == 0
                    ? 0 : vidaVisual / (double) jogo.getVidaTotal();
            boolean vidaCritica = fracaoVida > 0 && fracaoVida <= 0.30;

            Color bordaCoracoes = Estilo.OURO_ESCURO;
            if (framesDano > 0) {
                bordaCoracoes = Estilo.VERMELHO;
            } else if (framesCura > 0) {
                bordaCoracoes = Estilo.VERDE;
            } else if (vidaCritica && Math.sin(fase * 2.4) > 0) {
                bordaCoracoes = Estilo.VERMELHO;
            }

            Estilo.painel(g2, 14 + shakeX, 44 + shakeY, 368, 38, new Color(6, 14, 10),
                    bordaCoracoes, new Color(2, 6, 4));
            desenharCoracoesAnimados(g2, 20 + shakeX, 47 + shakeY,
                    (int) Math.round(vidaVisual), jogo.getVidaTotal(), vidaCritica);

            Color corTextoVida = framesDano > 0 ? new Color(255, 110, 110)
                    : (framesCura > 0 ? Estilo.VERDE : Estilo.TEXTO);
            PixelFont.desenharComSombra(g2,
                    "VIDA " + (int) Math.round(vidaVisual) + "/" + jogo.getVidaTotal(),
                    410, 14, 1, corTextoVida, Estilo.PRETO);

            double fracaoRastro = jogo.getVidaTotal() == 0
                    ? 0 : vidaRastro / (double) jogo.getVidaTotal();
            desenharBarraVidaAnimada(g2, fracaoVida, fracaoRastro);

            if (jogo.isMago()) {
                int bobMana = (int) Math.round(Math.sin(fase * 0.9) * 1.5);
                AssetManager.desenhar(g2, Sprites.MANA, 410, 50 + bobMana, 32, 32);

                int manaTotal = Math.max(1, jogo.getManaTotal());
                int manaVis = (int) Math.round(recursoVisual);
                PixelFont.desenharComSombra(g2, "MANA " + manaVis + "/" + jogo.getManaTotal(),
                        450, 52, 1, Estilo.TEXTO, Estilo.PRETO);

                double fracaoMana = recursoVisual / (double) manaTotal;
                desenharBarraRecursoAnimada(g2, 450, 250,
                        fracaoMana, Estilo.AZUL, Estilo.AZUL_ESCURO, new Color(110, 215, 255));
            } else {
                int furiaVis = (int) Math.round(recursoVisual);
                boolean furiaCheia = furiaVis >= 100;
                Color corTxtFuria = furiaCheia ? Estilo.OURO_CLARO : Estilo.TEXTO;
                PixelFont.desenharComSombra(g2, "FURIA " + furiaVis + "/100",
                        410, 52, 1, corTxtFuria, Estilo.PRETO);

                desenharBarraRecursoAnimada(g2, 410, 290,
                        recursoVisual / 100.0, Estilo.LARANJA, new Color(0x5C2C10),
                        Estilo.OURO_CLARO);
            }

            desenharRupeeAnimada(g2, w - 190);
            int xpMostrado = (int) Math.round(xpVisual);
            boolean subindoXp = xpMostrado < jogo.getXp();
            PixelFont.desenharComSombra(g2, "XP " + xpMostrado,
                    w - 164, 14, 2,
                    subindoXp ? Color.WHITE : Estilo.OURO_CLARO, Estilo.PRETO);

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

    private void desenharCoracoesAnimados(Graphics2D g2, int x, int y,
                                          int vida, int vidaTotal, boolean critico) {
        final int total = 10;
        double porCoracao = Math.max(1.0, vidaTotal / (double) total);
        int cheios = (int) Math.ceil(Math.max(0, vida) / porCoracao);
        cheios = Math.max(0, Math.min(total, cheios));

        int lado = 32;
        int espaco = 2;

        double velBatimento = critico ? 2.4 : 1.1;
        double pulso = Math.pow(Math.max(0, Math.sin(fase * velBatimento)), 4);

        for (int i = 0; i < total; i++) {
            boolean cheio = i < cheios;
            String sprite = cheio ? Sprites.CORACAO_CHEIO : Sprites.CORACAO_VAZIO;
            int hx = x + i * (lado + espaco);
            int hy = y;
            int tam = lado;

            if (cheio && i == cheios - 1 && pulso > 0.35) {
                hy -= critico ? 3 : 2;
                tam += 2;
                hx -= 1;
            }

            if (cheio && framesCura > 0) {
                int onda = (22 - framesCura) - i;
                if (onda >= 0 && onda <= 5) {
                    hy -= 3;
                }
            }

            AssetManager.desenhar(g2, sprite, hx, hy, tam, tam);

            if (cheio && framesCura > 0 && ((framesCura + i) % 4 == 0)) {
                g2.setColor(Color.WHITE);
                g2.fillRect(hx + 6, hy + 4, 3, 3);
            }
        }
    }

    private void desenharBarraVidaAnimada(Graphics2D g2, double fracaoAtual, double fracaoRastro) {
        int x = 410;
        int y = 30;
        int w = 290;
        int h = 16;

        double fAtual = Math.max(0.0, Math.min(1.0, fracaoAtual));
        double fRastro = Math.max(fAtual, Math.min(1.0, fracaoRastro));

        Estilo.barra(g2, x, y, w, h, fAtual, Estilo.VERMELHO, Estilo.VERMELHO_ESCURO);

        if (fRastro - fAtual > 0.005) {
            int xAtual = x + (int) Math.round(w * fAtual);
            int largRastro = Math.max(2, (int) Math.round(w * (fRastro - fAtual)));
            g2.setColor(Estilo.OURO_CLARO);
            g2.fillRect(xAtual, y + 1, largRastro, h - 2);
        }

        if (framesCura > 0) {
            int alpha = Math.min(150, framesCura * 7);
            int largPreenchida = (int) Math.round(w * fAtual);
            g2.setColor(new Color(140, 255, 170, alpha));
            g2.fillRect(x, y, largPreenchida, h);
        }
    }

    private void desenharBarraRecursoAnimada(Graphics2D g2, int x, int w,
                                             double fracao, Color cor, Color corFundo,
                                             Color corPulso) {
        int y = 68;
        int h = 14;

        double f = Math.max(0.0, Math.min(1.0, fracao));
        Estilo.barra(g2, x, y, w, h, f, cor, corFundo);

        int preenchido = (int) Math.round(w * f);

        if (framesBrilhoRecurso > 0 && preenchido > 3) {
            int alpha = Math.min(220, framesBrilhoRecurso * 18);
            g2.setColor(new Color(255, 255, 255, alpha));
            g2.fillRect(x + preenchido - 3, y + 1, 3, h - 2);
        }

        if (f >= 0.999) {
            int alpha = (int) (95 + Math.sin(fase * 2.2) * 75);
            g2.setColor(new Color(corPulso.getRed(), corPulso.getGreen(), corPulso.getBlue(),
                    Math.max(20, Math.min(220, alpha))));
            g2.drawRect(x - 2, y - 2, w + 3, h + 3);
        }
    }

    private void desenharRupeeAnimada(Graphics2D g2, int x) {
        int y = 12;
        int giro = (int) Math.round(Math.cos(fase * 1.1) * 2.0);
        int centro = x + 7;
        int wMeio = Math.max(4, 6 + giro);
        int wTopo = Math.max(2, wMeio - 2);

        g2.setColor(Estilo.OURO_ESCURO);
        g2.fillRect(centro - wTopo / 2, y, wTopo, 2);
        g2.fillRect(centro - wMeio / 2, y + 2, wMeio, 2);
        g2.fillRect(centro - (wMeio + 2) / 2, y + 4, wMeio + 2, 8);
        g2.fillRect(centro - wMeio / 2, y + 12, wMeio, 2);
        g2.fillRect(centro - wTopo / 2, y + 14, wTopo, 2);

        g2.setColor(Estilo.OURO);
        g2.fillRect(centro - wTopo / 2, y + 2, wTopo, 2);
        g2.fillRect(centro - wMeio / 2, y + 4, wMeio, 6);
        g2.fillRect(centro - wTopo / 2, y + 10, wTopo, 2);

        int posBrilho = (int) ((fase * 2.0) % 8);
        g2.setColor(Estilo.OURO_CLARO);
        g2.fillRect(centro - wTopo / 2, y + 4 + (posBrilho / 2), 2, 2);
        if (posBrilho < 2) {
            g2.setColor(Color.WHITE);
            g2.fillRect(centro, y + 3, 2, 2);
        }
    }
}
