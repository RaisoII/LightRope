package modelo;
import java.io.File;

import archivosSoloLectura.datosSonidoLectura;
import javafx.scene.media.*;
import javafx.scene.media.MediaPlayer;

public class creadorReproductor {
	
	MediaPlayer mediaPlayer;
	reproductorSonido reproductor;
	String nombreArchivo;
	double volumen,fadeIn,fadeOut;
	boolean loop;
	
	public creadorReproductor(datosSonidoLectura datosLectura,reproductorSonido reproductor) 
	{
		
		nombreArchivo = datosLectura.getNombreArchivo();
		loop = datosLectura.getLoop();
		volumen = datosLectura.getVolumen(); // la escala es de 0 a 1
		fadeIn = datosLectura.getFadeIn();
		fadeOut = datosLectura.getFadeOut();
		System.out.println("este es el datasoo: "+volumen);
		this.reproductor = reproductor;
		File file = new File(datosLectura.getRutaArchivoAudio());
	    Media sonido = new Media(file.toURI().toString());
	    
	    mediaPlayer = new MediaPlayer(sonido);
	    mediaPlayer.setCycleCount(loop ? MediaPlayer.INDEFINITE : 1);
	    agregarListenerMedia();
	    mediaPlayer.setVolume(volumen);
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
	}
	
	public void setVolumen(double volumen) 
	{
		this.volumen = volumen;
		mediaPlayer.setVolume(volumen);
	}
	
	public void setLoop(boolean loop) 
	{
		this.loop = loop;
		mediaPlayer.setCycleCount(loop ? MediaPlayer.INDEFINITE : 1);
	}
}
