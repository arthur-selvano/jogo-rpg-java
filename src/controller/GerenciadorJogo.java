package controller;

import exception.ItemNaoEncontradoException;
import exception.VidaInsuficienteException;
import model.Demonio;
import model.Dragao;
import model.Esqueleto;
import model.Guerreiro;
import model.Mago;
import model.Monstro;
import model.Necromante;
import model.Personagem;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Controla o fluxo do jogo e é a única ponte entre a camada visual (Swing)
 * e as regras de negócio do pacote {@code model}.
 *
 * <p>A interface nunca lê/escreve no model diretamente: ela pede ações a esta
 * classe e consulta os getters de estado expostos aqui.</p>
 */
public class GerenciadorJogo {

    public static final String ITEM_POCAO = "Poção de Vida";
    public static final String ITEM_MANA = "Essência de Mana";

    private static final int CURA_POCAO = 30;
    private static final int[] LIMIARES_XP = {30, 80, 130};

    /** Estados possíveis da jornada. */
    public enum Estado {
        MENU, EXPLORACAO, BATALHA, DERROTA, FIM_DA_JORNADA
    }

    /** Classes jogáveis disponíveis na criação de personagem. */
    public enum Classe {
        GUERREIRO("Guerreiro", "Resistente. Gera fúria ao atacar e ao levar dano.",
                120, 10, 28, 0),
        MAGO("Mago", "Frágil, mas domina a mana e magias devastadoras.",
                100, 6, 22, 60);

        private final String nome;
        private final String descricao;
        private final int vidaBase;
        private final int defesaBase;
        private final int ataqueBase;
        private final int manaBase;

        Classe(String nome, String descricao, int vidaBase, int defesaBase,
               int ataqueBase, int manaBase) {
            this.nome = nome;
            this.descricao = descricao;
            this.vidaBase = vidaBase;
            this.defesaBase = defesaBase;
            this.ataqueBase = ataqueBase;
            this.manaBase = manaBase;
        }

        public String getNome() { return nome; }
        public String getDescricao() { return descricao; }
        public int getVidaBase() { return vidaBase; }
        public int getDefesaBase() { return defesaBase; }
        public int getAtaqueBase() { return ataqueBase; }
        public int getManaBase() { return manaBase; }
    }

    /** Tipo de cada sala do mapa. */
    public enum TipoSala { INICIO, INIMIGO, TESOURO, CHEFE }

    /** Identifica o inimigo para que a view escolha o sprite correto. */
    public enum TipoInimigo { ESQUELETO, NECROMANTE, DEMONIO, DRAGAO }

    /** Resultado de uma ação (batalha, item, avanço no mapa). */
    public static final class Resultado {
        private final List<String> mensagens;
        private final boolean turnoConsumido;
        private final Estado estado;

        Resultado(List<String> mensagens, boolean turnoConsumido, Estado estado) {
            this.mensagens = Collections.unmodifiableList(new ArrayList<>(mensagens));
            this.turnoConsumido = turnoConsumido;
            this.estado = estado;
        }

        public List<String> getMensagens() { return mensagens; }
        public boolean isTurnoConsumido() { return turnoConsumido; }
        public Estado getEstado() { return estado; }
    }

    private static final class Sala {
        private final String nome;
        private final TipoSala tipo;
        private final TipoInimigo inimigo;
        private final String nomeInimigo;
        private boolean visitada;

        Sala(String nome, TipoSala tipo, TipoInimigo inimigo, String nomeInimigo) {
            this.nome = nome;
            this.tipo = tipo;
            this.inimigo = inimigo;
            this.nomeInimigo = nomeInimigo;
        }
    }

    private final Random sorteio = new Random();
    private final List<String> inventario = new ArrayList<>();
    private List<Sala> salas = criarMapa();

    private Personagem jogador;
    private Classe classeJogador;
    private Monstro inimigo;
    private TipoInimigo tipoInimigo;
    private Estado estado = Estado.MENU;
    private int salaAtual;
    private int xp;
    private int nivel = 1;
    private int inimigosDerrotados;
    private int turnosInimigo;

    // ------------------------------------------------------------------
    // Criação de personagem / jornada
    // ------------------------------------------------------------------

    /**
     * Cria o herói e inicia uma nova jornada.
     *
     * @param classe classe escolhida
     * @param nome   nome do herói (vazio vira "Herói")
     */
    public void iniciarJornada(Classe classe, String nome) {
        String nomeFinal = (nome == null || nome.trim().isEmpty()) ? "Herói" : nome.trim();
        this.classeJogador = classe == null ? Classe.GUERREIRO : classe;
        if (this.classeJogador == Classe.MAGO) {
            jogador = new MagoJogavel(nomeFinal, this.classeJogador.getVidaBase(),
                    this.classeJogador.getDefesaBase(), this.classeJogador.getAtaqueBase(),
                    this.classeJogador.getManaBase());
        } else {
            jogador = new GuerreiroJogavel(nomeFinal, this.classeJogador.getVidaBase(),
                    this.classeJogador.getDefesaBase(), this.classeJogador.getAtaqueBase());
        }

        inventario.clear();
        inventario.add(ITEM_POCAO);
        inventario.add(ITEM_POCAO);

        salas = criarMapa();
        salas.get(0).visitada = true;
        salaAtual = 0;
        inimigo = null;
        tipoInimigo = null;
        turnosInimigo = 0;
        xp = 0;
        nivel = 1;
        inimigosDerrotados = 0;
        estado = Estado.EXPLORACAO;
    }

    /** Volta ao estado inicial (usado ao retornar ao menu principal). */
    public void reiniciar() {
        jogador = null;
        classeJogador = null;
        inimigo = null;
        tipoInimigo = null;
        inventario.clear();
        salas = criarMapa();
        salaAtual = 0;
        xp = 0;
        nivel = 1;
        inimigosDerrotados = 0;
        turnosInimigo = 0;
        estado = Estado.MENU;
    }

    private List<Sala> criarMapa() {
        List<Sala> mapa = new ArrayList<>();
        mapa.add(new Sala("Clareira Inicial", TipoSala.INICIO, null, null));
        mapa.add(new Sala("Floresta dos Ossos", TipoSala.INIMIGO, TipoInimigo.ESQUELETO, "Ossada"));
        mapa.add(new Sala("Ruínas Esquecidas", TipoSala.TESOURO, null, null));
        mapa.add(new Sala("Cripta Sombria", TipoSala.INIMIGO, TipoInimigo.NECROMANTE, "Morvan"));
        mapa.add(new Sala("Santuário Perdido", TipoSala.TESOURO, null, null));
        mapa.add(new Sala("Portão Infernal", TipoSala.INIMIGO, TipoInimigo.DEMONIO, "Belzebu"));
        mapa.add(new Sala("Covil do Dragão", TipoSala.CHEFE, TipoInimigo.DRAGAO, "Ignis"));
        return mapa;
    }

    // ------------------------------------------------------------------
    // Exploração / mapa
    // ------------------------------------------------------------------

    /** Avança para a próxima sala e dispara o evento dela. */
    public Resultado avancar() {
        List<String> msgs = new ArrayList<>();
        if (estado != Estado.EXPLORACAO) {
            msgs.add("Você não pode avançar agora.");
            return new Resultado(msgs, false, estado);
        }
        if (salaAtual >= salas.size() - 1) {
            msgs.add("Não há mais caminhos a seguir.");
            return new Resultado(msgs, false, estado);
        }

        salaAtual++;
        Sala sala = salas.get(salaAtual);
        sala.visitada = true;

        switch (sala.tipo) {
            case INIMIGO:
            case CHEFE:
                inimigo = criarInimigo(sala.inimigo, sala.nomeInimigo);
                tipoInimigo = sala.inimigo;
                turnosInimigo = 0;
                estado = Estado.BATALHA;
                msgs.add(sala.nome + ": " + sala.nomeInimigo + " bloqueia o caminho!");
                break;
            case TESOURO:
                String item = sortearItem();
                inventario.add(item);
                msgs.add("Você encontrou um baú antigo com: " + item + "!");
                break;
            default:
                msgs.add("O caminho está tranquilo por aqui.");
        }
        return new Resultado(msgs, false, estado);
    }

    private String sortearItem() {
        return sorteio.nextDouble() < 0.7 ? ITEM_POCAO : ITEM_MANA;
    }

    private Monstro criarInimigo(TipoInimigo tipo, String nome) {
        switch (tipo) {
            case NECROMANTE: return new Necromante(nome);
            case DEMONIO:    return new Demonio(nome);
            case DRAGAO:     return new Dragao(nome);
            case ESQUELETO:
            default:         return new Esqueleto(nome);
        }
    }

    // ------------------------------------------------------------------
    // Ações de batalha
    // ------------------------------------------------------------------

    /** Ataque básico do herói seguido do turno do inimigo. */
    public Resultado atacar() {
        List<String> msgs = new ArrayList<>();
        if (!podeAgir()) {
            msgs.add("Não há batalha em andamento.");
            return new Resultado(msgs, false, estado);
        }

        msgs.addAll(capturarSaida(() -> jogador.atacar(inimigo)));
        if (!inimigo.personagemVivo()) {
            finalizarVitoria(msgs);
            return new Resultado(msgs, true, estado);
        }
        msgs.addAll(turnoInimigo());
        return new Resultado(msgs, true, estado);
    }

    /** Habilidade especial da classe (fúria do guerreiro / mana do mago). */
    public Resultado usarHabilidade() {
        List<String> msgs = new ArrayList<>();
        if (!podeAgir()) {
            msgs.add("Não há batalha em andamento.");
            return new Resultado(msgs, false, estado);
        }

        if (jogador instanceof Guerreiro) {
            Guerreiro guerreiro = (Guerreiro) jogador;
            if (guerreiro.getFuria() < 100) {
                msgs.add("Fúria insuficiente: " + guerreiro.getFuria()
                        + "/100. Atacar gera fúria!");
                return new Resultado(msgs, false, estado);
            }
        } else if (jogador instanceof Mago) {
            Mago mago = (Mago) jogador;
            if (mago.getMana() < 30) {
                msgs.add("Mana insuficiente: " + mago.getMana() + "/" + mago.getManaTotal()
                        + ". Meditar recupera mana!");
                return new Resultado(msgs, false, estado);
            }
        }

        msgs.addAll(capturarSaida(() -> jogador.habilidadeEspecial(inimigo)));
        if (!inimigo.personagemVivo()) {
            finalizarVitoria(msgs);
            return new Resultado(msgs, true, estado);
        }
        msgs.addAll(turnoInimigo());
        return new Resultado(msgs, true, estado);
    }

    /** Meditação do mago: recupera mana, mas concede um turno ao inimigo. */
    public Resultado meditar() {
        List<String> msgs = new ArrayList<>();
        if (!(jogador instanceof Mago)) {
            msgs.add("Apenas magos podem meditar.");
            return new Resultado(msgs, false, estado);
        }
        Mago mago = (Mago) jogador;
        if (mago.getMana() >= mago.getManaTotal()) {
            msgs.add("Sua mana já está cheia.");
            return new Resultado(msgs, false, estado);
        }
        if (!podeAgir()) {
            msgs.add("Não há batalha em andamento.");
            return new Resultado(msgs, false, estado);
        }

        msgs.addAll(capturarSaida(mago::meditar));
        msgs.addAll(turnoInimigo());
        return new Resultado(msgs, true, estado);
    }

    /**
     * Golpe de sangue: sacrifica parte da própria vida por um ataque poderoso.
     *
     * @throws VidaInsuficienteException quando a vida atual não supera o custo.
     */
    public Resultado golpeDeSangue() throws VidaInsuficienteException {
        List<String> msgs = new ArrayList<>();
        if (!podeAgir()) {
            msgs.add("Não há batalha em andamento.");
            return new Resultado(msgs, false, estado);
        }

        int custo = Math.max(1, jogador.getVidaTotal() / 10);
        if (jogador.getVida() <= custo) {
            throw new VidaInsuficienteException(jogador.getVida(), custo + 1);
        }

        ((PersonagemJogavel) jogador).perderVida(custo);
        int dano = jogador.getAtaque() + custo * 2;
        msgs.add("Você sacrificou " + custo + " de vida por um golpe brutal!");
        msgs.addAll(capturarSaida(() -> inimigo.receberDano(dano)));

        if (!inimigo.personagemVivo()) {
            finalizarVitoria(msgs);
            return new Resultado(msgs, true, estado);
        }
        msgs.addAll(turnoInimigo());
        return new Resultado(msgs, true, estado);
    }

    /** Tenta fugir da batalha (o chefe impede a fuga). */
    public Resultado fugir() {
        List<String> msgs = new ArrayList<>();
        if (!podeAgir()) {
            msgs.add("Não há batalha em andamento.");
            return new Resultado(msgs, false, estado);
        }

        Sala sala = salas.get(salaAtual);
        if (sala.tipo == TipoSala.CHEFE) {
            msgs.add("O Dragão bloqueia a fuga! Não há escapatória.");
            return new Resultado(msgs, false, estado);
        }

        if (sorteio.nextDouble() < 0.5) {
            msgs.add("Você conseguiu fugir da batalha!");
            inimigo = null;
            tipoInimigo = null;
            estado = Estado.EXPLORACAO;
        } else {
            msgs.add("A fuga falhou!");
            msgs.addAll(turnoInimigo());
        }
        return new Resultado(msgs, true, estado);
    }

    // ------------------------------------------------------------------
    // Inventário
    // ------------------------------------------------------------------

    /**
     * Usa o item na posição informada.
     *
     * @throws ItemNaoEncontradoException se a posição não existir no inventário.
     */
    public Resultado usarItem(int indice) throws ItemNaoEncontradoException {
        List<String> msgs = new ArrayList<>();
        if (indice < 0 || indice >= inventario.size()) {
            throw new ItemNaoEncontradoException("posição " + (indice + 1)
                    + " do inventário (total: " + inventario.size() + ")");
        }

        String item = inventario.get(indice);
        boolean turnoConsumido = false;

        if (ITEM_POCAO.equals(item)) {
            if (jogador.getVida() >= jogador.getVidaTotal()) {
                msgs.add("Sua vida já está cheia. A poção continua na mochila.");
                return new Resultado(msgs, false, estado);
            }
            int antes = jogador.getVida();
            ((PersonagemJogavel) jogador).curar(CURA_POCAO);
            inventario.remove(indice);
            msgs.add("Você bebeu uma Poção de Vida e recuperou "
                    + (jogador.getVida() - antes) + " de vida.");
            turnoConsumido = estado == Estado.BATALHA;
        } else if (ITEM_MANA.equals(item)) {
            if (!(jogador instanceof Mago)) {
                msgs.add("Apenas magos podem absorver Essência de Mana.");
                return new Resultado(msgs, false, estado);
            }
            Mago mago = (Mago) jogador;
            if (mago.getMana() >= mago.getManaTotal()) {
                msgs.add("Sua mana já está cheia. A essência continua na mochila.");
                return new Resultado(msgs, false, estado);
            }
            int antes = mago.getMana();
            msgs.addAll(capturarSaida(mago::meditar));
            inventario.remove(indice);
            msgs.add("Você absorveu a Essência de Mana e recuperou "
                    + (mago.getMana() - antes) + " de mana.");
            turnoConsumido = estado == Estado.BATALHA;
        } else {
            inventario.remove(indice);
            msgs.add("O item " + item + " se desfez em suas mãos.");
        }

        if (turnoConsumido) {
            msgs.addAll(turnoInimigo());
        }
        return new Resultado(msgs, turnoConsumido, estado);
    }

    /** Índice do primeiro item que pode ser usado agora (-1 se nenhum). */
    public int getIndicePrimeiroItemUtil() {
        for (int i = 0; i < inventario.size(); i++) {
            String item = inventario.get(i);
            if (ITEM_POCAO.equals(item) && jogador != null
                    && jogador.getVida() < jogador.getVidaTotal()) {
                return i;
            }
            if (ITEM_MANA.equals(item) && jogador instanceof Mago
                    && ((Mago) jogador).getMana() < ((Mago) jogador).getManaTotal()) {
                return i;
            }
        }
        return -1;
    }

    // ------------------------------------------------------------------
    // Internos de combate
    // ------------------------------------------------------------------

    private boolean podeAgir() {
        return estado == Estado.BATALHA && jogador != null
                && inimigo != null && jogador.personagemVivo();
    }

    private List<String> turnoInimigo() {
        List<String> msgs = new ArrayList<>();
        if (inimigo == null || !inimigo.personagemVivo() || !jogador.personagemVivo()) {
            return msgs;
        }
        turnosInimigo++;
        final int turno = turnosInimigo;
        msgs.addAll(capturarSaida(() -> {
            if (turno % 3 == 0) {
                inimigo.habilidadeEspecial(jogador);
            } else {
                inimigo.atacar(jogador);
            }
        }));

        if (!jogador.personagemVivo()) {
            estado = Estado.DERROTA;
            msgs.add("Sua vida chegou a zero. A jornada termina aqui...");
        }
        return msgs;
    }

    private void finalizarVitoria(List<String> msgs) {
        int recompensa = 0;
        if (inimigo != null) {
            recompensa = ((Monstro) inimigo).getRecompensaXP();
        }
        xp += recompensa;
        inimigosDerrotados++;
        if (inimigo != null) {
            msgs.add(inimigo.getNome() + " foi derrotado! +" + recompensa + " XP.");
        }
        aplicarNiveis(msgs);

        Sala sala = salas.get(salaAtual);
        if (sala.tipo == TipoSala.CHEFE) {
            estado = Estado.FIM_DA_JORNADA;
            msgs.add("O Dragão caiu. O reino está salvo!");
        } else {
            estado = Estado.EXPLORACAO;
        }
    }

    private void aplicarNiveis(List<String> msgs) {
        while (nivel - 1 < LIMIARES_XP.length && xp >= LIMIARES_XP[nivel - 1]) {
            nivel++;
            ((PersonagemJogavel) jogador).evoluir();
            msgs.add("Subiu para o nível " + nivel
                    + "! Ataque, defesa e vida aumentaram (vida restaurada).");
        }
    }

    private List<String> capturarSaida(Runnable acao) {
        PrintStream original = System.out;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream captura = new PrintStream(buffer, true, StandardCharsets.UTF_8);
        try {
            System.setOut(captura);
            acao.run();
        } finally {
            System.setOut(original);
            captura.flush();
        }

        List<String> linhas = new ArrayList<>();
        String texto = buffer.toString(StandardCharsets.UTF_8);
        for (String linha : texto.split("\\R")) {
            if (!linha.trim().isEmpty()) {
                linhas.add(linha.trim());
            }
        }
        return linhas;
    }

    // ------------------------------------------------------------------
    // Consultas de estado (usadas pela view)
    // ------------------------------------------------------------------

    public Estado getEstado() { return estado; }

    public String getNomeJogador() { return jogador == null ? "" : jogador.getNome(); }
    public String getNomeClasse() { return classeJogador == null ? "" : classeJogador.getNome(); }
    public Classe getClasseJogador() { return classeJogador; }

    public int getVida() { return jogador == null ? 0 : jogador.getVida(); }
    public int getVidaTotal() { return jogador == null ? 0 : jogador.getVidaTotal(); }
    public int getAtaque() { return jogador == null ? 0 : jogador.getAtaque(); }
    public int getDefesa() { return jogador == null ? 0 : jogador.getDefesa(); }

    public int getMana() {
        return jogador instanceof Mago ? ((Mago) jogador).getMana() : 0;
    }

    public int getManaTotal() {
        return jogador instanceof Mago ? ((Mago) jogador).getManaTotal() : 0;
    }

    public int getFuria() {
        return jogador instanceof Guerreiro ? ((Guerreiro) jogador).getFuria() : 0;
    }

    public boolean isMago() { return jogador instanceof Mago; }

    public int getXp() { return xp; }
    public int getNivel() { return nivel; }
    public int getInimigosDerrotados() { return inimigosDerrotados; }

    /** XP acumulado no início do nível atual (para desenhar a barra de XP). */
    public int getXpNivelInicio() {
        return nivel <= 1 ? 0 : LIMIARES_XP[Math.min(nivel - 2, LIMIARES_XP.length - 1)];
    }

    /** XP necessário para o próximo nível, ou -1 se já está no máximo. */
    public int getXpProximoNivel() {
        return nivel - 1 < LIMIARES_XP.length ? LIMIARES_XP[nivel - 1] : -1;
    }

    public List<String> getInventario() {
        return Collections.unmodifiableList(inventario);
    }

    public int getSalaAtual() { return salaAtual; }
    public int getTotalSalas() { return salas.size(); }
    public String getNomeSala(int indice) { return salas.get(indice).nome; }
    public TipoSala getTipoSala(int indice) { return salas.get(indice).tipo; }
    public boolean isSalaVisitada(int indice) { return salas.get(indice).visitada; }
    public TipoInimigo getTipoInimigoSala(int indice) { return salas.get(indice).inimigo; }

    public String getNomeInimigo() { return inimigo == null ? "" : inimigo.getNome(); }
    public int getVidaInimigo() { return inimigo == null ? 0 : inimigo.getVida(); }
    public int getVidaTotalInimigo() { return inimigo == null ? 0 : inimigo.getVidaTotal(); }
    public TipoInimigo getTipoInimigo() { return tipoInimigo; }
    public boolean isBatalhaDeChefe() {
        return tipoInimigo == TipoInimigo.DRAGAO
                || (salaAtual >= 0 && salaAtual < salas.size()
                    && salas.get(salaAtual).tipo == TipoSala.CHEFE);
    }

    // ==================================================================
    // Subclasses controladas (não alteram o model)
    // ==================================================================

    /**
     * Pequeno contrato implementado pelas subclasses de herói criadas aqui
     * para permitir cura/evolução sem tocar nas classes do pacote model.
     */
    private interface PersonagemJogavel {
        void curar(int quantidade);
        void perderVida(int quantidade);
        void evoluir();
    }

    private static final class GuerreiroJogavel extends Guerreiro
            implements PersonagemJogavel {

        GuerreiroJogavel(String nome, int vida, int defesa, int ataque) {
            super(nome, vida, vida, defesa, ataque);
        }

        @Override
        public void curar(int quantidade) {
            if (quantidade > 0) {
                this.vida = Math.min(this.vidaTotal, this.vida + quantidade);
            }
        }

        @Override
        public void perderVida(int quantidade) {
            this.vida = Math.max(0, this.vida - quantidade);
        }

        @Override
        public void evoluir() {
            this.ataque += 8;
            this.defesa += 3;
            this.vidaTotal += 20;
            this.vida = this.vidaTotal;
        }
    }

    private static final class MagoJogavel extends Mago
            implements PersonagemJogavel {

        MagoJogavel(String nome, int vida, int defesa, int ataque, int mana) {
            super(nome, vida, vida, defesa, ataque, mana);
        }

        @Override
        public void curar(int quantidade) {
            if (quantidade > 0) {
                this.vida = Math.min(this.vidaTotal, this.vida + quantidade);
            }
        }

        @Override
        public void perderVida(int quantidade) {
            this.vida = Math.max(0, this.vida - quantidade);
        }

        @Override
        public void evoluir() {
            this.ataque += 8;
            this.defesa += 3;
            this.vidaTotal += 20;
            this.vida = this.vidaTotal;
        }
    }

    /** Exporta o estado utilizado de fato pela interface gráfica. */
    public persistence.ProgressoJogo exportarProgresso() {
        if (jogador == null || estado == Estado.MENU) {
            throw new IllegalStateException("Inicie uma jornada antes de salvar.");
        }

        java.util.Properties p = new java.util.Properties();
        p.setProperty("formato", "1");
        p.setProperty("nome", getNomeJogador());
        p.setProperty("classe", classeJogador.name());
        p.setProperty("estado", estado.name());
        p.setProperty("vida", String.valueOf(getVida()));
        p.setProperty("vidaTotal", String.valueOf(getVidaTotal()));
        p.setProperty("ataque", String.valueOf(getAtaque()));
        p.setProperty("defesa", String.valueOf(getDefesa()));
        p.setProperty("mana", String.valueOf(getMana()));
        p.setProperty("manaTotal", String.valueOf(getManaTotal()));
        p.setProperty("furia", String.valueOf(getFuria()));
        p.setProperty("sala", String.valueOf(salaAtual));
        p.setProperty("xp", String.valueOf(xp));
        p.setProperty("nivel", String.valueOf(nivel));
        p.setProperty("derrotados", String.valueOf(inimigosDerrotados));
        p.setProperty("turnos", String.valueOf(turnosInimigo));

        for (int i = 0; i < salas.size(); i++) {
            p.setProperty("visitada." + i, String.valueOf(salas.get(i).visitada));
        }

        if (inimigo != null) {
            p.setProperty("inimigo.tipo", tipoInimigo.name());
            p.setProperty("inimigo.nome", inimigo.getNome());
            p.setProperty("inimigo.vida", String.valueOf(inimigo.getVida()));
            if (inimigo instanceof Dragao) {
                p.setProperty("inimigo.fogo",
                        String.valueOf(((Dragao) inimigo).getCargaFogo()));
            }
        }

        return new persistence.ProgressoJogo(p, inventario);
    }

    /** Valida separadamente para preservar a partida se o save for inválido. */
    public void restaurarProgresso(persistence.ProgressoJogo progresso) {
        GerenciadorJogo novo = new GerenciadorJogo();
        novo.lerProgresso(progresso);

        jogador = novo.jogador;
        classeJogador = novo.classeJogador;
        inimigo = novo.inimigo;
        tipoInimigo = novo.tipoInimigo;
        estado = novo.estado;
        salaAtual = novo.salaAtual;
        xp = novo.xp;
        nivel = novo.nivel;
        inimigosDerrotados = novo.inimigosDerrotados;
        turnosInimigo = novo.turnosInimigo;
        salas = novo.salas;
        inventario.clear();
        inventario.addAll(novo.inventario);
    }

    private void lerProgresso(persistence.ProgressoJogo progresso) {
        java.util.Properties p = progresso.getDados();
        if (!"1".equals(p.getProperty("formato"))) {
            throw new IllegalArgumentException("Formato de save incompatível.");
        }
        String nome = p.getProperty("nome");
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Nome salvo inválido.");
        }

        classeJogador = Classe.valueOf(p.getProperty("classe", ""));
        estado = Estado.valueOf(p.getProperty("estado", ""));
        if (estado == Estado.MENU) {
            throw new IllegalArgumentException("Save sem jornada.");
        }

        int total = inteiroSalvo(p, "vidaTotal", 1, Integer.MAX_VALUE);
        int ataque = inteiroSalvo(p, "ataque", 0, Integer.MAX_VALUE);
        int defesa = inteiroSalvo(p, "defesa", 0, Integer.MAX_VALUE);

        jogador = classeJogador == Classe.MAGO
                ? new MagoJogavel(nome, total, defesa, ataque,
                    inteiroSalvo(p, "manaTotal", 0, Integer.MAX_VALUE))
                : new GuerreiroJogavel(nome, total, defesa, ataque);

        jogador.restaurarVida(inteiroSalvo(p, "vida", 0, total));
        if (jogador instanceof Mago) {
            ((Mago) jogador).restaurarMana(
                    inteiroSalvo(p, "mana", 0, getManaTotal()));
        } else {
            ((Guerreiro) jogador).restaurarFuria(
                    inteiroSalvo(p, "furia", 0, 100));
        }

        salaAtual = inteiroSalvo(p, "sala", 0, salas.size() - 1);
        xp = inteiroSalvo(p, "xp", 0, Integer.MAX_VALUE);
        nivel = inteiroSalvo(p, "nivel", 1, LIMIARES_XP.length + 1);
        inimigosDerrotados = inteiroSalvo(p, "derrotados", 0, salas.size());
        turnosInimigo = inteiroSalvo(p, "turnos", 0, Integer.MAX_VALUE);

        for (int i = 0; i < salas.size(); i++) {
            String visitada = p.getProperty("visitada." + i);
            if (!"true".equals(visitada) && !"false".equals(visitada)) {
                throw new IllegalArgumentException("Mapa salvo inválido.");
            }
            salas.get(i).visitada = Boolean.parseBoolean(visitada);
        }
        if (!salas.get(salaAtual).visitada) {
            throw new IllegalArgumentException("Sala atual não visitada.");
        }

        inventario.clear();
        for (String item : progresso.getItens()) {
            if (!ITEM_POCAO.equals(item) && !ITEM_MANA.equals(item)) {
                throw new IllegalArgumentException("Item salvo desconhecido.");
            }
            inventario.add(item);
        }

        if (p.containsKey("inimigo.tipo")) {
            tipoInimigo = TipoInimigo.valueOf(p.getProperty("inimigo.tipo"));
            if (tipoInimigo != salas.get(salaAtual).inimigo) {
                throw new IllegalArgumentException("Inimigo incompatível com a sala.");
            }
            String nomeInimigo = p.getProperty("inimigo.nome");
            if (nomeInimigo == null || nomeInimigo.isBlank()) {
                throw new IllegalArgumentException("Inimigo sem nome.");
            }
            inimigo = criarInimigo(tipoInimigo, nomeInimigo);
            inimigo.restaurarVida(
                    inteiroSalvo(p, "inimigo.vida", 0, inimigo.getVidaTotal()));
            if (inimigo instanceof Dragao) {
                ((Dragao) inimigo).restaurarCargaFogo(
                        inteiroSalvo(p, "inimigo.fogo", 0, 3));
            }
        }

        if (estado == Estado.BATALHA
                && (inimigo == null || !inimigo.personagemVivo())) {
            throw new IllegalArgumentException("Batalha sem inimigo vivo.");
        }
        if ((estado == Estado.DERROTA) != (getVida() == 0)) {
            throw new IllegalArgumentException("Vida incompatível com o estado.");
        }
    }

    private static int inteiroSalvo(java.util.Properties p,
                                   String chave, int minimo, int maximo) {
        try {
            int valor = Integer.parseInt(p.getProperty(chave, ""));
            if (valor >= minimo && valor <= maximo) {
                return valor;
            }
        } catch (NumberFormatException ignorado) {
            // O campo inválido será identificado abaixo.
        }
        throw new IllegalArgumentException("Campo salvo inválido: " + chave);
    }
}

