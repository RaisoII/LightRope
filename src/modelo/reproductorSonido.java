package modelo;

import java.io.File;
import javafx.util.Duration;  // Asegúrate de usar javafx.util.Duration

import javafx.scene.media.*;
import javafx.scene.media.MediaPlayer;

public class reproductorSonido  extends reproductor{

	private MediaPlayer mediaPlayer;

	@Override
    public void reproducirSonido(String archivo, String nombreArchivo, boolean loop) {
		  // Crear una instancia de Media a partir de la ruta del archivo
		System.out.println(archivo);
		System.out.println("hola perrin");
		File file = new File(archivo);
		
	    Media sonido = new Media(file.toURI().toString());

	    // Crear un reproductor y asignarle el archivo de media
	    MediaPlayer mediaPlayer = new MediaPlayer(sonido);

	    // Configurar el bucle si es necesario
	   // mediaPlayer.setCycleCount(loop ? MediaPlayer.INDEFINITE : 1);

	    // Reproducir el sonido
	    mediaPlayer.play();
    }

	@Override
	public void detenerSonido(String archivo) {
		// TODO Auto-generated method stub	
	}
}
