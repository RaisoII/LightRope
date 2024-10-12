package modelo;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javafx.scene.media.MediaPlayer;

public class edicionTiempoReal {
	
	private MediaPlayer mediaPlayer;
	private creadorReproductor creador;
	private ScheduledExecutorService scheduler;
	
	public edicionTiempoReal(creadorReproductor creador) 
	{
		this.creador = creador;
		this.mediaPlayer = creador.getMediaPlayer();
		
		scheduler = Executors.newScheduledThreadPool(1);
		 
		Runnable task = new Runnable() {
	          @Override
	          public void run() {
	               controlarReproduccion();
	          }
	    };

	    scheduler.scheduleAtFixedRate(task, 0, 2, TimeUnit.MILLISECONDS);
	}
	
	public void pararEdicion()
	{
		System.out.println("entra parar");
		scheduler.shutdown();
	}
	
	private void controlarReproduccion() 
	{
	}
}
