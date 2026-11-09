package model;

public class Esqueleto extends Monstro {

    public Esqueleto(String nome) {
        super(nome, 40, 40, 5, 8, "Morto-vivo", 20);
    }

    @Override
    public void atacar(Personagem inimigo) {
        System.out.println(this.nome + " golpeia " + inimigo.getNome() + " com seus ossos!");
        inimigo.receberDano(this.ataque);
    }

    @Override
    public void habilidadeEspecial(Personagem inimigo) {
        int danoPerfurante = this.ataque + 4;
        System.out.println(this.nome + " dispara uma flecha de osso em " + inimigo.getNome() + "!");
        inimigo.receberDano(danoPerfurante);
    }
}