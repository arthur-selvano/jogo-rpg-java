package view;

import controller.GerenciadorJogo;
import exception.VidaInsuficienteException;
import view.component.BotaoPixel;
import view.component.DialogoPixel;
import view.component.EfeitosBatalha;
import view.component.HudJogador;
import view.component.PainelInventario;
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
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Batalha por turnos com sprites, animações de combate, inventário visual e log colorido. */
public class PainelBatalha extends JPanel {

    private static final class LinhaRegistro {
        final String texto;
        final Color cor;

        LinhaRegistro(String texto, Color cor) {
            this.texto = texto;
            this.cor = cor;
        }
    }

    private final TelaPrincipal tela;
    private final GerenciadorJogo jogo;
    private final HudJogador hud;
    private final EfeitosBatalha efeitos;
    private final PainelInventario painelInventario;
    private final List<String> registro = new ArrayList<>();

    private final BotaoPixel btnAtacar;
    private final BotaoPixel btnHabilidade;
    private final BotaoPixel btnSangue;
    private final BotaoPixel btnMeditar;
    private final BotaoPixel btnItem;
    private final BotaoPixel btnFugir;

    private double vidaInimigoVisual = 0;
    private double vidaInimigoRastro = 0;
    private double vidaInimigoAlvo = 0;

    private int hpHeroiAntesInventario = 0;

    public PainelBatalha(TelaPrincipal tela, GerenciadorJogo jogo) {
        this.tela = tela;
        this.jogo = jogo;

        setLayout(null);
        setBackground(Estilo.PRETO);
        setFocusable(true);
        setPreferredSize(new Dimension(TelaPrincipal.LARGURA, TelaPrincipal.ALTURA));

        painelInventario = new PainelInventario(
                jogo,
                this::aoUsarOuEquiparNoInventario,
                tela::mostrarErro,
                () -> {
                    atualizarBotoes();
                    requestFocusInWindow();
                    repaint();
                }
        );
        painelInventario.setBounds(0, 0, TelaPrincipal.LARGURA, TelaPrincipal.ALTURA);
        painelInventario.setVisible(false);
        add(painelInventario);

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

        this.efeitos = new EfeitosBatalha(this);
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        efeitos.parar();
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
                        || painelInventario.isVisible()
                        || jogo.getEstado() != GerenciadorJogo.Estado.BATALHA
                        || efeitos.isOcupado()) {
                    return;
                }
                runnable.run();
            }
        };
    }

    /** Chamado ao entrar na batalha, com as mensagens vindas do mapa. */
    public void aoExibir(List<String> mensagens) {
        painelInventario.setVisible(false);
        registro.clear();
        registro.addAll(mensagens);
        registro.add("O que você vai fazer?");

        vidaInimigoVisual = jogo.getVidaInimigo();
        vidaInimigoRastro = jogo.getVidaInimigo();
        vidaInimigoAlvo = jogo.getVidaInimigo();

        atualizarBotoes();
        hud.repaint();
        requestFocusInWindow();
        repaint();
    }

    private void atualizarBotoes() {
        boolean ativo = jogo.getEstado() == GerenciadorJogo.Estado.BATALHA
                && !efeitos.isOcupado()
                && !painelInventario.isVisible();
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

    private void atacar() {
        if (efeitos.isOcupado() || painelInventario.isVisible()) return;
        int hpInimigoAntes = jogo.getVidaInimigo();
        int hpHeroiAntes = jogo.getVida();
        executar(jogo.atacar(), EfeitosBatalha.TipoGolpe.ATAQUE_FISICO,
                hpInimigoAntes, hpHeroiAntes, 0);
    }

    private void habilidade() {
        if (efeitos.isOcupado() || painelInventario.isVisible()) return;
        int hpInimigoAntes = jogo.getVidaInimigo();
        int hpHeroiAntes = jogo.getVida();
        executar(jogo.usarHabilidade(), EfeitosBatalha.TipoGolpe.HABILIDADE,
                hpInimigoAntes, hpHeroiAntes, 0);
    }

    private void meditar() {
        if (efeitos.isOcupado() || painelInventario.isVisible()) return;
        int hpInimigoAntes = jogo.getVidaInimigo();
        int hpHeroiAntes = jogo.getVida();
        executar(jogo.meditar(), EfeitosBatalha.TipoGolpe.MEDITAR,
                hpInimigoAntes, hpHeroiAntes, 0);
    }

    private void sangue() {
        if (efeitos.isOcupado() || painelInventario.isVisible()) return;
        int hpInimigoAntes = jogo.getVidaInimigo();
        int hpHeroiAntes = jogo.getVida();
        int custoEstimado = Math.max(1, jogo.getVidaTotal() / 10);
        try {
            executar(jogo.golpeDeSangue(), EfeitosBatalha.TipoGolpe.SACRIFICIO,
                    hpInimigoAntes, hpHeroiAntes, custoEstimado);
        } catch (VidaInsuficienteException e) {
            tela.mostrarErro(e);
        }
    }

    private void item() {
        if (efeitos.isOcupado() || painelInventario.isVisible()) return;
        hpHeroiAntesInventario = jogo.getVida();
        painelInventario.abrir();
        atualizarBotoes();
    }

    private void aoUsarOuEquiparNoInventario(GerenciadorJogo.Resultado resultado) {
        if (resultado.isTurnoConsumido()) {
            int hpPosCura = Math.min(jogo.getVidaTotal(), hpHeroiAntesInventario + 30);
            int curaReal = Math.max(0, hpPosCura - hpHeroiAntesInventario);
            int danoInimigoNoHeroi = Math.max(0, hpPosCura - jogo.getVida());

            hud.repaint();
            executarComDeltas(resultado, EfeitosBatalha.TipoGolpe.ITEM_CURA,
                    0, danoInimigoNoHeroi, curaReal);
        } else {
            registro.addAll(resultado.getMensagens());
            while (registro.size() > 40) {
                registro.remove(0);
            }
            hud.repaint();
            repaint();
        }
    }

    private void fugir() {
        if (efeitos.isOcupado() || painelInventario.isVisible()) return;
        int hpInimigoAntes = jogo.getVidaInimigo();
        int hpHeroiAntes = jogo.getVida();
        GerenciadorJogo.Resultado res = jogo.fugir();
        EfeitosBatalha.TipoGolpe tipo = (res.isTurnoConsumido()
                && res.getEstado() == GerenciadorJogo.Estado.BATALHA)
                ? EfeitosBatalha.TipoGolpe.FUGA_FALHOU : null;
        executar(res, tipo, hpInimigoAntes, hpHeroiAntes, 0);
    }

    private void executar(GerenciadorJogo.Resultado resultado,
                          EfeitosBatalha.TipoGolpe tipoGolpe,
                          int hpInimigoAntes,
                          int hpHeroiAntes,
                          int custoSacrificio) {
        int danoNoMonstro = Math.max(0, hpInimigoAntes - jogo.getVidaInimigo());
        int hpHeroiPosCusto = Math.max(0, hpHeroiAntes - custoSacrificio);
        int danoNoHeroi = Math.max(0, hpHeroiPosCusto - jogo.getVida());

        executarComDeltas(resultado, tipoGolpe, danoNoMonstro, danoNoHeroi, 0);
    }

    private void executarComDeltas(GerenciadorJogo.Resultado resultado,
                                   EfeitosBatalha.TipoGolpe tipoGolpe,
                                   int danoNoMonstro,
                                   int danoNoHeroi,
                                   int curaNoHeroi) {
        registro.addAll(resultado.getMensagens());
        while (registro.size() > 40) {
            registro.remove(0);
        }

        Runnable posAnimacao = () -> finalizarTurnoVisual(resultado);

        if (tipoGolpe != null && resultado.isTurnoConsumido()) {
            efeitos.reproduzirTurno(
                    tipoGolpe,
                    danoNoMonstro,
                    danoNoHeroi,
                    curaNoHeroi,
                    resultado.getMensagens(),
                    () -> vidaInimigoAlvo = jogo.getVidaInimigo(),
                    hud::repaint,
                    posAnimacao
            );
            atualizarBotoes();
        } else {
            vidaInimigoAlvo = jogo.getVidaInimigo();
            posAnimacao.run();
        }
    }

    private void finalizarTurnoVisual(GerenciadorJogo.Resultado resultado) {
        vidaInimigoAlvo = jogo.getVidaInimigo();
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

    @Override
    public void doLayout() {
        Estilo.ajustarLayout(this, TelaPrincipal.LARGURA, TelaPrincipal.ALTURA);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            Estilo.escalarTela(g2, this, TelaPrincipal.LARGURA, TelaPrincipal.ALTURA);
            AssetManager.preparar(g2);
            int w = TelaPrincipal.LARGURA;
            int h = TelaPrincipal.ALTURA;

            Estilo.fundoGrama(g2, w, h);
            Estilo.escurecer(g2, w, h, new Color(8, 18, 12, 80));
            Estilo.vinheta(g2, w, h);

            desenharArena(g2, w);
            desenharArvores(g2);

            Point centroHeroi = desenharHeroi(g2);
            Point centroInimigo = desenharInimigo(g2);
            efeitos.renderizar(g2, centroHeroi, centroInimigo);

            desenharRegistro(g2);
            desenharPainelAcoes(g2);
        } finally {
            g2.dispose();
        }
    }

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

    private Point desenharHeroi(Graphics2D g2) {
        String sprite = jogo.getClasseJogador() == GerenciadorJogo.Classe.MAGO
                ? Sprites.MAGO : Sprites.GUERREIRO;
        int tamanho = 128;
        int x = 170 + efeitos.getHeroiOffsetX() + efeitos.getShakeX();
        int y = (330 - tamanho) + efeitos.getHeroiOffsetY() + efeitos.getShakeY();

        Estilo.sombraProjetada(g2, x + tamanho / 2, 326, 96);
        if (!efeitos.isHeroiPiscando()) {
            AssetManager.desenhar(g2, sprite, x, y, tamanho, tamanho);
        }
        PixelFont.desenharCentralizado(g2, jogo.getNomeJogador(), x + tamanho / 2,
                104, 1, Estilo.VERDE, Estilo.PRETO);

        return new Point(x + tamanho / 2, y + tamanho / 2);
    }

    private Point desenharInimigo(Graphics2D g2) {
        boolean chefe = jogo.isBatalhaDeChefe();
        int tamanho = chefe ? 160 : 128;
        int centroBase = 730;
        int centroAnimado = centroBase + efeitos.getMonstroOffsetX() + efeitos.getShakeX();
        int x = centroAnimado - tamanho / 2;
        int y = (330 - tamanho) + efeitos.getMonstroOffsetY() + efeitos.getShakeY();
        String sprite = Sprites.doInimigo(jogo.getTipoInimigo());

        Estilo.sombraProjetada(g2, centroAnimado, 326, (int) (tamanho * 0.72));
        if (!efeitos.isMonstroPiscando()) {
            AssetManager.desenhar(g2, sprite, x, y, tamanho, tamanho);
        }

        if (chefe) {
            PixelFont.desenharCentralizado(g2, "CHEFE", centroBase, 92, 1,
                    Estilo.VERMELHO, Estilo.PRETO);
        }
        PixelFont.centralizadoComSombra(g2, jogo.getNomeInimigo(), centroBase, 108, 2,
                Estilo.VERMELHO, Estilo.PRETO);

        vidaInimigoVisual += (vidaInimigoAlvo - vidaInimigoVisual) * 0.22;
        if (Math.abs(vidaInimigoAlvo - vidaInimigoVisual) < 0.25) {
            vidaInimigoVisual = vidaInimigoAlvo;
        }
        if (vidaInimigoRastro > vidaInimigoVisual) {
            vidaInimigoRastro += (vidaInimigoVisual - vidaInimigoRastro) * 0.08;
        } else {
            vidaInimigoRastro = vidaInimigoVisual;
        }

        int vidaMaxInimigo = Math.max(1, jogo.getVidaTotalInimigo());
        desenharBarraInimigo(g2,
                vidaInimigoVisual / vidaMaxInimigo,
                vidaInimigoRastro / vidaMaxInimigo);

        if (efeitos.isBordaAtiva()) {
            g2.setColor(new Color(255, 220, 100, 110));
            g2.drawRect(centroBase - 144, 128, 288, 24);
        }
        String vida = "HP " + (int) Math.round(vidaInimigoVisual) + "/" + jogo.getVidaTotalInimigo();
        PixelFont.desenharCentralizado(g2, vida, centroBase, 156, 1,
                Estilo.TEXTO, Estilo.PRETO);

        return new Point(centroAnimado, y + tamanho / 2);
    }

    /** Desenha barra de HP do inimigo com rastro dourado quando sofre dano. */
    private void desenharBarraInimigo(Graphics2D g2, double fracaoAtual, double fracaoRastro) {
        int x = 590;
        int y = 132;
        int w = 280;
        int h = 16;

        double fAtual = Math.max(0.0, Math.min(1.0, fracaoAtual));
        double fRastro = Math.max(fAtual, Math.min(1.0, fracaoRastro));

        Estilo.barra(g2, x, y, w, h, fAtual, Estilo.VERMELHO, Estilo.VERMELHO_ESCURO);

        if (fRastro - fAtual > 0.005) {
            int xAtual = x + 2 + (int) ((w - 4) * fAtual);
            int largRastro = Math.max(2, (int) ((w - 4) * (fRastro - fAtual)));
            g2.setColor(Estilo.OURO_CLARO);
            g2.fillRect(xAtual, y + 2, largRastro, h - 4);
        }
    }

    private void desenharRegistro(Graphics2D g2) {
        Estilo.caixaDialogo(g2, 20, 392, 560, 196);
        PixelFont.desenharComSombra(g2, "REGISTRO DE BATALHA", 52, 426, 1,
                Estilo.OURO_CLARO, Estilo.PRETO);

        g2.setFont(Estilo.FONTE_PEQUENA);
        FontMetrics fm = g2.getFontMetrics();
        List<LinhaRegistro> linhas = new ArrayList<>();
        for (String mensagem : registro) {
            Color corMsg = classificarCorMensagem(mensagem);
            for (String trecho : Estilo.quebrar(mensagem, fm, 496)) {
                linhas.add(new LinhaRegistro(trecho, corMsg));
            }
        }
        int maxLinhas = 6;
        int inicio = Math.max(0, linhas.size() - maxLinhas);
        int y = 450;
        for (int i = inicio; i < linhas.size(); i++) {
            LinhaRegistro linha = linhas.get(i);
            g2.setColor(Estilo.SOMBRA_TEXTO);
            g2.drawString(linha.texto, 54, y + 2);
            g2.setColor(linha.cor);
            g2.drawString(linha.texto, 52, y);
            y += 18;
        }
    }

    /** Define a cor semântica de cada linha do Registro de Batalha. */
    private Color classificarCorMensagem(String msg) {
        if (msg == null) return Estilo.TEXTO;
        String lower = msg.toLowerCase();
        String nomeInimigo = jogo.getNomeInimigo().toLowerCase();
        String nomeJogador = jogo.getNomeJogador().toLowerCase();

        if (lower.contains("o que você vai fazer")) {
            return Estilo.OURO_CLARO;
        }
        if (lower.contains("recuperou") || lower.contains("curou")
                || lower.contains("restaurou") || lower.contains("derrotado")
                || lower.contains("xp") || lower.contains("nível")
                || lower.contains("nivel") || lower.contains("salvo")
                || lower.contains("conseguiu fugir")) {
            return Estilo.VERDE;
        }
        if (lower.contains("sacrificou") || lower.contains("derrota")
                || lower.contains("zero") || lower.contains("falhou")
                || lower.contains("bloqueia") || lower.contains("insuficiente")
                || (!nomeInimigo.isEmpty() && lower.startsWith(nomeInimigo))) {
            return new Color(255, 105, 105);
        }
        if (lower.contains("mana") || lower.contains("medit")
                || lower.contains("equipou") || lower.contains("desequipou")
                || lower.contains("essência") || lower.contains("essencia")) {
            return new Color(105, 205, 255);
        }
        if ((!nomeJogador.isEmpty() && lower.startsWith(nomeJogador))
                || lower.contains("atac") || lower.contains("causou")
                || lower.contains("dano") || lower.contains("golpe")
                || lower.contains("fúria") || lower.contains("furia")) {
            return new Color(255, 205, 95);
        }
        return Estilo.TEXTO;
    }

    private void desenharPainelAcoes(Graphics2D g2) {
        Estilo.painelZelda(g2, 592, 392, 348, 196, new Color(16, 24, 38));
        PixelFont.desenharComSombra(g2, "ACOES", 612, 410, 1,
                Estilo.OURO_CLARO, Estilo.PRETO);
    }
}
