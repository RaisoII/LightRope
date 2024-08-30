package controlador;
import vista.vista;

import java.nio.file.Path;
import java.nio.file.Paths;

import archivosSoloLectura.datosSonidoLectura;
import modelo.reproductorSonido;


public class controlador {
	
	private reproductorSonido reproducirSonido;
	private vista vista;
	
	public controlador(vista vista) 
	{
		this.vista = vista;
		reproducirSonido = new reproductorSonido();
		reproducirSonido.addObserver(vista);
		setearListenersVista();
	}
	
	private void setearListenersVista() 
	{
        // Listener para el ítem "Open"
        vista.agregarListenerMenuItemOpen(e -> seleccionarArchivos());
	}
	
	private void seleccionarArchivos() {
        // Usar el método en la vista para seleccionar archivos
        String[] archivosSeleccionados = vista.seleccionarArchivos();

        if (archivosSeleccionados != null) {
            for (String ruta : archivosSeleccionados) {
                // Extraer el nombre de la canción del archivo
                String nombreCancion = extraerNombreCancion(ruta);
                // Agregar un botón para cada archivo seleccionado
                vista.agregarBoton(ruta, nombreCancion, e -> manejarReproduccion(ruta, nombreCancion));
            }
        }
    }
	
	  private String extraerNombreCancion(String ruta) {
		  // Convertir la ruta a un objeto Path y obtener el nombre
		    Path path = Paths.get(ruta);
		    return path.getFileName().toString();
	  }

    private void manejarReproduccion(String rutaArchivo, String nombreArchivo) {
       
    	boolean reproduciendo = vista.getEstadoBoton(nombreArchivo);
    	 vista.colorearBotonReproduccion(nombreArchivo,!reproduciendo);
         System.out.println(reproduciendo);
         if (reproduciendo) 
        	 reproducirSonido.detenerSonido(nombreArchivo);
         else 
         {
         	datosSonidoLectura datos = vista.getDatosSonido(nombreArchivo);
         	ejecutarReproduccion(datos);
         }
     }
    
    private void ejecutarReproduccion(datosSonidoLectura datos) 
    {
    	reproducirSonido.reproducirSonido(datos);
    }
    
    public void setVolumenReproduccion(String nombreArchivo,double volumen) 
    {
    	reproducirSonido.setVolumen(nombreArchivo,volumen);
    }
    
    public void setLoopReproduccion(String nombreArchivo,boolean loop) 
    {
    	reproducirSonido.setLoop(nombreArchivo,loop);
    }
}
