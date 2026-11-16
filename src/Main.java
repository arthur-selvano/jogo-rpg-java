import view.TelaPrincipal;
import view.component.DialogoPixel;

import javax.swing.SwingUtilities;

/**
 * Ponto de entrada do jogo. Toda a interface Swing é criada na
 * Event Dispatch Thread.
 */
public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Thread.setDefaultUncaughtExceptionHandler((thread, erro) ->
                    SwingUtilities.invokeLater(() ->
                            new DialogoPixel(null, "ERRO INESPERADO",
                                    erro.getMessage() == null
                                            ? "O jogo encontrou um problema inesperado."
                                            : erro.getMessage()).setVisible(true)));

            TelaPrincipal tela = new TelaPrincipal();
            tela.setVisible(true);
        });
    }
}
