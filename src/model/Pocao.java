package model;

public class Pocao extends Item {

    private int quantidadeCura;

    public Pocao( String nome, String descricao, int quantidadeCura){

        super (nome, descricao);

        if(quantidadeCura<=0){
            throw new IllegalArgumentException("A quantidade de cura deve ser maior que zero.");

        }
        this.quantidadeCura = quantidadeCura;

    }

    public int getQuantidadeCura(){

        return quantidadeCura;

    }

    public  void usar(Personagem personagem){

        if(personagem == null){
            throw new IllegalArgumentException("Personagem inválido.");
        }

        if (!personagem.personagemVivo()){
            System.out.println("Personagem está morto!");
            return;
        }

        int vidaAntes = personagem.getVida();
        personagem.vida = (int) Math.min(
               (long) personagem.getVida() + quantidadeCura,
                personagem.getVidaTotal()
        );

        int vidaRecuperada = personagem.getVida() - vidaAntes;

        System.out.println(personagem.getNome() + " recuperou " + vidaRecuperada + " pontos de vida.");
    }

}
