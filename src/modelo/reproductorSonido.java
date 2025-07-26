package modelo;
import archivosSoloLectura.datosSonidoLectura;

import java.util.HashMap;
import java.util.Map;



public class reproductorSonido  extends reproductor{
	
	Map<Integer,creadorReproductor> creadoresSonido = new HashMap<Integer,creadorReproductor>();
	
	@Override
	public void reproducirSonido(datosSonidoLectura datosLectura)
	{

		System.out.println("activar: "+ datosLectura.getNombreArchivo());
		creadorReproductor creador = new creadorReproductor(datosLectura,this);
		creadoresSonido.put(datosLectura.getIdBoton(), creador);		
	}
	
	public void terminarReproduccion(int idBoton) 
	{
		detenerSonido(idBoton);
		notificarReproduccionTerminada(idBoton);
	}

	@Override
	public void detenerSonido(int idBoton)
	{
		System.out.println("detener: "+idBoton);
		creadorReproductor creador = creadoresSonido.get(idBoton);
		creador.pararReproduccion();
		creadoresSonido.remove(idBoton);
	}

	@Override
	public void setVolumen(int idBoton,double valorVolumen) {
	
		creadorReproductor creador = creadoresSonido.get(idBoton);
		if(creador != null) 
			creador.setVolumen(valorVolumen);
		
	}

	@Override
	public void setLoop(int idBoton,boolean loop) {
	
		creadorReproductor creador = creadoresSonido.get(idBoton);
		if(creador != null) 
			creador.setLoop(loop);
	
	}
	
	// para la edicion en tiempo Real
	public creadorReproductor getCreadorSonido(String nombreArchivo) 
	{
		creadorReproductor creador = creadoresSonido.get(nombreArchivo);
		return creador;
	}

	@Override
	public void actualizarAudio(datosSonidoLectura datos, float segundos) {

		creadorReproductor creador = creadoresSonido.get(datos.getNombreArchivo());
		creador.actualizarAudio(segundos);
	}
}
