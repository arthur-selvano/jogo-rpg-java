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
        setResizable(false);
        try {
            setIconImage(AssetManager.get(Sprites.PLAYER));
        } catch (RuntimeException ignorado) {
            // sem ícone a janela continua funcionando
        }

        raiz.setPreferredSize(new Dimension(LARGURA, ALTURA));
        raiz.setBackground(Color.BLACK);

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
}
