package exception;

import javax.swing.JOptionPane;

/**
 * Ponto único de tratamento de erros do jogo.
 * A interface gráfica (Swing) e o console chamam estes métodos nos blocos catch.
 */
public final class TratadorDeErros {

    private TratadorDeErros() {
    }

    /** Converte a exceção em uma mensagem amigável para o jogador. */
    public static String mensagemAmigavel(Exception e) {
        if (e instanceof VidaInsuficienteException) {
            return "Vida insuficiente para realizar esta ação! (" + e.getMessage() + ")";
        }
        if (e instanceof ItemNaoEncontradoException) {
            return "Esse item não está no seu inventário. (" + e.getMessage() + ")";
        }
        return "Ocorreu um erro inesperado: " + e.getMessage();
    }

    /** Para telas Swing (Pessoas 7 e 8). */
    public static void exibirDialogo(Exception e) {
        JOptionPane.showMessageDialog(null, mensagemAmigavel(e),
                "Atenção", JOptionPane.WARNING_MESSAGE);
    }

    /** Para testes e execução no console. */
    public static void exibirConsole(Exception e) {
        System.out.println("[ERRO] " + mensagemAmigavel(e));
    }
}