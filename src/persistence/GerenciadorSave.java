package persistence;

import java.sql.SQLException;
import java.util.Optional;

/** Serviço utilizado pela interface para salvar a última partida manual. */
public final class GerenciadorSave {
    public static final String SLOT_PADRAO = "principal";
    private final ProgressoDAO dao = new ProgressoDAO();

    public void salvar(ProgressoJogo progresso) throws SQLException {
        dao.salvar(SLOT_PADRAO, progresso);
    }

    public Optional<ProgressoJogo> carregar() throws SQLException {
        return dao.carregar(SLOT_PADRAO);
    }
}
