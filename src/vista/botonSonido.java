package vista;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import archivosSoloLectura.datosSonidoLectura;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class botonSonido {
	
	private String rutaArchivoAudio;
	private String nombreArchivo;
	private String rutaImagen;
	private Map<String, Boolean> mapaTags;
	private	Button botonInterfaceAsociado;
	private double duracion;
	private Stage ventanaEdicion;  // Ventana asociada al botón
	private int idBoton;
	private boolean botonApretado;
	// atributos sonidos
	private boolean loop;
	private double volumen,fadeIn,fadeOut; // escala de volumen [0,1]
	private Label labelNombre;
	private VBox contenedor;
	private StackPane bordeBoton;
	
	public botonSonido(String ruta,String nombreArchivo,int idBoton, double duracion, 
						Button botonAsociado, Label labelNombre) 
	{
		mapaTags = new LinkedHashMap<>();
		this.labelNombre = labelNombre;
		this.duracion = duracion; 
		this.nombreArchivo = nombreArchivo;
		this.idBoton = idBoton;
		rutaArchivoAudio = ruta;
		botonInterfaceAsociado = botonAsociado;
		loop = false;
		botonApretado = false;
		volumen = 1;
	}
	
	public void setRutaImagen(String rutaImagen)
	{
		this.rutaImagen = rutaImagen;
	}	
	
	public String getRutaImagen() 
	{
		return rutaImagen;
	}
	
	public Button getBotonAsociado()
	{
		return botonInterfaceAsociado;
	}
	
	public void setBotonApretado(boolean estado) 
	{
		botonApretado = estado;
		
		if(ventanaEdicion != null) 
		{
			ventanaEdicionSonido ventana = (ventanaEdicionSonido)ventanaEdicion;
			ventana.setEnabledSliderProgreso(estado);
		}
	}
	
	public boolean getBotonApretado() 
	{
		return botonApretado;
	}
	
	public Stage getVentanaEdicion() 
	{
		return ventanaEdicion;
	}
	
	public void setVentanaEdicion(Stage nuevaVentana) 
	{
		ventanaEdicion = nuevaVentana;
	}
	
	// setters y getters atributos audio

	public void setNombreLabel(String nombreArchivo)
	{
		labelNombre.setText(nombreArchivo);
	}
	
	public void setNombreArchivo(String nombreArchivo) 
	{
		this.nombreArchivo = nombreArchivo;
	}
	
	public String getNombreArchivo() 
	{
		return nombreArchivo;
	}
	
	public void setLoop(boolean loop) 
	{
		this.loop = loop;
	}
	
	public boolean getLoop() 
	{
		return loop;
	}
	
	public String getRutaArchivoAudio() 
	{
		return rutaArchivoAudio;
	}
	
	public void setFadeIn(double valor) 
	{
		fadeIn = valor;
	}
	
	public void setFadeOut(double valor) 
	{
		fadeOut = valor;
	}
	
	public double getFadeIn() 
	{
		return fadeIn;
	}
	
	public double getFadeOut() 
	{
		return fadeOut;
	}
	
	public double getVolumen() 
	{
		return volumen;
	}
	
	public void setVolumen(double volumen) 
	{
		this.volumen = volumen;
	} 
	
	public double getDuracion() 
	{
		return duracion;
	}
	
	public int getIdBoton() 
	{
		return idBoton;
	}
	
	public void setContenedor(VBox contenedor) 
	{
		this.contenedor = contenedor;
	}
	
	public VBox getContenedor() 
	{
		return contenedor;
	}
	
	public void setBordeBoton(StackPane borde) 
	{
		bordeBoton = borde;
	}
	
	public StackPane getBordeBoton() 
	{
		return bordeBoton;
	}
	
	public void setListaTagVentana(List<String> listaTags, List<String> tagsGlobales) {
	    for (String tag : listaTags) {
	        mapaTags.put(tag, true); // Activa o agrega como true
	    }

	    for (String tagGlobal : tagsGlobales) {
	        if (!listaTags.contains(tagGlobal)) {
	            mapaTags.put(tagGlobal, false); // Desactiva si no está en listaTags
	        }
	    }
	}

	
	// idea general: cargo los tags de los botones.
	// si quedan en true significa que están en uso y son tagsGlobales
	// si quedan en false significa que si son tags del boton pero no están en los tagsGlobales
	public void setListaTagInicial(List<String> tagsBoton,Set<String> tagsGlobales)
	{
		for(String tag : tagsBoton) 
		{
			mapaTags.put(tag, true);
		}
		
		List<String> listaTagsGlobales = new ArrayList<>(tagsGlobales);
		
		checkearTags(listaTagsGlobales);
		
	}
	
	public List<String> getTags() 
	{
	    
	    return getListaTags();
	}
	
	private List<String> getListaTags()
	{
		List<String> listaTags = new ArrayList<>();
	    
		for (Map.Entry<String, Boolean> entry : mapaTags.entrySet()) {
	        if (Boolean.TRUE.equals(entry.getValue())) 
	        {
	            listaTags.add(entry.getKey());
	        }
	    }
		
		return listaTags;
	}

	 // Método para obtener una versión de solo lectura
    public datosSonidoLectura getDatosLectura()
    {
    	datosSonidoLectura datos = new datosSonidoLectura(rutaArchivoAudio, nombreArchivo,idBoton,
				volumen,duracion,fadeIn,fadeOut, loop);
    	
    	datos.setRutaImagen(rutaImagen);
    	List<String> listaTags = getListaTags();
    	
    	datos.setListaTags(listaTags);
    	
    	return datos;
    }
    
    public void checkearTags(List<String> tagsGlobales) 
    {
        // Agregar nuevos tags globales como desactivados si el botón no los conocía
        for (String tag : tagsGlobales) {
            mapaTags.putIfAbsent(tag, false);
        }
    }
    
    public void borrarTagBoton(String tag) 
    {
    	mapaTags.remove(tag);
    }
}