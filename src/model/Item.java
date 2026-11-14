package model;

public abstract class Item {

    private final String nome;
    private final String descricao;

    public Item(String nome, String descricao) {

        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("O nome do item não pode ser vazio.");
        }
        if (descricao == null) {
            throw new IllegalArgumentException("A descrição não pode ser nula.");
        }
        this.nome = nome;
        this.descricao = descricao;

    }

    public String getNome(){
         return nome;

    }

    public String getDescricao(){
        return descricao;
    }

}
