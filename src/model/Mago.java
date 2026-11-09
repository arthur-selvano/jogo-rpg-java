package model;

public class Mago extends Personagem{

    private int mana; // Uma habilidade do mago que permite ele dar mais dano.
    private int manaTotal;


    public Mago(String nome, int vida, int vidaTotal, int defesa, int ataque, int manaTotal){
        super(nome, vida, vidaTotal, defesa, ataque);
        this.manaTotal = manaTotal;
        this.mana = manaTotal;
    }

    @Override
    public void atacar(Personagem inimigo){
        System.out.println(this.nome + " lançou magia sobre " + inimigo.getNome() + "! Mana atual: " + mana + "/" + manaTotal);
        inimigo.receberDano(ataque);
    }

    @Override
    public void habilidadeEspecial(Personagem inimigo){
        int custoMana = 30; // Utiliza sua mana para realizar o ataque especial.

        if(this.mana >= custoMana){
            this.mana -= custoMana;

            int ataqueEspecial = this.ataque + 25;

            System.out.println(this.nome + " lançou sua magia especial em " + inimigo.getNome() + "! Mana atual: " + mana + "/" + manaTotal);
            inimigo.receberDano(ataqueEspecial);
        }else{
            System.out.println(this.nome + " não possui mana suficiente para seu ataque especial!");
        }
    }

    public void meditar(){
        int manaRecuperada = 30; // Ganha mana ao meditar.

        this.mana += manaRecuperada;

        if(mana > manaTotal)
            mana = manaTotal;

        System.out.println(this.nome + " recuperou " + manaRecuperada + ". Mana atual: " + mana + "/" + manaTotal);
    }

    public int getMana(){
        return this.mana;
    }

    public int getManaTotal(){
        return this.manaTotal;
    }
}
