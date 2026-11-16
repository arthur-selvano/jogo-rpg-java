package view;

import controller.GerenciadorJogo;
import view.component.BotaoPixel;
import view.component.DialogoPixel;
import view.component.HudJogador;
import view.util.AssetManager;
import view.util.Estilo;
import view.util.PixelFont;
import view.util.Sprites;

import javax.swing.AbstractAction;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;

/** Mapa da jornada: salas, caminho, evento de cada parada e navegação. */
public class PainelMapa extends JPanel {

    private final TelaPrincipal tela;
    private final GerenciadorJogo jogo;
    private final HudJogador hud;
    private final BotaoPixel btnAvancar;
    private final BotaoPixel btnStatus;
    private final BotaoPixel btnMenu;
    private final Timer pulso;

    private boolean piscar = true;

    public PainelMapa(TelaPrincipal tela, GerenciadorJogo jogo) {
        this.tela = tela;
        this.jogo = jogo;

        setLayout(null);
        setFocusable(true);
        setPreferredSize(new Dimension(TelaPrincipal.LARGURA, TelaPrincipal.ALTURA));

        hud = new HudJogador(jogo);
        hud.setBounds(0, 0, TelaPrincipal.LARGURA, 92);
        add(hud);

        btnMenu = new BotaoPixel("MENU", this::voltarMenu, 160, 48);
        btnMenu.setBounds(300, 524, 160, 48);
        add(btnMenu);

        btnStatus = new BotaoPixel("STATUS", this::abrirStatus, 180, 48);
        btnStatus.setBounds(480, 524, 180, 48);
        add(btnStatus);

        btnAvancar = new BotaoPixel("AVANCAR", this::avancar, 220, 48);
        btnAvancar.setBounds(680, 524, 220, 48);
        btnAvancar.setAtalho("ENTER");
        add(btnAvancar);

        getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke("ENTER"), "avancar");
        getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke("I"), "status");
        getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke("ESCAPE"), "menu");
        getActionMap().put("avancar", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                avancar();
            }
        });
        getActionMap().put("status", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                abrirStatus();
            }
        });
        getActionMap().put("menu", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                voltarMenu();
            }
        });

        pulso = new Timer(500, e -> {
            piscar = !piscar;
            if (isShowing()) {
                repaint();
            }
        });
        pulso.start();
    }

    /** Atualiza o painel ao ser exibido pela TelaPrincipal. */
    public void aoExibir() {
        btnAvancar.setEnabled(jogo.getEstado() == GerenciadorJogo.Estado.EXPLORACAO);
        btnStatus.setEnabled(jogo.getEstado() == GerenciadorJogo.Estado.EXPLORACAO);
        hud.repaint();
        requestFocusInWindow();
        repaint();
    }

    private void avancar() {
        if (jogo.getEstado() != GerenciadorJogo.Estado.EXPLORACAO) {
            return;
        }
        GerenciadorJogo.Resultado resultado = jogo.avancar();
        switch (resultado.getEstado()) {
            case BATALHA:
                tela.mostrarBatalha(resultado.getMensagens());
                return;
            case FIM_DA_JORNADA:
                tela.mostrarGameOver(true);
                return;
            default:
                break;
        }
        String mensagem = String.join("\n", resultado.getMensagens());
        if (!mensagem.isEmpty()) {
            DialogoPixel.mostrar(this, "EXPLORACAO", mensagem);
        }
        aoExibir();
    }

    private void abrirStatus() {
        if (jogo.getEstado() != GerenciadorJogo.Estado.EXPLORACAO) {
            tela.mostrarAviso("STATUS", "Você só pode abrir o inventário fora de combate.");
            return;
        }
        tela.mostrarStatus();
    }

    private void voltarMenu() {
        if (DialogoPixel.confirmar(this, "VOLTAR AO MENU",
                "Todo o progresso desta jornada será perdido. Deseja continuar?")) {
            jogo.reiniciar();
            tela.mostrarMenu();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            AssetManager.preparar(g2);
            int w = getWidth();
            int h = getHeight();

            desenharCenario(g2, w, h);

            PixelFont.centralizadoComSombra(g2, "MAPA DA JORNADA", w / 2, 104, 2,
                    Estilo.CREME, Estilo.PRETO);
            String sala = "SALA " + (jogo.getSalaAtual() + 1) + "/" + jogo.getTotalSalas();
            PixelFont.desenharComSombra(g2, sala,
                    w - PixelFont.largura(sala, 2) - 24, 104, 2,
                    Estilo.OURO_CLARO, Estilo.PRETO);

            desenharSalas(g2);
            desenharDiario(g2);
        } finally {
            g2.dispose();
        }
    }

    private void desenharCenario(Graphics2D g2, int w, int h) {
        Estilo.fundoGrama(g2, w, h);
        Estilo.escurecer(g2, w, h, new Color(8, 18, 12, 82));
        Estilo.vinheta(g2, w, h);
    }

    /** Estrada de terra que liga as salas, com pedras e tufos de grama. */
    private void desenharTrilhaSalas(Graphics2D g2) {
        int x = 36;
        int largura = 888;
        int y = 292;
        int altura = 28;

        g2.setColor(new Color(0, 0, 0, 70));
        g2.fillRect(x + 4, y + 5, largura, altura);
        g2.setColor(new Color(78, 54, 30));
        g2.fillRect(x, y, largura, altura);
        g2.setColor(new Color(112, 80, 44));
        g2.fillRect(x + 3, y + 4, largura - 6, altura - 9);
        g2.setColor(new Color(140, 102, 56));
        g2.fillRect(x + 8, y + 8, largura - 16, altura - 18);
        g2.setColor(new Color(58, 38, 20));
        g2.fillRect(x, y, largura, 2);
        g2.fillRect(x, y + altura - 2, largura, 2);

        java.util.Random r = new java.util.Random(19);
        for (int i = 0; i < 34; i++) {
            int px = x + 12 + r.nextInt(largura - 24);
            int py = y + 4 + r.nextInt(altura - 10);
            g2.setColor(new Color(102, 94, 82));
            g2.fillRect(px, py, 3, 2);
            g2.setColor(new Color(148, 138, 120));
            g2.fillRect(px, py, 2, 1);
        }
        for (int gx = x + 16; gx < x + largura - 16; gx += 34) {
            g2.setColor(new Color(44, 116, 52));
            g2.fillRect(gx, y - 4, 2, 5);
            g2.fillRect(gx + 5, y + altura - 1, 2, 5);
            g2.setColor(new Color(96, 186, 96));
            g2.fillRect(gx + 1, y - 4, 1, 3);
        }
    }

    private void desenharSalas(Graphics2D g2) {
        int total = jogo.getTotalSalas();
        int inicio = 30;
        int espaco = 138;
        int lado = 72;
        int y = 258;

        desenharTrilhaSalas(g2);

        int salaAtual = jogo.getSalaAtual();
        for (int i = 0; i < total; i++) {
            int x = inicio + i * espaco;
            boolean visitada = jogo.isSalaVisitada(i);
            boolean atual = i == salaAtual;
            GerenciadorJogo.TipoSala tipo = jogo.getTipoSala(i);

            Color fundo = atual ? new Color(52, 46, 26)
                    : visitada ? new Color(22, 42, 30)
                    : new Color(18, 26, 40);
            Estilo.painelZelda(g2, x, y, lado, lado, fundo);

            if (atual) {
                Estilo.sombraProjetada(g2, x + lado / 2, y + 58, 44);
                AssetManager.desenhar(g2, Sprites.PLAYER, x + 8, y + 8, 56, 56);
                if (piscar) {
                    g2.setColor(Estilo.OURO_CLARO);
                    g2.fillRect(x + lado / 2 - 6, y - 20, 12, 5);
                    g2.fillRect(x + lado / 2 - 3, y - 15, 6, 5);
                    g2.setColor(Color.WHITE);
                    g2.fillRect(x + lado / 2 - 1, y - 18, 2, 2);
                }
            } else {
                Estilo.sombraProjetada(g2, x + lado / 2, y + 54, 32);
                desenharIconeSala(g2, i, tipo, visitada, x, y, lado);
            }

            Color corRotulo = atual ? Estilo.OURO_CLARO
                    : visitada ? Estilo.VERDE : Estilo.TEXTO_APAGADO;
            PixelFont.desenharCentralizado(g2, jogo.getNomeSala(i),
                    x + lado / 2, y + lado + 10, 1, corRotulo, Estilo.PRETO);
        }
    }

    private void desenharIconeSala(Graphics2D g2, int indice,
                                   GerenciadorJogo.TipoSala tipo,
                                   boolean visitada, int x, int y, int lado) {
        int cx = x + lado / 2;
        int cy = y + lado / 2;
        switch (tipo) {
            case INIMIGO:
                AssetManager.desenhar(g2, Sprites.doInimigo(jogo.getTipoInimigoSala(indice)),
                        cx - 16, cy - 16, 32, 32);
                if (!visitada) {
                    escurecer(g2, cx - 16, cy - 16, 32, 32);
                }
                break;
            case CHEFE:
                AssetManager.desenhar(g2, Sprites.DRAGAO, cx - 24, cy - 24, 48, 48);
                if (!visitada) {
                    escurecer(g2, cx - 24, cy - 24, 48, 48);
                }
                PixelFont.desenharCentralizado(g2, "!", cx, y + 2, 1, Estilo.VERMELHO);
                break;
            case TESOURO:
                desenharBau(g2, cx - 16, cy - 14);
                if (!visitada) {
                    escurecer(g2, cx - 16, cy - 14, 32, 28);
                }
                break;
            case INICIO:
            default:
                desenharBandeira(g2, cx, cy);
                break;
        }
        if (visitada) {
            g2.setColor(Estilo.VERDE);
            g2.fillRect(x + lado - 22, y + 6, 4, 12);
            g2.fillRect(x + lado - 18, y + 10, 4, 8);
            g2.fillRect(x + lado - 14, y + 14, 4, 4);
        }
    }

    private void desenharBau(Graphics2D g2, int x, int y) {
        g2.setColor(Estilo.PRETO);
        g2.fillRect(x - 2, y + 6, 34, 22);
        g2.setColor(new Color(0x6C3A1E));
        g2.fillRect(x, y + 8, 30, 18);
        g2.setColor(new Color(0xA86A2E));
        g2.fillRect(x, y + 2, 30, 8);
        g2.setColor(Estilo.OURO);
        g2.fillRect(x + 13, y + 10, 4, 9);
        g2.fillRect(x, y + 16, 30, 3);
    }

    private void desenharBandeira(Graphics2D g2, int cx, int cy) {
        g2.setColor(new Color(0x8C8CA8));
        g2.fillRect(cx - 2, cy - 20, 4, 40);
        g2.setColor(Estilo.VERDE);
        g2.fillRect(cx + 2, cy - 20, 18, 12);
        g2.fillRect(cx + 2, cy - 8, 12, 6);
        g2.setColor(Estilo.OURO);
        g2.fillRect(cx - 4, cy + 20, 8, 4);
    }

    private void escurecer(Graphics2D g2, int x, int y, int w, int h) {
        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRect(x, y, w, h);
    }

    private void desenharDiario(Graphics2D g2) {
        Estilo.caixaDialogo(g2, 60, 400, 840, 120);
        PixelFont.desenharComSombra(g2, "DIARIO DA JORNADA", 88, 428, 1,
                Estilo.OURO_CLARO, Estilo.PRETO);

        int proxima = jogo.getSalaAtual() + 1;
        String objetivo = proxima < jogo.getTotalSalas()
                ? "PROXIMA PARADA: " + jogo.getNomeSala(proxima)
                : "O COVIL DO DRAGAO E O ULTIMO DESAFIO.";
        PixelFont.desenharComSombra(g2, objetivo, 88, 448, 1,
                Estilo.TEXTO, Estilo.PRETO);
        PixelFont.desenharComSombra(g2,
                "ENTER AVANCA   -   I ABRE O STATUS   -   ESC VOLTA AO MENU",
                88, 468, 1, Estilo.TEXTO_APAGADO, Estilo.PRETO);
        PixelFont.desenharComSombra(g2,
                "VIDA " + jogo.getVida() + "/" + jogo.getVidaTotal()
                        + "   XP " + jogo.getXp() + "   NIVEL " + jogo.getNivel(),
                88, 488, 1, Estilo.OURO_CLARO, Estilo.PRETO);
    }
}
