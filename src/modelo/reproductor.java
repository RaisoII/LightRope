package modelo;
import interfacesObserver.*;
import archivosSoloLectura.datosSonidoLectura;
import java.util.ArrayList;
import java.util.List;

public abstract class reproductor {
	
	private List<interfaceReproductorListener> observers = new ArrayList<>();
	
    public void addObserver(interfaceReproductorListener observer) {
    	// esto pasa porque la ventanaEdicion puede suscribirse muchas veces si se clickea rapido
    	if(!observers.contains(observer))
    		observers.add(observer);
    }

    public void removeObserver(interfaceReproductorListener observer) {
        observers.remove(observer);
    }

    protected void notificarReproduccionTerminada(int idBoton) {
        for (interfaceReproductorListener observer : observers) {
            observer.onReproduccionTerminada(idBoton); 
        }
    }
    
    protected void notificarAvanceCancion(int idBoton, double avance) 
    {
    	for (interfaceReproductorListener observer : observers) {
    		observer.avanceReproduccion(idBoton,avance); 
        }
    }

    public abstract void reproducirSonido(datosSonidoLectura datos);
    
    public abstract void detenerSonido(int idBoton);
    
    public abstract void setVolumen(int idBoton,double valorVolumen);
    
    public abstract void setLoop(int idBoton,boolean loop);
    
    public abstract void actualizarAudio(datosSonidoLectura datos,float segundos);

}
