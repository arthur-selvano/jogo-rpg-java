package persistence;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** A transação é controlada pelo ProgressoDAO. */
public final class InventarioDAO {
    public void salvar(Connection conexao, String slot, List<String> itens)
            throws SQLException {
        try (PreparedStatement apagar = conexao.prepareStatement(
                "DELETE FROM inventario_save WHERE slot = ?")) {
            apagar.setString(1, slot);
            apagar.executeUpdate();
        }

        try (PreparedStatement inserir = conexao.prepareStatement(
                "INSERT INTO inventario_save(slot, posicao, item) VALUES (?, ?, ?)")) {
            for (int i = 0; i < itens.size(); i++) {
                inserir.setString(1, slot);
                inserir.setInt(2, i);
                inserir.setString(3, itens.get(i));
                inserir.addBatch();
            }
            inserir.executeBatch();
        }
    }

    public List<String> carregar(Connection conexao, String slot)
            throws SQLException {
        List<String> itens = new ArrayList<>();
        try (PreparedStatement consulta = conexao.prepareStatement(
                "SELECT item FROM inventario_save WHERE slot = ? ORDER BY posicao")) {
            consulta.setString(1, slot);
            try (ResultSet resultado = consulta.executeQuery()) {
                while (resultado.next()) {
                    itens.add(resultado.getString(1));
                }
            }
        }
        return itens;
    }
}
