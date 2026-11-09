package model;

public abstract class Monstro extends Personagem {

    protected String tipo;
    protected int recompensaXP;

    public Monstro(String nome, int vida, int vidaTotal, int defesa, int ataque,
                   String tipo, int recompensaXP) {
        super(nome, vida, vidaTotal, defesa, ataque);
        this.tipo = tipo;
        this.recompensaXP = recompensaXP;
    }


    @Override
    public abstract void atacar(Personagem inimigo);

    @Override
    public abstract void habilidadeEspecial(Personagem inimigo);


    public void rugir() {
        System.out.println(this.nome + " (" + this.tipo + ") ruge ameaçadoramente!");
    }

    public String getTipo() {
        return this.tipo;
    }

    public int getRecompensaXP() {
        return this.recompensaXP;
    }
}