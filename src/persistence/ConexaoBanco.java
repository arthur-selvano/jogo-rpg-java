package persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/** Abre uma conexão JDBC por operação, com configuração de ambiente. */
public final class ConexaoBanco {
    private ConexaoBanco() {}

    public static Connection abrir() throws SQLException {
        String senha = System.getenv("RPG_DB_PASSWORD");
        if (senha == null || senha.isBlank()) {
            throw new SQLException("Defina RPG_DB_PASSWORD no processo Java.");
        }

        String url = ambiente("RPG_DB_URL",
                "jdbc:postgresql://localhost:"
                + ambiente("RPG_DB_PORT", "5432")
                + "/" + ambiente("RPG_DB_NAME", "rpg"));

        Properties configuracao = new Properties();
        configuracao.setProperty("user", ambiente("RPG_DB_USER", "rpg"));
        configuracao.setProperty("password", senha);
        configuracao.setProperty("connectTimeout", "5");
        configuracao.setProperty("socketTimeout", "15");

        return DriverManager.getConnection(url, configuracao);
    }

    private static String ambiente(String nome, String padrao) {
        String valor = System.getenv(nome);
        return valor == null || valor.isBlank() ? padrao : valor;
    }
}
