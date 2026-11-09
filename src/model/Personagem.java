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
    public abstract void habilidadeEspecial(Personagem inimigo);


    public void receberDano(int danoTotal){

        int danoRecebido = danoTotal - this.defesa;

        if(danoRecebido > 0){
            this.vida -= danoRecebido;
        }else{
            this.vida -= 1;  // Para se ter pelo menos um dano base de ataque.
            danoRecebido = 1;
        }

        if(vida < 0){
            this.vida = 0; // Se o personagem morrer, deixa a vida dele com 0, invés de negativa.
        }

        System.out.println(this.nome + " recebeu " + danoRecebido + " de dano. \nVida restante: " + this.vida + "/" + this.vidaTotal);
    }

    public boolean personagemVivo(){
        return (this.vida > 0);
    }

    public String getNome(){
        return this.nome;
    }
    public int getVida(){
        return this.vida;
    }
    public int getVidaTotal(){
        return this.vidaTotal;
    }
    public int getDefesa(){
        return this.defesa;
    }
    public int getAtaque(){
        return this.ataque;
    }
}
