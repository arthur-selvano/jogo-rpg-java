package view;

import controller.GerenciadorJogo;
import view.component.BotaoPixel;
import view.component.DialogoPixel;
import view.util.AssetManager;
import view.util.Estilo;
import view.util.PixelFont;
import view.util.Sprites;

import javax.swing.AbstractAction;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.util.Random;

/** Tela final: vitória sobre o dragão ou derrota do herói. */
public class PainelGameOver extends JPanel {

    private final TelaPrincipal tela;
    private final GerenciadorJogo jogo;

    private boolean vitoria;

    public PainelGameOver(TelaPrincipal tela, GerenciadorJogo jogo) {
        this.tela = tela;
        this.jogo = jogo;

        setLayout(null);
        setFocusable(true);
        setPreferredSize(new Dimension(TelaPrincipal.LARGURA, TelaPrincipal.ALTURA));

        BotaoPixel btnReiniciar = new BotaoPixel("JOGAR NOVAMENTE", this::reiniciar, 260, 52);
        btnReiniciar.setBounds(146, 460, 260, 52);
        add(btnReiniciar);

        BotaoPixel btnMenu = new BotaoPixel("MENU", this::menu, 180, 52);
        btnMenu.setBounds(430, 460, 180, 52);
        add(btnMenu);

        BotaoPixel btnSair = new BotaoPixel("SAIR", this::sair, 180, 52);
        btnSair.setBounds(634, 460, 180, 52);
        add(btnSair);

        getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke("ENTER"), "reiniciar");
        getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke("ESCAPE"), "menu");
        getActionMap().put("reiniciar", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                reiniciar();
            }
        });
        getActionMap().put("menu", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                menu();
            }
        });
    }

    /** Chamado pela TelaPrincipal com o resultado final. */
    public void aoExibir(boolean vitoria) {
        this.vitoria = vitoria;
        requestFocusInWindow();
        repaint();
    }

    private void reiniciar() {
        jogo.reiniciar();
        tela.mostrarCriacao();
    }

    private void menu() {
        jogo.reiniciar();
        tela.mostrarMenu();
    }

    private void sair() {
        if (DialogoPixel.confirmar(this, "SAIR DO JOGO",
                "Deseja mesmo fechar o jogo?")) {
            tela.dispose();
            System.exit(0);
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

            if (vitoria) {
                desenharVitoria(g2, w, h);
            } else {
                desenharDerrota(g2, w, h);
            }

            desenharResumo(g2, w);

            Estilo.painel(g2, 0, h - 34, w, 34, new Color(8, 7, 16),
                    Estilo.OURO_ESCURO, Estilo.PRETO);
            PixelFont.desenharCentralizado(g2,
                    "ENTER JOGA NOVAMENTE   -   ESC MENU PRINCIPAL",
                    w / 2, h - 22, 1, Estilo.CREME, Estilo.PRETO);
        } finally {
            g2.dispose();
        }
    }

    private void desenharVitoria(Graphics2D g2, int w, int h) {
        Estilo.fundoGrama(g2, w, h);
        Estilo.escurecer(g2, w, h, new Color(72, 54, 8, 96));
        Estilo.vinheta(g2, w, h);

        java.util.Random r = new java.util.Random(5);
        for (int i = 0; i < 90; i++) {
            int x = r.nextInt(w);
            int y = r.nextInt(h);
            int t = r.nextInt(3) + 2;
            g2.setColor(r.nextInt(3) == 0 ? Estilo.OURO_CLARO : Estilo.CREME);
            g2.fillRect(x, y, t, t);
        }
        g2.setColor(new Color(240, 198, 75, 30));
        for (int i = 0; i < 12; i++) {
            double rad = Math.toRadians(i * 30);
            int x = (int) (w / 2 + Math.cos(rad) * 420);
            int y = (int) (h / 2 + Math.sin(rad) * 300);
            g2.fillRect(x, y, 6, 6);
        }

        PixelFont.centralizadoComSombra(g2, "VITORIA!", w / 2, 82, 8,
                Estilo.OURO_CLARO, Estilo.PRETO);
        PixelFont.desenharCentralizado(g2, "O REINO ESTA SALVO!", w / 2, 168, 2,
                Estilo.CREME, Estilo.PRETO);

        Estilo.sombraProjetada(g2, 114, 328, 96);
        AssetManager.desenhar(g2, Sprites.daClasse(jogo.getClasseJogador()),
                50, 200, 128, 128);
        Estilo.sombraProjetada(g2, 830, 316, 72);
        AssetManager.desenhar(g2, Sprites.DRAGAO, 782, 220, 96, 96);
        g2.setColor(new Color(0, 0, 0, 130));
        g2.fillRect(782, 220, 96, 96);
    }

    private void desenharDerrota(Graphics2D g2, int w, int h) {
        Estilo.fundoGrama(g2, w, h);
        Estilo.escurecer(g2, w, h, new Color(48, 6, 14, 168));
        Estilo.vinheta(g2, w, h);

        Random r = new Random(7);
        for (int i = 0; i < 70; i++) {
            int x = r.nextInt(w);
            int y = r.nextInt(h);
            int t = r.nextInt(4) + 2;
            g2.setColor(r.nextInt(2) == 0 ? new Color(0xE8601C) : new Color(0x8C2430));
            g2.fillRect(x, y, t, t);
        }

        PixelFont.centralizadoComSombra(g2, "GAME OVER", w / 2, 82, 8,
                Estilo.VERMELHO, Estilo.PRETO);
        PixelFont.desenharCentralizado(g2, "SUA JORNADA TERMINOU...", w / 2, 168, 2,
                new Color(0xF0C0C8), Estilo.PRETO);

        Estilo.sombraProjetada(g2, 830, 316, 72);
        AssetManager.desenhar(g2, Sprites.DRAGAO, 782, 220, 96, 96);
        Estilo.sombraProjetada(g2, 98, 316, 72);
        AssetManager.desenhar(g2, Sprites.daClasse(jogo.getClasseJogador()),
                50, 220, 96, 96);
        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRect(50, 220, 96, 96);
    }

    private void desenharResumo(Graphics2D g2, int w) {
        Estilo.caixaDialogo(g2, 230, 210, 500, 220);
        PixelFont.desenharComSombra(g2, "RESUMO DA JORNADA", 258, 242, 1,
                Estilo.OURO_CLARO, Estilo.PRETO);

        int y = 268;
        String classe = jogo.getNomeClasse().isEmpty() ? "-" : jogo.getNomeClasse();
        String nome = jogo.getNomeJogador().isEmpty() ? "SEM NOME" : jogo.getNomeJogador();

        PixelFont.desenharComSombra(g2, "HEROI: " + nome, 258, y, 1,
                Estilo.CREME, Estilo.PRETO);
        y += 22;
        PixelFont.desenharComSombra(g2, "CLASSE: " + classe, 258, y, 1,
                Estilo.TEXTO, Estilo.PRETO);
        y += 22;
        PixelFont.desenharComSombra(g2, "NIVEL " + jogo.getNivel() + "   -   XP "
                + jogo.getXp(), 258, y, 1, Estilo.TEXTO, Estilo.PRETO);
        y += 22;
        PixelFont.desenharComSombra(g2, "INIMIGOS DERROTADOS: "
                        + jogo.getInimigosDerrotados(), 258, y, 1,
                Estilo.TEXTO, Estilo.PRETO);
        y += 22;
        PixelFont.desenharComSombra(g2, "SALAS EXPLORADAS: "
                        + (jogo.getSalaAtual() + 1) + "/" + jogo.getTotalSalas(),
                258, y, 1, Estilo.TEXTO, Estilo.PRETO);
        y += 28;

        String finalTexto = vitoria
                ? "O DRAGAO IGNIS FOI DERROTADO!" : "VOCE CAIU EM BATALHA...";
        PixelFont.desenharComSombra(g2, finalTexto, 258, y, 1,
                vitoria ? Estilo.OURO_CLARO : Estilo.VERMELHO, Estilo.PRETO);
    }
}
