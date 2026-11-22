package view.component;

import controller.GerenciadorJogo;
import exception.ItemNaoEncontradoException;
import model.Arma;
import model.Item;
import model.Pocao;
import view.util.Estilo;
import view.util.PixelFont;

import javax.swing.AbstractAction;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/** Modal visual do inventário com filtro entre Poções e Armas e comparação de atributos. */
public class PainelInventario extends JPanel {

    public enum Aba {
        TODOS("TODOS"),
        POCOES("POCOES"),
        ARMAS("ARMAS");

        final String titulo;
        Aba(String titulo) { this.titulo = titulo; }
    }

    private static final class EntradaItem {
        final int indiceReal;
        final Item item;

        EntradaItem(int indiceReal, Item item) {
            this.indiceReal = indiceReal;
            this.item = item;
        }
    }

    private final GerenciadorJogo jogo;
    private final Consumer<GerenciadorJogo.Resultado> aoExecutarAcao;
    private final Consumer<Exception> aoOcorrerErro;
    private final Runnable aoFechar;

    private final BotaoPixel btnUsarPocao;
    private final BotaoPixel btnEquiparArma;
    private final BotaoPixel btnDesequiparArma;

    private Aba abaAtual = Aba.TODOS;
    private final List<EntradaItem> itensFiltrados = new ArrayList<>();
    private int selecionado = 0;
    private String avisoInline = "";

    // Áreas clicáveis (abas e linhas da lista)
    private final Rectangle[] rectsAbas = new Rectangle[3];
    private final List<Rectangle> rectsLinhas = new ArrayList<>();

    public PainelInventario(GerenciadorJogo jogo,
                            Consumer<GerenciadorJogo.Resultado> aoExecutarAcao,
                            Consumer<Exception> aoOcorrerErro,
                            Runnable aoFechar) {
        this.jogo = jogo;
        this.aoExecutarAcao = aoExecutarAcao;
        this.aoOcorrerErro = aoOcorrerErro;
        this.aoFechar = aoFechar;

        setLayout(null);
        setOpaque(false);
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);

        btnUsarPocao = new BotaoPixel("USAR POCAO", this::acionarItemSelecionado, 165, 38);
        btnUsarPocao.setBounds(515, 386, 165, 38);
        add(btnUsarPocao);

        btnEquiparArma = new BotaoPixel("EQUIPAR", this::acionarItemSelecionado, 165, 38);
        btnEquiparArma.setBounds(515, 386, 165, 38);
        add(btnEquiparArma);

        btnDesequiparArma = new BotaoPixel("DESEQUIPAR", this::desequiparAtual, 165, 38);
        btnDesequiparArma.setBounds(515, 386, 165, 38);
        add(btnDesequiparArma);

        BotaoPixel btnFechar = new BotaoPixel("FECHAR", this::fechar, 130, 38);
        btnFechar.setAtalho("ESC");
        btnFechar.setBounds(692, 386, 130, 38);
        add(btnFechar);

        instalarControles();
    }

    public void abrir() {
        avisoInline = "";
        recarregarLista();
        setVisible(true);
        requestFocusInWindow();
        repaint();
    }

    public void fechar() {
        setVisible(false);
        if (aoFechar != null) {
            aoFechar.run();
        }
    }

    private void instalarControles() {
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ESCAPE"), "fecharInv");
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("UP"), "cimaInv");
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("DOWN"), "baixoInv");
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("TAB"), "abaInv");
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ENTER"), "usarInv");

        getActionMap().put("fecharInv", acaoSeVisivel(this::fechar));
        getActionMap().put("cimaInv", acaoSeVisivel(() -> moverSelecao(-1)));
        getActionMap().put("baixoInv", acaoSeVisivel(() -> moverSelecao(1)));
        getActionMap().put("abaInv", acaoSeVisivel(this::proximaAba));
        getActionMap().put("usarInv", acaoSeVisivel(this::acionarItemSelecionado));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (!isVisible()) return;
                for (int i = 0; i < rectsAbas.length; i++) {
                    if (rectsAbas[i] != null && rectsAbas[i].contains(e.getPoint())) {
                        abaAtual = Aba.values()[i];
                        selecionado = 0;
                        avisoInline = "";
                        recarregarLista();
                        repaint();
                        return;
                    }
                }
                for (int i = 0; i < rectsLinhas.size(); i++) {
                    if (rectsLinhas.get(i).contains(e.getPoint())) {
                        selecionado = i;
                        avisoInline = "";
                        atualizarBotoesContexto();
                        repaint();
                        if (e.getClickCount() == 2) {
                            acionarItemSelecionado();
                        }
                        return;
                    }
                }
            }
        });
    }

    private AbstractAction acaoSeVisivel(Runnable r) {
        return new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isVisible()) r.run();
            }
        };
    }

    private void proximaAba() {
        Aba[] abas = Aba.values();
        abaAtual = abas[(abaAtual.ordinal() + 1) % abas.length];
        selecionado = 0;
        avisoInline = "";
        recarregarLista();
        repaint();
    }

    private void moverSelecao(int delta) {
        if (itensFiltrados.isEmpty()) return;
        selecionado = (selecionado + delta + itensFiltrados.size()) % itensFiltrados.size();
        avisoInline = "";
        atualizarBotoesContexto();
        repaint();
    }

    private void recarregarLista() {
        itensFiltrados.clear();
        List<Item> todos = jogo.getItensInventario();
        for (int i = 0; i < todos.size(); i++) {
            Item item = todos.get(i);
            boolean incluir = abaAtual == Aba.TODOS
                    || (abaAtual == Aba.POCOES && item instanceof Pocao)
                    || (abaAtual == Aba.ARMAS && item instanceof Arma);
            if (incluir) {
                itensFiltrados.add(new EntradaItem(i, item));
            }
        }
        if (selecionado >= itensFiltrados.size()) {
            selecionado = Math.max(0, itensFiltrados.size() - 1);
        }
        atualizarBotoesContexto();
    }

    private void atualizarBotoesContexto() {
        btnUsarPocao.setVisible(false);
        btnEquiparArma.setVisible(false);
        btnDesequiparArma.setVisible(false);

        if (itensFiltrados.isEmpty()) return;

        Item atual = itensFiltrados.get(selecionado).item;
        if (atual instanceof Pocao) {
            btnUsarPocao.setVisible(true);
        } else if (atual instanceof Arma) {
            if (atual == jogo.getArmaEquipada()) {
                btnDesequiparArma.setVisible(true);
            } else {
                btnEquiparArma.setVisible(true);
            }
        }
    }

    private void acionarItemSelecionado() {
        if (itensFiltrados.isEmpty()) return;
        EntradaItem entrada = itensFiltrados.get(selecionado);
        try {
            GerenciadorJogo.Resultado res = jogo.usarItem(entrada.indiceReal);
            if (res.isTurnoConsumido()) {
                // Consumiu poção em batalha: fecha o inventário e dispara a animação
                setVisible(false);
                if (aoExecutarAcao != null) {
                    aoExecutarAcao.accept(res);
                }
            } else {
                // Equipou arma ou poção não pôde ser usada (vida/mana cheia)
                avisoInline = res.getMensagens().isEmpty() ? "" : res.getMensagens().get(0);
                recarregarLista();
                if (aoExecutarAcao != null) {
                    aoExecutarAcao.accept(res);
                }
                repaint();
            }
        } catch (ItemNaoEncontradoException ex) {
            if (aoOcorrerErro != null) aoOcorrerErro.accept(ex);
        }
    }

    private void desequiparAtual() {
        GerenciadorJogo.Resultado res = jogo.desequiparArma();
        avisoInline = res.getMensagens().isEmpty() ? "" : res.getMensagens().get(0);
        recarregarLista();
        if (aoExecutarAcao != null) {
            aoExecutarAcao.accept(res);
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            // Fundo escurecido sobre a arena
            g2.setColor(new Color(4, 8, 14, 185));
            g2.fillRect(0, 0, getWidth(), getHeight());

            int x = 115;
            int y = 74;
            int w = 730;
            int h = 374;

            Estilo.painelZelda(g2, x, y, w, h, new Color(16, 24, 38));
            PixelFont.desenharComSombra(g2, "INVENTARIO DO HEROI", x + 24, y + 32, 2,
                    Estilo.OURO_CLARO, Estilo.PRETO);

            Arma equipada = jogo.getArmaEquipada();
            String textoEquipada = "ARMA: " + (equipada == null ? "NENHUMA"
                    : equipada.getNome().toUpperCase() + " (+" + equipada.getBonusAtaque() + " ATK)");
            PixelFont.desenharComSombra(g2, textoEquipada, x + 24, y + 54, 1,
                    equipada == null ? Estilo.TEXTO : Estilo.VERDE, Estilo.PRETO);

            // Abas: TODOS | POCOES | ARMAS
            int tabX = x + 24;
            int tabY = y + 66;
            Aba[] abas = Aba.values();
            for (int i = 0; i < abas.length; i++) {
                int tabW = 116;
                int tabH = 28;
                rectsAbas[i] = new Rectangle(tabX, tabY, tabW, tabH);
                boolean ativa = abas[i] == abaAtual;

                g2.setColor(ativa ? new Color(56, 78, 114) : new Color(24, 34, 52));
                g2.fillRect(tabX, tabY, tabW, tabH);
                g2.setColor(ativa ? Estilo.OURO_CLARO : new Color(90, 105, 130));
                g2.drawRect(tabX, tabY, tabW, tabH);

                PixelFont.desenharCentralizado(g2, abas[i].titulo,
                        tabX + tabW / 2, tabY + 19, 1,
                        ativa ? Estilo.OURO_CLARO : Estilo.TEXTO, Estilo.PRETO);
                tabX += tabW + 8;
            }

            // Coluna esquerda: Lista de itens
            int listaX = x + 24;
            int listaY = y + 104;
            int listaW = 360;
            int listaH = 248;
            Estilo.caixaDialogo(g2, listaX, listaY, listaW, listaH);

            rectsLinhas.clear();
            if (itensFiltrados.isEmpty()) {
                PixelFont.desenharCentralizado(g2, "NENHUM ITEM NESTA ABA",
                        listaX + listaW / 2, listaY + 120, 1, Estilo.TEXTO, Estilo.PRETO);
            } else {
                int maxVisiveis = 6;
                int inicio = Math.max(0, selecionado - maxVisiveis + 1);
                int rowY = listaY + 14;
                for (int i = inicio; i < Math.min(itensFiltrados.size(), inicio + maxVisiveis); i++) {
                    Item item = itensFiltrados.get(i).item;
                    boolean sel = (i == selecionado);
                    Rectangle rLinha = new Rectangle(listaX + 12, rowY, listaW - 24, 34);
                    rectsLinhas.add(rLinha);

                    if (sel) {
                        g2.setColor(new Color(62, 86, 126, 210));
                        g2.fillRect(rLinha.x, rLinha.y, rLinha.width, rLinha.height);
                        g2.setColor(Estilo.OURO_CLARO);
                        g2.drawRect(rLinha.x, rLinha.y, rLinha.width, rLinha.height);
                    }

                    String tag = (item instanceof Arma) ? "[ARMA]" : "[POCAO]";
                    Color corTag = (item instanceof Arma) ? new Color(255, 195, 90) : Estilo.VERDE;
                    PixelFont.desenharComSombra(g2, tag, rLinha.x + 8, rLinha.y + 22, 1,
                            corTag, Estilo.PRETO);

                    String nomeExibido = item.getNome();
                    if (item == equipada) {
                        nomeExibido += " [E]";
                    }
                    g2.setFont(Estilo.FONTE_PEQUENA);
                    g2.setColor(sel ? Estilo.OURO_CLARO : Estilo.TEXTO);
                    g2.drawString(nomeExibido, rLinha.x + 76, rLinha.y + 22);

                    rowY += 37;
                }
            }

            // Coluna direita: Detalhes e comparação de atributos
            int detX = x + 398;
            int detY = y + 104;
            int detW = 308;
            int detH = 198;
            Estilo.caixaDialogo(g2, detX, detY, detW, detH);

            if (!itensFiltrados.isEmpty()) {
                Item item = itensFiltrados.get(selecionado).item;
                PixelFont.desenharComSombra(g2, item.getNome().toUpperCase(),
                        detX + 18, detY + 32, 1, Estilo.OURO_CLARO, Estilo.PRETO);

                g2.setFont(Estilo.FONTE_PEQUENA);
                FontMetrics fm = g2.getFontMetrics();
                List<String> descLinhas = Estilo.quebrar(item.getDescricao(), fm, detW - 36);
                int dy = detY + 56;
                for (int i = 0; i < Math.min(3, descLinhas.size()); i++) {
                    g2.setColor(Estilo.TEXTO);
                    g2.drawString(descLinhas.get(i), detX + 18, dy);
                    dy += 18;
                }

                // Bloco de impacto numérico (Cura/Mana ou Comparação de Ataque)
                int statY = detY + 126;
                if (item instanceof Pocao pocao) {
                    boolean ehMana = GerenciadorJogo.ITEM_MANA.equals(pocao.getNome());
                    String efeito = ehMana
                            ? "EFEITO: RESTAURA MANA"
                            : "EFEITO: +" + pocao.getQuantidadeCura() + " HP";
                    PixelFont.desenharComSombra(g2, efeito, detX + 18, statY, 1,
                            ehMana ? new Color(90, 190, 255) : Estilo.VERDE, Estilo.PRETO);

                    String atualStr = ehMana
                            ? "MANA ATUAL: " + jogo.getMana() + "/" + jogo.getManaTotal()
                            : "VIDA ATUAL: " + jogo.getVida() + "/" + jogo.getVidaTotal();
                    PixelFont.desenharComSombra(g2, atualStr, detX + 18, statY + 22, 1,
                            Estilo.TEXTO, Estilo.PRETO);
                } else if (item instanceof Arma arma) {
                    int atkAtual = jogo.getAtaque();
                    int bonusAtual = equipada == null ? 0 : equipada.getBonusAtaque();
                    int atkNovo = atkAtual - bonusAtual + arma.getBonusAtaque();
                    int diff = atkNovo - atkAtual;

                    PixelFont.desenharComSombra(g2,
                            "BONUS DA ARMA: +" + arma.getBonusAtaque() + " ATK",
                            detX + 18, statY, 1, Estilo.OURO_CLARO, Estilo.PRETO);

                    String comp = "ATAQUE: " + atkAtual + " -> " + atkNovo
                            + (diff == 0 ? " (EQUIPADA)"
                            : (diff > 0 ? " (+" + diff + ")" : " (" + diff + ")"));
                    Color corComp = diff > 0 ? Estilo.VERDE
                            : (diff < 0 ? Estilo.VERMELHO : Estilo.TEXTO);
                    PixelFont.desenharComSombra(g2, comp, detX + 18, statY + 22, 1,
                            corComp, Estilo.PRETO);
                }
            }

            if (!avisoInline.isEmpty()) {
                g2.setFont(Estilo.FONTE_PEQUENA);
                g2.setColor(Estilo.OURO_CLARO);
                g2.drawString(avisoInline, x + 26, y + h - 14);
            }
        } finally {
            g2.dispose();
        }
    }
}