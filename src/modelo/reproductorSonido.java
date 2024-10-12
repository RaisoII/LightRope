package modelo;
import archivosSoloLectura.datosSonidoLectura;

import java.util.HashMap;
import java.util.Map;



public class reproductorSonido  extends reproductor{
	
	Map<String,creadorReproductor> creadoresSonido = new HashMap<String,creadorReproductor>();
	
	@Override
	public void reproducirSonido(datosSonidoLectura datosLectura)
	{
		
		creadorReproductor creador = new creadorReproductor(datosLectura,this);
		creadoresSonido.put(datosLectura.getNombreArchivo(), creador);		
	}
	
	public void terminarReproduccion(String nombreArchivo) 
	{
		detenerSonido(nombreArchivo);
		notificarReproduccionTerminada(nombreArchivo);
	}

	@Override
	public void detenerSonido(String archivo)
	{
		creadorReproductor creador = creadoresSonido.get(archivo);
		creador.pararReproduccion();
		creadoresSonido.remove(archivo);
	}

	@Override
	public void setVolumen(String archivo,double valorVolumen) {
	
		creadorReproductor creador = creadoresSonido.get(archivo);
		if(creador != null) 
			creador.setVolumen(valorVolumen);
	}

	@Override
	public void setLoop(String archivo,boolean loop) {
	
		creadorReproductor creador = creadoresSonido.get(archivo);
		if(creador != null) 
			creador.setLoop(loop);
	
	}
	
	// para la edicion en tiempo Real
	public creadorReproductor getCreadorSonido(String nombreArchivo) 
	{
		creadorReproductor creador = creadoresSonido.get(nombreArchivo);
		return creador;
	}
}
