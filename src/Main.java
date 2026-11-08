import model.Guerreiro;
import model.Personagem;

public class Main {
    public static void main(String[] args) {


        Personagem heroi = new Guerreiro("Josias", 100, 100, 35, 10, 5);
        Personagem inimigo = new Guerreiro("Roberto", 70, 100, 20, 8, 3);

        heroi.atacar(inimigo);



    }
}
