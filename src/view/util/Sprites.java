package view.util;

import controller.GerenciadorJogo;

/** Caminhos dos assets usados pela interface. */
public final class Sprites {

    public static final String TITULO = "ui/title_banner.png";
    public static final String BTN_JOGAR = "ui/btn_play.png";
    public static final String BTN_JOGAR_HOVER = "ui/btn_play_hover.png";
    public static final String BTN_SAIR = "ui/btn_exit.png";
    public static final String BTN_SAIR_HOVER = "ui/btn_exit_hover.png";
    public static final String DIALOGO = "ui/dialog_box.png";
    public static final String CORACAO_CHEIO = "ui/heart_full.png";
    public static final String CORACAO_VAZIO = "ui/heart_empty.png";
    public static final String MANA = "ui/mana_full.png";
    public static final String BG_TILE = "ui/bg_tile.png";

    public static final String PLAYER = "sprites/player.png";
    public static final String GUERREIRO = "sprites/guerreiro.png";
    public static final String MAGO = "sprites/mago.png";
    public static final String ESQUELETO = "sprites/esqueleto.png";
    public static final String NECROMANTE = "sprites/necromante.png";
    public static final String DEMONIO = "sprites/demonio.png";
    public static final String DRAGAO = "sprites/dragao.png";

    private Sprites() {
    }

    public static String daClasse(GerenciadorJogo.Classe classe) {
        if (classe == GerenciadorJogo.Classe.MAGO) {
            return MAGO;
        }
        return GUERREIRO;
    }

    public static String doInimigo(GerenciadorJogo.TipoInimigo tipo) {
        if (tipo == null) {
            return ESQUELETO;
        }
        switch (tipo) {
            case NECROMANTE: return NECROMANTE;
            case DEMONIO:    return DEMONIO;
            case DRAGAO:     return DRAGAO;
            case ESQUELETO:
            default:         return ESQUELETO;
        }
    }
}
