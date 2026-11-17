package persistence;

import java.io.*;
import java.sql.*;
import java.util.Optional;
import java.util.Properties;

/** Persiste progresso e inventário juntos, com commit ou rollback. */
public final class ProgressoDAO {
    private final InventarioDAO inventario = new InventarioDAO();

    public void salvar(String slot, ProgressoJogo progresso) throws SQLException {
        validarSlot(slot);
        StringWriter texto = new StringWriter();
        try {
            progresso.getDados().store(texto, "RPG save v1");
        } catch (IOException e) {
            throw new SQLException("Falha ao codificar o progresso.", e);
        }

        try (Connection conexao = ConexaoBanco.abrir()) {
            conexao.setAutoCommit(false);
            try {
                try (PreparedStatement comando = conexao.prepareStatement(
                        "INSERT INTO progresso(slot, versao, dados) VALUES (?, 1, ?) "
                        + "ON CONFLICT (slot) DO UPDATE SET versao = 1, "
                        + "dados = EXCLUDED.dados, atualizado_em = CURRENT_TIMESTAMP")) {
                    comando.setString(1, slot);
                    comando.setString(2, texto.toString());
                    comando.executeUpdate();
                }
                inventario.salvar(conexao, slot, progresso.getItens());
                conexao.commit();
            } catch (SQLException | RuntimeException e) {
                try {
                    conexao.rollback();
                } catch (SQLException rollback) {
                    e.addSuppressed(rollback);
                }
                throw e;
            }
        }
    }

    public Optional<ProgressoJogo> carregar(String slot) throws SQLException {
        validarSlot(slot);
        try (Connection conexao = ConexaoBanco.abrir()) {
            // Os dois SELECTs devem observar a mesma versão do save.
            conexao.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
            conexao.setReadOnly(true);
            conexao.setAutoCommit(false);
            try {
                Properties dados = new Properties();
                boolean encontrado;
                try (PreparedStatement consulta = conexao.prepareStatement(
                        "SELECT versao, dados FROM progresso WHERE slot = ?")) {
                    consulta.setString(1, slot);
                    try (ResultSet resultado = consulta.executeQuery()) {
                        encontrado = resultado.next();
                        if (encontrado) {
                            if (resultado.getInt("versao") != 1) {
                                throw new SQLException("Versão de save incompatível.");
                            }
                            try {
                                dados.load(new StringReader(resultado.getString("dados")));
                            } catch (IOException | IllegalArgumentException e) {
                                throw new SQLException("Save inválido.", e);
                            }
                        }
                    }
                }

                if (!encontrado) {
                    conexao.commit();
                    return Optional.empty();
                }

                ProgressoJogo progresso = new ProgressoJogo(
                        dados, inventario.carregar(conexao, slot));
                conexao.commit();
                return Optional.of(progresso);
            } catch (SQLException | RuntimeException e) {
                try {
                    conexao.rollback();
                } catch (SQLException rollback) {
                    e.addSuppressed(rollback);
                }
                throw e;
            }
        }
    }

    private void validarSlot(String slot) {
        if (slot == null || slot.isBlank() || slot.length() > 80) {
            throw new IllegalArgumentException(
                    "O nome do save deve ter entre 1 e 80 caracteres.");
        }
    }
}
