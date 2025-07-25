package modelo;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import javafx.animation.PauseTransition;

public class AudioFader {

    private final MediaPlayer mediaPlayer;
    private Timeline currentFadeTimeline;
    private double volumenTarget; // Este es el volumen al que siempre queremos llegar si no hay fades especiales
    private double fadeInDuracion; // En segundos
    private double fadeOutDuracion; // En segundos

    // Variables de estado del fade actual
    private double currentFadeStartTimeVolume; // Volumen desde el que se inició el fade actual
    private double currentFadeTargetVolume;    // Volumen objetivo del fade actual

    public AudioFader(MediaPlayer mediaPlayer, double volumenTarget, double fadeInDuracion, double fadeOutDuracion) {
        this.mediaPlayer = mediaPlayer;
        this.volumenTarget = volumenTarget;
        this.fadeInDuracion = fadeInDuracion;
        this.fadeOutDuracion = fadeOutDuracion;

        if (this.fadeInDuracion > 0) {
            this.mediaPlayer.setVolume(0);
            startSmoothFade(this.fadeInDuracion * 1000, this.volumenTarget); // true indica que es un fade-in
        }
        else
            this.mediaPlayer.setVolume(this.volumenTarget);

        // Asegurarse de que el fade-in se reinicie en cada loop
        mediaPlayer.setOnEndOfMedia(() -> {
            if (mediaPlayer.getCycleCount() == MediaPlayer.INDEFINITE) { // Solo si está en modo loop
                mediaPlayer.seek(Duration.ZERO);
                // Reiniciar el fade-in si corresponde
                if (this.fadeInDuracion > 0) {
                    this.mediaPlayer.setVolume(0);
                    startSmoothFade(this.fadeInDuracion * 1000, this.volumenTarget);
                } else {
                    this.mediaPlayer.setVolume(this.volumenTarget);
                }
                mediaPlayer.play();
            }
        });
    }

    private void startSmoothFade(double durationMillis, double targetVolume) {

    	if (currentFadeTimeline != null) {
            currentFadeTimeline.stop();
        }
    	 
    	currentFadeTargetVolume = targetVolume;                
       
        if (durationMillis <= 0 || Math.abs(currentFadeStartTimeVolume - targetVolume) < 0.001) {
            mediaPlayer.setVolume(targetVolume);
            currentFadeTimeline = null;
            return;
        }

        currentFadeTimeline = new Timeline(
            new KeyFrame(Duration.millis(0), event -> {
                // Asegura que el volumen inicial del fade sea el volumen actual del mediaPlayer
                mediaPlayer.setVolume(currentFadeStartTimeVolume);
            }),
            new KeyFrame(Duration.millis(durationMillis), event -> {
                mediaPlayer.setVolume(targetVolume);
                currentFadeTimeline = null; // Fade terminado
            })
        );

        currentFadeTimeline.currentTimeProperty().addListener((obs, oldVal, newVal) -> {
            // Recalcula el volumen basado en el progreso actual y los *valores de inicio/fin del fade*
            double progress = newVal.toMillis() / durationMillis;
            double currentVolume = currentFadeStartTimeVolume + (currentFadeTargetVolume - currentFadeStartTimeVolume) * progress;
            mediaPlayer.setVolume(currentVolume);
        });

        currentFadeTimeline.play();
    }

    // Este es el método que creadorReproductor llama cuando el volumen cambia
    public void setVolumenTarget(double newVolumen) {
       
    	this.volumenTarget = newVolumen; // Actualizar el volumen objetivo final
    	
    	boolean dentroDelFadeIn = mediaPlayer.getCurrentTime().toSeconds() < fadeInDuracion;

    	if (dentroDelFadeIn)
        {
    		double remainingDurationMillis;
        	currentFadeStartTimeVolume = mediaPlayer.getVolume(); 	
   
        	if(newVolumen > 0) 
        	{
        		if(currentFadeTimeline != null) 
            	{
            		remainingDurationMillis = currentFadeTimeline.getTotalDuration().toMillis() - currentFadeTimeline.getCurrentTime().toMillis();
                    currentFadeTimeline.stop();
            	}
            	else 
            	{
            		double tiempoActual = mediaPlayer.getCurrentTime().toSeconds();
                    remainingDurationMillis = 1000 * (fadeInDuracion - tiempoActual);
            	}
            		
                startSmoothFade(Math.max(300, remainingDurationMillis), newVolumen);
        	}
        	else 
        	{
        		if(currentFadeTimeline != null) 
        			currentFadeTimeline.stop();
        	
        		mediaPlayer.setVolume(0);
        	}
        }
        else
        	mediaPlayer.setVolume(newVolumen);
    }

    // Se llama cuando el creadorReproductor hace un seek
    public void onSeek()
    {
    	if(currentFadeTimeline != null) 
    	{
    		System.out.println("esta verga está activa");
    		currentFadeTimeline.stop();
    		currentFadeTimeline = null;
    	}
		
			
    	PauseTransition delay = new PauseTransition(Duration.millis(30)); // el delay mínimo que funcione bien
    	
    	delay.setOnFinished(e -> {
            double tiempoActual = mediaPlayer.getCurrentTime().toSeconds();
            
            boolean dentroDelFadeIn = tiempoActual < fadeInDuracion;
            if (dentroDelFadeIn) {
                // volumen que debería tener si el fade hubiera sido continuo
                currentFadeStartTimeVolume = volumenTarget * (tiempoActual / fadeInDuracion);
                double remainingDurationMillis = 1000 * (fadeInDuracion - tiempoActual);
                
                startSmoothFade(Math.max(300, remainingDurationMillis), volumenTarget);
            }
            else
            	mediaPlayer.setVolume(volumenTarget);
        });
    	
        delay.play();
    }

    public void stopFading()
    {
        if (currentFadeTimeline != null)
        {
            currentFadeTimeline.stop();
            currentFadeTimeline = null;
        }
    }
}