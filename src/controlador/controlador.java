package controlador;

import vista.vista;
import archivosSoloLectura.datosSonidoLectura;
import modelo.reproductorSonido;
import modelo.edicionSonido;
import java.util.function.Consumer;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.File;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import javax.sound.sampled.*;

public class controlador {
	
	private reproductorSonido reproducirSonido;
	private edicionSonido edicionSonido;
	private vista vista;
	
	public controlador(vista vista) 
	{
		this.vista = vista;
		reproducirSonido = new reproductorSonido();
		reproducirSonido.addObserver(vista);
		edicionSonido = new edicionSonido();
		edicionSonido.setReproductor(reproducirSonido);
		reproducirSonido.addObserver(edicionSonido);
		setearListenersVista();
	}
	
	private void setearListenersVista() 
	{
        // Listener para el ítem "Open"
        vista.agregarListenerMenuItemOpen(e -> seleccionarArchivos());
	}
	
	private void seleccionarArchivos() {
        // Usar el método en la vista para seleccionar archivos
        String[] archivosSeleccionados = vista.seleccionarArchivos();

        if (archivosSeleccionados != null) {
            for (String ruta : archivosSeleccionados) 
            {
            	crearBotonVista(ruta);
            }
        }
    }
	
	private void  crearBotonVista(String ruta) {
		String extension = buscarExtension(ruta);
		String nombreCancion = extraerNombreCancion(ruta);
		float duracion;
		
		if (extension.equals("mp3")) {
	        duracionMP3(ruta, duracionFinal -> {
	            vista.agregarBoton(ruta, nombreCancion, duracionFinal.floatValue(), e -> manejarReproduccion(ruta, nombreCancion));
	        });
	    }
		else {
	         duracion = duracionWAV(ruta);
	        vista.agregarBoton(ruta, nombreCancion, duracion, e -> manejarReproduccion(ruta, nombreCancion));
	    }
	}
	
	private void duracionMP3(String ruta, Consumer<Double> callback) {
	    Media media = new Media(new File(ruta).toURI().toString());
	    MediaPlayer mediaPlayer = new MediaPlayer(media);

	    mediaPlayer.setOnReady(() -> {
	        double duracion = media.getDuration().toSeconds();
	        mediaPlayer.dispose(); // Liberás recursos
	        callback.accept(duracion);
	    });
	}
	
	private float duracionWAV(String ruta) {
		try 
		{
			File archivo = new File(ruta);
	         AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(archivo);
	         AudioFormat format = audioInputStream.getFormat();

	         long frames = audioInputStream.getFrameLength();
	         float duracion = frames / format.getFrameRate();

	         audioInputStream.close();
	         return duracion;
		}
		catch (Exception e) {
            e.printStackTrace();
            return -1f;
        }
	}
	
	private String extraerNombreCancion(String ruta) {
	  // Convertir la ruta a un objeto Path y obtener el nombre
	    Path path = Paths.get(ruta);
	    return path.getFileName().toString();
	}
	  
	  private String buscarExtension(String ruta) {
		    int punto = ruta.lastIndexOf('.');
		    if (punto > 0 && punto < ruta.length() - 1) {
		        return ruta.substring(punto + 1).toLowerCase(); // Ej: "mp3", "wav"
		    } else {
		        return ""; // No hay extensión
		    }
		}

    private void manejarReproduccion(String rutaArchivo, String nombreArchivo) {
       
    	boolean reproduciendo = vista.getEstadoBoton(nombreArchivo);
    	 vista.colorearBotonReproduccion(nombreArchivo,!reproduciendo);
    
    	 if (reproduciendo) 
        	 reproducirSonido.detenerSonido(nombreArchivo);
         else 
         {
         	datosSonidoLectura datos = vista.getDatosSonido(nombreArchivo);
         	ejecutarReproduccion(datos);
         }
     }
    
    private void ejecutarReproduccion(datosSonidoLectura datos) 
    {
    	reproducirSonido.reproducirSonido(datos);
    }
    
    public void setVolumenReproduccion(String nombreArchivo,double volumen) 
    {
    	reproducirSonido.setVolumen(nombreArchivo,volumen);
    }
    
    public void setLoopReproduccion(String nombreArchivo,boolean loop) 
    {
    	reproducirSonido.setLoop(nombreArchivo,loop);
    }
    
    public void entrarModoEdicion(datosSonidoLectura datos) 
    {
    	edicionSonido.analizarSonido(datos);
    }
}
