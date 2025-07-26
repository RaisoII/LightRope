package modelo;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import javafx.animation.PauseTransition;

public class AudioFader {

    private final MediaPlayer mediaPlayer;
    private Timeline currentFadeTimeline;
    private double volumenTarget; 
    private double fadeInDuracion; // [s]
    private double fadeOutDuracion; // [s]

    // Variables de estado del fadeIn actual
    private double currentFadeStartTimeVolume;
    private double currentFadeTargetVolume;
    
    //variable de estado del FadeOut
    private PauseTransition fadeOutFinalDelay;


    public AudioFader(MediaPlayer mediaPlayer, double volumenTarget, double fadeInDuracion, double fadeOutDuracion) {
        this.mediaPlayer = mediaPlayer;
        this.volumenTarget = volumenTarget;
        this.fadeInDuracion = fadeInDuracion;
        this.fadeOutDuracion = fadeOutDuracion;
        iniciarFader();
    }
    
    public void iniciarFader() 
    {
    	if (this.fadeInDuracion > 0) {
            this.mediaPlayer.setVolume(0);
            startSmoothFade(this.fadeInDuracion * 1000, volumenTarget); // true indica que es un fade-in
        }
        else
            this.mediaPlayer.setVolume(volumenTarget);
    
        mediaPlayer.setOnReady(() -> {
            programarFadeOutFinal();
        });
    }
    
    private void programarFadeOutFinal() {
        
    	if (fadeOutFinalDelay != null)
    	{
            fadeOutFinalDelay.stop();
            fadeOutFinalDelay = null;
        }

        if (fadeOutDuracion <= 0) return;

        Duration total = mediaPlayer.getTotalDuration();
        Duration actual = mediaPlayer.getCurrentTime();

        double remainingMillis = total.toMillis() - actual.toMillis() - (fadeOutDuracion * 1000);
        
        if (remainingMillis <= 0)
        {
        	double tiempoRestante = total.toMillis() - actual.toMillis();
            currentFadeStartTimeVolume = volumenTarget * (tiempoRestante / (fadeOutDuracion * 1000));
            currentFadeStartTimeVolume = Math.max(0, Math.min(currentFadeStartTimeVolume, volumenTarget)); // Clamp

            mediaPlayer.setVolume(currentFadeStartTimeVolume);

            System.out.println("[FadeOut] Ya estamos dentro del fade-out. Ejecutando desde ahora hasta el final.");
            crearFadeOut(0); 
            return;
        }
        
        crearFadeOut(remainingMillis);
    }

    private void crearFadeOut(double remainingMillis) 
    {
    	fadeOutFinalDelay = new PauseTransition(Duration.millis(remainingMillis));
        fadeOutFinalDelay.setOnFinished(e -> {
            if (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                System.out.println("[FadeOut] Ejecutando fade-out final (tras seek)");
                currentFadeStartTimeVolume = mediaPlayer.getVolume();
                startSmoothFade(fadeOutDuracion * 1000, 0);
            }
        });
        
        fadeOutFinalDelay.play();
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
    public void setVolumenTarget(double newVolumen)
    {
       
    	this.volumenTarget = newVolumen; // Actualizar el volumen objetivo final
    	
    	boolean dentroDelFadeIn = mediaPlayer.getCurrentTime().toSeconds() < fadeInDuracion;
    	// LOGICA PARA EL FADEIN
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
    	else // NO ESTOY EN EL FADEIN
    	{
    	    double tiempoActual = mediaPlayer.getCurrentTime().toSeconds();
    	    double tiempoRestante = mediaPlayer.getTotalDuration().toSeconds() - tiempoActual;

    	    boolean dentroDelFadeOut = tiempoRestante < fadeOutDuracion;
    	  
    	    // LOGICA PARA EL FADEOUT
    	    if (dentroDelFadeOut)
    	    {
    	    	
    	    	if (fadeOutFinalDelay != null)
	       	 	{
	       		 	fadeOutFinalDelay.stop();
	       		 	fadeOutFinalDelay = null;	
	       	 	}

    	    	mediaPlayer.setVolume(newVolumen);
    	    	
    	        // Si el volumen nuevo es mayor a 0, hacemos un fade desde el volumen actual
    	        if (newVolumen > 0) 
    	        {
    	        	 Duration total = mediaPlayer.getTotalDuration();
    	        	    Duration actual = mediaPlayer.getCurrentTime();

    	        	    double tiempoTotalFadeOutMillis = fadeOutDuracion * 1000;
    	        	    double tiempoRestanteMillis = total.toMillis() - actual.toMillis();
    	        	    double tiempoYaConsumido = tiempoTotalFadeOutMillis - tiempoRestanteMillis;

    	        	    // Clamp entre 0 y 1
    	        	    double progreso = Math.max(0, Math.min(1, tiempoYaConsumido / tiempoTotalFadeOutMillis));

    	        	    // Calcular el volumen actual simulado del fade desde newVolumen
    	        	    double volumenInterpolado = newVolumen * (1 - progreso);
    	        	    mediaPlayer.setVolume(volumenInterpolado);
    	        	    currentFadeStartTimeVolume = volumenInterpolado;

    	        	    crearFadeOut(tiempoRestanteMillis);
    	        }
    	        else 
    	        {
    	        	stopFading();
    	        	mediaPlayer.setVolume(0);
    	        }   	        	
    	        
    	        return;
    	    }

    	    // FUERA DEL FADEIN y OUT
    	    mediaPlayer.setVolume(newVolumen);
    	}
    }

    // Se llama cuando el creadorReproductor hace un seek
    public void onSeek()
    {
    	if(currentFadeTimeline != null) 
    	{
    		currentFadeTimeline.stop();
    		currentFadeTimeline = null;
    	}
    	
    	 if (fadeOutFinalDelay != null)
    	 {
    		 fadeOutFinalDelay.stop();
    	     fadeOutFinalDelay = null;
    	 }
		
			
    	PauseTransition delay = new PauseTransition(Duration.millis(100)); // el delay mínimo que funcione bien
    	
    	delay.setOnFinished(e -> {
            double tiempoActual = mediaPlayer.getCurrentTime().toSeconds();
            
            boolean dentroDelFadeIn = tiempoActual < fadeInDuracion;
            if (dentroDelFadeIn)
            {
                // volumen que debería tener si el fade hubiera sido continuo
                currentFadeStartTimeVolume = volumenTarget * (tiempoActual / fadeInDuracion);
                double remainingDurationMillis = 1000 * (fadeInDuracion - tiempoActual);
                
                startSmoothFade(Math.max(300, remainingDurationMillis), volumenTarget);
            }
            else  
            {
            	double tiempoRestante = mediaPlayer.getTotalDuration().toSeconds() - tiempoActual;
                boolean dentroDelFadeOut = tiempoRestante < fadeOutDuracion;

                if (dentroDelFadeOut)
                {
                    currentFadeStartTimeVolume = volumenTarget * (tiempoRestante / fadeOutDuracion);
                    mediaPlayer.setVolume(currentFadeStartTimeVolume);
                } else
                    mediaPlayer.setVolume(volumenTarget);
                
                programarFadeOutFinal();
            }
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