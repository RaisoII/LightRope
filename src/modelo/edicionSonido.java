package modelo;

import java.util.HashMap;
import java.util.Map;

import archivosSoloLectura.datosSonidoLectura;
import interfacesObserver.interfaceReproductorListener;

public class edicionSonido implements interfaceReproductorListener {
	
	private reproductorSonido reproductor;
	private Map<String,edicionTiempoReal> creadores = new HashMap<String,edicionTiempoReal>();
	
	// solo invocado cuando se abre la ventana de edición
	public void analizarSonido (datosSonidoLectura datos) 
	{
		String nombreArchivo =  datos.getNombreArchivo();
		creadorReproductor creador = reproductor.getCreadorSonido(nombreArchivo);
		
		if(creador != null)
		{
			edicionTiempoReal  edicion = new edicionTiempoReal(creador);
			creadores.put(nombreArchivo,edicion);
		}
	}
	
	public void pararReproduccion(String nombreArchivo) 
	{
		edicionTiempoReal  edicion = creadores.get(nombreArchivo);
		edicion.pararEdicion();
	}
	
	public void setReproductor(reproductorSonido reproductor) 
	{
		this.reproductor = reproductor;
	}
	
	public void onReproduccionTerminada(String nombreCancion)
	{
		edicionTiempoReal  edicion = creadores.get(nombreCancion);
		if(edicion != null)
			edicion.pararEdicion();
	}
	
}
