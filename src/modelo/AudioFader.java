package modelo;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

public class AudioFader {
	private static final double INTERVALO = 0.1;
    private double fadeVolume = 0;
    private double masterVolume = 1.0;

    private final MediaPlayer mediaPlayer;
    private Timeline fadeTimeline;

    public AudioFader(MediaPlayer mediaPlayer) {
        this.mediaPlayer = mediaPlayer;
    }

    public void setMasterVolume(double masterVolume) {
        this.masterVolume = masterVolume;
        applyVolume(); // aplica el volumen combinado actual
    }

    private void applyVolume() {
        mediaPlayer.setVolume(fadeVolume * masterVolume);
    }

    public void fadeIn(double duracion, double volumenObjetivo, Runnable onFinish) {
        if (fadeTimeline != null) fadeTimeline.stop();

        int steps = (int) (duracion / INTERVALO);
        double increment = volumenObjetivo / steps;

        fadeTimeline = new Timeline();
        fadeVolume = 0;
        applyVolume();

        KeyFrame keyFrame = new KeyFrame(Duration.seconds(INTERVALO), event -> {
            fadeVolume = Math.min(fadeVolume + increment, volumenObjetivo);
            applyVolume();
        });

        fadeTimeline.getKeyFrames().add(keyFrame);
        fadeTimeline.setCycleCount(steps);
        fadeTimeline.setOnFinished(e -> {
            fadeVolume = volumenObjetivo;
            applyVolume();
            if (onFinish != null) onFinish.run();
        });

        fadeTimeline.play();
    }

    public void fadeOut(double duracion, Runnable onFinish) {
    	
        if (fadeTimeline != null) fadeTimeline.stop();

        int steps = (int) (duracion / INTERVALO);
        double decrement = fadeVolume / steps;

        fadeTimeline = new Timeline();

        KeyFrame keyFrame = new KeyFrame(Duration.seconds(INTERVALO), event -> {
            fadeVolume = Math.max(0, fadeVolume - decrement);
            applyVolume();
        });

        fadeTimeline.getKeyFrames().add(keyFrame);
        fadeTimeline.setCycleCount(steps);
        fadeTimeline.setOnFinished(e -> {
            fadeVolume = 0;
            applyVolume();
            if (onFinish != null) onFinish.run();
        });

        fadeTimeline.play();
    }

    public void stopFade() {
        if (fadeTimeline != null) {
            fadeTimeline.stop();
        }
    }

    public double getFadeVolume() {
        return fadeVolume;
    }

    public double getEffectiveVolume() {
        return fadeVolume * masterVolume;
    }
}
