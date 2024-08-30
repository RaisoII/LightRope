package modelo;
import interfacesObserver.*;
import java.util.ArrayList;
import java.util.List;

public abstract class reproductor {
	
	private List<interfaceVistaReproductorObserver> observers = new ArrayList<>();
	
    public void addObserver(interfaceVistaReproductorObserver observer) {
        observers.add(observer);
    }

    public void removeObserver(interfaceVistaReproductorObserver observer) {
        observers.remove(observer);
    }

    protected void notificarReproduccionTerminada(String nombreCancion) {
        for (interfaceVistaReproductorObserver observer : observers) {
            observer.onReproduccionTerminada(nombreCancion); 
        }
    }

    public abstract void reproducirSonido(String archivo,String nombreArchivo, boolean loop);
    public abstract void detenerSonido(String archivo);

}
