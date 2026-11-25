package view;

import controller.GerenciadorJogo;
import exception.TratadorDeErros;
import view.component.DialogoPixel;
import view.util.AssetManager;
import view.util.Sprites;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

/**
 * Janela principal do jogo. Um {@link CardLayout} alterna entre as telas
 * (menu, criação, mapa, batalha, status e fim de jogo).
 */
public class TelaPrincipal extends JFrame {
    static {
        javax.swing.UIManager.put(
                "AuditoryCues.playList",
                javax.swing.UIManager.get("AuditoryCues.noAuditoryCues")
        );
    }

    public static final int LARGURA = 960;
    public static final int ALTURA = 600;

    private static final String CARD_MENU = "menu";
    private static final String CARD_CRIACAO = "criacao";
    private static final String CARD_MAPA = "mapa";
    private static final String CARD_BATALHA = "batalha";
    private static final String CARD_STATUS = "status";
    private static final String CARD_FIM = "fim";

    private final CardLayout cards = new CardLayout();
    private final JPanel raiz = new JPanel(cards);
    private final GerenciadorJogo jogo = new GerenciadorJogo();

    private final PainelMenu menu;
    private final PainelCriacao criacao;
    private final PainelMapa mapa;
    private final PainelBatalha batalha;
    private final PainelStatus status;
    private final PainelGameOver gameOver;

    public TelaPrincipal() {
        super("RPG - A Lenda do Reino Perdido");

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(true);
        try {
            setIconImage(AssetManager.get(Sprites.PLAYER));
        } catch (RuntimeException ignorado) {
            // sem ícone a janela continua funcionando
        }

        raiz.setPreferredSize(new Dimension(LARGURA, ALTURA));
        raiz.setBackground(Color.BLACK);

        raiz.getActionMap().put("beep", new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                // Silencia o bipe padrão do Swing
            }
        });

        menu = new PainelMenu(this, jogo);
        criacao = new PainelCriacao(this, jogo);
        mapa = new PainelMapa(this, jogo);
        batalha = new PainelBatalha(this, jogo);
        status = new PainelStatus(this, jogo);
        gameOver = new PainelGameOver(this, jogo);

        raiz.add(menu, CARD_MENU);
        raiz.add(criacao, CARD_CRIACAO);
        raiz.add(mapa, CARD_MAPA);
        raiz.add(batalha, CARD_BATALHA);
        raiz.add(status, CARD_STATUS);
        raiz.add(gameOver, CARD_FIM);

        instalarMenuPersistencia();
        setContentPane(raiz);
        pack();
        setLocationRelativeTo(null);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                menu.requestFocusInWindow();
            }
        });

        mostrarMenu();
    }

    // ------------------------------------------------------------------
    // Navegação entre telas
    // ------------------------------------------------------------------

    public void mostrarMenu() {
        cards.show(raiz, CARD_MENU);
        menu.aoExibir();
    }

    public void mostrarCriacao() {
        cards.show(raiz, CARD_CRIACAO);
        criacao.aoExibir();
    }

    public void mostrarMapa() {
        cards.show(raiz, CARD_MAPA);
        mapa.aoExibir();
    }

    public void mostrarBatalha(List<String> mensagens) {
        cards.show(raiz, CARD_BATALHA);
        batalha.aoExibir(mensagens);
    }

    public void mostrarStatus() {
        cards.show(raiz, CARD_STATUS);
        status.aoExibir();
    }

    public void mostrarGameOver(boolean vitoria) {
        cards.show(raiz, CARD_FIM);
        gameOver.aoExibir(vitoria);
    }

    // ------------------------------------------------------------------
    // Alertas amigáveis (sem stacktrace para o jogador)
    // ------------------------------------------------------------------

    /** Mostra a mensagem amigável de uma exceção do pacote exception. */
    public void mostrarErro(Exception e) {
        DialogoPixel.mostrar(this, "ATENCAO", TratadorDeErros.mensagemAmigavel(e));
    }

    /** Mostra um aviso simples dentro do estilo 8-bits do jogo. */
    public void mostrarAviso(String titulo, String mensagem) {
        DialogoPixel.mostrar(this, titulo, mensagem);
    }

    public GerenciadorJogo getJogo() {
        return jogo;
    }

    private boolean persistenciaOcupada;

    private void instalarMenuPersistencia() {
        javax.swing.JMenuBar barra = new javax.swing.JMenuBar();
        javax.swing.JMenu partida = new javax.swing.JMenu("Partida");
        javax.swing.JMenuItem salvar = new javax.swing.JMenuItem("Salvar progresso (F5)");
        javax.swing.JMenuItem carregar = new javax.swing.JMenuItem("Carregar progresso (F9)");

        salvar.setAccelerator(javax.swing.KeyStroke.getKeyStroke("F5"));
        carregar.setAccelerator(javax.swing.KeyStroke.getKeyStroke("F9"));
        salvar.addActionListener(e -> executarPersistencia(false));
        carregar.addActionListener(e -> executarPersistencia(true));

        partida.add(salvar);
        partida.add(carregar);
        barra.add(partida);
        setJMenuBar(barra);
    }

    private void executarPersistencia(boolean carregar) {
        if (persistenciaOcupada) return;

        if (carregar && jogo.getEstado() != GerenciadorJogo.Estado.MENU
                && !DialogoPixel.confirmar(this, "CARREGAR",
                    "Substituir a partida atual pelo último progresso salvo?")) {
            return;
        }

        final persistence.ProgressoJogo fotografia;
        try {
            fotografia = carregar ? null : jogo.exportarProgresso();
        } catch (RuntimeException e) {
            mostrarAviso("SALVAR", e.getMessage());
            return;
        }

        persistenciaOcupada = true;
        java.awt.Component focoAnterior =
                java.awt.KeyboardFocusManager.getCurrentKeyboardFocusManager()
                    .getFocusOwner();
        java.awt.Component vidro = getGlassPane();
        vidro.setFocusable(true);
        vidro.addMouseListener(new java.awt.event.MouseAdapter() {});
        vidro.setVisible(true);
        vidro.requestFocusInWindow();

        // O JDBC roda fora da thread gráfica.
        new javax.swing.SwingWorker<
                java.util.Optional<persistence.ProgressoJogo>, Void>() {
            @Override
            protected java.util.Optional<persistence.ProgressoJogo> doInBackground()
                    throws Exception {
                persistence.GerenciadorSave saves = new persistence.GerenciadorSave();
                if (carregar) return saves.carregar();
                saves.salvar(fotografia);
                return java.util.Optional.empty();
            }

            @Override
            protected void done() {
                vidro.setVisible(false);
                persistenciaOcupada = false;
                try {
                    java.util.Optional<persistence.ProgressoJogo> resultado = get();
                    if (!carregar) {
                        mostrarAviso("SALVAR", "Progresso salvo com sucesso.");
                    } else if (resultado.isEmpty()) {
                        mostrarAviso("CARREGAR", "Nenhuma partida salva foi encontrada.");
                    } else {
                        jogo.restaurarProgresso(resultado.get());
                        switch (jogo.getEstado()) {
                            case BATALHA:
                                mostrarBatalha(java.util.List.of("Batalha recuperada."));
                                break;
                            case DERROTA:
                                mostrarGameOver(false);
                                break;
                            case FIM_DA_JORNADA:
                                mostrarGameOver(true);
                                break;
                            default:
                                mostrarMapa();
                        }
                    }
                } catch (Exception e) {
                    Throwable causa = e.getCause() == null ? e : e.getCause();
                    mostrarAviso(carregar ? "CARREGAR" : "SALVAR",
                            "Não foi possível concluir a operação. "
                            + "Confira o banco e as variáveis de ambiente.\n"
                            + causa.getMessage());
                }
                if (focoAnterior != null && focoAnterior.isShowing()) {
                    focoAnterior.requestFocusInWindow();
                }
            }
        }.execute();
    }
}

