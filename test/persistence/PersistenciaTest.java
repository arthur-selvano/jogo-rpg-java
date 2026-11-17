package persistence;

import controller.GerenciadorJogo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import java.sql.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class PersistenciaTest {
    @Test
    void restauraBatalhaDasDuasClasses() {
        for (GerenciadorJogo.Classe classe : GerenciadorJogo.Classe.values()) {
            GerenciadorJogo original = new GerenciadorJogo();
            original.iniciarJornada(classe, "Herói ç ' 日本");
            original.avancar();
            original.atacar();
            if (classe == GerenciadorJogo.Classe.MAGO) {
                original.usarHabilidade();
            }

            ProgressoJogo foto = original.exportarProgresso();
            GerenciadorJogo copia = new GerenciadorJogo();
            copia.restaurarProgresso(foto);

            assertEquals(foto.getDados(), copia.exportarProgresso().getDados());
            assertEquals(foto.getItens(), copia.getInventario());
            assertEquals(original.getVidaInimigo(), copia.getVidaInimigo());
        }
    }

    @Test
    void saveInvalidoPreservaPartidaAtual() {
        GerenciadorJogo jogo = new GerenciadorJogo();
        jogo.iniciarJornada(GerenciadorJogo.Classe.MAGO, "Original");
        ProgressoJogo antes = jogo.exportarProgresso();
        Properties invalido = antes.getDados();
        invalido.setProperty("sala", "999");

        assertThrows(IllegalArgumentException.class,
                () -> jogo.restaurarProgresso(
                        new ProgressoJogo(invalido, antes.getItens())));
        assertEquals(antes.getDados(), jogo.exportarProgresso().getDados());
    }

    @Test
    void menuNaoPodeSerSalvo() {
        assertThrows(IllegalStateException.class,
                () -> new GerenciadorJogo().exportarProgresso());
    }

    @Test
    void preservaCargaDoDragao() {
        GerenciadorJogo jogo = new GerenciadorJogo();
        jogo.iniciarJornada(GerenciadorJogo.Classe.MAGO, "Teste");
        ProgressoJogo inicio = jogo.exportarProgresso();
        Properties p = inicio.getDados();
        p.setProperty("sala", "6");
        p.setProperty("visitada.6", "true");
        p.setProperty("estado", "BATALHA");
        p.setProperty("inimigo.tipo", "DRAGAO");
        p.setProperty("inimigo.nome", "Ignis");
        p.setProperty("inimigo.vida", "180");
        p.setProperty("inimigo.fogo", "2");
        p.setProperty("turnos", "2");

        jogo.restaurarProgresso(new ProgressoJogo(p, inicio.getItens()));
        assertEquals("2", jogo.exportarProgresso().getDados()
                .getProperty("inimigo.fogo"));
    }

    /** Exige o banco do Compose e RUN_DB_TESTS=true. */
    @Test
    @EnabledIfEnvironmentVariable(named = "RUN_DB_TESTS", matches = "true")
    void jdbcSalvaAtualizaCarregaEReverteFalha() throws Exception {
        String slot = "teste-" + UUID.randomUUID();
        ProgressoDAO dao = new ProgressoDAO();
        GerenciadorJogo jogo = new GerenciadorJogo();
        jogo.iniciarJornada(GerenciadorJogo.Classe.MAGO, "Teste JDBC");

        try {
            assertTrue(dao.carregar(slot).isEmpty());
            ProgressoJogo inicial = jogo.exportarProgresso();
            dao.salvar(slot, inicial);
            assertEquals(inicial.getItens(),
                    dao.carregar(slot).orElseThrow().getItens());

            jogo.avancar();
            jogo.atacar();
            ProgressoJogo batalha = jogo.exportarProgresso();
            dao.salvar(slot, batalha);
            ProgressoJogo lido = dao.carregar(slot).orElseThrow();

            assertEquals(batalha.getDados(), lido.getDados());
            assertEquals(batalha.getItens(), lido.getItens());

            // PostgreSQL rejeita NUL; o save anterior deve sobreviver ao erro.
            assertThrows(SQLException.class, () -> dao.salvar(slot,
                    new ProgressoJogo(inicial.getDados(),
                            List.of("item" + (char) 0 + "invalido"))));
            ProgressoJogo depoisDaFalha = dao.carregar(slot).orElseThrow();
            assertEquals(batalha.getDados(), depoisDaFalha.getDados());
            assertEquals(batalha.getItens(), depoisDaFalha.getItens());
        } finally {
            try (Connection c = ConexaoBanco.abrir();
                 PreparedStatement excluir = c.prepareStatement(
                         "DELETE FROM progresso WHERE slot = ?")) {
                excluir.setString(1, slot);
                excluir.executeUpdate();
            }
        }
    }
}
