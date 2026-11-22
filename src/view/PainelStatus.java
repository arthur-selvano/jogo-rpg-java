package view;

import controller.GerenciadorJogo;
import exception.ItemNaoEncontradoException;
import model.Arma;
import model.Item;
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

/** Ficha do herói e inventário, com equipamento de armas e uso de poções fora de combate. */
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
    private final BotaoPixel btnEquipar;
    private final BotaoPixel btnDesequipar;

    private int selecionado;
    private int inicioVisivel;

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

        btnEquipar = new BotaoPixel("EQUIPAR", this::usar, 180, 48);
        btnEquipar.setBounds(480, 520, 180, 48);
        btnEquipar.setAtalho("U");
        btnEquipar.setVisible(false);
        add(btnEquipar);

        btnDesequipar = new BotaoPixel("DESEQUIPAR", this::usar, 180, 48);
        btnDesequipar.setBounds(480, 520, 180, 48);
        btnDesequipar.setAtalho("U");
        btnDesequipar.setVisible(false);
        add(btnDesequipar);

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
                    atualizarBotaoAcao();
                    requestFocusInWindow();
                    repaint();
                    if (e.getClickCount() == 2) {
                        usar();
                    }
                }
            }
        });

        addMouseWheelListener(e -> {
            if (isShowing()) {
                moverSelecao(e.getWheelRotation() > 0 ? 1 : -1);
            }
        });

        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("UP"), "anterior");
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("DOWN"), "proximo");
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("U"), "usar");
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ENTER"), "usar");
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ESCAPE"), "voltar");

        getActionMap().put("anterior", acao(() -> moverSelecao(-1)));
        getActionMap().put("proximo", acao(() -> moverSelecao(1)));
        getActionMap().put("usar", acao(this::usar));
        getActionMap().put("voltar", acao(this::voltarMapa));
    }

    private AbstractAction acao(Runnable r) {
        return new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isShowing()) {
                    r.run();
                }
            }
        };
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
        int linha = (y - Y_LISTA) / (ALTURA_ITEM + ESPACO_ITEM);
        if (y < Y_LISTA || linha < 0 || linha >= MAX_VISIVEIS) {
            return -1;
        }
        int topo = Y_LISTA + linha * (ALTURA_ITEM + ESPACO_ITEM);
        if (y > topo + ALTURA_ITEM) {
            return -1;
        }
        int indiceReal = inicioVisivel + linha;
        return indiceReal < jogo.getItensInventario().size() ? indiceReal : -1;
    }

    private void moverSelecao(int delta) {
        int total = jogo.getItensInventario().size();
        if (total == 0) {
            selecionado = -1;
            inicioVisivel = 0;
            atualizarBotaoAcao();
            repaint();
            return;
        }
        if (selecionado < 0) {
            selecionado = 0;
        } else {
            selecionado = (selecionado + delta + total) % total;
        }
        garantirVisibilidade();
        atualizarBotaoAcao();
        repaint();
    }

    private void garantirVisibilidade() {
        if (selecionado < inicioVisivel) {
            inicioVisivel = selecionado;
        } else if (selecionado >= inicioVisivel + MAX_VISIVEIS) {
            inicioVisivel = selecionado - MAX_VISIVEIS + 1;
        }
    }

    private void ajustarSelecao() {
        List<Item> itens = jogo.getItensInventario();
        int total = itens.size();
        if (total == 0) {
            selecionado = -1;
            inicioVisivel = 0;
        } else {
            selecionado = Math.max(0, Math.min(selecionado, total - 1));
            inicioVisivel = Math.max(0, Math.min(inicioVisivel, Math.max(0, total - MAX_VISIVEIS)));
            garantirVisibilidade();
        }
        atualizarBotaoAcao();
    }

    private void atualizarBotaoAcao() {
        List<Item> itens = jogo.getItensInventario();
        btnUsar.setVisible(false);
        btnEquipar.setVisible(false);
        btnDesequipar.setVisible(false);

        if (itens.isEmpty() || selecionado < 0 || selecionado >= itens.size()) {
            btnUsar.setVisible(true);
            btnUsar.setEnabled(false);
            return;
        }

        Item atual = itens.get(selecionado);
        if (atual instanceof Arma) {
            if (atual == jogo.getArmaEquipada()) {
                btnDesequipar.setVisible(true);
                btnDesequipar.setEnabled(true);
            } else {
                btnEquipar.setVisible(true);
                btnEquipar.setEnabled(true);
            }
        } else {
            btnUsar.setVisible(true);
            btnUsar.setEnabled(true);
        }
    }

    private void usar() {
        List<Item> itens = jogo.getItensInventario();
        if (itens.isEmpty() || selecionado < 0) {
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
                    "SETAS ESCOLHEM   -   U / ENTER USA OU EQUIPA   -   ESC VOLTA AO MAPA",
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
        Estilo.sombraProjetada(g2, 175, 298, 108);
        AssetManager.desenhar(g2, sprite, 95, 142, 160, 160);

        desenharSlotArmaEquipada(g2);

        PixelFont.centralizadoComSombra(g2, jogo.getNomeJogador(), 175, 310, 2,
                Estilo.CREME, Estilo.PRETO);
        PixelFont.desenharCentralizado(g2, jogo.getNomeClasse() + " - NIVEL " + jogo.getNivel(),
                175, 334, 1, Estilo.OURO, Estilo.PRETO);

        PixelFont.desenharComSombra(g2, "VIDA " + jogo.getVida() + "/" + jogo.getVidaTotal(),
                44, 358, 1, Estilo.TEXTO, Estilo.PRETO);
        Estilo.barra(g2, 44, 376, 392, 16,
                jogo.getVidaTotal() == 0 ? 0 : jogo.getVida() / (double) jogo.getVidaTotal(),
                Estilo.VERMELHO, Estilo.VERMELHO_ESCURO);

        desenharLinhaAtaqueEDefesa(g2);

        if (jogo.isMago()) {
            PixelFont.desenharComSombra(g2, "MANA " + jogo.getMana() + "/" + jogo.getManaTotal(),
                    44, 426, 1, Estilo.TEXTO, Estilo.PRETO);
            Estilo.barra(g2, 44, 442, 392, 16,
                    jogo.getManaTotal() == 0 ? 0 : jogo.getMana() / (double) jogo.getManaTotal(),
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
        PixelFont.desenharComSombra(g2, textoXp, 44, 466, 1, Estilo.TEXTO, Estilo.PRETO);
        Estilo.barra(g2, 44, 482, 392, 12, fracaoXp, Estilo.OURO, Estilo.OURO_ESCURO);
    }

    private void desenharSlotArmaEquipada(Graphics2D g2) {
        int x = 304;
        int y = 156;
        int w = 132;
        int h = 136;

        Arma equipada = jogo.getArmaEquipada();
        Color borda = equipada != null ? Estilo.VERDE : Estilo.BORDA_CLARA;
        Estilo.painel(g2, x, y, w, h, new Color(12, 20, 32), borda, Estilo.BORDA_ESCURA);

        PixelFont.desenharCentralizado(g2, "EQUIPADA", x + w / 2, y + 18, 1,
                Estilo.OURO_CLARO, Estilo.PRETO);

        int boxX = x + (w - 48) / 2;
        int boxY = y + 28;
        g2.setColor(new Color(22, 34, 52));
        g2.fillRect(boxX, boxY, 48, 48);
        g2.setColor(equipada != null ? Estilo.OURO : Estilo.BORDA_ESCURA);
        g2.drawRect(boxX, boxY, 48, 48);

        if (equipada != null) {
            desenharIconeItem(g2, equipada, boxX + 8, boxY + 8);
            String nomeCurto = equipada.getNome().toUpperCase();
            if (nomeCurto.length() > 16) {
                nomeCurto = nomeCurto.substring(0, 15) + ".";
            }
            PixelFont.desenharCentralizado(g2, nomeCurto, x + w / 2, y + 96, 1,
                    Estilo.CREME, Estilo.PRETO);
            PixelFont.desenharCentralizado(g2, "+" + equipada.getBonusAtaque() + " ATK",
                    x + w / 2, y + 116, 1, Estilo.VERDE, Estilo.PRETO);
        } else {
            g2.setColor(new Color(60, 72, 92));
            g2.fillRect(boxX + 22, boxY + 10, 4, 22);
            g2.fillRect(boxX + 16, boxY + 28, 16, 3);
            g2.fillRect(boxX + 22, boxY + 31, 4, 7);

            PixelFont.desenharCentralizado(g2, "NENHUMA", x + w / 2, y + 102, 1,
                    Estilo.TEXTO_APAGADO, Estilo.PRETO);
        }
    }

    private void desenharLinhaAtaqueEDefesa(Graphics2D g2) {
        int x = 44;
        int y = 404;

        int atkAtual = jogo.getAtaque();
        List<Item> itens = jogo.getItensInventario();
        Item selItem = (selecionado >= 0 && selecionado < itens.size()) ? itens.get(selecionado) : null;

        if (selItem instanceof Arma candidata) {
            Arma equipada = jogo.getArmaEquipada();
            int bonusAtual = equipada == null ? 0 : equipada.getBonusAtaque();
            int atkPrevisto = atkAtual - bonusAtual + candidata.getBonusAtaque();
            int diff = atkPrevisto - atkAtual;

            if (diff != 0) {
                Color corDiff = diff > 0 ? Estilo.VERDE : Estilo.VERMELHO;
                String sinal = diff > 0 ? "+" + diff : String.valueOf(diff);
                PixelFont.desenharComSombra(g2,
                        "ATAQUE " + atkAtual + " -> " + atkPrevisto + " (" + sinal + ")",
                        x, y, 1, corDiff, Estilo.PRETO);
            } else {
                PixelFont.desenharComSombra(g2, "ATAQUE " + atkAtual + " (EQUIPADA)",
                        x, y, 1, Estilo.VERDE, Estilo.PRETO);
            }
        } else {
            PixelFont.desenharComSombra(g2, "ATAQUE " + atkAtual,
                    x, y, 1, Estilo.TEXTO, Estilo.PRETO);
        }

        PixelFont.desenharComSombra(g2, "DEFESA " + jogo.getDefesa(),
                x + 246, y, 1, Estilo.TEXTO, Estilo.PRETO);
    }

    private void desenharInventario(Graphics2D g2) {
        Estilo.painelZelda(g2, 480, 104, 460, 400, new Color(16, 26, 42));
        PixelFont.desenharComSombra(g2, "INVENTARIO", 504, 120, 2,
                Estilo.OURO_CLARO, Estilo.PRETO);

        List<Item> itens = jogo.getItensInventario();
        Arma equipada = jogo.getArmaEquipada();

        if (itens.isEmpty()) {
            PixelFont.desenharCentralizado(g2, "MOCHILA VAZIA", 710, 260, 2,
                    Estilo.TEXTO_APAGADO, Estilo.PRETO);
            PixelFont.desenharCentralizado(g2, "ENCONTRE BAUS PELO MAPA", 710, 292, 1,
                    Estilo.TEXTO_APAGADO, Estilo.PRETO);
            return;
        }

        g2.setFont(Estilo.FONTE_PEQUENA);
        FontMetrics fm = g2.getFontMetrics();

        int fim = Math.min(itens.size(), inicioVisivel + MAX_VISIVEIS);
        for (int i = inicioVisivel; i < fim; i++) {
            Item item = itens.get(i);
            int linha = i - inicioVisivel;
            int y = Y_LISTA + linha * (ALTURA_ITEM + ESPACO_ITEM);
            boolean ativo = (i == selecionado);
            boolean estaEquipada = (item == equipada);

            Color fundo = ativo ? new Color(52, 44, 28)
                    : (estaEquipada ? new Color(20, 42, 34) : new Color(20, 30, 44));
            Color bordaClara = ativo ? Estilo.OURO
                    : (estaEquipada ? Estilo.VERDE : Estilo.BORDA_CLARA);
            Color bordaEscura = ativo ? Estilo.OURO_ESCURO : Estilo.BORDA_ESCURA;

            Estilo.painel(g2, X_LISTA, y, 420, ALTURA_ITEM, fundo, bordaClara, bordaEscura);
            if (ativo) {
                g2.setColor(Estilo.OURO_CLARO);
                g2.drawRect(X_LISTA + 3, y + 3, 413, ALTURA_ITEM - 7);
                g2.fillRect(X_LISTA + 400, y + 21, 9, 9);
                g2.setColor(new Color(20, 14, 10));
                g2.fillRect(X_LISTA + 406, y + 24, 2, 3);
            }

            desenharIconeItem(g2, item, X_LISTA + 10, y + 9);

            PixelFont.desenharComSombra(g2, item.getNome(), X_LISTA + 54, y + 8, 2,
                    ativo ? Estilo.OURO_CLARO : Estilo.CREME, Estilo.PRETO);

            if (estaEquipada) {
                PixelFont.desenharComSombra(g2, "[EQUIPADA]", X_LISTA + 312, y + 10, 1,
                        Estilo.VERDE, Estilo.PRETO);
            } else if (item instanceof Arma arma) {
                int bonusAtual = equipada == null ? 0 : equipada.getBonusAtaque();
                int diff = arma.getBonusAtaque() - bonusAtual;
                String tagDiff = diff > 0 ? "(+" + diff + " ATK)"
                        : (diff < 0 ? "(" + diff + " ATK)" : "(IGUAL)");
                Color corDiff = diff > 0 ? Estilo.VERDE
                        : (diff < 0 ? Estilo.VERMELHO : Estilo.TEXTO_APAGADO);
                PixelFont.desenharComSombra(g2, tagDiff, X_LISTA + 322, y + 10, 1,
                        corDiff, Estilo.PRETO);
            }

            String descAjustada = truncarTexto(descricaoItem(item), fm);
            Estilo.textoComSombra(g2, descAjustada, X_LISTA + 54, y + 34,
                    ativo ? Estilo.TEXTO : Estilo.TEXTO_APAGADO, Estilo.FONTE_PEQUENA);
        }

        if (itens.size() > MAX_VISIVEIS) {
            PixelFont.desenharComSombra(g2,
                    "ITEM " + (selecionado + 1) + "/" + itens.size() + " (ROLE PARA VER MAIS)",
                    504, 496, 1, Estilo.TEXTO_APAGADO, Estilo.PRETO);
        }
    }

    private String truncarTexto(String texto, FontMetrics fm) {
        int larguraMax = 336;
        if (texto == null) return "";
        if (fm.stringWidth(texto) <= larguraMax) {
            return texto;
        }
        String reticencias = "...";
        int limite = larguraMax - fm.stringWidth(reticencias);
        int fim = texto.length();
        while (fim > 0 && fm.stringWidth(texto.substring(0, fim)) > limite) {
            fim--;
        }
        return texto.substring(0, fim).trim() + reticencias;
    }

    private String descricaoItem(Item item) {
        if (item instanceof Arma arma) {
            return "+" + arma.getBonusAtaque() + " ATK - " + arma.getDescricao();
        }
        return item.getDescricao();
    }

    /** Desenha o sprite pixel art específico de Espada, Cajado ou Poção (32x32). */
    private void desenharIconeItem(Graphics2D g2, Item item, int x, int y) {
        if (item instanceof Arma) {
            String nome = item.getNome().toLowerCase();
            if (nome.contains("cajado") || nome.contains("varinha") || nome.contains("cetro")) {
                desenharSpriteCajado(g2, x, y);
            } else {
                desenharSpriteEspada(g2, x, y);
            }
            return;
        }

        boolean mana = GerenciadorJogo.ITEM_MANA.equals(item.getNome());
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

    private void desenharSpriteEspada(Graphics2D g2, int x, int y) {
        g2.setColor(Estilo.PRETO);
        for (int i = 0; i < 16; i++) {
            g2.fillRect(x + 23 - i, y + 3 + i, 6, 6);
        }
        g2.setColor(new Color(0x9BAEC2));
        for (int i = 0; i < 15; i++) {
            g2.fillRect(x + 24 - i, y + 4 + i, 4, 4);
        }
        g2.setColor(new Color(0xF0F6FC));
        for (int i = 0; i < 14; i++) {
            g2.fillRect(x + 25 - i, y + 4 + i, 2, 2);
        }
        g2.setColor(Estilo.PRETO);
        g2.fillRect(x + 5, y + 16, 14, 6);
        g2.setColor(Estilo.OURO);
        g2.fillRect(x + 6, y + 17, 12, 4);
        g2.setColor(Estilo.OURO_CLARO);
        g2.fillRect(x + 7, y + 17, 4, 2);

        g2.setColor(Estilo.PRETO);
        g2.fillRect(x + 4, y + 22, 6, 6);
        g2.setColor(new Color(0x7C4D2B));
        g2.fillRect(x + 5, y + 22, 4, 4);
        g2.setColor(Estilo.OURO);
        g2.fillRect(x + 3, y + 26, 4, 4);
    }

    private void desenharSpriteCajado(Graphics2D g2, int x, int y) {
        g2.setColor(Estilo.PRETO);
        for (int i = 0; i < 18; i++) {
            g2.fillRect(x + 20 - i, y + 9 + i, 5, 5);
        }
        g2.setColor(new Color(0x8C5828));
        for (int i = 0; i < 17; i++) {
            g2.fillRect(x + 21 - i, y + 10 + i, 3, 3);
        }
        g2.setColor(new Color(0xB87A3D));
        for (int i = 0; i < 15; i++) {
            g2.fillRect(x + 21 - i, y + 10 + i, 1, 1);
        }

        g2.setColor(Estilo.PRETO);
        g2.fillRect(x + 18, y + 3, 10, 10);
        g2.setColor(Estilo.OURO);
        g2.fillRect(x + 19, y + 6, 8, 6);

        g2.setColor(new Color(0x2E86DE));
        g2.fillRect(x + 20, y + 3, 6, 6);
        g2.setColor(new Color(0x54E8FF));
        g2.fillRect(x + 21, y + 4, 4, 4);
        g2.setColor(Color.WHITE);
        g2.fillRect(x + 21, y + 4, 2, 2);
    }
}