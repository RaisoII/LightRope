package modelo;

import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.File;

import archivosSoloLectura.datosSonidoLectura;

public class XMLManager {
	
	public static void saveXML(List<datosSonidoLectura> datosGuardar,String ruta)
	{
		try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElement("sonidos");
            doc.appendChild(rootElement);

            for (datosSonidoLectura datos : datosGuardar) {
                Element sonido = doc.createElement("sonido");

                Element nombre = doc.createElement("nombre");
                nombre.setTextContent(datos.getNombreArchivo());

                Element rutaArchivo = doc.createElement("ruta");
                rutaArchivo.setTextContent(datos.getRutaArchivoAudio());
                
                Element volumen = doc.createElement("volumen");
                volumen.setTextContent(String.valueOf(datos.getVolumen()));
                
                Element duracion = doc.createElement("duracion");
                duracion.setTextContent(String.valueOf(datos.getDuracion()));
                
                Element loop = doc.createElement("loop");
                loop.setTextContent(String.valueOf(datos.getLoop()));
                
                Element fadeIn = doc.createElement("fadeIn");
                fadeIn.setTextContent(String.valueOf(datos.getFadeIn()));
                
                Element fadeOut = doc.createElement("fadeOut");
                fadeOut.setTextContent(String.valueOf(datos.getFadeOut()));
                
                sonido.appendChild(nombre);
                sonido.appendChild(rutaArchivo);
                sonido.appendChild(volumen);
                sonido.appendChild(duracion);
                sonido.appendChild(loop);
                sonido.appendChild(fadeIn);
                sonido.appendChild(fadeOut);
                
                rootElement.appendChild(sonido);
            }

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");

            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(ruta));

            transformer.transform(source, result);

            System.out.println("Archivo XML guardado en: " + ruta);

        } catch (Exception e) {
            e.printStackTrace();
        }
	}
	
	public static List<datosSonidoLectura> loadXML(String ruta) {
        List<datosSonidoLectura> datosCargados = new ArrayList<>();

        try {
            File archivo = new File(ruta);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(archivo);
            doc.getDocumentElement().normalize();

            NodeList listaSonidos = doc.getElementsByTagName("sonido");

            for (int i = 0; i < listaSonidos.getLength(); i++) {
                Element elemSonido = (Element) listaSonidos.item(i);

                String nombre = elemSonido.getElementsByTagName("nombre").item(0).getTextContent();
                String rutaArchivo = elemSonido.getElementsByTagName("ruta").item(0).getTextContent();
                float volumen = Float.parseFloat(elemSonido.getElementsByTagName("volumen").item(0).getTextContent());
                float duracion = Float.parseFloat(elemSonido.getElementsByTagName("duracion").item(0).getTextContent());
                boolean loop = Boolean.parseBoolean(elemSonido.getElementsByTagName("loop").item(0).getTextContent());
                float fadeIn = Float.parseFloat(elemSonido.getElementsByTagName("fadeIn").item(0).getTextContent());
                float fadeOut = Float.parseFloat(elemSonido.getElementsByTagName("fadeOut").item(0).getTextContent());
          
                datosSonidoLectura datos = new datosSonidoLectura(rutaArchivo,nombre,volumen,duracion,fadeIn,fadeOut,loop);
                datosCargados.add(datos);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return datosCargados;
    }
}
