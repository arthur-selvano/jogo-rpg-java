package exception;

import java.util.ArrayList;
import java.util.List;

/**
 * Demonstração isolada do uso das exceções (não depende das classes dos colegas).
 * Serve de modelo para integrar em Personagem, Inventario e SistemaBatalha.
 */
public class ExemploIntegracao {

    // Simula Inventario.remover(...) da Pessoa 3
    static void removerItem(List<String> itens, String nome) throws ItemNaoEncontradoException {
        if (!itens.remove(nome)) {
            throw new ItemNaoEncontradoException(nome);
        }
    }

    // Simula uma habilidade que gasta vida (Pessoa 1)
    static int usarHabilidadeSacrificio(int vida, int custo) throws VidaInsuficienteException {
        if (vida <= custo) {
            throw new VidaInsuficienteException(vida, custo + 1);
        }
        return vida - custo;
    }

    public static void main(String[] args) {
        List<String> inventario = new ArrayList<>();
        inventario.add("Pocao");

        try {
            removerItem(inventario, "Pocao");
            removerItem(inventario, "Espada"); // vai lançar
        } catch (ItemNaoEncontradoException e) {
            System.out.println("Erro: " + e.getMessage());
        }

        try {
            int vida = usarHabilidadeSacrificio(10, 15); // vai lançar
            System.out.println("Vida restante: " + vida);
        } catch (VidaInsuficienteException e) {
            System.out.println("Erro: " + e.getMessage());
        } finally {
            System.out.println("Fim do teste.");
        }
    }
}