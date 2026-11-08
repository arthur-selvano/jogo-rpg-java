package model;

public class Guerreiro extends Personagem {

    private int danoExpecial;



    public Guerreiro(String nome, int vida, int vidaTotal, int defesa, int ataque, int danoExpecial){
        super(nome, vida, vidaTotal, defesa, ataque);
        this.danoExpecial = danoExpecial;
    }

    @Override
    public void atacar(Personagem inimigo){
        inimigo.vida -= 10 + danoExpecial;
        System.out.println("Vida atual do inimigo: " + inimigo.vida);
    }
}
