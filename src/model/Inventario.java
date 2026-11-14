package model;

import exception.ItemNaoEncontradoException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Inventário de um personagem. Cada objeto Item representa uma unidade. */
public class Inventario {

    private final Personagem dono;
    private final List<Item> itens = new ArrayList<>();
    private Arma armaEquipada;

    public Inventario(Personagem dono) {
        if (dono == null) {
            throw new IllegalArgumentException("O dono do inventário não pode ser nulo.");
        }
        this.dono = dono;
    }

    public void adicionarItem(Item item) {
        if (item == null) {
            throw new IllegalArgumentException("O item não pode ser nulo.");
        }
        if (itens.contains(item)) {
            throw new IllegalArgumentException("Este objeto já está no inventário.");
        }
        itens.add(item);
    }

    /** Nomes repetidos são permitidos; a busca retorna a primeira unidade. */
    public Item buscarItem(String nome) throws ItemNaoEncontradoException {
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("Informe o nome do item.");
        }
        for (Item item : itens) {
            if (nome.equalsIgnoreCase(item.getNome())) {
                return item;
            }
        }
        throw new ItemNaoEncontradoException(nome);
    }

    public Item removerItem(String nome) throws ItemNaoEncontradoException {
        Item item = buscarItem(nome);
        itens.remove(item);
        if (item == armaEquipada) {
            desequiparArma();
        }
        return item;
    }

    /** Cópia somente para leitura, preservando o controle da coleção. */
    public List<Item> listarItens() {
        return Collections.unmodifiableList(new ArrayList<>(itens));
    }

    public void usarItem(String nome) throws ItemNaoEncontradoException {
        Item item = buscarItem(nome);
        if (item instanceof Pocao) {
            usarPocao(nome);
        } else if (item instanceof Arma) {
            equiparArma(nome);
        } else {
            throw new IllegalArgumentException("Este tipo de item não pode ser utilizado.");
        }
    }

    /** Consome uma unidade apenas quando houve recuperação de vida. */
    public void usarPocao(String nome) throws ItemNaoEncontradoException {
        Item item = buscarItem(nome);
        if (!(item instanceof Pocao)) {
            throw new IllegalArgumentException("O item informado não é uma poção.");
        }
        int vidaAntes = dono.getVida();
        ((Pocao) item).usar(dono);
        if (dono.getVida() > vidaAntes) {
            itens.remove(item);
        }
    }

    /** Substitui a arma anterior; nunca acumula bônus ao reequipar. */
    public void equiparArma(String nome) throws ItemNaoEncontradoException {
        Item item = buscarItem(nome);
        if (!(item instanceof Arma)) {
            throw new IllegalArgumentException("O item informado não é uma arma.");
        }
        armaEquipada = (Arma) item;
    }

    public void desequiparArma() {
        armaEquipada = null;
    }

    public Arma getArmaEquipada() {
        return armaEquipada;
    }

    /** O combate deverá consultar este valor para considerar a arma. */
    public int getAtaqueTotal() {
        int bonus = armaEquipada == null ? 0 : armaEquipada.getBonusAtaque();
        return Math.addExact(dono.getAtaque(), bonus);
    }
}
