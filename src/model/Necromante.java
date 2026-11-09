package model;

public class Necromante extends Monstro {

    public Necromante(String nome) {
        super(nome, 70, 70, 10, 12, "Morto-vivo", 45);
    }

    @Override
    public void atacar(Personagem inimigo) {
        System.out.println(this.nome + " lança uma maldição em " + inimigo.getNome() + "!");
        inimigo.receberDano(this.ataque);
    }

    @Override
    public void habilidadeEspecial(Personagem inimigo) {
        int danoDrenado = this.ataque + 8;
        System.out.println(this.nome + " drena a vida de " + inimigo.getNome() + "!");
        inimigo.receberDano(danoDrenado);

        int cura = danoDrenado / 2;
        this.vida += cura;
        if (this.vida > this.vidaTotal) this.vida = this.vidaTotal;

        System.out.println(this.nome + " recuperou " + cura + " de vida. Vida: " + this.vida + "/" + this.vidaTotal);
    }
}