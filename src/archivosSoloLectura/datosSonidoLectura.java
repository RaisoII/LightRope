package archivosSoloLectura;

public class datosSonidoLectura {
 	
	private String rutaArchivoAudio;
	private String nombreArchivo;
	private boolean loop;
	private double volumen,fadeIn,fadeOut,duracion;
	private int idBoton;

	public datosSonidoLectura(String rutaArchivoAudio, String nombreArchivo,int idBoton, double volumen,
			double duracion, double fadeIn,double fadeOut,boolean loop)
	{
		this.duracion = duracion;
		this.rutaArchivoAudio = rutaArchivoAudio;
	    this.nombreArchivo = nombreArchivo;
	    this.volumen = volumen;
	    this.fadeIn = fadeIn;
	    this.fadeOut = fadeOut;
	    this.loop = loop;
	    this.idBoton = idBoton;
	}

	public String getRutaArchivoAudio()
	{
		return rutaArchivoAudio;
	}

	public String getNombreArchivo()
	{
		return nombreArchivo;
	}

	public boolean getLoop()
	{
		return loop;
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
	
	public double getDuracion() 
	{
		return duracion;
	}
	
	public int getIdBoton() 
	{
		return idBoton;
	}
}
