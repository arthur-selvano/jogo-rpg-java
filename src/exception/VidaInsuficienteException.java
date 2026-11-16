package exception;

/**
 * Lançada quando uma ação exige mais vida do que o personagem/monstro possui
 * (ex.: habilidade que custa vida) ou quando se tenta agir com vida zerada.
 */
public class VidaInsuficienteException extends Exception {

    private final int vidaAtual;
    private final int vidaNecessaria;

    public VidaInsuficienteException(String mensagem) {
        super(mensagem);
        this.vidaAtual = 0;
        this.vidaNecessaria = 0;
    }

    public VidaInsuficienteException(int vidaAtual, int vidaNecessaria) {
        super("Vida insuficiente: atual = " + vidaAtual
                + ", necessária = " + vidaNecessaria + ".");
        this.vidaAtual = vidaAtual;
        this.vidaNecessaria = vidaNecessaria;
    }

    public int getVidaAtual() {
        return vidaAtual;
    }

    public int getVidaNecessaria() {
        return vidaNecessaria;
    }
}