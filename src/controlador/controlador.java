package controlador;

import vista.vista;
import archivosSoloLectura.datosSonidoLectura;
import interfacesObserver.interfaceReproductorListener;
import modelo.ConfigManager;
import modelo.XMLManager;
import modelo.reproductorSonido;

import java.util.List;
import java.util.function.Consumer;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.File;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import javax.sound.sampled.*;

public class controlador {
	
	private reproductorSonido reproducirSonido;
	private vista vista;
	private int idBoton;
	
	public controlador(vista vista) 
	{
		idBoton = 0;
		this.vista = vista;
		reproducirSonido = new reproductorSonido();
		setObserver(vista);
		setearListenersVista();
		setearListenerSave();
		setearListenerLoad();
		cargarArchivosUltimaRuta();
		cargarTags();
	}
	
	private void setearListenersVista() 
	{
        // Listener para el ítem "Open"
        vista.agregarListenerMenuItemOpen(e -> seleccionarArchivos());
	}
	
	private void setearListenerSave() 
	{
		vista.agregarListenerMenuItemSave(e -> {
		    String ruta = vista.seleccionarRutaGuardado();
		    if (ruta != null) 
		    {
		    	XMLManager.saveXML(vista.getDatosSonidos(), ruta);
		    }
		});
	}
	
	private void setearListenerLoad() 
	{
		ConfigManager config = new ConfigManager();
		
		vista.agregarListenerMenuItemLoad(e -> {
		    String ruta = vista.seleccionarArchivoXML(); // este método está en vista
		    if (ruta != null) 
		    {
		    	idBoton = 0;
				vista.borrarTodosLosBotones();
		    	crearBotonesCarga(ruta);
		    	config.setUltimaRutaXML(ruta);
		    }
		});
	}
	
	private void cargarArchivosUltimaRuta() 
	{
		ConfigManager config = new ConfigManager();
		String ruta = config.getUltimaRutaXML();

		if (ruta != null && new File(ruta).exists())
		{
			vista.borrarTodosLosBotones();
			crearBotonesCarga(ruta);
		}
	}
	
	public void guardarTags(List<String> listaTags) 
	{
		ConfigManager config = new ConfigManager();
		config.guardarTags(listaTags);
	}
	
	public void cargarTags() 
	{
		ConfigManager config = new ConfigManager();
		List<String> listaTags = config.obtenerTags();
		vista.setTagsGlobales(listaTags);
	}
	
	private void crearBotonesCarga(String ruta) 
	{
		List<datosSonidoLectura> datosCargados = XMLManager.loadXML(ruta);
        for (datosSonidoLectura datos : datosCargados) {
        	
        	final int idBotonEstatico = idBoton;
            
        	vista.agregarBoton(
                datos.getRutaArchivoAudio(),
                datos.getNombreArchivo(),
                idBotonEstatico,
                datos.getDuracion(),
                event -> manejarReproduccion(datos.getRutaArchivoAudio(),idBotonEstatico)
            );
            
        	vista.setImagen(idBotonEstatico,datos.getRutaImagen());
            vista.setVolumen(idBotonEstatico, datos.getVolumen());
            vista.setFadeIn(idBotonEstatico, datos.getFadeIn());
            vista.setFadeOut(idBotonEstatico, datos.getFadeOut());
            vista.setLoop(idBotonEstatico, datos.getLoop());
            vista.setTagsBoton(idBotonEstatico, datos.getTags());
            
            idBoton++;
        }
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
		final int idBotonEstatico = idBoton;
		if (extension.equals("mp3")) {
	        duracionMP3(ruta, duracionFinal -> {
	            vista.agregarBoton(ruta, nombreCancion,idBotonEstatico, duracionFinal.floatValue(), e -> manejarReproduccion(ruta, idBotonEstatico));
	        });
	    }
		else {
	         duracion = duracionWAV(ruta);
	         vista.agregarBoton(ruta, nombreCancion,idBotonEstatico, duracion, e -> manejarReproduccion(ruta, idBotonEstatico));
	    }
		
		idBoton++;
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

    private void manejarReproduccion(String rutaArchivo, int idBoton) {
       
    	boolean reproduciendo = vista.getEstadoBoton(idBoton);
    	 vista.colorearBotonReproduccion(idBoton,!reproduciendo);
    
    	 if (reproduciendo) 
        	 reproducirSonido.detenerSonido(idBoton);
         else 
         {
         	datosSonidoLectura datos = vista.getDatosSonido(idBoton);
         	ejecutarReproduccion(datos);
         }
     }
    
    public void actualizarAudio(datosSonidoLectura datos, float segundos) {
    	reproducirSonido.actualizarAudio(datos,segundos);
    }
    
    public void borrarReproduccion(int idBoton) 
    {
   	 	reproducirSonido.detenerSonido(idBoton);
    }
    
    private void ejecutarReproduccion(datosSonidoLectura datos) 
    {
    	reproducirSonido.reproducirSonido(datos);
    }
    
    public void setVolumenReproduccion(int idBoton,double volumen) 
    {
    	reproducirSonido.setVolumen(idBoton,volumen);
    }
    
    public void setLoopReproduccion(int idBoton,boolean loop) 
    {
    	reproducirSonido.setLoop(idBoton,loop);
    }
    
    public void setObserver(interfaceReproductorListener objeto) 
    {
    	reproducirSonido.addObserver(objeto);
    }
    
    public void deleteObserver(interfaceReproductorListener objeto) 
    {
    	reproducirSonido.removeObserver(objeto);
    }
}
