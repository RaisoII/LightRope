package vista;

import java.util.ArrayList;
import java.util.List;

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
	private List<String> listaTags;
	private Button botonInterfaceAsociado;
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
		listaTags = new ArrayList<String>();
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
		System.out.println("ruta: "+rutaImagen);
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
	
	public void setTag(String tag) 
	{
		if(!listaTags.contains(tag))
			listaTags.add(tag);
	}
	
	public List<String> getTags()
	{
		return listaTags;
	}
	

	 // Método para obtener una versión de solo lectura
    public datosSonidoLectura getDatosLectura() {

    	datosSonidoLectura datos = new datosSonidoLectura(rutaArchivoAudio, nombreArchivo,idBoton,
				volumen,duracion,fadeIn,fadeOut, loop);
    	
    	datos.setRutaImagen(rutaImagen);
    	
    	return datos;
    }
}