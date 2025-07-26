package modelo;

import java.io.*;
import java.util.Properties;

public class ConfigManager {

    private static final String ARCHIVO_CONFIG = "config.properties";
    private static final String CLAVE_ULTIMA_RUTA = "ultimaRutaXML";

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
}

