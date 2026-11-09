package model;

public class Guerreiro extends Personagem {

    private int furia; // Uma habilidade do guerreiro que permite ele dar mais dano.


    public Guerreiro(String nome, int vida, int vidaTotal, int defesa, int ataque){
        super(nome, vida, vidaTotal, defesa, ataque);
        this.furia = 0;
    }

    @Override
    public void atacar(Personagem inimigo){
        System.out.println(this.nome + " atacou " + inimigo.nome + "!");
        inimigo.receberDano(this.ataque);

        this.furia += 20; // Ganha furia ao atacar.

        if (furia > 100)
            furia = 100;

        System.out.println(this.nome + " 20 de fúria gerada pelo ataque! Raiva atual: " + this.furia + "/100!");
    }

    @Override
    public void habilidadeEspecial(Personagem inimigo){
        int custoFuria = 100;

        if(this.furia == custoFuria){
            this.furia = 0;
            int ataqueEspecial = this.ataque + 15;
            inimigo.receberDano(ataqueEspecial);

            System.out.println("Dano crítico realizado em " + inimigo.getNome() + "!");
        }else{
            System.out.println(this.nome + " não possui fúria suficiente! Fúria atual: " + this.furia + "/100.");
        }
    }

    @Override
    public void receberDano(int danoTotal){
        super.receberDano(danoTotal);

        this.furia += 10; // Ganha furia ao levar um ataque.

        if (furia > 100)
            furia = 100;
    }

    public int getFuria(){
        return this.furia;
    }
}
