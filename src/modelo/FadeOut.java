package modelo;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

public class FadeOut {
	
	private Timeline timeline;
    private MediaPlayer mediaPlayer;
    private double duracion;
    private double volumenInicial;
    private Runnable alFinalizar;
    
    public FadeOut(MediaPlayer mediaPlayer, double duracionSegundos, double volumenInicial, Runnable alFinalizar) {
        this.mediaPlayer = mediaPlayer;
        this.duracion = duracionSegundos;
        this.volumenInicial = volumenInicial;
        this.alFinalizar = alFinalizar;

        iniciarFadeOut();
    }
    
    private void iniciarFadeOut() {
        int pasos = 30; // 30 pasos suaves
        double decremento = volumenInicial / pasos;
        double intervalo = duracion / pasos;

        timeline = new Timeline();

        for (int i = 0; i <= pasos; i++) {
            double nuevoVolumen = Math.max(0, volumenInicial - (i * decremento));
            timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(i * intervalo), e -> {
                mediaPlayer.setVolume(nuevoVolumen);
            }));
        }

        timeline.setOnFinished(e -> {
            if (alFinalizar != null) {
            	mediaPlayer.setVolume(0);
                alFinalizar.run();
            }
        });

        timeline.play();
    }
    
    public void pararFadeOut() {
        if (timeline != null) {
            timeline.stop();
        }
    }
}
