package vista;

public class datosSonidoLectura {
 	
	private String rutaArchivoAudio;
	private String nombreArchivo;
	private boolean loop;

	public datosSonidoLectura(String rutaArchivoAudio, String nombreArchivo, boolean loop) {
		this.rutaArchivoAudio = rutaArchivoAudio;
	    this.nombreArchivo = nombreArchivo;
	    this.loop = loop;
	}

	public String getRutaArchivoAudio() {
		return rutaArchivoAudio;
	}

	public String getNombreArchivo() {
		return nombreArchivo;
	}

	public boolean getLoop() {
		return loop;
	}
}
