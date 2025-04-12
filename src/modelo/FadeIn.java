package modelo;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

public class FadeIn {
	
	private MediaPlayer mediaPlayer;
    private double volumen, increment, fadeInDuration, intervalo;
    private Timeline fadeInTimeline;
    private double currentVolume; // Hacer currentVolume global para rastrear el progreso
    private Runnable finish;
    
    public FadeIn(MediaPlayer mediaPlayer, double fadeInDuration, double volumenActual, Runnable finish) {
        this.mediaPlayer = mediaPlayer;
        this.fadeInDuration = fadeInDuration;
        this.intervalo = 0.1f; // Frecuencia en segundos
        this.finish = finish;
        volumen = volumenActual;
        currentVolume = 0; // Inicia con volumen 0 para el fade in
        mediaPlayer.setVolume(0); // Iniciar el MediaPlayer con volumen 0
        calcularIncremento();
        iniciarFadeIn();
    }

    private void iniciarFadeIn() {
        fadeInTimeline = new Timeline();
        KeyFrame keyFrame = new KeyFrame(Duration.seconds(intervalo), 
                                         event -> incrementarVolumen());
        fadeInTimeline.getKeyFrames().add(keyFrame);
        fadeInTimeline.setCycleCount((int) (fadeInDuration / intervalo)); // Número de repeticiones basado en la duración
        fadeInTimeline.setAutoReverse(false); // No invertir la animación
        fadeInTimeline.play();
    }

    private void incrementarVolumen() {

        currentVolume =currentVolume + increment;
        // Incrementar o ajustar currentVolume gradualmente
        currentVolume = Math.min(currentVolume, volumen);
        
        mediaPlayer.setVolume(currentVolume);
        //System.out.println(currentVolume);
        if (currentVolume >= volumen - 0.05f)
        {
        	mediaPlayer.setVolume(currentVolume);
            if (fadeInTimeline != null) fadeInTimeline.stop();
            if (finish != null) finish.run(); // ← Lo ejecutás
        }
        //System.out.println("volumen actual: " + currentVolume);
    }

    public void setVolumenActual(double volumen) {
    	this.volumen = volumen; // Actualiza el volumen objetivo    
    	calcularIncremento(); // Recalcula el incremento basado en el nuevo volumen objetivo
        ajustarFadeIn(); // Ajusta la animación en curso con el nuevo volumen   	
     }

    private void calcularIncremento() {
        // Calcular el incremento basado en la diferencia entre currentVolume y el nuevo volumen objetivo
        double diferencia = volumen - currentVolume; // La diferencia refleja el ajuste necesario
        int steps = (int) ((fadeInDuration / intervalo)); // Número de pasos para completar el fade in
        this.increment = diferencia / steps; // Incremento ajustado basado en los pasos
    }

    private void ajustarFadeIn()
    {
    	
    	// Detener la animación actual si está en ejecución
        if (fadeInTimeline != null && fadeInTimeline.getStatus() == Timeline.Status.RUNNING) {
            fadeInTimeline.stop();
        }
        // Reiniciar el fade in con los valores actualizados
        iniciarFadeIn();
    }
    
    public void pararFadeIn() 
    {
    	fadeInTimeline.stop();
    }
}

