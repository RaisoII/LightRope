package modelo;
import java.io.File;
import java.util.concurrent.atomic.AtomicReference;

import archivosSoloLectura.datosSonidoLectura;
import javafx.scene.media.*;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

public class creadorReproductor {
	
	MediaPlayer mediaPlayer;
	reproductorSonido reproductor;
	int idBoton;
	double volumen,fadeInDuracion,fadeOutDuracion;
	boolean loop;
	private AudioFader fader;
	
	public creadorReproductor(datosSonidoLectura datosLectura,reproductorSonido reproductor) 
	{
		setearVariables(datosLectura);
		crearMediaPlayer(datosLectura.getRutaArchivoAudio(),reproductor);
		crearAudioFader();
	}
	
	private void setearVariables(datosSonidoLectura datosLectura) {
		idBoton = datosLectura.getIdBoton();
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
	     
	    mediaPlayer.play();
	}
	
	private void crearAudioFader() {
		
		fader = new AudioFader(mediaPlayer,volumen,
				fadeInDuracion,
				fadeOutDuracion
		);
	}

	
	private void agregarListenerMedia() 
	{
		mediaPlayer.setOnEndOfMedia(() -> avisarFinReproduccion());
		mediaPlayer.currentTimeProperty().addListener((obs, oldTime, newTime)-> checkAvance(newTime));
	}
	
	private void checkAvance(Duration newTime) 
	{
		checkBarraAvance(newTime);
	}
	
	private void checkBarraAvance(Duration newTime) {
		reproductor.notificarAvanceCancion(idBoton, newTime.toSeconds());
	}
	
	private void avisarFinReproduccion() 
	{
		if(!loop) 
		{
			reproductor.terminarReproduccion(idBoton);
		}
		else 
		{
			mediaPlayer.seek(Duration.ZERO);
			fader.iniciarFader();
			fader.onSeek();
		} 
	}
	
	public void pararReproduccion() 
	{
		mediaPlayer.stop();
		
	}
	
	public void setVolumen(double volumen) 
	{
		this.volumen = volumen;
		fader.setVolumenTarget(volumen);
	}
	
	public void setLoop(boolean loop) 
	{
		this.loop = loop;
	}
	
	public MediaPlayer getMediaPlayer() 
	{
		return mediaPlayer;
	}
	
	public void actualizarAudio(float segundos) 
	{
		mediaPlayer.seek(Duration.seconds(segundos));
		fader.onSeek();
	}
}
