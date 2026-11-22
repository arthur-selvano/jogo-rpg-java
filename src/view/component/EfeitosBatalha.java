package view.component;

import view.util.Estilo;
import view.util.PixelFont;

import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Controla animações de turno (herói e inimigo), partículas, shake e textos flutuantes. */
public class EfeitosBatalha {

    public enum TipoGolpe {
        ATAQUE_FISICO,
        HABILIDADE,
        SACRIFICIO,
        MEDITAR,
        ITEM_CURA,
        FUGA_FALHOU
    }

    private enum FaseTurno {
        OCIOSO,
        HEROI_ATACANDO,
        PAUSA_CONTRA_ATAQUE,
        MONSTRO_ATACANDO
    }

    private static final Pattern PADRAO_NUMERO = Pattern.compile("(\\d+)");

    private final JPanel canvas;
    private final Timer loop;
    private final Random random = new Random();

    private double faseIdle = 0.0;
    private int contadorPiscar = 0;
    private boolean bordaAtiva = true;

    private FaseTurno fase = FaseTurno.OCIOSO;
    private double progresso = 0.0;
    private int framesPausa = 0;
    private boolean impactoDisparado = false;
    private Runnable aoFinalizar;

    private int danoNoMonstro = 0;
    private int danoNoHeroi = 0;
    private TipoGolpe tipoAtual = TipoGolpe.ATAQUE_FISICO;

    private Runnable onImpactoMonstro;
    private Runnable onImpactoHeroi;

    private int flashHeroi = 0;
    private int flashMonstro = 0;
    private int shakeFrames = 0;
    private int shakeForca = 0;
    private int arcoCorteMonstro = 0;
    private int arcoCorteHeroi = 0;

    private final List<TextoFlutuante> textos = new ArrayList<>();
    private final List<Particula> particulas = new ArrayList<>();

    public EfeitosBatalha(JPanel canvas) {
        this.canvas = canvas;
        this.loop = new Timer(16, e -> atualizar());
        this.loop.start();
    }

    public void parar() {
        loop.stop();
    }

    public boolean isOcupado() {
        return fase != FaseTurno.OCIOSO;
    }

    public boolean isBordaAtiva() {
        return bordaAtiva;
    }

    /** Inicia a sequência animada do turno usando os deltas reais de HP. */
    public void reproduzirTurno(TipoGolpe tipo,
                                int danoInimigo,
                                int danoHeroi,
                                int curaHeroi,
                                List<String> mensagens,
                                Runnable onImpactoMonstro,
                                Runnable onImpactoHeroi,
                                Runnable onComplete) {
        this.tipoAtual = tipo;
        this.danoNoMonstro = Math.max(0, danoInimigo);
        this.danoNoHeroi = Math.max(0, danoHeroi);
        this.onImpactoMonstro = onImpactoMonstro;
        this.onImpactoHeroi = onImpactoHeroi;
        this.aoFinalizar = onComplete;
        this.progresso = 0.0;
        this.impactoDisparado = false;

        if (tipo == TipoGolpe.MEDITAR) {
            gerarParticulas(true, new Color(90, 190, 255), 22, -2.0);
            textos.add(new TextoFlutuante("+MANA", true, new Color(90, 190, 255), 1));
            iniciarContraAtaqueOuConcluir(16);
            return;
        }

        if (tipo == TipoGolpe.ITEM_CURA) {
            int curaLiquida = Math.max(0, curaHeroi);
            gerarParticulas(true, Estilo.VERDE, 24, -2.2);
            String label = curaLiquida > 0 ? "+" + curaLiquida + " HP" : "ITEM!";
            textos.add(new TextoFlutuante(label, true, Estilo.VERDE, 2));
            iniciarContraAtaqueOuConcluir(22);
            return;
        }

        if (tipo == TipoGolpe.FUGA_FALHOU) {
            textos.add(new TextoFlutuante("FALHOU!", true, Estilo.OURO_CLARO, 1));
            iniciarContraAtaqueOuConcluir(14);
            return;
        }

        if (tipo == TipoGolpe.SACRIFICIO) {
            int custo = extrairPrimeiroValor(mensagens);
            flashHeroi = 8;
            gerarParticulas(true, new Color(220, 30, 60), 20, -2.0);
            if (custo > 0) {
                textos.add(new TextoFlutuante("-" + custo + " HP", true, Estilo.VERMELHO, 1));
            }
        }

        this.fase = FaseTurno.HEROI_ATACANDO;
    }

    private void iniciarContraAtaqueOuConcluir(int pausaFrames) {
        if (danoNoHeroi > 0) {
            this.fase = FaseTurno.PAUSA_CONTRA_ATAQUE;
            this.framesPausa = pausaFrames;
        } else {
            dispararCallbackImpactoHeroi();
            concluirTurno();
        }
    }

    private void concluirTurno() {
        this.fase = FaseTurno.OCIOSO;
        this.progresso = 0.0;
        if (aoFinalizar != null) {
            Runnable cb = aoFinalizar;
            aoFinalizar = null;
            cb.run();
        }
    }

    private void atualizar() {
        faseIdle += 0.08;
        contadorPiscar++;
        if (contadorPiscar >= 25) {
            contadorPiscar = 0;
            bordaAtiva = !bordaAtiva;
        }

        if (fase == FaseTurno.PAUSA_CONTRA_ATAQUE) {
            framesPausa--;
            if (framesPausa <= 0) {
                fase = FaseTurno.MONSTRO_ATACANDO;
                progresso = 0.0;
                impactoDisparado = false;
            }
        } else if (fase == FaseTurno.HEROI_ATACANDO || fase == FaseTurno.MONSTRO_ATACANDO) {
            progresso += 0.065;

            if (progresso >= 0.5 && !impactoDisparado) {
                impactoDisparado = true;
                if (fase == FaseTurno.HEROI_ATACANDO) {
                    aplicarImpactoNoMonstro();
                } else {
                    aplicarImpactoNoHeroi();
                }
            }

            if (progresso >= 1.0) {
                FaseTurno faseConcluida = fase;
                progresso = 0.0;
                impactoDisparado = false;

                if (faseConcluida == FaseTurno.HEROI_ATACANDO && danoNoHeroi > 0) {
                    fase = FaseTurno.PAUSA_CONTRA_ATAQUE;
                    framesPausa = 12;
                } else {
                    concluirTurno();
                }
            }
        }

        if (flashHeroi > 0) flashHeroi--;
        if (flashMonstro > 0) flashMonstro--;
        if (shakeFrames > 0) shakeFrames--;
        if (arcoCorteMonstro > 0) arcoCorteMonstro--;
        if (arcoCorteHeroi > 0) arcoCorteHeroi--;

        Iterator<TextoFlutuante> itT = textos.iterator();
        while (itT.hasNext()) {
            TextoFlutuante t = itT.next();
            t.vida--;
            t.offsetY += 0.9;
            if (t.vida <= 0) itT.remove();
        }

        Iterator<Particula> itP = particulas.iterator();
        while (itP.hasNext()) {
            Particula p = itP.next();
            p.x += p.vx;
            p.y += p.vy;
            p.vy += 0.12;
            p.vida--;
            if (p.vida <= 0) itP.remove();
        }

        canvas.repaint();
    }

    private void aplicarImpactoNoMonstro() {
        flashMonstro = 10;
        boolean golpeForte = tipoAtual == TipoGolpe.HABILIDADE || tipoAtual == TipoGolpe.SACRIFICIO;
        shakeFrames = golpeForte ? 12 : 8;
        shakeForca = golpeForte ? 7 : 4;

        if (onImpactoMonstro != null) {
            onImpactoMonstro.run();
            onImpactoMonstro = null;
        }

        if (danoNoMonstro > 0) {
            Color cor = golpeForte ? Estilo.OURO_CLARO : Estilo.VERMELHO;
            textos.add(new TextoFlutuante("-" + danoNoMonstro, false, cor, golpeForte ? 2 : 1));
        } else {
            textos.add(new TextoFlutuante("BLOQUEADO!", false, Estilo.TEXTO, 1));
        }

        if (tipoAtual == TipoGolpe.HABILIDADE) {
            gerarParticulas(false, new Color(90, 190, 255), 26, 0.0);
        } else if (tipoAtual == TipoGolpe.SACRIFICIO) {
            arcoCorteMonstro = 10;
            gerarParticulas(false, new Color(220, 30, 60), 26, 0.0);
        } else {
            arcoCorteMonstro = 8;
            gerarParticulas(false, Estilo.OURO_CLARO, 14, 0.0);
        }
    }

    private void aplicarImpactoNoHeroi() {
        flashHeroi = 12;
        shakeFrames = 10;
        shakeForca = 6;
        arcoCorteHeroi = 9;

        dispararCallbackImpactoHeroi();

        textos.add(new TextoFlutuante("-" + danoNoHeroi + " HP", true, Estilo.VERMELHO, 2));
        gerarParticulas(true, Estilo.VERMELHO, 18, 0.0);
    }

    private void dispararCallbackImpactoHeroi() {
        if (onImpactoHeroi != null) {
            onImpactoHeroi.run();
            onImpactoHeroi = null;
        }
    }

    private void gerarParticulas(boolean noHeroi, Color cor, int qtd, double empuxoY) {
        for (int i = 0; i < qtd; i++) {
            double ang = random.nextDouble() * Math.PI * 2;
            double vel = 1.5 + random.nextDouble() * 3.5;
            particulas.add(new Particula(
                    noHeroi,
                    Math.cos(ang) * vel,
                    Math.sin(ang) * vel + empuxoY,
                    cor,
                    18 + random.nextInt(12)
            ));
        }
    }

    private int extrairPrimeiroValor(List<String> mensagens) {
        if (mensagens == null || mensagens.isEmpty()) return 0;
        Matcher m = PADRAO_NUMERO.matcher(mensagens.get(0));
        return m.find() ? Integer.parseInt(m.group(1)) : 0;
    }

    public int getHeroiOffsetX() {
        return fase == FaseTurno.HEROI_ATACANDO ? (int) (Math.sin(progresso * Math.PI) * 90) : 0;
    }

    public int getHeroiOffsetY() {
        return fase == FaseTurno.HEROI_ATACANDO ? 0 : (int) (Math.sin(faseIdle) * 3);
    }

    public int getMonstroOffsetX() {
        return fase == FaseTurno.MONSTRO_ATACANDO ? (int) (-Math.sin(progresso * Math.PI) * 95) : 0;
    }

    public int getMonstroOffsetY() {
        return fase == FaseTurno.MONSTRO_ATACANDO ? 0 : (int) (Math.cos(faseIdle) * 3);
    }

    public int getShakeX() {
        return shakeFrames > 0 ? random.nextInt(shakeForca * 2 + 1) - shakeForca : 0;
    }

    public int getShakeY() {
        return shakeFrames > 0 ? random.nextInt(shakeForca * 2 + 1) - shakeForca : 0;
    }

    public boolean isHeroiPiscando() {
        return flashHeroi > 0 && (flashHeroi % 2 == 0);
    }

    public boolean isMonstroPiscando() {
        return flashMonstro > 0 && (flashMonstro % 2 == 0);
    }

    public void renderizar(Graphics2D g2, Point centroHeroi, Point centroMonstro) {
        Composite compOriginal = g2.getComposite();
        Stroke strokeOriginal = g2.getStroke();

        if (arcoCorteMonstro > 0) {
            float alpha = arcoCorteMonstro / 10f;
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawArc(centroMonstro.x - 45, centroMonstro.y - 45, 90, 90, 35, 130);
            g2.setColor(Estilo.OURO_CLARO);
            g2.setStroke(new BasicStroke(2f));
            g2.drawArc(centroMonstro.x - 40, centroMonstro.y - 40, 80, 80, 40, 120);
        }

        if (arcoCorteHeroi > 0) {
            float alpha = arcoCorteHeroi / 9f;
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g2.setColor(new Color(255, 70, 70));
            g2.setStroke(new BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawArc(centroHeroi.x - 45, centroHeroi.y - 45, 90, 90, 15, 130);
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(2f));
            g2.drawArc(centroHeroi.x - 38, centroHeroi.y - 38, 76, 76, 20, 120);
        }

        for (Particula p : particulas) {
            Point base = p.noHeroi ? centroHeroi : centroMonstro;
            float alpha = Math.max(0f, Math.min(1f, p.vida / (float) p.vidaMax));
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g2.setColor(p.cor);
            g2.fillRect((int) (base.x + p.x), (int) (base.y + p.y), 6, 6);
        }

        for (TextoFlutuante t : textos) {
            Point base = t.noHeroi ? centroHeroi : centroMonstro;
            float alpha = Math.max(0f, Math.min(1f, t.vida / (float) t.vidaMax));
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            PixelFont.desenharCentralizado(
                    g2, t.texto, base.x, base.y - 68 - (int) t.offsetY,
                    t.escala, t.cor, Estilo.PRETO
            );
        }

        g2.setComposite(compOriginal);
        g2.setStroke(strokeOriginal);
    }

    private static class TextoFlutuante {
        final String texto;
        final boolean noHeroi;
        final Color cor;
        final int escala;
        final int vidaMax = 48;
        int vida = vidaMax;
        double offsetY = 0;

        TextoFlutuante(String texto, boolean noHeroi, Color cor, int escala) {
            this.texto = texto;
            this.noHeroi = noHeroi;
            this.cor = cor;
            this.escala = escala;
        }
    }

    private static class Particula {
        final boolean noHeroi;
        double x = 0, y = 0;
        double vx, vy;
        final Color cor;
        final int vidaMax;
        int vida;

        Particula(boolean noHeroi, double vx, double vy, Color cor, int vidaMax) {
            this.noHeroi = noHeroi;
            this.vx = vx;
            this.vy = vy;
            this.cor = cor;
            this.vidaMax = vidaMax;
            this.vida = vidaMax;
        }
    }
}