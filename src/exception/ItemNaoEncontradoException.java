package exception;

/**
 * Lançada quando um item buscado, removido ou utilizado
 * não existe no inventário.
 */
public class ItemNaoEncontradoException extends Exception {

    private final String nomeItem;

    public ItemNaoEncontradoException(String nomeItem) {
        super("Item não encontrado no inventário: " + nomeItem);
        this.nomeItem = nomeItem;
    }

    public String getNomeItem() {
        return nomeItem;
    }
}
