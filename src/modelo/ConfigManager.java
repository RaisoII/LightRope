package modelo;

import java.io.*;
import java.util.List;
import java.util.Properties;

public class ConfigManager {

    private static final String ARCHIVO_CONFIG = "config.properties";
    private static final String CLAVE_ULTIMA_RUTA = "ultimaRutaXML";
    private static final String CLAVE_TAGS = "tags";
    
    private Properties props;

    public ConfigManager() {
        props = new Properties();

        File archivo = new File(ARCHIVO_CONFIG);
        if (archivo.exists()) {
            try (FileInputStream fis = new FileInputStream(archivo)) {
                props.load(fis);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String getUltimaRutaXML() {
        return props.getProperty(CLAVE_ULTIMA_RUTA);
    }

    public void setUltimaRutaXML(String ruta) {
        props.setProperty(CLAVE_ULTIMA_RUTA, ruta);
        guardar();
    }

    private void guardar() {
        try (FileOutputStream fos = new FileOutputStream(ARCHIVO_CONFIG)) {
            props.store(fos, "Configuración de la aplicación");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void guardarTags(List<String> tags)
    {
        if (tags == null || tags.isEmpty()) {
            props.remove(CLAVE_TAGS); // Caso 1 y 2: eliminar si está vacía
        } else {
            String tagsConcatenados = String.join(",", tags); // Caso 3
            props.setProperty(CLAVE_TAGS, tagsConcatenados);
        }
        guardar();
    }
    
    public List<String> obtenerTags()
    {
        String tagsConcatenados = props.getProperty(CLAVE_TAGS);
        if (tagsConcatenados == null || tagsConcatenados.isEmpty()) {
            return List.of(); // lista vacía inmutable
        }
        return List.of(tagsConcatenados.split(","));
    }
}

