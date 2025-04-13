package modelo;
import java.io.File;

import archivosSoloLectura.datosSonidoLectura;
import javafx.scene.media.*;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

public class creadorReproductor {
	
	MediaPlayer mediaPlayer;
	reproductorSonido reproductor;
	String nombreArchivo;
	double volumen,fadeInDuracion,fadeOutDuracion;
	boolean loop;
	private FadeIn fadeIn;
	private FadeOut fadeOut;
	private boolean fadeOutIniciado;
	
	public creadorReproductor(datosSonidoLectura datosLectura,reproductorSonido reproductor) 
	{
		setearVariables(datosLectura);
		crearMediaPlayer(datosLectura.getRutaArchivoAudio(),reproductor);
	}
	
	private void setearVariables(datosSonidoLectura datosLectura) {
		fadeOutIniciado = false;
		nombreArchivo = datosLectura.getNombreArchivo();
		loop = datosLectura.getLoop();
		volumen = datosLectura.getVolumen(); // la escala es de 0 a 1
		fadeInDuracion = datosLectura.getFadeIn();
		fadeOutDuracion = datosLectura.getFadeOut();
	}
	
	private void crearMediaPlayer(String ruta,reproductorSonido reproductor) {
		this.reproductor = reproductor;
		File file = new File(ruta);
	    Media sonido = new Media(file.toURI().toString());
	    
	    mediaPlayer = new MediaPlayer(sonido);
	    //mediaPlayer.setCycleCount(loop ? MediaPlayer.INDEFINITE : 1);
	    agregarListenerMedia();
	    mediaPlayer.setVolume(volumen);
	    
	    if(fadeInDuracion > 0)
	    	fadeIn = new FadeIn(mediaPlayer,fadeInDuracion,volumen,() -> {
	    	    fadeIn = null;
	    	});
	     
	    mediaPlayer.play();
	}
	
	private void agregarListenerMedia() 
	{
		mediaPlayer.setOnEndOfMedia(() -> avisarFinReproduccion());
		mediaPlayer.currentTimeProperty().addListener((obs, oldTime, newTime)-> checkAvance(newTime));
	}
	
	private void checkAvance(Duration newTime) 
	{
		checkBarraAvance(newTime);
		checkFadeOut(newTime);
		
	}
	
	private void checkBarraAvance(Duration newTime) {
		reproductor.notificarAvanceCancion(nombreArchivo, newTime.toSeconds());
	}
	
	private void checkFadeOut(Duration newTime) {
		
		if(fadeOutDuracion > 0 && !fadeOutIniciado) 
		{
			Duration total = mediaPlayer.getTotalDuration();
            Duration restante = total.subtract(newTime);
     
            if (restante.toSeconds() <= fadeOutDuracion) {
            	
            	if(fadeIn != null) 
            	{
            		fadeIn.pararFadeIn();
            		fadeIn = null;
            	}
            	
            	fadeOutIniciado = true;
                fadeOut = new FadeOut(mediaPlayer, fadeOutDuracion, mediaPlayer.getVolume(), () -> {
                    fadeOut = null;
                });
            }
		}
	}
	
	private void avisarFinReproduccion() 
	{
		if(!loop)
			reproductor.terminarReproduccion(nombreArchivo);
		else 
		{
			fadeOutIniciado = false;
			mediaPlayer.seek(Duration.ZERO);
	        mediaPlayer.play();
	       
			if(fadeInDuracion > 0)
		    	fadeIn = new FadeIn(mediaPlayer,fadeInDuracion,volumen,() -> {
		    	fadeIn = null;
		    });
		} 
	}
	
	
	public void pararReproduccion() 
	{
		mediaPlayer.stop();
		
		if(fadeIn != null) 
		{
			fadeIn.pararFadeIn();
			fadeIn = null;
		}
		
		if (fadeOut != null) {
	        fadeOut.pararFadeOut();
	        fadeOut = null;
	    }

	    fadeOutIniciado = false;
	}
	
	public void setVolumen(double volumen) 
	{
		this.volumen = volumen;
		if(fadeIn != null)
			fadeIn.setVolumenActual(volumen);
		else if(fadeOut != null)
			fadeOut.setVolumenActual(volumen);
		else
			mediaPlayer.setVolume(volumen);
	}
	
	public void setLoop(boolean loop) 
	{
		this.loop = loop;
		mediaPlayer.setCycleCount(loop ? MediaPlayer.INDEFINITE : 1);
	}
	
	public MediaPlayer getMediaPlayer() 
	{
		return mediaPlayer;
	}
	
	public void actualizarAudio(float segundos) 
	{
		mediaPlayer.seek(Duration.seconds(segundos));
	}
}
