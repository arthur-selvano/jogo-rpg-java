package model;

public class Dragao extends Monstro {

    private int cargaFogo;

    public Dragao(String nome) {
        super(nome, 200, 200, 25, 30, "Dragão", 200);
        this.cargaFogo = 0;
    }

    @Override
    public void atacar(Personagem inimigo) {
        System.out.println(this.nome + " ataca " + inimigo.getNome() + " com suas garras!");
        inimigo.receberDano(this.ataque);

        this.cargaFogo++;
        if (this.cargaFogo > 3) this.cargaFogo = 3;
    }

    @Override
    public void habilidadeEspecial(Personagem inimigo) {
        if (this.cargaFogo >= 3) {
            this.cargaFogo = 0;
            int danoSopro = this.ataque + 40;
            System.out.println(this.nome + " libera seu SOPRO DE FOGO sobre " + inimigo.getNome() + "!");
            inimigo.receberDano(danoSopro);
        } else {
            System.out.println(this.nome + " ainda está carregando o sopro de fogo! Carga: "
                    + this.cargaFogo + "/3");
        }
    }

    public int getCargaFogo() {
        return this.cargaFogo;
    }
}