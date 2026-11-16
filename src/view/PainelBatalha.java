package view;

import controller.GerenciadorJogo;
import exception.ItemNaoEncontradoException;
import exception.VidaInsuficienteException;
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
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Batalha por turnos com sprites, HUD, registro e ações por teclado. */
public class PainelBatalha extends JPanel {

    private final TelaPrincipal tela;
    private final GerenciadorJogo jogo;
    private final HudJogador hud;
    private final List<String> registro = new ArrayList<>();
    private final Timer faisca;

    private final BotaoPixel btnAtacar;
    private final BotaoPixel btnHabilidade;
    private final BotaoPixel btnSangue;
    private final BotaoPixel btnMeditar;
    private final BotaoPixel btnItem;
    private final BotaoPixel btnFugir;

    private boolean piscar = true;

    public PainelBatalha(TelaPrincipal tela, GerenciadorJogo jogo) {
        this.tela = tela;
        this.jogo = jogo;

        setLayout(null);
        setFocusable(true);
        setPreferredSize(new Dimension(TelaPrincipal.LARGURA, TelaPrincipal.ALTURA));

        hud = new HudJogador(jogo);
        hud.setBounds(0, 0, TelaPrincipal.LARGURA, 92);
        add(hud);

        btnAtacar = new BotaoPixel("ATACAR", this::atacar, 150, 40);
        btnAtacar.setBounds(610, 432, 150, 40);
        add(btnAtacar);

        btnHabilidade = new BotaoPixel("HABILIDADE", this::habilidade, 150, 40);
        btnHabilidade.setBounds(774, 432, 150, 40);
        add(btnHabilidade);

        btnSangue = new BotaoPixel("SACRIFICIO", this::sangue, 150, 40);
        btnSangue.setBounds(610, 484, 150, 40);
        add(btnSangue);

        btnMeditar = new BotaoPixel("MEDITAR", this::meditar, 150, 40);
        btnMeditar.setBounds(774, 484, 150, 40);
        add(btnMeditar);

        btnItem = new BotaoPixel("ITEM", this::item, 150, 40);
        btnItem.setBounds(610, 536, 150, 40);
        add(btnItem);

        btnFugir = new BotaoPixel("FUGIR", this::fugir, 150, 40);
        btnFugir.setBounds(774, 536, 150, 40);
        add(btnFugir);

        instalarAtalhos();

        faisca = new Timer(400, e -> {
            piscar = !piscar;
            if (isShowing()) {
                repaint();
            }
        });
        faisca.start();
    }

    private void instalarAtalhos() {
        ligarTecla("1", "atacar");
        ligarTecla("A", "atacar");
        ligarTecla("2", "habilidade");
        ligarTecla("H", "habilidade");
        ligarTecla("3", "sangue");
        ligarTecla("S", "sangue");
        ligarTecla("4", "atalho4");
        ligarTecla("M", "meditar");
        ligarTecla("I", "item");
        ligarTecla("5", "atalho5");
        ligarTecla("6", "fugir");
        ligarTecla("F", "fugir");

        getActionMap().put("atacar", acao(this::atacar));
        getActionMap().put("habilidade", acao(this::habilidade));
        getActionMap().put("sangue", acao(this::sangue));
        getActionMap().put("meditar", acao(this::meditar));
        getActionMap().put("item", acao(this::item));
        getActionMap().put("fugir", acao(this::fugir));
        getActionMap().put("atalho4", acao(() -> {
            if (jogo.isMago()) {
                meditar();
            } else {
                item();
            }
        }));
        getActionMap().put("atalho5", acao(() -> {
            if (jogo.isMago()) {
                item();
            } else {
                fugir();
            }
        }));
    }

    private void ligarTecla(String tecla, String nomeAcao) {
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(tecla), nomeAcao);
    }

    private AbstractAction acao(Runnable runnable) {
        return new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!isShowing()
                        || jogo.getEstado() != GerenciadorJogo.Estado.BATALHA) {
                    return;
                }
                runnable.run();
            }
        };
    }

    /** Chamado ao entrar na batalha, com as mensagens vindas do mapa. */
    public void aoExibir(List<String> mensagens) {
        registro.clear();
        registro.addAll(mensagens);
        registro.add("O que você vai fazer?");
        atualizarBotoes();
        hud.repaint();
        requestFocusInWindow();
        repaint();
    }

    private void atualizarBotoes() {
        boolean ativo = jogo.getEstado() == GerenciadorJogo.Estado.BATALHA;
        btnAtacar.setEnabled(ativo);
        btnHabilidade.setEnabled(ativo);
        btnSangue.setEnabled(ativo);
        btnItem.setEnabled(ativo);
        btnFugir.setEnabled(ativo);
        btnMeditar.setEnabled(ativo);
        btnMeditar.setVisible(jogo.isMago());

        if (jogo.isMago()) {
            btnSangue.setAtalho("3");
            btnMeditar.setAtalho("4");
            btnItem.setAtalho("5");
            btnFugir.setAtalho("6");
        } else {
            btnSangue.setAtalho("3");
            btnItem.setAtalho("4");
            btnFugir.setAtalho("5");
        }
    }

    // ------------------------------------------------------------------
    // Ações
    // ------------------------------------------------------------------

    private void atacar() {
        executar(jogo.atacar());
    }

    private void habilidade() {
        executar(jogo.usarHabilidade());
    }

    private void meditar() {
        executar(jogo.meditar());
    }

    private void sangue() {
        try {
            executar(jogo.golpeDeSangue());
        } catch (VidaInsuficienteException e) {
            tela.mostrarErro(e);
        }
    }

    private void item() {
        int indice = jogo.getIndicePrimeiroItemUtil();
        try {
            executar(jogo.usarItem(indice >= 0 ? indice : 0));
        } catch (ItemNaoEncontradoException e) {
            tela.mostrarErro(e);
        }
    }

    private void fugir() {
        executar(jogo.fugir());
    }

    private void executar(GerenciadorJogo.Resultado resultado) {
        registro.addAll(resultado.getMensagens());
        while (registro.size() > 40) {
            registro.remove(0);
        }
        atualizarBotoes();
        hud.repaint();
        repaint();

        switch (resultado.getEstado()) {
            case DERROTA:
                DialogoPixel.mostrar(this, "DERROTA",
                        "Sua jornada termina aqui...\n"
                                + String.join("\n", resultado.getMensagens()));
                tela.mostrarGameOver(false);
                break;
            case FIM_DA_JORNADA:
                DialogoPixel.mostrar(this, "VITORIA",
                        "O Dragão Ignis caiu!\n"
                                + String.join("\n", resultado.getMensagens()));
                tela.mostrarGameOver(true);
                break;
            case EXPLORACAO:
                boolean venceu = !jogo.getNomeInimigo().isEmpty();
                String titulo = venceu ? "VITORIA" : "FUGA";
                String mensagem = String.join("\n", resultado.getMensagens());
                DialogoPixel.mostrar(this, titulo,
                        mensagem.isEmpty() ? "De volta ao caminho." : mensagem);
                tela.mostrarMapa();
                break;
            default:
                break;
        }
    }

    // ------------------------------------------------------------------
    // Pintura
    // ------------------------------------------------------------------

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            AssetManager.preparar(g2);
            int w = getWidth();
            int h = getHeight();

            Estilo.fundoGrama(g2, w, h);
            Estilo.escurecer(g2, w, h, new Color(8, 18, 12, 80));
            Estilo.vinheta(g2, w, h);

            desenharArena(g2, w);
            desenharArvores(g2);
            desenharHeroi(g2);
            desenharInimigo(g2);
            desenharRegistro(g2);
            desenharPainelAcoes(g2);
        } finally {
            g2.dispose();
        }
    }

    /** Arena de terra batida com pedrinhas e tufos de grama nas bordas. */
    private void desenharArena(Graphics2D g2, int w) {
        int y = 300;
        int h = 92;
        g2.setColor(new Color(0, 0, 0, 80));
        g2.fillRect(0, y + 5, w, h);
        g2.setColor(new Color(76, 54, 30));
        g2.fillRect(0, y, w, h);
        g2.setColor(new Color(108, 80, 44));
        g2.fillRect(0, y + 4, w, h - 8);
        g2.setColor(new Color(136, 100, 54));
        g2.fillRect(0, y + 12, w, h - 24);
        g2.setColor(new Color(58, 38, 20));
        g2.fillRect(0, y, w, 3);
        g2.fillRect(0, y + h - 3, w, 3);

        Random r = new Random(23);
        for (int i = 0; i < 46; i++) {
            int px = r.nextInt(w);
            int py = y + 8 + r.nextInt(h - 16);
            g2.setColor(new Color(100, 92, 80));
            g2.fillRect(px, py, 3, 2);
            g2.setColor(new Color(150, 140, 122));
            g2.fillRect(px, py, 2, 1);
        }
        for (int x = 0; x < w; x += 28) {
            g2.setColor(new Color(42, 116, 52));
            g2.fillRect(x, y - 4, 2, 6);
            g2.fillRect(x + 5, y + h - 2, 2, 6);
            g2.setColor(new Color(96, 186, 96));
            g2.fillRect(x + 1, y - 4, 1, 3);
        }
    }

    /** Árvores de Hyrule emoldurando o combate. */
    private void desenharArvores(Graphics2D g2) {
        Estilo.sombraProjetada(g2, 46, 326, 84);
        g2.setColor(new Color(52, 34, 18));
        g2.fillRect(39, 252, 16, 78);
        g2.setColor(new Color(92, 62, 32));
        g2.fillRect(42, 252, 7, 78);
        g2.setColor(new Color(10, 40, 24));
        g2.fillOval(0, 180, 94, 86);
        g2.setColor(new Color(30, 96, 52));
        g2.fillOval(4, 184, 84, 76);
        g2.setColor(new Color(56, 140, 70));
        g2.fillOval(8, 188, 62, 54);
        g2.setColor(new Color(96, 196, 104));
        g2.fillRect(16, 194, 12, 6);
        g2.fillRect(30, 188, 6, 4);

        Estilo.sombraProjetada(g2, 918, 326, 84);
        g2.setColor(new Color(52, 34, 18));
        g2.fillRect(910, 252, 16, 78);
        g2.setColor(new Color(92, 62, 32));
        g2.fillRect(913, 252, 7, 78);
        g2.setColor(new Color(10, 40, 24));
        g2.fillOval(866, 180, 94, 86);
        g2.setColor(new Color(30, 96, 52));
        g2.fillOval(870, 184, 84, 76);
        g2.setColor(new Color(56, 140, 70));
        g2.fillOval(878, 188, 62, 54);
        g2.setColor(new Color(96, 196, 104));
        g2.fillRect(886, 194, 12, 6);
        g2.fillRect(900, 188, 6, 4);
    }

    private void desenharHeroi(Graphics2D g2) {
        String sprite = jogo.getClasseJogador() == GerenciadorJogo.Classe.MAGO
                ? Sprites.MAGO : Sprites.GUERREIRO;
        int x = 170;
        int tamanho = 128;
        Estilo.sombraProjetada(g2, x + tamanho / 2, 326, 96);
        AssetManager.desenhar(g2, sprite, x, 330 - tamanho, tamanho, tamanho);
        PixelFont.desenharCentralizado(g2, jogo.getNomeJogador(), x + tamanho / 2,
                104, 1, Estilo.VERDE, Estilo.PRETO);
    }

    private void desenharInimigo(Graphics2D g2) {
        boolean chefe = jogo.isBatalhaDeChefe();
        int tamanho = chefe ? 160 : 128;
        int centro = 730;
        int x = centro - tamanho / 2;
        String sprite = Sprites.doInimigo(jogo.getTipoInimigo());

        Estilo.sombraProjetada(g2, centro, 326, (int) (tamanho * 0.72));
        AssetManager.desenhar(g2, sprite, x, 330 - tamanho, tamanho, tamanho);

        if (chefe) {
            PixelFont.desenharCentralizado(g2, "CHEFE", centro, 92, 1,
                    Estilo.VERMELHO, Estilo.PRETO);
        }
        PixelFont.centralizadoComSombra(g2, jogo.getNomeInimigo(), centro, 108, 2,
                Estilo.VERMELHO, Estilo.PRETO);

        Estilo.barra(g2, centro - 140, 132, 280, 16,
                jogo.getVidaTotalInimigo() == 0 ? 0
                        : jogo.getVidaInimigo() / (double) jogo.getVidaTotalInimigo(),
                Estilo.VERMELHO, Estilo.VERMELHO_ESCURO);
        if (piscar) {
            g2.setColor(new Color(255, 220, 100, 110));
            g2.drawRect(centro - 144, 128, 288, 24);
        }
        String vida = "HP " + jogo.getVidaInimigo() + "/" + jogo.getVidaTotalInimigo();
        PixelFont.desenharCentralizado(g2, vida, centro, 156, 1,
                Estilo.TEXTO, Estilo.PRETO);
    }

    private void desenharRegistro(Graphics2D g2) {
        Estilo.caixaDialogo(g2, 20, 392, 560, 196);
        PixelFont.desenharComSombra(g2, "REGISTRO DE BATALHA", 52, 426, 1,
                Estilo.OURO_CLARO, Estilo.PRETO);

        g2.setFont(Estilo.FONTE_PEQUENA);
        FontMetrics fm = g2.getFontMetrics();
        List<String> linhas = new ArrayList<>();
        for (String mensagem : registro) {
            linhas.addAll(Estilo.quebrar(mensagem, fm, 496));
        }
        int maxLinhas = 6;
        int inicio = Math.max(0, linhas.size() - maxLinhas);
        int y = 450;
        for (int i = inicio; i < linhas.size(); i++) {
            g2.setColor(Estilo.SOMBRA_TEXTO);
            g2.drawString(linhas.get(i), 54, y + 2);
            g2.setColor(Estilo.TEXTO);
            g2.drawString(linhas.get(i), 52, y);
            y += 18;
        }
    }

    private void desenharPainelAcoes(Graphics2D g2) {
        Estilo.painelZelda(g2, 592, 392, 348, 196, new Color(16, 24, 38));
        PixelFont.desenharComSombra(g2, "ACOES", 612, 410, 1,
                Estilo.OURO_CLARO, Estilo.PRETO);
    }
}
