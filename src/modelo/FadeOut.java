package modelo;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

public class FadeOut {
	
	private MediaPlayer mediaPlayer;
    private double currentVolume;
    private double decrement;
    private double fadeOutDuration;
    private double intervalo;
    private Timeline fadeOutTimeline;
    private Runnable finish;

    public FadeOut(MediaPlayer mediaPlayer, double fadeOutDuration, double volumenInicial, Runnable finish) {
        this.mediaPlayer = mediaPlayer;
        this.fadeOutDuration = fadeOutDuration;
        this.intervalo = 0.1f; // intervalo en segundos
        this.finish = finish;
        this.currentVolume = volumenInicial;
        mediaPlayer.setVolume(currentVolume);
        calcularDecremento();
        iniciarFadeOut();
    }

    private void iniciarFadeOut() {
        fadeOutTimeline = new Timeline();
        KeyFrame keyFrame = new KeyFrame(Duration.seconds(intervalo),
                                         event -> decrementarVolumen());
        fadeOutTimeline.getKeyFrames().add(keyFrame);
        fadeOutTimeline.setCycleCount((int) (fadeOutDuration / intervalo));
        fadeOutTimeline.setAutoReverse(false);
        fadeOutTimeline.play();
    }

    private void decrementarVolumen() {
        currentVolume -= decrement;
        currentVolume = Math.max(0, currentVolume);

        mediaPlayer.setVolume(currentVolume);

        if (currentVolume <= 0.05f) {
            mediaPlayer.setVolume(0);
            if (fadeOutTimeline != null) fadeOutTimeline.stop();
            if (finish != null) finish.run();
        }
    }

    private void calcularDecremento() {
        double diferencia = currentVolume; // ya que el objetivo siempre es 0
        int steps = (int) (fadeOutDuration / intervalo);
        this.decrement = diferencia / steps;
    }

    public void setVolumenActual(double nuevoVolumenInicial) {
        this.currentVolume = nuevoVolumenInicial;
        mediaPlayer.setVolume(currentVolume);
        calcularDecremento();
        ajustarFadeOut();
    }

    private void ajustarFadeOut() {
        if (fadeOutTimeline != null && fadeOutTimeline.getStatus() == Timeline.Status.RUNNING) {
            fadeOutTimeline.stop();
        }
        iniciarFadeOut();
    }

    public void pararFadeOut() {
        if (fadeOutTimeline != null) {
            fadeOutTimeline.stop();
        }
    }
}
