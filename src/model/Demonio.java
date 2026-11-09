package model;

public class Demonio extends Monstro {

    public Demonio(String nome) {
        super(nome, 120, 120, 18, 20, "Demônio", 80);
    }

    @Override
    public void atacar(Personagem inimigo) {
        System.out.println(this.nome + " ataca " + inimigo.getNome() + " com suas garras infernais!");
        inimigo.receberDano(this.ataque);
    }

    @Override
    public void habilidadeEspecial(Personagem inimigo) {
        int danoFuria = this.ataque + 20;
        System.out.println(this.nome + " libera sua fúria demoníaca sobre " + inimigo.getNome() + "!");
        inimigo.receberDano(danoFuria);
    }
}