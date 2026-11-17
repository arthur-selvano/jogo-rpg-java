package persistence;

import java.util.List;
import java.util.Properties;

/** Fotografia independente da partida, sem serializar objetos Java. */
public final class ProgressoJogo {
    private final Properties dados = new Properties();
    private final List<String> itens;

    public ProgressoJogo(Properties dados, List<String> itens) {
        this.dados.putAll(dados);
        this.itens = List.copyOf(itens);
    }

    public Properties getDados() {
        Properties copia = new Properties();
        copia.putAll(dados);
        return copia;
    }

    public List<String> getItens() {
        return itens;
    }
}
