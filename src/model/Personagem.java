package model;

public abstract class Personagem {

    protected String nome;
    protected int vida;
    protected int vidaTotal;
    protected int defesa;
    protected int ataque;

    public Personagem(String nome, int vida, int vidaTotal, int defesa, int ataque){
        this.nome = nome;
        this.vida = vida;
        this.vidaTotal = vidaTotal;
        this.defesa = defesa;
        this.ataque = ataque;
    }

    public abstract void atacar(Personagem inimigo);
}
