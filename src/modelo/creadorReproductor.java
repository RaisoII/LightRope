package modelo;
import java.io.File;

import archivosSoloLectura.datosSonidoLectura;
import javafx.scene.media.*;
import javafx.scene.media.MediaPlayer;

public class creadorReproductor {
	
	MediaPlayer mediaPlayer;
	reproductorSonido reproductor;
	String nombreArchivo;
	double volumen,fadeInDuracion,fadeOutDuracion;
	boolean loop;
	private FadeIn fadeIn;
	
	
	public creadorReproductor(datosSonidoLectura datosLectura,reproductorSonido reproductor) 
	{
		nombreArchivo = datosLectura.getNombreArchivo();
		loop = datosLectura.getLoop();
		volumen = datosLectura.getVolumen(); // la escala es de 0 a 1
		fadeInDuracion = datosLectura.getFadeIn();
		fadeOutDuracion = datosLectura.getFadeOut();
		this.reproductor = reproductor;
		File file = new File(datosLectura.getRutaArchivoAudio());
	    Media sonido = new Media(file.toURI().toString());
	    
	    mediaPlayer = new MediaPlayer(sonido);
	    mediaPlayer.setCycleCount(loop ? MediaPlayer.INDEFINITE : 1);
	    agregarListenerMedia();
	    mediaPlayer.setVolume(volumen);
	    if(fadeInDuracion > 0)
	    	fadeIn = new FadeIn(mediaPlayer,fadeInDuracion,volumen);
	     
	    mediaPlayer.play();
	}
	
	private void agregarListenerMedia() 
	{
		mediaPlayer.setOnEndOfMedia(() -> avisarFinReproduccion());
	}
	
	private void avisarFinReproduccion() 
	{
		if(!loop)
			reproductor.terminarReproduccion(nombreArchivo);
	}
	
	public void pararReproduccion() 
	{
		mediaPlayer.stop();
		if(fadeInActivo())
			fadeIn.pararFadeIn();
	}
	
	public void setVolumen(double volumen) 
	{
		this.volumen = volumen;
		
		if(fadeInActivo()) 
		{
			fadeIn.setVolumenActual(volumen);
		}
		else
			mediaPlayer.setVolume(volumen);	
	}
	
	public void setLoop(boolean loop) 
	{
		this.loop = loop;
		mediaPlayer.setCycleCount(loop ? MediaPlayer.INDEFINITE : 1);
	}
	
	private boolean fadeInActivo() 
	{
		return mediaPlayer.getCurrentTime().toSeconds() < fadeInDuracion;
	}
}
