package modelo;
import interfacesObserver.*;
import archivosSoloLectura.datosSonidoLectura;
import java.util.ArrayList;
import java.util.List;

public abstract class reproductor {
	
	private List<interfaceReproductorListener> observers = new ArrayList<>();
	
    public void addObserver(interfaceReproductorListener observer) {
        observers.add(observer);
    }

    public void removeObserver(interfaceReproductorListener observer) {
        observers.remove(observer);
    }

    protected void notificarReproduccionTerminada(String nombreCancion) {
        for (interfaceReproductorListener observer : observers) {
            observer.onReproduccionTerminada(nombreCancion); 
        }
    }
    
    protected void notificarAvanceCancion(String nombreCancion, double avance) 
    {
    	for (interfaceReproductorListener observer : observers) {
            observer.avanceReproduccion(nombreCancion,avance); 
        }
    }

    public abstract void reproducirSonido(datosSonidoLectura datos);
    
    public abstract void detenerSonido(String archivo);
    
    public abstract void setVolumen(String archivo,double valorVolumen);
    
    public abstract void setLoop(String archivo,boolean loop);

}
