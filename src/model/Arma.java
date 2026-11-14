package model;

public class Arma extends Item {

    private final int bonusAtaque;

    public Arma(String nome, String descricao, int bonusAtaque) {
        super(nome, descricao);
        if (bonusAtaque <= 0) {
            throw new IllegalArgumentException("O bônus de ataque deve ser maior que zero.");
        }
        this.bonusAtaque = bonusAtaque;
    }

    public int getBonusAtaque() {
        return bonusAtaque;
    }
}
