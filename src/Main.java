import model.Guerreiro;
import model.Personagem;
import model.Esqueleto;
import model.Dragao;


public class Main {
    public static void main(String[] args) {


        Personagem heroi = new Guerreiro("Josias", 100, 100, 15, 10);
        Personagem inimigo_Guerreiro = new Guerreiro("Roberto", 70, 100, 10, 8);
        Personagem inimigo_Esqueleto_1 = new Esqueleto("Ossada");

        heroi.atacar(inimigo_Guerreiro);
        inimigo_Esqueleto_1.atacar(heroi);

        Personagem inimigo_Dragao_1 = new Dragao("Ignis");
        inimigo_Dragao_1.atacar(heroi);
    }
}

// classe main ainda apenas para demonstração
