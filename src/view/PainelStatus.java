package view;

import controller.GerenciadorJogo;
import exception.ItemNaoEncontradoException;
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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/** Ficha do herói e inventário, com uso de itens fora de combate. */
public class PainelStatus extends JPanel {

    private static final int X_LISTA = 500;
    private static final int Y_LISTA = 150;
    private static final int ALTURA_ITEM = 50;
    private static final int ESPACO_ITEM = 8;
    private static final int MAX_VISIVEIS = 6;

    private final TelaPrincipal tela;
    private final GerenciadorJogo jogo;
    private final HudJogador hud;
    private final BotaoPixel btnUsar;

    private int selecionado;

    public PainelStatus(TelaPrincipal tela, GerenciadorJogo jogo) {
        this.tela = tela;
        this.jogo = jogo;

        setLayout(null);
        setFocusable(true);
        setPreferredSize(new Dimension(TelaPrincipal.LARGURA, TelaPrincipal.ALTURA));

        hud = new HudJogador(jogo);
        hud.setBounds(0, 0, TelaPrincipal.LARGURA, 92);
        add(hud);

        btnUsar = new BotaoPixel("USAR", this::usar, 180, 48);
        btnUsar.setBounds(480, 520, 180, 48);
        btnUsar.setAtalho("U");
        add(btnUsar);

        BotaoPixel btnVoltar = new BotaoPixel("VOLTAR", this::voltarMapa, 180, 48);
        btnVoltar.setBounds(760, 520, 180, 48);
        btnVoltar.setAtalho("ESC");
        add(btnVoltar);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int indice = indiceItem(e.getX(), e.getY());
                if (indice >= 0) {
                    selecionado = indice;
                    requestFocusInWindow();
                    repaint();
                }
            }
        });

        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke("UP"), "anterior");
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke("DOWN"), "proximo");
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke("U"), "usar");
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke("ESCAPE"), "voltar");

        getActionMap().put("anterior", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!isShowing()) return;
                moverSelecao(-1);
            }
        });
        getActionMap().put("proximo", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!isShowing()) return;
                moverSelecao(1);
            }
        });
        getActionMap().put("usar", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!isShowing()) return;
                usar();
            }
        });
        getActionMap().put("voltar", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!isShowing()) return;
                voltarMapa();
            }
        });
    }

    /** Atualiza a seleção e o HUD ao entrar na tela. */
    public void aoExibir() {
        ajustarSelecao();
        hud.repaint();
        requestFocusInWindow();
        repaint();
    }

    private int indiceItem(int x, int y) {
        if (x < X_LISTA || x > X_LISTA + 420) {
            return -1;
        }
        int indice = (y - Y_LISTA) / (ALTURA_ITEM + ESPACO_ITEM);
        if (y < Y_LISTA || indice < 0 || indice >= MAX_VISIVEIS) {
            return -1;
        }
        int topo = Y_LISTA + indice * (ALTURA_ITEM + ESPACO_ITEM);
        if (y > topo + ALTURA_ITEM) {
            return -1;
        }
        return indice < jogo.getInventario().size() ? indice : -1;
    }

    private void moverSelecao(int delta) {
        int total = jogo.getInventario().size();
        if (total == 0) {
            selecionado = -1;
            repaint();
            return;
        }
        if (selecionado < 0) {
            selecionado = 0;
        } else {
            selecionado = (selecionado + delta + total) % total;
        }
        repaint();
    }

    private void ajustarSelecao() {
        int total = jogo.getInventario().size();
        if (total == 0) {
            selecionado = -1;
        } else {
            selecionado = Math.max(0, Math.min(selecionado, total - 1));
        }
        btnUsar.setEnabled(total > 0);
    }

    private void usar() {
        List<String> inventario = jogo.getInventario();
        if (inventario.isEmpty()) {
            tela.mostrarAviso("INVENTARIO",
                    "Sua mochila está vazia.\nDerrote inimigos e procure baús pelo mapa.");
            return;
        }
        try {
            GerenciadorJogo.Resultado resultado = jogo.usarItem(selecionado);
            DialogoPixel.mostrar(this, "ITEM",
                    String.join("\n", resultado.getMensagens()));
            ajustarSelecao();
            hud.repaint();
            repaint();
        } catch (ItemNaoEncontradoException e) {
            tela.mostrarErro(e);
        }
    }

    private void voltarMapa() {
        tela.mostrarMapa();
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
            Estilo.escurecer(g2, w, h, new Color(8, 18, 12, 122));
            Estilo.vinheta(g2, w, h);

            desenharFicha(g2);
            desenharInventario(g2);

            PixelFont.desenharCentralizado(g2,
                    "SETAS ESCOLHEM   -   U USA O ITEM   -   ESC VOLTA AO MAPA",
                    w / 2, 580, 1, Estilo.CREME, Estilo.PRETO);
        } finally {
            g2.dispose();
        }
    }

    private void desenharFicha(Graphics2D g2) {
        Estilo.painelZelda(g2, 20, 104, 440, 400, new Color(16, 26, 42));
        PixelFont.desenharComSombra(g2, "FICHA DO HEROI", 44, 120, 2,
                Estilo.OURO_CLARO, Estilo.PRETO);

        String sprite = Sprites.daClasse(jogo.getClasseJogador());
        Estilo.sombraProjetada(g2, 240, 298, 112);
        AssetManager.desenhar(g2, sprite, 160, 142, 160, 160);

        PixelFont.centralizadoComSombra(g2, jogo.getNomeJogador(), 240, 310, 2,
                Estilo.CREME, Estilo.PRETO);
        PixelFont.desenharCentralizado(g2, jogo.getNomeClasse() + "  -  NIVEL "
                + jogo.getNivel(), 240, 334, 1, Estilo.OURO, Estilo.PRETO);

        PixelFont.desenharComSombra(g2, "VIDA " + jogo.getVida() + "/"
                        + jogo.getVidaTotal(), 44, 358, 1,
                Estilo.TEXTO, Estilo.PRETO);
        Estilo.barra(g2, 44, 376, 392, 16,
                jogo.getVidaTotal() == 0 ? 0 : jogo.getVida() / (double) jogo.getVidaTotal(),
                Estilo.VERMELHO, Estilo.VERMELHO_ESCURO);

        PixelFont.desenharComSombra(g2, "ATAQUE " + jogo.getAtaque(),
                44, 404, 1, Estilo.TEXTO, Estilo.PRETO);
        PixelFont.desenharComSombra(g2, "DEFESA " + jogo.getDefesa(),
                250, 404, 1, Estilo.TEXTO, Estilo.PRETO);

        if (jogo.isMago()) {
            PixelFont.desenharComSombra(g2, "MANA " + jogo.getMana() + "/"
                            + jogo.getManaTotal(), 44, 426, 1,
                    Estilo.TEXTO, Estilo.PRETO);
            Estilo.barra(g2, 44, 442, 392, 16,
                    jogo.getManaTotal() == 0 ? 0
                            : jogo.getMana() / (double) jogo.getManaTotal(),
                    Estilo.AZUL, Estilo.AZUL_ESCURO);
        } else {
            PixelFont.desenharComSombra(g2, "FURIA " + jogo.getFuria() + "/100",
                    44, 426, 1, Estilo.TEXTO, Estilo.PRETO);
            Estilo.barra(g2, 44, 442, 392, 16,
                    jogo.getFuria() / 100.0, Estilo.LARANJA, new Color(0x5C2C10));
        }

        int proximo = jogo.getXpProximoNivel();
        double fracaoXp = 1;
        String textoXp;
        if (proximo < 0) {
            textoXp = "XP " + jogo.getXp() + "  (NIVEL MAXIMO)";
        } else {
            int inicio = jogo.getXpNivelInicio();
            fracaoXp = (jogo.getXp() - inicio) / (double) Math.max(1, proximo - inicio);
            textoXp = "XP " + jogo.getXp() + "/" + proximo;
        }
        PixelFont.desenharComSombra(g2, textoXp, 44, 466, 1,
                Estilo.TEXTO, Estilo.PRETO);
        Estilo.barra(g2, 44, 482, 392, 12, fracaoXp,
                Estilo.OURO, Estilo.OURO_ESCURO);
    }

    private void desenharInventario(Graphics2D g2) {
        Estilo.painelZelda(g2, 480, 104, 460, 400, new Color(16, 26, 42));
        PixelFont.desenharComSombra(g2, "INVENTARIO", 504, 120, 2,
                Estilo.OURO_CLARO, Estilo.PRETO);

        List<String> itens = jogo.getInventario();
        if (itens.isEmpty()) {
            PixelFont.desenharCentralizado(g2, "MOCHILA VAZIA", 710, 260, 2,
                    Estilo.TEXTO_APAGADO, Estilo.PRETO);
            PixelFont.desenharCentralizado(g2, "ENCONTRE BAUS PELO MAPA", 710, 292, 1,
                    Estilo.TEXTO_APAGADO, Estilo.PRETO);
            return;
        }

        for (int i = 0; i < itens.size() && i < MAX_VISIVEIS; i++) {
            String item = itens.get(i);
            int y = Y_LISTA + i * (ALTURA_ITEM + ESPACO_ITEM);
            boolean ativo = i == selecionado;

            Color fundo = ativo ? new Color(52, 44, 28) : new Color(20, 30, 44);
            Color bordaClara = ativo ? Estilo.OURO : Estilo.BORDA_CLARA;
            Color bordaEscura = ativo ? Estilo.OURO_ESCURO : Estilo.BORDA_ESCURA;
            Estilo.painel(g2, X_LISTA, y, 420, ALTURA_ITEM, fundo,
                    bordaClara, bordaEscura);
            if (ativo) {
                g2.setColor(Estilo.OURO_CLARO);
                g2.drawRect(X_LISTA + 3, y + 3, 413, ALTURA_ITEM - 7);
                g2.fillRect(X_LISTA + 400, y + 21, 9, 9);
                g2.setColor(new Color(20, 14, 10));
                g2.fillRect(X_LISTA + 406, y + 24, 2, 3);
            }

            desenharIconeItem(g2, item, X_LISTA + 10, y + 9);

            PixelFont.desenharComSombra(g2, item, X_LISTA + 54, y + 8, 2,
                    ativo ? Estilo.OURO_CLARO : Estilo.CREME, Estilo.PRETO);

            Estilo.textoComSombra(g2, descricaoItem(item), X_LISTA + 54, y + 34,
                    ativo ? Estilo.TEXTO : Estilo.TEXTO_APAGADO, Estilo.FONTE_PEQUENA);
        }

        if (itens.size() > MAX_VISIVEIS) {
            PixelFont.desenharComSombra(g2, "..." + (itens.size() - MAX_VISIVEIS)
                            + " ITENS", 504, 500, 1,
                    Estilo.TEXTO_APAGADO, Estilo.PRETO);
        }
    }

    private String descricaoItem(String item) {
        if (GerenciadorJogo.ITEM_POCAO.equals(item)) {
            return "Recupera 30 pontos de vida.";
        }
        if (GerenciadorJogo.ITEM_MANA.equals(item)) {
            return "Recupera mana. Somente para magos.";
        }
        return "Um item misterioso.";
    }

    private void desenharIconeItem(Graphics2D g2, String item, int x, int y) {
        boolean mana = GerenciadorJogo.ITEM_MANA.equals(item);
        Color liquido = mana ? Estilo.AZUL : Estilo.VERMELHO;

        g2.setColor(Estilo.PRETO);
        g2.fillRect(x + 9, y + 2, 14, 14);
        g2.setColor(new Color(0x8C6A3C));
        g2.fillRect(x + 11, y + 1, 10, 5);
        g2.setColor(new Color(0xC8D8F0));
        g2.fillRect(x + 12, y + 6, 8, 7);
        g2.fillRect(x + 8, y + 13, 16, 15);
        g2.setColor(liquido);
        g2.fillRect(x + 9, y + 17, 14, 10);
        g2.setColor(liquido.brighter());
        g2.fillRect(x + 10, y + 18, 4, 4);
        g2.setColor(new Color(0xE8E8F0));
        g2.fillRect(x + 13, y + 7, 2, 4);
    }
}
