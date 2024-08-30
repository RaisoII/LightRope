package vista;

import javafx.application.Application;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class botonSonido {
	
	private String rutaArchivoAudio;
	private String nombreArchivo;
	private String rutaImagen;
	private Button botonInterfaceAsociado;
	private Stage ventanaEdicion;  // Ventana asociada al botón
	
	private boolean botonApretado;
	// atributos sonidos
	private boolean loop;
	
	
	public botonSonido(String ruta,String nombreArchivo, Button botonAsociado) 
	{
		this.nombreArchivo = nombreArchivo;
		rutaArchivoAudio = ruta;
		botonInterfaceAsociado = botonAsociado;
		loop = false;
		botonApretado = false;
	}
	
	public String getRutaArchivoAudio() 
	{
		return rutaArchivoAudio;
	}
	
	public String getRutaImagen() 
	{
		return rutaImagen;
	}
	
	public Button getBotonAsociado()
	{
		return botonInterfaceAsociado;
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
	
	public void setBotonApretado(boolean estado) 
	{
		botonApretado = estado;
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
	
	 // Método para obtener una versión de solo lectura
    public datosSonidoLectura getDatosLectura() {
        return new datosSonidoLectura(rutaArchivoAudio, nombreArchivo, loop);
    }
}